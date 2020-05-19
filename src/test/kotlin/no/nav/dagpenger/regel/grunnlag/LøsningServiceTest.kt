package no.nav.dagpenger.regel.grunnlag

import com.fasterxml.jackson.databind.JsonNode
import de.huxhorn.sulky.ulid.ULID
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import java.math.BigDecimal
import java.time.YearMonth
import kotlinx.coroutines.runBlocking
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import no.nav.dagpenger.inntekt.rpc.InntektHenter
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

class LøsningServiceTest {
    private val instrumentation = mockk<GrunnlagInstrumentation>(relaxed = true)

    private val inntekt = Inntekt(
        inntektsId = "12345",
        inntektsListe = listOf(
            KlassifisertInntektMåned(
                årMåned = YearMonth.of(2018, 2),
                klassifiserteInntekter = listOf(
                    KlassifisertInntekt(
                        beløp = BigDecimal(25000),
                        inntektKlasse = InntektKlasse.ARBEIDSINNTEKT
                    )
                )

            )
        ),
        sisteAvsluttendeKalenderMåned = YearMonth.of(2018, 2)
    )

    private val inntektHenter = mockk<InntektHenter>().also {
        every { runBlocking { it.hentKlassifisertInntekt(any()) } } returns inntekt
    }

    private val rapid = TestRapid().apply {
        LøsningService(rapidsConnection = this, inntektHenter = inntektHenter, instrumentation = instrumentation)
    }

    @Test
    fun `skal fastsette dagpengegrunnlag`() {
        @Language("json")
        val json = """
            {
                "@behov": ["Grunnlag"],
                "@id": "32",
                "beregningsdato": "2020-03-01",
                "inntektId": "${ULID().nextULID()}",
                "vedtakId" : "1234"
            }
            """.trimIndent()

        rapid.sendTestMessage(json)

        val inspektør = rapid.inspektør
        println(inspektør.message(0))
        inspektør.size shouldBeExactly 1
        inspektør.field(0, "@behov").map(JsonNode::asText) shouldContain "Grunnlag"
        inspektør.field(0, "@løsning").hasNonNull("Grunnlag") shouldBe true
        inspektør.field(0, "@løsning")["Grunnlag"]["avkortet"].asInt() shouldBeGreaterThan 0
        inspektør.field(0, "@løsning")["Grunnlag"]["uavkortet"].asInt() shouldBeGreaterThan 0
        inspektør.field(0, "@løsning")["Grunnlag"]["harAvkortet"] shouldNotBe null
        inspektør.field(0, "@løsning")["Grunnlag"]["grunnbeløp"].asInt() shouldBeGreaterThan 0
        inspektør.field(0, "@løsning")["Grunnlag"]["inntektsperioder"] shouldNotBe null
    }
}

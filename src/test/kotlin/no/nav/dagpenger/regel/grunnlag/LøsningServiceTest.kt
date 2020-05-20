package no.nav.dagpenger.regel.grunnlag

import com.fasterxml.jackson.databind.JsonNode
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
        rapid.sendTestMessage(packet)

        with(rapid.inspektør) {
            size shouldBeExactly 1

            field(0, "@behov").map(JsonNode::asText) shouldContain "Grunnlag"
            field(0, "@løsning").hasNonNull("Grunnlag") shouldBe true
            field(0, "@løsning")["Grunnlag"]["avkortet"].asInt() shouldBeGreaterThan 0
            field(0, "@løsning")["Grunnlag"]["uavkortet"].asInt() shouldBeGreaterThan 0
            field(0, "@løsning")["Grunnlag"]["harAvkortet"] shouldNotBe null
            field(0, "@løsning")["Grunnlag"]["grunnbeløp"].asInt() shouldBeGreaterThan 0
            field(0, "@løsning")["Grunnlag"]["inntektsperioder"] shouldNotBe null
        }
    }
}

@Language("JSON")
val packet = """{
  "@event_name": "behov",
  "@opprettet": "2020-05-20T13:36:05.114891",
  "@id": "01E8RXX07TVAR8GE86WEWNCK70",
  "@behov": [
    "Grunnlag"
  ],
  "@forårsaket_av": {
    "event_name": "behov",
    "id": "01E8RXX03JMMB5G7DFX6CKYG51",
    "opprettet": "2020-05-20T13:36:04.978438"
  },
  "fødselsnummer": "9999223837",
  "aktørId": "1334014935246",
  "sakId": "01E8RXWY80PX2GX8YCRBVFD8VP",
  "vedtakId": "01E8RXWY819GVH1P3XNGBVAV1X",
  "beregningsdato": "2020-04-21",
  "rettighetstype": "Permittering",
  "tilstand": "AvventerGrunnlag",
  "inntektId": "01E8RXWZZQZR68M9ATANCR0E23",
  "harAvtjentVerneplikt": false,
  "lærling": false,
  "oppFyllerKravTilFangstOgFisk": false,
  "system_read_count": 0,
  "system_participating_services": [
    {
      "service": "dp-saksbehandling",
      "instance": "dp-saksbehandling-85cb9dd9c6-g7p8d",
      "time": "2020-05-20T13:36:05.115012"
    }
  ]
}"""

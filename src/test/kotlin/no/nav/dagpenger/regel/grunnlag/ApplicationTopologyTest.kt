package no.nav.dagpenger.regel.grunnlag

import com.fasterxml.jackson.databind.JsonNode
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.ints.shouldBeExactly
import io.mockk.mockk
import no.nav.dagpenger.regel.grunnlag.RapidGrunnlag.Companion.BEREGNINGSDATO
import no.nav.dagpenger.regel.grunnlag.RapidGrunnlag.Companion.GRUNNLAG
import no.nav.dagpenger.regel.grunnlag.RapidGrunnlag.Companion.INNTEKT
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

class ApplicationTopologyTest {
    private val instrumentation = mockk<GrunnlagInstrumentation>(relaxed = true)
    private val rapid = TestRapid().apply {
        RapidGrunnlag(this, instrumentation)
    }

    @Test
    fun `skal fastsette dagpengegrunnlag`() {
        @Language("json")
        val json = """
            {
                "@behov": ["$GRUNNLAG"],
                "@id": "32",
                "$BEREGNINGSDATO": "2020-03-01",
                "$INNTEKT": {
                    "inntektsId": "12345",
                    "sisteAvsluttendeKalenderMåned": "2020-01",
                    "inntektsListe": [
                      {
                        "årMåned": "2020-01",
                        "klassifiserteInntekter": [
                          {
                            "beløp": "500000",
                            "inntektKlasse": "ARBEIDSINNTEKT"
                          }
                        ]
                      }
                    ],
                    "manueltRedigert": false
                }
            }
            """.trimIndent()

        rapid.sendTestMessage(json)

        val inspektør = rapid.inspektør
        inspektør.size shouldBeExactly 1

        inspektør.field(0, "@behov").map(JsonNode::asText) shouldContain GRUNNLAG
        inspektør.field(0, "@løsning").hasNonNull(GRUNNLAG)
        inspektør.field(0, "@løsning")[GRUNNLAG].hasNonNull("avkortetGrunnlag")
        inspektør.field(0, "@løsning")[GRUNNLAG].hasNonNull("uavkortetGrunnlag")
        inspektør.field(0, "@løsning")[GRUNNLAG].hasNonNull("harAvkortet")
        inspektør.field(0, "@løsning")[GRUNNLAG].hasNonNull("grunnbeløpBrukt")
        inspektør.field(0, "@løsning")[GRUNNLAG].hasNonNull("grunnlagInntektsPerioder")
    }
}

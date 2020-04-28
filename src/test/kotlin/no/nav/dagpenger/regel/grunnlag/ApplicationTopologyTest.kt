package no.nav.dagpenger.regel.grunnlag

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.ints.shouldBeExactly
import io.mockk.mockk
import no.nav.dagpenger.regel.grunnlag.RapidGrunnlag.Companion.BEREGNINGSDATO
import no.nav.dagpenger.regel.grunnlag.RapidGrunnlag.Companion.GRUNNLAG
import no.nav.dagpenger.regel.grunnlag.RapidGrunnlag.Companion.INNTEKT
import no.nav.helse.rapids_rivers.InMemoryRapid
import no.nav.helse.rapids_rivers.inMemoryRapid
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ApplicationTopologyTest {
    private val instrumentation = mockk<GrunnlagInstrumentation>(relaxed = true)
    private lateinit var rapid: InMemoryRapid

    @BeforeEach
    fun setUp() {
        rapid = createRapid {
            RapidGrunnlag(it, instrumentation = instrumentation)
        }
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

        rapid.sendToListeners(json)

        validateMessages(rapid) { messages ->
            messages.size shouldBeExactly 1

            messages.first().also { message ->
                message["@behov"].map(JsonNode::asText) shouldContain GRUNNLAG
                message["@løsning"].hasNonNull(GRUNNLAG)
                message["@løsning"][GRUNNLAG].hasNonNull("avkortetGrunnlag")
                message["@løsning"][GRUNNLAG].hasNonNull("uavkortetGrunnlag")
                message["@løsning"][GRUNNLAG].hasNonNull("harAvkortet")
                message["@løsning"][GRUNNLAG].hasNonNull("grunnbeløpBrukt")
                message["@løsning"][GRUNNLAG].hasNonNull("grunnlagInntektsPerioder")
            }
        }
    }

    private fun createRapid(service: (InMemoryRapid) -> Any): InMemoryRapid {
        return inMemoryRapid { }.also { service(it) }
    }

    private fun validateMessages(rapid: InMemoryRapid, assertions: (messages: List<JsonNode>) -> Any) {
        rapid.outgoingMessages.map { jacksonObjectMapper().readTree(it.value) }.also { assertions(it) }
    }
}

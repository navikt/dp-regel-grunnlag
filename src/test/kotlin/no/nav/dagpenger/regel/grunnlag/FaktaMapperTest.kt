package no.nav.dagpenger.regel.grunnlag

import io.kotest.assertions.throwables.shouldNotThrowAny
import no.nav.dagpenger.regel.grunnlag.GrunnlagsberegningBehovløser.Companion.FORRIGE_GRUNNLAG
import no.nav.dagpenger.regel.grunnlag.GrunnlagsberegningBehovløser.Companion.INNTEKT
import no.nav.dagpenger.regel.grunnlag.GrunnlagsberegningBehovløser.Companion.MANUELT_GRUNNLAG
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageProblems
import org.junit.jupiter.api.Test

private val resourceRetriever = object {}.javaClass

class FaktaMapperTest {
    @Test
    fun `greier å parse json`() {
        resourceRetriever.getResource("/test_packet.json")?.readText()!!.let { json ->
            val packet = JsonMessage(json, MessageProblems("")).also {
                it.interestedIn(MANUELT_GRUNNLAG, INNTEKT, FORRIGE_GRUNNLAG)
                shouldNotThrowAny {
                    it.inntekt()
                }
            }
        }
    }
}

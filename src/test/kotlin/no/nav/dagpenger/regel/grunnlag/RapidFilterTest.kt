package no.nav.dagpenger.regel.grunnlag

import io.kotest.matchers.shouldBe
import no.nav.dagpenger.regel.grunnlag.GrunnlagsberegningBehovløser.Companion.BEREGNINGSDATO
import no.nav.dagpenger.regel.grunnlag.GrunnlagsberegningBehovløser.Companion.PROBLEM
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Test

class RapidFilterTest {
    private val testRapid = TestRapid()

    @Test
    fun `Skal ikke behandle pakker med problem`() {
        val testListener = TestListener(testRapid)
        testRapid.sendTestMessage(
            JsonMessage.newMessage(
                mapOf(
                    BEREGNINGSDATO to "2020-04-30",
                    PROBLEM to "Det er et problem",
                ),
            ).toJson(),
        )
        testListener.onPacketCalled shouldBe false
    }

    private class TestListener(rapidsConnection: RapidsConnection) : River.PacketListener {
        var onPacketCalled = false

        init {
            River(rapidsConnection).apply(
                GrunnlagsberegningBehovløser.rapidFilter,
            ).register(this)
        }

        override fun onPacket(
            packet: JsonMessage,
            context: MessageContext,
        ) {
            this.onPacketCalled = true
        }

        override fun onError(
            problems: MessageProblems,
            context: MessageContext,
        ) {
        }
    }
}

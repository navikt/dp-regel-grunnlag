package no.nav.dagpenger.regel.grunnlag

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageProblems
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.kotest.matchers.shouldBe
import no.nav.dagpenger.regel.grunnlag.GrunnlagsberegningBehovløser.Companion.BEREGNINGSDATO
import no.nav.dagpenger.regel.grunnlag.GrunnlagsberegningBehovløser.Companion.PROBLEM
import org.junit.jupiter.api.Test

class RapidFilterTest {
    private val testRapid = TestRapid()

    @Test
    fun `Skal ikke behandle pakker med problem`() {
        val testListener = TestListener(testRapid)
        testRapid.sendTestMessage(
            JsonMessage
                .newMessage(
                    mapOf(
                        BEREGNINGSDATO to "2020-04-30",
                        PROBLEM to "Det er et problem",
                    ),
                ).toJson(),
        )
        testListener.onPacketCalled shouldBe false
    }

    private class TestListener(
        rapidsConnection: RapidsConnection,
    ) : River.PacketListener {
        var onPacketCalled = false

        init {
            River(rapidsConnection)
                .apply(
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

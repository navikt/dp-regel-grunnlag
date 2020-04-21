package no.nav.dagpenger.regel.grunnlag

import io.kotlintest.shouldBe
import java.time.LocalDate
import org.junit.jupiter.api.Test

internal class PacketToFaktaKtTest {

    @Test
    fun packetToFakta() {
        isThisGjusteringTest(LocalDate.now(), false) shouldBe false
    }
}

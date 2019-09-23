package no.nav.dagpenger.regel.grunnlag

import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.time.LocalDate

internal class PacketToFaktaKtTest {

    @Test
    fun packetToFakta() {
        isThisGjusteringTest(LocalDate.now(), false) shouldBe false
    }
}
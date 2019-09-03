package no.nav.dagpenger.regel.grunnlag

import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.YearMonth
import kotlin.test.assertEquals

class PacketToFaktaTest {

    private val emptyInntekt: Inntekt = Inntekt(
        inntektsId = "12345",
        inntektsListe = emptyList(),
        sisteAvsluttendeKalenderMåned = YearMonth.of(2018, 3)
    )

    private val simplePacketJson = """
        {
            "beregningsDato":"2019-04-10"
        }""".trimIndent()

    @Test
    fun ` should map avtjent_verneplikt from packet to Fakta `() {
        val json = """
        {
            "beregningsDato":"2019-04-10",
            "harAvtjentVerneplikt": true
        }""".trimIndent()

        val packet = Packet(json)

        val fakta = packetToFakta(packet)

        assertTrue(fakta.verneplikt)
    }

    @Test
    fun ` should map fangst_og_fisk from packet to Fakta `() {
        val json = """
        {
            "beregningsDato":"2019-04-10",
            "oppfyllerKravTilFangstOgFisk": true
        }""".trimIndent()

        val packet = Packet(json)

        val fakta = packetToFakta(packet)

        assertTrue(fakta.fangstOgFisk)
    }

    @Test
    fun ` should map inntekt from packet to Fakta `() {

        val packet = Packet(simplePacketJson)

        packet.putValue("inntektV1", Grunnlag.inntektAdapter.toJsonValue(emptyInntekt)!!)

        val fakta = packetToFakta(packet)

        assertEquals("12345", fakta.inntekt!!.inntektsId)
    }

    @Test
    fun ` should map beregningsDato from packet to Fakta`() {

        val packet = Packet(simplePacketJson)

        val fakta = packetToFakta(packet)

        assertEquals(LocalDate.of(2019, 4, 10), fakta.beregningsdato)
    }

    @Test
    fun ` should map manutelt_grunnlag from packet to Fakta`() {
        val json = """
        {
            "beregningsDato":"2019-04-10",
            "manueltGrunnlag":"1000"
        }""".trimIndent()

        val packet = Packet(json)

        val fakta = packetToFakta(packet)

        assertEquals(1000, fakta.manueltGrunnlag)
    }
}
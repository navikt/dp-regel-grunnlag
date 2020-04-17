package no.nav.dagpenger.regel.grunnlag

import io.mockk.mockk
import io.mockk.verify
import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.YearMonth
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OnPacketTest {
    companion object {
        val fakeGrunnlagInstrumentation = mockk<GrunnlagInstrumentation>(relaxed = true)
    }

    @Test
    fun ` Skal legge på minus i grunnlag dersom det blir negativt i sum `() {

        val grunnlag = Grunnlag(
            Configuration(),
            fakeGrunnlagInstrumentation
        )

        val inntekt = getInntekt((-1000).toBigDecimal())

        val json = """
            {
                "beregningsDato":"2018-08-10",
                "harAvtjentVerneplikt": false,
                "oppfyllerKravTilFangstOgFisk": false
            }
            """.trimIndent()

        val packet = Packet(json)
        packet.putValue("inntektV1", Grunnlag.inntektAdapter.toJsonValue(inntekt)!!)

        val resultPacket = grunnlag.onPacket(packet)

        assertTrue { resultPacket.hasField("grunnlagResultat") }

        assertEquals("ArbeidsinntektSiste36", resultPacket.getMapValue("grunnlagResultat")["beregningsregel"])
    }

    @Test
    fun ` Skal legge på 0 i grunnlag hvis det ikke er inntekt `() {

        val grunnlag = Grunnlag(
            Configuration(),
            fakeGrunnlagInstrumentation
        )

        val inntekt = getInntekt((0).toBigDecimal())

        val json = """
            {
                "beregningsDato":"2018-08-10",
                "harAvtjentVerneplikt": false,
                "oppfyllerKravTilFangstOgFisk": false
            }
            """.trimIndent()

        val packet = Packet(json)
        packet.putValue("inntektV1", Grunnlag.inntektAdapter.toJsonValue(inntekt)!!)

        val resultPacket = grunnlag.onPacket(packet)

        assertTrue { resultPacket.hasField("grunnlagResultat") }

        assertEquals("ArbeidsinntektSiste12", resultPacket.getMapValue("grunnlagResultat")["beregningsregel"])
    }

    @Test
    fun ` Skal velge rett beregningsregel og gi rett resultat ved arbeidsinntekt `() {

        val grunnlag = Grunnlag(
            Configuration(),
            fakeGrunnlagInstrumentation
        )

        val inntekt = getInntekt(1000.toBigDecimal())

        val json = """
            {
                "beregningsDato":"2018-08-10",
                "harAvtjentVerneplikt": false,
                "oppfyllerKravTilFangstOgFisk": false
            }
            """.trimIndent()

        val packet = Packet(json)
        packet.putValue("inntektV1", Grunnlag.inntektAdapter.toJsonValue(inntekt)!!)

        val resultPacket = grunnlag.onPacket(packet)

        assertTrue { resultPacket.hasField("grunnlagResultat") }

        assertEquals(3035, Integer.parseInt(resultPacket.getMapValue("grunnlagResultat")["avkortet"].toString()))
        assertEquals(
            Integer.parseInt(resultPacket.getMapValue("grunnlagResultat")["avkortet"].toString()),
            Integer.parseInt(resultPacket.getMapValue("grunnlagResultat")["uavkortet"].toString())
        )
        assertEquals("ArbeidsinntektSiste12", resultPacket.getMapValue("grunnlagResultat")["beregningsregel"])
        assertEquals(false, resultPacket.getMapValue("grunnlagResultat")["harAvkortet"])
    }

    @Test
    fun ` Skal velge rett beregningsregel og gi rett resultat ved verneplikt `() {

        val grunnlag = Grunnlag(
            Configuration(),
            fakeGrunnlagInstrumentation
        )

        val inntekt = getInntekt(1000.toBigDecimal())

        val json = """
            {
                "beregningsDato":"2018-08-10",
                "harAvtjentVerneplikt": true,
                "oppfyllerKravTilFangstOgFisk": false
            }
            """.trimIndent()

        val packet = Packet(json)
        packet.putValue("inntektV1", Grunnlag.inntektAdapter.toJsonValue(inntekt)!!)

        val resultPacket = grunnlag.onPacket(packet)

        assertTrue { resultPacket.hasField("grunnlagResultat") }

        assertEquals(299574, Integer.parseInt(resultPacket.getMapValue("grunnlagResultat")["avkortet"].toString()))
        assertEquals(
            Integer.parseInt(resultPacket.getMapValue("grunnlagResultat")["avkortet"].toString()),
            Integer.parseInt(resultPacket.getMapValue("grunnlagResultat")["uavkortet"].toString())
        )
        assertEquals("Verneplikt", resultPacket.getMapValue("grunnlagResultat")["beregningsregel"])
        assertEquals(false, resultPacket.getMapValue("grunnlagResultat")["harAvkortet"])
    }

    @Test
    fun ` Skal velge rett beregningsregel ved lærling `() {
        val grunnlag = Grunnlag(
            Configuration(),
            fakeGrunnlagInstrumentation
        )

        val inntekt = getInntekt(1000.toBigDecimal())

        val json = """
            {
                "beregningsDato":"2018-08-10",
                "harAvtjentVerneplikt": true,
                "oppfyllerKravTilFangstOgFisk": false,
                "lærling": true
            }
            """.trimIndent()

        val packet = Packet(json)
        packet.putValue("inntektV1", Grunnlag.inntektAdapter.toJsonValue(inntekt)!!)

        val resultPacket = grunnlag.onPacket(packet)

        assertTrue { resultPacket.hasField("grunnlagResultat") }

        assertEquals(
            Integer.parseInt(resultPacket.getMapValue("grunnlagResultat")["avkortet"].toString()),
            Integer.parseInt(resultPacket.getMapValue("grunnlagResultat")["uavkortet"].toString())
        )
        assertEquals("LærlingArbeidsinntekt3x4", resultPacket.getMapValue("grunnlagResultat")["beregningsregel"])
        assertEquals(false, resultPacket.getMapValue("grunnlagResultat")["harAvkortet"])
    }

    @Test
    fun ` Skal instrumentere beregninger`() {
        val grunnlag = Grunnlag(
            Configuration(),
            fakeGrunnlagInstrumentation
        )

        val inntekt = getInntekt(1000.toBigDecimal())

        val json = """
            {
                "beregningsDato":"2018-08-10",
                "harAvtjentVerneplikt": true,
                "oppfyllerKravTilFangstOgFisk": false
            }
            """.trimIndent()

        val packet = Packet(json)
        packet.putValue("inntektV1", Grunnlag.inntektAdapter.toJsonValue(inntekt)!!)

        grunnlag.onPacket(packet)

        verify {
            fakeGrunnlagInstrumentation.grunnlagBeregnet(
                regelIdentifikator = ofType(String::class),
                fakta = ofType(Fakta::class),
                resultat = ofType(GrunnlagResultat::class)
            )
        }
    }

    private fun getInntekt(månedsbeløp: BigDecimal): Inntekt {
        return Inntekt(
            inntektsId = "12345",
            inntektsListe = listOf(
                KlassifisertInntektMåned(
                    årMåned = YearMonth.of(2018, 4),
                    klassifiserteInntekter = listOf(
                        KlassifisertInntekt(
                            beløp = månedsbeløp,
                            inntektKlasse = InntektKlasse.ARBEIDSINNTEKT
                        )
                    )
                ),
                KlassifisertInntektMåned(
                    YearMonth.of(2018, 5),
                    listOf(
                        KlassifisertInntekt(
                            månedsbeløp,
                            InntektKlasse.ARBEIDSINNTEKT
                        ),
                        KlassifisertInntekt(
                            månedsbeløp,
                            InntektKlasse.ARBEIDSINNTEKT
                        )
                    )
                )
            ),
            sisteAvsluttendeKalenderMåned = YearMonth.of(2018, 7)
        )
    }
}
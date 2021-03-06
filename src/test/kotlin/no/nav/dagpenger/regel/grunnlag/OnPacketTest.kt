package no.nav.dagpenger.regel.grunnlag

import io.mockk.mockk
import io.mockk.verify
import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import no.nav.dagpenger.grunnbelop.Grunnbeløp
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.YearMonth
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OnPacketTest {
    companion object {
        val fakeGrunnlagInstrumentation = mockk<GrunnlagInstrumentation>(relaxed = true)
        val grunnlag = Grunnlag(
            Configuration(),
            fakeGrunnlagInstrumentation
        )
    }

    @Test
    fun ` Skal legge på minus i grunnlag dersom det blir negativt i sum `() {

        val inntekt = getInntekt((-1000).toBigDecimal())

        val json =
            """
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

        val inntekt = getInntekt((0).toBigDecimal())

        val json =
            """
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

        val inntekt = getInntekt(1000.toBigDecimal())

        val json =
            """
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

        val inntekt = getInntekt(1000.toBigDecimal())

        val json =
            """
            {
                "beregningsDato":"2018-08-10",
                "harAvtjentVerneplikt": true,
                "oppfyllerKravTilFangstOgFisk": false,
                "regelverksdato": "2021-03-16"
            }
            """.trimIndent()

        val packet = Packet(json)
        packet.putValue("inntektV1", Grunnlag.inntektAdapter.toJsonValue(inntekt)!!)

        val resultPacket = grunnlag.onPacket(packet)

        assertTrue { resultPacket.hasField("grunnlagResultat") }

        val g2020 = (Grunnbeløp.FastsattI2020.verdi * BigDecimal(3)).toInt()

        assertEquals(g2020, Integer.parseInt(resultPacket.getMapValue("grunnlagResultat")["avkortet"].toString()))
        assertEquals(
            Integer.parseInt(resultPacket.getMapValue("grunnlagResultat")["avkortet"].toString()),
            Integer.parseInt(resultPacket.getMapValue("grunnlagResultat")["uavkortet"].toString())
        )
        assertEquals("Verneplikt", resultPacket.getMapValue("grunnlagResultat")["beregningsregel"])
        assertEquals(false, resultPacket.getMapValue("grunnlagResultat")["harAvkortet"])
    }

    @Test
    fun ` Skal velge rett beregningsregel ved lærling `() {

        val inntekt = getInntekt(1000.toBigDecimal(), YearMonth.of(2020, 3))

        val json =
            """
            {
                "beregningsDato":"2020-03-20",
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
        assertEquals("LærlingArbeidsinntekt1x12", resultPacket.getMapValue("grunnlagResultat")["beregningsregel"])
        assertEquals(false, resultPacket.getMapValue("grunnlagResultat")["harAvkortet"])
    }

    @Test
    fun ` Skal velge rett beregningsregel ved forrigeGrunnlag`() {

        val json =
            """
            {
                "beregningsDato":"2020-03-20",
                "oppfyllerKravTilFangstOgFisk": false,
                "lærling": false,
                "forrigeGrunnlag": 10880
            }
            """.trimIndent()

        val resultPacket = grunnlag.onPacket(Packet(json))

        assertTrue { resultPacket.hasField("grunnlagResultat") }

        assertEquals(
            Integer.parseInt(resultPacket.getMapValue("grunnlagResultat")["avkortet"].toString()),
            Integer.parseInt(resultPacket.getMapValue("grunnlagResultat")["uavkortet"].toString())
        )
        assertEquals("ForrigeGrunnlag", resultPacket.getMapValue("grunnlagResultat")["beregningsregel"])
        assertEquals(false, resultPacket.getMapValue("grunnlagResultat")["harAvkortet"])
    }

    @Test
    fun ` Skal velge rett beregningsregel ved lærling og forrige grunnlag `() {
        val json =
            """
            {
                "beregningsDato":"2020-03-20",
                "oppfyllerKravTilFangstOgFisk": false,
                "lærling": true,
                "forrigeGrunnlag": 300000
            }
            """.trimIndent()

        val packet = Packet(json)
        val resultPacket = grunnlag.onPacket(packet)

        assertTrue { resultPacket.hasField("grunnlagResultat") }
        assertEquals("ForrigeGrunnlag", resultPacket.getMapValue("grunnlagResultat")["beregningsregel"])
    }

    @Test
    fun ` Skal instrumentere beregninger`() {

        val inntekt = getInntekt(1000.toBigDecimal())

        val json =
            """
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

    private fun getInntekt(månedsbeløp: BigDecimal, inntektsdatoStart: YearMonth? = null): Inntekt {
        return Inntekt(
            inntektsId = "12345",
            inntektsListe = listOf(
                KlassifisertInntektMåned(
                    årMåned = inntektsdatoStart ?: YearMonth.of(2018, 4),
                    klassifiserteInntekter = listOf(
                        KlassifisertInntekt(
                            beløp = månedsbeløp,
                            inntektKlasse = InntektKlasse.ARBEIDSINNTEKT
                        )
                    )
                ),
                KlassifisertInntektMåned(
                    inntektsdatoStart?.plusMonths(1) ?: YearMonth.of(2018, 5),
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

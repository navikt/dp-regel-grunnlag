package no.nav.dagpenger.regel.grunnlag.beregning

import io.kotlintest.matchers.types.shouldBeTypeOf
import io.kotlintest.shouldBe
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import no.nav.dagpenger.grunnbelop.Grunnbeløp
import no.nav.dagpenger.regel.grunnlag.Fakta
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

internal class EtterLærlingForskriftTest() {
    @Test
    fun `Skal bruke siste kalender måned og gange med 12 for å finne uavkortet grunnlag for arbeidsinntekt`() {

        val fakta = Fakta(
            inntekt = Inntekt("123", inntektsListe, sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 3)),
            verneplikt = false,
            fangstOgFisk = false,
            lærling = true,
            beregningsdato = LocalDate.of(2019, 4, 1),
            gjeldendeGrunnbeløpVedBeregningsdato = Grunnbeløp.FastsattI2018,
            gjeldendeGrunnbeløpForDagensDato = Grunnbeløp.FastsattI2019
        )

        when (val beregningsResultat =
            LærlingForskriftSisteAvsluttendeKalenderMåned().calculate(fakta)) {
            is BeregningsResultat -> {
                BigDecimal("12000.00000000000000000000") shouldBe beregningsResultat.uavkortet
                BigDecimal("12000.00000000000000000000") shouldBe beregningsResultat.avkortet
                "LærlingArbeidsinntekt1x12" shouldBe beregningsResultat.beregningsregel
            }

            else -> beregningsResultat.shouldBeTypeOf<BeregningsResultat>()
        }
    }

    @Test
    fun `Skal bruke siste 3 kalendermånedene og gange med 4 for å finne uavkortet grunnlag for arbeidsinntekt `() {

        val fakta = Fakta(
            inntekt = Inntekt("123", inntektsListe, sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 3)),
            verneplikt = false,
            fangstOgFisk = false,
            lærling = true,
            beregningsdato = LocalDate.of(2019, 4, 1),
            gjeldendeGrunnbeløpVedBeregningsdato = Grunnbeløp.FastsattI2018,
            gjeldendeGrunnbeløpForDagensDato = Grunnbeløp.FastsattI2019
        )

        when (val beregningsResultat =
            LærlingForskriftSiste3AvsluttendeKalenderMåned().calculate(fakta)) {
            is BeregningsResultat -> {
                BigDecimal("20000.00000000000000000000") shouldBe beregningsResultat.uavkortet
                BigDecimal("20000.00000000000000000000") shouldBe beregningsResultat.avkortet
                "LærlingArbeidsinntekt3x4" shouldBe beregningsResultat.beregningsregel
            }

            else -> beregningsResultat.shouldBeTypeOf<BeregningsResultat>()
        }
    }

    @Test
    fun `Skal bruke siste kalender måned og gange med 12 for å finne uavkortet grunnlag for fangst- og fiskeinntekt`() {

        val fakta = Fakta(
            inntekt = Inntekt("123", inntektsListe, sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 3)),
            verneplikt = false,
            fangstOgFisk = true,
            lærling = true,
            beregningsdato = LocalDate.of(2019, 4, 1),
            gjeldendeGrunnbeløpVedBeregningsdato = Grunnbeløp.FastsattI2018,
            gjeldendeGrunnbeløpForDagensDato = Grunnbeløp.FastsattI2019
        )

        when (val beregningsResultat =
            LærlingForskriftSisteAvsluttendeKalenderMånedFangstOgFisk().calculate(fakta)) {
            is BeregningsResultat -> {
                BigDecimal("36000.00000000000000000000") shouldBe beregningsResultat.uavkortet
                BigDecimal("36000.00000000000000000000") shouldBe beregningsResultat.avkortet
                "LærlingFangstOgFisk1x12" shouldBe beregningsResultat.beregningsregel
            }

            else -> beregningsResultat.shouldBeTypeOf<BeregningsResultat>()
        }
    }

    @Test
    fun `Skal bruke siste 3 kalendermånedene og gange med 4 for å finne uavkortet grunnlag for fangst- og fiskeinntekt `() {

        val fakta = Fakta(
            inntekt = Inntekt("123", inntektsListe, sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 3)),
            verneplikt = false,
            fangstOgFisk = true,
            lærling = true,
            beregningsdato = LocalDate.of(2019, 4, 1),
            gjeldendeGrunnbeløpVedBeregningsdato = Grunnbeløp.FastsattI2018,
            gjeldendeGrunnbeløpForDagensDato = Grunnbeløp.FastsattI2019
        )

        when (val beregningsResultat =
            LærlingForskriftSiste3AvsluttendeKalenderMånedFangsOgFisk().calculate(fakta)) {
            is BeregningsResultat -> {
                BigDecimal("28000.00000000000000000000") shouldBe beregningsResultat.uavkortet
                BigDecimal("28000.00000000000000000000") shouldBe beregningsResultat.avkortet
                "LærlingFangstOgFisk3x4" shouldBe beregningsResultat.beregningsregel
            }

            else -> beregningsResultat.shouldBeTypeOf<BeregningsResultat>()
        }
    }

    private val inntektsListe = listOf(
        KlassifisertInntektMåned(
            YearMonth.of(2019, 3),
            listOf(
                KlassifisertInntekt(
                    BigDecimal(1000),
                    InntektKlasse.ARBEIDSINNTEKT
                ),
                KlassifisertInntekt(
                    BigDecimal(2000),
                    InntektKlasse.FANGST_FISKE
                )
            )
        ),
        KlassifisertInntektMåned(
            YearMonth.of(2019, 2),
            listOf(
                KlassifisertInntekt(
                    BigDecimal(2000),
                    InntektKlasse.ARBEIDSINNTEKT
                )
            )
        ),
        KlassifisertInntektMåned(
            YearMonth.of(2019, 1),
            listOf(
                KlassifisertInntekt(
                    BigDecimal(2000),
                    InntektKlasse.ARBEIDSINNTEKT
                )
            )
        ),
        KlassifisertInntektMåned(
            YearMonth.of(2018, 12),
            listOf(
                KlassifisertInntekt(
                    BigDecimal(2000),
                    InntektKlasse.ARBEIDSINNTEKT
                )
            )
        )
    )
}

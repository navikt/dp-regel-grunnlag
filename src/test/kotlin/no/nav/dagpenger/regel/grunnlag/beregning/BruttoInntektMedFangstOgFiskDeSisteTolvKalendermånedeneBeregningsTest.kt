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

class BruttoInntektMedFangstOgFiskDeSisteTolvKalendermånedeneBeregningsTest {

    private val beregning = BruttoInntektMedFangstOgFiskDeSiste12AvsluttedeKalendermånedene()

    @Test
    fun `Skal ikke behandle lærlinger`() {
        val fakta = Fakta(
            inntekt = null,
            fangstOgFisk = false,
            lærling = true,
            verneplikt = false,
            beregningsdato = LocalDate.of(2019, 4, 1),
            gjeldendeGrunnbeløpVedBeregningsdato = Grunnbeløp.FastsattI2018,
            gjeldendeGrunnbeløpForDagensDato = Grunnbeløp.FastsattI2019
        )
        false shouldBe beregning.isActive(fakta)
    }

    @Test
    fun ` Skal gi grunnlag på 2034,699 siste 12 kalendermåned gitt mars 2019 `() {

        val inntektsListe = listOf(
            KlassifisertInntektMåned(
                YearMonth.of(2018, 4),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(500),
                        InntektKlasse.ARBEIDSINNTEKT
                    ), KlassifisertInntekt(
                        BigDecimal(500),
                        InntektKlasse.FANGST_FISKE
                    )
                )
            ),
            KlassifisertInntektMåned(
                YearMonth.of(2018, 5),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(500),
                        InntektKlasse.ARBEIDSINNTEKT
                    ), KlassifisertInntekt(
                        BigDecimal(500),
                        InntektKlasse.SYKEPENGER_FANGST_FISKE
                    )
                )
            )
        )

        val fakta = Fakta(
            inntekt = Inntekt("123", inntektsListe, sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 3)),
            verneplikt = false,
            fangstOgFisk = true,
            beregningsdato = LocalDate.of(2019, 4, 1),
            gjeldendeGrunnbeløpVedBeregningsdato = Grunnbeløp.FastsattI2018,
            gjeldendeGrunnbeløpForDagensDato = Grunnbeløp.FastsattI2019
        )

        when (val beregningsResultat = beregning.calculate(fakta)) {
            is BeregningsResultat ->
                beregningsResultat.uavkortet shouldBe BigDecimal("2034.69893414785227588000")
            else -> beregningsResultat.shouldBeTypeOf<BeregningsResultat>()
        }
    }

    @Test
    fun ` Skal gi riktig grunnlag med minusinntekt`() {

        val inntektsListe = listOf(
            KlassifisertInntektMåned(
                YearMonth.of(2018, 4),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(1000),
                        InntektKlasse.FANGST_FISKE
                    )
                )
            ),
            KlassifisertInntektMåned(
                YearMonth.of(2018, 5),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(1000),
                        InntektKlasse.ARBEIDSINNTEKT
                    ),
                    KlassifisertInntekt(
                        BigDecimal(-1000),
                        InntektKlasse.FANGST_FISKE
                    )
                )
            )
        )

        val fakta = Fakta(
            inntekt = Inntekt("123", inntektsListe, sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 1)),
            verneplikt = false,
            fangstOgFisk = true,
            beregningsdato = LocalDate.of(2019, 4, 1),
            gjeldendeGrunnbeløpVedBeregningsdato = Grunnbeløp.FastsattI2018,
            gjeldendeGrunnbeløpForDagensDato = Grunnbeløp.FastsattI2019
        )

        when (val beregningsResultat = beregning.calculate(fakta)) {
            is BeregningsResultat ->
                beregningsResultat.uavkortet shouldBe BigDecimal("1034.69893414785227588000")
            else -> beregningsResultat.shouldBeTypeOf<BeregningsResultat>()
        }
    }

    @Test
    fun ` Skal gi riktig grunnlag dersom summen av inntekter er minus`() {

        val inntektsListe = listOf(
            KlassifisertInntektMåned(
                YearMonth.of(2018, 4),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(-1000),
                        InntektKlasse.FANGST_FISKE
                    )
                )
            ),
            KlassifisertInntektMåned(
                YearMonth.of(2018, 5),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(1000),
                        InntektKlasse.ARBEIDSINNTEKT
                    ),
                    KlassifisertInntekt(
                        BigDecimal(-1000),
                        InntektKlasse.FANGST_FISKE
                    )
                )
            )
        )

        val fakta = Fakta(
            inntekt = Inntekt("123", inntektsListe, sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 1)),
            fangstOgFisk = true,
            verneplikt = false,
            beregningsdato = LocalDate.of(2019, 2, 10),
            gjeldendeGrunnbeløpVedBeregningsdato = Grunnbeløp.FastsattI2018,
            gjeldendeGrunnbeløpForDagensDato = Grunnbeløp.FastsattI2019)

        when (val beregningsResultat = beregning.calculate(fakta)) {
            is BeregningsResultat ->
                beregningsResultat.uavkortet shouldBe BigDecimal("-1034.69893414785227588000")
            else -> beregningsResultat.shouldBeTypeOf<BeregningsResultat>()
        }
    }

    @Test
    fun `Skal returnere ingenBeregningsResultat når fangst og fisk er false`() {

        val fakta = Fakta(
            inntekt = Inntekt("123", emptyList(), sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 3)),
            fangstOgFisk = false,
            verneplikt = false,
            beregningsdato = LocalDate.of(2019, 4, 1),
            gjeldendeGrunnbeløpVedBeregningsdato = Grunnbeløp.FastsattI2018,
            gjeldendeGrunnbeløpForDagensDato = Grunnbeløp.FastsattI2019
        )

        when (val beregningsResultat = beregning.calculate(fakta)) {
            is IngenBeregningsResultat ->
                beregningsResultat.beskrivelse shouldBe "FangstOgFiskeSiste12"
            else -> beregningsResultat.shouldBeTypeOf<IngenBeregningsResultat>()
        }
    }

    @Test
    fun ` Skal gi ingen beregningsresultat dersom fangst og fisk ikke er satt selv om det er inntekt`() {

        val inntektsListe = listOf(
            KlassifisertInntektMåned(
                YearMonth.of(2018, 4),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(500),
                        InntektKlasse.ARBEIDSINNTEKT
                    ), KlassifisertInntekt(
                        BigDecimal(500),
                        InntektKlasse.FANGST_FISKE
                    )
                )
            ),
            KlassifisertInntektMåned(
                YearMonth.of(2018, 5),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(500),
                        InntektKlasse.ARBEIDSINNTEKT
                    ), KlassifisertInntekt(
                        BigDecimal(500),
                        InntektKlasse.SYKEPENGER_FANGST_FISKE
                    )
                )
            )
        )

        val fakta = Fakta(
            inntekt = Inntekt("123", inntektsListe, sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 3)),
            verneplikt = false,
            fangstOgFisk = false,
            beregningsdato = LocalDate.of(2019, 4, 1),
            gjeldendeGrunnbeløpVedBeregningsdato = Grunnbeløp.FastsattI2018,
            gjeldendeGrunnbeløpForDagensDato = Grunnbeløp.FastsattI2019
        )

        beregning.calculate(fakta).also {
            it.shouldBeTypeOf<IngenBeregningsResultat>()
            val resultat = it as IngenBeregningsResultat
            resultat.beskrivelse shouldBe "FangstOgFiskeSiste12"
        }
    }
}
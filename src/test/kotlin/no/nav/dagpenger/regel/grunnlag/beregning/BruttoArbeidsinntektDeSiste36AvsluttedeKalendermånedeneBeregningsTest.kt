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

class BruttoArbeidsinntektDeSiste36AvsluttedeKalendermånedeneBeregningsTest {

    @Test
    fun ` Skal gi uavkortet grunnlag på 4115 siste 36 kalendermåned gitt mars 2019 inntekt`() {

        val inntektsListe = listOf(
            KlassifisertInntektMåned(
                YearMonth.of(2018, 4),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(1000),
                        InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            ),
            KlassifisertInntektMåned(
                YearMonth.of(2018, 5),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(1000),
                        InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            ),
            KlassifisertInntektMåned(
                YearMonth.of(2017, 5),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(1000),
                        InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            ),
            KlassifisertInntektMåned(
                YearMonth.of(2016, 5),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(1000),
                        InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            )
        )

        val fakta = Fakta(
            inntekt = Inntekt("123", inntektsListe, sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 3)),
            fangstOgFisk = false,
            verneplikt = false,
            beregningsdato = LocalDate.of(2019, 4, 1),
            gjeldendeGrunnbeløpVedBeregningsdato = Grunnbeløp.FastsattI2018,
            gjeldendeGrunnbeløpForDagensDato = Grunnbeløp.FastsattI2019
        )

        when (val beregningsResultat = BruttoArbeidsinntektDeSiste36AvsluttedeKalendermånedene().calculate(fakta)) {
            is BeregningsResultat ->
                beregningsResultat.uavkortet shouldBe
                BigDecimal("1371.97393512841033163333")
            else -> beregningsResultat.shouldBeTypeOf<BeregningsResultat>()
        }
    }

    @Test
    fun `Skal gi riktig avkortet grunnlag siste 36 kalendermåneder gitt mars 2019 inntekt`() {
        val inntektsListe = listOf(
            KlassifisertInntektMåned(
                YearMonth.of(2018, 4),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(500000),
                        InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            ),
            KlassifisertInntektMåned(
                YearMonth.of(2018, 5),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(500000),
                        InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            ),
            KlassifisertInntektMåned(
                YearMonth.of(2017, 5),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(1000),
                        InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            ),
            KlassifisertInntektMåned(
                YearMonth.of(2016, 5),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(600000),
                        InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            )
        )

        val fakta = Fakta(
            inntekt = Inntekt("123", inntektsListe, sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 3)),
            fangstOgFisk = false,
            verneplikt = false,
            beregningsdato = LocalDate.of(2019, 4, 1),
            gjeldendeGrunnbeløpVedBeregningsdato = Grunnbeløp.FastsattI2018,
            gjeldendeGrunnbeløpForDagensDato = Grunnbeløp.FastsattI2019
        )

        when (val beregningsResultat = BruttoArbeidsinntektDeSiste36AvsluttedeKalendermånedene().calculate(fakta)) {
            is BeregningsResultat ->
                beregningsResultat.avkortet shouldBe
                BigDecimal("387876.89964471595075862667")
            else -> beregningsResultat.shouldBeTypeOf<BeregningsResultat>()
        }
    }

    @Test
    fun `Skal ikke ta med måneder som ikke er innenfor ønsket periode`() {

        val inntektsListe = listOf(
            KlassifisertInntektMåned(
                YearMonth.of(2018, 4),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(1000),
                        InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            ),
            KlassifisertInntektMåned(
                YearMonth.of(2018, 5),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(1000),
                        InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            ),
            KlassifisertInntektMåned(
                YearMonth.of(2017, 5),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(1000),
                        InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            ),
            KlassifisertInntektMåned(
                YearMonth.of(2016, 5),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(1000),
                        InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            ),
            KlassifisertInntektMåned(
                YearMonth.of(2015, 5),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(1000),
                        InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            )
        )

        val fakta = Fakta(
            inntekt = Inntekt("123", inntektsListe, sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 3)),
            fangstOgFisk = false,
            verneplikt = false,
            beregningsdato = LocalDate.of(2019, 4, 1),
            gjeldendeGrunnbeløpVedBeregningsdato = Grunnbeløp.FastsattI2018,
            gjeldendeGrunnbeløpForDagensDato = Grunnbeløp.FastsattI2019
        )

        when (val beregningsResultat = BruttoArbeidsinntektDeSiste36AvsluttedeKalendermånedene().calculate(fakta)) {
            is BeregningsResultat ->
                beregningsResultat.uavkortet shouldBe
                BigDecimal("1371.97393512841033163333")
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
                        InntektKlasse.ARBEIDSINNTEKT
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
                        InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            ),
            KlassifisertInntektMåned(
                YearMonth.of(2017, 5),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(1000),
                        InntektKlasse.ARBEIDSINNTEKT
                    ),
                    KlassifisertInntekt(
                        BigDecimal(-1000),
                        InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            ),
            KlassifisertInntektMåned(
                YearMonth.of(2016, 10),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(1000),
                        InntektKlasse.ARBEIDSINNTEKT
                    ),
                    KlassifisertInntekt(
                        BigDecimal(-1000),
                        InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            )
        )

        val fakta = Fakta(
            inntekt = Inntekt("123", inntektsListe, sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 1)),
            fangstOgFisk = false,
            verneplikt = false,
            beregningsdato = LocalDate.of(2019, 2, 10),
            gjeldendeGrunnbeløpVedBeregningsdato = Grunnbeløp.FastsattI2019,
            gjeldendeGrunnbeløpForDagensDato = Grunnbeløp.FastsattI2019
        )

        when (val beregningsResultat = BruttoArbeidsinntektDeSiste36AvsluttedeKalendermånedene().calculate(fakta)) {
            is BeregningsResultat ->
                beregningsResultat.uavkortet shouldBe
                BigDecimal("355.49052694534036781667")
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
                        InntektKlasse.ARBEIDSINNTEKT
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
                        InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            ),
            KlassifisertInntektMåned(
                YearMonth.of(2017, 5),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(1000),
                        InntektKlasse.ARBEIDSINNTEKT
                    ),
                    KlassifisertInntekt(
                        BigDecimal(-1000),
                        InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            ),
            KlassifisertInntektMåned(
                YearMonth.of(2016, 10),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(1000),
                        InntektKlasse.ARBEIDSINNTEKT
                    ),
                    KlassifisertInntekt(
                        BigDecimal(-1000),
                        InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            )
        )

        val fakta = Fakta(
            inntekt = Inntekt("123", inntektsListe, sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 1)),
            fangstOgFisk = false,
            verneplikt = false,
            beregningsdato = LocalDate.of(2019, 2, 10),
            gjeldendeGrunnbeløpVedBeregningsdato = Grunnbeløp.FastsattI2019,
            gjeldendeGrunnbeløpForDagensDato = Grunnbeløp.FastsattI2019
        )

        when (val beregningsResultat = BruttoArbeidsinntektDeSiste36AvsluttedeKalendermånedene().calculate(fakta)) {
            is BeregningsResultat ->
                beregningsResultat.uavkortet shouldBe
                BigDecimal("-355.49052694534036781667")
            else -> beregningsResultat.shouldBeTypeOf<BeregningsResultat>()
        }
    }

    @Test
    fun `Skal returnere 0 som grunnlag hvis ingen inntekt`() {

        val fakta = Fakta(
            inntekt = Inntekt("123", emptyList(), sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 3)),
            fangstOgFisk = false,
            verneplikt = false,
            beregningsdato = LocalDate.of(2019, 4, 1),
            gjeldendeGrunnbeløpVedBeregningsdato = Grunnbeløp.FastsattI2018,
            gjeldendeGrunnbeløpForDagensDato = Grunnbeløp.FastsattI2019
        )

        when (val beregningsResultat = BruttoArbeidsinntektDeSiste36AvsluttedeKalendermånedene().calculate(fakta)) {
            is BeregningsResultat ->
                beregningsResultat.uavkortet shouldBe
                BigDecimal.ZERO.setScale(20)
            else -> beregningsResultat.shouldBeTypeOf<BeregningsResultat>()
        }
    }

    @Test
    fun ` gir ikke harAvkortet selv om heltallene er like (BigDecimal må lages i samme skala)`() {

        val inntektsListe = listOf(
            KlassifisertInntektMåned(
                YearMonth.of(2018, 4),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(1000).setScale(3),
                        InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            ),
            KlassifisertInntektMåned(
                YearMonth.of(2017, 5),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(1000).setScale(5),
                        InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            )
        )

        val fakta = Fakta(
            inntekt = Inntekt("123", inntektsListe, sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 3)),
            fangstOgFisk = false,
            verneplikt = false,
            beregningsdato = LocalDate.of(2019, 4, 1),
            gjeldendeGrunnbeløpVedBeregningsdato = Grunnbeløp.FastsattI2018,
            gjeldendeGrunnbeløpForDagensDato = Grunnbeløp.FastsattI2019
        )

        when (val beregningsResultat = BruttoArbeidsinntektDeSiste36AvsluttedeKalendermånedene().calculate(fakta)) {
            is BeregningsResultat -> {
                beregningsResultat.harAvkortet shouldBe false
                beregningsResultat.avkortet shouldBe beregningsResultat.uavkortet
            } else -> beregningsResultat.shouldBeTypeOf<BeregningsResultat>()
        }
    }
}
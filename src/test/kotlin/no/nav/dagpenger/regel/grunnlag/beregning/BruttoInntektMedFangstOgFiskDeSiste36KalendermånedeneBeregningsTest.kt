package no.nav.dagpenger.regel.grunnlag.beregning

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import no.nav.dagpenger.grunnbelop.Grunnbeløp
import no.nav.dagpenger.regel.grunnlag.Fakta
import org.junit.jupiter.api.Test

class BruttoInntektMedFangstOgFiskDeSiste36KalendermånedeneBeregningsTest {

    private val beregning = BruttoInntektMedFangstOgFiskDeSiste36AvsluttedeKalendermånedene()

    @Test
    fun `Skal ikke behandle lærlinger der beregningsdato er definert innenfor korona periode (20 mars til 31 desember 2020)`() {
        val fakta = Fakta(
            inntekt = null,
            fangstOgFisk = false,
            lærling = true,
            verneplikt = false,
            beregningsdato = LocalDate.of(2020, 3, 20),
            gjeldendeGrunnbeløpVedBeregningsdato = Grunnbeløp.FastsattI2018,
            gjeldendeGrunnbeløpForDagensDato = Grunnbeløp.FastsattI2019
        )
        false shouldBe beregning.isActive(fakta)
    }

    @Test
    fun `Skal ikke behandle lærlinger som ordinær der beregningsdato er definert utenfor korona periode (20 mars til 31 desember 2020)`() {
        val fakta = Fakta(
            inntekt = null,
            fangstOgFisk = false,
            lærling = true,
            verneplikt = false,
            beregningsdato = LocalDate.of(2020, 3, 1),
            gjeldendeGrunnbeløpVedBeregningsdato = Grunnbeløp.FastsattI2018,
            gjeldendeGrunnbeløpForDagensDato = Grunnbeløp.FastsattI2019
        )
        true shouldBe beregning.isActive(fakta)
    }

    @Test
    fun ` Skal gi grunnlag på 4115 siste 36 kalendermåned gitt mars 2019 inntekt med fangstOgFisk inntekt når fangst og fisk er satt`() {

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
                    )
                )
            ),
            KlassifisertInntektMåned(
                YearMonth.of(2017, 5),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(1000),
                        InntektKlasse.FANGST_FISKE
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
            fangstOgFisk = true,
            verneplikt = false,
            beregningsdato = LocalDate.of(2019, 4, 1),
            gjeldendeGrunnbeløpVedBeregningsdato = Grunnbeløp.FastsattI2018,
            gjeldendeGrunnbeløpForDagensDato = Grunnbeløp.FastsattI2018
        )

        when (val beregningsResultat = beregning.calculate(fakta)) {
            is BeregningsResultat ->
                beregningsResultat.uavkortet shouldBe BigDecimal("1371.97393512841033163333")
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
            ),
            KlassifisertInntektMåned(
                YearMonth.of(2017, 5),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(1000),
                        InntektKlasse.FANGST_FISKE
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
                        InntektKlasse.FANGST_FISKE
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
            gjeldendeGrunnbeløpVedBeregningsdato = Grunnbeløp.FastsattI2019,
            gjeldendeGrunnbeløpForDagensDato = Grunnbeløp.FastsattI2019
        )

        when (val beregningsResultat = beregning.calculate(fakta)) {
            is BeregningsResultat ->
                beregningsResultat.uavkortet shouldBe BigDecimal("355.49052694534036781667")
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
                        InntektKlasse.FANGST_FISKE
                    )
                )
            ),
            KlassifisertInntektMåned(
                YearMonth.of(2016, 10),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(1000),
                        InntektKlasse.FANGST_FISKE
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
            gjeldendeGrunnbeløpVedBeregningsdato = Grunnbeløp.FastsattI2019,
            gjeldendeGrunnbeløpForDagensDato = Grunnbeløp.FastsattI2019
        )

        when (val beregningsResultat = beregning.calculate(fakta)) {
            is BeregningsResultat ->
                beregningsResultat.uavkortet shouldBe BigDecimal("-355.49052694534036781667")
            else -> beregningsResultat.shouldBeTypeOf<BeregningsResultat>()
        }
    }

    @Test
    fun `Skal returnere IngenBeregningsResultat fra denne reglenen hvis ingen inntekt`() {

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
                beregningsResultat.beskrivelse shouldBe
                "FangstOgFiskSiste36"
            else -> beregningsResultat.shouldBeTypeOf<IngenBeregningsResultat>()
        }
    }
}

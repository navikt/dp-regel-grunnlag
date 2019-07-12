package no.nav.dagpenger.regel.grunnlag.beregning

import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import no.nav.dagpenger.regel.grunnlag.Fakta
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import kotlin.test.assertEquals

class BruttoInntektMedFangstOgFiskDeSiste36KalendermånedeneBeregningsTest {

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
            beregningsdato = LocalDate.of(2019, 4, 1)
        )

        assertEquals(
            BigDecimal("1371.97393512841033163333"),
            BruttoInntektMedFangstOgFiskDeSiste36AvsluttedeKalendermånedene().calculate(fakta).uavkortet
        )
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
            beregningsdato = LocalDate.of(2019, 2, 10)
        )

        assertEquals(
            BigDecimal("344.89964471595075862667"),
            BruttoInntektMedFangstOgFiskDeSiste36AvsluttedeKalendermånedene().calculate(fakta).uavkortet
        )
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
            beregningsdato = LocalDate.of(2019, 2, 10)
        )

        assertEquals(
            BigDecimal("-344.89964471595075862667"),
            BruttoInntektMedFangstOgFiskDeSiste36AvsluttedeKalendermånedene().calculate(fakta).uavkortet
        )
    }

    @Test
    fun `Skal returnere 0 som grunnlag hvis ingen inntekt`() {

        val fakta = Fakta(
            inntekt = Inntekt("123", emptyList(), sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 3)),
            fangstOgFisk = false,
            verneplikt = false,
            beregningsdato = LocalDate.of(2019, 4, 1)
        )

        assertEquals(
            BigDecimal.ZERO,
            BruttoInntektMedFangstOgFiskDeSiste36AvsluttedeKalendermånedene().calculate(fakta).uavkortet
        )
    }
}
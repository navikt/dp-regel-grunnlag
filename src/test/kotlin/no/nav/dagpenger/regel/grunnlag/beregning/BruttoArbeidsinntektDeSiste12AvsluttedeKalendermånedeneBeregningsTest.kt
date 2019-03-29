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
import kotlin.test.assertTrue

class BruttoArbeidsinntektDeSiste12AvsluttedeKalendermånedeneBeregningsTest {

    @Test
    fun ` Skal gi uavkortet grunnlag på 2034,699 siste 12 kalendermåned gitt mars 2019 inntekt`() {

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
            )
        )

        val fakta = Fakta(
            inntekt = Inntekt("123", inntektsListe),
            senesteInntektsmåned = YearMonth.of(2019, 3),
            fangstOgFisk = false,
            verneplikt = false,
            beregningsdato = LocalDate.of(2019, 4, 1)
        )

        assertEquals(
            BigDecimal("2034.69893414785227588000"),
            BruttoArbeidsinntektDeSiste12AvsluttedeKalendermånedene().calculate(fakta).uavkortet
        )
    }

    @Test
    fun `Skal gi riktig avkortet grunnlag siste 12 kalendermåneder gitt mars 2019 inntekt ` () {

        val inntektsListe = listOf(
            KlassifisertInntektMåned(
                YearMonth.of(2018, 4),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(300000),
                        InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            ),
            KlassifisertInntektMåned(
                YearMonth.of(2018, 5),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(300000),
                        InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            )
        )

        val fakta = Fakta(
            inntekt = Inntekt("123", inntektsListe),
            senesteInntektsmåned = YearMonth.of(2019, 3),
            fangstOgFisk = false,
            verneplikt = false,
            beregningsdato = LocalDate.of(2019, 4, 1)
        )

        val resultat = BruttoArbeidsinntektDeSiste12AvsluttedeKalendermånedene().calculate(fakta)
        assertEquals(
            BigDecimal("581298"),
            resultat.avkortet
        )

        assertTrue(resultat.harAvkortet)
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
            )
        )

        val fakta = Fakta(
            inntekt = Inntekt("123", inntektsListe),
            senesteInntektsmåned = YearMonth.of(2019, 3),
            fangstOgFisk = false,
            verneplikt = false,
            beregningsdato = LocalDate.of(2019, 4, 1)
        )

        assertEquals(
            BigDecimal("2034.69893414785227588000"),
            BruttoArbeidsinntektDeSiste12AvsluttedeKalendermånedene().calculate(fakta).uavkortet
        )
    }

    @Test
    fun `Skal returnere 0 som grunnlag hvis ingen inntekt`() {

        val fakta = Fakta(
            inntekt = Inntekt("123", emptyList()),
            senesteInntektsmåned = YearMonth.of(2019, 3),
            fangstOgFisk = false,
            verneplikt = false,
            beregningsdato = LocalDate.of(2019, 4, 1)
        )

        assertEquals(
            BigDecimal.ZERO,
            BruttoArbeidsinntektDeSiste12AvsluttedeKalendermånedene().calculate(fakta).uavkortet
        )
    }
}
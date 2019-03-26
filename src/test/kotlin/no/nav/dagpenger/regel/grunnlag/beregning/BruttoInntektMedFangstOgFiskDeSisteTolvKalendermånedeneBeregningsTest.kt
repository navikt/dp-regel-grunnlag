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

class BruttoInntektMedFangstOgFiskDeSisteTolvKalendermånedeneBeregningsTest {

    @Test
    fun ` Skal gi grunnlag på 2034,699 siste 12 kalendermåned gitt mars 2019 `() {

        val inntektsListe = listOf(
            KlassifisertInntektMåned(
                YearMonth.of(2018, 4),
                listOf(KlassifisertInntekt(
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
                listOf(KlassifisertInntekt(
                    BigDecimal(500),
                    InntektKlasse.ARBEIDSINNTEKT
                ), KlassifisertInntekt(
                    BigDecimal(500 ),
                    InntektKlasse.SYKEPENGER_FANGST_FISKE
                )
                )
            )
        )

        val fakta = Fakta(
            inntekt = Inntekt("123", inntektsListe),
            senesteInntektsmåned = YearMonth.of(2019, 3),
            verneplikt = false,
            fangstOgFisk = true,
            beregningsdato = LocalDate.of(2019, 4, 1)
        )

        assertEquals(BigDecimal("2034.699000"), BruttoInntektMedFangstOgFiskDeSiste12AvsluttedeKalendermånedene().calculate(fakta))
    }

    @Test
    fun ` Skal gi grunnlag på 0 dersom fangst og fisk ikke er satt `() {

        val inntektsListe = listOf(
            KlassifisertInntektMåned(
                YearMonth.of(2018, 4),
                listOf(KlassifisertInntekt(
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
                listOf(KlassifisertInntekt(
                    BigDecimal(500),
                    InntektKlasse.ARBEIDSINNTEKT
                ), KlassifisertInntekt(
                    BigDecimal(500 ),
                    InntektKlasse.SYKEPENGER_FANGST_FISKE
                )
                )
            )
        )

        val fakta = Fakta(
            inntekt = Inntekt("123", inntektsListe),
            senesteInntektsmåned = YearMonth.of(2019, 3),
            verneplikt = false,
            fangstOgFisk = false,
            beregningsdato = LocalDate.of(2019, 4, 1)
        )

        assertEquals(BigDecimal(0), BruttoInntektMedFangstOgFiskDeSiste12AvsluttedeKalendermånedene().calculate(fakta))
    }
}
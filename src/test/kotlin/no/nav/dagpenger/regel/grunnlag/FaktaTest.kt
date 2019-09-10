package no.nav.dagpenger.regel.grunnlag

import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import no.nav.dagpenger.grunnbelop.Grunnbeløp
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import java.util.EnumSet
import kotlin.test.assertEquals

internal class FaktaTest {

    private val inntekt = Inntekt(
        "123",
        listOf(
            KlassifisertInntektMåned(
                YearMonth.of(2018, 5),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(1000),
                        InntektKlasse.ARBEIDSINNTEKT
                    ),
                    KlassifisertInntekt(
                        BigDecimal(2000),
                        InntektKlasse.DAGPENGER
                    )
                )
            ),
            KlassifisertInntektMåned(
                YearMonth.of(2018, 4),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(500),
                        InntektKlasse.ARBEIDSINNTEKT
                    ),
                    KlassifisertInntekt(
                        BigDecimal(2000),
                        InntektKlasse.DAGPENGER
                    )
                )
            )

        ),
        sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 3)
    )

    @Test
    fun ` Skal returnere en liste over inntektene måned for måned når inntekt er satt `() {

        val fakta = Fakta(
            inntekt = inntekt,
            verneplikt = false,
            fangstOgFisk = false,
            beregningsdato = LocalDate.of(2019, 4, 1),
            gjeldendeGrunnbeløpVedBeregningsdato = Grunnbeløp.FastsattI2018,
            gjeldendeGrunnbeløpForDagensDato = Grunnbeløp.FastsattI2019
        )

        val expected = BigDecimal("5586.74733536963068970000")
        assertEquals(
            expected,
            fakta.oppjusterteInntekterFørstePeriode(EnumSet.of(InntektKlasse.ARBEIDSINNTEKT, InntektKlasse.DAGPENGER))
        )
    }

    @Test
    fun `Skal bruke kun inntekter som er i inntektsklasse parameteret `() {
        val fakta = Fakta(
            inntekt = inntekt,
            verneplikt = false,
            fangstOgFisk = false,
            beregningsdato = LocalDate.of(2019, 4, 1),
            gjeldendeGrunnbeløpVedBeregningsdato = Grunnbeløp.FastsattI2018,
            gjeldendeGrunnbeløpForDagensDato = Grunnbeløp.FastsattI2019
        )

        val expected = 0.toBigDecimal()
        assertEquals(
            expected,
            fakta.oppjusterteInntekterFørstePeriode(EnumSet.noneOf(InntektKlasse::class.java))
        )
    }
}
package no.nav.dagpenger.regel.grunnlag

import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
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

        )
    )


    @Test
    fun ` Skal returnere en liste over inntektene måned for måned `() {

        val fakta = Fakta(inntekt, YearMonth.of(2019, 3), false, false, LocalDate.of(2019, 4, 1))

        val expected = mapOf(
            YearMonth.of(2018, 5) to 3000.toBigDecimal(),
            YearMonth.of(2018, 4) to 2500.toBigDecimal()
        )

        assertEquals(
            expected,
            fakta.sumMåneder(EnumSet.of(InntektKlasse.ARBEIDSINNTEKT, InntektKlasse.DAGPENGER))
        )
    }

    @Test
    fun `Skal bruke kun inntekter som er i inntektsklasse parameteret ` (){
        val fakta = Fakta(inntekt, YearMonth.of(2019, 3), false, false, LocalDate.of(2019, 4, 1))

        val expected = mapOf(
            YearMonth.of(2018, 4) to BigDecimal.ZERO,
            YearMonth.of(2018, 5) to BigDecimal.ZERO
        )

        assertEquals(
            expected,
            fakta.sumMåneder(EnumSet.noneOf(InntektKlasse::class.java))
        )
    }
}
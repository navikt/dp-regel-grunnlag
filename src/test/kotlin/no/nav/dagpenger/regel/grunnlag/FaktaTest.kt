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

class FaktaTest {

    @Test
    fun ` Skal returnere en liste over inntektene måned for måned `(){

        val inntekt = Inntekt(
            "123",
            listOf (
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
                    YearMonth.of(2018, 5),
                    listOf(
                        KlassifisertInntekt(
                            BigDecimal(2000),
                            InntektKlasse.DAGPENGER
                        )
                    )
                )
            )
        )

        val fakta = Fakta(inntekt, YearMonth.of(2019, 3), false, false, LocalDate.of(2019, 4,1))

        assertEquals(listOf(YearMonth.of(2018, 5) to 3000.toBigDecimal()), fakta.sumMåneder(EnumSet.of(InntektKlasse.ARBEIDSINNTEKT, InntektKlasse.DAGPENGER)))
    }

}
package no.nav.dagpenger.regel.grunnlag.beregning

import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.grunnbelop.Grunnbeløp
import no.nav.dagpenger.regel.grunnlag.Fakta
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManueltGrunnlagBeregningsTest {

    @Test
    fun ` Skal gi avkortet grunnlag lik uavkortet grunnlag når uavkortet er under 6 G `() {
        val fakta = Fakta(
            Inntekt(
                "123",
                emptyList(), sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 3)
            ),
            false,
            false,
            LocalDate.of(2019, 4, 10),
            manueltGrunnlag = 50000,
            gjeldendeGrunnbeløpVedBeregningsdato = Grunnbeløp.FastsattI2018,
            gjeldendeGrunnbeløpForDagensDato = Grunnbeløp.FastsattI2019
        )

        assertEquals(BigDecimal("50000"), ManueltGrunnlagBeregning().calculate(fakta).uavkortet)
        assertEquals(BigDecimal("50000"), ManueltGrunnlagBeregning().calculate(fakta).avkortet)
        assertEquals("Manuell", ManueltGrunnlagBeregning().calculate(fakta).beregningsregel)
        assertFalse(ManueltGrunnlagBeregning().calculate(fakta).harAvkortet)
    }

    @Test
    fun ` Skal gi avkortet grunnlag lik 6G når uavkortet grunnlag er høyere enn 6G `() {
        val fakta = Fakta(
            Inntekt(
                "123",
                emptyList(), sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 3)
            ),
            false,
            false,
            LocalDate.of(2019, 4, 10),
            manueltGrunnlag = 600000,
            gjeldendeGrunnbeløpVedBeregningsdato = Grunnbeløp.FastsattI2018,
            gjeldendeGrunnbeløpForDagensDato = Grunnbeløp.FastsattI2019
        )

        assertEquals(BigDecimal("600000"), ManueltGrunnlagBeregning().calculate(fakta).uavkortet)
        assertEquals(BigDecimal("581298"), ManueltGrunnlagBeregning().calculate(fakta).avkortet)
        assertEquals("Manuell", ManueltGrunnlagBeregning().calculate(fakta).beregningsregel)
        assertTrue(ManueltGrunnlagBeregning().calculate(fakta).harAvkortet)
    }
}
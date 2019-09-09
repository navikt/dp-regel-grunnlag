package no.nav.dagpenger.regel.grunnlag.beregning

import no.nav.dagpenger.events.inntekt.v1.Inntekt
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
            manueltGrunnlag = 50000
        )

        when (val beregningsResultat = ManueltGrunnlagBeregning().calculate(fakta)) {
            is BeregningsResultat -> {
                assertEquals(BigDecimal("50000"), beregningsResultat.uavkortet)
                assertEquals(BigDecimal("50000"), beregningsResultat.avkortet)
                assertEquals("Manuell", beregningsResultat.beregningsregel)
                assertFalse(beregningsResultat.harAvkortet)
            }
        }
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
            manueltGrunnlag = 600000
        )

        when (val beregningsResultat = ManueltGrunnlagBeregning().calculate(fakta)) {
            is BeregningsResultat -> {
                assertEquals(BigDecimal("600000"), beregningsResultat.uavkortet)
                assertEquals(BigDecimal("581298"), beregningsResultat.avkortet)
                assertEquals("Manuell", beregningsResultat.beregningsregel)
                assertTrue(beregningsResultat.harAvkortet)
            }
        }
    }
}
package no.nav.dagpenger.regel.grunnlag.beregning

import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.grunnbelop.Grunnbeløp
import no.nav.dagpenger.regel.grunnlag.Fakta
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.YearMonth
import kotlin.test.assertEquals

class DagpengerEtterAvtjentVernepliktBeregningsTest {

    @Test
    fun `Skal få uavkortet grunnlag på 3G når verneplikt er satt`() {

        val fakta = Fakta(
            inntekt = Inntekt("123", emptyList(), sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 3)),
            verneplikt = true,
            fangstOgFisk = false,
            beregningsdato = LocalDate.of(2019, 4, 1),
            gjeldendeGrunnbeløpVedBeregningsdato = Grunnbeløp.FastsattI2019,
            gjeldendeGrunnbeløpForDagensDato = Grunnbeløp.FastsattI2019
        )

        assertEquals(299574.toBigDecimal(), DagpengerEtterAvtjentVerneplikt().calculate(fakta).uavkortet)
    }

    @Test
    fun `Skal få uavkortet grunnlag på 0 når verneplikt ikke er satt`() {

        val fakta = Fakta(
            inntekt = Inntekt("123", emptyList(), sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 3)),
            verneplikt = false,
            fangstOgFisk = false,
            beregningsdato = LocalDate.of(2019, 4, 1),
            gjeldendeGrunnbeløpVedBeregningsdato = Grunnbeløp.FastsattI2019,
            gjeldendeGrunnbeløpForDagensDato = Grunnbeløp.FastsattI2019
        )

        assertEquals(0.toBigDecimal(), DagpengerEtterAvtjentVerneplikt().calculate(fakta).uavkortet)
    }
}
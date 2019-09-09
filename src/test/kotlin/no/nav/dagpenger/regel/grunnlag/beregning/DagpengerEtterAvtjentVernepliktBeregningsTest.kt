package no.nav.dagpenger.regel.grunnlag.beregning

import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.regel.grunnlag.Fakta
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.YearMonth
import kotlin.test.assertEquals

class DagpengerEtterAvtjentVernepliktBeregningsTest {

    @Test
    fun `Skal få uavkortet grunnlag på 3G når verneplikt er satt`() {

        val fakta = Fakta(
            Inntekt("123", emptyList(), sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 3)),
            true,
            false,
            LocalDate.of(2019, 4, 1)
        )

        when (val beregningsResultat = DagpengerEtterAvtjentVerneplikt().calculate(fakta)) {
            is BeregningsResultat -> assertEquals(
                290649.toBigDecimal(),
                beregningsResultat.uavkortet
            )
        }
    }

    @Test
    fun `Skal få ingenBeregningsResultat på verneplikt når den ikke er satt`() {

        val fakta = Fakta(
            Inntekt("123", emptyList(), sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 3)),
            false,
            false,
            LocalDate.of(2019, 4, 1)
        )

        when (val beregningsResultat = DagpengerEtterAvtjentVerneplikt().calculate(fakta)) {
            is IngenBeregningsResultat -> assertEquals(
                "Verneplikt",
                beregningsResultat.beskrivelse
            )
        }
    }
}
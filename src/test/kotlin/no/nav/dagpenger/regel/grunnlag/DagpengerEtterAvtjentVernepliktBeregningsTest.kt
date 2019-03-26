package no.nav.dagpenger.regel.grunnlag

import no.nav.dagpenger.events.inntekt.v1.Inntekt
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.YearMonth
import kotlin.test.assertEquals

class DagpengerEtterAvtjentVernepliktBeregningsTest {

    @Test
    fun `Skal få uavkortet grunnlag på 3G når verneplikt er satt`() {

        val fakta = Fakta(Inntekt("123", emptyList()), YearMonth.of(2019, 3), true, false, LocalDate.of(2019, 4, 1))

        assertEquals(290649.toBigDecimal(), DagpengerEtterAvtjentVerneplikt(fakta).resultat)
    }

    @Test
    fun `Skal få uavkortet grunnlag på 0 når verneplikt ikke er satt`() {

        val fakta = Fakta(Inntekt("123", emptyList()), YearMonth.of(2019, 3), false, false, LocalDate.of(2019, 4, 1))

        assertEquals(0.toBigDecimal(), DagpengerEtterAvtjentVerneplikt(fakta).resultat)
    }
}
package no.nav.dagpenger.regel.grunnlag.beregning

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BeregningsResultatTest {

    @Test
    fun `Skal returnere den av beregningsresultatene med hlyest avkortet grunnlag`() {

        val resultater = listOf(
            BeregningsResultat(1000.toBigDecimal(), 500.toBigDecimal(), "Regel1", true),
            BeregningsResultat(1000.toBigDecimal(), 1000.toBigDecimal(), "Regel2", false),
            BeregningsResultat(500.toBigDecimal(), 500.toBigDecimal(), "Regel3", true))

        assertEquals(1000.toBigDecimal(), resultater.finnHøyeste()?.avkortet)
        assertEquals("Regel2", resultater.finnHøyeste()?.beregningsregel)
    }

    @Test
    fun `Skal returnere beregningsregel Ordinær dersom verneplikt og ordinær gir likt avkortet grunnlag`() {
        val resultater = setOf(
            BeregningsResultat(1000.toBigDecimal(), 1000.toBigDecimal(), "Verneplikt", true),
            BeregningsResultat(1000.toBigDecimal(), 1000.toBigDecimal(), "Verneplikt", true),
            BeregningsResultat(1000.toBigDecimal(), 1000.toBigDecimal(), "Verneplikt", true),
            BeregningsResultat(1000.toBigDecimal(), 1000.toBigDecimal(), "Ordinær", false),
            BeregningsResultat(1000.toBigDecimal(), 1000.toBigDecimal(), "Verneplikt", true),
            BeregningsResultat(1000.toBigDecimal(), 1000.toBigDecimal(), "Verneplikt", true),
            BeregningsResultat(1000.toBigDecimal(), 1000.toBigDecimal(), "Verneplikt", true))

        assertEquals(1000.toBigDecimal(), resultater.finnHøyeste()?.avkortet)
        assertEquals("Ordinær", resultater.finnHøyeste()?.beregningsregel)
    }
}
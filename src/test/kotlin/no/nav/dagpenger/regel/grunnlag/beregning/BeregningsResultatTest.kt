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

        assertEquals(1000.toBigDecimal(), resultater.finnHøyesteAvkortetVerdi()?.avkortet)
        assertEquals("Regel2", resultater.finnHøyesteAvkortetVerdi()?.beregningsregel)
    }

    @Test
    fun `Skal returnere beregningsregel Ordinær dersom verneplikt og ordinær gir likt avkortet grunnlag`() {
        val resultater = setOf(
            BeregningsResultat(1000.toBigDecimal(), 1000.toBigDecimal(), "Verneplikt", true),
            BeregningsResultat(1000.toBigDecimal(), 1000.toBigDecimal(), "Ordinær", false))

        assertEquals(1000.toBigDecimal(), resultater.finnHøyesteAvkortetVerdi()?.avkortet)
        assertEquals("Ordinær", resultater.finnHøyesteAvkortetVerdi()?.beregningsregel)
    }

    @Test
    fun `Skal returnere beregningsregel Vernelikt dersom verneplikt gir høyest avkortet grunnlag`() {
        val resultater = setOf(
            BeregningsResultat(1000.toBigDecimal(), 2000.toBigDecimal(), "Verneplikt", true),
            BeregningsResultat(1000.toBigDecimal(), 1000.toBigDecimal(), "Ordinær", false))

        assertEquals(2000.toBigDecimal(), resultater.finnHøyesteAvkortetVerdi()?.avkortet)
        assertEquals("Verneplikt", resultater.finnHøyesteAvkortetVerdi()?.beregningsregel)
    }
}
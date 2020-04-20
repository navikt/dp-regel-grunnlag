package no.nav.dagpenger.regel.grunnlag.beregning

import io.kotlintest.assertSoftly
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BeregningsResultatTest {

    @Test
    fun `Skal returnere den av beregningsresultatene med høyest avkortet grunnlag`() {

        val resultater = listOf(
            BeregningsResultat(1000.toBigDecimal(), 500.toBigDecimal(), "Regel1", true),
            BeregningsResultat(1000.toBigDecimal(), 1000.toBigDecimal(), "Regel2", false),
            BeregningsResultat(500.toBigDecimal(), 500.toBigDecimal(), "Regel3", true))

        resultater.finnHøyesteAvkortetVerdi()?.avkortet shouldBe 1000.toBigDecimal()
        resultater.finnHøyesteAvkortetVerdi()?.beregningsregel shouldBe "Regel2"
        assertEquals(1000.toBigDecimal(), resultater.finnHøyesteAvkortetVerdi()?.avkortet)
    }

    @Test
    fun `Skal returnere beregningsregel Ordinær dersom verneplikt og ordinær gir likt avkortet grunnlag`() {
        val resultater = setOf(
            BeregningsResultat(1000.toBigDecimal(), 1000.toBigDecimal(), "Verneplikt", true),
            BeregningsResultat(1000.toBigDecimal(), 1000.toBigDecimal(), "Ordinær", false))

        resultater.finnHøyesteAvkortetVerdi()?.avkortet shouldBe 1000.toBigDecimal()
        resultater.finnHøyesteAvkortetVerdi()?.beregningsregel shouldBe "Ordinær"
    }

    @Test
    fun `Skal returnere beregningsregel Vernelikt dersom verneplikt gir høyest avkortet grunnlag`() {
        val resultater = setOf(
            BeregningsResultat(1000.toBigDecimal(), 2000.toBigDecimal(), "Verneplikt", true),
            BeregningsResultat(1000.toBigDecimal(), 1000.toBigDecimal(), "Ordinær", false))

        resultater.finnHøyesteAvkortetVerdi()?.avkortet shouldBe 2000.toBigDecimal()
        resultater.finnHøyesteAvkortetVerdi()?.beregningsregel shouldBe "Verneplikt"
    }

    @Test
    fun `Skal alltid returnere manuelt grunnlag dersom det er satt`() {
        val resultater = setOf(
            BeregningsResultat(100000.toBigDecimal(), 20000.toBigDecimal(), "Verneplikt", true),
            BeregningsResultat(1000.toBigDecimal(), 2000.toBigDecimal(), "Manuell", true),
            BeregningsResultat(10000.toBigDecimal(), 10000.toBigDecimal(), "Ordinær", false))

        resultater.finnHøyesteAvkortetVerdi()?.avkortet shouldBe 2000.toBigDecimal()
        resultater.finnHøyesteAvkortetVerdi()?.beregningsregel shouldBe "Manuell"
    }

    @Test
    fun `Skal ikke returnere manuelt grunnlag dersom det ikke er satt`() {
        val resultater = setOf(
            BeregningsResultat(1000.toBigDecimal(), 2000.toBigDecimal(), "Verneplikt", true),
            BeregningsResultat(0.toBigDecimal(), 0.toBigDecimal(), "Manuell", true),
            BeregningsResultat(10000.toBigDecimal(), 10000.toBigDecimal(), "Ordinær", false))

        resultater.finnHøyesteAvkortetVerdi()?.avkortet shouldBe 10000.toBigDecimal()
        resultater.finnHøyesteAvkortetVerdi()?.beregningsregel shouldBe "Ordinær"
    }

    @Test
    fun `Skal returnere beregningsregel Lærling selvom lærling gir mindre enn og ordinær avkortet grunnlag`() {

        val resultater = setOf(
            BeregningsResultat(2000.toBigDecimal(), 2000.toBigDecimal(), "LærlingFangstOgFisk1x12", true),
            BeregningsResultat(4000.toBigDecimal(), 4000.toBigDecimal(), "LærlingFangstOgFisk3x4", true))

        assertSoftly {
            with(resultater.finnHøyesteAvkortetVerdi()!!) {
                this.avkortet shouldBe 4000.toBigDecimal()
                this.beregningsregel shouldBe "LærlingFangstOgFisk3x4"
            }
        }
    }

    @Test
    fun `Skal returnere beste grunnlag for Lærling`() {

        val resultater = setOf(
            BeregningsResultat(100.toBigDecimal(), 100.toBigDecimal(), "LærlingFangstOgFisk1x12", true),
            BeregningsResultat(1000.toBigDecimal(), 1000.toBigDecimal(), "LærlingArbeidsinntekt1x12", true),
            BeregningsResultat(500.toBigDecimal(), 500.toBigDecimal(), "LærlingFangstOgFisk3x4", true),
            BeregningsResultat(11.toBigDecimal(), 11.toBigDecimal(), "LærlingArbeidsinntekt3x4", true)
        )

        assertSoftly {
            with(resultater.finnHøyesteAvkortetVerdi()!!) {
                this.avkortet shouldBe 1000.toBigDecimal()
                this.beregningsregel shouldBe "LærlingArbeidsinntekt1x12"
            }
        }
    }


    @Test
    fun `Skal alltid returnere manuelt grunnlag dersom det er satt i lærlingsforskrift`() {
        val resultater = setOf(
            BeregningsResultat(100000.toBigDecimal(), 20000.toBigDecimal(), "LærlingFangstOgFisk1x12", true),
            BeregningsResultat(1000.toBigDecimal(), 2000.toBigDecimal(), "Manuell", true),
            BeregningsResultat(10000.toBigDecimal(), 10000.toBigDecimal(), "LærlingFangstOgFisk3x4", false))

        resultater.finnHøyesteAvkortetVerdi()?.avkortet shouldBe 2000.toBigDecimal()
        resultater.finnHøyesteAvkortetVerdi()?.beregningsregel shouldBe "Manuell"
    }

}
package no.nav.dagpenger.regel.grunnlag

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.YearMonth

class GrunnlagInputTest {

    @Test
    fun `Should not process behov without inntekt`() {
        val behov = SubsumsjonsBehov.Builder().build()

        assertFalse(shouldBeProcessed(behov))
    }

    @Test
    fun `Process behov with inntekt`() {

        val behov = SubsumsjonsBehov.Builder()
            .inntekt(Inntekt("123", emptyList()))
            .senesteInntektsMåned(YearMonth.of(2018, 1))
            .build()

        assert(shouldBeProcessed(behov))
    }

    @Test
    fun `Do not reprocess behov with grunnlagResultat`() {
        val behov = SubsumsjonsBehov.Builder()
            .grunnlagResultat(
                GrunnlagResultat(
                    "123",
                    "987",
                    "555",
                    BigDecimal(2000),
                    BigDecimal(2000)))
            .inntekt(Inntekt("123", emptyList()))
            .senesteInntektsMåned(YearMonth.of(2018, 1))
            .build()

        assertFalse(shouldBeProcessed(behov))
    }
}

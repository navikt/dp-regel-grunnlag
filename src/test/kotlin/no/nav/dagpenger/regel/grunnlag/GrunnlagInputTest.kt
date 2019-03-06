package no.nav.dagpenger.regel.grunnlag

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class GrunnlagInputTest {

    @Test
    fun `Process behov without inntekt and no inntekt tasks`() {
        val behov = SubsumsjonsBehov.Builder().build()

        assert(shouldBeProcessed(behov))
    }

    @Test
    fun `Process behov without inntekt and no hentInntekt task`() {
        val behov = SubsumsjonsBehov.Builder()
            .task(listOf("noe annet"))
            .build()

        assert(shouldBeProcessed(behov))
    }

    @Test
    fun `Do not process behov without inntekt but with hentInntekt task`() {
        val behov = SubsumsjonsBehov.Builder()
            .task(listOf("hentInntekt"))
            .build()
        assertFalse(shouldBeProcessed(behov))
    }

    @Test
    fun `Process behov with inntekt`() {

        val behov = SubsumsjonsBehov.Builder()
            .inntekt(Inntekt("123", emptyList()))
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
            .build()

        assertFalse(shouldBeProcessed(behov))
    }
}

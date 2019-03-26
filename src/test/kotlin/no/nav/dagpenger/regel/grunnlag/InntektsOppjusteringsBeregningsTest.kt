package no.nav.dagpenger.regel.grunnlag

import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertEquals

class InntektsOppjusteringsBeregningsTest {

    @Test
    fun ` Skal oppjustere inntekt for en måned i forhold til grunnbeløpet som var den måneden `() {

        val inntekt: BigDecimal = 20000.toBigDecimal()

        val gjeldendeG: BigDecimal = 2.toBigDecimal()
        val månedsG: BigDecimal = 1.toBigDecimal()

        assertEquals(40000.toBigDecimal(), InntektsOppjusteringsBeregning(inntekt, gjeldendeG, månedsG).resultat)
    }
}
package no.nav.dagpenger.regel.grunnlag

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class GrunnlagsResultatTest {

    @Test
    fun `Skal få grunnlag på 3G når verneplikt er satt `() {
        val resultat = finnUavkortetGrunnlag(true, Inntekt("", 0))
        assertEquals(290649, resultat)
    }

    @Test
    fun `Skal få grunnlag på 0 uten inntekt og uten verneplikt`() {
        val resultat = finnUavkortetGrunnlag(false, Inntekt("", 0))
        assertEquals(0, resultat)
    }
}
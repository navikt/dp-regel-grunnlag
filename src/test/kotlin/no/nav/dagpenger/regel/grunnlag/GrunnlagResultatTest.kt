package no.nav.dagpenger.regel.grunnlag

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class GrunnlagResultatTest {

    @Test
    fun `toMap skal returnere rett resultat`() {
        val grunnlagResultat = GrunnlagResultat(
            "111",
            "222",
            "Grunnlag.v1",
            4455.toBigDecimal(),
            1122.toBigDecimal(),
            "ORDINÆR",
            true)

        assertEquals("111", grunnlagResultat.sporingsId)
        assertEquals("222", grunnlagResultat.subsumsjonsId)
        assertEquals("Grunnlag.v1", grunnlagResultat.regelidentifikator)
        assertEquals(4455.toBigDecimal(), grunnlagResultat.avkortetGrunnlag)
        assertEquals(1122.toBigDecimal(), grunnlagResultat.uavkortetGrunnlag)
        assertEquals("ORDINÆR", grunnlagResultat.beregningsregel)
        assertTrue(grunnlagResultat.harAvkortet)
    }

    @Test
    fun `getAvrundetUavkortet skal avrunde det uavkortede resultatet riktig`() {
        val grunnlagResultat = GrunnlagResultat(
            "123",
            "123",
            "Grunnlag.v1",
            1234.5678.toBigDecimal(),
            100.499.toBigDecimal(),
            "ORDINÆR",
            false)

        assertEquals(100.toBigDecimal(), grunnlagResultat.avrundetUavkortet)
    }

    @Test
    fun `getAvrundetAvkortet skal avrunde det avkortede resultatet riktig`() {
        val grunnlagResultat = GrunnlagResultat(
            "123",
            "123",
            "Grunnlag.v1",
            1234.5678.toBigDecimal(),
            100.499.toBigDecimal(),
            "ORDINÆR",
            false)

        assertEquals(1235.toBigDecimal(), grunnlagResultat.avrundetAvkortet)
    }
}
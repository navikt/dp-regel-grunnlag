package no.nav.dagpenger.regel.grunnlag

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class GrunnlagResultatTest {

    @Test
    fun `toMap skal returnere rett resultat`() {
        val grunnlagResultat = GrunnlagResultat(
            sporingsId = "111",
            subsumsjonsId = "222",
            regelidentifikator = "Grunnlag.v1",
            avkortetGrunnlag = 4455.toBigDecimal(),
            uavkortetGrunnlag = 1122.toBigDecimal(),
            beregningsregel = "ORDINÆR",
            harAvkortet = true,
            grunnbeløpBrukt = 123.toBigDecimal()
        )

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
            sporingsId = "123",
            subsumsjonsId = "123",
            regelidentifikator = "Grunnlag.v1",
            avkortetGrunnlag = 1234.5678.toBigDecimal(),
            uavkortetGrunnlag = 100.499.toBigDecimal(),
            beregningsregel = "ORDINÆR",
            harAvkortet = false,
            grunnbeløpBrukt = 123.toBigDecimal()
        )

        assertEquals(100.toBigDecimal(), grunnlagResultat.avrundetUavkortet)
    }

    @Test
    fun `getAvrundetAvkortet skal avrunde det avkortede resultatet riktig`() {
        val grunnlagResultat = GrunnlagResultat(
            sporingsId = "123",
            subsumsjonsId = "123",
            regelidentifikator = "Grunnlag.v1",
            avkortetGrunnlag = 1234.5678.toBigDecimal(),
            uavkortetGrunnlag = 100.499.toBigDecimal(),
            beregningsregel = "ORDINÆR",
            harAvkortet = false,
            grunnbeløpBrukt = 123.toBigDecimal()
        )

        assertEquals(1235.toBigDecimal(), grunnlagResultat.avrundetAvkortet)
    }
}
package no.nav.dagpenger.regel.grunnlag

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

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
            grunnbeløpBrukt = 123.toBigDecimal(),
        )

        grunnlagResultat.sporingsId shouldBe "111"
        grunnlagResultat.subsumsjonsId shouldBe "222"
        grunnlagResultat.regelidentifikator shouldBe "Grunnlag.v1"
        grunnlagResultat.avkortetGrunnlag shouldBe 4455.toBigDecimal()
        grunnlagResultat.uavkortetGrunnlag shouldBe 1122.toBigDecimal()
        grunnlagResultat.beregningsregel shouldBe "ORDINÆR"
        grunnlagResultat.harAvkortet shouldBe true
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
            grunnbeløpBrukt = 123.toBigDecimal(),
        )

        grunnlagResultat.avrundetUavkortet shouldBe 100.toBigDecimal()
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
            grunnbeløpBrukt = 123.toBigDecimal(),
        )

        grunnlagResultat.avrundetAvkortet shouldBe 1235.toBigDecimal()
    }
}

package no.nav.dagpenger.regel.grunnlag

import java.math.BigDecimal
import java.math.RoundingMode

data class GrunnlagResultat(
    val sporingsId: String,
    val subsumsjonsId: String,
    val regelidentifikator: String,
    val avkortetGrunnlag: BigDecimal,
    val uavkortetGrunnlag: BigDecimal,
    val beregningsregel: String,
    val harAvkortet: Boolean
) {

    companion object {
        val SPORINGSID = "sporingsId"
        val SUBSUMSJONSID = "subsumsjonsId"
        val REGELIDENTIFIKATOR = "regelIdentifikator"
        val AVKORTET_GRUNNLAG = "avkortet"
        val UAVKORTET_GRUNNLAG = "uavkortet"
        val BEREGNINGSREGEL = "beregningsregel"
        val HAR_AVKORTET = "harAvkortet"
    }

    fun toMap(): Map<String, Any> {
        return mapOf(
            SPORINGSID to sporingsId,
            SUBSUMSJONSID to subsumsjonsId,
            REGELIDENTIFIKATOR to regelidentifikator,
            AVKORTET_GRUNNLAG to avrundetAvkortet,
            UAVKORTET_GRUNNLAG to avrundetUavkortet,
            BEREGNINGSREGEL to beregningsregel,
            HAR_AVKORTET to harAvkortet
        )
    }

    val avrundetUavkortet: BigDecimal = uavkortetGrunnlag.setScale(0, RoundingMode.HALF_UP)

    val avrundetAvkortet: BigDecimal = avkortetGrunnlag.setScale(0, RoundingMode.HALF_UP)
}
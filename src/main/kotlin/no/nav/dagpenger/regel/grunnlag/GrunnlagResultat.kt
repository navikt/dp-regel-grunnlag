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
    val harAvkortet: Boolean,
    val grunnbeløpBrukt: BigDecimal,
) {
    companion object {
        const val SPORINGSID = "sporingsId"
        const val SUBSUMSJONSID = "subsumsjonsId"
        const val REGELIDENTIFIKATOR = "regelIdentifikator"
        const val AVKORTET_GRUNNLAG = "avkortet"
        const val UAVKORTET_GRUNNLAG = "uavkortet"
        const val BEREGNINGSREGEL = "beregningsregel"
        const val HAR_AVKORTET = "harAvkortet"
        const val GRUNNBELØP_BRUKT = "grunnbeløpBrukt"
    }

    fun toMap(): Map<String, Any> {
        return mapOf(
            SPORINGSID to sporingsId,
            SUBSUMSJONSID to subsumsjonsId,
            REGELIDENTIFIKATOR to regelidentifikator,
            AVKORTET_GRUNNLAG to avrundetAvkortet,
            UAVKORTET_GRUNNLAG to avrundetUavkortet,
            BEREGNINGSREGEL to beregningsregel,
            HAR_AVKORTET to harAvkortet,
            GRUNNBELØP_BRUKT to grunnbeløpBrukt,
        )
    }

    val avrundetUavkortet: BigDecimal = uavkortetGrunnlag.setScale(0, RoundingMode.HALF_UP)
    val avrundetAvkortet: BigDecimal = avkortetGrunnlag.setScale(0, RoundingMode.HALF_UP)
}

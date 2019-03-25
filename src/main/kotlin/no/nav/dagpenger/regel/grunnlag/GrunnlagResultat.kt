package no.nav.dagpenger.regel.grunnlag

import java.math.BigDecimal
import java.time.YearMonth

data class GrunnlagResultat(
    val sporingsId: String,
    val subsumsjonsId: String,
    val regelidentifikator: String,
    val avkortetGrunnlag: BigDecimal,
    val uavkortetGrunnlag: BigDecimal
) {

    companion object {
        val SPORINGSID = "sporingsId"
        val SUBSUMSJONSID = "subsumsjonsId"
        val REGELIDENTIFIKATOR = "regelIdentifikator"
        val AVKORTET_GRUNNLAG = "avkortet"
        val UAVKORTET_GRUNNLAG = "uavkortet"
    }

    fun toMap(): Map<String, Any> {
        return mapOf(
            SPORINGSID to sporingsId,
            SUBSUMSJONSID to subsumsjonsId,
            REGELIDENTIFIKATOR to regelidentifikator,
            AVKORTET_GRUNNLAG to avkortetGrunnlag,
            UAVKORTET_GRUNNLAG to uavkortetGrunnlag
        )
    }
}

data class InntektsPeriode(
    val førsteMåned: YearMonth,
    val sisteMåned: YearMonth
)

package no.nav.dagpenger.regel.grunnlag

import java.math.BigDecimal
import java.math.RoundingMode

data class RapidGrunnlagResultat(
    val sporingsId: String,
    val subsumsjonsId: String,
    val regelidentifikator: String,
    var avkortetGrunnlag: BigDecimal,
    var uavkortetGrunnlag: BigDecimal,
    val beregningsregel: String,
    val harAvkortet: Boolean,
    val grunnbeløpBrukt: BigDecimal,
    val grunnlagInntektsPerioder: List<InntektPeriodeInfo>?
) {

    init {
        avkortetGrunnlag = avkortetGrunnlag.setScale(0, RoundingMode.HALF_UP)
        uavkortetGrunnlag = uavkortetGrunnlag.setScale(0, RoundingMode.HALF_UP)
    }
}

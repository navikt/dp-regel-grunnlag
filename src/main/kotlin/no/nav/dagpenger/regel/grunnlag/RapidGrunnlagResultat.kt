package no.nav.dagpenger.regel.grunnlag

import java.math.BigDecimal
import java.math.RoundingMode

data class RapidGrunnlagResultat(
    private val avkortetKandidat: BigDecimal,
    private val uavkortetKandidat: BigDecimal,
    val beregningsregel: String,
    val harAvkortet: Boolean,
    val grunnbeløp: BigDecimal,
    val inntektsperioder: List<InntektPeriodeInfo>?
) {
    val avkortet = avkortetKandidat.setScale(0, RoundingMode.HALF_UP)
    val uavkortet = uavkortetKandidat.setScale(0, RoundingMode.HALF_UP)
}

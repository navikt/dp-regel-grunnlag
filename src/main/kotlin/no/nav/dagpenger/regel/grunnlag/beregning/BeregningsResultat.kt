package no.nav.dagpenger.regel.grunnlag.beregning

import java.math.BigDecimal

data class BeregningsResultat(
    val uavkortet: BigDecimal,
    val avkortet: BigDecimal,
    val beregningsregel: String,
    val harAvkortet: Boolean = uavkortet != avkortet
)
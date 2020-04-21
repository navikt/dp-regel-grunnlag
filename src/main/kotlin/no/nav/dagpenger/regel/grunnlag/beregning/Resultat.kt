package no.nav.dagpenger.regel.grunnlag.beregning

import java.math.BigDecimal

sealed class Resultat

data class BeregningsResultat(
    val uavkortet: BigDecimal,
    val avkortet: BigDecimal,
    val beregningsregel: String,
    val harAvkortet: Boolean = uavkortet != avkortet
) : Resultat()

data class IngenBeregningsResultat(
    val beskrivelse: String
) : Resultat()

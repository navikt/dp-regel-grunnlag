package no.nav.dagpenger.regel.grunnlag.beregning

import no.nav.dagpenger.regel.grunnlag.Fakta

abstract class GrunnlagBeregning(val beregningsregel: String) {
    abstract fun calculate(fakta: Fakta): BeregningsResultat
}

val grunnlagsBeregninger = setOf(
    BruttoArbeidsinntektDeSiste12AvsluttedeKalendermånedene(),
    BruttoArbeidsinntektDeSiste36AvsluttedeKalendermånedene(),
    BruttoInntektMedFangstOgFiskDeSiste12AvsluttedeKalendermånedene(),
    BruttoInntektMedFangstOgFiskDeSiste36AvsluttedeKalendermånedene(),
    DagpengerEtterAvtjentVerneplikt()
)
package no.nav.dagpenger.regel.grunnlag.beregning

import no.nav.dagpenger.regel.grunnlag.Fakta
import java.math.BigDecimal

abstract class GrunnlagBeregning {
    abstract fun calculate(fakta: Fakta): BigDecimal
}

val grunnlagsBeregninger = setOf(
    BruttoArbeidsinntektDeSiste12AvsluttedeKalendermånedene(),
    BruttoArbeidsinntektDeSiste36AvsluttedeKalendermånedene(),
    BruttoInntektMedFangstOgFiskDeSiste12AvsluttedeKalendermånedene(),
    BruttoInntektMedFangstOgFiskDeSiste36AvsluttedeKalendermånedene(),
    DagpengerEtterAvtjentVerneplikt()
)
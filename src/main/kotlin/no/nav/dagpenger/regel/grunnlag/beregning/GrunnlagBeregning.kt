package no.nav.dagpenger.regel.grunnlag.beregning

import no.nav.dagpenger.regel.grunnlag.Fakta
import java.math.BigDecimal

abstract class GrunnlagBeregning(val beregningsregel: String) {
    abstract fun calculate(fakta: Fakta): Resultat
}

val lærlingGrunnlagsberegninger = setOf(
    LærlingForskriftSisteAvsluttendeKalenderMånedFangstOgFisk(),
    LærlingForskriftSiste3AvsluttendeKalenderMånedFangsOgFisk(),
    LærlingForskriftSisteAvsluttendeKalenderMåned(),
    LærlingForskriftSiste3AvsluttendeKalenderMåned()
)
val grunnlagsBeregninger = setOf(
    BruttoArbeidsinntektDeSiste12AvsluttedeKalendermånedene(),
    BruttoArbeidsinntektDeSiste36AvsluttedeKalendermånedene(),
    BruttoInntektMedFangstOgFiskDeSiste12AvsluttedeKalendermånedene(),
    BruttoInntektMedFangstOgFiskDeSiste36AvsluttedeKalendermånedene(),
    ManueltGrunnlagBeregning(),
    DagpengerEtterAvtjentVerneplikt()
)

fun Collection<BeregningsResultat>.finnHøyesteAvkortetVerdi() =
    this.maxWith(PresedensOverManueltGrunnlag() then PresedensOverVernepliktHvisAvkortertVerdiErLik())

fun Collection<BeregningsResultat>.finnHøyesteAvkortetVerdiLæring() = this.maxWith(Comparator { o1, o2 -> o1.avkortet.compareTo(o2.avkortet) })

private class PresedensOverManueltGrunnlag : Comparator<BeregningsResultat> {
    override fun compare(resultat1: BeregningsResultat, resultat2: BeregningsResultat): Int {
        return when {
            isManuellBeregningsRegel(resultat1.beregningsregel) && resultat1.avkortet > BigDecimal.ZERO -> 1
            isManuellBeregningsRegel(resultat2.beregningsregel) && resultat2.avkortet > BigDecimal.ZERO -> -1
            else -> 0
        }
    }
}

private class PresedensOverVernepliktHvisAvkortertVerdiErLik : Comparator<BeregningsResultat> {
    override fun compare(resultat1: BeregningsResultat, resultat2: BeregningsResultat): Int {
        return if (resultat1.avkortet == resultat2.avkortet) {
            if (resultat1.beregningsregel != "Verneplikt") resultat1.avkortet.compareTo(resultat2.avkortet) else -1
        } else {
            resultat1.avkortet.compareTo(resultat2.avkortet)
        }
    }
}

package no.nav.dagpenger.regel.grunnlag.beregning

import no.nav.dagpenger.regel.grunnlag.Fakta
import no.nav.dagpenger.regel.grunnlag.NoResultException
import java.math.BigDecimal
import java.time.LocalDate

abstract class GrunnlagBeregning(val beregningsregel: String) {
    abstract fun calculate(fakta: Fakta): Resultat
}

internal class HovedBeregning : GrunnlagBeregning("Hoved") {

    companion object {
        private val lærlingGrunnlagsberegninger = setOf(
            LærlingForskriftSisteAvsluttendeKalenderMånedFangstOgFisk(),
            LærlingForskriftSiste3AvsluttendeKalenderMånedFangsOgFisk(),
            LærlingForskriftSisteAvsluttendeKalenderMåned(),
            LærlingForskriftSiste3AvsluttendeKalenderMåned()
        )
        private val grunnlagsBeregninger = setOf(
            BruttoArbeidsinntektDeSiste12AvsluttedeKalendermånedene(),
            BruttoArbeidsinntektDeSiste36AvsluttedeKalendermånedene(),
            BruttoInntektMedFangstOgFiskDeSiste12AvsluttedeKalendermånedene(),
            BruttoInntektMedFangstOgFiskDeSiste36AvsluttedeKalendermånedene(),
            ManueltGrunnlagBeregning(),
            DagpengerEtterAvtjentVerneplikt()
        )
    }

    override fun calculate(fakta: Fakta): BeregningsResultat {
        return when (fakta.lærling && fakta.beregningsdato.erKoronaPeriode()) {
            true ->
                lærlingGrunnlagsberegninger
                    .map { beregning -> beregning.calculate(fakta) }
                    .filterIsInstance<BeregningsResultat>()
                    .toSet()
                    .finnHøyesteAvkortetVerdiLæring()
                    ?: throw NoResultException("Ingen resultat for grunnlagsberegning")
            else ->
                grunnlagsBeregninger
                    .map { beregning -> beregning.calculate(fakta) }
                    .filterIsInstance<BeregningsResultat>()
                    .toSet()
                    .finnHøyesteAvkortetVerdi()
                    ?: throw NoResultException("Ingen resultat for grunnlagsberegning")
        }
    }
}

private fun LocalDate.erKoronaPeriode() = this in (LocalDate.of(2020, 3, 20)..LocalDate.of(2020, 12, 31))

fun Collection<BeregningsResultat>.finnHøyesteAvkortetVerdi() =
    this.maxWith(PresedensOverManueltGrunnlag() then PresedensOverVernepliktHvisAvkortertVerdiErLik())

fun Collection<BeregningsResultat>.finnHøyesteAvkortetVerdiLæring() =
    this.maxWith(Comparator { o1, o2 -> o1.avkortet.compareTo(o2.avkortet) })

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

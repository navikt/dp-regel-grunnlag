package no.nav.dagpenger.regel.grunnlag.beregning

import no.nav.dagpenger.regel.grunnlag.Fakta
import no.nav.dagpenger.regel.grunnlag.NoResultException
import java.math.BigDecimal
import java.time.LocalDate

abstract class GrunnlagBeregning(val beregningsregel: String) {
    abstract fun isActive(fakta: Fakta): Boolean
    abstract fun calculate(fakta: Fakta): Resultat
}

internal class HovedBeregning : GrunnlagBeregning("Hoved") {

    companion object {
        private val grunnlagsBeregninger = setOf(
            LærlingForskriftSisteAvsluttendeKalenderMånedFangstOgFisk(),
            LærlingForskriftSiste3AvsluttendeKalenderMånedFangsOgFisk(),
            LærlingForskriftSisteAvsluttendeKalenderMåned(),
            LærlingForskriftSiste3AvsluttendeKalenderMåned(),
            BruttoArbeidsinntektDeSiste12AvsluttedeKalendermånedene(),
            BruttoArbeidsinntektDeSiste36AvsluttedeKalendermånedene(),
            BruttoInntektMedFangstOgFiskDeSiste12AvsluttedeKalendermånedene(),
            BruttoInntektMedFangstOgFiskDeSiste36AvsluttedeKalendermånedene(),
            DagpengerEtterAvtjentVerneplikt(),
            ManueltGrunnlagBeregning(),
            ForrigeGrunnlagBeregning()
        )
    }

    override fun isActive(fakta: Fakta): Boolean = true

    override fun calculate(fakta: Fakta): BeregningsResultat {
        return grunnlagsBeregninger
            .filter { it.isActive(fakta) }
            .map { it.calculate(fakta) }
            .filterIsInstance<BeregningsResultat>()
            .toSet()
            .finnHøyesteAvkortetVerdi()
            ?: throw NoResultException("Ingen resultat for grunnlagsberegning")
    }
}

fun LocalDate.erKoronaPeriode() = this in (LocalDate.of(2020, 3, 20)..LocalDate.of(2021, 3, 31))

fun Collection<BeregningsResultat>.finnHøyesteAvkortetVerdi() =
    this.maxWith(PresedensOverManueltGrunnlag() then PresedensOverVernepliktHvisAvkortertVerdiErLik())

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

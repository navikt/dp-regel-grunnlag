package no.nav.dagpenger.regel.grunnlag.beregning

import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.sumInntekt
import no.nav.dagpenger.regel.grunnlag.Fakta
import java.math.BigDecimal
import java.util.EnumSet

abstract class GrunnlagEtterLærlingForskrift(
    private val regelIdentifikator: String,
    private val grunnlagUtvelgelse: GrunnlagUtvelgelse,
    private val inntektKlasser: EnumSet<InntektKlasse>,
) : GrunnlagBeregning(regelIdentifikator) {

    override fun isActive(fakta: Fakta): Boolean {
        val erInnenforRegelverksperiode = fakta.regelverksdato.erKoronaPeriode()
        return fakta.lærling && erInnenforRegelverksperiode && fakta.manueltGrunnlag == null && fakta.forrigeGrunnlag == null
    }

    override fun calculate(fakta: Fakta): Resultat {
        return if (isActive(fakta)) {
            val sisteAvsluttendeKalenderMåned = fakta.inntekt?.sisteAvsluttendeKalenderMåned ?: throw RuntimeException("GrunnlagEtterLærlingForskrift kan bare håndteres hvis inntekt er satt")

            val sortertEtterInntektsmåned =
                fakta.inntektsPerioderOrEmpty.first
                    .filter { it.årMåned.isBefore(sisteAvsluttendeKalenderMåned) || it.årMåned == sisteAvsluttendeKalenderMåned }
                    .sortedByDescending { it.årMåned }

            val uavkortet =
                sortertEtterInntektsmåned.take(grunnlagUtvelgelse.antallMåneder).sumInntekt(inntektKlasser.toList())
                    .multiply(grunnlagUtvelgelse.månedFaktor.toBigDecimal())
            val seksGangerGrunnbeløp = fakta.grunnbeløpVedBeregningsdato().verdi.multiply(BigDecimal(6))
            val avkortet = if (uavkortet > seksGangerGrunnbeløp) seksGangerGrunnbeløp else uavkortet

            BeregningsResultat(
                avkortet = avkortet,
                uavkortet = uavkortet,
                beregningsregel = regelIdentifikator,
            )
        } else {
            IngenBeregningsResultat(regelIdentifikator)
        }
    }
}

sealed class GrunnlagUtvelgelse(
    val antallMåneder: Int,
    val månedFaktor: Int,
)

class SisteAvsluttendeMånedUtvelgelse : GrunnlagUtvelgelse(antallMåneder = 1, månedFaktor = 12)
class Siste3AvsluttendeMånederUtvelgelse : GrunnlagUtvelgelse(antallMåneder = 3, månedFaktor = 4)

class LærlingForskriftSisteAvsluttendeKalenderMånedFangstOgFisk : GrunnlagEtterLærlingForskrift(
    regelIdentifikator = "LærlingFangstOgFisk1x12",
    grunnlagUtvelgelse = SisteAvsluttendeMånedUtvelgelse(),
    inntektKlasser = inntektKlassifisertEtterFangstOgFisk,
) {

    override fun calculate(fakta: Fakta): Resultat {
        return if (fakta.fangstOgFiske) super.calculate(fakta) else IngenBeregningsResultat(this.beregningsregel)
    }
}

class LærlingForskriftSiste3AvsluttendeKalenderMånedFangsOgFisk : GrunnlagEtterLærlingForskrift(
    regelIdentifikator = "LærlingFangstOgFisk3x4",
    grunnlagUtvelgelse = Siste3AvsluttendeMånederUtvelgelse(),
    inntektKlasser = inntektKlassifisertEtterFangstOgFisk,
) {
    override fun calculate(fakta: Fakta): Resultat {
        return if (fakta.fangstOgFiske) super.calculate(fakta) else IngenBeregningsResultat(this.beregningsregel)
    }
}

class LærlingForskriftSisteAvsluttendeKalenderMåned : GrunnlagEtterLærlingForskrift(
    regelIdentifikator = "LærlingArbeidsinntekt1x12",
    grunnlagUtvelgelse = SisteAvsluttendeMånedUtvelgelse(),
    inntektKlasser = inntektKlassifisertEtterArbeidsInntekt,
)

class LærlingForskriftSiste3AvsluttendeKalenderMåned : GrunnlagEtterLærlingForskrift(
    regelIdentifikator = "LærlingArbeidsinntekt3x4",
    grunnlagUtvelgelse = Siste3AvsluttendeMånederUtvelgelse(),
    inntektKlasser = inntektKlassifisertEtterArbeidsInntekt,
)

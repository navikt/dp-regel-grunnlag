package no.nav.dagpenger.regel.grunnlag.beregning

import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.sumInntekt
import no.nav.dagpenger.regel.grunnlag.Fakta
import java.math.BigDecimal
import java.util.EnumSet

abstract class GrunnlagEtterLærlingForskrift(
    private val regelIdentifikator: String,
    private val grunnlagUtvelgelse: GrunnlagUtvelgelse,
    private val inntektKlasser: EnumSet<InntektKlasse>
) : GrunnlagBeregning(regelIdentifikator) {

    override fun calculate(fakta: Fakta): Resultat {
        return if (fakta.lærling) {

            val sortertEtterInntektsmåned =
                fakta.inntektsPerioderOrEmpty.first.map(fakta.oppjusterTilGjeldendeGrunnbeløp())
                    .sortedByDescending { it.årMåned }
            val uavkortet =
                sortertEtterInntektsmåned.take(grunnlagUtvelgelse.antallMåneder).sumInntekt(inntektKlasser.toList())
                    .multiply(grunnlagUtvelgelse.månedFaktor.toBigDecimal())
            val seksGangerGrunnbeløp = fakta.gjeldendeGrunnbeløpVedBeregningsdato.verdi.multiply(BigDecimal(6))
            val avkortet = if (uavkortet > seksGangerGrunnbeløp) seksGangerGrunnbeløp else uavkortet

            BeregningsResultat(
                avkortet = avkortet,
                uavkortet = uavkortet,
                beregningsregel = regelIdentifikator
            )
        } else {
            IngenBeregningsResultat(regelIdentifikator)
        }
    }
}

sealed class GrunnlagUtvelgelse(
    val antallMåneder: Int,
    val månedFaktor: Int
)

class SisteAvsluttendeMånedUtvelgelse : GrunnlagUtvelgelse(antallMåneder = 1, månedFaktor = 12)
class Siste3AvsluttendeMånederUtvelgelse : GrunnlagUtvelgelse(antallMåneder = 3, månedFaktor = 4)

class LærlingForskriftSisteAvsluttendeKalenderMånedFangstOgFisk : GrunnlagEtterLærlingForskrift(
    regelIdentifikator = "LærlingFangstOgFiskSiste1",
    grunnlagUtvelgelse = SisteAvsluttendeMånedUtvelgelse(),
    inntektKlasser = inntektKlassifisertEtterFangstOgFisk
) {

    override fun calculate(fakta: Fakta): Resultat {
        return if (fakta.fangstOgFisk) super.calculate(fakta) else IngenBeregningsResultat(this.beregningsregel)
    }
}

class LærlingForskriftSiste3AvsluttendeKalenderMånedFangsOgFisk : GrunnlagEtterLærlingForskrift(
    regelIdentifikator = "LærlingFangstOgFiskSiste3",
    grunnlagUtvelgelse = Siste3AvsluttendeMånederUtvelgelse(),
    inntektKlasser = inntektKlassifisertEtterFangstOgFisk
) {
    override fun calculate(fakta: Fakta): Resultat {
        return if (fakta.fangstOgFisk) super.calculate(fakta) else IngenBeregningsResultat(this.beregningsregel)
    }
}

class LærlingForskriftSisteAvsluttendeKalenderMåned : GrunnlagEtterLærlingForskrift(
    regelIdentifikator = "LærlingArbeidsinntektSiste1",
    grunnlagUtvelgelse = SisteAvsluttendeMånedUtvelgelse(),
    inntektKlasser = inntektKlassifisertEtterArbeidsInntekt
)

class LærlingForskriftSiste3AvsluttendeKalenderMåned : GrunnlagEtterLærlingForskrift(
    regelIdentifikator = "LærlingArbeidsinntektSiste3",
    grunnlagUtvelgelse = Siste3AvsluttendeMånederUtvelgelse(),
    inntektKlasser = inntektKlassifisertEtterArbeidsInntekt
)

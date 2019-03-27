package no.nav.dagpenger.regel.grunnlag.beregning

import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.regel.grunnlag.Fakta
import java.util.EnumSet

class BruttoInntektMedFangstOgFiskDeSiste36AvsluttedeKalendermånedene : MånedsGrunnlagBeregning(EnumSet.of(
    InntektKlasse.ARBEIDSINNTEKT,
    InntektKlasse.DAGPENGER,
    InntektKlasse.SYKEPENGER,
    InntektKlasse.TILTAKSLØNN,
    InntektKlasse.FANGST_FISKE,
    InntektKlasse.DAGPENGER_FANGST_FISKE,
    InntektKlasse.SYKEPENGER_FANGST_FISKE),
    36, "FangstOgFiskSiste36") {

    override fun calculate(fakta: Fakta): BeregningsResultat {
        return if (fakta.fangstOgFisk) super.calculate(fakta) else BeregningsResultat(0.toBigDecimal(), 0.toBigDecimal(), "FanstOgFiskSiste36")
    }
}
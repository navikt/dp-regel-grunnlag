package no.nav.dagpenger.regel.grunnlag.beregning

import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.regel.grunnlag.Fakta
import java.util.EnumSet

class BruttoInntektMedFangstOgFiskDeSiste36AvsluttedeKalendermånedene : TrettiseksMånedersBeregning(EnumSet.of(
    InntektKlasse.ARBEIDSINNTEKT,
    InntektKlasse.DAGPENGER,
    InntektKlasse.SYKEPENGER,
    InntektKlasse.TILTAKSLØNN,
    InntektKlasse.FANGST_FISKE,
    InntektKlasse.DAGPENGER_FANGST_FISKE,
    InntektKlasse.SYKEPENGER_FANGST_FISKE),
    "FangstOgFiskSiste36") {

    override fun calculate(fakta: Fakta): Resultat {
        return if (fakta.fangstOgFisk) super.calculate(fakta) else IngenBeregningsResultat("FanstOgFiskSiste36")
    }
}
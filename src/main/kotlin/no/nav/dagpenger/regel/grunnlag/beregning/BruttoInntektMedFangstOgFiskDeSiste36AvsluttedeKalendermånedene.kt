package no.nav.dagpenger.regel.grunnlag.beregning

import no.nav.dagpenger.regel.grunnlag.Fakta

class BruttoInntektMedFangstOgFiskDeSiste36AvsluttedeKalendermånedene : TrettiseksMånedersBeregning(
    inntektKlassifisertEtterFangstOgFisk,
    "FangstOgFiskSiste36") {

    override fun calculate(fakta: Fakta): Resultat {
        return if (fakta.fangstOgFisk) super.calculate(fakta) else IngenBeregningsResultat("FangstOgFiskSiste36")
    }
}
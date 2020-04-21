package no.nav.dagpenger.regel.grunnlag.beregning

import no.nav.dagpenger.regel.grunnlag.Fakta

class BruttoInntektMedFangstOgFiskDeSiste12AvsluttedeKalendermånedene : TolvMånedersBeregning(
    inntektKlassifisertEtterFangstOgFisk,
    "FangstOgFiskSiste12") {

    override fun calculate(fakta: Fakta): Resultat {
        return if (fakta.fangstOgFisk) super.calculate(fakta) else IngenBeregningsResultat("FangstOgFiskeSiste12")
    }
}

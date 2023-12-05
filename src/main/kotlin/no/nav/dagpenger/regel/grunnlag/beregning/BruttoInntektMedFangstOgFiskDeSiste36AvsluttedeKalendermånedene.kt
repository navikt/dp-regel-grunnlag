package no.nav.dagpenger.regel.grunnlag.beregning

import no.nav.dagpenger.regel.grunnlag.Fakta
import java.time.LocalDate

class BruttoInntektMedFangstOgFiskDeSiste36AvsluttedeKalendermånedene : TrettiseksMånedersBeregning(
    inntektsklasserMedFangstOgFiske,
    "FangstOgFiskSiste36(2021)",
) {
    private val avviklingsDato = LocalDate.of(2022, 1, 1)
    private fun skalInkludereFangstOgFisk(fakta: Fakta) = fakta.fangstOgFiske && fakta.regelverksdato < avviklingsDato

    override fun calculate(fakta: Fakta) =
        if (skalInkludereFangstOgFisk(fakta)) {
            super.calculate(fakta)
        } else {
            IngenBeregningsResultat("FangstOgFiskSiste36(2021)")
        }
}

package no.nav.dagpenger.regel.grunnlag

import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import java.math.BigDecimal
import java.util.EnumSet

class BruttoInntektMedFangstOgFiskDeSiste36AvsluttedeKalendermånedene : MånedsBeregning(EnumSet.of(
    InntektKlasse.ARBEIDSINNTEKT,
    InntektKlasse.DAGPENGER,
    InntektKlasse.SYKEPENGER,
    InntektKlasse.TILTAKSLØNN,
    InntektKlasse.FANGST_FISKE,
    InntektKlasse.DAGPENGER_FANGST_FISKE,
    InntektKlasse.SYKEPENGER_FANGST_FISKE),
    36) {

    override fun calculate(fakta: Fakta): BigDecimal {
        return if (fakta.fangstOgFisk) super.calculate(fakta) else 0.toBigDecimal()
    }
}
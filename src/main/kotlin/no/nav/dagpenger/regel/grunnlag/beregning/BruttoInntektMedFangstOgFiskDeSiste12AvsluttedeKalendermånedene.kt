package no.nav.dagpenger.regel.grunnlag.beregning

import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.regel.grunnlag.Fakta
import java.math.BigDecimal
import java.util.EnumSet

class BruttoInntektMedFangstOgFiskDeSiste12AvsluttedeKalendermånedene : MånedsGrunnlagBeregning(EnumSet.of(
    InntektKlasse.ARBEIDSINNTEKT,
    InntektKlasse.DAGPENGER,
    InntektKlasse.SYKEPENGER,
    InntektKlasse.TILTAKSLØNN,
    InntektKlasse.FANGST_FISKE,
    InntektKlasse.DAGPENGER_FANGST_FISKE,
    InntektKlasse.SYKEPENGER_FANGST_FISKE),
    12) {

    override fun calculate(fakta: Fakta): BigDecimal {
        return if (fakta.fangstOgFisk) super.calculate(fakta) else BigDecimal.ZERO
    }
}
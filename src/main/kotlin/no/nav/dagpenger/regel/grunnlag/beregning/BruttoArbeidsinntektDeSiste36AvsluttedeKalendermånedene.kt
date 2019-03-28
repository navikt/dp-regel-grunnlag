package no.nav.dagpenger.regel.grunnlag.beregning

import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import java.util.EnumSet

class BruttoArbeidsinntektDeSiste36AvsluttedeKalendermånedene : TrettiseksMånedersBeregning(
    EnumSet.of(
        InntektKlasse.ARBEIDSINNTEKT,
        InntektKlasse.DAGPENGER,
        InntektKlasse.SYKEPENGER,
        InntektKlasse.TILTAKSLØNN
    ), "ArbeidsinntektSiste36"
)
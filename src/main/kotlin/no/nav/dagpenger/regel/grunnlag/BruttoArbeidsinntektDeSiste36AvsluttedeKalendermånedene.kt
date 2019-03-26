package no.nav.dagpenger.regel.grunnlag

import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import java.util.EnumSet

class BruttoArbeidsinntektDeSiste36AvsluttedeKalendermånedene : MånedsBeregning(EnumSet.of(
InntektKlasse.ARBEIDSINNTEKT,
InntektKlasse.DAGPENGER,
InntektKlasse.SYKEPENGER,
InntektKlasse.TILTAKSLØNN),
35)
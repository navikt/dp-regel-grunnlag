package no.nav.dagpenger.regel.grunnlag.beregning

import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import java.util.EnumSet

class BruttoArbeidsinntektDeSiste36AvsluttedeKalendermånedene : MånedsGrunnlagBeregning(
    EnumSet.of(
        InntektKlasse.ARBEIDSINNTEKT,
        InntektKlasse.DAGPENGER,
        InntektKlasse.SYKEPENGER,
        InntektKlasse.TILTAKSLØNN
    ),
    35, "ArbeidsinntektSiste36"
)
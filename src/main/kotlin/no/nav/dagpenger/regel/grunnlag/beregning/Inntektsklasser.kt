package no.nav.dagpenger.regel.grunnlag.beregning

import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import java.util.EnumSet

val inntektKlassifisertEtterFangstOgFisk = EnumSet.of(
    InntektKlasse.ARBEIDSINNTEKT,
    InntektKlasse.DAGPENGER,
    InntektKlasse.SYKEPENGER,
    InntektKlasse.TILTAKSLØNN,
    InntektKlasse.FANGST_FISKE,
    InntektKlasse.DAGPENGER_FANGST_FISKE,
    InntektKlasse.SYKEPENGER_FANGST_FISKE
)

val inntektKlassifisertEtterArbeidsInntekt = EnumSet.of(
    InntektKlasse.ARBEIDSINNTEKT,
    InntektKlasse.DAGPENGER,
    InntektKlasse.SYKEPENGER,
    InntektKlasse.TILTAKSLØNN
)

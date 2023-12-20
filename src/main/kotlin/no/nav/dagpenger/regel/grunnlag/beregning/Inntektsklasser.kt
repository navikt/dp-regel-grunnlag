package no.nav.dagpenger.regel.grunnlag.beregning

import no.nav.dagpenger.inntekt.v1.InntektKlasse
import java.util.EnumSet

val inntektsklasser =
    EnumSet.of(
        InntektKlasse.ARBEIDSINNTEKT,
        InntektKlasse.DAGPENGER,
        InntektKlasse.SYKEPENGER,
        InntektKlasse.TILTAKSLØNN,
        InntektKlasse.PLEIEPENGER,
        InntektKlasse.OPPLÆRINGSPENGER,
        InntektKlasse.OMSORGSPENGER,
    )

val inntektsklasserMedFangstOgFiske =
    EnumSet.of(
        InntektKlasse.FANGST_FISKE,
        InntektKlasse.DAGPENGER_FANGST_FISKE,
        InntektKlasse.SYKEPENGER_FANGST_FISKE,
        InntektKlasse.ARBEIDSINNTEKT,
        InntektKlasse.DAGPENGER,
        InntektKlasse.SYKEPENGER,
        InntektKlasse.TILTAKSLØNN,
        InntektKlasse.PLEIEPENGER,
        InntektKlasse.OPPLÆRINGSPENGER,
        InntektKlasse.OMSORGSPENGER,
    )

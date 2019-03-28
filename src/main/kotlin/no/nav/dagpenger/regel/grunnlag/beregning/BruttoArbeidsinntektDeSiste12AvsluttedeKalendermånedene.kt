package no.nav.dagpenger.regel.grunnlag.beregning

import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import java.util.EnumSet

open class BruttoArbeidsinntektDeSiste12AvsluttedeKalendermånedene (
    inntektKlasse: EnumSet<InntektKlasse> = EnumSet.of(
        InntektKlasse.ARBEIDSINNTEKT,
        InntektKlasse.DAGPENGER,
        InntektKlasse.SYKEPENGER,
        InntektKlasse.TILTAKSLØNN
    )
) : TolvMånedersBeregning(inntektKlasse, "ArbeidsinntektSiste12")

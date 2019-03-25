package no.nav.dagpenger.regel.grunnlag

import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import java.math.BigDecimal

class BruttoArbeidsinntektDeSisteTolvAvsluttedeKalendermånedene(fakta: Fakta) {

    val resultat: BigDecimal = if(!fakta.fangstOgFisk) fakta.arbeidsinntektSiste12 else fakta.inntektSiste12inkludertFangstOgFiske



}

package no.nav.dagpenger.regel.grunnlag

import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import java.math.BigDecimal
import java.time.YearMonth
import java.util.EnumSet

class BruttoInntektMedFangstOgFiskDeTolvSisteKlendermånedene(fakta: Fakta) {

    val resultat: BigDecimal = if (fakta.fangstOgFisk) calculate(fakta) else BigDecimal(0)

    private fun calculate(fakta: Fakta): BigDecimal {
        val månedsInntekt: List<Pair<YearMonth, BigDecimal>> = fakta.sumMåneder(EnumSet.of(
            InntektKlasse.ARBEIDSINNTEKT,
            InntektKlasse.SYKEPENGER,
            InntektKlasse.DAGPENGER,
            InntektKlasse.TILTAKSLØNN,
            InntektKlasse.FANGST_FISKE,
            InntektKlasse.SYKEPENGER_FANGST_FISKE,
            InntektKlasse.DAGPENGER_FANGST_FISKE
        ))
        val senesteInntektsmåned = fakta.senesteInntektsmåned
        val tidligsteInntektsmåned = senesteInntektsmåned.minusMonths(11)
        val gjeldendeGrunnbeløp = getGrunnbeløpForMåned(YearMonth.from(fakta.beregningsdato))

        return månedsInntekt.filter { it.first >= tidligsteInntektsmåned && it.first <= senesteInntektsmåned }
            .map { it.second.multiply(gjeldendeGrunnbeløp.faktorMellom(getGrunnbeløpForMåned(it.first))) }
            .fold(BigDecimal.ZERO, BigDecimal::add)
    }
}
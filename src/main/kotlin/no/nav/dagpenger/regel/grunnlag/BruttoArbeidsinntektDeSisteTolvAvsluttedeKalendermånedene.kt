package no.nav.dagpenger.regel.grunnlag

import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import java.math.BigDecimal
import java.time.YearMonth
import java.util.EnumSet

class BruttoArbeidsinntektDeSisteTolvAvsluttedeKalendermånedene(fakta: Fakta) {

    val resultat: BigDecimal = calculate(fakta)


    private fun calculate(fakta: Fakta): BigDecimal  {
        val månedsInntekt: List<Pair<YearMonth, BigDecimal>>  = fakta.sumMåneder(EnumSet.of(
            InntektKlasse.ARBEIDSINNTEKT,
            InntektKlasse.TILTAKSLØNN,
            InntektKlasse.DAGPENGER,
            InntektKlasse.SYKEPENGER))

        val senesteInntektsmåned = fakta.senesteInntektsmåned
        val tidligsteInntektsmåned = senesteInntektsmåned.minusMonths(11)
        val gjeldendeGrunnbeløp = getGrunnbeløpForMåned(YearMonth.from(fakta.beregningsdato))

         return månedsInntekt.filter { it.first >= tidligsteInntektsmåned && it.first <= senesteInntektsmåned }
            .map { it.second.multiply(gjeldendeGrunnbeløp.faktorMellom(getGrunnbeløpForMåned(it.first)) ) }
            .fold(BigDecimal.ZERO, BigDecimal::add)

        }
}

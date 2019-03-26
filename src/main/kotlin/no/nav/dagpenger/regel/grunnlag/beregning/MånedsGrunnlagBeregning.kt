package no.nav.dagpenger.regel.grunnlag.beregning

import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.regel.grunnlag.Fakta
import no.nav.dagpenger.regel.grunnlag.getGrunnbeløpForMåned
import java.math.BigDecimal
import java.time.YearMonth
import java.util.EnumSet

abstract class MånedsGrunnlagBeregning(val inntektKlasser: EnumSet<InntektKlasse>, val måneder: Long, beregningsregel: String) :
    GrunnlagBeregning(beregningsregel) {

    override fun calculate(fakta: Fakta): BeregningsResultat {
        val månedsInntekt: List<Pair<YearMonth, BigDecimal>> = fakta.sumMåneder(
            inntektKlasser)

        val senesteInntektsmåned = fakta.senesteInntektsmåned
        val tidligsteInntektsmåned = senesteInntektsmåned.minusMonths(måneder)
        val gjeldendeGrunnbeløp =
            getGrunnbeløpForMåned(YearMonth.from(fakta.beregningsdato))

        val uavkortet = månedsInntekt.filter { it.first >= tidligsteInntektsmåned && it.first <= senesteInntektsmåned }
            .map { it.second.multiply(gjeldendeGrunnbeløp.faktorMellom(
                getGrunnbeløpForMåned(
                    it.first
                )
            ) ) }
            .fold(BigDecimal.ZERO, BigDecimal::add)

        return BeregningsResultat(uavkortet, 0.toBigDecimal(), beregningsregel)
    }
}
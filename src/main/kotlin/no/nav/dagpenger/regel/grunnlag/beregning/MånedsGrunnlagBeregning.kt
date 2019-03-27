package no.nav.dagpenger.regel.grunnlag.beregning

import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.regel.grunnlag.Fakta
import no.nav.dagpenger.regel.grunnlag.getGrunnbeløpForMåned
import no.nav.dagpenger.regel.grunnlag.grunnbeløp
import java.math.BigDecimal
import java.time.YearMonth
import java.util.EnumSet

abstract class MånedsGrunnlagBeregning(
    val inntektKlasser: EnumSet<InntektKlasse>,
    val måneder: Long,
    beregningsregel: String
) :
    GrunnlagBeregning(beregningsregel) {

    override fun calculate(fakta: Fakta): BeregningsResultat {
        val månedsInntekt: Map<YearMonth, BigDecimal> = fakta.sumMåneder(
            inntektKlasser
        )

        val senesteInntektsmåned = fakta.senesteInntektsmåned
        val tidligsteInntektsmåned = senesteInntektsmåned.minusMonths(måneder)
        val gjeldendeGrunnbeløp =
            getGrunnbeløpForMåned(YearMonth.from(fakta.beregningsdato))

        val seksGangerGrunnbeløp = gjeldendeGrunnbeløp.verdi.multiply(BigDecimal(6))

        val oppjustertInntektIforholdTilGjelendenGrunnbeløp = månedsInntekt
            .filterKeys { måned -> måned >= tidligsteInntektsmåned && måned <= senesteInntektsmåned }
            .mapValues { (måned, inntekt) ->
                inntekt.multiply(gjeldendeGrunnbeløp.faktorMellom(getGrunnbeløpForMåned(måned)))
            }

        val uavkortet = oppjustertInntektIforholdTilGjelendenGrunnbeløp.values.fold(BigDecimal.ZERO, BigDecimal::add)

        val uavkortetPerPeriode = oppjustertInntektIforholdTilGjelendenGrunnbeløp.filterKeys { måned -> måned >= tidligsteInntektsmåned && måned <= senesteInntektsmåned }.values.fold(BigDecimal.ZERO, BigDecimal::add)

        val avkortet = if (uavkortetPerPeriode > seksGangerGrunnbeløp) seksGangerGrunnbeløp else uavkortetPerPeriode

        return BeregningsResultat(uavkortet, avkortet, beregningsregel)
    }
}
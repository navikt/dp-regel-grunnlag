package no.nav.dagpenger.regel.grunnlag

import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import no.nav.dagpenger.events.inntekt.v1.sumInntekt
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import java.util.EnumSet

data class Fakta(
    val inntekt: Inntekt,
    val senesteInntektsmåned: YearMonth,
    val verneplikt: Boolean,
    val fangstOgFisk: Boolean,
    val beregningsdato: LocalDate
) {
    val gjeldendeGrunnbeløp =
        getGrunnbeløpForMåned(YearMonth.from(beregningsdato))

    val inntektsPerioder = inntekt.splitIntoInntektsPerioder(senesteInntektsmåned)

    fun oppjusterteInntekterFørstePeriode(inntektsKlasser: EnumSet<InntektKlasse>): BigDecimal = inntektsPerioder.first.map(oppjusterTilGjeldendeGrunnbeløp(gjeldendeGrunnbeløp)).sumInntekt(inntektsKlasser.toList())

    fun oppjusterteInntekterAndrePeriode(inntektsKlasser: EnumSet<InntektKlasse>): BigDecimal = inntektsPerioder.second.map(oppjusterTilGjeldendeGrunnbeløp(gjeldendeGrunnbeløp)).sumInntekt(inntektsKlasser.toList())

    fun oppjusterteInntekterTredjePeriode(inntektsKlasser: EnumSet<InntektKlasse>): BigDecimal = inntektsPerioder.third.map(oppjusterTilGjeldendeGrunnbeløp(gjeldendeGrunnbeløp)).sumInntekt(inntektsKlasser.toList())

    private fun oppjusterTilGjeldendeGrunnbeløp(gjeldendeGrunnbeløp: Grunnbeløp): (KlassifisertInntektMåned) -> KlassifisertInntektMåned {
        return { inntekt ->
            val oppjusterteinntekter = inntekt.klassifiserteInntekter.map { klassifisertInntekt ->
                val oppjustert = klassifisertInntekt.beløp.multiply(
                    gjeldendeGrunnbeløp.faktorMellom(
                        getGrunnbeløpForMåned(inntekt.årMåned)
                    )
                )
                klassifisertInntekt.copy(beløp = oppjustert)
            }
            inntekt.copy(klassifiserteInntekter = oppjusterteinntekter)
        }
    }
}
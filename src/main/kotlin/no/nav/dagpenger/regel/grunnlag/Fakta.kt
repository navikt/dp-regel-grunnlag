package no.nav.dagpenger.regel.grunnlag

import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.InntektsPerioder
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import no.nav.dagpenger.events.inntekt.v1.sumInntekt
import no.nav.dagpenger.grunnbelop.Grunnbeløp
import no.nav.dagpenger.grunnbelop.getGrunnbeløpForDato
import no.nav.dagpenger.grunnbelop.getGrunnbeløpForMåned
import java.math.BigDecimal
import java.time.LocalDate
import java.time.Month
import java.util.EnumSet

data class Fakta(
    val inntekt: Inntekt? = null,
    val verneplikt: Boolean,
    val fangstOgFisk: Boolean,
    val beregningsdato: LocalDate,
    val manueltGrunnlag: Int? = null
) {
    val gjeldendeGrunnbeløp = getGrunnbeløp(LocalDate.from(beregningsdato))
    val inntektsPerioder = inntekt?.splitIntoInntektsPerioder()

    private val inntektsPerioderOrEmpty = inntektsPerioder ?: InntektsPerioder(emptyList(), emptyList(), emptyList())

    fun oppjusterteInntekterFørstePeriode(inntektsKlasser: EnumSet<InntektKlasse>): BigDecimal =
        inntektsPerioderOrEmpty.first.map(oppjusterTilGjeldendeGrunnbeløp(gjeldendeGrunnbeløp)).sumInntekt(
            inntektsKlasser.toList()
        )

    fun oppjusterteInntekterAndrePeriode(inntektsKlasser: EnumSet<InntektKlasse>): BigDecimal =
        inntektsPerioderOrEmpty.second.map(oppjusterTilGjeldendeGrunnbeløp(gjeldendeGrunnbeløp)).sumInntekt(
            inntektsKlasser.toList()
        )

    fun oppjusterteInntekterTredjePeriode(inntektsKlasser: EnumSet<InntektKlasse>): BigDecimal =
        inntektsPerioderOrEmpty.third.map(oppjusterTilGjeldendeGrunnbeløp(gjeldendeGrunnbeløp)).sumInntekt(
            inntektsKlasser.toList()
        )

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

private fun getGrunnbeløp(beregningsdato: LocalDate): Grunnbeløp {
    if (features.isEnabled("gjustering")) {
        if (beregningsdato.isAfter(LocalDate.of(2019, 8, 1).minusDays(1)))
            return Grunnbeløp(
                LocalDate.of(2019, Month.AUGUST, 1),
                LocalDate.of(2020, Month.APRIL, 30),
                100000.toBigDecimal()
            )
    }

    return getGrunnbeløpForDato(LocalDate.from(beregningsdato))
}

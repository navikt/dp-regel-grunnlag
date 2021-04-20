package no.nav.dagpenger.regel.grunnlag

import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.InntektsPerioder
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import no.nav.dagpenger.events.inntekt.v1.sumInntekt
import no.nav.dagpenger.grunnbelop.Grunnbeløp
import no.nav.dagpenger.grunnbelop.Regel
import no.nav.dagpenger.grunnbelop.faktorMellom
import no.nav.dagpenger.grunnbelop.forDato
import no.nav.dagpenger.grunnbelop.forMåned
import no.nav.dagpenger.grunnbelop.getGrunnbeløpForRegel
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

data class Fakta(
    val inntekt: Inntekt? = null,
    val verneplikt: Boolean,
    val fangstOgFisk: Boolean,
    val beregningsdato: LocalDate,
    val regelverksdato: LocalDate = beregningsdato,
    val manueltGrunnlag: Int? = null,
    val forrigeGrunnlag: Int? = null,
    val lærling: Boolean = false
) {
    val inntektsPerioder = inntekt?.splitIntoInntektsPerioder()

    val inntektsPerioderOrEmpty = inntektsPerioder ?: InntektsPerioder(emptyList(), emptyList(), emptyList())

    fun oppjusterteInntekterFørstePeriode(inntektsKlasser: EnumSet<InntektKlasse>): BigDecimal =
        inntektsPerioderOrEmpty.first.map(oppjusterTilGjeldendeGrunnbeløp(grunnbeløpVedBeregningsdato(this))).sumInntekt(
            inntektsKlasser.toList()
        )

    fun oppjusterteInntekterAndrePeriode(inntektsKlasser: EnumSet<InntektKlasse>): BigDecimal =
        inntektsPerioderOrEmpty.second.map(oppjusterTilGjeldendeGrunnbeløp(grunnbeløpVedBeregningsdato(this))).sumInntekt(
            inntektsKlasser.toList()
        )

    fun oppjusterteInntekterTredjePeriode(inntektsKlasser: EnumSet<InntektKlasse>): BigDecimal =
        inntektsPerioderOrEmpty.third.map(oppjusterTilGjeldendeGrunnbeløp(grunnbeløpVedBeregningsdato(this))).sumInntekt(
            inntektsKlasser.toList()
        )

    fun oppjusterTilGjeldendeGrunnbeløp(): (KlassifisertInntektMåned) -> KlassifisertInntektMåned {
        return oppjusterTilGjeldendeGrunnbeløp(grunnbeløpVedBeregningsdato(this))
    }

    private fun oppjusterTilGjeldendeGrunnbeløp(gjeldendeGrunnbeløp: Grunnbeløp): (KlassifisertInntektMåned) -> KlassifisertInntektMåned {
        return { inntekt ->
            val oppjusterteinntekter = inntekt.klassifiserteInntekter.map { klassifisertInntekt ->
                val oppjustert = klassifisertInntekt.beløp.multiply(
                    gjeldendeGrunnbeløp.faktorMellom(
                        getGrunnbeløpForRegel(Regel.Grunnlag).forMåned(inntekt.årMåned)
                    )
                )
                klassifisertInntekt.copy(beløp = oppjustert)
            }
            inntekt.copy(klassifiserteInntekter = oppjusterteinntekter)
        }
    }
}

internal fun grunnbeløpVedBeregningsdato(fakta: Fakta) = when {
    isThisGjusteringTest(fakta.regelverksdato) -> Grunnbeløp.GjusteringsTest
    else -> getGrunnbeløpForRegel(Regel.Grunnlag).forDato(fakta.beregningsdato)
}

internal fun grunnbeløpVedRegelverksdato(regelverksdato: LocalDate) = when {
    isThisGjusteringTest(regelverksdato) -> Grunnbeløp.GjusteringsTest
    else -> getGrunnbeløpForRegel(Regel.Grunnlag).forDato(regelverksdato)
}

internal fun isThisGjusteringTest(regelverksdato: LocalDate): Boolean {
    val gVirkning = LocalDate.of(2021, 3, 27)
    val isRegelverksdatoAfterGjustering = regelverksdato.isAfter(gVirkning.minusDays(1))
    return Grunnlag.unleash.isEnabled(GJUSTERING_TEST) && isRegelverksdatoAfterGjustering
}
package no.nav.dagpenger.regel.grunnlag

import no.nav.dagpenger.grunnbelop.Grunnbeløp
import no.nav.dagpenger.grunnbelop.Regel
import no.nav.dagpenger.grunnbelop.faktorMellom
import no.nav.dagpenger.grunnbelop.forDato
import no.nav.dagpenger.grunnbelop.forMåned
import no.nav.dagpenger.grunnbelop.getGrunnbeløpForRegel
import no.nav.dagpenger.inntekt.v1.Inntekt
import no.nav.dagpenger.inntekt.v1.InntektKlasse
import no.nav.dagpenger.inntekt.v1.InntektsPerioder
import no.nav.dagpenger.inntekt.v1.KlassifisertInntektMåned
import no.nav.dagpenger.inntekt.v1.sumInntekt
import java.math.BigDecimal
import java.time.LocalDate
import java.util.EnumSet

data class Fakta(
    val inntekt: Inntekt? = null,
    val verneplikt: Boolean,
    val fangstOgFiske: Boolean,
    val beregningsdato: LocalDate,
    val regelverksdato: LocalDate = beregningsdato,
    val manueltGrunnlag: Int? = null,
    val forrigeGrunnlag: Int? = null,
    val lærling: Boolean = false,
) {
    val inntektsPerioder = inntekt?.splitIntoInntektsPerioder()

    val inntektsPerioderOrEmpty = inntektsPerioder ?: InntektsPerioder(emptyList(), emptyList(), emptyList())

    fun grunnbeløpVedBeregningsdato() =
        when {
            isThisGjusteringTest(beregningsdato) -> Grunnbeløp.GjusteringsTest
            else -> getGrunnbeløpForRegel(Regel.Grunnlag).forDato(beregningsdato)
        }

    fun grunnbeløpVedRegelverksdato() =
        when {
            isThisGjusteringTest(regelverksdato) -> Grunnbeløp.GjusteringsTest
            else -> getGrunnbeløpForRegel(Regel.Grunnlag).forDato(regelverksdato)
        }

    fun oppjusterteInntekterFørstePeriode(inntektsKlasser: EnumSet<InntektKlasse>): BigDecimal =
        inntektsPerioderOrEmpty.first.map(oppjusterTilGjeldendeGrunnbeløp(grunnbeløpVedBeregningsdato())).sumInntekt(
            inntektsKlasser.toList(),
        )

    fun oppjusterteInntekterAndrePeriode(inntektsKlasser: EnumSet<InntektKlasse>): BigDecimal =
        inntektsPerioderOrEmpty.second.map(oppjusterTilGjeldendeGrunnbeløp(grunnbeløpVedBeregningsdato())).sumInntekt(
            inntektsKlasser.toList(),
        )

    fun oppjusterteInntekterTredjePeriode(inntektsKlasser: EnumSet<InntektKlasse>): BigDecimal =
        inntektsPerioderOrEmpty.third.map(oppjusterTilGjeldendeGrunnbeløp(grunnbeløpVedBeregningsdato())).sumInntekt(
            inntektsKlasser.toList(),
        )

    private fun oppjusterTilGjeldendeGrunnbeløp(gjeldendeGrunnbeløp: Grunnbeløp): (KlassifisertInntektMåned) -> KlassifisertInntektMåned {
        return { inntekt ->
            val oppjusterteinntekter =
                inntekt.klassifiserteInntekter.map { klassifisertInntekt ->
                    val oppjustert =
                        klassifisertInntekt.beløp.multiply(
                            gjeldendeGrunnbeløp.faktorMellom(
                                getGrunnbeløpForRegel(Regel.Grunnlag).forMåned(inntekt.årMåned),
                            ),
                        )
                    klassifisertInntekt.copy(beløp = oppjustert)
                }
            inntekt.copy(klassifiserteInntekter = oppjusterteinntekter)
        }
    }
}

internal fun isThisGjusteringTest(regelverksdato: LocalDate): Boolean {
    // Dette er G
    val gVirkning = LocalDate.of(2025, 5, 1)
    val isRegelverksdatoAfterGjustering = regelverksdato.isAfter(gVirkning.minusDays(1))
    return Configuration.unleash.isEnabled("dp-g-justeringstest") && isRegelverksdatoAfterGjustering
}

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
import java.util.EnumSet

data class Fakta(
    val inntekt: Inntekt? = null,
    val verneplikt: Boolean,
    val fangstOgFisk: Boolean,
    val beregningsdato: LocalDate,
    val dagensDato: LocalDate = LocalDate.now(),
    val manueltGrunnlag: Int? = null,
    val gjeldendeGrunnbeløpVedBeregningsdato: Grunnbeløp = getGrunnbeløpWithFeatureFlagForGjustering(LocalDate.from(beregningsdato)),
    val gjeldendeGrunnbeløpForDagensDato: Grunnbeløp = getGrunnbeløpWithFeatureFlagForGjustering(dagensDato, verneplikt)
) {
    val inntektsPerioder = inntekt?.splitIntoInntektsPerioder()

    private val inntektsPerioderOrEmpty = inntektsPerioder ?: InntektsPerioder(emptyList(), emptyList(), emptyList())

    fun oppjusterteInntekterFørstePeriode(inntektsKlasser: EnumSet<InntektKlasse>): BigDecimal =
        inntektsPerioderOrEmpty.first.map(oppjusterTilGjeldendeGrunnbeløp(gjeldendeGrunnbeløpVedBeregningsdato)).sumInntekt(
            inntektsKlasser.toList()
        )

    fun oppjusterteInntekterAndrePeriode(inntektsKlasser: EnumSet<InntektKlasse>): BigDecimal =
        inntektsPerioderOrEmpty.second.map(oppjusterTilGjeldendeGrunnbeløp(gjeldendeGrunnbeløpVedBeregningsdato)).sumInntekt(
            inntektsKlasser.toList()
        )

    fun oppjusterteInntekterTredjePeriode(inntektsKlasser: EnumSet<InntektKlasse>): BigDecimal =
        inntektsPerioderOrEmpty.third.map(oppjusterTilGjeldendeGrunnbeløp(gjeldendeGrunnbeløpVedBeregningsdato)).sumInntekt(
            inntektsKlasser.toList()
        )

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

private fun getGrunnbeløpWithFeatureFlagForGjustering(beregningsdato: LocalDate, verneplikt: Boolean = false): Grunnbeløp {
    if (features.isEnabled("gjustering")) {
        val isBeregningsDatoAfterGjustering = beregningsdato.isAfter(LocalDate.of(2019, 8, 1).minusDays(1))

        if (isBeregningsDatoAfterGjustering || verneplikt) {
            return Grunnbeløp.GjusteringsTest
        }
    }

    return getGrunnbeløpForRegel(Regel.Grunnlag).forDato(beregningsdato)
}

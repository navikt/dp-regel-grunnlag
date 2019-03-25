package no.nav.dagpenger.regel.grunnlag

import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.all
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
    fun sumMåneder(inntektKlasser: EnumSet<InntektKlasse>): List<Pair<YearMonth, BigDecimal>> {
        return inntekt.inntektsListe.map { klassifisertInntektMåned ->
            klassifisertInntektMåned.årMåned to klassifisertInntektMåned.klassifiserteInntekter.filter {
                inntektKlasser.contains(
                    it.inntektKlasse
                )
            }.map { it.beløp }.fold(BigDecimal.ZERO, BigDecimal::add)
        }
    }

    val splitInntekt = inntekt.splitIntoInntektsPerioder(senesteInntektsmåned)

    val arbeidsinntektSiste12 = splitInntekt.first.sumInntekt(
        listOf(
            InntektKlasse.ARBEIDSINNTEKT,
            InntektKlasse.DAGPENGER,
            InntektKlasse.SYKEPENGER,
            InntektKlasse.TILTAKSLØNN
        )
    )
    val arbeidsinntektSiste36 = splitInntekt.all().sumInntekt(
        listOf(
            InntektKlasse.ARBEIDSINNTEKT,
            InntektKlasse.DAGPENGER,
            InntektKlasse.SYKEPENGER,
            InntektKlasse.TILTAKSLØNN
        )
    )

    val inntektSiste12inkludertFangstOgFiske = arbeidsinntektSiste12 + splitInntekt.first.sumInntekt(
        listOf(
            InntektKlasse.FANGST_FISKE,
            InntektKlasse.DAGPENGER_FANGST_FISKE,
            InntektKlasse.SYKEPENGER_FANGST_FISKE
        )
    )
    val inntektSiste36inkludertFangstOgFiske = arbeidsinntektSiste36 + splitInntekt.all().sumInntekt(
        listOf(
            InntektKlasse.FANGST_FISKE,
            InntektKlasse.DAGPENGER_FANGST_FISKE,
            InntektKlasse.SYKEPENGER_FANGST_FISKE
        )
    )
}
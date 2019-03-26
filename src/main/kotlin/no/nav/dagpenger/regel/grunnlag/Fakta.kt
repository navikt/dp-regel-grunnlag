package no.nav.dagpenger.regel.grunnlag

import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
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
}
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

    fun sumMåneder(inntektsKlasser: EnumSet<InntektKlasse>): Map<YearMonth, BigDecimal> {
        return inntekt.inntektsListe.groupBy {
            it.årMåned
        }.mapValues { (key, inntekter) ->
            inntekter.flatMap { klassifisertInntektMåned ->
                klassifisertInntektMåned.klassifiserteInntekter.filter {
                    inntektsKlasser.contains(
                        it.inntektKlasse
                    )
                }.map { it.beløp }
            }.fold(BigDecimal.ZERO, BigDecimal::add)
        }
    }
}
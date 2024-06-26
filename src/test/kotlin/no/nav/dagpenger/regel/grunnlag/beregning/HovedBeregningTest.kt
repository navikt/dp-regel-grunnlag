package no.nav.dagpenger.regel.grunnlag.beregning

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.string.shouldNotStartWith
import io.kotest.matchers.string.shouldStartWith
import io.kotest.matchers.types.shouldBeInstanceOf
import no.nav.dagpenger.inntekt.v1.Inntekt
import no.nav.dagpenger.inntekt.v1.InntektKlasse
import no.nav.dagpenger.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.inntekt.v1.KlassifisertInntektMåned
import no.nav.dagpenger.regel.grunnlag.Fakta
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

private val koronatid = DateIterator(startDate = LocalDate.of(2020, 3, 20), endDateInclusive = LocalDate.of(2020, 12, 31))

internal class HovedBeregningTest : FreeSpec({

    "skal velge lærling ved lærling parameter satt og covid-19 forskrift er ikraftsatt " {

        koronatid.forEach { beregningsDato ->
            val sisteAvsluttendeKalenderMåned = YearMonth.of(2020, 3)
            val inntekt = generateArbeidsinntekt(12, 2000.toBigDecimal(), sisteAvsluttendeKalenderMåned)
            val fakta =
                Fakta(
                    inntekt =
                        Inntekt(
                            "id",
                            inntekt,
                            sisteAvsluttendeKalenderMåned = sisteAvsluttendeKalenderMåned,
                        ),
                    verneplikt = false,
                    fangstOgFiske = false,
                    lærling = true,
                    beregningsdato = beregningsDato,
                )

            val beregningsResultat = HovedBeregning().calculate(fakta)
            beregningsResultat.shouldBeInstanceOf<BeregningsResultat>()
            beregningsResultat.beregningsregel.shouldStartWith("Lærling")
        }
    }

    " skal ikke velge lærling ved lærling parameter satt og covid-19 forskrift er ikraftsatt " {
        koronatid.forEach { beregningsDato ->
            val sisteAvsluttendeKalenderMåned = YearMonth.of(2020, 3)
            val inntekt = generateArbeidsinntekt(12, 2000.toBigDecimal(), sisteAvsluttendeKalenderMåned)
            val fakta =
                Fakta(
                    inntekt =
                        Inntekt(
                            "id",
                            inntekt,
                            sisteAvsluttendeKalenderMåned = sisteAvsluttendeKalenderMåned,
                        ),
                    verneplikt = false,
                    fangstOgFiske = false,
                    lærling = true,
                    beregningsdato = beregningsDato,
                )

            val beregningsResultat = HovedBeregning().calculate(fakta)
            beregningsResultat.shouldBeInstanceOf<BeregningsResultat>()
            beregningsResultat.beregningsregel.shouldNotStartWith("Lærling")
        }
    }
})

internal fun generateArbeidsinntekt(
    numberOfMonths: Int,
    beløpPerMnd: BigDecimal,
    senesteMåned: YearMonth = YearMonth.of(2019, 1),
): List<KlassifisertInntektMåned> {
    return (0 until numberOfMonths).toList().map {
        KlassifisertInntektMåned(
            senesteMåned.minusMonths(it.toLong()),
            listOf(
                KlassifisertInntekt(
                    beløpPerMnd,
                    InntektKlasse.ARBEIDSINNTEKT,
                ),
            ),
        )
    }
}

internal class DateIterator(
    startDate: LocalDate,
    val endDateInclusive: LocalDate,
    val stepDays: Long = 1,
) : Iterator<LocalDate> {
    private var currentDate = startDate

    override fun hasNext() = currentDate <= endDateInclusive

    override fun next(): LocalDate {
        val next = currentDate

        currentDate = currentDate.plusDays(stepDays)

        return next
    }
}

package no.nav.dagpenger.regel.grunnlag

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth

data class Grunnbeløp(
    val fraMåned: LocalDate,
    val verdi: BigDecimal
) {
    fun faktorMellom(grunnbeløp: Grunnbeløp): BigDecimal {

        return this.verdi.divide(grunnbeløp.verdi, 6, RoundingMode.HALF_UP)
    }
}

val grunnbeløp = listOf<Grunnbeløp>(
    Grunnbeløp(LocalDate.of(2018, Month.MAY, 1), 96883.toBigDecimal()),
    Grunnbeløp(LocalDate.of(2017, Month.MAY, 1), 93634.toBigDecimal()),
    Grunnbeløp(LocalDate.of(2016, Month.MAY, 1), 92576.toBigDecimal()),
    Grunnbeløp(LocalDate.of(2015, Month.MAY, 1), 90068.toBigDecimal())
)

fun getGrunnbeløpForMåned(måned: YearMonth): Grunnbeløp {

    val sisteMåned = findLastChange(måned)

    return grunnbeløp.filter { it.fraMåned.year == sisteMåned.year && it.fraMåned.month == sisteMåned.month }.first()
}

private fun findLastChange(yearMonth: YearMonth): YearMonth {

    if (yearMonth.month >= Month.MAY) {
        return YearMonth.of(yearMonth.year, Month.MAY)
    } else {
        return YearMonth.of(yearMonth.year - 1, Month.MAY)
    }
}
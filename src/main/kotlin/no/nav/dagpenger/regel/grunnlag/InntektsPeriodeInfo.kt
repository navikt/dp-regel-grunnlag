package no.nav.dagpenger.regel.grunnlag

import java.math.BigDecimal
import java.time.YearMonth

data class InntektPeriodeInfo(
    val inntektsPeriode: InntektsPeriode,
    val inntekt: BigDecimal,
    val periode: Int,
    val inneholderFangstOgFisk: Boolean,
)

data class InntektsPeriode(
    val førsteMåned: YearMonth,
    val sisteMåned: YearMonth,
)

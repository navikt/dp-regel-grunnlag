package no.nav.dagpenger.regel.grunnlag

import java.math.BigDecimal
import java.time.YearMonth

internal fun List<InntektPeriodeInfo>.toMaps() =
    this.map(InntektPeriodeInfo::toMap)

data class InntektPeriodeInfo(
    val inntektsPeriode: InntektsPeriode,
    val inntekt: BigDecimal,
    val periode: Int,
    val inneholderFangstOgFisk: Boolean,
) {
    fun toMap() = mapOf(
        "inntektsPeriode" to inntektsPeriode.toMap(),
        "inntekt" to inntekt,
        "periode" to periode,
        "inneholderFangstOgFisk" to inneholderFangstOgFisk,
    )
}

data class InntektsPeriode(
    val førsteMåned: YearMonth,
    val sisteMåned: YearMonth,
) {
    fun toMap(): Map<String, Any> = mapOf(
        "førsteMåned" to this.førsteMåned.toString(),
        "sisteMåned" to this.sisteMåned.toString(),
    )
}

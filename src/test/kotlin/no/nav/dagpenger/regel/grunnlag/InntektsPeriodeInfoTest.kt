package no.nav.dagpenger.regel.grunnlag

import io.kotest.matchers.collections.shouldContainExactly
import org.junit.jupiter.api.Test
import java.time.YearMonth

internal class InntektsPeriodeInfoTest {
    @Test
    fun `Kan serialsere en liste av InntektPeriodeInfo til en list av maps`() {
        listOf(
            InntektPeriodeInfo(
                inntektsPeriode =
                    InntektsPeriode(
                        førsteMåned = YearMonth.of(2022, 1),
                        sisteMåned = YearMonth.of(2023, 12),
                    ),
                inntekt = 10.toBigDecimal(),
                periode = 1,
                inneholderFangstOgFisk = false,
            ),
            InntektPeriodeInfo(
                inntektsPeriode =
                    InntektsPeriode(
                        førsteMåned = YearMonth.of(2022, 1),
                        sisteMåned = YearMonth.of(2023, 2),
                    ),
                inntekt = 123.3.toBigDecimal(),
                periode = 10,
                inneholderFangstOgFisk = true,
            ),
        ).toMaps() shouldContainExactly
            listOf(
                mapOf(
                    "inntektsPeriode" to
                        mapOf(
                            "førsteMåned" to "2022-01",
                            "sisteMåned" to "2023-12",
                        ),
                    "inntekt" to "${10.toBigDecimal()}",
                    "periode" to 1,
                    "inneholderFangstOgFisk" to false,
                ),
                mapOf(
                    "inntektsPeriode" to
                        mapOf(
                            "førsteMåned" to "2022-01",
                            "sisteMåned" to "2023-02",
                        ),
                    "inntekt" to "${123.3.toBigDecimal()}",
                    "periode" to 10,
                    "inneholderFangstOgFisk" to true,
                ),
            )
    }
}

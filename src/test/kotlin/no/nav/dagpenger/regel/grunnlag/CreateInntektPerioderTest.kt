package no.nav.dagpenger.regel.grunnlag

import no.nav.dagpenger.inntekt.v1.Inntekt
import no.nav.dagpenger.inntekt.v1.InntektKlasse
import no.nav.dagpenger.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.inntekt.v1.KlassifisertInntektMåned
import no.nav.dagpenger.regel.grunnlag.beregning.inntektsklasserMedFangstOgFiske
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal class CreateInntektPerioderTest {
    @Test
    fun `Skal ha perioder med 0 inntekt hvis det ikke er inntekt`() {
        val beregningsdato = LocalDate.of(2019, 2, 1)
        val fakta =
            Fakta(
                verneplikt = false,
                fangstOgFiske = false,
                beregningsdato = beregningsdato,
            )

        val inntektsPerioder = createInntektPerioder(fakta)

        assertNull(inntektsPerioder)
    }

    @Test
    fun ` Skal bare ta med Arbeidsinntekt hvis det kun finnes arbeidsinntekt i inntektslista  `() {
        val sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 1)
        val beregningsdato = LocalDate.of(2019, 2, 1)
        val inntektsListe = generateArbeidsinntekt(36, BigDecimal(1000), sisteAvsluttendeKalenderMåned)
        val fakta =
            Fakta(
                inntekt =
                    Inntekt(
                        "id",
                        inntektsListe,
                        sisteAvsluttendeKalenderMåned = sisteAvsluttendeKalenderMåned,
                    ),
                verneplikt = false,
                fangstOgFiske = false,
                beregningsdato = beregningsdato,
            )

        val inntektsPerioder = createInntektPerioder(fakta)!!
        assertThreeCorrectPeriods(inntektsPerioder, sisteAvsluttendeKalenderMåned)

        assertTrue(inntektsPerioder.all { it.inntekt == BigDecimal(12000) })
        assertTrue(inntektsPerioder.none { it.inneholderFangstOgFisk })
    }

    @Suppress("ktlint:standard:max-line-length")
    @Test
    fun `Skal indikere at fangst og fisk er med men ikke summere opp fangst og fisk sammen med arbeidsinntekt hvis ikke parameteret fangstOgFisk er satt til true `() {
        val sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 1)
        val beregningsdato = LocalDate.of(2019, 2, 1)
        val inntekt =
            Inntekt(
                "id",
                generateArbeidsOgFangstOgFiskInntekt(
                    36,
                    BigDecimal(2000),
                    BigDecimal(2000),
                    sisteAvsluttendeKalenderMåned,
                ),
                sisteAvsluttendeKalenderMåned = sisteAvsluttendeKalenderMåned,
            )
        val fakta =
            Fakta(
                inntekt,
                false,
                false,
                beregningsdato,
            )

        val inntektsPerioder = createInntektPerioder(fakta)!!
        assertThreeCorrectPeriods(inntektsPerioder, sisteAvsluttendeKalenderMåned)

        assertTrue(inntektsPerioder.all { it.inntekt == BigDecimal(24000) })
        assertTrue(inntektsPerioder.all { it.inneholderFangstOgFisk })
    }

    @ParameterizedTest
    @CsvSource(
        "PLEIEPENGER",
        "OMSORGSPENGER",
        "OPPLÆRINGSPENGER",
    )
    fun `Summer inntekter fra ulike inntektsklasser`(inntektKlasse: InntektKlasse) {
        val sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 1)
        val beregningsdato = LocalDate.of(2019, 2, 1)

        val fakta =
            Fakta(
                inntekt =
                    Inntekt(
                        inntektsId = "ID",
                        inntektsListe =
                            generateInntektMed(
                                inntektKlasse = inntektKlasse,
                                numberOfMonths = 36,
                                beløpPerMnd = BigDecimal(4000),
                                senesteMåned = sisteAvsluttendeKalenderMåned,
                            ),
                        sisteAvsluttendeKalenderMåned = sisteAvsluttendeKalenderMåned,
                    ),
                verneplikt = false,
                fangstOgFiske = true,
                beregningsdato = beregningsdato,
            )

        val inntektsPerioder = createInntektPerioder(fakta)!!
        assertThreeCorrectPeriods(inntektsPerioder, sisteAvsluttendeKalenderMåned)
        assertTrue(inntektsPerioder.all { it.inntekt == BigDecimal(48000) })
        assertFalse(inntektsPerioder.all { it.inneholderFangstOgFisk })
    }

    @Test
    fun ` Skal summere opp med fangst og fisk hvis paremeteret er satt til true`() {
        val sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 1)
        val beregningsdato = LocalDate.of(2019, 2, 1)
        val fakta =
            Fakta(
                Inntekt(
                    "id",
                    generateArbeidsOgFangstOgFiskInntekt(
                        36,
                        BigDecimal(2000),
                        BigDecimal(2000),
                        sisteAvsluttendeKalenderMåned,
                    ),
                    sisteAvsluttendeKalenderMåned = sisteAvsluttendeKalenderMåned,
                ),
                verneplikt = false,
                fangstOgFiske = true,
                beregningsdato = beregningsdato,
            )

        val inntektsPerioder = createInntektPerioder(fakta)!!
        assertThreeCorrectPeriods(inntektsPerioder, sisteAvsluttendeKalenderMåned)
        assertTrue(inntektsPerioder.all { it.inntekt == BigDecimal(48000) })
    }

    @Test
    fun `Skal bare ta med fangst og fisk hvis paremeteret fangstOgFisk er satt til true og det bare finnes fangst og fiske inntekter`() {
        val sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 1)
        val beregningsdato = LocalDate.of(2019, 2, 1)
        val fakta =
            Fakta(
                Inntekt(
                    "id",
                    generateFangstOgFiskInntekt(36, BigDecimal(2000), sisteAvsluttendeKalenderMåned),
                    sisteAvsluttendeKalenderMåned = sisteAvsluttendeKalenderMåned,
                ),
                verneplikt = false,
                fangstOgFiske = true,
                beregningsdato = beregningsdato,
            )

        val inntektsPerioder = createInntektPerioder(fakta)!!
        assertThreeCorrectPeriods(inntektsPerioder, sisteAvsluttendeKalenderMåned)

        assertTrue(inntektsPerioder.all { it.inntekt == BigDecimal(24000) })
        assertTrue(inntektsPerioder.all { it.inneholderFangstOgFisk })
    }

    @Suppress("ktlint:standard:max-line-length")
    @Test
    fun ` Skal bare ta med skal bare ta med Arbeidsinntekter selvom fangstOgFisk parameteret er satt til true men det ikke foreligger fangs og fiske inntekter`() {
        val sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 1)
        val beregningsdato = LocalDate.of(2019, 2, 1)
        val fakta =
            Fakta(
                Inntekt(
                    "id",
                    generateArbeidsinntekt(36, BigDecimal(2000), sisteAvsluttendeKalenderMåned),
                    sisteAvsluttendeKalenderMåned = sisteAvsluttendeKalenderMåned,
                ),
                verneplikt = false,
                fangstOgFiske = true,
                beregningsdato = beregningsdato,
            )

        val inntektsPerioder = createInntektPerioder(fakta)!!
        assertThreeCorrectPeriods(inntektsPerioder, sisteAvsluttendeKalenderMåned)

        assertTrue(inntektsPerioder.all { it.inntekt == BigDecimal(24000) })
        assertFalse(inntektsPerioder.all { it.inneholderFangstOgFisk })
    }

    @Test
    fun ` Skal ta med minus-inntekt `() {
        val sisteAvsluttedeKalenderMåned = YearMonth.of(2019, 4)
        val inntekt =
            listOf(
                KlassifisertInntektMåned(
                    YearMonth.of(2019, 3),
                    klassifiserteInntekter = getMinusInntekt(),
                ),
                KlassifisertInntektMåned(
                    YearMonth.of(2018, 3),
                    klassifiserteInntekter = getMinusInntekt(),
                ),
                KlassifisertInntektMåned(
                    YearMonth.of(2017, 3),
                    klassifiserteInntekter = getMinusInntekt(),
                ),
            )

        val fakta =
            Fakta(
                inntekt =
                    Inntekt(
                        "123",
                        inntekt,
                        sisteAvsluttendeKalenderMåned = sisteAvsluttedeKalenderMåned,
                    ),
                verneplikt = false,
                fangstOgFiske = false,
                beregningsdato = LocalDate.of(2019, 5, 20),
            )

        val inntektsPerioder = createInntektPerioder(fakta)!!
        assertThreeCorrectPeriods(inntektsPerioder, sisteAvsluttedeKalenderMåned)

        assertTrue(inntektsPerioder.all { it.inntekt == BigDecimal(-100) })
    }

    fun assertThreeCorrectPeriods(
        inntektsInfoListe: List<InntektPeriodeInfo>?,
        senesteMåned: YearMonth,
    ) {
        assertEquals(3, inntektsInfoListe?.size)

        val førstePeriode = inntektsInfoListe?.find { it.periode == 1 }
        val andrePeriode = inntektsInfoListe?.find { it.periode == 2 }
        val tredjePeriode = inntektsInfoListe?.find { it.periode == 3 }

        assertNotNull(førstePeriode)
        assertNotNull(andrePeriode)
        assertNotNull(tredjePeriode)

        assertEquals(senesteMåned, førstePeriode.inntektsPeriode.sisteMåned)
        assertEquals(senesteMåned.minusYears(1).plusMonths(1), førstePeriode.inntektsPeriode.førsteMåned)
        assertEquals(senesteMåned.minusYears(1), andrePeriode.inntektsPeriode.sisteMåned)
        assertEquals(senesteMåned.minusYears(2).plusMonths(1), andrePeriode.inntektsPeriode.førsteMåned)
        assertEquals(senesteMåned.minusYears(2), tredjePeriode.inntektsPeriode.sisteMåned)
        assertEquals(senesteMåned.minusYears(3).plusMonths(1), tredjePeriode.inntektsPeriode.førsteMåned)
    }

    private val inntektsklasser = no.nav.dagpenger.regel.grunnlag.beregning.inntektsklasser.toList()
    private val inntektsklasserFangstOgFiske =
        inntektsklasserMedFangstOgFiske.toList()
            .filterNot { no.nav.dagpenger.regel.grunnlag.beregning.inntektsklasser.toList().contains(it) }

    fun generateArbeidsinntekt(
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
                        inntektsklasser.random(),
                    ),
                ),
            )
        }
    }

    fun generateInntektMed(
        inntektKlasse: InntektKlasse,
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
                        inntektKlasse,
                    ),
                ),
            )
        }
    }

    fun getMinusInntekt(): List<KlassifisertInntekt> {
        return listOf(
            KlassifisertInntekt(
                beløp = BigDecimal(100),
                inntektKlasse = InntektKlasse.ARBEIDSINNTEKT,
            ),
            KlassifisertInntekt(
                beløp = BigDecimal(-200),
                inntektKlasse = InntektKlasse.ARBEIDSINNTEKT,
            ),
        )
    }

    fun generateFangstOgFiskInntekt(
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
                        inntektsklasserFangstOgFiske.random(),
                    ),
                ),
            )
        }
    }

    fun generateArbeidsOgFangstOgFiskInntekt(
        numberOfMonths: Int,
        arbeidsInntektBeløpPerMnd: BigDecimal,
        fangstOgFiskeBeløpPerMnd: BigDecimal,
        senesteMåned: YearMonth = YearMonth.of(2019, 1),
    ): List<KlassifisertInntektMåned> {
        return (0 until numberOfMonths).toList().map {
            KlassifisertInntektMåned(
                senesteMåned.minusMonths(it.toLong()),
                listOf(
                    KlassifisertInntekt(arbeidsInntektBeløpPerMnd, inntektsklasser.random()),
                    KlassifisertInntekt(fangstOgFiskeBeløpPerMnd, inntektsklasserFangstOgFiske.random()),
                ),
            )
        }
    }
}

package no.nav.dagpenger.regel.grunnlag

import io.mockk.mockk
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import no.nav.dagpenger.grunnbelop.Grunnbeløp
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal class CreateInntektPerioderTest {
    private val grunnlag = Grunnlag(Configuration(), mockk<GrunnlagInstrumentation>())

    @Test
    fun `Skal ha perioder med 0 inntekt hvis det ikke er inntekt`() {
        val beregningsdato = LocalDate.of(2019, 2, 1)
        val fakta = Fakta(
            verneplikt = false,
            fangstOgFisk = false,
            beregningsdato = beregningsdato,
            dagensDato = LocalDate.now(),
            gjeldendeGrunnbeløpVedBeregningsdato = Grunnbeløp.FastsattI2019,
            gjeldendeGrunnbeløpForDagensDato = Grunnbeløp.FastsattI2019
        )

        val inntektsPerioder = grunnlag.createInntektPerioder(fakta)

        assertNull(inntektsPerioder)
    }

    @Test
    fun ` Skal bare ta med Arbeidsinntekt hvis det kun finnes arbeidsinntekt i inntektslista  `() {
        val sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 1)
        val beregningsdato = LocalDate.of(2019, 2, 1)
        val inntektsListe = generateArbeidsinntekt(36, BigDecimal(1000), sisteAvsluttendeKalenderMåned)
        val fakta = Fakta(
            inntekt = Inntekt(
                "id",
                inntektsListe,
                sisteAvsluttendeKalenderMåned = sisteAvsluttendeKalenderMåned
            ),
            verneplikt = false,
            fangstOgFisk = false,
            beregningsdato = beregningsdato,
            dagensDato = LocalDate.now(),
            gjeldendeGrunnbeløpVedBeregningsdato = Grunnbeløp.FastsattI2019,
            gjeldendeGrunnbeløpForDagensDato = Grunnbeløp.FastsattI2019
        )

        val inntektsPerioder = grunnlag.createInntektPerioder(fakta)!!
        assertThreeCorrectPeriods(inntektsPerioder, sisteAvsluttendeKalenderMåned)

        Assertions.assertTrue(inntektsPerioder.all { it.inntekt == BigDecimal(12000) })
        Assertions.assertTrue(inntektsPerioder.none { it.inneholderFangstOgFisk })
    }

    @Test
    fun ` Skal indikere at fangst og fisk er med men ikke summere opp fangst og fisk sammen med arbeidsinntekt hvis ikke parameteret fangstOgFisk er satt til true `() {
        val sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 1)
        val beregningsdato = LocalDate.of(2019, 2, 1)
        val inntekt = Inntekt(
            "id",
            generateArbeidsOgFangstOgFiskInntekt(36, BigDecimal(2000), BigDecimal(2000), sisteAvsluttendeKalenderMåned),
            sisteAvsluttendeKalenderMåned = sisteAvsluttendeKalenderMåned
        )
        val fakta = Fakta(
            inntekt,
            false,
            false,
            beregningsdato,
            dagensDato = LocalDate.now(),
            gjeldendeGrunnbeløpVedBeregningsdato = Grunnbeløp.FastsattI2019,
            gjeldendeGrunnbeløpForDagensDato = Grunnbeløp.FastsattI2019
        )

        val inntektsPerioder = grunnlag.createInntektPerioder(fakta)!!
        assertThreeCorrectPeriods(inntektsPerioder, sisteAvsluttendeKalenderMåned)

        Assertions.assertTrue(inntektsPerioder.all { it.inntekt == BigDecimal(24000) })
        Assertions.assertTrue(inntektsPerioder.all { it.inneholderFangstOgFisk })
    }

    @Test
    fun ` Skal summere opp med fangst og fisk hvis paremeteret er satt til true`() {
        val sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 1)
        val beregningsdato = LocalDate.of(2019, 2, 1)
        val fakta = Fakta(
            Inntekt(
                "id",
                generateArbeidsOgFangstOgFiskInntekt(
                    36,
                    BigDecimal(2000),
                    BigDecimal(2000),
                    sisteAvsluttendeKalenderMåned
                ),
                sisteAvsluttendeKalenderMåned = sisteAvsluttendeKalenderMåned
            ),
            verneplikt = false,
            fangstOgFisk = true,
            beregningsdato = beregningsdato,
            dagensDato = LocalDate.now(),
            gjeldendeGrunnbeløpVedBeregningsdato = Grunnbeløp.FastsattI2019,
            gjeldendeGrunnbeløpForDagensDato = Grunnbeløp.FastsattI2019
        )

        val inntektsPerioder = grunnlag.createInntektPerioder(fakta)!!
        assertThreeCorrectPeriods(inntektsPerioder, sisteAvsluttendeKalenderMåned)
        Assertions.assertTrue(inntektsPerioder.all { it.inntekt == BigDecimal(48000) })
        Assertions.assertTrue(inntektsPerioder.all { it.inneholderFangstOgFisk })
    }

    @Test
    fun `Skal bare ta med fangst og fisk hvis paremeteret fangstOgFisk er satt til true og det bare finnes fangst og fiske inntekter`() {
        val sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 1)
        val beregningsdato = LocalDate.of(2019, 2, 1)
        val fakta = Fakta(
            Inntekt(
                "id",
                generateFangstOgFiskInntekt(36, BigDecimal(2000), sisteAvsluttendeKalenderMåned),
                sisteAvsluttendeKalenderMåned = sisteAvsluttendeKalenderMåned
            ),
            verneplikt = false,
            fangstOgFisk = true,
            beregningsdato = beregningsdato,
            dagensDato = LocalDate.now(),
            gjeldendeGrunnbeløpVedBeregningsdato = Grunnbeløp.FastsattI2019,
            gjeldendeGrunnbeløpForDagensDato = Grunnbeløp.FastsattI2019
        )

        val inntektsPerioder = grunnlag.createInntektPerioder(fakta)!!
        assertThreeCorrectPeriods(inntektsPerioder, sisteAvsluttendeKalenderMåned)

        Assertions.assertTrue(inntektsPerioder.all { it.inntekt == BigDecimal(24000) })
        Assertions.assertTrue(inntektsPerioder.all { it.inneholderFangstOgFisk })
    }

    @Test
    fun ` Skal bare ta med skal bare ta med Arbeidsinntekter selvom fangstOgFisk parameteret er satt til true men det ikke foreligger fangs og fiske inntekter`() {

        val sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 1)
        val beregningsdato = LocalDate.of(2019, 2, 1)
        val fakta = Fakta(
            Inntekt(
                "id",
                generateArbeidsinntekt(36, BigDecimal(2000), sisteAvsluttendeKalenderMåned),
                sisteAvsluttendeKalenderMåned = sisteAvsluttendeKalenderMåned
            ),
            verneplikt = false,
            fangstOgFisk = true,
            beregningsdato = beregningsdato,
            dagensDato = LocalDate.now(),
            gjeldendeGrunnbeløpVedBeregningsdato = Grunnbeløp.FastsattI2019,
            gjeldendeGrunnbeløpForDagensDato = Grunnbeløp.FastsattI2019
        )

        val inntektsPerioder = grunnlag.createInntektPerioder(fakta)!!
        assertThreeCorrectPeriods(inntektsPerioder, sisteAvsluttendeKalenderMåned)

        Assertions.assertTrue(inntektsPerioder.all { it.inntekt == BigDecimal(24000) })
        Assertions.assertFalse(inntektsPerioder.all { it.inneholderFangstOgFisk })
    }

    fun assertThreeCorrectPeriods(inntektsInfoListe: List<InntektPeriodeInfo>?, senesteMåned: YearMonth) {
        Assertions.assertEquals(3, inntektsInfoListe?.size)

        val førstePeriode = inntektsInfoListe?.find { it.periode == 1 }
        val andrePeriode = inntektsInfoListe?.find { it.periode == 2 }
        val tredjePeriode = inntektsInfoListe?.find { it.periode == 3 }

        assertNotNull(førstePeriode)
        assertNotNull(andrePeriode)
        assertNotNull(tredjePeriode)

        Assertions.assertEquals(senesteMåned, førstePeriode.inntektsPeriode.sisteMåned)
        Assertions.assertEquals(senesteMåned.minusYears(1).plusMonths(1), førstePeriode.inntektsPeriode.førsteMåned)
        Assertions.assertEquals(senesteMåned.minusYears(1), andrePeriode.inntektsPeriode.sisteMåned)
        Assertions.assertEquals(senesteMåned.minusYears(2).plusMonths(1), andrePeriode.inntektsPeriode.førsteMåned)
        Assertions.assertEquals(senesteMåned.minusYears(2), tredjePeriode.inntektsPeriode.sisteMåned)
        Assertions.assertEquals(senesteMåned.minusYears(3).plusMonths(1), tredjePeriode.inntektsPeriode.førsteMåned)
    }

    private val arbeidsInntekt = listOf(
        InntektKlasse.ARBEIDSINNTEKT,
        InntektKlasse.DAGPENGER,
        InntektKlasse.SYKEPENGER,
        InntektKlasse.TILTAKSLØNN
    )

    private val medFangstOgFisk = listOf(
        InntektKlasse.FANGST_FISKE,
        InntektKlasse.DAGPENGER_FANGST_FISKE,
        InntektKlasse.SYKEPENGER_FANGST_FISKE
    )

    fun generateArbeidsinntekt(
        numberOfMonths: Int,
        beløpPerMnd: BigDecimal,
        senesteMåned: YearMonth = YearMonth.of(2019, 1)
    ): List<KlassifisertInntektMåned> {
        return (0 until numberOfMonths).toList().map {
            KlassifisertInntektMåned(
                senesteMåned.minusMonths(it.toLong()), listOf(
                    KlassifisertInntekt(
                        beløpPerMnd, arbeidsInntekt.random()
                    )
                )
            )
        }
    }

    fun generateFangstOgFiskInntekt(
        numberOfMonths: Int,
        beløpPerMnd: BigDecimal,
        senesteMåned: YearMonth = YearMonth.of(2019, 1)
    ): List<KlassifisertInntektMåned> {
        return (0 until numberOfMonths).toList().map {
            KlassifisertInntektMåned(
                senesteMåned.minusMonths(it.toLong()), listOf(
                    KlassifisertInntekt(
                        beløpPerMnd, medFangstOgFisk.random()
                    )
                )
            )
        }
    }

    fun generateArbeidsOgFangstOgFiskInntekt(
        numberOfMonths: Int,
        arbeidsInntektBeløpPerMnd: BigDecimal,
        fangstOgFiskeBeløpPerMnd: BigDecimal,
        senesteMåned: YearMonth = YearMonth.of(2019, 1)
    ): List<KlassifisertInntektMåned> {
        return (0 until numberOfMonths).toList().map {
            KlassifisertInntektMåned(
                senesteMåned.minusMonths(it.toLong()),
                listOf(
                    KlassifisertInntekt(arbeidsInntektBeløpPerMnd, arbeidsInntekt.random()),
                    KlassifisertInntekt(fangstOgFiskeBeløpPerMnd, medFangstOgFisk.random())
                )
            )
        }
    }
}
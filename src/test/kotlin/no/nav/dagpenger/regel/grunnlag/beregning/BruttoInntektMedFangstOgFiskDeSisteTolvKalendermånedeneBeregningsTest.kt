package no.nav.dagpenger.regel.grunnlag.beregning

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import no.nav.dagpenger.inntekt.v1.Inntekt
import no.nav.dagpenger.inntekt.v1.InntektKlasse
import no.nav.dagpenger.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.inntekt.v1.KlassifisertInntektMåned
import no.nav.dagpenger.regel.grunnlag.Fakta
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import kotlin.test.assertEquals

class BruttoInntektMedFangstOgFiskDeSisteTolvKalendermånedeneBeregningsTest {
    private val beregning = BruttoInntektMedFangstOgFiskDeSiste12AvsluttedeKalendermånedene()

    @ParameterizedTest
    @CsvSource(
        "2021-12-31, true",
        "2022-01-01, false",
    )
    fun `Regelverk for fangst og fisk skal avvikles 01-01-2022`(
        regelverksdato: LocalDate,
        skalInkludereFangstOgFisk: Boolean,
    ) {
        val sisteAvsluttendeKalenderMåned = YearMonth.of(2021, 11)
        val arbeidsInntektsListe = generateArbeidsinntekt(12, BigDecimal(1000), sisteAvsluttendeKalenderMåned)
        val fiskOgFangstInntekt = generateFiskOgFangst(12, BigDecimal(1000), sisteAvsluttendeKalenderMåned)
        val fakta =
            Fakta(
                inntekt =
                Inntekt(
                    "123",
                    arbeidsInntektsListe + fiskOgFangstInntekt,
                    sisteAvsluttendeKalenderMåned = sisteAvsluttendeKalenderMåned,
                ),
                fangstOgFiske = true,
                lærling = false,
                verneplikt = false,
                beregningsdato = regelverksdato,
                regelverksdato = regelverksdato,
            )

        beregning.calculate(fakta).also {
            assertEquals(skalInkludereFangstOgFisk, it is BeregningsResultat)
        }
    }

    @Test
    fun `Skal ikke behandle lærlinger der beregningsdato er definert innenfor korona periode (20 mars til 31 desember 2020)`() {
        val fakta =
            Fakta(
                inntekt = null,
                fangstOgFiske = false,
                lærling = true,
                verneplikt = false,
                beregningsdato = LocalDate.of(2020, 3, 20),
            )
        false shouldBe beregning.isActive(fakta)
    }

    @Test
    fun `Skal ikke behandle lærlinger som ordinær der beregningsdato er definert utenfor korona periode (20 mars til 31 desember 2020)`() {
        val fakta =
            Fakta(
                inntekt = null,
                fangstOgFiske = false,
                lærling = true,
                verneplikt = false,
                beregningsdato = LocalDate.of(2020, 3, 1),
            )
        true shouldBe beregning.isActive(fakta)
    }

    @Test
    fun ` Skal gi grunnlag på 2034,699 siste 12 kalendermåned gitt mars 2019 `() {
        val inntektsListe =
            listOf(
                KlassifisertInntektMåned(
                    YearMonth.of(2018, 4),
                    listOf(
                        KlassifisertInntekt(
                            BigDecimal(500),
                            InntektKlasse.ARBEIDSINNTEKT,
                        ),
                        KlassifisertInntekt(
                            BigDecimal(500),
                            InntektKlasse.FANGST_FISKE,
                        ),
                    ),
                ),
                KlassifisertInntektMåned(
                    YearMonth.of(2018, 5),
                    listOf(
                        KlassifisertInntekt(
                            BigDecimal(500),
                            InntektKlasse.ARBEIDSINNTEKT,
                        ),
                        KlassifisertInntekt(
                            BigDecimal(500),
                            InntektKlasse.SYKEPENGER_FANGST_FISKE,
                        ),
                    ),
                ),
            )

        val fakta =
            Fakta(
                inntekt = Inntekt("123", inntektsListe, sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 3)),
                verneplikt = false,
                fangstOgFiske = true,
                beregningsdato = LocalDate.of(2019, 4, 1),
            )

        when (val beregningsResultat = beregning.calculate(fakta)) {
            is BeregningsResultat ->
                beregningsResultat.uavkortet shouldBe BigDecimal("2034.69893414785227588000")
            else -> beregningsResultat.shouldBeTypeOf<BeregningsResultat>()
        }
    }

    @Test
    fun ` Skal gi riktig grunnlag med minusinntekt`() {
        val inntektsListe =
            listOf(
                KlassifisertInntektMåned(
                    YearMonth.of(2018, 4),
                    listOf(
                        KlassifisertInntekt(
                            BigDecimal(1000),
                            InntektKlasse.FANGST_FISKE,
                        ),
                    ),
                ),
                KlassifisertInntektMåned(
                    YearMonth.of(2018, 5),
                    listOf(
                        KlassifisertInntekt(
                            BigDecimal(1000),
                            InntektKlasse.ARBEIDSINNTEKT,
                        ),
                        KlassifisertInntekt(
                            BigDecimal(-1000),
                            InntektKlasse.FANGST_FISKE,
                        ),
                    ),
                ),
            )

        val fakta =
            Fakta(
                inntekt = Inntekt("123", inntektsListe, sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 1)),
                verneplikt = false,
                fangstOgFiske = true,
                beregningsdato = LocalDate.of(2019, 4, 1),
            )

        when (val beregningsResultat = beregning.calculate(fakta)) {
            is BeregningsResultat ->
                beregningsResultat.uavkortet shouldBe BigDecimal("1034.69893414785227588000")
            else -> beregningsResultat.shouldBeTypeOf<BeregningsResultat>()
        }
    }

    @Test
    fun ` Skal gi riktig grunnlag dersom summen av inntekter er minus`() {
        val inntektsListe =
            listOf(
                KlassifisertInntektMåned(
                    YearMonth.of(2018, 4),
                    listOf(
                        KlassifisertInntekt(
                            BigDecimal(-1000),
                            InntektKlasse.FANGST_FISKE,
                        ),
                    ),
                ),
                KlassifisertInntektMåned(
                    YearMonth.of(2018, 5),
                    listOf(
                        KlassifisertInntekt(
                            BigDecimal(1000),
                            InntektKlasse.ARBEIDSINNTEKT,
                        ),
                        KlassifisertInntekt(
                            BigDecimal(-1000),
                            InntektKlasse.FANGST_FISKE,
                        ),
                    ),
                ),
            )

        val fakta =
            Fakta(
                inntekt = Inntekt("123", inntektsListe, sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 1)),
                fangstOgFiske = true,
                verneplikt = false,
                beregningsdato = LocalDate.of(2019, 2, 10),
            )

        when (val beregningsResultat = beregning.calculate(fakta)) {
            is BeregningsResultat ->
                beregningsResultat.uavkortet shouldBe BigDecimal("-1034.69893414785227588000")
            else -> beregningsResultat.shouldBeTypeOf<BeregningsResultat>()
        }
    }

    @Test
    fun `Skal returnere ingenBeregningsResultat når fangst og fisk er false`() {
        val fakta =
            Fakta(
                inntekt = Inntekt("123", emptyList(), sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 3)),
                fangstOgFiske = false,
                verneplikt = false,
                beregningsdato = LocalDate.of(2019, 4, 1),
            )

        when (val beregningsResultat = beregning.calculate(fakta)) {
            is IngenBeregningsResultat ->
                beregningsResultat.beskrivelse shouldBe "FangstOgFiskeSiste12(2021)"
            else -> beregningsResultat.shouldBeTypeOf<IngenBeregningsResultat>()
        }
    }

    @Test
    fun ` Skal gi ingen beregningsresultat dersom fangst og fisk ikke er satt selv om det er inntekt`() {
        val inntektsListe =
            listOf(
                KlassifisertInntektMåned(
                    YearMonth.of(2018, 4),
                    listOf(
                        KlassifisertInntekt(
                            BigDecimal(500),
                            InntektKlasse.ARBEIDSINNTEKT,
                        ),
                        KlassifisertInntekt(
                            BigDecimal(500),
                            InntektKlasse.FANGST_FISKE,
                        ),
                    ),
                ),
                KlassifisertInntektMåned(
                    YearMonth.of(2018, 5),
                    listOf(
                        KlassifisertInntekt(
                            BigDecimal(500),
                            InntektKlasse.ARBEIDSINNTEKT,
                        ),
                        KlassifisertInntekt(
                            BigDecimal(500),
                            InntektKlasse.SYKEPENGER_FANGST_FISKE,
                        ),
                    ),
                ),
            )

        val fakta =
            Fakta(
                inntekt = Inntekt("123", inntektsListe, sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 3)),
                verneplikt = false,
                fangstOgFiske = false,
                beregningsdato = LocalDate.of(2019, 4, 1),
            )

        beregning.calculate(fakta).also {
            it.shouldBeTypeOf<IngenBeregningsResultat>()
            val resultat = it
            resultat.beskrivelse shouldBe "FangstOgFiskeSiste12(2021)"
        }
    }
}

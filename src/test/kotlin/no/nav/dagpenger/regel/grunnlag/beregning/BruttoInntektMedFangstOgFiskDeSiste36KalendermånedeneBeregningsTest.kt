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

class BruttoInntektMedFangstOgFiskDeSiste36KalendermånedeneBeregningsTest {
    private val beregning = BruttoInntektMedFangstOgFiskDeSiste36AvsluttedeKalendermånedene()

    @ParameterizedTest
    @CsvSource(
        "2020-03-01, true",
        "2020-03-20, false",
        "2022-03-31, false",
        "2022-04-01, true",
    )
    fun `Skal ikke behandle lærlinger der regelverksdato er definert innenfor en av korona periodene`(
        regelverksdato: LocalDate,
        lærlingSkalBehandles: Boolean,
    ) {
        val fakta =
            Fakta(
                inntekt = null,
                fangstOgFiske = true,
                lærling = true,
                verneplikt = false,
                beregningsdato = regelverksdato,
                regelverksdato = regelverksdato,
            )
        beregning.isActive(fakta) shouldBe lærlingSkalBehandles
    }

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
        val arbeidsInntektsListe = generateArbeidsinntekt(36, BigDecimal(1000), sisteAvsluttendeKalenderMåned)
        val fiskOgFangstInntekt = generateFiskOgFangst(36, BigDecimal(1000), sisteAvsluttendeKalenderMåned)
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
    fun ` Skal gi grunnlag på 4115 siste 36 kalendermåned gitt mars 2019 inntekt med fangstOgFisk inntekt når fangst og fisk er satt`() {
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
                    ),
                ),
                KlassifisertInntektMåned(
                    YearMonth.of(2017, 5),
                    listOf(
                        KlassifisertInntekt(
                            BigDecimal(1000),
                            InntektKlasse.FANGST_FISKE,
                        ),
                    ),
                ),
                KlassifisertInntektMåned(
                    YearMonth.of(2016, 5),
                    listOf(
                        KlassifisertInntekt(
                            BigDecimal(1000),
                            InntektKlasse.ARBEIDSINNTEKT,
                        ),
                    ),
                ),
            )

        val fakta =
            Fakta(
                inntekt = Inntekt("123", inntektsListe, sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 3)),
                fangstOgFiske = true,
                verneplikt = false,
                beregningsdato = LocalDate.of(2019, 4, 1),
            )

        when (val beregningsResultat = beregning.calculate(fakta)) {
            is BeregningsResultat ->
                beregningsResultat.uavkortet shouldBe BigDecimal("1371.97393512841033163333")
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
                KlassifisertInntektMåned(
                    YearMonth.of(2017, 5),
                    listOf(
                        KlassifisertInntekt(
                            BigDecimal(1000),
                            InntektKlasse.FANGST_FISKE,
                        ),
                        KlassifisertInntekt(
                            BigDecimal(-1000),
                            InntektKlasse.ARBEIDSINNTEKT,
                        ),
                    ),
                ),
                KlassifisertInntektMåned(
                    YearMonth.of(2016, 10),
                    listOf(
                        KlassifisertInntekt(
                            BigDecimal(1000),
                            InntektKlasse.FANGST_FISKE,
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
                beregningsdato = LocalDate.of(2019, 5, 10),
            )

        when (val beregningsResultat = beregning.calculate(fakta)) {
            is BeregningsResultat ->
                beregningsResultat.uavkortet shouldBe BigDecimal("355.49052694534036781667")
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
                KlassifisertInntektMåned(
                    YearMonth.of(2017, 5),
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
                KlassifisertInntektMåned(
                    YearMonth.of(2016, 10),
                    listOf(
                        KlassifisertInntekt(
                            BigDecimal(1000),
                            InntektKlasse.FANGST_FISKE,
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
                beregningsdato = LocalDate.of(2019, 5, 10),
            )

        when (val beregningsResultat = beregning.calculate(fakta)) {
            is BeregningsResultat ->
                beregningsResultat.uavkortet shouldBe BigDecimal("-355.49052694534036781667")
            else -> beregningsResultat.shouldBeTypeOf<BeregningsResultat>()
        }
    }

    @Test
    fun `Skal returnere IngenBeregningsResultat fra denne reglenen hvis ingen inntekt`() {
        val fakta =
            Fakta(
                inntekt = Inntekt("123", emptyList(), sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 3)),
                fangstOgFiske = false,
                verneplikt = false,
                beregningsdato = LocalDate.of(2019, 4, 1),
            )

        when (val beregningsResultat = beregning.calculate(fakta)) {
            is IngenBeregningsResultat ->
                beregningsResultat.beskrivelse shouldBe
                    "FangstOgFiskSiste36(2021)"
            else -> beregningsResultat.shouldBeTypeOf<IngenBeregningsResultat>()
        }
    }
}

internal fun generateFiskOgFangst(
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
                    InntektKlasse.FANGST_FISKE,
                ),
            ),
        )
    }
}

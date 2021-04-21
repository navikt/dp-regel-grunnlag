package no.nav.dagpenger.regel.grunnlag.beregning

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import no.nav.dagpenger.regel.grunnlag.Fakta
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

class BruttoArbeidsinntektDeSiste12AvsluttedeKalendermånedeneBeregningsTest {

    private val beregning = BruttoArbeidsinntektDeSiste12AvsluttedeKalendermånedene()

    @Test
    fun `Skal ikke behandle lærlinger der beregningsdato er definert innenfor korona periode (20 mars til 31 desember 2020)`() {
        val fakta = Fakta(
            inntekt = null,
            fangstOgFisk = false,
            lærling = true,
            verneplikt = false,
            beregningsdato = LocalDate.of(2020, 3, 20)
        )
        false shouldBe beregning.isActive(fakta)
    }

    @Test
    fun `Skal ikke behandle lærlinger som ordinær der beregningsdato er definert utenfor korona periode (20 mars til 31 desember 2020)`() {
        val fakta = Fakta(
            inntekt = null,
            fangstOgFisk = false,
            lærling = true,
            verneplikt = false,
            beregningsdato = LocalDate.of(2020, 3, 1)
        )
        true shouldBe beregning.isActive(fakta)
    }

    @Test
    fun ` Skal gi uavkortet grunnlag på 2034,699 siste 12 kalendermåned gitt mars 2019 inntekt`() {

        val inntektsListe = listOf(
            KlassifisertInntektMåned(
                YearMonth.of(2018, 4),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(1000),
                        InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            ),
            KlassifisertInntektMåned(
                YearMonth.of(2018, 5),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(1000),
                        InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            )
        )

        val fakta = Fakta(
            inntekt = Inntekt("123", inntektsListe, sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 3)),
            fangstOgFisk = false,
            verneplikt = false,
            beregningsdato = LocalDate.of(2019, 4, 1)
        )

        when (val beregningsResultat = beregning.calculate(fakta)) {
            is BeregningsResultat ->
                beregningsResultat.uavkortet shouldBe
                    BigDecimal("2034.69893414785227588000")
            else -> beregningsResultat.shouldBeTypeOf<BeregningsResultat>()
        }
    }

    @Test
    fun `Skal gi riktig avkortet grunnlag siste 12 kalendermåneder gitt mars 2019 inntekt `() {

        val inntektsListe = listOf(
            KlassifisertInntektMåned(
                YearMonth.of(2018, 4),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(300000),
                        InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            ),
            KlassifisertInntektMåned(
                YearMonth.of(2018, 5),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(300000),
                        InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            )
        )

        val fakta = Fakta(
            inntekt = Inntekt("123", inntektsListe, sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 3)),
            fangstOgFisk = false,
            verneplikt = false,
                beregningsdato = LocalDate.of(2019, 4, 1)
        )

        when (val beregningsResultat = beregning.calculate(fakta)) {
            is BeregningsResultat -> {
                beregningsResultat.avkortet shouldBe BigDecimal("581298")
                beregningsResultat.harAvkortet shouldBe true
            }
            else -> beregningsResultat.shouldBeTypeOf<BeregningsResultat>()
        }
    }

    @Test
    fun `Skal ikke ta med måneder som ikke er innenfor ønsket periode`() {
        val inntektsListe = listOf(
            KlassifisertInntektMåned(
                YearMonth.of(2018, 4),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(1000),
                        InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            ),
            KlassifisertInntektMåned(
                YearMonth.of(2018, 5),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(1000),
                        InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            ),
            KlassifisertInntektMåned(
                YearMonth.of(2017, 5),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(1000),
                        InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            )
        )

        val fakta = Fakta(
            inntekt = Inntekt("123", inntektsListe, sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 3)),
            fangstOgFisk = false,
            verneplikt = false,
                beregningsdato = LocalDate.of(2019, 4, 1)
        )

        when (val beregningsResultat = beregning.calculate(fakta)) {
            is BeregningsResultat ->
                beregningsResultat.uavkortet shouldBe
                    BigDecimal("2034.69893414785227588000")
            else -> beregningsResultat.shouldBeTypeOf<BeregningsResultat>()
        }
    }

    @Test
    fun ` Skal gi riktig grunnlag med minusinntekt`() {

        val inntektsListe = listOf(
            KlassifisertInntektMåned(
                YearMonth.of(2018, 4),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(1000),
                        InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            ),
            KlassifisertInntektMåned(
                YearMonth.of(2018, 5),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(1000),
                        InntektKlasse.ARBEIDSINNTEKT
                    ),
                    KlassifisertInntekt(
                        BigDecimal(-1000),
                        InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            )
        )

        val fakta = Fakta(
            inntekt = Inntekt("123", inntektsListe, sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 1)),
            fangstOgFisk = false,
            verneplikt = false,
                beregningsdato = LocalDate.of(2019, 5, 10)
        )

        when (val beregningsResultat = beregning.calculate(fakta)) {
            is BeregningsResultat ->
                beregningsResultat.uavkortet shouldBe
                    BigDecimal("1066.47158083602110345000")
            else -> beregningsResultat.shouldBeTypeOf<BeregningsResultat>()
        }
    }

    @Test
    fun ` Skal gi riktig grunnlag dersom summen av inntekter er minus`() {

        val inntektsListe = listOf(
            KlassifisertInntektMåned(
                YearMonth.of(2018, 4),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(-1000),
                        InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            ),
            KlassifisertInntektMåned(
                YearMonth.of(2018, 5),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(1000),
                        InntektKlasse.ARBEIDSINNTEKT
                    ),
                    KlassifisertInntekt(
                        BigDecimal(-1000),
                        InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            )
        )

        val fakta = Fakta(
            inntekt = Inntekt("123", inntektsListe, sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 1)),
            fangstOgFisk = false,
            verneplikt = false,
                beregningsdato = LocalDate.of(2019, 5, 10)
        )

        when (val beregningsResultat = beregning.calculate(fakta)) {
            is BeregningsResultat ->
                beregningsResultat.uavkortet shouldBe
                    BigDecimal("-1066.47158083602110345000")
            else -> beregningsResultat.shouldBeTypeOf<BeregningsResultat>()
        }
    }

    @Test
    fun `Skal returnere 0 som grunnlag hvis ingen inntekt`() {

        val fakta = Fakta(
            inntekt = Inntekt("123", emptyList(), sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 3)),
            fangstOgFisk = false,
            verneplikt = false,
                beregningsdato = LocalDate.of(2019, 4, 1)
        )

        when (val beregningsResultat = beregning.calculate(fakta)) {
            is BeregningsResultat ->
                beregningsResultat.uavkortet shouldBe
                    BigDecimal.ZERO
            else -> beregningsResultat.shouldBeTypeOf<BeregningsResultat>()
        }
    }
}

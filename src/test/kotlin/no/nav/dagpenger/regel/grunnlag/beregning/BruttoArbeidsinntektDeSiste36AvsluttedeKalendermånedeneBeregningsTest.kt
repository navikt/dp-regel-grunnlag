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

class BruttoArbeidsinntektDeSiste36AvsluttedeKalendermånedeneBeregningsTest {

    private val beregning = BruttoArbeidsinntektDeSiste36AvsluttedeKalendermånedene()

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
    fun ` Skal gi uavkortet grunnlag på 4115 siste 36 kalendermåned gitt mars 2019 inntekt`() {

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
            ),
            KlassifisertInntektMåned(
                YearMonth.of(2016, 5),
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
                    BigDecimal("1371.97393512841033163333")
            else -> beregningsResultat.shouldBeTypeOf<BeregningsResultat>()
        }
    }

    @Test
    fun `Skal gi riktig avkortet grunnlag siste 36 kalendermåneder gitt desember 2021 inntekt`() {
        val inntektsListe = listOf(
            KlassifisertInntektMåned(
                YearMonth.of(2021, 11),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(500000),
                        InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            ),
            KlassifisertInntektMåned(
                YearMonth.of(2021, 10),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(500000),
                        InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            ),
            KlassifisertInntektMåned(
                YearMonth.of(2019, 5),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(1000),
                        InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            )
        )

        val fakta = Fakta(
            inntekt = Inntekt("123", inntektsListe, sisteAvsluttendeKalenderMåned = YearMonth.of(2021, 12)),
            fangstOgFisk = false,
            verneplikt = false,
            beregningsdato = LocalDate.of(2021, 12, 18)
        )

        when (val beregningsResultat = beregning.calculate(fakta)) {
            is BeregningsResultat ->
                beregningsResultat.avkortet shouldBe
                    BigDecimal("213153.16767142675933158333")
            else -> beregningsResultat.shouldBeTypeOf<BeregningsResultat>()
        }
    }

    @Test
    fun `Ulike beregningsregler brukes avhengig av beregningsdato`() {
        val g = 106399.toBigDecimal()

        val inntektsListe = listOf(
            KlassifisertInntektMåned(
                YearMonth.of(2022, 1),
                listOf(
                    KlassifisertInntekt(
                        g.multiply(9.toBigDecimal()),
                        InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            )
        )

        val fakta = Fakta(
            inntekt = Inntekt("123", inntektsListe, sisteAvsluttendeKalenderMåned = YearMonth.of(2022, 1)),
            fangstOgFisk = false,
            verneplikt = false,
            beregningsdato = LocalDate.of(2022, 4, 10)
        )
        val beregningsResultat = beregning.calculate(fakta)
        require(beregningsResultat is BeregningsResultat)
        val toG = g.multiply(2.toBigDecimal())
        beregningsResultat.avkortet.toInt() shouldBe toG.toInt()
        beregningsResultat.beregningsregel shouldBe "ArbeidsinntektSiste36(2021)"

        val fakta2 = fakta.copy(beregningsdato = LocalDate.of(2021, 12, 16))
        val beregningsResultat2 = beregning.calculate(fakta2)
        require(beregningsResultat2 is BeregningsResultat)
        val treG = g.multiply(3.toBigDecimal())
        beregningsResultat2.avkortet.toInt() shouldBe treG.toInt()
        beregningsResultat2.beregningsregel shouldBe "ArbeidsinntektSiste36(2021)"
    }

    @Test
    fun `Ulike beregningsregler brukes avhengig av beregningsdato - uavkortet overstiger 6g`() {
        val g = 106399.toBigDecimal()

        val inntektsListe = listOf(
            KlassifisertInntektMåned(
                YearMonth.of(2022, 1),
                listOf(
                    KlassifisertInntekt(
                        g.multiply(21.toBigDecimal()),
                        InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            )
        )

        val fakta = Fakta(
            inntekt = Inntekt("123", inntektsListe, sisteAvsluttendeKalenderMåned = YearMonth.of(2022, 1)),
            fangstOgFisk = false,
            verneplikt = false,
            beregningsdato = LocalDate.of(2022, 4, 10)
        )
        val beregningsResultat = beregning.calculate(fakta)
        require(beregningsResultat is BeregningsResultat)

        val toG = g.multiply(2.toBigDecimal())
        beregningsResultat.avkortet.toInt() shouldBe toG.toInt()

        val fakta2 = fakta.copy(beregningsdato = LocalDate.of(2021, 12, 16))
        val beregningsResultat2 = beregning.calculate(fakta2)
        require(beregningsResultat2 is BeregningsResultat)

        val seksG = g.multiply(6.toBigDecimal())
        beregningsResultat2.avkortet.toInt() shouldBe seksG.toInt()
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
            ),
            KlassifisertInntektMåned(
                YearMonth.of(2016, 5),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(1000),
                        InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            ),
            KlassifisertInntektMåned(
                YearMonth.of(2015, 5),
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
                    BigDecimal("1371.97393512841033163333")
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
            ),
            KlassifisertInntektMåned(
                YearMonth.of(2017, 5),
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
            ),
            KlassifisertInntektMåned(
                YearMonth.of(2016, 10),
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
                    BigDecimal("355.49052694534036781667")
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
            ),
            KlassifisertInntektMåned(
                YearMonth.of(2017, 5),
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
            ),
            KlassifisertInntektMåned(
                YearMonth.of(2016, 10),
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
                    BigDecimal("-355.49052694534036781667")
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
                    BigDecimal.ZERO.setScale(20)
            else -> beregningsResultat.shouldBeTypeOf<BeregningsResultat>()
        }
    }

    @Test
    fun ` gir ikke harAvkortet selv om heltallene er like (BigDecimal må lages i samme skala)`() {

        val inntektsListe = listOf(
            KlassifisertInntektMåned(
                YearMonth.of(2018, 4),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(1000).setScale(3),
                        InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            ),
            KlassifisertInntektMåned(
                YearMonth.of(2017, 5),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(1000).setScale(5),
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
                beregningsResultat.harAvkortet shouldBe false
                beregningsResultat.avkortet shouldBe beregningsResultat.uavkortet
            } else -> beregningsResultat.shouldBeTypeOf<BeregningsResultat>()
        }
    }
}

package no.nav.dagpenger.regel.grunnlag.beregning

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import no.nav.dagpenger.regel.grunnlag.Fakta
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import kotlin.test.assertFalse

internal class EtterLærlingForskriftTest() {

    val beregning = object : GrunnlagEtterLærlingForskrift(
        regelIdentifikator = "test",
        grunnlagUtvelgelse = SisteAvsluttendeMånedUtvelgelse(),
        inntektKlasser = inntektKlassifisertEtterFangstOgFisk,
    ) {
    }

    @Test
    fun ` Beregning er aktiv til fra 20 mars 2020 til 31 mars 2022`() {
        val fakta = Fakta(
            inntekt = null,
            fangstOgFisk = false,
            lærling = true,
            verneplikt = false,
            beregningsdato = LocalDate.of(2020, 3, 20),
        )

        assertTrue(beregning.isActive(fakta))
        assertTrue(beregning.isActive(fakta.copy(regelverksdato = LocalDate.of(2022, 3, 31))))
        assertFalse(beregning.isActive(fakta.copy(regelverksdato = LocalDate.of(2020, 2, 29))))
        assertFalse(beregning.isActive(fakta.copy(regelverksdato = LocalDate.of(2022, 4, 1))))
    }

    @Test
    fun `Skal behandle lærlinger der beregningsdato er definert innenfor korona periode (20 mars til 31 desember 2020)`() {
        val fakta = Fakta(
            inntekt = null,
            fangstOgFisk = false,
            lærling = true,
            verneplikt = false,
            beregningsdato = LocalDate.of(2020, 3, 20),
        )
        true shouldBe beregning.isActive(fakta)
    }

    @Test
    fun `Skal ikke behandle lærlinger der beregningsdato er definert utenfor korona periode 20 mars til 31 desember 2020`() {
        val fakta = Fakta(
            inntekt = null,
            fangstOgFisk = false,
            lærling = true,
            verneplikt = false,
            beregningsdato = LocalDate.of(2020, 3, 1),
        )
        false shouldBe beregning.isActive(fakta)
    }

    @Test
    fun `Skal ikke behandle lærlinger der manuelt grunnlag er satt`() {
        val fakta = Fakta(
            inntekt = null,
            fangstOgFisk = false,
            manueltGrunnlag = 1000,
            lærling = true,
            verneplikt = false,
            beregningsdato = LocalDate.of(2020, 3, 21),
        )
        false shouldBe beregning.isActive(fakta)
    }

    @Test
    fun `Skal bruke siste kalender måned og gange med 12 for å finne uavkortet grunnlag for arbeidsinntekt`() {
        val fakta = Fakta(
            inntekt = Inntekt("123", inntektsListe, sisteAvsluttendeKalenderMåned = YearMonth.of(2020, 3)),
            verneplikt = false,
            fangstOgFisk = false,
            lærling = true,
            beregningsdato = LocalDate.of(2020, 4, 1),
        )

        when (
            val beregningsResultat =
                LærlingForskriftSisteAvsluttendeKalenderMåned().calculate(fakta)
        ) {
            is BeregningsResultat -> {
                BigDecimal("12000") shouldBe beregningsResultat.uavkortet
                BigDecimal("12000") shouldBe beregningsResultat.avkortet
                "LærlingArbeidsinntekt1x12" shouldBe beregningsResultat.beregningsregel
            }

            else -> beregningsResultat.shouldBeTypeOf<BeregningsResultat>()
        }
    }

    @ParameterizedTest
    @ValueSource(ints = [2020, 2021])
    fun `Skal aldri oppjusteres med G faktor `(år: Int) {
        val fakta = Fakta(
            inntekt = Inntekt("123", inntektsListe, sisteAvsluttendeKalenderMåned = YearMonth.of(2020, 3)),
            verneplikt = false,
            fangstOgFisk = false,
            lærling = true,
            beregningsdato = LocalDate.of(år, 5, 31),
        )

        when (
            val beregningsResultat =
                LærlingForskriftSisteAvsluttendeKalenderMåned().calculate(fakta)
        ) {
            is BeregningsResultat -> {
                BigDecimal("12000") shouldBe beregningsResultat.uavkortet
                BigDecimal("12000") shouldBe beregningsResultat.avkortet
                "LærlingArbeidsinntekt1x12" shouldBe beregningsResultat.beregningsregel
            }

            else -> beregningsResultat.shouldBeTypeOf<BeregningsResultat>()
        }
    }

    @Test
    fun `Skal bruke siste 3 kalendermånedene og gange med 4 for å finne uavkortet grunnlag for arbeidsinntekt `() {
        val fakta = Fakta(
            inntekt = Inntekt("123", inntektsListe, sisteAvsluttendeKalenderMåned = YearMonth.of(2020, 3)),
            verneplikt = false,
            fangstOgFisk = false,
            lærling = true,
            beregningsdato = LocalDate.of(2020, 4, 1),
        )

        when (
            val beregningsResultat =
                LærlingForskriftSiste3AvsluttendeKalenderMåned().calculate(fakta)
        ) {
            is BeregningsResultat -> {
                BigDecimal("20000") shouldBe beregningsResultat.uavkortet
                BigDecimal("20000") shouldBe beregningsResultat.avkortet
                "LærlingArbeidsinntekt3x4" shouldBe beregningsResultat.beregningsregel
            }

            else -> beregningsResultat.shouldBeTypeOf<BeregningsResultat>()
        }
    }

    @Test
    fun `Skal bruke siste kalender måned og gange med 12 for å finne uavkortet grunnlag for fangst- og fiskeinntekt`() {
        val fakta = Fakta(
            inntekt = Inntekt("123", inntektsListe, sisteAvsluttendeKalenderMåned = YearMonth.of(2020, 3)),
            verneplikt = false,
            fangstOgFisk = true,
            lærling = true,
            beregningsdato = LocalDate.of(2020, 4, 1),
        )

        when (
            val beregningsResultat =
                LærlingForskriftSisteAvsluttendeKalenderMånedFangstOgFisk().calculate(fakta)
        ) {
            is BeregningsResultat -> {
                BigDecimal("36000") shouldBe beregningsResultat.uavkortet
                BigDecimal("36000") shouldBe beregningsResultat.avkortet
                "LærlingFangstOgFisk1x12" shouldBe beregningsResultat.beregningsregel
            }

            else -> beregningsResultat.shouldBeTypeOf<BeregningsResultat>()
        }
    }

    @Test
    fun `Skal bruke siste 3 kalendermånedene og gange med 4 for å finne uavkortet grunnlag for fangst- og fiskeinntekt `() {
        val fakta = Fakta(
            inntekt = Inntekt("123", inntektsListe, sisteAvsluttendeKalenderMåned = YearMonth.of(2020, 3)),
            verneplikt = false,
            fangstOgFisk = true,
            lærling = true,
            beregningsdato = LocalDate.of(2020, 4, 1),
        )

        when (
            val beregningsResultat =
                LærlingForskriftSiste3AvsluttendeKalenderMånedFangsOgFisk().calculate(fakta)
        ) {
            is BeregningsResultat -> {
                BigDecimal("28000") shouldBe beregningsResultat.uavkortet
                BigDecimal("28000") shouldBe beregningsResultat.avkortet
                "LærlingFangstOgFisk3x4" shouldBe beregningsResultat.beregningsregel
            }

            else -> beregningsResultat.shouldBeTypeOf<BeregningsResultat>()
        }
    }

    @Test
    fun `Manuelt grunnlag skal ikke beregnes av lærling grunnlag `() {
        val fakta = Fakta(
            inntekt = null,
            manueltGrunnlag = 1000,
            verneplikt = false,
            fangstOgFisk = true,
            lærling = true,
            beregningsdato = LocalDate.now(),
        )

        LærlingForskriftSiste3AvsluttendeKalenderMånedFangsOgFisk().calculate(fakta).shouldBeTypeOf<IngenBeregningsResultat>()
        LærlingForskriftSiste3AvsluttendeKalenderMånedFangsOgFisk().calculate(fakta).shouldBeTypeOf<IngenBeregningsResultat>()
        LærlingForskriftSiste3AvsluttendeKalenderMånedFangsOgFisk().calculate(fakta).shouldBeTypeOf<IngenBeregningsResultat>()
        LærlingForskriftSisteAvsluttendeKalenderMånedFangstOgFisk().calculate(fakta).shouldBeTypeOf<IngenBeregningsResultat>()
    }

    @Test
    fun `Ingen inntekt gir ingen grunnlag for fangst og fisk`() {
        val fakta = Fakta(
            inntekt = Inntekt("123", emptyList(), sisteAvsluttendeKalenderMåned = YearMonth.now()),
            verneplikt = false,
            fangstOgFisk = true,
            lærling = true,
            beregningsdato = LocalDate.of(2021, 5, 1),
        )

        assertSoftly {
            val resultatSiste3 = LærlingForskriftSiste3AvsluttendeKalenderMånedFangsOgFisk().calculate(fakta) as BeregningsResultat
            0.toBigDecimal() shouldBe resultatSiste3.avkortet
            0.toBigDecimal() shouldBe resultatSiste3.uavkortet

            val resultatSiste1 = LærlingForskriftSisteAvsluttendeKalenderMånedFangstOgFisk().calculate(fakta) as BeregningsResultat
            0.toBigDecimal() shouldBe resultatSiste1.avkortet
            0.toBigDecimal() shouldBe resultatSiste1.uavkortet
        }
    }

    @Test
    fun `Ingen inntekt gir ingen grunnlag for arbeidsinntekt `() {
        val fakta = Fakta(
            inntekt = Inntekt("123", emptyList(), sisteAvsluttendeKalenderMåned = YearMonth.now()),
            verneplikt = false,
            fangstOgFisk = false,
            lærling = true,
            beregningsdato = LocalDate.of(2022, 3, 30),
        )

        assertSoftly {
            val resultatSiste3 = LærlingForskriftSisteAvsluttendeKalenderMåned().calculate(fakta) as BeregningsResultat
            0.toBigDecimal() shouldBe resultatSiste3.avkortet
            0.toBigDecimal() shouldBe resultatSiste3.uavkortet

            val resultatSiste1 = LærlingForskriftSiste3AvsluttendeKalenderMåned().calculate(fakta) as BeregningsResultat
            0.toBigDecimal() shouldBe resultatSiste1.avkortet
            0.toBigDecimal() shouldBe resultatSiste1.uavkortet
        }
    }

    private val inntektsListe = listOf(
        KlassifisertInntektMåned(
            YearMonth.of(2020, 3),
            listOf(
                KlassifisertInntekt(
                    BigDecimal(1000),
                    InntektKlasse.ARBEIDSINNTEKT,
                ),
                KlassifisertInntekt(
                    BigDecimal(2000),
                    InntektKlasse.FANGST_FISKE,
                ),
            ),
        ),
        KlassifisertInntektMåned(
            YearMonth.of(2020, 2),
            listOf(
                KlassifisertInntekt(
                    BigDecimal(2000),
                    InntektKlasse.ARBEIDSINNTEKT,
                ),
            ),
        ),
        KlassifisertInntektMåned(
            YearMonth.of(2020, 1),
            listOf(
                KlassifisertInntekt(
                    BigDecimal(2000),
                    InntektKlasse.ARBEIDSINNTEKT,
                ),
            ),
        ),
        KlassifisertInntektMåned(
            YearMonth.of(2019, 12),
            listOf(
                KlassifisertInntekt(
                    BigDecimal(2000),
                    InntektKlasse.ARBEIDSINNTEKT,
                ),
            ),
        ),
    )
}

package no.nav.dagpenger.regel.grunnlag

import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.YearMonth
import kotlin.test.assertEquals

class GrunnlagsResultatTest {

/*    @Test
    fun `Skal få grunnlag på 3G når verneplikt er satt `() {
        val resultat = finnUavkortetGrunnlag(
            true,
            Inntekt("123", emptyList()),
            YearMonth.of(2019, 4),
            false)
        assertEquals( BigDecimal(290649), resultat)
    }

    @Test
    fun `Skal få grunnlag på 0 uten inntekt og uten verneplikt`() {
        val resultat = finnUavkortetGrunnlag(
            false,
            Inntekt("123", emptyList()),
            YearMonth.of(2019, 4),
            false)
        assertEquals(BigDecimal(0), resultat)
    }

    @Test
    fun `Skal få riktig grunnlag med mellom 1,5 og 2G inntekt siste 12 mnd`() {

        val inntektsListe = (1..30).toList().map {
            KlassifisertInntektMåned(
                YearMonth.now().minusMonths(it.toLong()),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(14500),
                        InntektKlasse.ARBEIDSINNTEKT)
                ))
        }

        val resultat = finnUavkortetGrunnlag(
            false,
            Inntekt("123", inntektsListe),
            YearMonth.now().minusMonths(1),
            false)
        assertEquals(BigDecimal(174000), resultat)
    }

    @Test
    fun `Skal få riktig grunnlag med næringsinntekt siste 12 mnd`() {

        val inntektsListe = (1..30).toList().map {
            KlassifisertInntektMåned(
                YearMonth.now().minusMonths(it.toLong()),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(14500),
                        InntektKlasse.FANGST_FISKE)))
        }

        val resultat = finnUavkortetGrunnlag(
            false,
            Inntekt("123", inntektsListe),
            YearMonth.now().minusMonths(1),
            true)
        assertEquals(BigDecimal(174000), resultat)
    }*/
}
package no.nav.dagpenger.regel.grunnlag

import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import kotlin.test.assertEquals

class BruttoArbeidsinntektDeSisteTolvAvsluttedeKalendermånedeneBeregningsTest {

    @Test
    fun ` Skal gi grunnlag på 174000 siste 12 kalendermåned gitt mars 2019 inntekt`(){

        val inntektsListe = listOf (
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

        val fakta = Fakta(Inntekt("123", inntektsListe), YearMonth.of(2019, 3), false, false, LocalDate.of(2019, 4,1))

        assertEquals(BigDecimal(2034.69), BruttoArbeidsinntektDeSisteTolvAvsluttedeKalendermånedene(fakta).resultat)
    }


}
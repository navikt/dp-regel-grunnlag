package no.nav.dagpenger.regel.grunnlag.beregning

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.regel.grunnlag.Fakta
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.YearMonth

class DagpengerEtterAvtjentVernepliktBeregningsTest {

    private val beregning = DagpengerEtterAvtjentVerneplikt()

    @Test
    fun `Skal ikke behandle hvis ikke verneplikt parameter er satt`() {
        val fakta = Fakta(
            inntekt = null,
            fangstOgFiske = false,
            lærling = true,
            verneplikt = false,
            beregningsdato = LocalDate.of(2019, 4, 1),
        )
        false shouldBe beregning.isActive(fakta)
    }

    @Test
    fun `Skal få uavkortet grunnlag på 3G når verneplikt er satt`() {
        val fakta = Fakta(
            inntekt = Inntekt("123", emptyList(), sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 3)),
            verneplikt = true,
            fangstOgFiske = false,
            beregningsdato = LocalDate.of(2019, 5, 1),
        )

        when (val beregningsResultat = beregning.calculate(fakta)) {
            is BeregningsResultat ->
                beregningsResultat.uavkortet shouldBe 299574.toBigDecimal()
            else -> beregningsResultat.shouldBeTypeOf<BeregningsResultat>()
        }
    }

    @Test
    fun `Skal få ingenBeregningsResultat på verneplikt når den ikke er satt`() {
        val fakta = Fakta(
            inntekt = Inntekt("123", emptyList(), sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 3)),
            verneplikt = false,
            fangstOgFiske = false,
            beregningsdato = LocalDate.of(2019, 4, 1),
        )

        when (val beregningsResultat = beregning.calculate(fakta)) {
            is IngenBeregningsResultat ->
                beregningsResultat.beskrivelse shouldBe "Verneplikt"
            else -> beregningsResultat.shouldBeTypeOf<BeregningsResultat>()
        }
    }
}

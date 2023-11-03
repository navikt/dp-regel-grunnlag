package no.nav.dagpenger.regel.grunnlag.beregning

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.regel.grunnlag.Fakta
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

class ForrigeGrunnlagBeregningsTest {

    @Test
    fun ` Skal gi IngenBeregningsRegel når forrige grunnlag er satt til 0 `() {
        val fakta = Fakta(
            Inntekt(
                "123",
                emptyList(),
                sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 3),
            ),
            false,
            false,
            LocalDate.of(2019, 4, 10),
            forrigeGrunnlag = 0,
        )

        when (val beregningsResultat = ForrigeGrunnlagBeregning().calculate(fakta)) {
            is IngenBeregningsResultat ->
                beregningsResultat.beskrivelse shouldBe "ForrigeGrunnlag"
            else -> beregningsResultat.shouldBeTypeOf<IngenBeregningsResultat>()
        }
    }

    @Test
    fun ` Skal gi avkortet grunnlag lik uavkortet grunnlag`() {
        val fakta = Fakta(
            Inntekt(
                "123",
                emptyList(),
                sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 3),
            ),
            false,
            false,
            LocalDate.of(2019, 4, 10),
            forrigeGrunnlag = 50000,
        )

        when (val beregningsResultat = ForrigeGrunnlagBeregning().calculate(fakta)) {
            is BeregningsResultat -> {
                beregningsResultat.uavkortet shouldBe BigDecimal("50000")
                beregningsResultat.avkortet shouldBe BigDecimal("50000")
                beregningsResultat.beregningsregel shouldBe "ForrigeGrunnlag"
                beregningsResultat.harAvkortet shouldBe false
            }

            is IngenBeregningsResultat -> TODO()
        }
    }
}

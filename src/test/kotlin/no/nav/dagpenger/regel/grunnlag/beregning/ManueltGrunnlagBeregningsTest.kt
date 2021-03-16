package no.nav.dagpenger.regel.grunnlag.beregning

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.grunnbelop.Grunnbeløp
import no.nav.dagpenger.regel.grunnlag.Fakta
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

class ManueltGrunnlagBeregningsTest {

    @Test
    fun ` Skal gi avkortet grunnlag lik uavkortet grunnlag når uavkortet er under 6 G `() {
        val fakta = Fakta(
            Inntekt(
                "123",
                emptyList(),
                sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 3)
            ),
            false,
            false,
            LocalDate.of(2019, 4, 10),
            manueltGrunnlag = 50000,
            gjeldendeGrunnbeløpVedBeregningsdato = Grunnbeløp.FastsattI2018,
            gjeldendeGrunnbeløpForRegelverksdato = Grunnbeløp.FastsattI2019
        )

        when (val beregningsResultat = ManueltGrunnlagBeregning().calculate(fakta)) {
            is BeregningsResultat -> {
                beregningsResultat.uavkortet shouldBe BigDecimal("50000")
                beregningsResultat.avkortet shouldBe BigDecimal("50000")
                beregningsResultat.beregningsregel shouldBe "Manuell"
                beregningsResultat.harAvkortet shouldBe false
            }
        }
    }

    @Test
    fun ` Skal gi avkortet grunnlag lik 6G når uavkortet grunnlag er høyere enn 6G `() {
        val fakta = Fakta(
            Inntekt(
                "123",
                emptyList(),
                sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 3)
            ),
            false,
            false,
            LocalDate.of(2019, 4, 10),
            manueltGrunnlag = 600000,
            gjeldendeGrunnbeløpVedBeregningsdato = Grunnbeløp.FastsattI2018,
            gjeldendeGrunnbeløpForRegelverksdato = Grunnbeløp.FastsattI2019
        )

        when (val beregningsResultat = ManueltGrunnlagBeregning().calculate(fakta)) {
            is BeregningsResultat -> {
                beregningsResultat.uavkortet shouldBe BigDecimal("600000")
                beregningsResultat.avkortet shouldBe BigDecimal("581298")
                beregningsResultat.beregningsregel shouldBe "Manuell"
                beregningsResultat.harAvkortet shouldBe true
            }
        }
    }

    @Test
    fun ` Skal gi IngenBeregningsRegel når manuelt grunnlag er satt til 0 `() {
        val fakta = Fakta(
            Inntekt(
                "123",
                emptyList(),
                sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 3)
            ),
            false,
            false,
            LocalDate.of(2019, 4, 10),
            manueltGrunnlag = 0,
            gjeldendeGrunnbeløpVedBeregningsdato = Grunnbeløp.FastsattI2019,
            gjeldendeGrunnbeløpForRegelverksdato = Grunnbeløp.FastsattI2019
        )

        when (val beregningsResultat = ManueltGrunnlagBeregning().calculate(fakta)) {
            is IngenBeregningsResultat ->
                beregningsResultat.beskrivelse shouldBe "Manuell"
            else -> beregningsResultat.shouldBeTypeOf<IngenBeregningsResultat>()
        }
    }
}

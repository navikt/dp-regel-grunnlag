package no.nav.dagpenger.regel.grunnlag.beregning

import no.nav.dagpenger.inntekt.v1.InntektKlasse
import no.nav.dagpenger.regel.grunnlag.Fakta
import java.math.BigDecimal
import java.util.EnumSet

abstract class TolvMånedersBeregning(
    private val inntektKlasser: EnumSet<InntektKlasse>,
    beregningsregel: String,
) : GrunnlagBeregning(beregningsregel) {
    override fun isActive(fakta: Fakta): Boolean = !(fakta.lærling && fakta.beregningsdato.erKoronaPeriode())

    override fun calculate(fakta: Fakta): Resultat {
        val uavkortet = fakta.oppjusterteInntekterFørstePeriode(inntektKlasser)

        val seksGangerGrunnbeløp = fakta.grunnbeløpVedBeregningsdato().verdi.multiply(BigDecimal(6))

        val avkortet = if (uavkortet > seksGangerGrunnbeløp) seksGangerGrunnbeløp else uavkortet

        return BeregningsResultat(uavkortet, avkortet, beregningsregel)
    }
}

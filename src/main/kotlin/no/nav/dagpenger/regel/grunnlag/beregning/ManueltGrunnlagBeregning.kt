package no.nav.dagpenger.regel.grunnlag.beregning

import no.nav.dagpenger.regel.grunnlag.Fakta
import java.math.BigDecimal

class ManueltGrunnlagBeregning : GrunnlagBeregning("Manuell") {
    override fun isActive(fakta: Fakta): Boolean = fakta.manueltGrunnlag != null

    override fun calculate(fakta: Fakta): Resultat {
        val manueltGrunnlag = fakta.manueltGrunnlag ?: 0
        val seksGangerGrunnbeløp = fakta.grunnbeløpVedBeregningsdato().verdi.multiply(BigDecimal(6))

        return if (manueltGrunnlag <= 0) {
            IngenBeregningsResultat(beregningsregel)
        } else if (manueltGrunnlag.toBigDecimal() <= seksGangerGrunnbeløp) {
            BeregningsResultat(manueltGrunnlag.toBigDecimal(), manueltGrunnlag.toBigDecimal(), beregningsregel)
        } else {
            BeregningsResultat(manueltGrunnlag.toBigDecimal(), seksGangerGrunnbeløp, beregningsregel)
        }
    }
}

internal fun isManuellBeregningsRegel(beregningsregel: String) = beregningsregel == "Manuell"

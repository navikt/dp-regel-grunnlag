package no.nav.dagpenger.regel.grunnlag.beregning

import no.nav.dagpenger.regel.grunnlag.Fakta
import java.math.BigDecimal

class ManueltGrunnlagBeregning : GrunnlagBeregning("Manuell") {
    override fun calculate(fakta: Fakta): Resultat {

        val manueltGrunnlag = fakta.manueltGrunnlag ?: 0
        val seksGangerGrunnbeløp = fakta.gjeldendeGrunnbeløp.verdi.multiply(BigDecimal(6))

        if (manueltGrunnlag <= 0) {
            return BeregningsResultat(Integer.MIN_VALUE.toBigDecimal(), Integer.MIN_VALUE.toBigDecimal(), beregningsregel)
        } else if (manueltGrunnlag.toBigDecimal() <= seksGangerGrunnbeløp) {
            return BeregningsResultat(manueltGrunnlag.toBigDecimal(), manueltGrunnlag.toBigDecimal(), beregningsregel)
        } else {
            return BeregningsResultat(manueltGrunnlag.toBigDecimal(), seksGangerGrunnbeløp, beregningsregel)
        }
    }
}

internal fun isManuellBeregningsRegel(beregningsregel: String) = beregningsregel == "Manuell"

package no.nav.dagpenger.regel.grunnlag.beregning

import no.nav.dagpenger.regel.grunnlag.Fakta
import java.math.BigDecimal

val MANUELL_UNDER_6G = "Manuell under 6G"
val MANUELL_OVER_6G = "Manuell over 6G"

class ManueltGrunnlagBeregning : GrunnlagBeregning("Manuell") {
    override fun calculate(fakta: Fakta): BeregningsResultat {

        val manueltGrunnlag = fakta.manueltGrunnlag ?: 0
        val seksGangerGrunnbeløp = fakta.gjeldendeGrunnbeløp.verdi.multiply(BigDecimal(6))

        if (manueltGrunnlag.toBigDecimal() <= seksGangerGrunnbeløp) {
            return BeregningsResultat(manueltGrunnlag.toBigDecimal(), manueltGrunnlag.toBigDecimal(), MANUELL_UNDER_6G)
        } else {
            return BeregningsResultat(manueltGrunnlag.toBigDecimal(), seksGangerGrunnbeløp, MANUELL_OVER_6G)
        }
    }
}

internal fun isManuellBeregningsRegel(beregningsregel: String) = beregningsregel == MANUELL_UNDER_6G || beregningsregel == MANUELL_OVER_6G

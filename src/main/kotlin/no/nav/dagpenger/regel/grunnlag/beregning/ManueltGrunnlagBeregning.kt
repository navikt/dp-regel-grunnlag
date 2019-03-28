package no.nav.dagpenger.regel.grunnlag.beregning

import no.nav.dagpenger.regel.grunnlag.Fakta
import java.math.BigDecimal

class ManueltGrunnlagBeregning : GrunnlagBeregning("Manuell") {
    override fun calculate(fakta: Fakta): BeregningsResultat {

        val manueltGrunnlag = fakta.manueltGrunnlag ?: 0
        val seksGangerGrunnbeløp = fakta.gjeldendeGrunnbeløp.verdi.multiply(BigDecimal(6))

        if (manueltGrunnlag.toBigDecimal() <= seksGangerGrunnbeløp) {
            return BeregningsResultat(manueltGrunnlag.toBigDecimal(), manueltGrunnlag.toBigDecimal(), "Manuell under 6G")
        } else {
            return BeregningsResultat(manueltGrunnlag.toBigDecimal(), seksGangerGrunnbeløp, "Manuell over 6G")
        }
    }
}
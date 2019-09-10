package no.nav.dagpenger.regel.grunnlag.beregning

import no.nav.dagpenger.regel.grunnlag.Fakta
import java.math.BigDecimal

class ManueltGrunnlagBeregning : GrunnlagBeregning("Manuell") {
    override fun calculate(fakta: Fakta): BeregningsResultat {

        val manueltGrunnlag = fakta.manueltGrunnlag ?: 0
        val seksGangerGrunnbeløp = fakta.gjeldendeGrunnbeløpVedBeregningsdato.verdi.multiply(BigDecimal(6))

        if (manueltGrunnlag.toBigDecimal() <= seksGangerGrunnbeløp) {
            return BeregningsResultat(manueltGrunnlag.toBigDecimal(), manueltGrunnlag.toBigDecimal(), beregningsregel)
        } else {
            return BeregningsResultat(manueltGrunnlag.toBigDecimal(), seksGangerGrunnbeløp, beregningsregel)
        }
    }
}

internal fun isManuellBeregningsRegel(beregningsregel: String) = beregningsregel == "Manuell"

package no.nav.dagpenger.regel.grunnlag.beregning

import no.nav.dagpenger.regel.grunnlag.Fakta

class ForrigeGrunnlagBeregning : GrunnlagBeregning("ForrigeGrunnlag") {
    override fun isActive(fakta: Fakta): Boolean = fakta.forrigeGrunnlag != null

    override fun calculate(fakta: Fakta): Resultat {
        val forrigeGrunnlag = fakta.forrigeGrunnlag ?: 0

        return if (forrigeGrunnlag <= 0) {
            IngenBeregningsResultat(beregningsregel)
        } else {
            BeregningsResultat(forrigeGrunnlag.toBigDecimal(), forrigeGrunnlag.toBigDecimal(), beregningsregel)
        }
    }
}

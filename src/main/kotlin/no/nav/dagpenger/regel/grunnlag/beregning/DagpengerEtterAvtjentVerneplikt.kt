package no.nav.dagpenger.regel.grunnlag.beregning

import no.nav.dagpenger.regel.grunnlag.Fakta

class DagpengerEtterAvtjentVerneplikt : GrunnlagBeregning("Verneplikt") {
    override fun isActive(fakta: Fakta): Boolean = fakta.verneplikt

    override fun calculate(fakta: Fakta): Resultat {
        return if (isActive(fakta)) {
            val vernepliktGrunnlag = fakta.gjeldendeGrunnbeløpForDagensDato.verdi * 3.toBigDecimal()
            BeregningsResultat(
                vernepliktGrunnlag,
                vernepliktGrunnlag,
                "Verneplikt"
            )
        } else {
            IngenBeregningsResultat("Verneplikt")
        }
    }
}

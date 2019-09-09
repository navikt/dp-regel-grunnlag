package no.nav.dagpenger.regel.grunnlag.beregning

import no.nav.dagpenger.regel.grunnlag.Fakta

class DagpengerEtterAvtjentVerneplikt() : GrunnlagBeregning("Verneplikt") {

    override fun calculate(fakta: Fakta): Resultat {
        return if (fakta.verneplikt) {
            val vernepliktGrunnlag = fakta.gjeldendeGrunnbeløp.verdi * 3.toBigDecimal()

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

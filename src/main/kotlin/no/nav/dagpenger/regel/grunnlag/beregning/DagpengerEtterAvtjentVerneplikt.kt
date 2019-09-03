package no.nav.dagpenger.regel.grunnlag.beregning

import no.nav.dagpenger.regel.grunnlag.Fakta

class DagpengerEtterAvtjentVerneplikt() : GrunnlagBeregning("Verneplikt") {

    override fun calculate(fakta: Fakta): BeregningsResultat {
        if (fakta.verneplikt) {
            val vernepliktGrunnlag = fakta.gjeldendeGrunnbeløp.verdi * 3.toBigDecimal()

            return BeregningsResultat(
                vernepliktGrunnlag,
                vernepliktGrunnlag,
                "Verneplikt"
                )
        } else {
            return BeregningsResultat(0.toBigDecimal(), 0.toBigDecimal(), "Verneplikt")
        }
    }
}

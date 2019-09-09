package no.nav.dagpenger.regel.grunnlag.beregning

import no.nav.dagpenger.regel.grunnlag.Fakta

class DagpengerEtterAvtjentVerneplikt() : GrunnlagBeregning("Verneplikt") {

    override fun calculate(fakta: Fakta): BeregningsResultat {
        return if (fakta.verneplikt) {
            val vernepliktGrunnlag = fakta.gjeldendeGrunnbeløpForDagensDato.verdi * 3.toBigDecimal()

            BeregningsResultat(
                vernepliktGrunnlag,
                vernepliktGrunnlag,
                "Verneplikt"
            )
        } else {
            BeregningsResultat(0.toBigDecimal(), 0.toBigDecimal(), "Verneplikt")
        }
    }
}

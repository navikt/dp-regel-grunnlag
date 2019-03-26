package no.nav.dagpenger.regel.grunnlag.beregning

import no.nav.dagpenger.regel.grunnlag.Fakta
import no.nav.dagpenger.regel.grunnlag.getGrunnbeløpForMåned
import java.time.YearMonth

class DagpengerEtterAvtjentVerneplikt() : GrunnlagBeregning("Verneplikt") {

    override fun calculate(fakta: Fakta): BeregningsResultat {
        if (fakta.verneplikt) {
            val vernepliktGrunnlag = getGrunnbeløpForMåned(YearMonth.from(fakta.beregningsdato)).verdi * 3.toBigDecimal()

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

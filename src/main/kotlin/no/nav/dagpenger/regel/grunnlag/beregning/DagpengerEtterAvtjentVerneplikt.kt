package no.nav.dagpenger.regel.grunnlag.beregning

import no.nav.dagpenger.regel.grunnlag.Fakta
import no.nav.dagpenger.regel.grunnlag.getGrunnbeløpForMåned
import java.math.BigDecimal
import java.time.YearMonth

class DagpengerEtterAvtjentVerneplikt() : GrunnlagBeregning() {

    override fun calculate(fakta: Fakta): BigDecimal {
        return if (fakta.verneplikt) getGrunnbeløpForMåned(YearMonth.from(fakta.beregningsdato)).verdi * 3.toBigDecimal() else 0.toBigDecimal()
    }
}

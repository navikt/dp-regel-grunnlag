package no.nav.dagpenger.regel.grunnlag

import java.math.BigDecimal
import java.time.YearMonth

class DagpengerEtterAvtjentVerneplikt() : Beregning() {

    override fun calculate(fakta: Fakta): BigDecimal {
        return if (fakta.verneplikt) getGrunnbeløpForMåned(YearMonth.from(fakta.beregningsdato)).verdi * 3.toBigDecimal() else 0.toBigDecimal()
    }
}

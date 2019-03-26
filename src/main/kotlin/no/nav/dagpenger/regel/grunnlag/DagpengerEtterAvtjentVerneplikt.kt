package no.nav.dagpenger.regel.grunnlag

import java.math.BigDecimal
import java.time.YearMonth

class DagpengerEtterAvtjentVerneplikt(fakta: Fakta) {

    val resultat: BigDecimal = if(fakta.verneplikt)
        getGrunnbeløpForMåned(YearMonth.from(fakta.beregningsdato)).verdi * 3.toBigDecimal() else 0.toBigDecimal()
}

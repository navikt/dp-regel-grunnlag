package no.nav.dagpenger.regel.grunnlag

import java.math.BigDecimal

class InntektsOppjusteringsBeregning(inntekt: BigDecimal, gjeldendeG: BigDecimal, månedsG: BigDecimal) {

    val resultat: BigDecimal = inntekt * (gjeldendeG / månedsG)

}

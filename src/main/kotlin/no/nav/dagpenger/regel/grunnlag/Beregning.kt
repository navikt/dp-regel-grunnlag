package no.nav.dagpenger.regel.grunnlag

import java.math.BigDecimal

abstract class Beregning {

    abstract fun calculate(fakta: Fakta): BigDecimal
}
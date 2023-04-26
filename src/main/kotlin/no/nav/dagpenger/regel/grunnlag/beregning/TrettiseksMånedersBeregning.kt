package no.nav.dagpenger.regel.grunnlag.beregning

import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.regel.grunnlag.Fakta
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.util.EnumSet

abstract class TrettiseksMånedersBeregning(
    val inntektKlasser: EnumSet<InntektKlasse>,
    beregningsregel: String,
) :
    GrunnlagBeregning(beregningsregel) {
    private val antallDesimaler = 20
    private val roundingMode = RoundingMode.HALF_UP
    private val grensedato = LocalDate.of(2021, 12, 17) // ifbm feilsaker om avkorting mot 6G må vi ha to praksiser for beregning for å rydde opp
    private lateinit var seksGangerGrunnbeløp: BigDecimal

    override fun isActive(fakta: Fakta): Boolean = !(fakta.lærling && fakta.beregningsdato.erKoronaPeriode())

    override fun calculate(fakta: Fakta): Resultat {
        seksGangerGrunnbeløp = fakta.grunnbeløpVedBeregningsdato().verdi.multiply(BigDecimal(6))

        val inntekter = Triple(
            fakta.oppjusterteInntekterFørstePeriode(inntektKlasser),
            fakta.oppjusterteInntekterAndrePeriode(inntektKlasser),
            fakta.oppjusterteInntekterTredjePeriode(inntektKlasser)
        )

        val uavkortet = inntekter.toList().sumOf { it }.divide(
            3.toBigDecimal(),
            antallDesimaler,
            roundingMode
        )

        val avkortet = if (fakta.beregningsdato.isBefore(grensedato)) {
            avkortetFør17_12_21(uavkortet)
        } else {
            avkortetEtter17_12_21(inntekter)
        }

        return BeregningsResultat(uavkortet, avkortet, beregningsregel)
    }

    private fun avkortetFør17_12_21(uavkortet: BigDecimal) =
        if (uavkortet > seksGangerGrunnbeløp) seksGangerGrunnbeløp else uavkortet

    private fun avkortetEtter17_12_21(
        inntekter: Triple<BigDecimal, BigDecimal, BigDecimal>,
    ): BigDecimal {

        return inntekter.toList().sumOf { inntekt ->
            if (inntekt > seksGangerGrunnbeløp) seksGangerGrunnbeløp else inntekt
        }.divide(
            3.toBigDecimal(),
            antallDesimaler,
            RoundingMode.HALF_UP
        )
    }
}

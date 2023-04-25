package no.nav.dagpenger.regel.grunnlag.beregning

import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.regel.grunnlag.Fakta
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.util.EnumSet

abstract class TrettiseksMånedersBeregning(
    val inntektKlasser: EnumSet<InntektKlasse>,
    beregningsregel: String
) :
    GrunnlagBeregning(beregningsregel) {
    private val antallDesimaler = 20
    private val roundingMode = RoundingMode.HALF_UP

    private fun avkortetFør17_12_21(fakta: Fakta): Resultat {
        val uavkortet = uavkortet(fakta)

        val seksGangerGrunnbeløp = fakta.grunnbeløpVedBeregningsdato().verdi.multiply(BigDecimal(6))
        val avkortet = if (uavkortet > seksGangerGrunnbeløp) seksGangerGrunnbeløp else uavkortet

        return BeregningsResultat(uavkortet, avkortet, beregningsregel)
    }

    private fun avkortetEtter17_12_21(fakta: Fakta): Resultat {

        val uavkortetFørstePeriode = fakta.oppjusterteInntekterFørstePeriode(inntektKlasser)
        val uavkortetAndrePeriode = fakta.oppjusterteInntekterAndrePeriode(inntektKlasser)
        val uavkortetTredjePeriode = fakta.oppjusterteInntekterTredjePeriode(inntektKlasser)
        val uavkortet = uavkortet(fakta)

        val seksGangerGrunnbeløp = fakta.grunnbeløpVedBeregningsdato().verdi.multiply(BigDecimal(6))

        val avkortetFørste = if (uavkortetFørstePeriode > seksGangerGrunnbeløp) seksGangerGrunnbeløp else uavkortetFørstePeriode

        val avkortetAndre = if (uavkortetAndrePeriode > seksGangerGrunnbeløp) seksGangerGrunnbeløp else uavkortetAndrePeriode

        val avkortetTredje = if (uavkortetTredjePeriode > seksGangerGrunnbeløp) seksGangerGrunnbeløp else uavkortetTredjePeriode

        val avkortet = (avkortetFørste + avkortetAndre + avkortetTredje).divide(3.toBigDecimal(), antallDesimaler, RoundingMode.HALF_UP)

        return BeregningsResultat(uavkortet, avkortet, beregningsregel)
    }

    private fun uavkortet(fakta: Fakta): BigDecimal {
        val uavkortetFørstePeriode = fakta.oppjusterteInntekterFørstePeriode(inntektKlasser)
        val uavkortetAndrePeriode = fakta.oppjusterteInntekterAndrePeriode(inntektKlasser)
        val uavkortetTredjePeriode = fakta.oppjusterteInntekterTredjePeriode(inntektKlasser)

        return (uavkortetFørstePeriode + uavkortetAndrePeriode + uavkortetTredjePeriode).divide(3.toBigDecimal(), antallDesimaler, roundingMode)
    }

    override fun isActive(fakta: Fakta): Boolean = !(fakta.lærling && fakta.beregningsdato.erKoronaPeriode())

    override fun calculate(fakta: Fakta): Resultat {

        return if (fakta.beregningsdato.isBefore(LocalDate.of(2021, 12, 17))) {
            avkortetFør17_12_21(fakta)
        } else {
            avkortetEtter17_12_21(fakta)
        }
    }
}

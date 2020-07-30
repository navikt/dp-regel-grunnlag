package no.nav.dagpenger.regel.grunnlag.beregning

import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.regel.grunnlag.Fakta
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.EnumSet

abstract class TrettiseksMånedersBeregning(
    val inntektKlasser: EnumSet<InntektKlasse>,
    beregningsregel: String
) :
    GrunnlagBeregning(beregningsregel) {

    override fun isActive(fakta: Fakta): Boolean = !(fakta.lærling && fakta.beregningsdato.erKoronaPeriode())

    override fun calculate(fakta: Fakta): Resultat {

        val uavkortetFørstePeriode = fakta.oppjusterteInntekterFørstePeriode(inntektKlasser)
        val uavkortetAndrePeriode = fakta.oppjusterteInntekterAndrePeriode(inntektKlasser)
        val uavkortetTredjePeriode = fakta.oppjusterteInntekterTredjePeriode(inntektKlasser)
        val antallDesimaler = 20

        val uavkortet = (uavkortetFørstePeriode + uavkortetAndrePeriode + uavkortetTredjePeriode).divide(3.toBigDecimal(), antallDesimaler, RoundingMode.HALF_UP)

        val seksGangerGrunnbeløp = fakta.gjeldendeGrunnbeløpVedBeregningsdato.verdi.multiply(BigDecimal(6))

        val avkortetFørste = if (uavkortetFørstePeriode > seksGangerGrunnbeløp) seksGangerGrunnbeløp else uavkortetFørstePeriode

        val avkortetAndre = if (uavkortetAndrePeriode > seksGangerGrunnbeløp) seksGangerGrunnbeløp else uavkortetAndrePeriode

        val avkortetTredje = if (uavkortetTredjePeriode > seksGangerGrunnbeløp) seksGangerGrunnbeløp else uavkortetTredjePeriode

        val avkortet = (avkortetFørste + avkortetAndre + avkortetTredje).divide(3.toBigDecimal(), antallDesimaler, RoundingMode.HALF_UP)

        return BeregningsResultat(uavkortet, avkortet, beregningsregel)
    }
}

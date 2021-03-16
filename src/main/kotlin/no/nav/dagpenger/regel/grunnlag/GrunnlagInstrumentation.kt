package no.nav.dagpenger.regel.grunnlag

import io.prometheus.client.Counter
import java.math.BigDecimal

class GrunnlagInstrumentation {
    private val regelBrukt = Counter.build()
        .namespace("dagpenger")
        .name("grunnlag_regel_brukt")
        .help("Antall ganger regel")
        .labelNames(
            "regelIdentifikator",
            "beregningsregel",
            "harAvkortet"
        )
        .register()

    private val fastsattArbeidstid = Counter.build()
        .namespace("dagpenger")
        .name("fastsatt_arbeidstid")
        .help("Grupper arbeidstid")
        .labelNames(
            "regelIdentifikator",
            "beregningsregel",
            "harAvkortet",
            "arbeidstid"
        )
        .register()

    fun grunnlagBeregnet(
        regelIdentifikator: String,
        fakta: Fakta,
        resultat: GrunnlagResultat
    ) {
        this.countGrunnlagBeregnet(
            regelIdentifikator = regelIdentifikator,
            beregningsregel = resultat.beregningsregel,
            harAvkortet = resultat.harAvkortet
        )

        this.beregnNormaltFastsattArbeidstid(
            regelIdentifikator = regelIdentifikator,
            beregningsregel = resultat.beregningsregel,
            harAvkortet = resultat.harAvkortet,
            klassifisering = fastsettArbeidstid(resultat.avkortetGrunnlag, fakta.gjeldendeGrunnbeløpForRegelverksdato.verdi)
        )
    }

    fun grunnlagBeregnet(
        regelIdentifikator: String,
        fakta: Fakta,
        resultat: RapidGrunnlagResultat
    ) {
        this.countGrunnlagBeregnet(
            regelIdentifikator = regelIdentifikator,
            beregningsregel = resultat.beregningsregel,
            harAvkortet = resultat.harAvkortet
        )

        this.beregnNormaltFastsattArbeidstid(
            regelIdentifikator = regelIdentifikator,
            beregningsregel = resultat.beregningsregel,
            harAvkortet = resultat.harAvkortet,
            klassifisering = fastsettArbeidstid(resultat.avkortet, fakta.gjeldendeGrunnbeløpForRegelverksdato.verdi)
        )
    }

    private fun countGrunnlagBeregnet(
        regelIdentifikator: String,
        beregningsregel: String,
        harAvkortet: Boolean
    ) {
        regelBrukt.labels(
            regelIdentifikator,
            beregningsregel,
            harAvkortet.toString()
        ).inc()
    }

    private fun beregnNormaltFastsattArbeidstid(
        regelIdentifikator: String,
        beregningsregel: String,
        harAvkortet: Boolean,
        klassifisering: String
    ) {
        fastsattArbeidstid.labels(
            regelIdentifikator,
            beregningsregel,
            harAvkortet.toString(),
            klassifisering
        ).inc()
    }

    private fun fastsettArbeidstid(grunnlag: BigDecimal, grunnbelop: BigDecimal): String = when (grunnlag) {
        in BigDecimal(0)..grunnbelop -> "Null"
        in grunnbelop..grunnbelop.times(BigDecimal(3)) -> "Middels"
        else -> "Maks"
    }
}

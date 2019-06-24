package no.nav.dagpenger.regel.grunnlag

import io.prometheus.client.Counter

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

    fun grunnlagBeregnet(
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
}

package no.nav.dagpenger.regel.grunnlag

import tools.jackson.databind.introspect.DefaultAccessorNamingStrategy
import tools.jackson.module.kotlin.jacksonMapperBuilder

internal val objectMapper =
    jacksonMapperBuilder().accessorNaming(DefaultAccessorNamingStrategy.Provider().withFirstCharAcceptance(true, true)).build()

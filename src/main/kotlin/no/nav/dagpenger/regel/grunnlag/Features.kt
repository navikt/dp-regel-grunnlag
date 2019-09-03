package no.nav.dagpenger.regel.grunnlag

class Features(private val features: Map<String, Boolean>) {
    fun isEnabled(feature: String): Boolean {
        return features.getOrDefault(feature, false)
    }
}
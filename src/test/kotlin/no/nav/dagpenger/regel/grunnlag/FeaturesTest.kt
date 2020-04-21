package no.nav.dagpenger.regel.grunnlag

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class FeaturesTest {
    @Test
    fun `non-existing feature flags should return false`() {
        var features = Features(emptyMap())
        features.isEnabled("FEATURE_FOO") shouldBe false
    }

    @Test
    fun `disabled feature flags should return false`() {
        var features = Features(mapOf("FEATURE_FOO" to false))
        features.isEnabled("FEATURE_FOO") shouldBe false
    }

    @Test
    fun `enabled feature flags should return true`() {
        var features = Features(mapOf("FEATURE_FOO" to true))
        features.isEnabled("FEATURE_FOO") shouldBe true
    }
}

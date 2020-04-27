package no.nav.dagpenger.regel.grunnlag

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import no.finn.unleash.FakeUnleash
import no.nav.dagpenger.regel.grunnlag.Grunnlag.Companion.BEREGNINGSDATO
import no.nav.dagpenger.regel.sats.LøsningService.Companion.ANTALL_BARN
import no.nav.dagpenger.regel.sats.LøsningService.Companion.AVKORTET_GRUNNLAG
import no.nav.dagpenger.regel.sats.LøsningService.Companion.BEREGNINGSDATO
import no.nav.dagpenger.regel.sats.LøsningService.Companion.SATS
import no.nav.helse.rapids_rivers.InMemoryRapid
import no.nav.helse.rapids_rivers.inMemoryRapid
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ApplicationTopologyTest {
    private val instrumentation = mockk<GrunnlagInstrumentation>(relaxed = true)
    private lateinit var rapid: InMemoryRapid

    @BeforeEach
    fun setUp() {
        rapid = createRapid {
            LøsningService(it, unleash = FakeUnleash(), instrumentation = instrumentation)
        }
    }

    @Test
    fun `skal fastsette både dag- og ukessats`() {
        rapid.sendToListeners(
            """
            {
                "@behov": ["$GRUNNLAG"],
                "@id": "32",
                "aktørId": "123",
                "$BEREGNINGSDATO": "2020-01-01"
            }
            """.trimIndent()
        )

        validateMessages(rapid) { messages ->
            messages.size shouldBeExactly 1

            messages.first().also { message ->
                message["@behov"].map(JsonNode::asText) shouldContain GRUNNLAG
                message["@løsning"].hasNonNull(GRUNNLAG)
                message["@løsning"][SATS]["dagSats"].asInt() shouldBe 295
                message["@løsning"][SATS]["ukeSats"].asInt() shouldBe 1900
                message["@løsning"][SATS]["benyttet90ProsentRegel"].asBoolean() shouldBe false
            }
        }
    }

    private fun createRapid(service: (InMemoryRapid) -> Any): InMemoryRapid {
        return inMemoryRapid { }.also { service(it) }
    }

    private fun validateMessages(rapid: InMemoryRapid, assertions: (messages: List<JsonNode>) -> Any) {
        rapid.outgoingMessages.map { jacksonObjectMapper().readTree(it.value) }.also { assertions(it) }
    }
}

package no.nav.dagpenger.regel.grunnlag

import io.mockk.mockk
import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import no.nav.dagpenger.events.moshiInstance
import no.nav.dagpenger.regel.grunnlag.Grunnlag.Companion.inntektAdapter
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.TestInputTopic
import org.apache.kafka.streams.TestOutputTopic
import org.apache.kafka.streams.TopologyTestDriver
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.net.URI
import java.time.YearMonth
import java.util.Properties
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private val jsonMapAdapter = moshiInstance.adapter(Map::class.java)

class GrunnlagTopologyTest {
    companion object {

        val config = Properties().apply {
            this[StreamsConfig.APPLICATION_ID_CONFIG] = "test"
            this[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = "dummy:1234"
        }

        val fakeGrunnlagInstrumentation = mockk<GrunnlagInstrumentation>()

        val grunnlag = Grunnlag(
            Configuration(),
            fakeGrunnlagInstrumentation
        )
    }

    @Test
    fun ` Should add not process behov without inntekt eller manuelt grunnlag`() {
        val json =
            """
            {
                "beregningsDato": "2019-05-20"
            }
            """.trimIndent()

        TopologyTestDriver(grunnlag.buildTopology(), config).use { topologyTestDriver ->
            topologyTestDriver.behovInputTopic().also { it.pipeInput(Packet(json)) }
            assertTrue { topologyTestDriver.behovOutputTopic().isEmpty }
        }
    }

    @Test
    fun ` Should add not process behov without beregningsDato `() {

        val json =
            """
            {
                "manueltGrunnlag":50000
            }
            """.trimIndent()

        TopologyTestDriver(grunnlag.buildTopology(), config).use { topologyTestDriver ->
            topologyTestDriver.behovInputTopic().also { it.pipeInput(Packet(json)) }
            assertTrue { topologyTestDriver.behovOutputTopic().isEmpty }
        }
    }

    @Test
    fun ` Should add GrunnlagSubsumsjon to subsumsjonsBehov with inntekt `() {
        val inntekt = Inntekt(
            inntektsId = "12345",
            inntektsListe = listOf(
                KlassifisertInntektMåned(
                    årMåned = YearMonth.of(2019, 2),
                    klassifiserteInntekter = listOf(
                        KlassifisertInntekt(
                            beløp = BigDecimal(25000),
                            inntektKlasse = InntektKlasse.ARBEIDSINNTEKT
                        )
                    )

                )
            ),
            sisteAvsluttendeKalenderMåned = YearMonth.of(2018, 3)
        )

        val json =
            """
            {
                "beregningsDato":"2018-04-06",
                "harAvtjentVerneplikt": true
            }
            """.trimIndent()

        val packet = Packet(json)
        packet.putValue("inntektV1", inntektAdapter.toJsonValue(inntekt)!!)

        TopologyTestDriver(grunnlag.buildTopology(), config).use { topologyTestDriver ->
            topologyTestDriver.behovInputTopic().also { it.pipeInput(packet) }
            val resultPacket = topologyTestDriver.behovOutputTopic().readValue()
            assertTrue { resultPacket.hasField("grunnlagResultat") }
            assertTrue { resultPacket.hasField("grunnlagInntektsPerioder") }
        }
    }

    @Test
    fun ` Should add GrunnlagSubsumsjon to subsumsjonsBehov with manueltGrunnlag `() {

        val json =
            """
            {
                "beregningsDato":"2018-04-06",
                "harAvtjentVerneplikt": true,
                "manueltGrunnlag":50000
            }
            """.trimIndent()

        TopologyTestDriver(grunnlag.buildTopology(), config).use { topologyTestDriver ->
            topologyTestDriver.behovInputTopic().also { it.pipeInput(Packet(json)) }
            val resultPacket = topologyTestDriver.behovOutputTopic().readValue()

            assertTrue { resultPacket.hasField("grunnlagResultat") }
            assertEquals(50000, Integer.parseInt(resultPacket.getMapValue("grunnlagResultat")["avkortet"].toString()))
            assertFalse { resultPacket.hasField("inntektV1") }
            assertFalse { resultPacket.hasField("grunnlagInntektsPerioder") }
        }
    }

    @Test
    fun ` Should add GrunnlagSubsumsjon to subsumsjonsBehov with oppfyllerKravTilFangstOgFisk `() {

        val inntekt: Inntekt = Inntekt(
            inntektsId = "12345",
            inntektsListe = listOf(
                KlassifisertInntektMåned(
                    årMåned = YearMonth.of(2018, 2),
                    klassifiserteInntekter = listOf(
                        KlassifisertInntekt(
                            beløp = BigDecimal(99999),
                            inntektKlasse = InntektKlasse.FANGST_FISKE
                        )
                    )

                )
            ),
            sisteAvsluttendeKalenderMåned = YearMonth.of(2018, 3)
        )

        val json =
            """
            {
                "beregningsDato":"2018-04-06",
                "harAvtjentVerneplikt": false,
                "oppfyllerKravTilFangstOgFisk": true
            }
            """.trimIndent()

        val packet = Packet(json)
        packet.putValue("inntektV1", inntektAdapter.toJsonValue(inntekt)!!)

        TopologyTestDriver(grunnlag.buildTopology(), config).use { topologyTestDriver ->
            topologyTestDriver.behovInputTopic().also { it.pipeInput(packet) }
            val resultPacket = topologyTestDriver.behovOutputTopic().readValue()
            assertTrue { resultPacket.hasField("grunnlagResultat") }
            val grunnlagresultat = resultPacket.toJson()?.let { jsonMapAdapter.fromJson(it) }?.get("grunnlagResultat") as Map<*, *>
            assertEquals("99999", grunnlagresultat["avkortet"])
        }
    }

    @Test
    fun ` Should add problem on failure`() {
        val json =
            """
            {
                "beregningsDato": "2019-05-20"
            }
            """.trimIndent()

        val packet = Packet(json)
        packet.putValue("inntektV1", "ERROR")

        TopologyTestDriver(grunnlag.buildTopology(), config).use { topologyTestDriver ->
            topologyTestDriver.behovInputTopic().also { it.pipeInput(packet) }
            val resultPacket = topologyTestDriver.behovOutputTopic().readValue()
            assert(resultPacket.hasProblem())
            Assertions.assertEquals(URI("urn:dp:error:regel"), resultPacket.getProblem()!!.type)
            Assertions.assertEquals(URI("urn:dp:regel:grunnlag"), resultPacket.getProblem()!!.instance)
        }
    }
}

private fun TopologyTestDriver.behovInputTopic(): TestInputTopic<String, Packet> =
    this.createInputTopic(
        REGEL_TOPIC.name,
        REGEL_TOPIC.keySerde.serializer(),
        REGEL_TOPIC.valueSerde.serializer()
    )

private fun TopologyTestDriver.behovOutputTopic(): TestOutputTopic<String, Packet> =
    this.createOutputTopic(
        REGEL_TOPIC.name,
        REGEL_TOPIC.keySerde.deserializer(),
        REGEL_TOPIC.valueSerde.deserializer()
    )

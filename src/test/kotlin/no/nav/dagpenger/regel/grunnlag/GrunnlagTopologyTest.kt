package no.nav.dagpenger.regel.grunnlag

import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import no.nav.dagpenger.regel.grunnlag.Grunnlag.Companion.inntektAdapter
import no.nav.dagpenger.streams.Topics.DAGPENGER_BEHOV_PACKET_EVENT
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.TopologyTestDriver
import org.apache.kafka.streams.test.ConsumerRecordFactory
import org.json.JSONObject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.net.URI
import java.time.YearMonth
import java.util.Properties
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GrunnlagTopologyTest {

    companion object {
        val factory = ConsumerRecordFactory<String, Packet>(
            DAGPENGER_BEHOV_PACKET_EVENT.name,
            DAGPENGER_BEHOV_PACKET_EVENT.keySerde.serializer(),
            DAGPENGER_BEHOV_PACKET_EVENT.valueSerde.serializer()
        )

        val config = Properties().apply {
            this[StreamsConfig.APPLICATION_ID_CONFIG] = "test"
            this[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = "dummy:1234"
        }
    }

    @Test
    fun ` Should add not process behov without inntekt eller manuelt grunnlag`() {
        val grunnlag = Grunnlag(
            Environment(
                username = "bogus",
                password = "bogus"
            )
        )
        val json = """
            {
                "beregningsDato": "2019-05-20"
            }
            """.trimIndent()

        TopologyTestDriver(grunnlag.buildTopology(), config).use { topologyTestDriver ->
            val inputRecord = factory.create(Packet(json))
            topologyTestDriver.pipeInput(inputRecord)

            val ut = topologyTestDriver.readOutput(
                DAGPENGER_BEHOV_PACKET_EVENT.name,
                DAGPENGER_BEHOV_PACKET_EVENT.keySerde.deserializer(),
                DAGPENGER_BEHOV_PACKET_EVENT.valueSerde.deserializer()
            )

            assertTrue { null == ut }
        }
    }

    @Test
    fun ` Should add not process behov without beregningsDato `() {
        val grunnlag = Grunnlag(
            Environment(
                username = "bogus",
                password = "bogus"
            )
        )
        val json = """
            {
                "manueltGrunnlag":50000
            }
            """.trimIndent()

        TopologyTestDriver(grunnlag.buildTopology(), config).use { topologyTestDriver ->
            val inputRecord = factory.create(Packet(json))
            topologyTestDriver.pipeInput(inputRecord)

            val ut = topologyTestDriver.readOutput(
                DAGPENGER_BEHOV_PACKET_EVENT.name,
                DAGPENGER_BEHOV_PACKET_EVENT.keySerde.deserializer(),
                DAGPENGER_BEHOV_PACKET_EVENT.valueSerde.deserializer()
            )

            assertTrue { null == ut }
        }
    }

    @Test
    fun ` Should add GrunnlagSubsumsjon to subsumsjonsBehov with inntekt `() {
        val grunnlag = Grunnlag(
            Environment(
                username = "bogus",
                password = "bogus"
            )
        )

        val inntekt: Inntekt = Inntekt(
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

        val json = """
        {
            "beregningsDato":"2018-04-06",
            "harAvtjentVerneplikt": true
            }
            """.trimIndent()

        val packet = Packet(json)
        packet.putValue("inntektV1", inntektAdapter.toJsonValue(inntekt)!!)

        TopologyTestDriver(grunnlag.buildTopology(), config).use { topologyTestDriver ->
            val inputRecord = factory.create(packet)
            topologyTestDriver.pipeInput(inputRecord)

            val ut = topologyTestDriver.readOutput(
                DAGPENGER_BEHOV_PACKET_EVENT.name,
                DAGPENGER_BEHOV_PACKET_EVENT.keySerde.deserializer(),
                DAGPENGER_BEHOV_PACKET_EVENT.valueSerde.deserializer()
            )

            val resultPacket = ut.value()

            assertTrue { resultPacket.hasField("grunnlagResultat") }
            assertTrue { resultPacket.hasField("grunnlagInntektsPerioder") }
        }
    }

    @Test
    fun ` Should add GrunnlagSubsumsjon to subsumsjonsBehov with manueltGrunnlag `() {
        val grunnlag = Grunnlag(
            Environment(
                username = "bogus",
                password = "bogus"
            )
        )

        val json = """
        {
            "beregningsDato":"2018-04-06",
            "harAvtjentVerneplikt": true,
            "manueltGrunnlag":50000
            }
            """.trimIndent()

        val packet = Packet(json)

        TopologyTestDriver(grunnlag.buildTopology(), config).use { topologyTestDriver ->
            val inputRecord = factory.create(packet)
            topologyTestDriver.pipeInput(inputRecord)

            val ut = topologyTestDriver.readOutput(
                DAGPENGER_BEHOV_PACKET_EVENT.name,
                DAGPENGER_BEHOV_PACKET_EVENT.keySerde.deserializer(),
                DAGPENGER_BEHOV_PACKET_EVENT.valueSerde.deserializer()
            )

            val resultPacket = ut.value()

            assertTrue { resultPacket.hasField("grunnlagResultat") }
            assertEquals(50000, Integer.parseInt(resultPacket.getMapValue("grunnlagResultat")["avkortet"].toString()))
            assertFalse { resultPacket.hasField("inntektV1") }
            assertFalse { resultPacket.hasField("grunnlagInntektsPerioder") }
        }
    }

    @Test
    fun ` Should add GrunnlagSubsumsjon to subsumsjonsBehov with oppfyllerKravTilFangstOgFisk `() {
        val grunnlag = Grunnlag(
            Environment(
                username = "bogus",
                password = "bogus"
            )
        )

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

        val json = """
        {
            "beregningsDato":"2018-04-06",
            "harAvtjentVerneplikt": false,
            "oppfyllerKravTilFangstOgFisk": true
            }
            """.trimIndent()

        val packet = Packet(json)
        packet.putValue("inntektV1", inntektAdapter.toJsonValue(inntekt)!!)

        TopologyTestDriver(grunnlag.buildTopology(), config).use { topologyTestDriver ->
            val inputRecord = factory.create(packet)
            topologyTestDriver.pipeInput(inputRecord)

            val ut = topologyTestDriver.readOutput(
                DAGPENGER_BEHOV_PACKET_EVENT.name,
                DAGPENGER_BEHOV_PACKET_EVENT.keySerde.deserializer(),
                DAGPENGER_BEHOV_PACKET_EVENT.valueSerde.deserializer()
            )

            val resultPacket = ut.value()

            assertTrue { resultPacket.hasField("grunnlagResultat") }

            val grunnlagResultat = JSONObject(ut.value().toJson()).getJSONObject("grunnlagResultat").get("avkortet")

            assertNotEquals(0, grunnlagResultat)
        }
    }

    @Test
    fun ` Should add problem on failure`() {
        val grunnlag = Grunnlag(
            Environment(
                username = "bogus",
                password = "bogus"
            )
        )

        val json = """
            {
                "beregningsDato": "2019-05-20"
            }
            """.trimIndent()

        val packet = Packet(json)
        packet.putValue("inntektV1", "ERROR")

        TopologyTestDriver(grunnlag.buildTopology(), config).use { topologyTestDriver ->
            val inputRecord = factory.create(packet)
            topologyTestDriver.pipeInput(inputRecord)

            val ut = topologyTestDriver.readOutput(
                DAGPENGER_BEHOV_PACKET_EVENT.name,
                DAGPENGER_BEHOV_PACKET_EVENT.keySerde.deserializer(),
                DAGPENGER_BEHOV_PACKET_EVENT.valueSerde.deserializer()
            )

            assert(ut.value().hasProblem())
            Assertions.assertEquals(URI("urn:dp:error:regel"), ut.value().getProblem()!!.type)
            Assertions.assertEquals(URI("urn:dp:regel:grunnlag"), ut.value().getProblem()!!.instance)
        }
    }
}

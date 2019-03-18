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
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.YearMonth
import java.util.Properties
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
    fun ` Should add not process behov without inntekt `() {
        val grunnlag = Grunnlag(
            Environment(
                username = "bogus",
                password = "bogus"
            )
        )
        val emptyjsonBehov = """
            {}
            """.trimIndent()

        TopologyTestDriver(grunnlag.buildTopology(), config).use { topologyTestDriver ->
            val inputRecord = factory.create(Packet(emptyjsonBehov))
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
            )
        )

        val json = """
        {
            "senesteInntektsmåned":"2018-03"
            }
            """.trimIndent()

        val packet = Packet(json)
        packet.putValue("inntektV1", inntekt, inntektAdapter::toJson)

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
        }
    }
}

package no.nav.dagpenger.regel.grunnlag

import mu.KotlinLogging
import no.nav.dagpenger.streams.KafkaCredential
import no.nav.dagpenger.streams.Service
import no.nav.dagpenger.streams.Topic
import no.nav.dagpenger.streams.Topics
import no.nav.dagpenger.streams.kbranch
import no.nav.dagpenger.streams.streamConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Produced
import org.json.JSONObject
import java.util.Properties

private val LOGGER = KotlinLogging.logger {}

val dagpengerBehovTopic = Topic(
        Topics.DAGPENGER_BEHOV_EVENT.name,
        Serdes.StringSerde(),
        Serdes.serdeFrom(JsonSerializer(), JsonDeserializer())
)

class Grunnlag(val env: Environment): Service() {
    override val SERVICE_APP_ID: String = "dagpenger-regel-grunnlag"
    override val HTTP_PORT: Int = env.httpPort ?: super.HTTP_PORT

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val service = Grunnlag(Environment())
            service.start()
        }
    }

    override fun setupStreams(): KafkaStreams {
        LOGGER.info { "Initiating start of $SERVICE_APP_ID" }
        return KafkaStreams(buildTopology(), getConfig())
    }

    internal fun buildTopology(): Topology {
        val builder = StreamsBuilder()

        val stream = builder.stream(
                dagpengerBehovTopic.name,
                Consumed.with(dagpengerBehovTopic.keySerde, dagpengerBehovTopic.valueSerde)
        )

        val (needsInntekt, needsSubsumsjon) = stream
                .peek { key, value -> LOGGER.info("Processing ${value.javaClass} with key $key") }
                .mapValues { value: JSONObject -> SubsumsjonsBehov(value) }
                .filter { _, behov -> shouldBeProcessed(behov) }
                .kbranch(
                        { _, behov: SubsumsjonsBehov -> behov.needsHentInntektsTask() },
                        { _, behov: SubsumsjonsBehov -> behov.needsGrunnlagResultat() })

        needsInntekt.mapValues(this::addInntektTask)
        needsSubsumsjon.mapValues(this::addRegelresultat)

        needsInntekt.merge(needsSubsumsjon)
                .peek { key, value -> LOGGER.info("Producing ${value.javaClass} with key $key") }
                .mapValues { _, behov -> behov.jsonObject }
                .to(dagpengerBehovTopic.name, Produced.with(dagpengerBehovTopic.keySerde, dagpengerBehovTopic.valueSerde))

        return builder.build()
    }

    override fun getConfig(): Properties {
        val props = streamConfig(
                appId = SERVICE_APP_ID,
                bootStapServerUrl = env.bootstrapServersUrl,
                credential = KafkaCredential(env.username, env.password)
        )
        return props
    }

    private fun addInntektTask(behov: SubsumsjonsBehov): SubsumsjonsBehov {
        behov.addTask("hentInntekt")

        return behov
    }

    private fun addRegelresultat(behov: SubsumsjonsBehov): SubsumsjonsBehov {
        behov.addGrunnlagResultat(
            SubsumsjonsBehov.GrunnlagResultat(
            "123",
            "456",
            "Grunnlag.v1",
            2000

        ))

        return behov
    }
}

fun shouldBeProcessed(behov: SubsumsjonsBehov): Boolean = behov.needsHentInntektsTask() || behov.needsGrunnlagResultat()

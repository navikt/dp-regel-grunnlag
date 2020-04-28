package no.nav.dagpenger.regel.grunnlag

import com.natpryce.konfig.ConfigurationMap
import com.natpryce.konfig.ConfigurationProperties.Companion.systemProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.booleanType
import com.natpryce.konfig.intType
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType
import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.streams.KafkaCredential
import no.nav.dagpenger.streams.Topic
import no.nav.dagpenger.streams.Topics

private const val TOPIC = "privat-dagpenger-behov-v2"

private val localProperties = ConfigurationMap(
    mapOf(
        "kafka.bootstrap.servers" to "localhost:9092",
        "kafka.topic" to TOPIC,
        "kafka.reset.policy" to "earliest",
        "nav.truststore.path" to "",
        "nav.truststore.password" to "changeme",
        "application.profile" to Profile.LOCAL.toString(),
        "application.httpPort" to "8098",
        "behov.topic" to Topics.DAGPENGER_BEHOV_PACKET_EVENT.name
    )
)
private val devProperties = ConfigurationMap(
    mapOf(
        "kafka.bootstrap.servers" to "b27apvl00045.preprod.local:8443,b27apvl00046.preprod.local:8443,b27apvl00047.preprod.local:8443",
        "kafka.topic" to TOPIC,
        "kafka.reset.policy" to "earliest",
        "application.profile" to Profile.DEV.toString(),
        "application.httpPort" to "8098",
        "feature.gjustering" to false.toString(),
        "behov.topic" to Topics.DAGPENGER_BEHOV_PACKET_EVENT.name
    )
)
private val prodProperties = ConfigurationMap(
    mapOf(
        "kafka.bootstrap.servers" to "a01apvl00145.adeo.no:8443,a01apvl00146.adeo.no:8443,a01apvl00147.adeo.no:8443,a01apvl00148.adeo.no:8443,a01apvl00149.adeo.no:8443,a01apvl00150.adeo.no:8443",
        "kafka.topic" to TOPIC, // Used for Behov v2 / rapids-and-rivers
        "kafka.reset.policy" to "earliest",
        "kafka.bootstrap.servers" to "a01apvl00145.adeo.no:8443,a01apvl00146.adeo.no:8443,a01apvl00147.adeo.no:8443,a01apvl00148.adeo.no:8443,a01apvl00149.adeo.no:8443,a01apvl00150.adeo.no:8443",
        "application.profile" to Profile.PROD.toString(),
        "application.httpPort" to "8098",
        "behov.topic" to Topics.DAGPENGER_BEHOV_PACKET_EVENT.name
    )
)

private fun config() = when (System.getenv("NAIS_CLUSTER_NAME") ?: System.getProperty("NAIS_CLUSTER_NAME")) {
    "dev-fss" -> systemProperties() overriding EnvironmentVariables overriding devProperties
    "prod-fss" -> systemProperties() overriding EnvironmentVariables overriding prodProperties
    else -> {
        systemProperties() overriding EnvironmentVariables overriding localProperties
    }
}

data class Configuration(
    val kafka: Kafka = Kafka(),
    val application: Application = Application(),
    val behovTopic: Topic<String, Packet> = Topics.DAGPENGER_BEHOV_PACKET_EVENT.copy(
        name = config()[Key("behov.topic", stringType)]
    ),
    val rapidApplication: Map<String, String> = mapOf(
        "KAFKA_BOOTSTRAP_SERVERS" to config()[Key("kafka.bootstrap.servers", stringType)],
        "KAFKA_CONSUMER_GROUP_ID" to "dp-regel-sats-rapid",
        "KAFKA_RAPID_TOPIC" to config()[Key("kafka.topic", stringType)],
        "KAFKA_RESET_POLICY" to config()[Key("kafka.reset.policy", stringType)],
        "NAV_TRUSTSTORE_PATH" to config()[Key("nav.truststore.path", stringType)],
        "NAV_TRUSTSTORE_PASSWORD" to config()[Key("nav.truststore.password", stringType)]
    )
) {
    data class Kafka(
        val brokers: String = config()[Key("kafka.bootstrap.servers", stringType)],
        val user: String? = config().getOrNull(Key("srvdp.regel.grunnlag.username", stringType)),
        val password: String? = config().getOrNull(Key("srvdp.regel.grunnlag.password", stringType))
    ) {
        fun credential(): KafkaCredential? {
            return if (user != null && password != null) {
                KafkaCredential(user, password)
            } else null
        }
    }

    data class Application(
        val id: String = config().getOrElse(Key("application.id", stringType), "dagpenger-regel-grunnlag"),
        val profile: Profile = config()[Key("application.profile", stringType)].let { Profile.valueOf(it) },
        val httpPort: Int = config()[Key("application.httpPort", intType)]
    )

    val features = mapOf(
        "gjustering" to config().getOrElse(Key("feature.gjustering", booleanType), false)
    )
}

enum class Profile {
    LOCAL, DEV, PROD
}

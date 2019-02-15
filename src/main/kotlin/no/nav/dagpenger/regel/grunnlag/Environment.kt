package no.nav.dagpenger.regel.grunnlag

data class Environment(
    val username: String = getEnvVar("SRVDP_REGEL_GRUNNLAG_USERNAME"),
    val password: String = getEnvVar("SRVDP_REGEL_GRUNNLAG_PASSWORD"),
    val bootstrapServersUrl: String = getEnvVar("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092"),
    val httpPort: Int? = 8098
)

fun getEnvVar(varName: String, defaultValue: String? = null) =
    System.getenv(varName) ?: defaultValue ?: throw RuntimeException("Missing required variable \"$varName\"")

package no.nav.dagpenger.regel.grunnlag

import de.huxhorn.sulky.ulid.ULID
import mu.KotlinLogging
import no.nav.dagpenger.streams.KafkaCredential
import no.nav.dagpenger.streams.Service
import no.nav.dagpenger.streams.Topic
import no.nav.dagpenger.streams.Topics
import no.nav.dagpenger.streams.kbranch
import no.nav.dagpenger.streams.streamConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Produced
import org.json.JSONObject
import java.math.BigDecimal
import java.time.YearMonth
import java.util.Properties

private val LOGGER = KotlinLogging.logger {}

val dagpengerBehovTopic = Topic(
    Topics.DAGPENGER_BEHOV_EVENT.name,
    Serdes.StringSerde(),
    Serdes.serdeFrom(JsonSerializer(), JsonDeserializer())
)

class Grunnlag(val env: Environment) : Service() {
    override val SERVICE_APP_ID: String = "dagpenger-regel-grunnlag"
    override val HTTP_PORT: Int = env.httpPort ?: super.HTTP_PORT
    val ulidGenerator = ULID()
    val REGELIDENTIFIKATOR = "Grunnlag.v1"

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val service = Grunnlag(Environment())
            service.start()
        }
    }

    override fun buildTopology(): Topology {
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
        val uavkortet = finnUavkortetGrunnlag(
            behov.harAvtjentVerneplikt(),
            behov.getInntekt(),
            behov.getSenesteInntektsmåned(),
            behov.hasFangstOgFisk()
        )
        val avkortet = uavkortet
        behov.addGrunnlagResultat(
            GrunnlagResultat(
                ulidGenerator.nextULID(),
                ulidGenerator.nextULID(),
                REGELIDENTIFIKATOR,
                avkortet,
                uavkortet
            )
        )
        return behov
    }
}

fun finnUavkortetGrunnlag(
    harAvtjentVerneplikt: Boolean,
    inntekt: Inntekt,
    senesteInntektsMåned: YearMonth,
    fangstOgFisk: Boolean
): BigDecimal {

    val enG = BigDecimal(96883)
    var inntektSiste12 = sumInntektIkkeFangstOgFisk(inntekt, senesteInntektsMåned, 11)
    var inntektSiste36 = sumInntektIkkeFangstOgFisk(inntekt, senesteInntektsMåned, 35)

    var arbeidsInntektSiste12 = sumArbeidInntekt(inntekt, senesteInntektsMåned, 11)
    var arbeidsInntektSiste36 = sumArbeidInntekt(inntekt, senesteInntektsMåned, 35)

    if (fangstOgFisk) {
        arbeidsInntektSiste12 += sumNæringsInntekt(inntekt, senesteInntektsMåned, 11)
        arbeidsInntektSiste36 += sumNæringsInntekt(inntekt, senesteInntektsMåned, 35)
        inntektSiste12 += sumFangstOgFiskInntekt(inntekt, senesteInntektsMåned, 11)
        inntektSiste36 += sumFangstOgFiskInntekt(inntekt, senesteInntektsMåned, 35)
    }
    val årligSnittInntektSiste36 = inntektSiste36 / BigDecimal(3)

    var harTjentNok = false
    if (arbeidsInntektSiste12 > (enG.times(BigDecimal(1.5))) || arbeidsInntektSiste36 > (enG.times(BigDecimal(3)))) {
        harTjentNok = true
    }

    if (harTjentNok) {
        if (inntektSiste12 >= årligSnittInntektSiste36) {
            return inntektSiste12
        }
        return årligSnittInntektSiste36
    }

    return when {
        harAvtjentVerneplikt -> (enG * BigDecimal(3))
        else -> BigDecimal(0)
    }
}

fun sumArbeidInntekt(inntekt: Inntekt, fraMåned: YearMonth, lengde: Int): BigDecimal {
    val tidligsteMåned = finnTidligsteMåned(fraMåned, lengde)

    val gjeldendeMåneder = inntekt.inntektsListe.filter { it.årMåned <= fraMåned && it.årMåned >= tidligsteMåned }

    val sumGjeldendeMåneder = gjeldendeMåneder
        .flatMap {
            it.klassifiserteInntekter
                .filter { it.inntektKlasse == InntektKlasse.ARBEIDSINNTEKT }
                .map { it.beløp }
        }.fold(BigDecimal.ZERO, BigDecimal::add)

    return sumGjeldendeMåneder
}

fun sumNæringsInntekt(inntekt: Inntekt, senesteMåned: YearMonth, lengde: Int): BigDecimal {
    val tidligsteMåned = finnTidligsteMåned(senesteMåned, lengde)

    val gjeldendeMåneder = inntekt.inntektsListe.filter { it.årMåned <= senesteMåned && it.årMåned >= tidligsteMåned }

    val sumGjeldendeMåneder = gjeldendeMåneder
        .flatMap {
            it.klassifiserteInntekter
                .filter { it.inntektKlasse == InntektKlasse.NÆRINGSINNTEKT }
                .map { it.beløp }
        }.fold(BigDecimal.ZERO, BigDecimal::add)

    return sumGjeldendeMåneder
}

fun sumInntektIkkeFangstOgFisk(inntekt: Inntekt, fraMåned: YearMonth, lengde: Int): BigDecimal {
    val tidligsteMåned = finnTidligsteMåned(fraMåned, lengde)

    val gjeldendeMåneder = inntekt.inntektsListe.filter { it.årMåned <= fraMåned && it.årMåned >= tidligsteMåned }

    val sumGjeldendeMåneder = gjeldendeMåneder
        .flatMap {
            it.klassifiserteInntekter
                .filter {
                    it.inntektKlasse == InntektKlasse.ARBEIDSINNTEKT ||
                        it.inntektKlasse == InntektKlasse.DAGPENGER ||
                        it.inntektKlasse == InntektKlasse.SYKEPENGER ||
                        it.inntektKlasse == InntektKlasse.TILTAKSLØNN
                }
                .map { it.beløp }
        }.fold(BigDecimal.ZERO, BigDecimal::add)

    return sumGjeldendeMåneder
}

fun sumFangstOgFiskInntekt(inntekt: Inntekt, senesteMåned: YearMonth, lengde: Int): BigDecimal {
    val tidligsteMåned = finnTidligsteMåned(senesteMåned, lengde)

    val gjeldendeMåneder = inntekt.inntektsListe.filter { it.årMåned <= senesteMåned && it.årMåned >= tidligsteMåned }

    val sumGjeldendeMåneder = gjeldendeMåneder
        .flatMap {
            it.klassifiserteInntekter
                .filter {
                    it.inntektKlasse == InntektKlasse.NÆRINGSINNTEKT ||
                        it.inntektKlasse == InntektKlasse.DAGPENGER_FANGST_FISKE ||
                        it.inntektKlasse == InntektKlasse.SYKEPENGER_FANGST_FISKE
                }
                .map { it.beløp }
        }.fold(BigDecimal.ZERO, BigDecimal::add)

    return sumGjeldendeMåneder
}

fun finnTidligsteMåned(fraMåned: YearMonth, lengde: Int): YearMonth {

    return fraMåned.minusMonths(lengde.toLong())
}

fun shouldBeProcessed(behov: SubsumsjonsBehov): Boolean =
    (behov.needsHentInntektsTask() || behov.needsGrunnlagResultat())

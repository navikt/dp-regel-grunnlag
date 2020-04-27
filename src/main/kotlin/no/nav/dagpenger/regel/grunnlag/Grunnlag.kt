package no.nav.dagpenger.regel.grunnlag

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import de.huxhorn.sulky.ulid.ULID
import java.net.URI
import java.util.Properties
import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.events.Problem
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.sumInntekt
import no.nav.dagpenger.events.moshiInstance
import no.nav.dagpenger.regel.grunnlag.beregning.HovedBeregning
import no.nav.dagpenger.streams.HealthCheck
import no.nav.dagpenger.streams.HealthStatus
import no.nav.dagpenger.streams.River
import no.nav.dagpenger.streams.streamConfig
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import org.apache.kafka.streams.kstream.Predicate

class Grunnlag(
    private val config: Configuration,
    private val instrumentation: GrunnlagInstrumentation
) : River(config.behovTopic) {
    override val SERVICE_APP_ID: String = config.application.id
    override val HTTP_PORT: Int = config.application.httpPort
    private val ulidGenerator = ULID()
    private val REGELIDENTIFIKATOR = "Grunnlag.v1"
    private val jsonAdapterInntektPeriodeInfo: JsonAdapter<List<InntektPeriodeInfo>> =
        moshiInstance.adapter(Types.newParameterizedType(List::class.java, InntektPeriodeInfo::class.java))

    companion object {
        const val LÆRLING: String = "lærling"
        const val GRUNNLAG_RESULTAT = "grunnlagResultat"
        const val INNTEKT = "inntektV1"
        const val AVTJENT_VERNEPLIKT = "harAvtjentVerneplikt"
        const val FANGST_OG_FISK = "oppfyllerKravTilFangstOgFisk"
        const val BEREGNINGSDATO = "beregningsDato"
        const val MANUELT_GRUNNLAG = "manueltGrunnlag"
        const val GRUNNLAG_INNTEKTSPERIODER = "grunnlagInntektsPerioder"
        val inntektAdapter: JsonAdapter<Inntekt> = moshiInstance.adapter(Inntekt::class.java)
    }

    override fun filterPredicates(): List<Predicate<String, Packet>> {
        return listOf(
            Predicate { _, packet -> packet.hasField(INNTEKT) || packet.hasField(MANUELT_GRUNNLAG) },
            Predicate { _, packet -> packet.hasField(BEREGNINGSDATO) },
            Predicate { _, packet -> !packet.hasField(GRUNNLAG_RESULTAT) }
        )
    }

    override fun onPacket(packet: Packet): Packet {
        val fakta = packetToFakta(packet)
        val resultat = HovedBeregning().calculate(fakta)

        val grunnlagResultat = GrunnlagResultat(
            sporingsId = ulidGenerator.nextULID(),
            subsumsjonsId = ulidGenerator.nextULID(),
            regelidentifikator = REGELIDENTIFIKATOR,
            avkortetGrunnlag = resultat.avkortet,
            uavkortetGrunnlag = resultat.uavkortet,
            beregningsregel = resultat.beregningsregel,
            harAvkortet = resultat.harAvkortet,
            grunnbeløpBrukt = when (fakta.verneplikt) {
                true -> fakta.gjeldendeGrunnbeløpForDagensDato.verdi
                false -> fakta.gjeldendeGrunnbeløpVedBeregningsdato.verdi
            }
        )

        createInntektPerioder(fakta)?.apply {
            packet.putValue(
                GRUNNLAG_INNTEKTSPERIODER, checkNotNull(
                    jsonAdapterInntektPeriodeInfo.toJsonValue(this)
                )
            )
        }

        packet.putValue(GRUNNLAG_RESULTAT, grunnlagResultat.toMap())

        instrumentation.grunnlagBeregnet(
            regelIdentifikator = REGELIDENTIFIKATOR,
            fakta = fakta,
            resultat = grunnlagResultat
        )

        return packet
    }

    override fun getConfig(): Properties {
        return streamConfig(
            appId = SERVICE_APP_ID,
            bootStapServerUrl = config.kafka.brokers,
            credential = config.kafka.credential()
        )
    }

    override fun onFailure(packet: Packet, error: Throwable?): Packet {

        packet.addProblem(
            Problem(
                type = URI("urn:dp:error:regel"),
                title = "Ukjent feil ved bruk av grunnlagregel",
                instance = URI("urn:dp:regel:grunnlag")
            )
        )
        return packet
    }
}

fun createInntektPerioder(fakta: Fakta): List<InntektPeriodeInfo>? {
    val arbeidsInntekt = listOf(
        InntektKlasse.ARBEIDSINNTEKT,
        InntektKlasse.DAGPENGER,
        InntektKlasse.SYKEPENGER,
        InntektKlasse.TILTAKSLØNN
    )
    val medFangstOgFisk = listOf(
        InntektKlasse.FANGST_FISKE,
        InntektKlasse.DAGPENGER_FANGST_FISKE,
        InntektKlasse.SYKEPENGER_FANGST_FISKE
    )

    return fakta.inntektsPerioder?.toList()?.mapIndexed { index, list ->
        InntektPeriodeInfo(
            inntektsPeriode = InntektsPeriode(
                list.first().årMåned,
                list.last().årMåned
            ),
            inntekt = list.sumInntekt(if (fakta.fangstOgFisk) medFangstOgFisk + arbeidsInntekt else arbeidsInntekt),
            periode = index + 1,
            inneholderFangstOgFisk = fakta.inntektsPerioder.toList()[index].any { klassifisertInntektMåned ->
                klassifisertInntektMåned.klassifiserteInntekter.any {
                    medFangstOgFisk.contains(
                        it.inntektKlasse
                    )
                }
            }
        )
    }
}

private val config = Configuration()
val features = Features(config.features)

fun main(args: Array<String>) {
    val instrumentation = GrunnlagInstrumentation()

    val service = Grunnlag(instrumentation = instrumentation, config = config)
    service.start()

    RapidApplication.create(
        Configuration().rapidApplication
    ).apply {
        RapidGrunnlag(
            this,
            instrumentation = instrumentation
        )
    }.also {
        it.register(RapidHealthCheck)
    }.start()
}

class NoResultException(message: String) : RuntimeException(message)

class ManueltGrunnlagOgInntektException(message: String) : RuntimeException(message)

object RapidHealthCheck : RapidsConnection.StatusListener, HealthCheck {
    var healthy: Boolean = false

    override fun onStartup(rapidsConnection: RapidsConnection) {
        healthy = true
    }

    override fun onReady(rapidsConnection: RapidsConnection) {
        healthy = true
    }

    override fun onNotReady(rapidsConnection: RapidsConnection) {
        healthy = false
    }

    override fun onShutdown(rapidsConnection: RapidsConnection) {
        healthy = false
    }

    override fun status(): HealthStatus = when (healthy) {
        true -> HealthStatus.UP
        false -> HealthStatus.DOWN
    }
}


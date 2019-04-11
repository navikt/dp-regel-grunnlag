package no.nav.dagpenger.regel.grunnlag

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import de.huxhorn.sulky.ulid.ULID
import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.sumInntekt
import no.nav.dagpenger.events.moshiInstance
import no.nav.dagpenger.regel.grunnlag.beregning.finnHøyesteAvkortetVerdi
import no.nav.dagpenger.regel.grunnlag.beregning.grunnlagsBeregninger
import no.nav.dagpenger.streams.KafkaCredential
import no.nav.dagpenger.streams.River
import no.nav.dagpenger.streams.streamConfig
import org.apache.kafka.streams.kstream.Predicate
import java.time.YearMonth
import java.util.Properties

class Grunnlag(private val env: Environment) : River() {
    override val SERVICE_APP_ID: String = "dagpenger-regel-grunnlag"
    override val HTTP_PORT: Int = env.httpPort ?: super.HTTP_PORT
    private val ulidGenerator = ULID()
    private val REGELIDENTIFIKATOR = "Grunnlag.v1"
    private val jsonAdapterInntektPeriodeInfo: JsonAdapter<List<InntektPeriodeInfo>> =
        moshiInstance.adapter(Types.newParameterizedType(List::class.java, InntektPeriodeInfo::class.java))

    companion object {
        val GRUNNLAG_RESULTAT = "grunnlagResultat"
        val INNTEKT = "inntektV1"
        val AVTJENT_VERNEPLIKT = "harAvtjentVerneplikt"
        val FANGST_OG_FISK = "fangstOgFisk"
        val SENESTE_INNTEKTSMÅNED = "senesteInntektsmåned"
        val BEREGNINGSDAGTO = "beregningsDato"
        val MANUELT_GRUNNLAG = "manueltGrunnlag"
        val GRUNNLAG_INNTEKTSPERIODER = "grunnlagInntektsPerioder"
        val inntektAdapter = moshiInstance.adapter(Inntekt::class.java)
    }

    override fun filterPredicates(): List<Predicate<String, Packet>> {
        return listOf(
            Predicate { _, packet -> packet.hasField(INNTEKT) || packet.hasField(MANUELT_GRUNNLAG) },
            Predicate { _, packet -> packet.hasField(SENESTE_INNTEKTSMÅNED) },
            Predicate { _, packet -> !packet.hasField(GRUNNLAG_RESULTAT) }
        )
    }

    override fun onPacket(packet: Packet): Packet {

        val verneplikt = packet.getNullableBoolean(AVTJENT_VERNEPLIKT) ?: false
        val inntekt: no.nav.dagpenger.events.inntekt.v1.Inntekt? = getInntekt(packet)
        val senesteInntektsmåned = YearMonth.parse(packet.getStringValue(SENESTE_INNTEKTSMÅNED))
        val fangstOgFisk = packet.getNullableBoolean(FANGST_OG_FISK) ?: false
        val beregningsDato = packet.getLocalDate(BEREGNINGSDAGTO)
        val manueltGrunnlag = packet.getNullableIntValue(MANUELT_GRUNNLAG)

        val fakta = Fakta(inntekt, senesteInntektsmåned, verneplikt, fangstOgFisk, beregningsDato, manueltGrunnlag)

        val resultat =
            grunnlagsBeregninger.map { beregning -> beregning.calculate(fakta) }.toSet().finnHøyesteAvkortetVerdi()
                ?: throw NoResultException("Ingen resultat for grunnlagsberegning")

        val grunnlagResultat = GrunnlagResultat(
            ulidGenerator.nextULID(),
            ulidGenerator.nextULID(),
            REGELIDENTIFIKATOR,
            resultat.avkortet,
            resultat.uavkortet,
            resultat.beregningsregel,
            resultat.harAvkortet
        )

        createInntektPerioder(fakta)?.apply { packet.putValue(GRUNNLAG_INNTEKTSPERIODER, checkNotNull(
            jsonAdapterInntektPeriodeInfo.toJsonValue(this))) }

        packet.putValue(GRUNNLAG_RESULTAT, grunnlagResultat.toMap())
        return packet
    }

    private fun getInntekt(packet: Packet): Inntekt? =
        if (packet.hasField(MANUELT_GRUNNLAG) && packet.hasField(INNTEKT)) {
            throw ManueltGrunnlagOgInntektException("Har manuelt grunnlag og inntekt")
        } else if (packet.hasField(INNTEKT)) {
            packet.getObjectValue(INNTEKT) { requireNotNull(inntektAdapter.fromJsonValue(it)) }
        } else {
            null
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

    override fun getConfig(): Properties {
        return streamConfig(
            appId = SERVICE_APP_ID,
            bootStapServerUrl = env.bootstrapServersUrl,
            credential = KafkaCredential(env.username, env.password)
        )
    }
}

fun main(args: Array<String>) {
    val service = Grunnlag(Environment())
    service.start()
}

class NoResultException(message: String) : RuntimeException(message)

class ManueltGrunnlagOgInntektException(message: String) : RuntimeException(message)
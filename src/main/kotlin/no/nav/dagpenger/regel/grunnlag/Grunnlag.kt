package no.nav.dagpenger.regel.grunnlag

import de.huxhorn.sulky.ulid.ULID
import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.events.inntekt.v1.Inntekt
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

    companion object {
        val GRUNNLAG_RESULTAT = "grunnlagResultat"
        val INNTEKT = "inntektV1"
        val AVTJENT_VERNEPLIKT = "harAvtjentVerneplikt"
        val FANGST_OG_FISK = "fangstOgFisk"
        val SENESTE_INNTEKTSMÅNED = "senesteInntektsmåned"
        val BEREGNINGSDAGTO = "beregningsDato"
        val inntektAdapter = moshiInstance.adapter(Inntekt::class.java)
    }

    override fun filterPredicates(): List<Predicate<String, Packet>> {
        return listOf(
            Predicate { _, packet -> packet.hasField(INNTEKT) },
            Predicate { _, packet -> packet.hasField(SENESTE_INNTEKTSMÅNED) },
            Predicate { _, packet -> !packet.hasField(GRUNNLAG_RESULTAT) }
        )
    }

    override fun onPacket(packet: Packet): Packet {

        val verneplikt = packet.getNullableBoolean(AVTJENT_VERNEPLIKT) ?: false
        val inntekt: no.nav.dagpenger.events.inntekt.v1.Inntekt =
            packet.getObjectValue(INNTEKT) { requireNotNull(inntektAdapter.fromJson(it)) }
        val senesteInntektsmåned = YearMonth.parse(packet.getStringValue(SENESTE_INNTEKTSMÅNED))
        val fangstOgFisk = packet.getNullableBoolean(FANGST_OG_FISK) ?: false
        val beregningsDato = packet.getLocalDate(BEREGNINGSDAGTO)

        val fakta = Fakta(inntekt, senesteInntektsmåned, verneplikt, fangstOgFisk, beregningsDato)

        /*val uavkortet = finnUavkortetGrunnlag(
            verneplikt,
            inntekt,
            senesteInntektsmåned,
            fangstOgFisk
        )

        val avkortet = finnAvkortetGrunnlag(verneplikt, inntekt, senesteInntektsmåned, fangstOgFisk)

        val resultat = GrunnlagResultat(
            ulidGenerator.nextULID(),
            ulidGenerator.nextULID(),
            REGELIDENTIFIKATOR,
            avkortet,
            uavkortet

        )*/

        //packet.putValue(GRUNNLAG_RESULTAT, resultat.toMap())
        return packet
    }

    override fun getConfig(): Properties {
        val props = streamConfig(
            appId = SERVICE_APP_ID,
            bootStapServerUrl = env.bootstrapServersUrl,
            credential = KafkaCredential(env.username, env.password)
        )
        return props
    }
}

fun main(args: Array<String>) {
    val service = Grunnlag(Environment())
    service.start()
}

fun finnTidligsteMåned(fraMåned: YearMonth, lengde: Int): YearMonth {

    return fraMåned.minusMonths(lengde.toLong())
}

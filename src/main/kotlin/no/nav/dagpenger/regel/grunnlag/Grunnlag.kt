package no.nav.dagpenger.regel.grunnlag

import de.huxhorn.sulky.ulid.ULID
import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.streams.KafkaCredential
import no.nav.dagpenger.streams.River
import no.nav.dagpenger.streams.streamConfig
import org.apache.kafka.streams.kstream.Predicate
import java.math.BigDecimal
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

        val uavkortet = finnUavkortetGrunnlag(
            verneplikt,
            inntekt,
            senesteInntektsmåned,
            fangstOgFisk
        )

        val avkortet = uavkortet

        val resultat = GrunnlagResultat(
            ulidGenerator.nextULID(),
            ulidGenerator.nextULID(),
            REGELIDENTIFIKATOR,
            avkortet,
            uavkortet

        )

        packet.putValue(GRUNNLAG_RESULTAT, resultat.toMap())
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
                .filter { it.inntektKlasse == InntektKlasse.FANGST_FISKE }
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
                    it.inntektKlasse == InntektKlasse.FANGST_FISKE ||
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

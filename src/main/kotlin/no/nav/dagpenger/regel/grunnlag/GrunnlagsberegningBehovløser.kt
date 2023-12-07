package no.nav.dagpenger.regel.grunnlag

import de.huxhorn.sulky.ulid.ULID
import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.events.inntekt.v1.sumInntekt
import no.nav.dagpenger.regel.grunnlag.beregning.HovedBeregning
import no.nav.dagpenger.regel.grunnlag.beregning.inntektsklasser
import no.nav.dagpenger.regel.grunnlag.beregning.inntektsklasserMedFangstOgFiske
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import java.net.URI

private val log = KotlinLogging.logger {}
private val sikkerLogg = KotlinLogging.logger("tjenestekall")

class GrunnlagsberegningBehovløser(
    rapidsConnection: RapidsConnection,
    private val instrumentation: GrunnlagInstrumentation = GrunnlagInstrumentation(),
) : River.PacketListener {
    private val regelidentifikator = "Grunnlag.v1"

    companion object {
        const val AVTJENT_VERNEPLIKT = "harAvtjentVerneplikt"
        const val FANGST_OG_FISKE = "oppfyllerKravTilFangstOgFisk"
        const val LÆRLING = "lærling"
        const val BEREGNINGSDATO = "beregningsDato"
        const val REGELVERKSDATO = "regelverksdato"
        const val INNTEKT = "inntektV1"
        const val MANUELT_GRUNNLAG = "manueltGrunnlag"
        const val FORRIGE_GRUNNLAG = "forrigeGrunnlag"
        const val GRUNNLAG_INNTEKTSPERIODER = "grunnlagInntektsPerioder"
        const val GRUNNLAG_RESULTAT = "grunnlagResultat"
        const val BEHOV_ID = "behovId"
        const val PROBLEM = "system_problem"

        val rapidFilter: River.() -> Unit = {
            validate { it.requireKey(BEHOV_ID) }
            validate { it.requireKey(BEREGNINGSDATO) }
            validate {
                it.interestedIn(INNTEKT)
                it.interestedIn(MANUELT_GRUNNLAG)
                it.interestedIn(FORRIGE_GRUNNLAG)
                it.interestedIn(AVTJENT_VERNEPLIKT)
                it.interestedIn(FANGST_OG_FISKE)
                it.interestedIn(LÆRLING)
                it.interestedIn(REGELVERKSDATO)
            }
            validate { it.rejectKey(GRUNNLAG_RESULTAT) }
        }
    }

    init {
        River(rapidsConnection).apply(rapidFilter).register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        withLoggingContext("behovId" to packet[BEHOV_ID].asText()) {
            try {
                sikkerLogg.info("Mottok pakke: ${packet.toJson()}")
                if (manglendePacketNøkler(packet)) return

                val fakta = mapToFaktaFrom(packet)
                val resultat = HovedBeregning().calculate(fakta)
                val ulidGenerator = ULID()

                val grunnlagResultat = GrunnlagResultat(
                    sporingsId = ulidGenerator.nextULID(),
                    subsumsjonsId = ulidGenerator.nextULID(),
                    regelidentifikator = regelidentifikator,
                    avkortetGrunnlag = resultat.avkortet,
                    uavkortetGrunnlag = resultat.uavkortet,
                    beregningsregel = resultat.beregningsregel,
                    harAvkortet = resultat.harAvkortet,
                    grunnbeløpBrukt = when (fakta.verneplikt) {
                        true -> fakta.grunnbeløpVedRegelverksdato().verdi
                        false -> fakta.grunnbeløpVedBeregningsdato().verdi
                    },
                )
                createInntektPerioder(fakta)?.let { inntektPerioder ->
                    packet[GRUNNLAG_INNTEKTSPERIODER] = inntektPerioder.toMaps()
                }

                packet[GRUNNLAG_RESULTAT] = grunnlagResultat.toMap()

                instrumentation.grunnlagBeregnet(
                    regelIdentifikator = regelidentifikator,
                    fakta = fakta,
                    resultat = grunnlagResultat,
                )

                context.publish(packet.toJson())
                sikkerLogg.info { "Løste behov for grunnlag $grunnlagResultat med fakta $fakta" }
            } catch (e: Exception) {
                val problem = Problem(
                    type = URI("urn:dp:error:regel"),
                    title = "Ukjent feil ved bruk av grunnlagregel",
                    instance = URI("urn:dp:regel:grunnlag"),
                )
                packet[PROBLEM] = problem.toMap
                context.publish(packet.toJson())
                throw e
            }
        }
    }

    private fun manglendePacketNøkler(packet: JsonMessage) =
        packet[INNTEKT].isMissingNode &&
            packet[MANUELT_GRUNNLAG].isMissingNode &&
            packet[FORRIGE_GRUNNLAG].isMissingNode
}

fun createInntektPerioder(fakta: Fakta): List<InntektPeriodeInfo>? {
    val arbeidsinntektKlasser = inntektsklasser.toList()
    val fangstOgFiskeKlasser = inntektsklasserMedFangstOgFiske.toList().filterNot {
        inntektsklasser.toList().contains(it)
    }

    return fakta.inntektsPerioder?.toList()?.mapIndexed { index, list ->
        InntektPeriodeInfo(
            inntektsPeriode = InntektsPeriode(
                list.first().årMåned,
                list.last().årMåned,
            ),
            inntekt = list.sumInntekt(
                if (fakta.fangstOgFiske) {
                    arbeidsinntektKlasser + fangstOgFiskeKlasser
                } else {
                    arbeidsinntektKlasser
                },
            ),
            periode = index + 1,
            inneholderFangstOgFisk = fakta.inntektsPerioder.toList()[index].any { klassifisertInntektMåned ->
                klassifisertInntektMåned.klassifiserteInntekter.any {
                    fangstOgFiskeKlasser.contains(it.inntektKlasse)
                }
            },
        )
    }
}

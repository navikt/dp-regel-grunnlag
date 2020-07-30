package no.nav.dagpenger.regel.grunnlag

import com.fasterxml.jackson.databind.JsonNode
import de.huxhorn.sulky.ulid.ULID
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.grunnbelop.Grunnbeløp
import no.nav.dagpenger.grunnbelop.Regel
import no.nav.dagpenger.grunnbelop.forDato
import no.nav.dagpenger.grunnbelop.getGrunnbeløpForRegel
import no.nav.dagpenger.inntekt.rpc.InntektHenter
import no.nav.dagpenger.regel.grunnlag.beregning.HovedBeregning
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.isMissingOrNull
import java.time.LocalDate

private val log = KotlinLogging.logger {}
private val sikkerLogg = KotlinLogging.logger("tjenestekall")

class LøsningService(
    rapidsConnection: RapidsConnection,
    private val inntektHenter: InntektHenter,
    private val instrumentation: GrunnlagInstrumentation = GrunnlagInstrumentation()
) : River.PacketListener {

    init {
        River(rapidsConnection).apply {
            validate { it.demandAll("@behov", listOf("Grunnlag")) }
            validate { it.rejectKey("@løsning") }
            validate { it.requireKey("@id", "vedtakId") }
            validate { it.require("inntektId", JsonNode::asULID) }
            validate { it.requireKey("beregningsdato") }
            validate {
                it.interestedIn(
                    "lærling",
                    "harAvtjentVerneplikt",
                    "oppfyllerKravTilFangstOgFisk",
                    "manueltGrunnlag"
                )
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: RapidsConnection.MessageContext) {
        val fakta = packet.toFakta(inntektHenter)

        withLoggingContext(
            "behovId" to packet["@id"].asText(),
            "vedtakId" to packet["vedtakId"].asText()
        ) {
            try {
                val resultat = HovedBeregning().calculate(fakta)

                val grunnlagResultat = RapidGrunnlagResultat(
                    avkortetKandidat = resultat.avkortet,
                    uavkortetKandidat = resultat.uavkortet,
                    beregningsregel = resultat.beregningsregel,
                    harAvkortet = resultat.harAvkortet,
                    grunnbeløp = when (fakta.verneplikt) {
                        true -> fakta.gjeldendeGrunnbeløpForDagensDato.verdi
                        false -> fakta.gjeldendeGrunnbeløpVedBeregningsdato.verdi
                    },
                    inntektsperioder = createInntektPerioder(fakta)
                )

                packet["@løsning"] = mapOf(
                    "Grunnlag" to grunnlagResultat
                )

                instrumentation.grunnlagBeregnet(
                    regelIdentifikator = "Grunnlag.v1",
                    fakta = fakta,
                    resultat = grunnlagResultat
                )

                log.info { "løser behov for ${packet["@id"].asText()}" }

                context.send(packet.toJson())
            } catch (err: Exception) {
                log.error(err) { "feil ved beregning av grunnlag: ${err.message} for ${packet["@id"].asText()}" }
            }
        }
    }

    override fun onError(problems: MessageProblems, context: RapidsConnection.MessageContext) {
        log.info { problems.toString() }
        sikkerLogg.info { problems.toExtendedReport() }
    }
}

internal fun JsonMessage.toFakta(inntektHenter: InntektHenter): Fakta {

    if (!this["manueltGrunnlag"].isMissingOrNull() && !this["inntektId"].isMissingOrNull()) {
        throw ManueltGrunnlagOgInntektException("Har manuelt grunnlag og inntekt")
    }

    val verneplikt = this["harAvtjentVerneplikt"].asBoolean(false)
    val inntekt =
        this["inntektId"].asULID().let { runBlocking { inntektHenter.hentKlassifisertInntekt(it.toString()) } }
    val fangstOgFisk = this["oppfyllerKravTilFangstOgFisk"].asBoolean(false)
    val beregningsdato = this["beregningsdato"].asLocalDate()
    val manueltGrunnlag = this["manueltGrunnlag"].asInt()
    val lærling = this["lærling"].asBoolean(false)
    val dagensDato = LocalDate.now()

    val grunnbeløpVedBeregningsdato = when {
        isThisGjusteringTest(beregningsdato, verneplikt) -> Grunnbeløp.GjusteringsTest
        else -> getGrunnbeløpForRegel(Regel.Grunnlag).forDato(beregningsdato)
    }
    val grunnbeløpVedDagensDato = when {
        isThisGjusteringTest(beregningsdato, verneplikt) -> Grunnbeløp.GjusteringsTest
        else -> getGrunnbeløpForRegel(Regel.Grunnlag).forDato(dagensDato)
    }

    return Fakta(
        inntekt = inntekt,
        verneplikt = verneplikt,
        fangstOgFisk = fangstOgFisk,
        beregningsdato = beregningsdato,
        manueltGrunnlag = manueltGrunnlag,
        lærling = lærling,
        gjeldendeGrunnbeløpVedBeregningsdato = grunnbeløpVedBeregningsdato,
        gjeldendeGrunnbeløpForDagensDato = grunnbeløpVedDagensDato
    )
}

fun JsonNode.asULID(): ULID.Value = asText().let { ULID.parseULID(it) }

package no.nav.dagpenger.regel.grunnlag

import de.huxhorn.sulky.ulid.ULID
import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.regel.grunnlag.beregning.HovedBeregning
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

private val log = KotlinLogging.logger {}

class RapidGrunnlag(
    rapidsConnection: RapidsConnection,
    private val instrumentation: GrunnlagInstrumentation = GrunnlagInstrumentation()
): River.PacketListener {
    private val ulidGenerator = ULID()

    init {
        River(rapidsConnection).apply {
            validate { it.requireAll("@behov", listOf(GRUNNLAG)) }
            validate { it.forbid("@løsning") }
            validate { it.requireKey("@id") }
            validate { it.requireKey(BEREGNINGSDATO) }
            validate { it.interestedIn(LÆRLING) }
        }.register(this)
    }

    companion object {
        const val LÆRLING: String = "lærling"
        const val GRUNNLAG_RESULTAT = "grunnlagResultat"
        const val INNTEKT = "inntektV1"
        const val AVTJENT_VERNEPLIKT = "harAvtjentVerneplikt"
        const val FANGST_OG_FISK = "oppfyllerKravTilFangstOgFisk"
        const val BEREGNINGSDATO = "beregningsdato"
        const val MANUELT_GRUNNLAG = "manueltGrunnlag"
        const val GRUNNLAG_INNTEKTSPERIODER = "grunnlagInntektsPerioder"
        const val GRUNNLAG = "Grunnlag"
        const val REGELIDENTIFIKATOR = "Grunnlag.v1"
    }

    override fun onPacket(packet: JsonMessage, context: RapidsConnection.MessageContext) {
        val fakta = messageToFakta(packet)

        withLoggingContext(
            "behovId" to packet["@id"].asText(),
            "beregningsdato" to fakta.beregningsdato.toString(),
            "lærling" to fakta.lærling.toString()
        ) {
            try {
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

                packet["@løsning"] = mapOf(
                    GRUNNLAG to grunnlagResultat
                )

                instrumentation.grunnlagBeregnet(
                    regelIdentifikator = REGELIDENTIFIKATOR,
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
}

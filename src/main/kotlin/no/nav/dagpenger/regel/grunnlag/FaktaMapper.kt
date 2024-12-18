package no.nav.dagpenger.regel.grunnlag

import com.fasterxml.jackson.databind.JsonNode
import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDate
import com.github.navikt.tbd_libs.rapids_and_rivers.isMissingOrNull
import mu.KotlinLogging
import no.nav.dagpenger.inntekt.v1.Inntekt
import no.nav.dagpenger.regel.grunnlag.GrunnlagsberegningBehovløser.Companion.AVTJENT_VERNEPLIKT
import no.nav.dagpenger.regel.grunnlag.GrunnlagsberegningBehovløser.Companion.BEREGNINGSDATO
import no.nav.dagpenger.regel.grunnlag.GrunnlagsberegningBehovløser.Companion.FANGST_OG_FISKE
import no.nav.dagpenger.regel.grunnlag.GrunnlagsberegningBehovløser.Companion.FORRIGE_GRUNNLAG
import no.nav.dagpenger.regel.grunnlag.GrunnlagsberegningBehovløser.Companion.INNTEKT
import no.nav.dagpenger.regel.grunnlag.GrunnlagsberegningBehovløser.Companion.LÆRLING
import no.nav.dagpenger.regel.grunnlag.GrunnlagsberegningBehovløser.Companion.MANUELT_GRUNNLAG
import no.nav.dagpenger.regel.grunnlag.GrunnlagsberegningBehovløser.Companion.REGELVERKSDATO
import java.time.LocalDate

private val sikkerLogg = KotlinLogging.logger("tjenestekall")

fun mapToFaktaFrom(packet: JsonMessage): Fakta {
    val avtjentVerneplikt: Boolean = packet.avtjentVerneplikt()
    val fangstOgFisk: Boolean = packet.fangstOgFiske()
    val lærling: Boolean = packet.lærling()
    val beregningsdato: LocalDate = packet[BEREGNINGSDATO].asLocalDate()
    val regelverksdato: LocalDate = packet.regelverksdato() ?: beregningsdato
    val inntekt: Inntekt? = packet.inntekt()
    val manueltGrunnlag: Int? = packet.manueltGrunnlag()
    val forrigeGrunnlag: Int? = packet.forrigeGrunnlag()

    return Fakta(
        inntekt = inntekt,
        verneplikt = avtjentVerneplikt,
        fangstOgFiske = fangstOgFisk,
        beregningsdato = beregningsdato,
        regelverksdato = regelverksdato,
        manueltGrunnlag = manueltGrunnlag,
        forrigeGrunnlag = forrigeGrunnlag,
        lærling = lærling,
    )
}

private fun JsonNode.asBooleanStrict(): Boolean = asText().toBooleanStrict()

private fun JsonMessage.avtjentVerneplikt() =
    when (this.harVerdi(AVTJENT_VERNEPLIKT)) {
        true -> this[AVTJENT_VERNEPLIKT].asBooleanStrict()
        false -> false
    }

private fun JsonMessage.fangstOgFiske() =
    when (this.harVerdi(FANGST_OG_FISKE)) {
        true -> this[FANGST_OG_FISKE].asBooleanStrict()
        false -> false
    }

private fun JsonMessage.lærling() =
    when (this.harVerdi(LÆRLING)) {
        true -> this[LÆRLING].asBooleanStrict()
        false -> false
    }

private fun JsonMessage.regelverksdato() =
    when (this.harVerdi(REGELVERKSDATO)) {
        true -> this[REGELVERKSDATO].asLocalDate()
        false -> null
    }

private fun JsonMessage.manueltGrunnlag() =
    when (this.harVerdi(MANUELT_GRUNNLAG)) {
        true -> this[MANUELT_GRUNNLAG].asText().toDouble().toInt()
        false -> null
    }

private fun JsonMessage.forrigeGrunnlag() =
    when (this.harVerdi(FORRIGE_GRUNNLAG)) {
        true -> this[FORRIGE_GRUNNLAG].asText().toDouble().toInt()
        false -> null
    }

internal fun JsonMessage.inntekt(): Inntekt? =
    when {
        this.harVerdi(MANUELT_GRUNNLAG) && this.harVerdi(INNTEKT) ->
            throw ManueltGrunnlagOgInntektException("Har manuelt grunnlag og inntekt")

        this.harVerdi(FORRIGE_GRUNNLAG) && this.harVerdi(INNTEKT) ->
            throw ForrigeGrunnlagOgInntektException("Har forrige grunnlag og inntekt")

        this.harVerdi(INNTEKT) -> {
            val inntektJson = this[INNTEKT]
            runCatching {
                objectMapper.convertValue(inntektJson, Inntekt::class.java)
            }.onFailure {
                sikkerLogg.error("Feilet å parse inntekt: $inntektJson")
            }.getOrThrow()
        }

        else -> null
    }

private fun JsonMessage.harVerdi(field: String) = !this[field].isMissingOrNull()

class ManueltGrunnlagOgInntektException(
    message: String,
) : RuntimeException(message)

class ForrigeGrunnlagOgInntektException(
    message: String,
) : RuntimeException(message)

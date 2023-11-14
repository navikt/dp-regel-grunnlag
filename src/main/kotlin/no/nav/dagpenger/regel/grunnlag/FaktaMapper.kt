package no.nav.dagpenger.regel.grunnlag

import mu.KotlinLogging
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.regel.grunnlag.GrunnlagsberegningBehovløser.Companion.AVTJENT_VERNEPLIKT
import no.nav.dagpenger.regel.grunnlag.GrunnlagsberegningBehovløser.Companion.BEREGNINGSDATO
import no.nav.dagpenger.regel.grunnlag.GrunnlagsberegningBehovløser.Companion.FANGST_OG_FISKE
import no.nav.dagpenger.regel.grunnlag.GrunnlagsberegningBehovløser.Companion.FORRIGE_GRUNNLAG
import no.nav.dagpenger.regel.grunnlag.GrunnlagsberegningBehovløser.Companion.INNTEKT
import no.nav.dagpenger.regel.grunnlag.GrunnlagsberegningBehovløser.Companion.LÆRLING
import no.nav.dagpenger.regel.grunnlag.GrunnlagsberegningBehovløser.Companion.MANUELT_GRUNNLAG
import no.nav.dagpenger.regel.grunnlag.GrunnlagsberegningBehovløser.Companion.REGELVERKSDATO
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.isMissingOrNull
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
        fangstOgFisk = fangstOgFisk,
        beregningsdato = beregningsdato,
        regelverksdato = regelverksdato,
        manueltGrunnlag = manueltGrunnlag,
        forrigeGrunnlag = forrigeGrunnlag,
        lærling = lærling,
    )
}

private fun JsonMessage.avtjentVerneplikt() =
    when (this.harVerdi(AVTJENT_VERNEPLIKT)) {
        true -> this[AVTJENT_VERNEPLIKT].asBoolean()
        false -> false
    }

private fun JsonMessage.fangstOgFiske() =
    when (this.harVerdi(FANGST_OG_FISKE)) {
        true -> this[AVTJENT_VERNEPLIKT].asBoolean()
        false -> false
    }

private fun JsonMessage.lærling() =
    when (this.harVerdi(LÆRLING)) {
        true -> this[LÆRLING].asBoolean()
        false -> false
    }

private fun JsonMessage.regelverksdato() =
    when (this.harVerdi(REGELVERKSDATO)) {
        true -> this[REGELVERKSDATO].asLocalDate()
        false -> null
    }

private fun JsonMessage.manueltGrunnlag() =
    when (this.harVerdi(MANUELT_GRUNNLAG)) {
        true -> this[MANUELT_GRUNNLAG].asInt()
        false -> null
    }

private fun JsonMessage.forrigeGrunnlag() =
    when (this.harVerdi(FORRIGE_GRUNNLAG)) {
        true -> this[FORRIGE_GRUNNLAG].asInt()
        false -> null
    }

internal fun JsonMessage.inntekt(): Inntekt? = when {
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

class ManueltGrunnlagOgInntektException(message: String) : RuntimeException(message)
class ForrigeGrunnlagOgInntektException(message: String) : RuntimeException(message)

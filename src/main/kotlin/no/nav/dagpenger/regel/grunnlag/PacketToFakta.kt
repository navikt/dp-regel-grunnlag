package no.nav.dagpenger.regel.grunnlag

import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import java.time.YearMonth

internal fun packetToFakta(packet: Packet): Fakta {

    val verneplikt = packet.getNullableBoolean(Grunnlag.AVTJENT_VERNEPLIKT) ?: false
    val inntekt: no.nav.dagpenger.events.inntekt.v1.Inntekt? = getInntekt(packet)
    val senesteInntektsmåned = YearMonth.parse(packet.getStringValue(Grunnlag.SENESTE_INNTEKTSMÅNED))
    val fangstOgFisk = packet.getNullableBoolean(Grunnlag.FANGST_OG_FISK) ?: false
    val beregningsDato = packet.getLocalDate(Grunnlag.BEREGNINGSDAGTO)
    val manueltGrunnlag = packet.getNullableIntValue(Grunnlag.MANUELT_GRUNNLAG)

    return Fakta(inntekt, senesteInntektsmåned, verneplikt, fangstOgFisk, beregningsDato, manueltGrunnlag)
}

private fun getInntekt(packet: Packet): Inntekt? =
    if (packet.hasField(Grunnlag.MANUELT_GRUNNLAG) && packet.hasField(Grunnlag.INNTEKT)) {
        throw ManueltGrunnlagOgInntektException("Har manuelt grunnlag og inntekt")
    } else if (packet.hasField(Grunnlag.INNTEKT)) {
        packet.getObjectValue(Grunnlag.INNTEKT) { requireNotNull(Grunnlag.inntektAdapter.fromJsonValue(it)) }
    } else {
        null
    }
package no.nav.dagpenger.regel.grunnlag

import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.grunnbelop.Grunnbeløp
import no.nav.dagpenger.grunnbelop.Regel
import no.nav.dagpenger.grunnbelop.forDato
import no.nav.dagpenger.grunnbelop.getGrunnbeløpForRegel
import java.time.LocalDate

internal fun packetToFakta(packet: Packet): Fakta {

    val verneplikt = packet.getNullableBoolean(Grunnlag.AVTJENT_VERNEPLIKT) ?: false
    val inntekt: Inntekt? = getInntekt(packet)
    val fangstOgFisk = packet.getNullableBoolean(Grunnlag.FANGST_OG_FISK) ?: false
    val beregningsdato = packet.getLocalDate(Grunnlag.BEREGNINGSDATO)
    val manueltGrunnlag = packet.getNullableIntValue(Grunnlag.MANUELT_GRUNNLAG)
    val forrigeGrunnlag = packet.getNullableIntValue(Grunnlag.FORRIGE_GRUNNLAG)
    val lærling = packet.getNullableBoolean(Grunnlag.LÆRLING) == true
    val regelverksdato = packet.getNullableLocalDate(Grunnlag.REGELVERKSDATO) ?: beregningsdato

    return Fakta(
        inntekt = inntekt,
        verneplikt = verneplikt,
        fangstOgFisk = fangstOgFisk,
        beregningsdato = beregningsdato,
        regelverksdato = regelverksdato,
        manueltGrunnlag = manueltGrunnlag,
        forrigeGrunnlag = forrigeGrunnlag,
        lærling = lærling
    )
}

private fun getInntekt(packet: Packet): Inntekt? =
    if (packet.hasField(Grunnlag.MANUELT_GRUNNLAG) && packet.hasField(Grunnlag.INNTEKT)) {
        throw ManueltGrunnlagOgInntektException("Har manuelt grunnlag og inntekt")
    } else if (packet.hasField(Grunnlag.FORRIGE_GRUNNLAG) && packet.hasField(Grunnlag.INNTEKT)) {
        throw ForrigeGrunnlagOgInntektException("Har forrige grunnlag og inntekt")
    } else if (packet.hasField(Grunnlag.INNTEKT)) {
        packet.getObjectValue(Grunnlag.INNTEKT) { requireNotNull(Grunnlag.inntektAdapter.fromJsonValue(it)) }
    } else {
        null
    }

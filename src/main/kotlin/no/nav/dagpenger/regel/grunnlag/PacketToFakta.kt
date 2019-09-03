package no.nav.dagpenger.regel.grunnlag

import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.grunnbelop.Grunnbeløp
import no.nav.dagpenger.grunnbelop.getGrunnbeløpForDato
import java.time.LocalDate
import java.time.Month

internal fun packetToFakta(packet: Packet): Fakta {

    val verneplikt = packet.getNullableBoolean(Grunnlag.AVTJENT_VERNEPLIKT) ?: false
    val inntekt: Inntekt? = getInntekt(packet)
    val fangstOgFisk = packet.getNullableBoolean(Grunnlag.FANGST_OG_FISK) ?: false
    val beregningsdato = packet.getLocalDate(Grunnlag.BEREGNINGSDAGTO)
    val manueltGrunnlag = packet.getNullableIntValue(Grunnlag.MANUELT_GRUNNLAG)

    return Fakta(
        inntekt = inntekt,
        verneplikt = verneplikt,
        fangstOgFisk = fangstOgFisk,
        beregningsdato = beregningsdato,
        manueltGrunnlag = manueltGrunnlag,
        grunnbeløp = getGrunnbeløp(beregningsdato)
    )
}


private fun getGrunnbeløp(beregningsdato: LocalDate): Grunnbeløp {
    if (features.isEnabled("gjustering")) {
        if (beregningsdato.isAfter(LocalDate.of(2019, 8, 1)))
            Grunnbeløp(
                LocalDate.of(2015, Month.MAY, 1),
                LocalDate.of(2016, Month.APRIL, 30),
                90068.toBigDecimal()
            )
    }

    return getGrunnbeløpForDato(LocalDate.from(beregningsdato))
}

private fun getInntekt(packet: Packet): Inntekt? =
    if (packet.hasField(Grunnlag.MANUELT_GRUNNLAG) && packet.hasField(Grunnlag.INNTEKT)) {
        throw ManueltGrunnlagOgInntektException("Har manuelt grunnlag og inntekt")
    } else if (packet.hasField(Grunnlag.INNTEKT)) {
        packet.getObjectValue(Grunnlag.INNTEKT) { requireNotNull(Grunnlag.inntektAdapter.fromJsonValue(it)) }
    } else {
        null
    }
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
    val beregningsdato = packet.getLocalDate(Grunnlag.BEREGNINGSDAGTO)
    val manueltGrunnlag = packet.getNullableIntValue(Grunnlag.MANUELT_GRUNNLAG)
    val lærling = packet.getNullableBoolean(Grunnlag.LÆRLING) == true
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

internal fun isThisGjusteringTest(
    beregningsdato: LocalDate,
    verneplikt: Boolean
): Boolean {
    val isBeregningsDatoAfterGjustering = beregningsdato.isAfter(LocalDate.of(2019, 8, 1).minusDays(1))
    return features.isEnabled("gjustering") && (isBeregningsDatoAfterGjustering || verneplikt)
}

private fun getInntekt(packet: Packet): Inntekt? =
    if (packet.hasField(Grunnlag.MANUELT_GRUNNLAG) && packet.hasField(Grunnlag.INNTEKT)) {
        throw ManueltGrunnlagOgInntektException("Har manuelt grunnlag og inntekt")
    } else if (packet.hasField(Grunnlag.INNTEKT)) {
        packet.getObjectValue(Grunnlag.INNTEKT) { requireNotNull(Grunnlag.inntektAdapter.fromJsonValue(it)) }
    } else {
        null
    }
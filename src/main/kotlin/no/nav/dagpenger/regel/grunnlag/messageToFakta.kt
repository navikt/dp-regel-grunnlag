package no.nav.dagpenger.regel.grunnlag

import java.math.BigDecimal
import java.time.LocalDate
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import no.nav.dagpenger.grunnbelop.Grunnbeløp
import no.nav.dagpenger.grunnbelop.Regel
import no.nav.dagpenger.grunnbelop.forDato
import no.nav.dagpenger.grunnbelop.getGrunnbeløpForRegel
import no.nav.dagpenger.regel.grunnlag.RapidGrunnlag.Companion.AVTJENT_VERNEPLIKT
import no.nav.dagpenger.regel.grunnlag.RapidGrunnlag.Companion.BEREGNINGSDATO
import no.nav.dagpenger.regel.grunnlag.RapidGrunnlag.Companion.FANGST_OG_FISK
import no.nav.dagpenger.regel.grunnlag.RapidGrunnlag.Companion.LÆRLING
import no.nav.dagpenger.regel.grunnlag.RapidGrunnlag.Companion.MANUELT_GRUNNLAG
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.asYearMonth
import no.nav.helse.rapids_rivers.isMissingOrNull

internal fun messageToFakta(packet: JsonMessage): Fakta {

    val verneplikt = packet[AVTJENT_VERNEPLIKT].asBoolean(false)
    val inntekt: Inntekt? = getInntekt(packet)
    val fangstOgFisk = packet[FANGST_OG_FISK].asBoolean(false)
    val beregningsdato = packet[BEREGNINGSDATO].asLocalDate()
    val manueltGrunnlag = packet[MANUELT_GRUNNLAG].asInt()
    val lærling = packet[LÆRLING].asBoolean(false)
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

private fun getInntekt(packet: JsonMessage): Inntekt? =
    if (!packet[Grunnlag.MANUELT_GRUNNLAG].isMissingOrNull() && !packet[Grunnlag.INNTEKT].isMissingOrNull()) {
        throw ManueltGrunnlagOgInntektException("Har manuelt grunnlag og inntekt")
    } else if (!packet[Grunnlag.INNTEKT].isMissingOrNull()) {
        packet[Grunnlag.INNTEKT].let {
            Inntekt(
                inntektsId = it["inntektsId"].asText(),
                inntektsListe = it["inntektsListe"].map {
                    KlassifisertInntektMåned(
                        årMåned = it["årMåned"].asYearMonth(),
                        klassifiserteInntekter = it["klassifiserteInntekter"].map {
                            KlassifisertInntekt(
                                beløp = BigDecimal(
                                    it["beløp"].asInt()
                                ), inntektKlasse = InntektKlasse.valueOf(it["inntektKlasse"].asText())
                            )
                        })
                },
                manueltRedigert = it["manueltRedigert"].asBoolean(),
                sisteAvsluttendeKalenderMåned = it["sisteAvsluttendeKalenderMåned"].asYearMonth()
            )
        }
    } else {
        null
    }

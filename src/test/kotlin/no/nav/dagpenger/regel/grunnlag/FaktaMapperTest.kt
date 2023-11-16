package no.nav.dagpenger.regel.grunnlag

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import no.nav.dagpenger.regel.grunnlag.GrunnlagsberegningBehovløser.Companion.AVTJENT_VERNEPLIKT
import no.nav.dagpenger.regel.grunnlag.GrunnlagsberegningBehovløser.Companion.BEREGNINGSDATO
import no.nav.dagpenger.regel.grunnlag.GrunnlagsberegningBehovløser.Companion.FANGST_OG_FISKE
import no.nav.dagpenger.regel.grunnlag.GrunnlagsberegningBehovløser.Companion.FORRIGE_GRUNNLAG
import no.nav.dagpenger.regel.grunnlag.GrunnlagsberegningBehovløser.Companion.GRUNNLAG_RESULTAT
import no.nav.dagpenger.regel.grunnlag.GrunnlagsberegningBehovløser.Companion.INNTEKT
import no.nav.dagpenger.regel.grunnlag.GrunnlagsberegningBehovløser.Companion.LÆRLING
import no.nav.dagpenger.regel.grunnlag.GrunnlagsberegningBehovløser.Companion.MANUELT_GRUNNLAG
import no.nav.dagpenger.regel.grunnlag.GrunnlagsberegningBehovløser.Companion.REGELVERKSDATO
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

private val resourceRetriever = object {}.javaClass

class FaktaMapperTest {
    @Test
    fun `greier å parse json`() {
        resourceRetriever.getResource("/test_packet.json")?.readText()!!.let { json ->
            val packet = JsonMessage(json, MessageProblems("")).also {
                it.interestedIn(MANUELT_GRUNNLAG, INNTEKT, FORRIGE_GRUNNLAG)
                shouldNotThrowAny {
                    it.inntekt()
                }
            }
        }
    }

    private val testRapid = TestRapid()

    @Test
    fun `Tar imot packet med beregningsdato`() {
        val behovløser = OnPacketTestListener(testRapid)

        @Language("JSON")
        val json = """{"$BEREGNINGSDATO":"${LocalDate.now()}"}}"""
        testRapid.sendTestMessage(json)
        behovløser.packet shouldNotBe null
    }

    @Test
    fun `Tar ikke imot packet som mangler beregningsdato`() {
        val behovløser = OnPacketTestListener(testRapid)
        val json = """{}"""
        testRapid.sendTestMessage(json)
        behovløser.packet shouldBe null
        behovløser.problems!!.toExtendedReport() shouldContain BEREGNINGSDATO
    }

    @Test
    fun `Tar ikke imot packet som inneholder grunnlagResultat`() {
        val behovløser = OnPacketTestListener(testRapid)
        val json = """{"$GRUNNLAG_RESULTAT":"hubba"}"""
        testRapid.sendTestMessage(json)
        behovløser.packet shouldBe null
        behovløser.problems shouldBe null
    }

    @Test
    fun `Kombinasjon av required og rejected keys`() {
        val behovløser = OnPacketTestListener(testRapid)

        @Language("JSON")
        val json = """{"$BEREGNINGSDATO":"${LocalDate.now()}","$GRUNNLAG_RESULTAT":"hubba"}"""
        testRapid.sendTestMessage(json)
        behovløser.packet shouldBe null
        behovløser.problems shouldBe null
    }

    @Test
    fun `Fangst og fiske blir mappet riktig`() {
        val behovløser = OnPacketTestListener(testRapid)

        testRapid.sendTestMessage("""{"$BEREGNINGSDATO":"${LocalDate.now()}","$FANGST_OG_FISKE":true}""")
        mapToFaktaFrom(behovløser.packet!!).fangstOgFiske shouldBe true

        testRapid.sendTestMessage("""{"$BEREGNINGSDATO":"${LocalDate.now()}","$FANGST_OG_FISKE":false}""")
        mapToFaktaFrom(behovløser.packet!!).fangstOgFiske shouldBe false

        testRapid.sendTestMessage("""{"$BEREGNINGSDATO":"${LocalDate.now()}"}""")
        mapToFaktaFrom(behovløser.packet!!).fangstOgFiske shouldBe false
    }

    @Test
    fun `Avtjent verneplikt blir mappet riktig`() {
        val behovløser = OnPacketTestListener(testRapid)

        testRapid.sendTestMessage("""{"$BEREGNINGSDATO":"${LocalDate.now()}","$AVTJENT_VERNEPLIKT":true}""")
        mapToFaktaFrom(behovløser.packet!!).verneplikt shouldBe true

        testRapid.sendTestMessage("""{"$BEREGNINGSDATO":"${LocalDate.now()}","$AVTJENT_VERNEPLIKT":false}""")
        mapToFaktaFrom(behovløser.packet!!).verneplikt shouldBe false

        testRapid.sendTestMessage("""{"$BEREGNINGSDATO":"${LocalDate.now()}"}""")
        mapToFaktaFrom(behovløser.packet!!).verneplikt shouldBe false
    }

    @Test
    fun `Lærling blir mappet riktig`() {
        val behovløser = OnPacketTestListener(testRapid)

        testRapid.sendTestMessage("""{"$BEREGNINGSDATO":"${LocalDate.now()}","$LÆRLING":true}""")
        mapToFaktaFrom(behovløser.packet!!).lærling shouldBe true

        testRapid.sendTestMessage("""{"$BEREGNINGSDATO":"${LocalDate.now()}","$LÆRLING":false}""")
        mapToFaktaFrom(behovløser.packet!!).lærling shouldBe false

        testRapid.sendTestMessage("""{"$BEREGNINGSDATO":"${LocalDate.now()}"}""")
        mapToFaktaFrom(behovløser.packet!!).lærling shouldBe false
    }

    @Test
    fun `Beregningsdato blir mappet riktig`() {
        val behovløser = OnPacketTestListener(testRapid)

        testRapid.sendTestMessage("""{"$BEREGNINGSDATO":"${LocalDate.MAX}"}""")
        mapToFaktaFrom(behovløser.packet!!).beregningsdato shouldBe LocalDate.MAX

        testRapid.sendTestMessage("""{"$BEREGNINGSDATO":"fqasfas"}""")
        assertThrows<Exception> { mapToFaktaFrom(behovløser.packet!!) }
    }

    @Test
    fun `Regelverksdato blir mappet riktig`() {
        val behovløser = OnPacketTestListener(testRapid)

        //language=JSON
        testRapid.sendTestMessage("""{"$BEREGNINGSDATO": "${LocalDate.MAX}", "$REGELVERKSDATO":"${LocalDate.MIN}"}""")
        mapToFaktaFrom(behovløser.packet!!).regelverksdato shouldBe LocalDate.MIN

        testRapid.sendTestMessage("""{"$BEREGNINGSDATO": "${LocalDate.MAX}", "$REGELVERKSDATO":"s"}""")
        assertThrows<Exception> { mapToFaktaFrom(behovløser.packet!!) }
    }

    @Test
    fun `Dersom regelverksdato mangler brukes beregningsdato`() {
        val behovløser = OnPacketTestListener(testRapid)
        //language=JSON
        testRapid.sendTestMessage("""{"$BEREGNINGSDATO": "${LocalDate.MAX}" }""")
        mapToFaktaFrom(behovløser.packet!!).regelverksdato shouldBe LocalDate.MAX
    }

    @Test
    fun `Parser inntekt riktig`() {
        val behovløser = OnPacketTestListener(testRapid)
        val inntektId = "01HF4BNZTR2F30GR0Q0TCH22KS"

        testRapid.sendTestMessage("""{"$BEREGNINGSDATO": "${LocalDate.MAX}", "$INNTEKT": ${inntektJson(inntektId)} }""")

        mapToFaktaFrom(behovløser.packet!!).inntekt.let { inntekt ->
            requireNotNull(inntekt)
            inntekt.inntektsId shouldBe inntektId
            inntekt.inntektsListe.size shouldBe 2
        }
    }

    @Language("JSON")
    fun inntektJson(inntektId: String) = """
          {
            "inntektsId": "$inntektId",
            "inntektsListe": [
              {
                "årMåned": "2020-10",
                "klassifiserteInntekter": [
                  {
                    "beløp": "40000",
                    "inntektKlasse": "ARBEIDSINNTEKT"
                  }
                ],
                "harAvvik": false
              },
              {
                "årMåned": "2020-11",
                "klassifiserteInntekter": [
                  {
                    "beløp": "40000",
                    "inntektKlasse": "ARBEIDSINNTEKT"
                  }
                ],
                "harAvvik": false
              }
            ],
            "manueltRedigert": false,
            "sisteAvsluttendeKalenderMåned": "2023-09"
          }
    """.trimIndent()

    @Test
    fun `Manuelt grunnlag blir mappet riktig`() {
        val behovløser = OnPacketTestListener(testRapid)

        //language=JSON
        testRapid.sendTestMessage("""{"$BEREGNINGSDATO": "${LocalDate.MAX}", "$MANUELT_GRUNNLAG":${Int.MAX_VALUE}}""")
        mapToFaktaFrom(behovløser.packet!!).manueltGrunnlag shouldBe Int.MAX_VALUE

        assertThrows<NumberFormatException> {
            testRapid.sendTestMessage("""{"$BEREGNINGSDATO": "${LocalDate.MAX}", "$MANUELT_GRUNNLAG":"hubba"}""")
            mapToFaktaFrom(behovløser.packet!!)
        }

        testRapid.sendTestMessage("""{"$BEREGNINGSDATO": "${LocalDate.MAX}"} """)
        mapToFaktaFrom(behovløser.packet!!).manueltGrunnlag shouldBe null
    }
}

private class OnPacketTestListener(rapidsConnection: RapidsConnection) : River.PacketListener {
    var problems: MessageProblems? = null
    var packet: JsonMessage? = null

    init {
        River(rapidsConnection).apply(
            GrunnlagsberegningBehovløser.rapidFilter,
        ).register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        this.packet = packet
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        this.problems = problems
    }
}

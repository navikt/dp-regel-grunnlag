package no.nav.dagpenger.regel.grunnlag

import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.http.HttpStatusCode
import io.mockk.mockk
import io.mockk.verify
import no.nav.dagpenger.grunnbelop.Grunnbeløp
import no.nav.dagpenger.inntekt.v1.Inntekt
import no.nav.dagpenger.inntekt.v1.InntektKlasse
import no.nav.dagpenger.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.inntekt.v1.KlassifisertInntektMåned
import no.nav.dagpenger.regel.grunnlag.GrunnlagsberegningBehovløser.Companion.AVTJENT_VERNEPLIKT
import no.nav.dagpenger.regel.grunnlag.GrunnlagsberegningBehovløser.Companion.BEHOV_ID
import no.nav.dagpenger.regel.grunnlag.GrunnlagsberegningBehovløser.Companion.BEREGNINGSDATO
import no.nav.dagpenger.regel.grunnlag.GrunnlagsberegningBehovløser.Companion.FANGST_OG_FISKE
import no.nav.dagpenger.regel.grunnlag.GrunnlagsberegningBehovløser.Companion.FORRIGE_GRUNNLAG
import no.nav.dagpenger.regel.grunnlag.GrunnlagsberegningBehovløser.Companion.GRUNNLAG_RESULTAT
import no.nav.dagpenger.regel.grunnlag.GrunnlagsberegningBehovløser.Companion.INNTEKT
import no.nav.dagpenger.regel.grunnlag.GrunnlagsberegningBehovløser.Companion.LÆRLING
import no.nav.dagpenger.regel.grunnlag.GrunnlagsberegningBehovløser.Companion.MANUELT_GRUNNLAG
import no.nav.dagpenger.regel.grunnlag.GrunnlagsberegningBehovløser.Companion.PROBLEM
import no.nav.dagpenger.regel.grunnlag.GrunnlagsberegningBehovløser.Companion.REGELVERKSDATO
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.YearMonth

class GrunnlagsberegningBehovløserTest {
    private val testRapid = TestRapid()
    private val fakeGrunnlagInstrumentation = mockk<GrunnlagInstrumentation>(relaxed = true)

    init {
        GrunnlagsberegningBehovløser(testRapid, fakeGrunnlagInstrumentation)
    }

    @Test
    fun `Beregnet grunnlag skal være negativt dersom summerte inntekter er negativt`() {
        val inntekt = getInntekt((-1000).toBigDecimal())
        val testMessage =
            JsonMessage.newMessage(
                map =
                    mapOf(
                        BEHOV_ID to "behovId",
                        BEREGNINGSDATO to "2018-08-10",
                        AVTJENT_VERNEPLIKT to false,
                        FANGST_OG_FISKE to false,
                        INNTEKT to inntekt.toMap(),
                    ),
            ).toJson()

        testRapid.sendTestMessage(testMessage)
        val resultat = testRapid.inspektør.message(0)

        resultat.hasNonNull(GRUNNLAG_RESULTAT) shouldBe true
        resultat[GRUNNLAG_RESULTAT]["beregningsregel"].asText() shouldBe "ArbeidsinntektSiste36(2021)"
        resultat[GRUNNLAG_RESULTAT]["avkortet"].asInt() < 0
    }

    @Test
    fun `Beregnet grunnlag skal være 0 dersom summerte inntekter er 0`() {
        val inntekt = getInntekt((0).toBigDecimal())
        val testMessage =
            JsonMessage.newMessage(
                map =
                    mapOf(
                        BEHOV_ID to "behovId",
                        BEREGNINGSDATO to "2018-08-10",
                        AVTJENT_VERNEPLIKT to false,
                        FANGST_OG_FISKE to false,
                        INNTEKT to inntekt.toMap(),
                    ),
            ).toJson()

        testRapid.sendTestMessage(testMessage)
        val resultat = testRapid.inspektør.message(0)

        resultat.hasNonNull(GRUNNLAG_RESULTAT) shouldBe true
        resultat[GRUNNLAG_RESULTAT]["beregningsregel"].asText() shouldBe "ArbeidsinntektSiste12(2021)"
        resultat[GRUNNLAG_RESULTAT]["avkortet"].asInt() shouldBe 0
    }

    @Test
    fun `Beregn med regel ArbeidsinntektSiste12(2021) når bruker kun har inntekt siste 12 mnd`() {
        val inntekt = getInntekt(1000.toBigDecimal())

        val testMessage =
            JsonMessage.newMessage(
                map =
                    mapOf(
                        BEHOV_ID to "behovId",
                        BEREGNINGSDATO to "2018-08-10",
                        AVTJENT_VERNEPLIKT to false,
                        FANGST_OG_FISKE to false,
                        INNTEKT to inntekt.toMap(),
                    ),
            ).toJson()

        testRapid.sendTestMessage(testMessage)
        val resultat = testRapid.inspektør.message(0)

        resultat.hasNonNull(GRUNNLAG_RESULTAT) shouldBe true
        resultat[GRUNNLAG_RESULTAT]["beregningsregel"].asText() shouldBe "ArbeidsinntektSiste12(2021)"
        resultat[GRUNNLAG_RESULTAT]["avkortet"].asInt() shouldBe 3035
        resultat[GRUNNLAG_RESULTAT]["uavkortet"].asInt() shouldBe 3035
        resultat[GRUNNLAG_RESULTAT]["harAvkortet"].asBoolean() shouldBe false
    }

    @Suppress("ktlint:standard:max-line-length")
    @Test
    fun `Skal beregne med Verneplikt-regel dersom bruker har avtjent verneplikt siste 12 måneder og det ikke lønner seg å velge annen beregningsregel`() {
        val inntekt = getInntekt((1000).toBigDecimal())
        val testMessage =
            JsonMessage.newMessage(
                map =
                    mapOf(
                        BEHOV_ID to "behovId",
                        BEREGNINGSDATO to "2018-08-10",
                        REGELVERKSDATO to "2021-03-16",
                        AVTJENT_VERNEPLIKT to true,
                        INNTEKT to inntekt.toMap(),
                    ),
            )

        testRapid.sendTestMessage(testMessage.toJson())
        val resultat = testRapid.inspektør.message(0)

        resultat.hasNonNull(GRUNNLAG_RESULTAT) shouldBe true
        val treGangerGrunnbeløpFastsattI2020 = (Grunnbeløp.FastsattI2020.verdi * BigDecimal(3)).toInt()
        resultat[GRUNNLAG_RESULTAT]["avkortet"].asInt() shouldBe treGangerGrunnbeløpFastsattI2020
        resultat[GRUNNLAG_RESULTAT]["uavkortet"].asInt() shouldBe treGangerGrunnbeløpFastsattI2020
        resultat[GRUNNLAG_RESULTAT]["beregningsregel"].asText() shouldBe "Verneplikt"
        resultat[GRUNNLAG_RESULTAT]["harAvkortet"].asBoolean() shouldBe false
    }

    @Test
    fun `Skal velge beregningsregel ForrigeGrunnlag dersom bruk av forrige grunnlag er angitt som input`() {
        val forrigeGrunnlag = 290000
        val testMessage =
            JsonMessage.newMessage(
                map =
                    mapOf(
                        BEHOV_ID to "behovId",
                        BEREGNINGSDATO to "2020-03-20",
                        FANGST_OG_FISKE to false,
                        LÆRLING to false,
                        FORRIGE_GRUNNLAG to forrigeGrunnlag,
                    ),
            )

        testRapid.sendTestMessage(testMessage.toJson())
        val resultat = testRapid.inspektør.message(0)

        resultat.hasNonNull(GRUNNLAG_RESULTAT) shouldBe true
        resultat[GRUNNLAG_RESULTAT]["avkortet"].asInt() shouldBe forrigeGrunnlag
        resultat[GRUNNLAG_RESULTAT]["uavkortet"].asInt() shouldBe forrigeGrunnlag
        resultat[GRUNNLAG_RESULTAT]["beregningsregel"].asText() shouldBe "ForrigeGrunnlag"
        resultat[GRUNNLAG_RESULTAT]["harAvkortet"].asBoolean() shouldBe false
    }

    @Test
    fun `Skal velge beregningsregel LærlingArbeidsinntekt1x12`() {
        val inntekt = getInntekt(1000.toBigDecimal(), YearMonth.of(2020, 3))

        val testMessage =
            JsonMessage.newMessage(
                map =
                    mapOf(
                        BEHOV_ID to "behovId",
                        BEREGNINGSDATO to "2020-03-20",
                        LÆRLING to true,
                        INNTEKT to inntekt.toMap(),
                    ),
            ).toJson()

        testRapid.sendTestMessage(testMessage)

        val resultat = testRapid.inspektør.message(0)

        resultat.hasNonNull(GRUNNLAG_RESULTAT) shouldBe true
        resultat[GRUNNLAG_RESULTAT]["avkortet"].asInt() shouldBe resultat[GRUNNLAG_RESULTAT]["uavkortet"].asInt()
        resultat[GRUNNLAG_RESULTAT]["beregningsregel"].asText() shouldBe "LærlingArbeidsinntekt1x12"
        resultat[GRUNNLAG_RESULTAT]["harAvkortet"].asBoolean() shouldBe false
    }

    @Test
    fun `Skal velge beregningsregel ForrigeGrunnlag når man har angitt forrige grunnlag for lærling`() {
        val forrigeGrunnlag = 300000
        val testMessage =
            JsonMessage.newMessage(
                map =
                    mapOf(
                        BEHOV_ID to "behovId",
                        BEREGNINGSDATO to "2020-03-20",
                        LÆRLING to true,
                        FORRIGE_GRUNNLAG to forrigeGrunnlag,
                    ),
            ).toJson()

        testRapid.sendTestMessage(testMessage)
        val resultat = testRapid.inspektør.message(0)

        resultat.hasNonNull(GRUNNLAG_RESULTAT) shouldBe true
        resultat[GRUNNLAG_RESULTAT]["beregningsregel"].asText() shouldBe "ForrigeGrunnlag"
        resultat[GRUNNLAG_RESULTAT]["harAvkortet"].asBoolean() shouldBe false
        resultat[GRUNNLAG_RESULTAT]["avkortet"].asInt() shouldBe forrigeGrunnlag
    }

    @Test
    fun `Skal velge beregningsregel Manuell`() {
        val manueltGrunnlag = 300000
        val testMessage =
            JsonMessage.newMessage(
                map =
                    mapOf(
                        BEHOV_ID to "behovId",
                        BEREGNINGSDATO to "2020-03-20",
                        MANUELT_GRUNNLAG to manueltGrunnlag,
                    ),
            ).toJson()

        testRapid.sendTestMessage(testMessage)
        val resultat = testRapid.inspektør.message(0)

        resultat.hasNonNull(GRUNNLAG_RESULTAT) shouldBe true
        resultat[GRUNNLAG_RESULTAT]["beregningsregel"].asText() shouldBe "Manuell"
        resultat[GRUNNLAG_RESULTAT]["harAvkortet"].asBoolean() shouldBe false
        resultat[GRUNNLAG_RESULTAT]["avkortet"].asInt() shouldBe manueltGrunnlag
    }

    @Test
    fun `Skal velge beregningsregel Manuell og harAvkortet ved manuelt grunnlag over 6G`() {
        val manueltGrunnlag = 900000
        val testMessage =
            JsonMessage.newMessage(
                map =
                    mapOf(
                        BEHOV_ID to "behovId",
                        BEREGNINGSDATO to "2020-03-20",
                        MANUELT_GRUNNLAG to manueltGrunnlag,
                    ),
            ).toJson()

        testRapid.sendTestMessage(testMessage)
        val resultat = testRapid.inspektør.message(0)

        resultat.hasNonNull(GRUNNLAG_RESULTAT) shouldBe true
        resultat[GRUNNLAG_RESULTAT]["beregningsregel"].asText() shouldBe "Manuell"
        resultat[GRUNNLAG_RESULTAT]["harAvkortet"].asBoolean() shouldBe true
        resultat[GRUNNLAG_RESULTAT]["uavkortet"].asInt() shouldBe manueltGrunnlag
        resultat[GRUNNLAG_RESULTAT]["avkortet"].asInt() shouldBeLessThan manueltGrunnlag
    }

    @Test
    fun `Skal instrumentere beregninger`() {
        val inntekt = getInntekt(1000.toBigDecimal())
        val testMessage =
            JsonMessage.newMessage(
                map =
                    mapOf(
                        BEHOV_ID to "behovId",
                        BEREGNINGSDATO to "2018-08-10",
                        AVTJENT_VERNEPLIKT to true,
                        INNTEKT to inntekt.toMap(),
                    ),
            ).toJson()

        testRapid.sendTestMessage(testMessage)

        verify {
            fakeGrunnlagInstrumentation.grunnlagBeregnet(
                regelIdentifikator = ofType(String::class),
                fakta = ofType(Fakta::class),
                resultat = ofType(GrunnlagResultat::class),
            )
        }
    }

    @Test
    fun `Packet fører ikke til løsning ved mangel av inntekt, manuelt grunnlag og forrige grunnlag`() {
        testRapid.sendTestMessage(
            JsonMessage.newMessage(
                mapOf(
                    BEHOV_ID to "behovId",
                    BEREGNINGSDATO to "2018-08-10",
                ),
            ).toJson(),
        )
        testRapid.inspektør.size shouldBe 0
    }

    @Test
    fun `Packet fører ikke til løsning ved både manuelt og forrige grunnlag`() {
        // TODO: Burde dette kaste en exception?
        testRapid.sendTestMessage(
            JsonMessage.newMessage(
                mapOf(
                    BEHOV_ID to "behovId",
                    BEREGNINGSDATO to "2018-08-10",
                    MANUELT_GRUNNLAG to "200000",
                    FORRIGE_GRUNNLAG to "700000",
                ),
            ).toJson(),
        )
        val resultatPacket = testRapid.inspektør.message(0)
    }

    @Test
    fun `Kaster exception når packet inneholder både inntekt og manuelt grunnlag`() {
        assertThrows<ManueltGrunnlagOgInntektException> {
            testRapid.sendTestMessage(
                JsonMessage.newMessage(
                    mapOf(
                        BEHOV_ID to "behovId",
                        BEREGNINGSDATO to "2018-08-10",
                        MANUELT_GRUNNLAG to "600000",
                        INNTEKT to getInntekt(1000.toBigDecimal()),
                    ),
                ).toJson(),
            )
        }
        val resultatPacket = testRapid.inspektør.message(0)
        resultatPacket[PROBLEM] shouldNotBe null
        resultatPacket[PROBLEM]["status"].asInt() shouldBe HttpStatusCode.InternalServerError.value
    }

    @Test
    fun `Kaster exception når packet inneholder både inntekt, manuelt grunnlag og forrige grunnlag`() {
        assertThrows<ManueltGrunnlagOgInntektException> {
            testRapid.sendTestMessage(
                JsonMessage.newMessage(
                    mapOf(
                        BEHOV_ID to "behovId",
                        BEREGNINGSDATO to "2018-08-10",
                        MANUELT_GRUNNLAG to "600000",
                        FORRIGE_GRUNNLAG to "600000",
                        INNTEKT to getInntekt(1000.toBigDecimal()),
                    ),
                ).toJson(),
            )
        }
        val resultatPacket = testRapid.inspektør.message(0)
        resultatPacket[PROBLEM] shouldNotBe null
        resultatPacket[PROBLEM]["status"].asInt() shouldBe HttpStatusCode.InternalServerError.value
    }

    @Test
    fun `Kaster exception når packet inneholder både inntekt og forrige grunnlag`() {
        assertThrows<ForrigeGrunnlagOgInntektException> {
            testRapid.sendTestMessage(
                JsonMessage.newMessage(
                    mapOf(
                        BEHOV_ID to "behovId",
                        BEREGNINGSDATO to "2018-08-10",
                        FORRIGE_GRUNNLAG to "600000",
                        INNTEKT to getInntekt(1000.toBigDecimal()),
                    ),
                ).toJson(),
            )
        }
        val resultatPacket = testRapid.inspektør.message(0)
        resultatPacket[PROBLEM] shouldNotBe null
        resultatPacket[PROBLEM]["status"].asInt() shouldBe HttpStatusCode.InternalServerError.value
    }

    private fun getInntekt(
        månedsbeløp: BigDecimal,
        inntektsdatoStart: YearMonth? = null,
    ): Inntekt {
        return Inntekt(
            inntektsId = "12345",
            inntektsListe =
                listOf(
                    KlassifisertInntektMåned(
                        årMåned = inntektsdatoStart ?: YearMonth.of(2018, 4),
                        klassifiserteInntekter =
                            listOf(
                                KlassifisertInntekt(
                                    beløp = månedsbeløp,
                                    inntektKlasse = InntektKlasse.ARBEIDSINNTEKT,
                                ),
                            ),
                    ),
                    KlassifisertInntektMåned(
                        inntektsdatoStart?.plusMonths(1) ?: YearMonth.of(2018, 5),
                        listOf(
                            KlassifisertInntekt(
                                månedsbeløp,
                                InntektKlasse.ARBEIDSINNTEKT,
                            ),
                            KlassifisertInntekt(
                                månedsbeløp,
                                InntektKlasse.ARBEIDSINNTEKT,
                            ),
                        ),
                    ),
                ),
            sisteAvsluttendeKalenderMåned = YearMonth.of(2018, 7),
        )
    }

    private fun Inntekt.toMap(): Map<*, *> {
        return objectMapper.convertValue(this, Map::class.java)
    }
}

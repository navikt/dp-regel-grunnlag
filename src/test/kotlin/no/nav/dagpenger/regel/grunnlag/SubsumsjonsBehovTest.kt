package no.nav.dagpenger.regel.grunnlag

import org.json.JSONException
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import kotlin.test.assertEquals

class SubsumsjonsBehovTest {

    fun jsonToBehov(json: String): SubsumsjonsBehov =
        SubsumsjonsBehov(JsonDeserializer().deserialize("", json.toByteArray()) ?: JSONObject())

    val emptyjsonBehov = """
            {}
            """.trimIndent()

    val jsonBehovMedInntekt = """
        {
            "inntektV1": {
                "inntektsId": "12345",
                "inntektsListe": [
                    {
                        "årMåned": "2018-03",
                        "klassifiserteInntekter": [
                            {
                                "beløp": "25000",
                                "inntektKlasse": "ARBEIDSINNTEKT"
                            }
                        ]
                    }
                ]
            }
            }
            """.trimIndent()

    val jsonBehovMedGrunnlagResultat = """
            {
                "grunnlagResultat": {}
            }
            """.trimIndent()

    val jsonBehovMedHentInntektTask = """
            {
                "tasks": ["hentInntekt"]
            }
            """.trimIndent()

    val jsonBehovMedAnnenTask = """
            {
                "tasks": ["annen task"]
            }
            """.trimIndent()

    val jsonBehovMedFlereTasks = """
            {
                "tasks": ["annen task", "hentInntekt"]
            }
            """.trimIndent()

    val jsonBehovMedInntektogGrunnlagResultat = """
            {
                "inntektV1": 0,
                "grunnlagResultat": {}
            }
            """.trimIndent()

    val jsonBehovMedInntektogHentInntektTask = """
            {
                "inntektV1": {"inntektsId": "", "inntekt": 0},
                "tasks": ["hentInntekt"]
            }
            """.trimIndent()

    val jsonBehovMedVernepliktTrue = """
            {
                "harAvtjentVerneplikt": true
            }
            """.trimIndent()

    val jsonBehovMedVernepliktFalse = """
            {
                "harAvtjentVerneplikt": false
            }
            """.trimIndent()

    @Test
    fun ` Should need hentInntektsTask when there is no hentInntektsTask and no inntekt `() {

        assert(jsonToBehov(emptyjsonBehov).needsHentInntektsTask())
        assertFalse(jsonToBehov(jsonBehovMedInntekt).needsHentInntektsTask())
        assertFalse(jsonToBehov(jsonBehovMedHentInntektTask).needsHentInntektsTask())
        assertFalse(jsonToBehov(jsonBehovMedInntektogHentInntektTask).needsHentInntektsTask())
    }

    @Test
    fun ` Should need grunnlagResultat when there is inntekt and no grunnlagResultat `() {

        assert(jsonToBehov(jsonBehovMedInntekt).needsGrunnlagResultat())
        assertFalse(jsonToBehov(emptyjsonBehov).needsGrunnlagResultat())
        assertFalse(jsonToBehov(jsonBehovMedInntektogGrunnlagResultat).needsGrunnlagResultat())
        assertFalse(jsonToBehov(jsonBehovMedGrunnlagResultat).needsGrunnlagResultat())
    }

    @Test
    fun ` Should have grunnlagResultat when it has grunnlagResultat `() {

        assert(jsonToBehov(jsonBehovMedGrunnlagResultat).hasGrunnlagResultat())
        assertFalse(jsonToBehov(emptyjsonBehov).hasGrunnlagResultat())
    }

    @Test
    fun ` Should have inntekt when it has inntekt `() {

        assert(jsonToBehov(jsonBehovMedInntekt).hasInntekt())
        assertFalse(jsonToBehov(emptyjsonBehov).hasInntekt())
    }

    @Test
    fun ` Should have hentInntektTask when it has hentInntektTask `() {

        assert(jsonToBehov(jsonBehovMedHentInntektTask).hasHentInntektTask())
        assert(jsonToBehov(jsonBehovMedFlereTasks).hasHentInntektTask())
        assertFalse(jsonToBehov(emptyjsonBehov).hasHentInntektTask())
        assertFalse(jsonToBehov(jsonBehovMedAnnenTask).hasHentInntektTask())
    }

    @Test
    fun ` Should have tasks when it has tasks `() {

        assert(jsonToBehov(jsonBehovMedHentInntektTask).hasTasks())
        assert(jsonToBehov(jsonBehovMedAnnenTask).hasTasks())
        assert(jsonToBehov(jsonBehovMedFlereTasks).hasTasks())
        assertFalse(jsonToBehov(emptyjsonBehov).hasTasks())
    }

    @Test
    fun ` Should be able to add tasks `() {
        val subsumsjonsBehov = jsonToBehov(emptyjsonBehov)

        assertFalse(subsumsjonsBehov.hasTasks())

        subsumsjonsBehov.addTask("Annen Task")

        assert(subsumsjonsBehov.hasTasks())
        assertFalse(subsumsjonsBehov.hasHentInntektTask())

        subsumsjonsBehov.addTask("hentInntekt")

        assert(subsumsjonsBehov.hasTasks())
        assert(subsumsjonsBehov.hasHentInntektTask())
    }

    @Test
    fun ` Should be able to return verneplikt `() {

        assert(jsonToBehov(jsonBehovMedVernepliktTrue).harAvtjentVerneplikt())
        assertFalse(jsonToBehov(jsonBehovMedVernepliktFalse).harAvtjentVerneplikt())
        assertFalse(jsonToBehov(emptyjsonBehov).harAvtjentVerneplikt())
    }

    @Test
    fun ` Should be able to add grunnlagResultat `() {
        val subsumsjonsBehov = jsonToBehov(emptyjsonBehov)

        assertFalse(subsumsjonsBehov.hasGrunnlagResultat())

        val grunnlagSubsumsjon = GrunnlagResultat("123", "456", "REGEL", BigDecimal(500), BigDecimal(600))
        subsumsjonsBehov.addGrunnlagResultat(grunnlagSubsumsjon)

        assert(subsumsjonsBehov.hasGrunnlagResultat())
    }

    @Test
    fun ` Should be able to return inntekt `() {

        assertEquals("12345", jsonToBehov(jsonBehovMedInntekt).getInntekt().inntektsId)
        assertThrows<JSONException> { jsonToBehov(emptyjsonBehov).getInntekt() }
    }
}

package no.nav.dagpenger.regel.grunnlag

import org.json.JSONException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class SubsumsjonsBehovTest {

    val emptyjsonBehov = """
            {}
            """.trimIndent()
    val emptyjsonObject = JsonDeserializer().deserialize(null, emptyjsonBehov.toByteArray())!!
    val emptysubsumsjonsBehov = SubsumsjonsBehov(emptyjsonObject)

    val jsonBehovMedInntekt = """
            {
                "inntekt": {"inntektsId": "", "inntekt": 0}
            }
            """.trimIndent()
    val jsonObjectMedInntekt = JsonDeserializer().deserialize(null, jsonBehovMedInntekt.toByteArray())!!
    val subsumsjonsBehovMedInntekt = SubsumsjonsBehov(jsonObjectMedInntekt)

    val jsonBehovMedGrunnlagResultat = """
            {
                "grunnlagResultat": {}
            }
            """.trimIndent()
    val jsonObjectMedGrunnlagResultat = JsonDeserializer().deserialize(null, jsonBehovMedGrunnlagResultat.toByteArray())!!
    val subsumsjonsBehovMedGrunnlagResultat = SubsumsjonsBehov(jsonObjectMedGrunnlagResultat)

    val jsonBehovMedHentInntektTask = """
            {
                "tasks": ["hentInntekt"]
            }
            """.trimIndent()
    val jsonObjectMedHentInntektTask = JsonDeserializer().deserialize(null, jsonBehovMedHentInntektTask.toByteArray())!!
    val subsumsjonsBehovMedHentInntektTask = SubsumsjonsBehov(jsonObjectMedHentInntektTask)

    val jsonBehovMedAnnenTask = """
            {
                "tasks": ["annen task"]
            }
            """.trimIndent()
    val jsonObjectMedAnnenTask = JsonDeserializer().deserialize(null, jsonBehovMedAnnenTask.toByteArray())!!
    val subsumsjonsBehovAnnentTask = SubsumsjonsBehov(jsonObjectMedAnnenTask)

    val jsonBehovMedFlereTasks = """
            {
                "tasks": ["annen task", "hentInntekt"]
            }
            """.trimIndent()
    val jsonObjectMedFlereTasks = JsonDeserializer().deserialize(null, jsonBehovMedFlereTasks.toByteArray())!!
    val subsumsjonsBehovFleretTasks = SubsumsjonsBehov(jsonObjectMedFlereTasks)

    val jsonBehovMedInntektogGrunnlagResultat = """
            {
                "inntekt": 0,
                "grunnlagResultat": {}
            }
            """.trimIndent()
    val jsonObjectMedInntektogGrunnlagResultat = JsonDeserializer().deserialize(null, jsonBehovMedInntektogGrunnlagResultat.toByteArray())!!
    val subsumsjonsBehovMedInntektogGrunnlagResultat = SubsumsjonsBehov(jsonObjectMedInntektogGrunnlagResultat)

    val jsonBehovMedInntektogHentInntektTask = """
            {
                "inntekt": {"inntektsId": "", "inntekt": 0},
                "tasks": ["hentInntekt"]
            }
            """.trimIndent()
    val jsonObjectMedInntektogHentInntektTask = JsonDeserializer().deserialize(null, jsonBehovMedInntektogHentInntektTask.toByteArray())!!
    val subsumsjonsBehovMedInntektogHentInntektTask = SubsumsjonsBehov(jsonObjectMedInntektogHentInntektTask)

    val jsonBehovMedVernepliktTrue = """
            {
                "harAvtjentVerneplikt": true
            }
            """.trimIndent()
    val jsonObjectMedVernepliktTrue = JsonDeserializer().deserialize(null, jsonBehovMedVernepliktTrue.toByteArray())!!
    val subsumsjonsBehovmedVernepliktTrue = SubsumsjonsBehov(jsonObjectMedVernepliktTrue)

    val jsonBehovMedVernepliktFalse = """
            {
                "harAvtjentVerneplikt": false
            }
            """.trimIndent()
    val jsonObjectMedVernepliktFalse = JsonDeserializer().deserialize(null, jsonBehovMedVernepliktFalse.toByteArray())!!
    val subsumsjonsBehovmedVernepliktFalse = SubsumsjonsBehov(jsonObjectMedVernepliktFalse)

    @Test
    fun ` Should need hentInntektsTask when there is no hentInntektsTask and no inntekt `() {

        assert(emptysubsumsjonsBehov.needsHentInntektsTask())
        Assertions.assertFalse(subsumsjonsBehovMedInntekt.needsHentInntektsTask())
        Assertions.assertFalse(subsumsjonsBehovMedHentInntektTask.needsHentInntektsTask())
        Assertions.assertFalse(subsumsjonsBehovMedInntektogHentInntektTask.needsHentInntektsTask())
    }

    @Test
    fun ` Should need grunnlagResultat when there is inntekt and no grunnlagResultat `() {

        assert(subsumsjonsBehovMedInntekt.needsGrunnlagResultat())
        Assertions.assertFalse(emptysubsumsjonsBehov.needsGrunnlagResultat())
        Assertions.assertFalse(subsumsjonsBehovMedInntektogGrunnlagResultat.needsGrunnlagResultat())
        Assertions.assertFalse(subsumsjonsBehovMedGrunnlagResultat.needsGrunnlagResultat())
    }

    @Test
    fun ` Should have grunnlagResultat when it has grunnlagResultat `() {

        assert(subsumsjonsBehovMedGrunnlagResultat.hasGrunnlagResultat())
        Assertions.assertFalse(emptysubsumsjonsBehov.hasGrunnlagResultat())
    }

    @Test
    fun ` Should have inntekt when it has inntekt `() {

        assert(subsumsjonsBehovMedInntekt.hasInntekt())
        Assertions.assertFalse(emptysubsumsjonsBehov.hasInntekt())
    }

    @Test
    fun ` Should have hentInntektTask when it has hentInntektTask `() {

        assert(subsumsjonsBehovMedHentInntektTask.hasHentInntektTask())
        assert(subsumsjonsBehovFleretTasks.hasHentInntektTask())
        Assertions.assertFalse(emptysubsumsjonsBehov.hasHentInntektTask())
        Assertions.assertFalse(subsumsjonsBehovAnnentTask.hasHentInntektTask())
    }

    @Test
    fun ` Should have tasks when it has tasks `() {

        assert(subsumsjonsBehovMedHentInntektTask.hasTasks())
        assert(subsumsjonsBehovAnnentTask.hasTasks())
        assert(subsumsjonsBehovFleretTasks.hasTasks())
        Assertions.assertFalse(emptysubsumsjonsBehov.hasTasks())
    }

    @Test
    fun ` Should be able to add tasks `() {
        val subsumsjonsBehov = emptysubsumsjonsBehov

        Assertions.assertFalse(subsumsjonsBehov.hasTasks())

        subsumsjonsBehov.addTask("Annen Task")

        assert(subsumsjonsBehov.hasTasks())
        Assertions.assertFalse(subsumsjonsBehov.hasHentInntektTask())

        subsumsjonsBehov.addTask("hentInntekt")

        assert(subsumsjonsBehov.hasTasks())
        assert(subsumsjonsBehov.hasHentInntektTask())
    }

    @Test
    fun ` Should be able to return verneplikt `() {

        assert(subsumsjonsBehovmedVernepliktTrue.harAvtjentVerneplikt())
        Assertions.assertFalse(subsumsjonsBehovmedVernepliktFalse.harAvtjentVerneplikt())
        Assertions.assertFalse(emptysubsumsjonsBehov.harAvtjentVerneplikt())
    }

    @Test
    fun ` Should be able to add grunnlagResultat `() {
        val subsumsjonsBehov = emptysubsumsjonsBehov

        Assertions.assertFalse(subsumsjonsBehov.hasGrunnlagResultat())

        val grunnlagSubsumsjon = GrunnlagResultat("123", "456", "REGEL", 500, 600)
        subsumsjonsBehov.addGrunnlagResultat(grunnlagSubsumsjon)

        assert(subsumsjonsBehov.hasGrunnlagResultat())
    }

    @Test
    fun ` Should be able to return inntekt `() {

        assertEquals(0, subsumsjonsBehovMedInntekt.getInntekt().inntektValue)
        assertThrows<JSONException> { emptysubsumsjonsBehov.getInntekt() }
    }
}

package no.nav.dagpenger.regel.grunnlag

import org.json.JSONException
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class SubsumsjonsBehovTest {
    fun jsonToBehov(json: String): SubsumsjonsBehov =
        SubsumsjonsBehov(JsonDeserializer().deserialize("", json.toByteArray()) ?: JSONObject())

    @Test
    fun `needsHentInntektsTask returns false when behov already has a task for hentInntekt`() {
        val jsonWithHentinntektTask = """
            {
                "otherField": "awe",
                "tasks": ["hentInntekt"]
            }
        """.trimIndent()

        assertFalse(jsonToBehov(jsonWithHentinntektTask).needsHentInntektsTask())
    }

    @Test
    fun `needsHentInntektsTask returns false if behov has inntekt `() {

        val jsonWithInntekt = """
            {
                "otherField": "awe",
                "inntekt": 50151
            }
        """.trimIndent()

        assertFalse(jsonToBehov(jsonWithInntekt).needsHentInntektsTask())
    }

    @Test
    fun `needsHentInntektsTask returns true if behov is missing both inntekt and hentInntektTask`() {
        val json = """
            {
                "otherField": "awe",
            }
        """.trimIndent()

        assertTrue(jsonToBehov(json).needsHentInntektsTask())
    }

    @Test
    fun `needsGrunnlagResult returns false if behov is missing inntekt`() {
        val json = """
            {
                "otherField": "awe",
            }
        """.trimIndent()

        assertFalse(jsonToBehov(json).needsGrunnlagResultat())
    }

    @Test
    fun `needsGrunnlagResult returns false if behov already has grunnlagResultat`() {
        val json = """
            {
                "otherField": "awe",
                "grunnlagResultat": "qwe"
            }
        """.trimIndent()

        assertFalse(jsonToBehov(json).needsGrunnlagResultat())
    }

    @Test
    fun `needsGrunnlagResult returns true if behov has inntekt and no grunnlagResultat`() {
        val json = """
            {
                "otherField": "awe",
                "inntekt": 0
            }
        """.trimIndent()

        assertTrue(jsonToBehov(json).needsGrunnlagResultat())
    }

    @Test
    fun `hasInntektTask returns true when behov has task named hasInntekt`() {
        val json = """
            {
                "otherField": "awe",
                "tasks": ["othertask", "hentInntekt"]
            }
        """.trimIndent()

        assertTrue(jsonToBehov(json).hasHentInntektTask())
    }

    @Test
    fun `hasInntektTask returns false when behov has no task named hasInntekt`() {
        val json = """
            {
                "otherField": "awe",
                "tasks": ["otherTask"]
            }
        """.trimIndent()

        assertFalse(jsonToBehov(json).hasHentInntektTask())
    }

    @Test
    fun `hasGrunnlagResultat returns false when json has no field named grunnlagResultat`() {
        val json = """
            {
                "otherField": "awe"
            }
        """.trimIndent()

        assertFalse(jsonToBehov(json).hasGrunnlagResultat())
    }

    @Test
    fun `hasGrunnlagResultat returns true when json has field named grunnlagResultat`() {
        val json = """
            {
                "otherField": "awe",
                "grunnlagResultat": {}
            }
        """.trimIndent()

        assertTrue(jsonToBehov(json).hasGrunnlagResultat())
    }

    @Test
    fun `hasTasks returns true when json has field named tasks`() {
        val json = """
            {
                "otherField": "awe",
                tasks: []
            }
        """.trimIndent()

        assertTrue(jsonToBehov(json).hasTasks())
    }

    @Test
    fun `hasTasks returns false when json is missing field named tasks`() {
        val json = """
            {
                "otherField": "awe",
            }
        """.trimIndent()

        assertFalse(jsonToBehov(json).hasTasks())
    }

    @Test
    fun `addTask should add task`() {
        val json = """
            {
                "otherField": "awe",
            }
        """.trimIndent()

        val behov = jsonToBehov(json)
        behov.addTask("some task")

        assertTrue(behov.hasTasks())
        assertTrue("some task" in behov.jsonObject.getJSONArray("tasks"))
    }

    @Test
    fun `addTask should not overwrite other tasks`() {
        val json = """
            {
                "otherField": "awe",
                "tasks": ["other task"]
            }
        """.trimIndent()

        val behov = jsonToBehov(json)
        behov.addTask("some task")

        assertTrue(behov.hasTasks())
        assertTrue("some task" in behov.jsonObject.getJSONArray("tasks"))
        assertTrue("other task" in behov.jsonObject.getJSONArray("tasks"))
    }

    @Test
    fun `getInntekt should return inntekt from json`() {
        val json = """
            {
                "otherField": "awe",
                "inntekt": 500
            }
        """.trimIndent()

        assertEquals(500, jsonToBehov(json).getInntekt())
    }

    @Test
    fun `getInntekt should throw JSONException if no inntekt in json`() {
        val json = """
            {
                "otherField": "awe",
            }
        """.trimIndent()

        assertThrows<JSONException> { jsonToBehov(json).getInntekt() }
    }

    @Test
    fun `addGrunnlagResultat should add grunnlagResultat to json`() {
        val json = """
            {
                "otherField": "awe",
            }
        """.trimIndent()

        val behov = jsonToBehov(json)

        behov.addGrunnlagResultat(
            SubsumsjonsBehov.GrunnlagResultat(
                "aaa",
                "bbb",
                "Grunnlag.v1",
                500
            )
        )

        assertTrue(behov.hasGrunnlagResultat())
        assertEquals("aaa", behov.jsonObject.getJSONObject("grunnlagResultat").getString("sporingsId"))
        assertEquals("bbb", behov.jsonObject.getJSONObject("grunnlagResultat").getString("subsumsjonsId"))
        assertEquals("Grunnlag.v1", behov.jsonObject.getJSONObject("grunnlagResultat").getString("regelIdentifikator"))
        assertEquals(500, behov.jsonObject.getJSONObject("grunnlagResultat").getInt("dagpengeGrunnlag"))
    }
}

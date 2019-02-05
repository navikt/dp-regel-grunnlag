package no.nav.dagpenger.regel.grunnlag

import org.json.JSONObject

data class SubsumsjonsBehov(val jsonObject: JSONObject) {

    fun needsHentInntektsTask(): Boolean = !hasInntekt() && !hasHentInntektTask()

    fun needsGrunnlagSubsumsjon(): Boolean = hasInntekt() && !hasGrunnlagSubsumsjon()

    private fun hasInntekt() = jsonObject.has("inntekt")

    fun hasHentInntektTask(): Boolean {
        if (jsonObject.has("tasks")) {
            val tasks = jsonObject.getJSONArray("tasks")
            for (task in tasks) {
                if (task.toString() == "hentInntekt") {
                    return true
                }
            }
        }
        return false
    }

    private fun hasGrunnlagSubsumsjon() = jsonObject.has("grunnlagSubsumsjon")

    fun hasTasks(): Boolean = jsonObject.has("tasks")
}

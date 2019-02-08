package no.nav.dagpenger.regel.grunnlag

import org.json.JSONObject

data class SubsumsjonsBehov(val jsonObject: JSONObject) {

    companion object {
        val GRUNNLAG_RESULTAT = "grunnlagResultat"
        val INNTEKT = "inntekt"
        val TASKS = "tasks"
        val TASKS_HENT_INNTEKT = "hentInntekt"

        val SPORINGSID = "sporingsId"
        val SUBSUMSJONSID = "subsumsjonsId"
        val REGELIDENTIFIKATOR = "regelIdentifikator"
        val DAGPENGEGRUNNLAG = "dagpengeGrunnlag"
    }

    fun needsHentInntektsTask(): Boolean = !hasInntekt() && !hasHentInntektTask()

    fun needsGrunnlagResultat(): Boolean = hasInntekt() && !hasGrunnlagResultat()

    private fun hasInntekt() = jsonObject.has(INNTEKT)

    fun hasHentInntektTask(): Boolean {
        if (jsonObject.has(TASKS)) {
            val tasks = jsonObject.getJSONArray(TASKS)
            for (task in tasks) {
                if (task.toString() == TASKS_HENT_INNTEKT) {
                    return true
                }
            }
        }
        return false
    }

    fun hasGrunnlagResultat() = jsonObject.has(GRUNNLAG_RESULTAT)

    fun hasTasks(): Boolean = jsonObject.has(TASKS)

    fun addTask(task: String) {
        if (hasTasks()) {
            jsonObject.append(TASKS, task)
        } else {
            jsonObject.put(TASKS, listOf(task))
        }
    }

    fun getInntekt(): Int = jsonObject.get(INNTEKT) as Int

    fun addGrunnlagResultat(grunnlagResultat: GrunnlagResultat) = jsonObject.put(GRUNNLAG_RESULTAT, grunnlagResultat.build())

    data class GrunnlagResultat(
        val sporingsId: String,
        val subsumsjonsId: String,
        val regelidentifikator: String,
        val dagpengeGrunnlag: Int
    ) {

        fun build(): JSONObject {
            return JSONObject()
                .put(SPORINGSID, sporingsId)
                .put(SUBSUMSJONSID, subsumsjonsId)
                .put(REGELIDENTIFIKATOR, regelidentifikator)
                .put(DAGPENGEGRUNNLAG, dagpengeGrunnlag)
        }
    }

    class Builder {

        val jsonObject = JSONObject()

        fun inntekt(inntekt: Int): Builder {
            jsonObject.put(INNTEKT, inntekt)
            return this
        }

        fun task(tasks: List<String>): Builder {
            jsonObject.put(TASKS, tasks)
            return this
        }

        fun grunnlagResultat(grunnlagResultat: GrunnlagResultat): Builder {
            jsonObject.put(GRUNNLAG_RESULTAT, grunnlagResultat.build())
            return this
        }

        fun build(): SubsumsjonsBehov = SubsumsjonsBehov(jsonObject)
    }
}

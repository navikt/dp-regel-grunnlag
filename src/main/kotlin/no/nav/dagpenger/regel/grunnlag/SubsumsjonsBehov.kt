package no.nav.dagpenger.regel.grunnlag

import org.json.JSONObject
import java.math.BigDecimal
import java.time.YearMonth

data class SubsumsjonsBehov(val jsonObject: JSONObject) {

    companion object {
        val GRUNNLAG_RESULTAT = "grunnlagResultat"
        val INNTEKT = "inntektV1"
        val TASKS = "tasks"
        val TASKS_HENT_INNTEKT = "hentInntekt"
        val AVTJENT_VERNEPLIKT = "harAvtjentVerneplikt"
        val SENESTE_INNTEKTSMÅNED = "senesteInntektsmåned"
        val jsonAdapterInntekt = moshiInstance.adapter(Inntekt::class.java)
    }

    fun needsHentInntektsTask(): Boolean = !hasInntekt() && !hasHentInntektTask()

    fun needsGrunnlagResultat(): Boolean = hasInntekt() && !hasGrunnlagResultat()

    fun hasInntekt() = jsonObject.has(INNTEKT)

    fun getSenesteInntektsmåned(): YearMonth = YearMonth.parse(jsonObject.get(SENESTE_INNTEKTSMÅNED).toString())

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

    fun harAvtjentVerneplikt(): Boolean = if (jsonObject.has(AVTJENT_VERNEPLIKT)) jsonObject.getBoolean(AVTJENT_VERNEPLIKT) else false

    fun getInntekt(): Inntekt = jsonAdapterInntekt.fromJson(jsonObject.get(INNTEKT).toString())!!

    class Builder {

        val jsonObject = JSONObject()

        fun inntekt(inntekt: Inntekt): Builder {
            val json = jsonAdapterInntekt.toJson(inntekt)
            jsonObject.put(INNTEKT,
                JSONObject(json)
            )
            return this
        }

        fun senesteInntektsMåned(senesteInntektsMåned: YearMonth): Builder {
            jsonObject.put(SENESTE_INNTEKTSMÅNED, senesteInntektsMåned)
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

    fun addGrunnlagResultat(grunnlagResultat: GrunnlagResultat) =
        jsonObject.put(GRUNNLAG_RESULTAT, grunnlagResultat.build())
}

data class GrunnlagResultat(val sporingsId: String, val subsumsjonsId: String, val regelidentifikator: String, val avkortetGrunnlag: BigDecimal, val uavkortetGrunnlag: BigDecimal) {

    companion object {
        val SPORINGSID = "sporingsId"
        val SUBSUMSJONSID = "subsumsjonsId"
        val REGELIDENTIFIKATOR = "regelIdentifikator"
        val AVKORTET_GRUNNLAG = "avkortet"
        val UAVKORTET_GRUNNLAG = "uavkortet"
    }

        fun build(): JSONObject {
            return JSONObject()
                .put(SPORINGSID, sporingsId)
                .put(SUBSUMSJONSID, subsumsjonsId)
                .put(REGELIDENTIFIKATOR, regelidentifikator)
                .put(AVKORTET_GRUNNLAG, avkortetGrunnlag)
                .put(UAVKORTET_GRUNNLAG, uavkortetGrunnlag)
        }
}

data class Inntekt(
    val inntektsId: String,
    val inntektsListe: List<KlassifisertInntektMåned>
)

data class KlassifisertInntektMåned(
    val årMåned: YearMonth,
    val klassifiserteInntekter: List<KlassifisertInntekt>
)

data class KlassifisertInntekt(
    val beløp: BigDecimal,
    val inntektKlasse: InntektKlasse
)

enum class InntektKlasse {
    ARBEIDSINNTEKT,
    DAGPENGER,
    DAGPENGER_FANGST_FISKE,
    SYKEPENGER_FANGST_FISKE,
    NÆRINGSINNTEKT,
    SYKEPENGER,
    TILTAKSLØNN
}

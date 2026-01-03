package com.maa.health.data.remote.medical

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * OpenFDA API Service
 *
 * Provides FDA-validated drug information including:
 * - Drug labeling and prescribing information
 * - Adverse event reports
 * - Drug interactions
 * - Recalls and safety alerts
 *
 * Source: https://open.fda.gov/apis/drug/
 *
 * Rate limits:
 * - Without API key: 40 requests/minute, 1000/day
 * - With API key: 240 requests/minute, 120000/day
 *
 * IMPORTANT: This provides ONLY FDA-verified drug data.
 * We do NOT generate drug information.
 */
@Singleton
class OpenFDAService @Inject constructor(
    @Named("default") private val okHttpClient: OkHttpClient
) {
    companion object {
        private const val BASE_URL = "https://api.fda.gov/drug"
        private const val LABEL_ENDPOINT = "$BASE_URL/label.json"
        private const val EVENT_ENDPOINT = "$BASE_URL/event.json"
        private const val NDC_ENDPOINT = "$BASE_URL/ndc.json"

        // Default limit for results
        private const val DEFAULT_LIMIT = 5
    }

    // Optional API key (increases rate limits)
    private var apiKey: String? = null

    fun setApiKey(key: String?) {
        apiKey = key?.takeIf { it.isNotBlank() }
    }

    /**
     * Search for drug information by name
     * Returns FDA-approved labeling information
     */
    suspend fun searchDrugByName(
        drugName: String,
        limit: Int = DEFAULT_LIMIT
    ): Result<List<DrugLabel>> = withContext(Dispatchers.IO) {
        try {
            val searchQuery = "openfda.brand_name:\"$drugName\"+openfda.generic_name:\"$drugName\""
            val url = buildUrl(LABEL_ENDPOINT, "search=$searchQuery&limit=$limit")

            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            val response = okHttpClient.newCall(request).execute()

            if (response.isSuccessful) {
                val jsonBody = response.body?.string() ?: "{}"
                val labels = parseDrugLabels(jsonBody)
                Result.success(labels)
            } else if (response.code == 404) {
                // No results found
                Result.success(emptyList())
            } else {
                Result.failure(Exception("OpenFDA search failed: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get drug interactions and warnings
     */
    suspend fun getDrugWarnings(
        drugName: String
    ): Result<DrugSafetyInfo> = withContext(Dispatchers.IO) {
        try {
            val searchQuery = "openfda.brand_name:\"$drugName\"+openfda.generic_name:\"$drugName\""
            val url = buildUrl(LABEL_ENDPOINT, "search=$searchQuery&limit=1")

            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            val response = okHttpClient.newCall(request).execute()

            if (response.isSuccessful) {
                val jsonBody = response.body?.string() ?: "{}"
                val safetyInfo = parseDrugSafetyInfo(jsonBody)
                Result.success(safetyInfo)
            } else if (response.code == 404) {
                Result.success(DrugSafetyInfo())
            } else {
                Result.failure(Exception("OpenFDA warnings lookup failed: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Check if drug is safe during pregnancy
     * Based on FDA pregnancy category
     */
    suspend fun checkPregnancySafety(
        drugName: String
    ): Result<PregnancySafetyInfo> = withContext(Dispatchers.IO) {
        try {
            val searchQuery = "openfda.brand_name:\"$drugName\"+openfda.generic_name:\"$drugName\""
            val url = buildUrl(LABEL_ENDPOINT, "search=$searchQuery&limit=1")

            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            val response = okHttpClient.newCall(request).execute()

            if (response.isSuccessful) {
                val jsonBody = response.body?.string() ?: "{}"
                val pregnancyInfo = parsePregnancySafety(jsonBody)
                Result.success(pregnancyInfo)
            } else if (response.code == 404) {
                Result.success(
                    PregnancySafetyInfo(
                        drugName = drugName,
                        safetyCategory = "Unknown",
                        pregnancyWarning = "No FDA data available for this drug. Consult your healthcare provider.",
                        lactationWarning = null
                    )
                )
            } else {
                Result.failure(Exception("OpenFDA pregnancy check failed: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get adverse event reports for a drug
     * Useful for understanding side effects
     */
    suspend fun getAdverseEvents(
        drugName: String,
        limit: Int = 10
    ): Result<List<AdverseEvent>> = withContext(Dispatchers.IO) {
        try {
            val searchQuery = "patient.drug.openfda.brand_name:\"$drugName\""
            val url = buildUrl(EVENT_ENDPOINT, "search=$searchQuery&limit=$limit")

            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            val response = okHttpClient.newCall(request).execute()

            if (response.isSuccessful) {
                val jsonBody = response.body?.string() ?: "{}"
                val events = parseAdverseEvents(jsonBody)
                Result.success(events)
            } else if (response.code == 404) {
                Result.success(emptyList())
            } else {
                Result.failure(Exception("OpenFDA adverse events failed: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Check for drug interactions between two medications
     */
    suspend fun checkDrugInteraction(
        drug1: String,
        drug2: String
    ): Result<DrugInteractionInfo> = withContext(Dispatchers.IO) {
        // Get warnings for both drugs and check for interaction mentions
        val drug1Warnings = getDrugWarnings(drug1).getOrNull()
        val drug2Warnings = getDrugWarnings(drug2).getOrNull()

        val interactionFound = drug1Warnings?.drugInteractions?.any {
            it.contains(drug2, ignoreCase = true)
        } == true || drug2Warnings?.drugInteractions?.any {
            it.contains(drug1, ignoreCase = true)
        } == true

        Result.success(
            DrugInteractionInfo(
                drug1 = drug1,
                drug2 = drug2,
                hasKnownInteraction = interactionFound,
                description = if (interactionFound) {
                    "Potential interaction found. Consult your healthcare provider."
                } else {
                    "No interaction data found in FDA records. This does not guarantee safety. Consult your healthcare provider."
                },
                drug1Interactions = drug1Warnings?.drugInteractions ?: emptyList(),
                drug2Interactions = drug2Warnings?.drugInteractions ?: emptyList()
            )
        )
    }

    private fun buildUrl(endpoint: String, query: String): String {
        val keyParam = apiKey?.let { "&api_key=$it" } ?: ""
        return "$endpoint?$query$keyParam"
    }

    private fun parseDrugLabels(json: String): List<DrugLabel> {
        val labels = mutableListOf<DrugLabel>()
        try {
            val jsonObj = JSONObject(json)
            val results = jsonObj.optJSONArray("results") ?: return emptyList()

            for (i in 0 until results.length()) {
                val result = results.optJSONObject(i) ?: continue
                val openfda = result.optJSONObject("openfda")

                labels.add(
                    DrugLabel(
                        brandName = openfda?.optJSONArray("brand_name")?.optString(0, "Unknown") ?: "Unknown",
                        genericName = openfda?.optJSONArray("generic_name")?.optString(0, "") ?: "",
                        manufacturer = openfda?.optJSONArray("manufacturer_name")?.optString(0, "") ?: "",
                        purpose = result.optJSONArray("purpose")?.optString(0, "") ?: "",
                        indications = result.optJSONArray("indications_and_usage")?.optString(0, "") ?: "",
                        warnings = result.optJSONArray("warnings")?.optString(0, "") ?: "",
                        dosage = result.optJSONArray("dosage_and_administration")?.optString(0, "") ?: "",
                        activeIngredient = result.optJSONArray("active_ingredient")?.optString(0, "") ?: ""
                    )
                )
            }
        } catch (e: Exception) {
            // Return empty on parse error
        }
        return labels
    }

    private fun parseDrugSafetyInfo(json: String): DrugSafetyInfo {
        return try {
            val jsonObj = JSONObject(json)
            val results = jsonObj.optJSONArray("results")
            val firstResult = results?.optJSONObject(0) ?: return DrugSafetyInfo()

            DrugSafetyInfo(
                warnings = firstResult.optJSONArray("warnings")?.let { arr ->
                    (0 until arr.length()).map { arr.optString(it, "") }
                } ?: emptyList(),
                contraindications = firstResult.optJSONArray("contraindications")?.let { arr ->
                    (0 until arr.length()).map { arr.optString(it, "") }
                } ?: emptyList(),
                drugInteractions = firstResult.optJSONArray("drug_interactions")?.let { arr ->
                    (0 until arr.length()).map { arr.optString(it, "") }
                } ?: emptyList(),
                adverseReactions = firstResult.optJSONArray("adverse_reactions")?.let { arr ->
                    (0 until arr.length()).map { arr.optString(it, "") }
                } ?: emptyList(),
                boxedWarning = firstResult.optJSONArray("boxed_warning")?.optString(0, null)
            )
        } catch (e: Exception) {
            DrugSafetyInfo()
        }
    }

    private fun parsePregnancySafety(json: String): PregnancySafetyInfo {
        return try {
            val jsonObj = JSONObject(json)
            val results = jsonObj.optJSONArray("results")
            val firstResult = results?.optJSONObject(0) ?: return PregnancySafetyInfo()
            val openfda = firstResult.optJSONObject("openfda")

            PregnancySafetyInfo(
                drugName = openfda?.optJSONArray("brand_name")?.optString(0, "Unknown") ?: "Unknown",
                safetyCategory = firstResult.optJSONArray("pregnancy_or_breast_feeding")?.optString(0, null)
                    ?: firstResult.optJSONArray("pregnancy")?.optString(0, null)
                    ?: "Not categorized",
                pregnancyWarning = firstResult.optJSONArray("pregnancy")?.optString(0, null)
                    ?: firstResult.optJSONArray("pregnancy_or_breast_feeding")?.optString(0, null),
                lactationWarning = firstResult.optJSONArray("nursing_mothers")?.optString(0, null)
            )
        } catch (e: Exception) {
            PregnancySafetyInfo()
        }
    }

    private fun parseAdverseEvents(json: String): List<AdverseEvent> {
        val events = mutableListOf<AdverseEvent>()
        try {
            val jsonObj = JSONObject(json)
            val results = jsonObj.optJSONArray("results") ?: return emptyList()

            for (i in 0 until results.length()) {
                val result = results.optJSONObject(i) ?: continue
                val patient = result.optJSONObject("patient") ?: continue
                val reactions = patient.optJSONArray("reaction")

                val reactionList = mutableListOf<String>()
                reactions?.let { arr ->
                    for (j in 0 until arr.length()) {
                        val reaction = arr.optJSONObject(j)
                        reaction?.optString("reactionmeddrapt")?.let { reactionList.add(it) }
                    }
                }

                events.add(
                    AdverseEvent(
                        reportDate = result.optString("receiptdate", ""),
                        serious = result.optString("serious", "0") == "1",
                        reactions = reactionList,
                        patientAge = patient.optJSONObject("patientonsetage")?.optString("value"),
                        patientSex = when (patient.optString("patientsex")) {
                            "1" -> "Male"
                            "2" -> "Female"
                            else -> "Unknown"
                        }
                    )
                )
            }
        } catch (e: Exception) {
            // Return empty on parse error
        }
        return events
    }
}

/**
 * FDA drug label information
 */
data class DrugLabel(
    val brandName: String,
    val genericName: String,
    val manufacturer: String,
    val purpose: String,
    val indications: String,
    val warnings: String,
    val dosage: String,
    val activeIngredient: String
)

/**
 * Drug safety information
 */
data class DrugSafetyInfo(
    val warnings: List<String> = emptyList(),
    val contraindications: List<String> = emptyList(),
    val drugInteractions: List<String> = emptyList(),
    val adverseReactions: List<String> = emptyList(),
    val boxedWarning: String? = null  // Most serious FDA warning
)

/**
 * Pregnancy safety information
 */
data class PregnancySafetyInfo(
    val drugName: String = "",
    val safetyCategory: String = "Unknown",
    val pregnancyWarning: String? = null,
    val lactationWarning: String? = null
)

/**
 * Adverse event report
 */
data class AdverseEvent(
    val reportDate: String,
    val serious: Boolean,
    val reactions: List<String>,
    val patientAge: String?,
    val patientSex: String
)

/**
 * Drug interaction information
 */
data class DrugInteractionInfo(
    val drug1: String,
    val drug2: String,
    val hasKnownInteraction: Boolean,
    val description: String,
    val drug1Interactions: List<String>,
    val drug2Interactions: List<String>
)

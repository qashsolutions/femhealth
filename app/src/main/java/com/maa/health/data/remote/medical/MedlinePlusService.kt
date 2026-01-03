package com.maa.health.data.remote.medical

import android.util.Xml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * MedlinePlus API Service
 *
 * Provides verified, NIH-validated health information.
 * This is FREE and requires no API key.
 *
 * Sources:
 * - MedlinePlus Web Service: https://medlineplus.gov/about/developers/webservices/
 * - MedlinePlus Connect: https://medlineplus.gov/medlineplus-connect/
 *
 * IMPORTANT: This service provides ONLY verified medical content.
 * We do NOT generate medical information - we retrieve it from NIH.
 */
@Singleton
class MedlinePlusService @Inject constructor(
    @Named("default") private val okHttpClient: OkHttpClient
) {
    companion object {
        // MedlinePlus Web Service (search-based)
        private const val SEARCH_BASE_URL = "https://wsearch.nlm.nih.gov/ws/query"

        // MedlinePlus Connect (code-based lookup)
        private const val CONNECT_BASE_URL = "https://connect.medlineplus.gov/service"

        // Rate limit: 100 requests per minute per IP
        private const val MAX_RESULTS = 10
    }

    /**
     * Search for health topics by keyword
     * Returns verified NIH health information
     */
    suspend fun searchHealthTopics(
        query: String,
        language: String = "en"  // "en" or "es"
    ): Result<List<HealthTopic>> = withContext(Dispatchers.IO) {
        try {
            val db = if (language == "es") "healthTopicsSpanish" else "healthTopics"
            val url = "$SEARCH_BASE_URL?db=$db&term=${query.encodeUrl()}&retmax=$MAX_RESULTS"

            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            val response = okHttpClient.newCall(request).execute()

            if (response.isSuccessful) {
                val xmlBody = response.body?.string() ?: ""
                val topics = parseHealthTopicsXml(xmlBody)
                Result.success(topics)
            } else {
                Result.failure(Exception("MedlinePlus search failed: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get health information by ICD-10 diagnosis code
     * Used for precise medical condition lookup
     */
    suspend fun getByDiagnosisCode(
        icd10Code: String,
        language: String = "en"
    ): Result<HealthTopicLink> = withContext(Dispatchers.IO) {
        try {
            val langCode = if (language == "es") "es" else "en"
            val url = "$CONNECT_BASE_URL?mainSearchCriteria.v.cs=2.16.840.1.113883.6.90" +
                    "&mainSearchCriteria.v.c=$icd10Code" +
                    "&informationRecipient.languageCode.c=$langCode" +
                    "&knowledgeResponseType=application/json"

            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            val response = okHttpClient.newCall(request).execute()

            if (response.isSuccessful) {
                val jsonBody = response.body?.string() ?: "{}"
                val link = parseConnectResponse(jsonBody)
                Result.success(link)
            } else {
                Result.failure(Exception("MedlinePlus Connect failed: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get health information by SNOMED CT code
     */
    suspend fun getBySnomedCode(
        snomedCode: String,
        language: String = "en"
    ): Result<HealthTopicLink> = withContext(Dispatchers.IO) {
        try {
            val langCode = if (language == "es") "es" else "en"
            val url = "$CONNECT_BASE_URL?mainSearchCriteria.v.cs=2.16.840.1.113883.6.96" +
                    "&mainSearchCriteria.v.c=$snomedCode" +
                    "&informationRecipient.languageCode.c=$langCode" +
                    "&knowledgeResponseType=application/json"

            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            val response = okHttpClient.newCall(request).execute()

            if (response.isSuccessful) {
                val jsonBody = response.body?.string() ?: "{}"
                val link = parseConnectResponse(jsonBody)
                Result.success(link)
            } else {
                Result.failure(Exception("MedlinePlus Connect failed: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get drug information by RxNorm code
     */
    suspend fun getDrugInfo(
        rxNormCode: String,
        language: String = "en"
    ): Result<HealthTopicLink> = withContext(Dispatchers.IO) {
        try {
            val langCode = if (language == "es") "es" else "en"
            val url = "$CONNECT_BASE_URL?mainSearchCriteria.v.cs=2.16.840.1.113883.6.88" +
                    "&mainSearchCriteria.v.c=$rxNormCode" +
                    "&informationRecipient.languageCode.c=$langCode" +
                    "&knowledgeResponseType=application/json"

            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            val response = okHttpClient.newCall(request).execute()

            if (response.isSuccessful) {
                val jsonBody = response.body?.string() ?: "{}"
                val link = parseConnectResponse(jsonBody)
                Result.success(link)
            } else {
                Result.failure(Exception("MedlinePlus Connect failed: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Search for condition-specific health topics
     * Maps common symptoms to MedlinePlus topics
     */
    suspend fun getTopicsForCondition(
        condition: HealthCondition
    ): Result<List<HealthTopic>> {
        val searchTerms = when (condition) {
            HealthCondition.PREGNANCY -> "pregnancy prenatal care"
            HealthCondition.POSTPARTUM -> "postpartum depression recovery"
            HealthCondition.BREASTFEEDING -> "breastfeeding lactation"
            HealthCondition.MENSTRUAL_HEALTH -> "menstruation period health"
            HealthCondition.FERTILITY -> "fertility conception"
            HealthCondition.MENOPAUSE -> "menopause perimenopause"
            HealthCondition.CHILD_DEVELOPMENT -> "child development milestones"
            HealthCondition.IMMUNIZATION -> "childhood vaccines immunization"
            HealthCondition.MENTAL_HEALTH -> "mental health depression anxiety"
            HealthCondition.NUTRITION -> "nutrition healthy eating"
        }
        return searchHealthTopics(searchTerms)
    }

    /**
     * Parse MedlinePlus XML response using Android's XmlPullParser
     * This avoids external dependencies and uses built-in Android XML parsing
     */
    private fun parseHealthTopicsXml(xml: String): List<HealthTopic> {
        val topics = mutableListOf<HealthTopic>()
        try {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(StringReader(xml))

            var eventType = parser.eventType
            var currentUrl = ""
            var currentTitle = ""
            var currentSnippet = ""
            var inDocument = false
            var currentContentName = ""

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "document" -> {
                                inDocument = true
                                currentUrl = parser.getAttributeValue(null, "url") ?: ""
                                currentTitle = ""
                                currentSnippet = ""
                            }
                            "content" -> {
                                if (inDocument) {
                                    currentContentName = parser.getAttributeValue(null, "name") ?: ""
                                }
                            }
                        }
                    }
                    XmlPullParser.TEXT -> {
                        if (inDocument && parser.text != null) {
                            val text = parser.text.trim()
                                .replace("<span class=\"qt0\">", "")
                                .replace("</span>", "")
                            when (currentContentName) {
                                "title" -> currentTitle = text
                                "snippet" -> currentSnippet = text
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        when (parser.name) {
                            "content" -> currentContentName = ""
                            "document" -> {
                                if (currentTitle.isNotEmpty()) {
                                    topics.add(
                                        HealthTopic(
                                            title = currentTitle,
                                            summary = currentSnippet,
                                            url = currentUrl,
                                            source = "MedlinePlus (NIH)"
                                        )
                                    )
                                }
                                inDocument = false
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            // Return empty list on parse error - fail gracefully
        }
        return topics
    }

    private fun parseConnectResponse(json: String): HealthTopicLink {
        return try {
            val jsonObj = JSONObject(json)
            val feed = jsonObj.optJSONObject("feed")
            val entries = feed?.optJSONArray("entry")

            if (entries != null && entries.length() > 0) {
                val firstEntry = entries.getJSONObject(0)
                HealthTopicLink(
                    title = firstEntry.optJSONObject("title")?.optString("_value", "") ?: "",
                    url = firstEntry.optJSONArray("link")?.optJSONObject(0)?.optString("href", "") ?: "",
                    summary = firstEntry.optJSONObject("summary")?.optString("_value", "") ?: "",
                    source = "MedlinePlus Connect (NIH)"
                )
            } else {
                HealthTopicLink(
                    title = "No information found",
                    url = "",
                    summary = "No matching health topic found for this code.",
                    source = "MedlinePlus Connect (NIH)"
                )
            }
        } catch (e: Exception) {
            HealthTopicLink(
                title = "Error",
                url = "",
                summary = "Failed to parse response",
                source = "MedlinePlus Connect (NIH)"
            )
        }
    }

    private fun String.encodeUrl(): String {
        return java.net.URLEncoder.encode(this, "UTF-8")
    }
}

/**
 * Health topic from MedlinePlus search
 */
data class HealthTopic(
    val title: String,
    val summary: String,
    val url: String,
    val source: String
)

/**
 * Health topic link from MedlinePlus Connect
 */
data class HealthTopicLink(
    val title: String,
    val url: String,
    val summary: String,
    val source: String
)

/**
 * Common health conditions for topic lookup
 */
enum class HealthCondition {
    PREGNANCY,
    POSTPARTUM,
    BREASTFEEDING,
    MENSTRUAL_HEALTH,
    FERTILITY,
    MENOPAUSE,
    CHILD_DEVELOPMENT,
    IMMUNIZATION,
    MENTAL_HEALTH,
    NUTRITION
}

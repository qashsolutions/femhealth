package com.maa.health.service

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.maa.health.data.model.Child
import com.maa.health.data.model.CycleLog
import com.maa.health.data.model.MoodLog
import com.maa.health.data.model.Pregnancy
import com.maa.health.data.model.ScreeningResult
import com.maa.health.data.model.SymptomLog
import com.maa.health.data.model.User
import com.maa.health.data.model.VaccinationRecord
import com.maa.health.data.repository.ChildRepository
import com.maa.health.data.repository.CycleRepository
import com.maa.health.data.repository.MoodRepository
import com.maa.health.data.repository.PregnancyRepository
import com.maa.health.data.repository.SymptomRepository
import com.maa.health.data.repository.UserRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data Export Service
 *
 * Exports user health data in multiple formats:
 * - JSON (full data export)
 * - PDF (formatted report)
 *
 * Includes:
 * - Profile data
 * - Symptom logs
 * - Cycle data
 * - Mood logs
 * - Pregnancy data
 * - Child health data
 * - Vaccination records
 * - Screening results
 */
@Singleton
class DataExportService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userRepository: UserRepository,
    private val symptomRepository: SymptomRepository,
    private val cycleRepository: CycleRepository,
    private val moodRepository: MoodRepository,
    private val pregnancyRepository: PregnancyRepository,
    private val childRepository: ChildRepository
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")

    /**
     * Export all user data as JSON
     */
    suspend fun exportAsJson(userId: String): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val exportData = gatherAllData(userId)
            val jsonString = json.encodeToString(exportData)

            val fileName = "maa_health_export_${System.currentTimeMillis()}.json"
            val file = File(context.cacheDir, fileName)
            file.writeText(jsonString)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Export user data as PDF report
     */
    suspend fun exportAsPdf(userId: String): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val exportData = gatherAllData(userId)
            val pdfDocument = createPdfReport(exportData)

            val fileName = "maa_health_report_${System.currentTimeMillis()}.pdf"
            val file = File(context.cacheDir, fileName)
            FileOutputStream(file).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            pdfDocument.close()

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Share exported file via Android share sheet
     */
    fun shareExport(uri: Uri, mimeType: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Health Data").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    /**
     * Gather all user data for export
     */
    private suspend fun gatherAllData(userId: String): HealthDataExport {
        val user = userRepository.getUser(userId).first()
        val symptoms = symptomRepository.getAllSymptoms(userId).first()
        val cycles = cycleRepository.getAllCycleLogs(userId).first()
        val moods = moodRepository.getAllMoodLogs(userId).first()
        val pregnancies = pregnancyRepository.getAllPregnancies(userId).first()
        val children = childRepository.getAllChildren(userId).first()
        val screenings = moodRepository.getAllScreenings(userId).first()

        return HealthDataExport(
            exportedAt = Instant.now().toString(),
            appVersion = "1.0.0",
            profile = user?.toExportProfile(),
            symptoms = symptoms.map { it.toExportSymptom() },
            cycles = cycles.map { it.toExportCycle() },
            moods = moods.map { it.toExportMood() },
            pregnancies = pregnancies.map { it.toExportPregnancy() },
            children = children.map { it.toExportChild() },
            screenings = screenings.map { it.toExportScreening() }
        )
    }

    /**
     * Create PDF report from export data
     */
    private fun createPdfReport(data: HealthDataExport): PdfDocument {
        val document = PdfDocument()
        val pageWidth = 595 // A4 width in points
        val pageHeight = 842 // A4 height in points
        var pageNumber = 1
        var yPosition = 50f

        val titlePaint = Paint().apply {
            color = Color.parseColor("#7C3AED")
            textSize = 24f
            isFakeBoldText = true
        }

        val headerPaint = Paint().apply {
            color = Color.parseColor("#1F2937")
            textSize = 16f
            isFakeBoldText = true
        }

        val textPaint = Paint().apply {
            color = Color.parseColor("#374151")
            textSize = 12f
        }

        val subtextPaint = Paint().apply {
            color = Color.parseColor("#6B7280")
            textSize = 10f
        }

        fun newPage(): PdfDocument.Page {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber++).create()
            return document.startPage(pageInfo)
        }

        var page = newPage()
        var canvas = page.canvas

        // Title
        canvas.drawText("Maa Health Report", 50f, yPosition, titlePaint)
        yPosition += 30f

        // Export date
        canvas.drawText("Generated: ${dateTimeFormatter.format(
            Instant.now().atZone(ZoneId.systemDefault())
        )}", 50f, yPosition, subtextPaint)
        yPosition += 40f

        // Profile section
        data.profile?.let { profile ->
            canvas.drawText("Profile", 50f, yPosition, headerPaint)
            yPosition += 25f
            canvas.drawText("Phone: ${profile.phoneNumber}", 60f, yPosition, textPaint)
            yPosition += 18f
            canvas.drawText("Language: ${profile.language}", 60f, yPosition, textPaint)
            yPosition += 18f
            canvas.drawText("Life Stage: ${profile.lifecycleStage}", 60f, yPosition, textPaint)
            yPosition += 30f
        }

        // Symptoms summary
        if (data.symptoms.isNotEmpty()) {
            canvas.drawText("Symptoms (${data.symptoms.size} logged)", 50f, yPosition, headerPaint)
            yPosition += 25f

            data.symptoms.take(10).forEach { symptom ->
                if (yPosition > pageHeight - 50) {
                    document.finishPage(page)
                    page = newPage()
                    canvas = page.canvas
                    yPosition = 50f
                }
                canvas.drawText(
                    "• ${symptom.symptomType} - ${symptom.bodyRegion} (${symptom.severity})",
                    60f, yPosition, textPaint
                )
                yPosition += 18f
            }
            if (data.symptoms.size > 10) {
                canvas.drawText("... and ${data.symptoms.size - 10} more", 60f, yPosition, subtextPaint)
                yPosition += 18f
            }
            yPosition += 20f
        }

        // Cycles summary
        if (data.cycles.isNotEmpty()) {
            if (yPosition > pageHeight - 100) {
                document.finishPage(page)
                page = newPage()
                canvas = page.canvas
                yPosition = 50f
            }
            canvas.drawText("Cycle Logs (${data.cycles.size} cycles)", 50f, yPosition, headerPaint)
            yPosition += 25f

            data.cycles.take(5).forEach { cycle ->
                canvas.drawText(
                    "• Started: ${cycle.startDate} (${cycle.cycleLength ?: "?"} days)",
                    60f, yPosition, textPaint
                )
                yPosition += 18f
            }
            yPosition += 20f
        }

        // Mood summary
        if (data.moods.isNotEmpty()) {
            if (yPosition > pageHeight - 100) {
                document.finishPage(page)
                page = newPage()
                canvas = page.canvas
                yPosition = 50f
            }
            canvas.drawText("Mood Logs (${data.moods.size} entries)", 50f, yPosition, headerPaint)
            yPosition += 25f

            val avgMood = data.moods.map { it.moodScore }.average()
            canvas.drawText("Average mood score: ${String.format("%.1f", avgMood)}/5", 60f, yPosition, textPaint)
            yPosition += 30f
        }

        // Screenings summary
        if (data.screenings.isNotEmpty()) {
            if (yPosition > pageHeight - 100) {
                document.finishPage(page)
                page = newPage()
                canvas = page.canvas
                yPosition = 50f
            }
            canvas.drawText("Mental Health Screenings (${data.screenings.size})", 50f, yPosition, headerPaint)
            yPosition += 25f

            data.screenings.take(5).forEach { screening ->
                canvas.drawText(
                    "• ${screening.type}: Score ${screening.score} (${screening.severity})",
                    60f, yPosition, textPaint
                )
                yPosition += 18f
            }
            yPosition += 20f
        }

        // Children summary
        if (data.children.isNotEmpty()) {
            if (yPosition > pageHeight - 100) {
                document.finishPage(page)
                page = newPage()
                canvas = page.canvas
                yPosition = 50f
            }
            canvas.drawText("Children (${data.children.size})", 50f, yPosition, headerPaint)
            yPosition += 25f

            data.children.forEach { child ->
                canvas.drawText(
                    "• ${child.name ?: "Child"} - Born: ${child.dateOfBirth}",
                    60f, yPosition, textPaint
                )
                yPosition += 18f
            }
        }

        // Footer
        yPosition = pageHeight - 30f
        canvas.drawText(
            "This report was generated by Maa - Women's Health Companion",
            50f, yPosition, subtextPaint
        )

        document.finishPage(page)
        return document
    }

    // Extension functions to convert domain models to export models

    private fun User.toExportProfile() = ExportProfile(
        phoneNumber = phoneNumber,
        language = language.englishName,
        lifecycleStage = lifecycleStage.name,
        dateOfBirth = dateOfBirth?.toString()
    )

    private fun SymptomLog.toExportSymptom() = ExportSymptom(
        timestamp = timestamp.toString(),
        bodyRegion = bodyRegion.name,
        symptomType = symptomType.name,
        severity = severity.name
    )

    private fun CycleLog.toExportCycle() = ExportCycle(
        startDate = startDate.toString(),
        endDate = endDate?.toString(),
        cycleLength = cycleLength
    )

    private fun MoodLog.toExportMood() = ExportMood(
        timestamp = timestamp.toString(),
        moodScore = moodScore,
        energyLevel = energyLevel,
        sleepQuality = sleepQuality,
        notes = notes
    )

    private fun Pregnancy.toExportPregnancy() = ExportPregnancy(
        lmpDate = lmpDate?.toString(),
        eddDate = eddDate?.toString(),
        isHighRisk = isHighRisk,
        outcome = outcome?.name
    )

    private fun Child.toExportChild() = ExportChild(
        name = name,
        dateOfBirth = dateOfBirth.toString(),
        gender = gender.name,
        birthWeight = birthWeight,
        birthHeight = birthHeight
    )

    private fun ScreeningResult.toExportScreening() = ExportScreening(
        timestamp = timestamp.toString(),
        type = screeningType.name,
        score = score,
        severity = severity.name,
        interpretation = interpretation
    )
}

// Export data models

@Serializable
data class HealthDataExport(
    val exportedAt: String,
    val appVersion: String,
    val profile: ExportProfile?,
    val symptoms: List<ExportSymptom>,
    val cycles: List<ExportCycle>,
    val moods: List<ExportMood>,
    val pregnancies: List<ExportPregnancy>,
    val children: List<ExportChild>,
    val screenings: List<ExportScreening>
)

@Serializable
data class ExportProfile(
    val phoneNumber: String,
    val language: String,
    val lifecycleStage: String,
    val dateOfBirth: String?
)

@Serializable
data class ExportSymptom(
    val timestamp: String,
    val bodyRegion: String,
    val symptomType: String,
    val severity: String
)

@Serializable
data class ExportCycle(
    val startDate: String,
    val endDate: String?,
    val cycleLength: Int?
)

@Serializable
data class ExportMood(
    val timestamp: String,
    val moodScore: Int,
    val energyLevel: Int?,
    val sleepQuality: Int?,
    val notes: String?
)

@Serializable
data class ExportPregnancy(
    val lmpDate: String?,
    val eddDate: String?,
    val isHighRisk: Boolean,
    val outcome: String?
)

@Serializable
data class ExportChild(
    val name: String?,
    val dateOfBirth: String,
    val gender: String,
    val birthWeight: Float?,
    val birthHeight: Float?
)

@Serializable
data class ExportScreening(
    val timestamp: String,
    val type: String,
    val score: Int,
    val severity: String,
    val interpretation: String
)

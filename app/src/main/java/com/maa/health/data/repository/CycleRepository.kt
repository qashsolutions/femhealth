package com.maa.health.data.repository

import com.maa.health.data.local.database.dao.CycleDao
import com.maa.health.data.local.database.entity.CycleLogEntity
import com.maa.health.data.model.CycleLog
import com.maa.health.data.model.CyclePattern
import com.maa.health.data.model.CycleSymptom
import com.maa.health.data.model.FlowDay
import com.maa.health.data.model.FlowIntensity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

/**
 * Repository for menstrual cycle tracking
 */
@Singleton
class CycleRepository @Inject constructor(
    private val cycleDao: CycleDao
) {
    // ═══════════════════════════════════════════════════════════════════════════
    // PERIOD LOGGING
    // ═══════════════════════════════════════════════════════════════════════════

    suspend fun startPeriod(
        userId: String,
        date: LocalDate = LocalDate.now(),
        intensity: FlowIntensity = FlowIntensity.MEDIUM
    ): CycleLog {
        // End any active period first
        endActivePeriod(userId)

        val id = java.util.UUID.randomUUID().toString()
        val flowDay = FlowDay(
            date = date,
            intensity = intensity,
            symptoms = emptyList()
        )

        val entity = CycleLogEntity(
            id = id,
            oderId = userId,
            startDate = date,
            endDate = null,
            cycleLength = null,
            flowDays = listOf(flowDay),
            symptoms = emptyList()
        )
        cycleDao.insertCycleLog(entity)

        // Update cycle pattern
        updateCyclePattern(userId)

        return entity.toDomain()
    }

    suspend fun logFlowDay(
        userId: String,
        date: LocalDate,
        intensity: FlowIntensity,
        symptoms: List<CycleSymptom> = emptyList()
    ) {
        val activeCycle = cycleDao.getActiveCycle(userId).first() ?: return

        val flowDay = FlowDay(
            date = date,
            intensity = intensity,
            symptoms = symptoms.map {
                com.maa.health.data.model.Symptom(
                    type = it.name,
                    severity = com.maa.health.data.model.Severity.MILD
                )
            }
        )

        val updatedFlowDays = activeCycle.flowDays.filterNot { it.date == date } + flowDay
        val updatedSymptoms = (activeCycle.symptoms + symptoms).distinct()

        cycleDao.updateCycleLog(
            activeCycle.copy(
                flowDays = updatedFlowDays,
                symptoms = updatedSymptoms
            )
        )
    }

    suspend fun endPeriod(userId: String, endDate: LocalDate = LocalDate.now()) {
        val activeCycle = cycleDao.getActiveCycle(userId).first() ?: return

        val cycleLength = calculateCycleLengthFromPrevious(userId, activeCycle.startDate)

        cycleDao.updateCycleLog(
            activeCycle.copy(
                endDate = endDate,
                cycleLength = cycleLength
            )
        )

        // Update pattern after ending period
        updateCyclePattern(userId)
    }

    private suspend fun endActivePeriod(userId: String) {
        val activeCycle = cycleDao.getActiveCycle(userId).first()
        if (activeCycle != null && activeCycle.endDate == null) {
            val lastFlowDay = activeCycle.flowDays.maxByOrNull { it.date }
            cycleDao.updateCycleLog(
                activeCycle.copy(endDate = lastFlowDay?.date ?: LocalDate.now())
            )
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // CYCLE HISTORY
    // ═══════════════════════════════════════════════════════════════════════════

    fun getCycleHistory(userId: String): Flow<List<CycleLog>> {
        return cycleDao.getCycleHistory(userId).map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getActiveCycle(userId: String): Flow<CycleLog?> {
        return cycleDao.getActiveCycle(userId).map { it?.toDomain() }
    }

    fun getRecentCycles(userId: String, limit: Int = 6): Flow<List<CycleLog>> {
        return cycleDao.getRecentCycles(userId, limit).map { list ->
            list.map { it.toDomain() }
        }
    }

    /**
     * Get the last completed cycle (for agentic analysis)
     */
    suspend fun getLastCycle(userId: String): CycleLog? {
        return cycleDao.getRecentCycles(userId, 1).first().firstOrNull()?.toDomain()
    }

    /**
     * Get total cycle count (for agentic analysis)
     */
    suspend fun getCycleCount(userId: String): Int {
        return cycleDao.getCycleHistory(userId).first().size
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PATTERN ANALYSIS & PREDICTIONS
    // ═══════════════════════════════════════════════════════════════════════════

    private suspend fun updateCyclePattern(userId: String) {
        val recentCycles = cycleDao.getRecentCycles(userId, 6).first()
        if (recentCycles.size < 2) return

        val cycleLengths = recentCycles.mapNotNull { it.cycleLength }
        if (cycleLengths.isEmpty()) return

        val avgCycleLength = cycleLengths.average().roundToInt()
        val variability = if (cycleLengths.size > 1) {
            val mean = cycleLengths.average()
            val variance = cycleLengths.map { (it - mean) * (it - mean) }.average()
            kotlin.math.sqrt(variance).roundToInt()
        } else 0

        val lastPeriodStart = recentCycles.firstOrNull()?.startDate ?: return
        val predictedNext = lastPeriodStart.plusDays(avgCycleLength.toLong())
        val predictedOvulation = lastPeriodStart.plusDays((avgCycleLength - 14).toLong())

        // Check for irregularity (cycle length varies by more than 7 days)
        val isIrregular = variability > 7

        // Simple PCOS risk flag (irregular cycles + other factors would be needed)
        val pcosRisk = isIrregular && cycleLengths.any { it > 35 }

        // Store pattern (would use a separate table in production)
        // For now, pattern is calculated on-the-fly
    }

    suspend fun getCyclePattern(userId: String): CyclePattern {
        val recentCycles = cycleDao.getRecentCycles(userId, 12).first()

        if (recentCycles.size < 2) {
            return CyclePattern(
                userId = userId,
                averageCycleLength = 28,
                cycleVariability = 0,
                averageOvulationDay = 14,
                predictedNextPeriod = null,
                predictedOvulation = null,
                confidence = 0f,
                cyclesAnalyzed = recentCycles.size,
                irregularityFlag = false,
                pcosRiskFlag = false
            )
        }

        val cycleLengths = recentCycles.mapNotNull { it.cycleLength }
        val avgCycleLength = if (cycleLengths.isNotEmpty()) {
            cycleLengths.average().roundToInt()
        } else 28

        val variability = if (cycleLengths.size > 1) {
            val mean = cycleLengths.average()
            val variance = cycleLengths.map { (it - mean) * (it - mean) }.average()
            kotlin.math.sqrt(variance).roundToInt()
        } else 0

        val lastPeriodStart = recentCycles.firstOrNull()?.startDate
        val predictedNext = lastPeriodStart?.plusDays(avgCycleLength.toLong())
        val predictedOvulation = lastPeriodStart?.plusDays((avgCycleLength - 14).toLong())

        val confidence = when {
            recentCycles.size >= 6 && variability <= 3 -> 0.9f
            recentCycles.size >= 4 && variability <= 5 -> 0.75f
            recentCycles.size >= 2 -> 0.5f
            else -> 0.25f
        }

        return CyclePattern(
            userId = userId,
            averageCycleLength = avgCycleLength,
            cycleVariability = variability,
            averageOvulationDay = avgCycleLength - 14,
            predictedNextPeriod = predictedNext,
            predictedOvulation = predictedOvulation,
            confidence = confidence,
            cyclesAnalyzed = recentCycles.size,
            irregularityFlag = variability > 7,
            pcosRiskFlag = variability > 7 && cycleLengths.any { it > 35 }
        )
    }

    fun getFertileWindow(userId: String): Flow<FertileWindow?> {
        return cycleDao.getRecentCycles(userId, 3).map { cycles ->
            if (cycles.isEmpty()) return@map null

            val cycleLengths = cycles.mapNotNull { it.cycleLength }
            val avgLength = if (cycleLengths.isNotEmpty()) {
                cycleLengths.average().roundToInt()
            } else 28

            val lastStart = cycles.firstOrNull()?.startDate ?: return@map null
            val ovulationDay = avgLength - 14
            val ovulationDate = lastStart.plusDays(ovulationDay.toLong())

            FertileWindow(
                startDate = ovulationDate.minusDays(5),
                endDate = ovulationDate.plusDays(1),
                ovulationDate = ovulationDate,
                confidence = if (cycleLengths.size >= 3) 0.7f else 0.4f
            )
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════════════════════

    private suspend fun calculateCycleLengthFromPrevious(userId: String, currentStart: LocalDate): Int? {
        val previousCycles = cycleDao.getRecentCycles(userId, 2).first()
        val previousCycle = previousCycles.getOrNull(1) ?: return null

        return ChronoUnit.DAYS.between(previousCycle.startDate, currentStart).toInt()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MAPPERS
    // ═══════════════════════════════════════════════════════════════════════════

    private fun CycleLogEntity.toDomain(): CycleLog {
        return CycleLog(
            id = id,
            userId = oderId,
            startDate = startDate,
            endDate = endDate,
            cycleLength = cycleLength,
            flowDays = flowDays,
            symptoms = symptoms
        )
    }
}

data class FertileWindow(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val ovulationDate: LocalDate,
    val confidence: Float
)

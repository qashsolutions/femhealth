package com.maa.health.data.repository

import com.maa.health.data.local.database.dao.PregnancyDao
import com.maa.health.data.local.database.entity.PregnancyEntity
import com.maa.health.data.model.Pregnancy
import com.maa.health.data.model.PregnancyOutcome
import com.maa.health.data.model.RiskFactor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for pregnancy tracking
 */
@Singleton
class PregnancyRepository @Inject constructor(
    private val pregnancyDao: PregnancyDao
) {
    // ═══════════════════════════════════════════════════════════════════════════
    // PREGNANCY CRUD
    // ═══════════════════════════════════════════════════════════════════════════

    suspend fun createPregnancy(
        userId: String,
        lmpDate: LocalDate
    ): Pregnancy {
        val id = java.util.UUID.randomUUID().toString()
        val eddDate = lmpDate.plusDays(280) // Naegele's rule
        val gestationalWeek = calculateGestationalWeek(lmpDate)

        val entity = PregnancyEntity(
            id = id,
            oderId = userId,
            lmpDate = lmpDate,
            eddDate = eddDate,
            gestationalWeek = gestationalWeek,
            isHighRisk = false,
            riskFactors = emptyList(),
            deliveryDate = null,
            outcome = null
        )
        pregnancyDao.insertPregnancy(entity)
        return entity.toDomain()
    }

    fun getActivePregnancy(userId: String): Flow<Pregnancy?> {
        return pregnancyDao.getActivePregnancy(userId).map { it?.toDomain() }
    }

    fun getAllPregnancies(userId: String): Flow<List<Pregnancy>> {
        return pregnancyDao.getAllPregnancies(userId).map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun getPregnancyById(id: String): Pregnancy? {
        return pregnancyDao.getPregnancyById(id)?.toDomain()
    }

    suspend fun updatePregnancy(pregnancy: Pregnancy) {
        pregnancyDao.updatePregnancy(pregnancy.toEntity())
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // GESTATIONAL CALCULATIONS
    // ═══════════════════════════════════════════════════════════════════════════

    fun calculateGestationalWeek(lmpDate: LocalDate): Int {
        val daysSinceLmp = ChronoUnit.DAYS.between(lmpDate, LocalDate.now())
        return (daysSinceLmp / 7).toInt()
    }

    fun calculateGestationalDay(lmpDate: LocalDate): Int {
        val daysSinceLmp = ChronoUnit.DAYS.between(lmpDate, LocalDate.now())
        return (daysSinceLmp % 7).toInt()
    }

    fun getDaysUntilDue(eddDate: LocalDate): Long {
        return ChronoUnit.DAYS.between(LocalDate.now(), eddDate)
    }

    fun getTrimester(gestationalWeek: Int): Int {
        return when {
            gestationalWeek < 13 -> 1
            gestationalWeek < 27 -> 2
            else -> 3
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // RISK ASSESSMENT
    // ═══════════════════════════════════════════════════════════════════════════

    suspend fun addRiskFactor(pregnancyId: String, riskFactor: RiskFactor) {
        val pregnancy = pregnancyDao.getPregnancyById(pregnancyId) ?: return
        val updatedFactors = pregnancy.riskFactors + riskFactor
        pregnancyDao.updatePregnancy(
            pregnancy.copy(
                riskFactors = updatedFactors,
                isHighRisk = updatedFactors.isNotEmpty()
            )
        )
    }

    suspend fun removeRiskFactor(pregnancyId: String, riskFactor: RiskFactor) {
        val pregnancy = pregnancyDao.getPregnancyById(pregnancyId) ?: return
        val updatedFactors = pregnancy.riskFactors - riskFactor
        pregnancyDao.updatePregnancy(
            pregnancy.copy(
                riskFactors = updatedFactors,
                isHighRisk = updatedFactors.isNotEmpty()
            )
        )
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PREGNANCY COMPLETION
    // ═══════════════════════════════════════════════════════════════════════════

    suspend fun recordDelivery(
        pregnancyId: String,
        deliveryDate: LocalDate,
        outcome: PregnancyOutcome
    ) {
        val pregnancy = pregnancyDao.getPregnancyById(pregnancyId) ?: return
        pregnancyDao.updatePregnancy(
            pregnancy.copy(
                deliveryDate = deliveryDate,
                outcome = outcome
            )
        )
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // WEEK BY WEEK DATA
    // ═══════════════════════════════════════════════════════════════════════════

    fun getWeekInfo(week: Int): WeekInfo {
        return weekInfoData.getOrElse(week) {
            WeekInfo(
                week = week,
                babySize = "Developing",
                babySizeComparison = "",
                babyLength = "",
                babyWeight = "",
                developments = listOf("Your baby continues to grow"),
                motherChanges = listOf("Your body is adapting to pregnancy"),
                tips = listOf("Continue prenatal vitamins", "Stay hydrated")
            )
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MAPPERS
    // ═══════════════════════════════════════════════════════════════════════════

    private fun PregnancyEntity.toDomain(): Pregnancy {
        return Pregnancy(
            id = id,
            userId = oderId,
            lmpDate = lmpDate,
            eddDate = eddDate,
            gestationalWeek = calculateGestationalWeek(lmpDate ?: LocalDate.now()),
            isHighRisk = isHighRisk,
            riskFactors = riskFactors,
            deliveryDate = deliveryDate,
            outcome = outcome
        )
    }

    private fun Pregnancy.toEntity(): PregnancyEntity {
        return PregnancyEntity(
            id = id,
            oderId = userId,
            lmpDate = lmpDate,
            eddDate = eddDate,
            gestationalWeek = gestationalWeek,
            isHighRisk = isHighRisk,
            riskFactors = riskFactors,
            deliveryDate = deliveryDate,
            outcome = outcome
        )
    }

    companion object {
        // Sample week data - in production this would be more comprehensive
        private val weekInfoData = mapOf(
            4 to WeekInfo(
                week = 4,
                babySize = "Poppy seed",
                babySizeComparison = "poppy seed",
                babyLength = "0.1 cm",
                babyWeight = "<1 g",
                developments = listOf(
                    "Implantation occurs",
                    "Amniotic sac forming",
                    "Placenta beginning to develop"
                ),
                motherChanges = listOf(
                    "Missed period expected",
                    "Possible light spotting",
                    "Breast tenderness may begin"
                ),
                tips = listOf(
                    "Take a pregnancy test",
                    "Start prenatal vitamins with folic acid",
                    "Avoid alcohol and smoking"
                )
            ),
            8 to WeekInfo(
                week = 8,
                babySize = "Raspberry",
                babySizeComparison = "raspberry",
                babyLength = "1.6 cm",
                babyWeight = "1 g",
                developments = listOf(
                    "Heart beating at 150-170 bpm",
                    "Arms and legs forming",
                    "Facial features developing"
                ),
                motherChanges = listOf(
                    "Morning sickness may be strong",
                    "Fatigue common",
                    "Frequent urination"
                ),
                tips = listOf(
                    "Eat small frequent meals",
                    "Get plenty of rest",
                    "Schedule first prenatal visit"
                )
            ),
            12 to WeekInfo(
                week = 12,
                babySize = "Lime",
                babySizeComparison = "lime",
                babyLength = "5.4 cm",
                babyWeight = "14 g",
                developments = listOf(
                    "All organs formed",
                    "Reflexes developing",
                    "Fingernails growing"
                ),
                motherChanges = listOf(
                    "Morning sickness may ease",
                    "Energy returning",
                    "Small bump may show"
                ),
                tips = listOf(
                    "First trimester screening",
                    "Announce pregnancy if ready",
                    "Start gentle exercise"
                )
            ),
            20 to WeekInfo(
                week = 20,
                babySize = "Banana",
                babySizeComparison = "banana",
                babyLength = "25.6 cm",
                babyWeight = "300 g",
                developments = listOf(
                    "Halfway point!",
                    "Can hear sounds",
                    "Regular sleep cycles"
                ),
                motherChanges = listOf(
                    "Feeling movements",
                    "Bump clearly visible",
                    "Possible backaches"
                ),
                tips = listOf(
                    "Anatomy scan ultrasound",
                    "Start counting kicks",
                    "Consider birth classes"
                )
            ),
            28 to WeekInfo(
                week = 28,
                babySize = "Eggplant",
                babySizeComparison = "eggplant",
                babyLength = "37.6 cm",
                babyWeight = "1 kg",
                developments = listOf(
                    "Eyes can open",
                    "Brain developing rapidly",
                    "Lungs maturing"
                ),
                motherChanges = listOf(
                    "Third trimester begins",
                    "Braxton Hicks contractions",
                    "Shortness of breath"
                ),
                tips = listOf(
                    "Glucose screening test",
                    "Start birth plan",
                    "Prepare hospital bag"
                )
            ),
            36 to WeekInfo(
                week = 36,
                babySize = "Papaya",
                babySizeComparison = "papaya",
                babyLength = "47.4 cm",
                babyWeight = "2.6 kg",
                developments = listOf(
                    "Lungs nearly mature",
                    "Head may engage",
                    "Fat layer developing"
                ),
                motherChanges = listOf(
                    "Baby dropping lower",
                    "Easier breathing",
                    "Pelvic pressure"
                ),
                tips = listOf(
                    "Weekly prenatal visits",
                    "Finalize birth plan",
                    "Install car seat"
                )
            ),
            40 to WeekInfo(
                week = 40,
                babySize = "Watermelon",
                babySizeComparison = "watermelon",
                babyLength = "51 cm",
                babyWeight = "3.4 kg",
                developments = listOf(
                    "Full term!",
                    "Ready for birth",
                    "Fully developed"
                ),
                motherChanges = listOf(
                    "Cervix ripening",
                    "Nesting instinct",
                    "Braxton Hicks increasing"
                ),
                tips = listOf(
                    "Watch for labor signs",
                    "Rest when possible",
                    "Stay close to hospital"
                )
            )
        )
    }
}

data class WeekInfo(
    val week: Int,
    val babySize: String,
    val babySizeComparison: String,
    val babyLength: String,
    val babyWeight: String,
    val developments: List<String>,
    val motherChanges: List<String>,
    val tips: List<String>
)

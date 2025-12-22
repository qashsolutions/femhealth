package com.maa.health.data.repository

import com.maa.health.data.local.database.dao.ChildDao
import com.maa.health.data.local.database.dao.GrowthDao
import com.maa.health.data.local.database.dao.VaccinationDao
import com.maa.health.data.local.database.entity.ChildEntity
import com.maa.health.data.local.database.entity.GrowthMeasurementEntity
import com.maa.health.data.local.database.entity.VaccinationRecordEntity
import com.maa.health.data.model.Child
import com.maa.health.data.model.Gender
import com.maa.health.data.model.GrowthMeasurement
import com.maa.health.data.model.VaccinationRecord
import com.maa.health.data.model.VaccineType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for child health tracking
 */
@Singleton
class ChildRepository @Inject constructor(
    private val childDao: ChildDao,
    private val vaccinationDao: VaccinationDao,
    private val growthDao: GrowthDao
) {
    // ═══════════════════════════════════════════════════════════════════════════
    // CHILD PROFILE
    // ═══════════════════════════════════════════════════════════════════════════

    suspend fun addChild(
        userId: String,
        name: String?,
        dateOfBirth: LocalDate,
        gender: Gender,
        birthWeight: Float?,
        birthHeight: Float?
    ): Child {
        val id = java.util.UUID.randomUUID().toString()
        val entity = ChildEntity(
            id = id,
            oderId = userId,
            name = name,
            dateOfBirth = dateOfBirth,
            gender = gender,
            birthWeight = birthWeight,
            birthHeight = birthHeight,
            currentWeight = birthWeight,
            currentHeight = birthHeight
        )
        childDao.insertChild(entity)

        // Generate vaccination schedule
        generateVaccinationSchedule(id, dateOfBirth)

        return entity.toDomain()
    }

    fun getChildren(userId: String): Flow<List<Child>> {
        return childDao.getChildrenForUser(userId).map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun getChildById(childId: String): Child? {
        return childDao.getChildById(childId)?.toDomain()
    }

    suspend fun updateChild(child: Child) {
        childDao.updateChild(child.toEntity())
    }

    suspend fun deleteChild(childId: String) {
        childDao.deleteChild(childId)
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // VACCINATION
    // ═══════════════════════════════════════════════════════════════════════════

    private suspend fun generateVaccinationSchedule(childId: String, dateOfBirth: LocalDate) {
        val schedule = listOf(
            VaccineSchedule(VaccineType.BCG, 0),
            VaccineSchedule(VaccineType.OPV_0, 0),
            VaccineSchedule(VaccineType.HEPATITIS_B_BIRTH, 0),
            VaccineSchedule(VaccineType.OPV_1, 6),
            VaccineSchedule(VaccineType.PENTAVALENT_1, 6),
            VaccineSchedule(VaccineType.ROTAVIRUS_1, 6),
            VaccineSchedule(VaccineType.PCV_1, 6),
            VaccineSchedule(VaccineType.OPV_2, 10),
            VaccineSchedule(VaccineType.PENTAVALENT_2, 10),
            VaccineSchedule(VaccineType.ROTAVIRUS_2, 10),
            VaccineSchedule(VaccineType.OPV_3, 14),
            VaccineSchedule(VaccineType.PENTAVALENT_3, 14),
            VaccineSchedule(VaccineType.ROTAVIRUS_3, 14),
            VaccineSchedule(VaccineType.PCV_2, 14),
            VaccineSchedule(VaccineType.MEASLES_1, 36), // 9 months
            VaccineSchedule(VaccineType.VITAMIN_A_1, 36),
            VaccineSchedule(VaccineType.JE_1, 36),
            VaccineSchedule(VaccineType.MR_1, 64), // 16 months
            VaccineSchedule(VaccineType.JE_2, 64),
            VaccineSchedule(VaccineType.DPT_BOOSTER_1, 64),
            VaccineSchedule(VaccineType.OPV_BOOSTER_1, 64),
            VaccineSchedule(VaccineType.PCV_BOOSTER, 64),
            VaccineSchedule(VaccineType.VITAMIN_A_2, 64)
        )

        schedule.forEach { vs ->
            val dueDate = dateOfBirth.plusWeeks(vs.weeksFromBirth.toLong())
            val record = VaccinationRecordEntity(
                id = java.util.UUID.randomUUID().toString(),
                childId = childId,
                vaccineType = vs.vaccineType,
                dueDate = dueDate,
                givenDate = null,
                batch = null,
                facility = null,
                sideEffects = null
            )
            vaccinationDao.insertVaccination(record)
        }
    }

    fun getVaccinationSchedule(childId: String): Flow<List<VaccinationRecord>> {
        return vaccinationDao.getVaccinationsForChild(childId).map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getPendingVaccinations(childId: String): Flow<List<VaccinationRecord>> {
        return vaccinationDao.getPendingVaccinations(childId, LocalDate.now()).map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getUpcomingVaccinations(childId: String, daysAhead: Int = 30): Flow<List<VaccinationRecord>> {
        val endDate = LocalDate.now().plusDays(daysAhead.toLong())
        return vaccinationDao.getUpcomingVaccinations(childId, LocalDate.now(), endDate).map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun recordVaccination(
        recordId: String,
        givenDate: LocalDate,
        batch: String?,
        facility: String?
    ) {
        val record = vaccinationDao.getVaccinationById(recordId) ?: return
        vaccinationDao.updateVaccination(
            record.copy(
                givenDate = givenDate,
                batch = batch,
                facility = facility
            )
        )
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // GROWTH TRACKING
    // ═══════════════════════════════════════════════════════════════════════════

    suspend fun addGrowthMeasurement(
        childId: String,
        weight: Float,
        height: Float,
        headCircumference: Float?
    ): GrowthMeasurement {
        val child = childDao.getChildById(childId) ?: throw IllegalArgumentException("Child not found")

        // Calculate percentiles (simplified - would use WHO growth charts)
        val percentiles = calculatePercentiles(child.gender, child.dateOfBirth, weight, height)

        val entity = GrowthMeasurementEntity(
            id = java.util.UUID.randomUUID().toString(),
            childId = childId,
            date = LocalDate.now(),
            weight = weight,
            height = height,
            headCircumference = headCircumference,
            weightForAgePercentile = percentiles.weightForAge,
            heightForAgePercentile = percentiles.heightForAge,
            weightForHeightPercentile = percentiles.weightForHeight
        )
        growthDao.insertMeasurement(entity)

        // Update child's current measurements
        childDao.updateChild(
            child.copy(
                currentWeight = weight,
                currentHeight = height
            )
        )

        return entity.toDomain()
    }

    fun getGrowthHistory(childId: String): Flow<List<GrowthMeasurement>> {
        return growthDao.getMeasurementsForChild(childId).map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun getLatestGrowth(childId: String): GrowthMeasurement? {
        return growthDao.getLatestMeasurement(childId)?.toDomain()
    }

    private fun calculatePercentiles(
        gender: Gender,
        dateOfBirth: LocalDate,
        weight: Float,
        height: Float
    ): GrowthPercentiles {
        // Simplified percentile calculation
        // In production, use WHO growth standards
        val ageMonths = java.time.Period.between(dateOfBirth, LocalDate.now()).toTotalMonths()

        // Placeholder logic - would use actual WHO growth curves
        return GrowthPercentiles(
            weightForAge = 50,
            heightForAge = 50,
            weightForHeight = 50
        )
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MAPPERS
    // ═══════════════════════════════════════════════════════════════════════════

    private fun ChildEntity.toDomain(): Child {
        return Child(
            id = id,
            userId = oderId,
            name = name,
            dateOfBirth = dateOfBirth,
            gender = gender,
            birthWeight = birthWeight,
            birthHeight = birthHeight,
            currentWeight = currentWeight,
            currentHeight = currentHeight
        )
    }

    private fun Child.toEntity(): ChildEntity {
        return ChildEntity(
            id = id,
            oderId = userId,
            name = name,
            dateOfBirth = dateOfBirth,
            gender = gender,
            birthWeight = birthWeight,
            birthHeight = birthHeight,
            currentWeight = currentWeight,
            currentHeight = currentHeight
        )
    }

    private fun VaccinationRecordEntity.toDomain(): VaccinationRecord {
        return VaccinationRecord(
            id = id,
            childId = childId,
            vaccineType = vaccineType,
            dueDate = dueDate,
            givenDate = givenDate,
            batch = batch,
            facility = facility,
            sideEffects = sideEffects
        )
    }

    private fun GrowthMeasurementEntity.toDomain(): GrowthMeasurement {
        return GrowthMeasurement(
            id = id,
            childId = childId,
            date = date,
            weight = weight,
            height = height,
            headCircumference = headCircumference,
            weightForAgePercentile = weightForAgePercentile,
            heightForAgePercentile = heightForAgePercentile,
            weightForHeightPercentile = weightForHeightPercentile
        )
    }
}

private data class VaccineSchedule(
    val vaccineType: VaccineType,
    val weeksFromBirth: Int
)

private data class GrowthPercentiles(
    val weightForAge: Int,
    val heightForAge: Int,
    val weightForHeight: Int
)

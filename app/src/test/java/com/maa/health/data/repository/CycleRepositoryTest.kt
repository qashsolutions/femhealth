package com.maa.health.data.repository

import com.maa.health.data.local.database.dao.CycleDao
import com.maa.health.data.local.database.entity.CycleLogEntity
import com.maa.health.data.local.database.entity.CyclePatternEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

/**
 * Unit tests for CycleRepository
 */
class CycleRepositoryTest {

    private lateinit var cycleDao: CycleDao
    private lateinit var cycleRepository: CycleRepository

    @Before
    fun setup() {
        cycleDao = mockk(relaxed = true)
        cycleRepository = CycleRepository(cycleDao)
    }

    @Test
    fun `getCycleLogs returns logs for user`() = runTest {
        // Given
        val userId = "test-user-123"
        val cycleLogs = listOf(
            createCycleLogEntity("log1", userId),
            createCycleLogEntity("log2", userId)
        )
        coEvery { cycleDao.getAllByUser(userId) } returns cycleLogs

        // When
        val result = cycleRepository.getCycleLogs(userId).first()

        // Then
        assertEquals(2, result.size)
    }

    @Test
    fun `getCurrentCycle returns latest cycle`() = runTest {
        // Given
        val userId = "test-user-123"
        val latestCycle = createCycleLogEntity("latest", userId)
        coEvery { cycleDao.getLatestCycle(userId) } returns latestCycle

        // When
        val result = cycleRepository.getCurrentCycle(userId)

        // Then
        assertNotNull(result)
        assertEquals("latest", result?.id)
    }

    @Test
    fun `startNewCycle creates and inserts cycle log`() = runTest {
        // Given
        val userId = "test-user-123"
        val startDate = LocalDate.now()

        // When
        val result = cycleRepository.startNewCycle(userId, startDate)

        // Then
        assertNotNull(result)
        assertEquals(userId, result.userId)
        coVerify { cycleDao.insert(any()) }
    }

    @Test
    fun `endCurrentCycle updates cycle with end date`() = runTest {
        // Given
        val cycleId = "cycle-123"
        val endDate = LocalDate.now()

        // When
        cycleRepository.endCurrentCycle(cycleId, endDate)

        // Then
        coVerify { cycleDao.updateEndDate(cycleId, endDate.toEpochDay()) }
    }

    @Test
    fun `getCyclePattern returns pattern for user`() = runTest {
        // Given
        val userId = "test-user-123"
        val pattern = CyclePatternEntity(
            userId = userId,
            averageCycleLength = 28,
            cycleVariability = 2,
            averageOvulationDay = 14
        )
        coEvery { cycleDao.getPattern(userId) } returns pattern

        // When
        val result = cycleRepository.getCyclePattern(userId).first()

        // Then
        assertNotNull(result)
        assertEquals(28, result?.averageCycleLength)
    }

    @Test
    fun `predictNextPeriod calculates based on pattern`() = runTest {
        // Given
        val userId = "test-user-123"
        val lastPeriodStart = LocalDate.now().minusDays(28)
        val pattern = CyclePatternEntity(
            userId = userId,
            averageCycleLength = 28
        )
        val lastCycle = createCycleLogEntity("last", userId, lastPeriodStart)

        coEvery { cycleDao.getPattern(userId) } returns pattern
        coEvery { cycleDao.getLatestCycle(userId) } returns lastCycle

        // When
        val prediction = cycleRepository.predictNextPeriod(userId)

        // Then
        assertNotNull(prediction)
        // Expected: lastPeriodStart + 28 days
        assertEquals(LocalDate.now(), prediction)
    }

    @Test
    fun `deleteCycleLog removes log from database`() = runTest {
        // Given
        val logId = "cycle-123"

        // When
        cycleRepository.deleteCycleLog(logId)

        // Then
        coVerify { cycleDao.delete(logId) }
    }

    @Test
    fun `getCycleCount returns correct count`() = runTest {
        // Given
        val userId = "test-user-123"
        coEvery { cycleDao.getCycleCount(userId) } returns 10

        // When
        val result = cycleRepository.getCycleCount(userId)

        // Then
        assertEquals(10, result)
    }

    private fun createCycleLogEntity(
        id: String,
        userId: String,
        startDate: LocalDate = LocalDate.now()
    ): CycleLogEntity {
        return CycleLogEntity(
            id = id,
            userId = userId,
            startDate = startDate.toEpochDay(),
            endDate = null,
            cycleLength = null,
            flowDays = "",
            symptoms = ""
        )
    }
}

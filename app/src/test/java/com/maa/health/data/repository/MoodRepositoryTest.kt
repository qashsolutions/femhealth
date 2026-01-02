package com.maa.health.data.repository

import com.maa.health.data.local.database.dao.MoodDao
import com.maa.health.data.local.database.entity.MoodLogEntity
import com.maa.health.data.local.database.entity.MoodPatternEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.Instant

/**
 * Unit tests for MoodRepository
 */
class MoodRepositoryTest {

    private lateinit var moodDao: MoodDao
    private lateinit var moodRepository: MoodRepository

    @Before
    fun setup() {
        moodDao = mockk(relaxed = true)
        moodRepository = MoodRepository(moodDao)
    }

    @Test
    fun `getMoodLogs returns logs for user`() = runTest {
        // Given
        val userId = "test-user-123"
        val moodLogs = listOf(
            createMoodLogEntity("log1", userId, 4),
            createMoodLogEntity("log2", userId, 3)
        )
        coEvery { moodDao.getAllByUser(userId) } returns moodLogs

        // When
        val result = moodRepository.getMoodLogs(userId).first()

        // Then
        assertEquals(2, result.size)
    }

    @Test
    fun `logMood creates and inserts mood log`() = runTest {
        // Given
        val userId = "test-user-123"
        val moodScore = 4
        val notes = "Feeling good today"

        // When
        val result = moodRepository.logMood(userId, moodScore, notes)

        // Then
        assertNotNull(result)
        assertEquals(moodScore, result.moodScore)
        coVerify { moodDao.insert(any()) }
    }

    @Test
    fun `logMood validates mood score range`() = runTest {
        // Given
        val userId = "test-user-123"

        // When/Then - score should be clamped to 1-5 range
        val resultLow = moodRepository.logMood(userId, 0, null)
        val resultHigh = moodRepository.logMood(userId, 10, null)

        assertTrue(resultLow.moodScore >= 1)
        assertTrue(resultHigh.moodScore <= 5)
    }

    @Test
    fun `getMoodPattern returns pattern for user`() = runTest {
        // Given
        val userId = "test-user-123"
        val pattern = MoodPatternEntity(
            userId = userId,
            averageMoodScore = 3.5f,
            moodTrend = "STABLE",
            lastUpdated = Instant.now().toEpochMilli()
        )
        coEvery { moodDao.getPattern(userId) } returns pattern

        // When
        val result = moodRepository.getMoodPattern(userId).first()

        // Then
        assertNotNull(result)
        assertEquals(3.5f, result?.averageMoodScore)
    }

    @Test
    fun `getAverageMoodScore calculates correctly`() = runTest {
        // Given
        val userId = "test-user-123"
        val moodLogs = listOf(
            createMoodLogEntity("log1", userId, 4),
            createMoodLogEntity("log2", userId, 3),
            createMoodLogEntity("log3", userId, 5)
        )
        coEvery { moodDao.getAllByUser(userId) } returns moodLogs

        // When
        val average = moodRepository.getAverageMoodScore(userId)

        // Then
        assertEquals(4.0f, average, 0.01f)  // (4+3+5)/3 = 4
    }

    @Test
    fun `getMoodTrend detects declining trend`() = runTest {
        // Given
        val userId = "test-user-123"
        // Mood declining over time
        val moodLogs = listOf(
            createMoodLogEntity("log1", userId, 5, Instant.now().minusSeconds(300)),
            createMoodLogEntity("log2", userId, 4, Instant.now().minusSeconds(200)),
            createMoodLogEntity("log3", userId, 3, Instant.now().minusSeconds(100)),
            createMoodLogEntity("log4", userId, 2, Instant.now())
        )
        coEvery { moodDao.getRecentLogs(userId, any()) } returns moodLogs

        // When
        val trend = moodRepository.getMoodTrend(userId)

        // Then
        assertEquals("DECLINING", trend)
    }

    @Test
    fun `deleteMoodLog removes log from database`() = runTest {
        // Given
        val logId = "mood-123"

        // When
        moodRepository.deleteMoodLog(logId)

        // Then
        coVerify { moodDao.delete(logId) }
    }

    @Test
    fun `getRecentMoodLogs returns logs within time range`() = runTest {
        // Given
        val userId = "test-user-123"
        val recentLogs = listOf(
            createMoodLogEntity("log1", userId, 4)
        )
        coEvery { moodDao.getRecentLogs(userId, any()) } returns recentLogs

        // When
        val result = moodRepository.getRecentMoodLogs(userId, days = 7).first()

        // Then
        assertEquals(1, result.size)
    }

    private fun createMoodLogEntity(
        id: String,
        userId: String,
        moodScore: Int,
        timestamp: Instant = Instant.now()
    ): MoodLogEntity {
        return MoodLogEntity(
            id = id,
            userId = userId,
            timestamp = timestamp.toEpochMilli(),
            moodScore = moodScore,
            energyLevel = null,
            sleepQuality = null,
            anxietyLevel = null,
            notes = null,
            voiceNoteUrl = null,
            detectedKeywords = ""
        )
    }
}

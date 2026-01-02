package com.maa.health.data.repository

import com.maa.health.data.local.database.dao.UserDao
import com.maa.health.data.local.database.entity.UserEntity
import com.maa.health.data.local.datastore.UserPreferences
import com.maa.health.data.local.datastore.UserPreferencesData
import com.maa.health.data.model.LifecycleStage
import com.maa.health.data.model.SupportedLanguage
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

/**
 * Unit tests for UserRepository
 */
class UserRepositoryTest {

    private lateinit var userDao: UserDao
    private lateinit var userPreferences: UserPreferences
    private lateinit var userRepository: UserRepository

    @Before
    fun setup() {
        userDao = mockk(relaxed = true)
        userPreferences = mockk(relaxed = true)
        userRepository = UserRepository(userDao, userPreferences)
    }

    @Test
    fun `getCurrentUser returns null when no user id in preferences`() = runTest {
        // Given
        val prefsData = UserPreferencesData(oderId = "")
        every { userPreferences.userPreferencesFlow } returns flowOf(prefsData)

        // When
        val result = userRepository.getCurrentUser()

        // Then
        assertNull(result)
    }

    @Test
    fun `getCurrentUser returns user when user id exists`() = runTest {
        // Given
        val userId = "test-user-123"
        val prefsData = UserPreferencesData(oderId = userId)
        val userEntity = createTestUserEntity(userId)

        every { userPreferences.userPreferencesFlow } returns flowOf(prefsData)
        coEvery { userDao.getUserById(userId) } returns userEntity

        // When
        val result = userRepository.getCurrentUser()

        // Then
        assertNotNull(result)
        assertEquals(userId, result?.id)
    }

    @Test
    fun `createUser inserts user and sets user id in preferences`() = runTest {
        // Given
        val phoneNumber = "+919876543210"
        val name = "Test User"
        val age = 25
        val pincode = "110001"

        // When
        val result = userRepository.createUser(phoneNumber, name, age, pincode)

        // Then
        assertNotNull(result)
        assertEquals(phoneNumber, result.phoneNumber)
        coVerify { userDao.insertUser(any()) }
        coVerify { userPreferences.setUserId(any()) }
    }

    @Test
    fun `setLifecycleStages updates preferences`() = runTest {
        // Given
        val stages = setOf(LifecycleStage.REPRODUCTIVE, LifecycleStage.PREGNANCY)

        // When
        userRepository.setLifecycleStages(stages)

        // Then
        coVerify { userPreferences.setLifecycleStages(stages) }
    }

    @Test
    fun `setLanguage updates preferences`() = runTest {
        // Given
        val language = SupportedLanguage.HINDI

        // When
        userRepository.setLanguage(language)

        // Then
        coVerify { userPreferences.setLanguage(language) }
    }

    @Test
    fun `isOnboardingComplete returns correct value`() = runTest {
        // Given
        val prefsData = UserPreferencesData(isOnboardingComplete = true)
        every { userPreferences.userPreferencesFlow } returns flowOf(prefsData)

        // When
        val result = userRepository.isOnboardingComplete()

        // Then
        assertTrue(result)
    }

    @Test
    fun `isPremiumUser always returns true for free app`() = runTest {
        // Given - All features are now free
        val prefsData = UserPreferencesData(isPremium = true)
        every { userPreferences.userPreferencesFlow } returns flowOf(prefsData)

        // When
        val result = userRepository.isPremiumUser()

        // Then
        assertTrue(result)
    }

    @Test
    fun `deleteAllUserData clears user and preferences`() = runTest {
        // Given
        val userId = "test-user-123"
        val prefsData = UserPreferencesData(oderId = userId)
        every { userPreferences.userPreferencesFlow } returns flowOf(prefsData)

        // When
        userRepository.deleteAllUserData()

        // Then
        coVerify { userDao.deleteUser(userId) }
        coVerify { userPreferences.clearAllPreferences() }
    }

    @Test
    fun `logout clears all preferences`() = runTest {
        // When
        userRepository.logout()

        // Then
        coVerify { userPreferences.clearAllPreferences() }
    }

    private fun createTestUserEntity(id: String): UserEntity {
        return UserEntity(
            id = id,
            phoneNumber = "+919876543210",
            languageCode = "hi",
            lifecycleStage = "REPRODUCTIVE",
            dateOfBirth = LocalDate.now().minusYears(25).toEpochDay(),
            biometricEnabled = false,
            subscriptionStatus = "FREE",
            createdAt = Instant.now().toEpochMilli(),
            lastActiveAt = Instant.now().toEpochMilli()
        )
    }
}

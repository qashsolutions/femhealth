package com.maa.health.service

import android.content.Context
import com.maa.health.data.local.database.MaaDatabase
import com.maa.health.data.local.datastore.UserPreferencesDataStore
import com.maa.health.data.local.datastore.UserPreferencesData
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for CloudSyncService
 */
class CloudSyncServiceTest {

    private lateinit var context: Context
    private lateinit var database: MaaDatabase
    private lateinit var userPreferencesDataStore: UserPreferencesDataStore
    private lateinit var cloudSyncService: CloudSyncService

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        database = mockk(relaxed = true)
        userPreferencesDataStore = mockk(relaxed = true)

        cloudSyncService = CloudSyncService(context, database, userPreferencesDataStore)
    }

    @Test
    fun `isPremiumUser always returns true for free app`() = runTest {
        // All features are now free
        val result = cloudSyncService.isPremiumUser()
        assertTrue(result)
    }

    @Test
    fun `syncToCloud fails when user not authenticated`() = runTest {
        // Given - No Firebase auth (getCurrentUserId returns null)

        // When
        val result = cloudSyncService.syncToCloud()

        // Then
        assertTrue(result is SyncResult.Error)
        assertEquals("User not authenticated", (result as SyncResult.Error).message)
    }

    @Test
    fun `restoreFromCloud fails when user not authenticated`() = runTest {
        // Given - No Firebase auth

        // When
        val result = cloudSyncService.restoreFromCloud()

        // Then
        assertTrue(result is SyncResult.Error)
        assertEquals("User not authenticated", (result as SyncResult.Error).message)
    }

    @Test
    fun `getLastSyncTime returns null when never synced`() = runTest {
        // Given
        val prefsData = UserPreferencesData(lastSyncedAt = null)
        coEvery { userPreferencesDataStore.getUserPreferences() } returns flowOf(prefsData)

        // When
        val result = cloudSyncService.getLastSyncTime()

        // Then
        assertNull(result)
    }

    // Note: Full sync tests require Firebase emulator or mocking Firebase dependencies
    // These tests focus on the local logic and error handling
}

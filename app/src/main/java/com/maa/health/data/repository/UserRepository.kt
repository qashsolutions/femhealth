package com.maa.health.data.repository

import com.maa.health.data.local.database.dao.UserDao
import com.maa.health.data.local.database.entity.UserEntity
import com.maa.health.data.local.datastore.UserPreferences
import com.maa.health.data.model.LifecycleStage
import com.maa.health.data.model.SupportedLanguage
import com.maa.health.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for user data operations
 */
@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val userPreferences: UserPreferences
) {
    // ═══════════════════════════════════════════════════════════════════════════
    // USER PROFILE
    // ═══════════════════════════════════════════════════════════════════════════

    suspend fun getCurrentUser(): User? {
        val userId = userPreferences.userPreferencesFlow.first().oderId
        if (userId.isBlank()) return null
        return userDao.getUserById(userId)?.toDomain()
    }

    fun getCurrentUserFlow(): Flow<User?> {
        return userPreferences.userPreferencesFlow.map { prefs ->
            if (prefs.oderId.isBlank()) null
            else userDao.getUserById(prefs.oderId)?.toDomain()
        }
    }

    suspend fun createUser(
        phoneNumber: String,
        name: String?,
        age: Int,
        pincode: String?
    ): User {
        val userId = java.util.UUID.randomUUID().toString()
        val user = UserEntity(
            id = userId,
            phoneNumber = phoneNumber,
            name = name,
            dateOfBirth = LocalDate.now().minusYears(age.toLong()),
            pincode = pincode,
            createdAt = java.time.Instant.now()
        )
        userDao.insertUser(user)
        userPreferences.setUserId(userId)
        return user.toDomain()
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(user.toEntity())
    }

    suspend fun updateUserName(name: String) {
        val userId = userPreferences.userPreferencesFlow.first().oderId
        userDao.updateUserName(userId, name)
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // LIFECYCLE STAGES
    // ═══════════════════════════════════════════════════════════════════════════

    suspend fun setLifecycleStages(stages: Set<LifecycleStage>) {
        userPreferences.setLifecycleStages(stages)
    }

    fun getLifecycleStagesFlow(): Flow<Set<LifecycleStage>> {
        return userPreferences.userPreferencesFlow.map { it.lifecycleStages }
    }

    suspend fun getLifecycleStages(): Set<LifecycleStage> {
        return userPreferences.userPreferencesFlow.first().lifecycleStages
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // LANGUAGE
    // ═══════════════════════════════════════════════════════════════════════════

    suspend fun setLanguage(language: SupportedLanguage) {
        userPreferences.setLanguage(language)
    }

    fun getLanguageFlow(): Flow<SupportedLanguage> {
        return userPreferences.userPreferencesFlow.map { it.language }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ONBOARDING
    // ═══════════════════════════════════════════════════════════════════════════

    suspend fun isOnboardingComplete(): Boolean {
        return userPreferences.userPreferencesFlow.first().isOnboardingComplete
    }

    fun isOnboardingCompleteFlow(): Flow<Boolean> {
        return userPreferences.userPreferencesFlow.map { it.isOnboardingComplete }
    }

    suspend fun setOnboardingComplete() {
        userPreferences.setOnboardingComplete(true)
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SUBSCRIPTION
    // ═══════════════════════════════════════════════════════════════════════════

    suspend fun isPremiumUser(): Boolean {
        return userPreferences.userPreferencesFlow.first().isPremium
    }

    fun isPremiumUserFlow(): Flow<Boolean> {
        return userPreferences.userPreferencesFlow.map { it.isPremium }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SETTINGS
    // ═══════════════════════════════════════════════════════════════════════════

    val userPreferencesFlow = userPreferences.userPreferencesFlow

    suspend fun getSelectedLanguage(): SupportedLanguage {
        return userPreferences.userPreferencesFlow.first().language
    }

    suspend fun setOfflineMode(enabled: Boolean) {
        userPreferences.setOfflineContentEnabled(enabled)
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        userPreferences.setBiometricEnabled(enabled)
    }

    suspend fun setPinEnabled(enabled: Boolean) {
        userPreferences.setPinEnabled(enabled)
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        userPreferences.setNotificationsEnabled(enabled)
    }

    suspend fun deleteAllUserData() {
        val userId = userPreferences.userPreferencesFlow.first().oderId
        if (userId.isNotBlank()) {
            userDao.deleteUser(userId)
        }
        userPreferences.clearAllPreferences()
    }

    suspend fun logout() {
        userPreferences.clearAllPreferences()
    }

    /**
     * Get user profile for agentic analysis
     * Combines user data with preferences for AI personalization
     */
    suspend fun getUserProfile(userId: String): UserProfileForAI? {
        val user = userDao.getUserById(userId)?.toDomain() ?: return null
        val prefs = userPreferences.userPreferencesFlow.first()

        val age = user.dateOfBirth?.let {
            java.time.Period.between(it, LocalDate.now()).years
        } ?: 25

        val primaryStage = prefs.lifecycleStages.firstOrNull() ?: LifecycleStage.REPRODUCTIVE

        return UserProfileForAI(
            id = user.id,
            lifecycleStage = primaryStage,
            age = age,
            isPregnant = prefs.lifecycleStages.contains(LifecycleStage.PREGNANCY),
            isPostpartum = prefs.lifecycleStages.contains(LifecycleStage.POSTPARTUM),
            hasChildren = prefs.lifecycleStages.contains(LifecycleStage.CHILD_CARE),
            preferredLanguage = prefs.language.code
        )
    }

/**
 * User profile data for AI/agentic analysis
 */
data class UserProfileForAI(
    val id: String,
    val lifecycleStage: LifecycleStage,
    val age: Int,
    val isPregnant: Boolean,
    val isPostpartum: Boolean,
    val hasChildren: Boolean,
    val preferredLanguage: String
)

    // ═══════════════════════════════════════════════════════════════════════════
    // MAPPERS
    // ═══════════════════════════════════════════════════════════════════════════

    private fun UserEntity.toDomain(): User {
        return User(
            id = id,
            phoneNumber = phoneNumber,
            name = name,
            dateOfBirth = dateOfBirth,
            pincode = pincode
        )
    }

    private fun User.toEntity(): UserEntity {
        return UserEntity(
            id = id,
            phoneNumber = phoneNumber,
            name = name,
            dateOfBirth = dateOfBirth,
            pincode = pincode
        )
    }
}

package com.maa.health.data.local.database.dao

import androidx.room.*
import com.maa.health.data.local.database.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserById(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE phoneNumber = :phone")
    suspend fun getUserByPhone(phone: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET lifecycleStage = :stage WHERE id = :userId")
    suspend fun updateLifecycleStage(userId: String, stage: String)

    @Query("UPDATE users SET lastActiveAt = :timestamp WHERE id = :userId")
    suspend fun updateLastActive(userId: String, timestamp: Long)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("DELETE FROM users")
    suspend fun deleteAll()
}

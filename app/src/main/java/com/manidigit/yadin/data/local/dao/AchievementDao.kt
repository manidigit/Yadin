package com.manidigit.yadin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.manidigit.yadin.data.local.entity.AchievementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements")
    fun getAllAchievementsFlow(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements WHERE id = :id LIMIT 1")
    suspend fun getAchievement(id: String): AchievementEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: AchievementEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAchievements(achievements: List<AchievementEntity>)

    @Query("UPDATE achievements SET unlockedAt = :unlockedAt WHERE id = :id AND unlockedAt IS NULL")
    suspend fun unlock(id: String, unlockedAt: Long = System.currentTimeMillis())

    @Query("UPDATE achievements SET unlockedAt = NULL WHERE id = :id AND unlockedAt IS NOT NULL")
    suspend fun relock(id: String)

    @Query("UPDATE achievements SET progress = :progress WHERE id = :id")
    suspend fun updateProgress(id: String, progress: Int)

    @Query("SELECT * FROM achievements")
    suspend fun getAllAchievements(): List<AchievementEntity>

    @Query("DELETE FROM achievements")
    suspend fun clearAchievements()
}

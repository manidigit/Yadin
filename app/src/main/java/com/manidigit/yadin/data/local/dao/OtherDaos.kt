package com.manidigit.yadin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.manidigit.yadin.data.local.entity.AchievementEntity
import com.manidigit.yadin.data.local.entity.ImportReviewItemEntity
import com.manidigit.yadin.data.local.entity.SettingEntity
import com.manidigit.yadin.domain.model.ImportItemStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("SELECT value FROM settings WHERE `key` = :key LIMIT 1")
    fun getSettingFlow(key: String): Flow<String?>

    @Query("SELECT value FROM settings WHERE `key` = :key LIMIT 1")
    suspend fun getSetting(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: SettingEntity)

    @Query("SELECT * FROM settings")
    suspend fun getAllSettings(): List<SettingEntity>
}

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

    @Query("UPDATE achievements SET progress = :progress WHERE id = :id")
    suspend fun updateProgress(id: String, progress: Int)
}

@Dao
interface ImportReviewDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ImportReviewItemEntity>)

    @Query("SELECT * FROM import_review_items WHERE sessionTag = :sessionTag ORDER BY lineNumber ASC")
    fun getItemsForSessionFlow(sessionTag: String): Flow<List<ImportReviewItemEntity>>

    @Query("SELECT * FROM import_review_items WHERE status = 'PENDING' ORDER BY createdAt DESC")
    fun getAllPendingItemsFlow(): Flow<List<ImportReviewItemEntity>>

    @Query("UPDATE import_review_items SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: ImportItemStatus)

    @Query("DELETE FROM import_review_items WHERE sessionTag = :sessionTag")
    suspend fun deleteSessionItems(sessionTag: String)

    @Query("DELETE FROM import_review_items WHERE id = :id")
    suspend fun deleteItem(id: String)
}

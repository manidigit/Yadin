package com.manidigit.yadin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.manidigit.yadin.data.local.entity.ReviewHistoryEntity
import com.manidigit.yadin.data.local.entity.ReviewSessionEntity
import com.manidigit.yadin.data.local.entity.ReviewSessionItemEntity
import com.manidigit.yadin.domain.model.SessionItemState
import com.manidigit.yadin.domain.model.SessionStatus
import kotlinx.coroutines.flow.Flow

data class DayCountRaw(
    val reviewedDay: String,
    val totalCount: Int,
    val correctCount: Int
)

@Dao
interface ReviewSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ReviewSessionEntity)

    @Update
    suspend fun updateSession(session: ReviewSessionEntity)

    @Query("SELECT * FROM review_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: String): ReviewSessionEntity?

    @Query("SELECT * FROM review_sessions WHERE status = 'ACTIVE' ORDER BY startedAt DESC LIMIT 1")
    suspend fun getActiveSession(): ReviewSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessionItems(items: List<ReviewSessionItemEntity>)

    @Query("SELECT * FROM review_session_items WHERE sessionId = :sessionId ORDER BY position ASC")
    suspend fun getItemsForSession(sessionId: String): List<ReviewSessionItemEntity>

    @Query("UPDATE review_session_items SET state = :state WHERE id = :itemId")
    suspend fun updateItemState(itemId: String, state: SessionItemState)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryItem(item: ReviewHistoryEntity)

    @Query("SELECT * FROM review_history WHERE sessionId = :sessionId ORDER BY reviewedAt ASC")
    suspend fun getHistoryForSession(sessionId: String): List<ReviewHistoryEntity>

    @Query("SELECT * FROM review_history WHERE reviewAttemptId = :attemptId LIMIT 1")
    suspend fun getHistoryByAttemptId(attemptId: String): ReviewHistoryEntity?

    @Query("SELECT * FROM review_session_items WHERE sessionId = :sessionId AND conceptId = :conceptId LIMIT 1")
    suspend fun getItemForSessionAndConcept(sessionId: String, conceptId: String): ReviewSessionItemEntity?

    @Query("SELECT COUNT(*) FROM review_history WHERE reviewedDay = :dayString")
    fun getReviewCountForDayFlow(dayString: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM review_history WHERE reviewedDay = :dayString")
    suspend fun getReviewCountForDay(dayString: String): Int

    @Query("SELECT COUNT(*) FROM review_history")
    fun getTotalReviewsCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM review_history WHERE isCorrect = 1")
    fun getTotalCorrectReviewsCountFlow(): Flow<Int>

    @Query("SELECT DISTINCT reviewedDay FROM review_history WHERE reviewedDay IS NOT NULL ORDER BY reviewedDay DESC")
    suspend fun getDistinctReviewedDays(): List<String>

    @Query("""
        SELECT 
            reviewedDay,
            COUNT(*) AS totalCount,
            SUM(CASE WHEN isCorrect = 1 THEN 1 ELSE 0 END) AS correctCount
        FROM review_history
        WHERE reviewedDay IS NOT NULL
        GROUP BY reviewedDay
        ORDER BY reviewedDay DESC
        LIMIT :limit
    """)
    suspend fun getRecentDailyStats(limit: Int = 90): List<DayCountRaw>

    @Query("""
        SELECT 
            reviewedDay,
            COUNT(*) AS totalCount,
            SUM(CASE WHEN isCorrect = 1 THEN 1 ELSE 0 END) AS correctCount
        FROM review_history
        WHERE reviewedDay IS NOT NULL
        GROUP BY reviewedDay
        ORDER BY reviewedDay DESC
        LIMIT :limit
    """)
    fun getRecentDailyStatsFlow(limit: Int = 90): Flow<List<DayCountRaw>>

    @Query("SELECT * FROM review_history")
    suspend fun getAllHistory(): List<ReviewHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryItems(items: List<ReviewHistoryEntity>)

    @Query("SELECT * FROM review_sessions")
    suspend fun getAllSessions(): List<ReviewSessionEntity>

    @Query("SELECT * FROM review_session_items")
    suspend fun getAllSessionItems(): List<ReviewSessionItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<ReviewSessionEntity>)

    @Query("DELETE FROM review_history")
    suspend fun clearHistory()

    @Query("DELETE FROM review_sessions")
    suspend fun clearSessions()

    @Query("DELETE FROM review_session_items")
    suspend fun clearSessionItems()
}

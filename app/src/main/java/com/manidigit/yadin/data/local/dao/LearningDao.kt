package com.manidigit.yadin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.manidigit.yadin.data.local.entity.DifficultyStateEntity
import com.manidigit.yadin.data.local.entity.LearningStateEntity
import com.manidigit.yadin.domain.model.CardDirection
import com.manidigit.yadin.domain.model.Stage
import com.manidigit.yadin.domain.model.VocabularyDifficulty
import kotlinx.coroutines.flow.Flow

data class StageCount(
    val stage: Stage,
    val count: Int
)

data class DifficultyCount(
    val current: VocabularyDifficulty,
    val count: Int
)

data class ProgressCalculationRaw(
    val totalScore: Double?,
    val totalActive: Int
)

@Dao
interface LearningDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLearningState(state: LearningStateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLearningStates(states: List<LearningStateEntity>)

    @Update
    suspend fun updateLearningState(state: LearningStateEntity)

    @Query("SELECT * FROM learning_states WHERE conceptId = :conceptId AND direction = :direction LIMIT 1")
    suspend fun getLearningState(conceptId: String, direction: CardDirection): LearningStateEntity?

    @Query("SELECT * FROM learning_states WHERE conceptId = :conceptId")
    suspend fun getLearningStatesForConcept(conceptId: String): List<LearningStateEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDifficultyState(state: DifficultyStateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDifficultyStates(states: List<DifficultyStateEntity>)

    @Update
    suspend fun updateDifficultyState(state: DifficultyStateEntity)

    @Query("SELECT * FROM difficulty_states WHERE conceptId = :conceptId AND direction = :direction LIMIT 1")
    suspend fun getDifficultyState(conceptId: String, direction: CardDirection): DifficultyStateEntity?

    @Query("SELECT * FROM difficulty_states WHERE conceptId = :conceptId")
    suspend fun getDifficultyStatesForConcept(conceptId: String): List<DifficultyStateEntity>

    @Query("SELECT * FROM difficulty_states WHERE conceptId IN (:conceptIds) AND direction = :direction")
    suspend fun getDifficultyStatesForConcepts(conceptIds: List<String>, direction: CardDirection): List<DifficultyStateEntity>

    @Query("SELECT * FROM learning_states")
    suspend fun getAllLearningStates(): List<LearningStateEntity>

    @Query("SELECT * FROM difficulty_states")
    suspend fun getAllDifficultyStates(): List<DifficultyStateEntity>

    @Query("DELETE FROM learning_states")
    suspend fun clearLearningStates()

    @Query("DELETE FROM difficulty_states")
    suspend fun clearDifficultyStates()

    // Due cards for review:
    // Respects same-day lock (lastReviewedDay != today) and Leitner scheduling
    @Query("""
        SELECT ls.conceptId FROM learning_states ls
        INNER JOIN concepts c ON ls.conceptId = c.id
        WHERE c.active = 1
        AND ls.direction = :direction
        AND (ls.lastReviewedDay IS NULL OR ls.lastReviewedDay != :todayDayString)
        AND (
            (ls.stage = 'DAILY' AND (ls.nextReviewDay IS NULL OR ls.nextReviewDay <= :todayDayString)) OR
            (ls.stage = 'WEEKLY' AND (ls.nextReviewDay IS NULL OR ls.nextReviewDay <= :todayDayString)) OR
            (ls.stage = 'MONTHLY' AND (ls.nextReviewDay IS NULL OR ls.nextReviewDay <= :todayDayString))
        )
        ORDER BY 
            CASE ls.stage 
                WHEN 'DAILY' THEN 1 
                WHEN 'WEEKLY' THEN 2 
                WHEN 'MONTHLY' THEN 3 
                ELSE 4 
            END ASC,
            ls.nextReviewDay ASC
        LIMIT :limit
    """)
    suspend fun getDueConceptIds(
        direction: CardDirection,
        todayDayString: String,
        limit: Int
    ): List<String>

    @Query("""
        SELECT COUNT(DISTINCT ls.conceptId) FROM learning_states ls
        INNER JOIN concepts c ON ls.conceptId = c.id
        WHERE c.active = 1
        AND ls.direction = :direction
        AND ls.lastReviewedDay IS NOT NULL
        AND ls.lastReviewedDay != :todayDayString
        AND ls.stage != 'LEARNED'
        AND (
            (ls.stage = 'DAILY' AND (ls.nextReviewDay IS NULL OR ls.nextReviewDay <= :todayDayString)) OR
            (ls.stage = 'WEEKLY' AND (ls.nextReviewDay IS NULL OR ls.nextReviewDay <= :todayDayString)) OR
            (ls.stage = 'MONTHLY' AND (ls.nextReviewDay IS NULL OR ls.nextReviewDay <= :todayDayString))
        )
    """)
    fun getDueCountFlow(direction: CardDirection, todayDayString: String): Flow<Int>

    @Query("""
        SELECT ls.conceptId FROM learning_states ls
        INNER JOIN concepts c ON ls.conceptId = c.id
        WHERE c.active = 1
        AND ls.direction = :direction
        AND ls.stage = :stage
        ORDER BY ls.updatedAt ASC
        LIMIT :limit
    """)
    suspend fun getConceptIdsByStage(
        stage: Stage,
        direction: CardDirection,
        limit: Int
    ): List<String>

    @Query("""
        SELECT c.id FROM concepts c
        WHERE c.active = 1
        ORDER BY RANDOM()
        LIMIT :limit
    """)
    suspend fun getRandomConceptIds(limit: Int): List<String>

    @Query("""
        SELECT COUNT(*) 
        FROM learning_states ls
        INNER JOIN concepts c ON ls.conceptId = c.id
        WHERE c.active = 1 AND ls.direction = :direction AND ls.stage = :stage
        AND (:stage = 'LEARNED' OR ls.lastReviewedDay IS NOT NULL)
    """)
    fun getCountByStageFlow(direction: CardDirection, stage: Stage): Flow<Int>

    @Query("""
        SELECT COUNT(*) 
        FROM learning_states ls
        INNER JOIN concepts c ON ls.conceptId = c.id
        WHERE c.active = 1 AND ls.direction = :direction AND ls.lastReviewedDay IS NULL
    """)
    fun getUnstartedCountFlow(direction: CardDirection): Flow<Int>

    @Query("""
        SELECT COUNT(*) 
        FROM difficulty_states ds
        INNER JOIN concepts c ON ds.conceptId = c.id
        WHERE c.active = 1 AND ds.direction = :direction AND ds.current = :difficulty
    """)
    fun getCountByDifficultyFlow(direction: CardDirection, difficulty: VocabularyDifficulty): Flow<Int>

    @Query("""
        SELECT ls.stage AS stage, COUNT(*) AS count
        FROM learning_states ls
        INNER JOIN concepts c ON ls.conceptId = c.id
        WHERE c.active = 1 AND ls.direction = :direction
        GROUP BY ls.stage
    """)
    suspend fun getStageBreakdown(direction: CardDirection): List<StageCount>

    @Query("""
        SELECT ls.conceptId FROM learning_states ls
        INNER JOIN concepts c ON ls.conceptId = c.id
        LEFT JOIN difficulty_states ds ON (ds.conceptId = ls.conceptId AND ds.direction = ls.direction)
        WHERE c.active = 1
        AND ls.direction = :direction
        AND (
            (:reviewType = 'DAILY' AND ls.stage = 'DAILY' AND (ls.lastReviewedDay IS NULL OR ls.lastReviewedDay != :todayDayString) AND (ls.nextReviewDay IS NULL OR ls.nextReviewDay <= :todayDayString)) OR
            (:reviewType = 'WEEKLY' AND ls.stage = 'WEEKLY' AND (ls.lastReviewedDay IS NULL OR ls.lastReviewedDay != :todayDayString) AND (ls.nextReviewDay IS NULL OR ls.nextReviewDay <= :todayDayString)) OR
            (:reviewType = 'MONTHLY' AND ls.stage = 'MONTHLY' AND (ls.lastReviewedDay IS NULL OR ls.lastReviewedDay != :todayDayString) AND (ls.nextReviewDay IS NULL OR ls.nextReviewDay <= :todayDayString)) OR
            (:reviewType = 'LEARNED' AND ls.stage = 'LEARNED') OR
            (:reviewType = 'RANDOM' AND (ls.lastReviewedDay IS NULL OR ls.lastReviewedDay != :todayDayString))
        )
        AND (:hasDifficultyFilter = 0 OR ds.current IN (:difficulties))
        AND (:hasCategoryFilter = 0 OR c.categoryId IN (:categoryIds))
        ORDER BY 
            CASE 
                WHEN :reviewType = 'RANDOM' THEN RANDOM() 
                WHEN ls.lastReviewedDay IS NOT NULL AND (ls.nextReviewDay IS NULL OR ls.nextReviewDay <= :todayDayString) THEN 0
                WHEN ls.lastReviewedDay IS NULL THEN 1
                ELSE 2
            END ASC,
            ls.nextReviewDay ASC
        LIMIT :limit
    """)
    suspend fun getFilteredCandidateConceptIds(
        direction: CardDirection,
        reviewType: String,
        todayDayString: String,
        hasDifficultyFilter: Int,
        difficulties: List<String>,
        hasCategoryFilter: Int,
        categoryIds: List<String>,
        limit: Int
    ): List<String>

    @Query("""
        SELECT COUNT(DISTINCT ls.conceptId) FROM learning_states ls
        INNER JOIN concepts c ON ls.conceptId = c.id
        LEFT JOIN difficulty_states ds ON (ds.conceptId = ls.conceptId AND ds.direction = ls.direction)
        WHERE c.active = 1
        AND ls.direction = :direction
        AND (
            (:reviewType = 'DAILY' AND ls.stage = 'DAILY' AND (ls.lastReviewedDay IS NULL OR ls.lastReviewedDay != :todayDayString) AND (ls.nextReviewDay IS NULL OR ls.nextReviewDay <= :todayDayString)) OR
            (:reviewType = 'WEEKLY' AND ls.stage = 'WEEKLY' AND (ls.lastReviewedDay IS NULL OR ls.lastReviewedDay != :todayDayString) AND (ls.nextReviewDay IS NULL OR ls.nextReviewDay <= :todayDayString)) OR
            (:reviewType = 'MONTHLY' AND ls.stage = 'MONTHLY' AND (ls.lastReviewedDay IS NULL OR ls.lastReviewedDay != :todayDayString) AND (ls.nextReviewDay IS NULL OR ls.nextReviewDay <= :todayDayString)) OR
            (:reviewType = 'LEARNED' AND ls.stage = 'LEARNED') OR
            (:reviewType = 'RANDOM' AND (ls.lastReviewedDay IS NULL OR ls.lastReviewedDay != :todayDayString))
        )
        AND (:hasDifficultyFilter = 0 OR ds.current IN (:difficulties))
        AND (:hasCategoryFilter = 0 OR c.categoryId IN (:categoryIds))
    """)
    suspend fun countFilteredCandidates(
        direction: CardDirection,
        reviewType: String,
        todayDayString: String,
        hasDifficultyFilter: Int,
        difficulties: List<String>,
        hasCategoryFilter: Int,
        categoryIds: List<String>
    ): Int

    @Query("""
        SELECT ds.current AS current, COUNT(*) AS count
        FROM difficulty_states ds
        INNER JOIN concepts c ON ds.conceptId = c.id
        WHERE c.active = 1 AND ds.direction = :direction
        GROUP BY ds.current
    """)
    fun getDifficultyBreakdownFlow(direction: CardDirection): Flow<List<DifficultyCount>>

    @Query("""
        SELECT ds.current AS current, COUNT(*) AS count
        FROM difficulty_states ds
        INNER JOIN concepts c ON ds.conceptId = c.id
        WHERE c.active = 1 AND ds.direction = :direction
        GROUP BY ds.current
    """)
    suspend fun getDifficultyBreakdown(direction: CardDirection): List<DifficultyCount>

    @Query("""
        SELECT 
            SUM(
                CASE 
                    WHEN ls.lastReviewedDay IS NULL THEN 0.0
                    WHEN ls.stage = 'LEARNED' THEN 100.0
                    WHEN ls.stage = 'MONTHLY' THEN 80.0
                    WHEN ls.stage = 'WEEKLY' THEN 60.0
                    WHEN ls.stage = 'DAILY' THEN 35.0
                    ELSE 0.0
                END
            ) AS totalScore,
            COUNT(DISTINCT c.id) AS totalActive
        FROM concepts c
        INNER JOIN learning_states ls ON ls.conceptId = c.id
        WHERE c.active = 1 AND ls.direction = :direction
    """)
    fun getProgressScoreFlow(direction: CardDirection): Flow<ProgressCalculationRaw>

    @Query("""
        SELECT COUNT(DISTINCT ls.conceptId)
        FROM learning_states ls
        INNER JOIN concepts c ON ls.conceptId = c.id
        WHERE c.active = 1 AND ls.direction = :direction
        AND ls.stage != 'LEARNED' AND ls.lastReviewedDay IS NOT NULL
    """)
    fun getPracticedWordsCountFlow(direction: CardDirection): Flow<Int>

    @Query("""
        SELECT COUNT(DISTINCT ls.conceptId)
        FROM learning_states ls
        INNER JOIN concepts c ON ls.conceptId = c.id
        WHERE c.active = 1 AND ls.lastReviewedDay IS NOT NULL
    """)
    suspend fun getTotalPracticedWordsCount(): Int

    @Query("""
        SELECT COUNT(DISTINCT ls.conceptId)
        FROM learning_states ls
        INNER JOIN concepts c ON ls.conceptId = c.id
        WHERE c.active = 1 AND ls.stage = 'LEARNED'
    """)
    suspend fun getTotalLearnedWordsCount(): Int

    @Query("""
        SELECT COUNT(DISTINCT ds.conceptId)
        FROM difficulty_states ds
        INNER JOIN concepts c ON ds.conceptId = c.id
        WHERE c.active = 1 AND ds.hasReachedVeryHard = 1 AND (ds.current = 'EASY' OR ds.current = 'MEDIUM')
    """)
    suspend fun getMasteredHardWordsCount(): Int
}

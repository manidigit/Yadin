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

    // Due cards for review:
    // When reviewType is DAILY: stage = 'DAILY' OR (nextReviewDay IS NOT NULL AND nextReviewDay <= :todayDayString)
    @Query("""
        SELECT ls.conceptId FROM learning_states ls
        INNER JOIN concepts c ON ls.conceptId = c.id
        WHERE c.active = 1
        AND ls.direction = :direction
        AND (ls.stage = 'DAILY' OR (ls.nextReviewDay IS NOT NULL AND ls.nextReviewDay <= :todayDayString))
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
        AND (ls.stage = 'DAILY' OR (ls.nextReviewDay IS NOT NULL AND ls.nextReviewDay <= :todayDayString))
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

    @Query("SELECT COUNT(*) FROM learning_states WHERE direction = :direction AND stage = :stage")
    fun getCountByStageFlow(direction: CardDirection, stage: Stage): Flow<Int>

    @Query("SELECT COUNT(*) FROM difficulty_states WHERE direction = :direction AND current = :difficulty")
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
        SELECT ds.current AS current, COUNT(*) AS count
        FROM difficulty_states ds
        INNER JOIN concepts c ON ds.conceptId = c.id
        WHERE c.active = 1 AND ds.direction = :direction
        GROUP BY ds.current
    """)
    suspend fun getDifficultyBreakdown(direction: CardDirection): List<DifficultyCount>
}

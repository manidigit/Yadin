package com.manidigit.yadin.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.manidigit.yadin.domain.model.CardDirection
import com.manidigit.yadin.domain.model.DuplicatePolicy
import com.manidigit.yadin.domain.model.EntryType
import com.manidigit.yadin.domain.model.ImportItemStatus
import com.manidigit.yadin.domain.model.QuizLevel
import com.manidigit.yadin.domain.model.ReviewMode
import com.manidigit.yadin.domain.model.ReviewType
import com.manidigit.yadin.domain.model.SessionItemState
import com.manidigit.yadin.domain.model.SessionStatus
import com.manidigit.yadin.domain.model.Stage
import com.manidigit.yadin.domain.model.VocabularyDifficulty

@Entity(
    tableName = "concepts",
    indices = [
        Index("categoryId"),
        Index("favorite"),
        Index("active")
    ]
)
data class ConceptEntity(
    @PrimaryKey val id: String,
    val entryType: EntryType = EntryType.WORD,
    val categoryId: String? = null,
    val favorite: Boolean = false,
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "contents",
    indices = [
        Index("conceptId"),
        Index("languageCode"),
        Index("canonicalKey"),
        Index(value = ["languageCode", "canonicalKey"])
    ]
)
data class ContentEntity(
    @PrimaryKey val id: String,
    val conceptId: String,
    val languageCode: String,
    val text: String,
    val canonicalKey: String,
    val note: String? = null,
    val pronunciation: String? = null,
    val translationIndex: Int = 0
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val sortOrder: Int = 0,
    val isDefault: Boolean = false
)

@Entity(
    tableName = "concept_categories",
    primaryKeys = ["conceptId", "categoryId"],
    indices = [Index("conceptId"), Index("categoryId")]
)
data class ConceptCategoryEntity(
    val conceptId: String,
    val categoryId: String
)

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey val id: String,
    val name: String
)

@Entity(
    tableName = "concept_tags",
    primaryKeys = ["conceptId", "tagId"],
    indices = [Index("conceptId"), Index("tagId")]
)
data class ConceptTagEntity(
    val conceptId: String,
    val tagId: String
)

@Entity(
    tableName = "learning_states",
    indices = [
        Index("conceptId"),
        Index(value = ["conceptId", "direction"], unique = true),
        Index("stage"),
        Index("nextReviewDay")
    ]
)
data class LearningStateEntity(
    @PrimaryKey val id: String,
    val conceptId: String,
    val direction: CardDirection = CardDirection.NORMAL,
    val stage: Stage = Stage.DAILY,
    val nextReviewDay: String? = null, // YYYY-MM-DD
    val lastReviewedDay: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "difficulty_states",
    indices = [
        Index("conceptId"),
        Index(value = ["conceptId", "direction"], unique = true),
        Index("current")
    ]
)
data class DifficultyStateEntity(
    @PrimaryKey val id: String,
    val conceptId: String,
    val direction: CardDirection = CardDirection.NORMAL,
    val current: VocabularyDifficulty = VocabularyDifficulty.MEDIUM,
    val consecutiveCorrect: Int = 0,
    val consecutiveWrong: Int = 0,
    val hasReachedVeryHard: Boolean = false
)

@Entity(tableName = "review_sessions")
data class ReviewSessionEntity(
    @PrimaryKey val id: String,
    val startedAt: Long,
    val endedAt: Long? = null,
    val reviewType: ReviewType,
    val mode: ReviewMode,
    val direction: CardDirection,
    val quizLevel: QuizLevel? = null,
    val status: SessionStatus = SessionStatus.ACTIVE,
    val currentPosition: Int = 0,
    val totalItems: Int = 0
)

@Entity(
    tableName = "review_session_items",
    indices = [Index("sessionId"), Index("conceptId")]
)
data class ReviewSessionItemEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val position: Int,
    val conceptId: String,
    val direction: CardDirection,
    val state: SessionItemState = SessionItemState.PENDING
)

@Entity(
    tableName = "review_history",
    indices = [
        Index("sessionId"),
        Index("conceptId"),
        Index("reviewedDay"),
        Index("reviewedAt")
    ]
)
data class ReviewHistoryEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val reviewAttemptId: String,
    val conceptId: String,
    val direction: CardDirection,
    val reviewedAt: Long,
    val reviewedDay: String, // YYYY-MM-DD
    val isCorrect: Boolean,
    val reviewType: ReviewType,
    val mode: ReviewMode,
    val stageBefore: Stage,
    val quizLevel: QuizLevel? = null,
    val optionsJson: String? = null,
    val selectedIndex: Int? = null,
    val correctIndex: Int? = null
)

@Entity(tableName = "settings")
data class SettingEntity(
    @PrimaryKey val key: String,
    val value: String
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val unlockedAt: Long? = null,
    val progress: Int = 0
)

@Entity(
    tableName = "import_review_items",
    indices = [Index("sessionTag"), Index("status")]
)
data class ImportReviewItemEntity(
    @PrimaryKey val id: String,
    val sessionTag: String,
    val sourceText: String,
    val translationsText: String?,
    val note: String?,
    val categoryNames: String?,
    val entryType: String?,
    val confidence: Double,
    val reason: String,
    val status: ImportItemStatus,
    val lineNumber: Int?,
    val rawText: String,
    val policy: DuplicatePolicy,
    val targetConceptId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

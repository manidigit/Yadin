package com.manidigit.yadin.data.local.database

import androidx.room.TypeConverter
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

class Converters {
    @TypeConverter
    fun fromEntryType(value: EntryType?): String = value?.name ?: EntryType.WORD.name

    @TypeConverter
    fun toEntryType(value: String?): EntryType = try {
        value?.let { EntryType.valueOf(it) } ?: EntryType.WORD
    } catch (_: Exception) {
        EntryType.WORD
    }

    @TypeConverter
    fun fromCardDirection(value: CardDirection?): String = value?.name ?: CardDirection.NORMAL.name

    @TypeConverter
    fun toCardDirection(value: String?): CardDirection = try {
        value?.let { CardDirection.valueOf(it) } ?: CardDirection.NORMAL
    } catch (_: Exception) {
        CardDirection.NORMAL
    }

    @TypeConverter
    fun fromStage(value: Stage?): String = value?.name ?: Stage.DAILY.name

    @TypeConverter
    fun toStage(value: String?): Stage = try {
        value?.let { Stage.valueOf(it) } ?: Stage.DAILY
    } catch (_: Exception) {
        Stage.DAILY
    }

    @TypeConverter
    fun fromDifficulty(value: VocabularyDifficulty?): String = value?.name ?: VocabularyDifficulty.EASY.name

    @TypeConverter
    fun toDifficulty(value: String?): VocabularyDifficulty = try {
        value?.let { VocabularyDifficulty.valueOf(it) } ?: VocabularyDifficulty.EASY
    } catch (_: Exception) {
        VocabularyDifficulty.EASY
    }

    @TypeConverter
    fun fromReviewType(value: ReviewType?): String = value?.name ?: ReviewType.DAILY.name

    @TypeConverter
    fun toReviewType(value: String?): ReviewType = try {
        value?.let { ReviewType.valueOf(it) } ?: ReviewType.DAILY
    } catch (_: Exception) {
        ReviewType.DAILY
    }

    @TypeConverter
    fun fromReviewMode(value: ReviewMode?): String = value?.name ?: ReviewMode.FLASHCARD.name

    @TypeConverter
    fun toReviewMode(value: String?): ReviewMode = try {
        value?.let { ReviewMode.valueOf(it) } ?: ReviewMode.FLASHCARD
    } catch (_: Exception) {
        ReviewMode.FLASHCARD
    }

    @TypeConverter
    fun fromQuizLevel(value: QuizLevel?): String? = value?.name

    @TypeConverter
    fun toQuizLevel(value: String?): QuizLevel? = try {
        value?.let { QuizLevel.valueOf(it) }
    } catch (_: Exception) {
        null
    }

    @TypeConverter
    fun fromSessionStatus(value: SessionStatus?): String = value?.name ?: SessionStatus.ACTIVE.name

    @TypeConverter
    fun toSessionStatus(value: String?): SessionStatus = try {
        value?.let { SessionStatus.valueOf(it) } ?: SessionStatus.ACTIVE
    } catch (_: Exception) {
        SessionStatus.ACTIVE
    }

    @TypeConverter
    fun fromSessionItemState(value: SessionItemState?): String = value?.name ?: SessionItemState.PENDING.name

    @TypeConverter
    fun toSessionItemState(value: String?): SessionItemState = try {
        value?.let { SessionItemState.valueOf(it) } ?: SessionItemState.PENDING
    } catch (_: Exception) {
        SessionItemState.PENDING
    }

    @TypeConverter
    fun fromDuplicatePolicy(value: DuplicatePolicy?): String = value?.name ?: DuplicatePolicy.SKIP.name

    @TypeConverter
    fun toDuplicatePolicy(value: String?): DuplicatePolicy = try {
        value?.let { DuplicatePolicy.valueOf(it) } ?: DuplicatePolicy.SKIP
    } catch (_: Exception) {
        DuplicatePolicy.SKIP
    }

    @TypeConverter
    fun fromImportItemStatus(value: ImportItemStatus?): String = value?.name ?: ImportItemStatus.PENDING.name

    @TypeConverter
    fun toImportItemStatus(value: String?): ImportItemStatus = try {
        value?.let { ImportItemStatus.valueOf(it) } ?: ImportItemStatus.PENDING
    } catch (_: Exception) {
        ImportItemStatus.PENDING
    }
}

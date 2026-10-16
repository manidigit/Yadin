package com.manidigit.yadin.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.manidigit.yadin.domain.model.CardDirection
import com.manidigit.yadin.domain.model.QuizLevel
import com.manidigit.yadin.domain.model.ReviewMode
import com.manidigit.yadin.domain.model.ReviewType
import com.manidigit.yadin.domain.model.Stage

@Entity(
    tableName = "review_history",
    foreignKeys = [
        ForeignKey(
            entity = ReviewSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ConceptEntity::class,
            parentColumns = ["id"],
            childColumns = ["conceptId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
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

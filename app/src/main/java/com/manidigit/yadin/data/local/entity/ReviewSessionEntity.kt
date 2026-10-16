package com.manidigit.yadin.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.manidigit.yadin.domain.model.CardDirection
import com.manidigit.yadin.domain.model.QuizLevel
import com.manidigit.yadin.domain.model.ReviewMode
import com.manidigit.yadin.domain.model.ReviewType
import com.manidigit.yadin.domain.model.SessionStatus

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

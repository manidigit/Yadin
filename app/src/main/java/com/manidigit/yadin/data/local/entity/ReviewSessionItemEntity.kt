package com.manidigit.yadin.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.manidigit.yadin.domain.model.CardDirection
import com.manidigit.yadin.domain.model.SessionItemState

@Entity(
    tableName = "review_session_items",
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

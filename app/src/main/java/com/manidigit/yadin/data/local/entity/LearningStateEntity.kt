package com.manidigit.yadin.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.manidigit.yadin.domain.model.CardDirection
import com.manidigit.yadin.domain.model.Stage

@Entity(
    tableName = "learning_states",
    foreignKeys = [
        ForeignKey(
            entity = ConceptEntity::class,
            parentColumns = ["id"],
            childColumns = ["conceptId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
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

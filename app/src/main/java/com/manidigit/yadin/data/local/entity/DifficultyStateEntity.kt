package com.manidigit.yadin.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.manidigit.yadin.domain.model.CardDirection
import com.manidigit.yadin.domain.model.VocabularyDifficulty

@Entity(
    tableName = "difficulty_states",
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
        Index("current")
    ]
)
data class DifficultyStateEntity(
    @PrimaryKey val id: String,
    val conceptId: String,
    val direction: CardDirection = CardDirection.NORMAL,
    val current: VocabularyDifficulty = VocabularyDifficulty.EASY,
    val consecutiveCorrect: Int = 0,
    val consecutiveWrong: Int = 0,
    val hasReachedVeryHard: Boolean = false
)

package com.manidigit.yadin.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.manidigit.yadin.domain.model.EntryType

@Entity(
    tableName = "concepts",
    indices = [
        Index("categoryId"),
        Index("active")
    ]
)
data class ConceptEntity(
    @PrimaryKey val id: String,
    val entryType: EntryType = EntryType.WORD,
    val categoryId: String? = null,
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

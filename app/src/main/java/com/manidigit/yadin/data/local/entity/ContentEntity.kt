package com.manidigit.yadin.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "contents",
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
    val translationIndex: Int = 0
)

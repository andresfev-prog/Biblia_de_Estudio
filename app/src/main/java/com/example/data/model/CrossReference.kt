package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cross_references")
data class CrossReference(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sourceBook: String,
    val sourceChapter: Int,
    val sourceVerse: Int,
    val targetBook: String,
    val targetChapter: Int,
    val targetVerse: Int,
    val targetText: String,
    val relationNote: String? = null
)

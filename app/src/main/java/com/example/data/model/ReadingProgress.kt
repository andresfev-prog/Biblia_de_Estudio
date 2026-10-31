package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reading_progress")
data class ReadingProgress(
    @PrimaryKey
    val id: Int = 1,
    val bookName: String = "Juan",
    val chapter: Int = 1,
    val verse: Int = 1,
    val version: String = "RVR1960",
    val timestamp: Long = System.currentTimeMillis()
)

package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_verses")
data class FavoriteVerse(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val version: String,
    val bookName: String,
    val chapter: Int,
    val verse: Int,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

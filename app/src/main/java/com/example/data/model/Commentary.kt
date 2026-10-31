package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "commentaries")
data class Commentary(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookName: String,
    val chapter: Int,
    val verse: Int,
    val author: String,
    val title: String,
    val content: String
)
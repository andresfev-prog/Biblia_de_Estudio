package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bible_modules")
data class BibleModule(
    @PrimaryKey
    val id: String,
    val category: String, // "VERSION", "COMMENTARY", "DICTIONARY"
    val title: String,
    val author: String,
    val description: String,
    val language: String, // "Español", "Inglés", "Griego / Hebreo"
    val versionTag: String,
    val sizeText: String,
    val isDownloaded: Boolean = true,
    val downloadUrl: String? = null,
    val fileName: String? = null
)

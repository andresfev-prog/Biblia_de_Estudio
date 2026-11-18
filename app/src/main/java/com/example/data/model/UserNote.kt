package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "user_notes")
data class UserNote(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val biblicalReference: String,
    val timestamp: Long = System.currentTimeMillis(),
    @ColumnInfo(defaultValue = "''")
    val cloudId: String = UUID.randomUUID().toString()
)

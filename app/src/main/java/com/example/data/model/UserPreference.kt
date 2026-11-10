package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_preferences")
data class UserPreference(
    @PrimaryKey
    val id: Int = 1,
    val userName: String = "Andrés Felipe",
    val email: String = "andresfev@gmail.com",
    val location: String = "Cali, Colombia",
    val usernameHandle: String = "andresfev",
    val password: String = "123456",
    val interfaceLanguage: String = "Español",
    val themeMode: String = "AUTOMATIC", // "LIGHT", "DARK", "AUTOMATIC"
    val fontFamily: String = "Google Sans Flex", // "Google Sans Flex", "Aktiv Grotesk", "Untitled Serif", "Serif", "Monospace"
    val fontSizePt: Float = 13f, // 8f to 16f
    val isFirstLaunch: Boolean = true
)

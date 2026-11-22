package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences

/**
 * Caché ligero persistido en SharedPreferences para el estado inicial de lectura
 * (`hasStartedReading`) y el último adelanto saneado de «Continúa donde quedaste».
 *
 * Permite leer el estado en 0 ms al abrir `HomeScreen` sin bloquear el hilo principal
 * ni alterar el esquema de Room.
 */
class ReadingStateCache(context: Context) {

    private val prefs: SharedPreferences = (context.applicationContext ?: context)
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun hasStartedReading(): Boolean =
        prefs.getBoolean(KEY_HAS_STARTED_READING, false)

    fun setHasStartedReading(started: Boolean) {
        if (prefs.getBoolean(KEY_HAS_STARTED_READING, false) != started) {
            prefs.edit().putBoolean(KEY_HAS_STARTED_READING, started).apply()
        }
    }

    fun loadPreviewFor(version: String, bookName: String, chapter: Int, verse: Int): String {
        val expectedKey = buildPreviewKey(version, bookName, chapter, verse)
        val storedKey = prefs.getString(KEY_PREVIEW_REF, null)
        return if (storedKey == expectedKey) {
            prefs.getString(KEY_PREVIEW_TEXT, "").orEmpty()
        } else {
            ""
        }
    }

    fun loadLastPreviewText(): String =
        prefs.getString(KEY_PREVIEW_TEXT, "").orEmpty()

    fun savePreview(version: String, bookName: String, chapter: Int, verse: Int, cleanText: String) {
        val trimmed = cleanText.trim()
        if (trimmed.isBlank()) return
        prefs.edit()
            .putString(KEY_PREVIEW_REF, buildPreviewKey(version, bookName, chapter, verse))
            .putString(KEY_PREVIEW_TEXT, trimmed)
            .apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        const val PREFS_NAME = "biblia_reading_state_cache"
        private const val KEY_HAS_STARTED_READING = "has_started_reading"
        private const val KEY_PREVIEW_REF = "continue_preview_ref"
        private const val KEY_PREVIEW_TEXT = "continue_preview_text"

        private fun buildPreviewKey(version: String, bookName: String, chapter: Int, verse: Int): String {
            return "${version.trim().uppercase()}|${bookName.trim().lowercase()}|$chapter|${verse.coerceAtLeast(1)}"
        }
    }
}

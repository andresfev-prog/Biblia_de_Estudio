package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences

data class CachedReadingPosition(
    val bookName: String,
    val chapter: Int,
    val verse: Int,
    val version: String,
    val parallelVersion: String = "KJV"
)

/**
 * Caché ligero persistido en SharedPreferences para el estado inicial de lectura
 * (`hasStartedReading`), la posición activa (`libro, capítulo, versículo, versión`) y
 * el último adelanto saneado de «Continúa donde quedaste».
 *
 * Permite leer y restaurar el punto exacto de lectura en 0 ms al abrir la app
 * sin bloquear el hilo principal ni alterar el esquema de Room.
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

    fun saveReadingPosition(
        bookName: String,
        chapter: Int,
        verse: Int,
        version: String,
        parallelVersion: String? = null
    ) {
        val cleanBook = bookName.trim()
        val cleanVersion = version.trim()
        if (cleanBook.isBlank() || cleanVersion.isBlank()) return
        val editor = prefs.edit()
            .putBoolean(KEY_HAS_STARTED_READING, true)
            .putString(KEY_LAST_BOOK, cleanBook)
            .putInt(KEY_LAST_CHAPTER, chapter.coerceAtLeast(1))
            .putInt(KEY_LAST_VERSE, verse.coerceAtLeast(1))
            .putString(KEY_LAST_VERSION, cleanVersion)
        if (!parallelVersion.isNullOrBlank()) {
            editor.putString(KEY_LAST_PARALLEL_VERSION, parallelVersion.trim())
        }
        editor.apply()
    }

    fun saveParallelVersion(parallelVersion: String) {
        val cleanParallel = parallelVersion.trim()
        if (cleanParallel.isBlank()) return
        prefs.edit().putString(KEY_LAST_PARALLEL_VERSION, cleanParallel).apply()
    }

    fun loadReadingPosition(): CachedReadingPosition? {
        val book = prefs.getString(KEY_LAST_BOOK, null)?.trim().orEmpty()
        val version = prefs.getString(KEY_LAST_VERSION, null)?.trim().orEmpty()
        if (book.isBlank() || version.isBlank()) return null
        val chapter = prefs.getInt(KEY_LAST_CHAPTER, 1).coerceAtLeast(1)
        val verse = prefs.getInt(KEY_LAST_VERSE, 1).coerceAtLeast(1)
        val parallelVersion = prefs.getString(KEY_LAST_PARALLEL_VERSION, "KJV")
            ?.trim()
            ?.ifBlank { "KJV" }
            ?: "KJV"
        return CachedReadingPosition(
            bookName = book,
            chapter = chapter,
            verse = verse,
            version = version,
            parallelVersion = parallelVersion
        )
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
        private const val KEY_LAST_BOOK = "last_reading_book"
        private const val KEY_LAST_CHAPTER = "last_reading_chapter"
        private const val KEY_LAST_VERSE = "last_reading_verse"
        private const val KEY_LAST_VERSION = "last_reading_version"
        private const val KEY_LAST_PARALLEL_VERSION = "last_parallel_version"
        private const val KEY_PREVIEW_REF = "continue_preview_ref"
        private const val KEY_PREVIEW_TEXT = "continue_preview_text"

        private fun buildPreviewKey(version: String, bookName: String, chapter: Int, verse: Int): String {
            return "${version.trim().uppercase()}|${bookName.trim().lowercase()}|$chapter|${verse.coerceAtLeast(1)}"
        }
    }
}

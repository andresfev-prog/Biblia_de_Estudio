package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject

data class NoteDraft(
    val draftKey: String,
    val noteId: Long,
    val title: String,
    val reference: String,
    val content: String,
    val updatedAt: Long
)

/**
 * Caché local persistido en SharedPreferences para borradores temporales del editor de notas.
 *
 * Guarda y restaura el estado en curso (título, referencia bíblica y contenido) asociado
 * al `noteId` (cuando se edita una nota existente) o al contexto de nota nueva (`new_note`),
 * evitando cualquier pérdida de texto ante cierre accidental, retroceso del sistema o cambio de app.
 */
class NoteDraftCache(context: Context) {

    private val prefs: SharedPreferences = (context.applicationContext ?: context)
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveDraft(
        noteId: Long,
        title: String,
        reference: String,
        content: String,
        contextKey: String = "",
        baselineTitle: String = "",
        baselineReference: String = "",
        baselineContent: String = ""
    ): Boolean {
        val key = buildDraftKey(noteId = noteId, contextKey = contextKey)
        val isAllBlank = title.isBlank() && reference.isBlank() && content.isBlank()
        val matchesBaseline = noteId > 0L &&
            title == baselineTitle &&
            reference == baselineReference &&
            content == baselineContent

        if (isAllBlank || matchesBaseline) {
            clearDraftByKey(key)
            return false
        }

        val json = JSONObject().apply {
            put(FIELD_NOTE_ID, noteId)
            put(FIELD_TITLE, title)
            put(FIELD_REFERENCE, reference)
            put(FIELD_CONTENT, content)
            put(FIELD_UPDATED_AT, System.currentTimeMillis())
        }
        prefs.edit().putString(storageKey(key), json.toString()).apply()
        return true
    }

    fun loadDraft(noteId: Long, contextKey: String = ""): NoteDraft? {
        val key = buildDraftKey(noteId = noteId, contextKey = contextKey)
        val raw = prefs.getString(storageKey(key), null) ?: return null
        return try {
            val json = JSONObject(raw)
            val title = json.optString(FIELD_TITLE, "")
            val reference = json.optString(FIELD_REFERENCE, "")
            val content = json.optString(FIELD_CONTENT, "")
            if (title.isBlank() && reference.isBlank() && content.isBlank()) {
                clearDraftByKey(key)
                null
            } else {
                NoteDraft(
                    draftKey = key,
                    noteId = json.optLong(FIELD_NOTE_ID, noteId),
                    title = title,
                    reference = reference,
                    content = content,
                    updatedAt = json.optLong(FIELD_UPDATED_AT, 0L)
                )
            }
        } catch (_: Exception) {
            clearDraftByKey(key)
            null
        }
    }

    fun hasDraft(noteId: Long, contextKey: String = ""): Boolean =
        loadDraft(noteId = noteId, contextKey = contextKey) != null

    fun clearDraft(noteId: Long, contextKey: String = "") {
        clearDraftByKey(buildDraftKey(noteId = noteId, contextKey = contextKey))
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    private fun clearDraftByKey(key: String) {
        val prefKey = storageKey(key)
        if (prefs.contains(prefKey)) {
            prefs.edit().remove(prefKey).apply()
        }
    }

    companion object {
        const val PREFS_NAME = "biblia_note_draft_cache"
        private const val KEY_PREFIX = "draft_"
        private const val FIELD_NOTE_ID = "noteId"
        private const val FIELD_TITLE = "title"
        private const val FIELD_REFERENCE = "reference"
        private const val FIELD_CONTENT = "content"
        private const val FIELD_UPDATED_AT = "updatedAt"

        fun buildDraftKey(noteId: Long, contextKey: String = ""): String {
            return when {
                noteId > 0L -> "note_$noteId"
                contextKey.isNotBlank() -> "new_${contextKey.trim().lowercase()}"
                else -> "new_note"
            }
        }

        private fun storageKey(draftKey: String): String = "$KEY_PREFIX$draftKey"
    }
}

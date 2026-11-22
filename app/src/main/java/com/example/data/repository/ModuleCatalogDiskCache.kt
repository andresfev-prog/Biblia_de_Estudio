package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.ModuleBibleCatalog

/**
 * Caché persistente en disco para [ModuleBibleCatalog] respaldado por SharedPreferences.
 *
 * Cada entrada almacena una huella `(fileSize, lastModified)` del archivo `.SQLite3`
 * correspondiente para invalidarse automáticamente si el módulo se actualiza, reemplaza
 * o vuelve a descargar. En arranques posteriores permite reconstruir el catálogo en <1 ms
 * sin ejecutar escaneos de la tabla `verses`.
 */
class ModuleCatalogDiskCache(context: Context) {

    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    fun get(
        version: String,
        dbFileName: String,
        fileSize: Long,
        lastModified: Long
    ): ModuleBibleCatalog? {
        if (dbFileName.isBlank() || fileSize <= 0L) return null
        val key = keyFor(dbFileName)
        val raw = prefs.getString(key, null) ?: return null
        return decode(raw, version, fileSize, lastModified)
    }

    fun put(
        dbFileName: String,
        fileSize: Long,
        lastModified: Long,
        catalog: ModuleBibleCatalog
    ) {
        if (dbFileName.isBlank() || fileSize <= 0L || catalog.chaptersByBookNumber.isEmpty()) return
        val encoded = encode(fileSize, lastModified, catalog.chaptersByBookNumber)
        prefs.edit().putString(keyFor(dbFileName), encoded).apply()
    }

    fun invalidate(dbFileName: String? = null) {
        if (dbFileName.isNullOrBlank()) {
            prefs.edit().clear().apply()
        } else {
            prefs.edit().remove(keyFor(dbFileName)).apply()
        }
    }

    companion object {
        private const val PREFS_NAME = "bible_module_catalog_cache_v1"
        private const val FORMAT_VERSION = "v1"

        private fun keyFor(dbFileName: String): String =
            "cat_${dbFileName.trim().lowercase()}"

        internal fun encode(
            fileSize: Long,
            lastModified: Long,
            chaptersByBook: Map<Int, List<Int>>
        ): String {
            val sb = StringBuilder(384)
            sb.append(FORMAT_VERSION)
                .append('|')
                .append(fileSize)
                .append('|')
                .append(lastModified)
                .append('|')

            var firstBook = true
            for (bookId in chaptersByBook.keys.sorted()) {
                val chapters = chaptersByBook[bookId]?.filter { it > 0 }?.distinct()?.sorted().orEmpty()
                if (chapters.isEmpty()) continue
                if (!firstBook) sb.append(';')
                firstBook = false
                sb.append(bookId).append(':')
                appendCompactRanges(sb, chapters)
            }
            return sb.toString()
        }

        internal fun decode(
            raw: String,
            version: String,
            expectedFileSize: Long,
            expectedLastModified: Long
        ): ModuleBibleCatalog? {
            return try {
                val parts = raw.split('|', limit = 4)
                if (parts.size != 4 || parts[0] != FORMAT_VERSION) return null
                val storedSize = parts[1].toLongOrNull() ?: return null
                val storedMtime = parts[2].toLongOrNull() ?: return null
                if (storedSize != expectedFileSize || storedMtime != expectedLastModified) {
                    return null
                }

                val payload = parts[3]
                if (payload.isBlank()) return null

                val chaptersByBook = linkedMapOf<Int, List<Int>>()
                for (entry in payload.split(';')) {
                    if (entry.isBlank()) continue
                    val sep = entry.indexOf(':')
                    if (sep <= 0 || sep >= entry.lastIndex) return null
                    val bookId = entry.substring(0, sep).toIntOrNull() ?: return null
                    val rangesPart = entry.substring(sep + 1)
                    val chapters = mutableListOf<Int>()
                    for (token in rangesPart.split(',')) {
                        if (token.isBlank()) continue
                        val dash = token.indexOf('-')
                        if (dash > 0) {
                            val start = token.substring(0, dash).toIntOrNull() ?: return null
                            val end = token.substring(dash + 1).toIntOrNull() ?: return null
                            if (start <= 0 || end < start) return null
                            for (ch in start..end) {
                                chapters.add(ch)
                            }
                        } else {
                            val ch = token.toIntOrNull() ?: return null
                            if (ch <= 0) return null
                            chapters.add(ch)
                        }
                    }
                    if (chapters.isNotEmpty()) {
                        chaptersByBook[bookId] = chapters
                    }
                }

                if (chaptersByBook.isEmpty()) return null
                ModuleBibleCatalog.fromChaptersMap(
                    version = version,
                    chaptersByCanonicalBook = chaptersByBook
                )
            } catch (_: Exception) {
                null
            }
        }

        private fun appendCompactRanges(sb: StringBuilder, sortedChapters: List<Int>) {
            var rangeStart = sortedChapters[0]
            var rangePrev = rangeStart
            var firstRange = true

            for (i in 1 until sortedChapters.size) {
                val current = sortedChapters[i]
                if (current == rangePrev + 1) {
                    rangePrev = current
                } else {
                    if (!firstRange) sb.append(',')
                    firstRange = false
                    if (rangeStart == rangePrev) {
                        sb.append(rangeStart)
                    } else {
                        sb.append(rangeStart).append('-').append(rangePrev)
                    }
                    rangeStart = current
                    rangePrev = current
                }
            }
            if (!firstRange) sb.append(',')
            if (rangeStart == rangePrev) {
                sb.append(rangeStart)
            } else {
                sb.append(rangeStart).append('-').append(rangePrev)
            }
        }
    }
}

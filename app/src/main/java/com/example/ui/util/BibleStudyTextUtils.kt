package com.example.ui.util

import com.example.data.model.BibleBooksData

sealed class VerseDisplayToken {
    data class BracketNote(val marker: String) : VerseDisplayToken()
    data class AsteriskNote(val ordinal: Int) : VerseDisplayToken()
    data class Word(val text: String) : VerseDisplayToken()
    data class Other(val text: String) : VerseDisplayToken()
}

object BibleStudyTextUtils {

    const val TEMP_HIGHLIGHT_DURATION_MS: Long = 3_000L
    const val RVR_NOTE_MARKER_PREFIX: String = "*:"

    const val SPANISH_FALLBACK_SUBHEADINGS_DB: String = "RVR1960-s.subheadings.SQLite3"
    const val ENGLISH_FALLBACK_SUBHEADINGS_DB: String = "KJV-s.subheadings.SQLite3"

    private val strongAndHtmlRegex = Regex("<S>.*?</S>|<.*?>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
    private val trailingStrongDigitsRegex = Regex("(?<=\\p{L})\\d+")
    private val whitespaceRegex = Regex("\\s+")
    private val rvrSpaceBeforeAsteriskRegex = Regex("(\\S)\\s+\\*")
    private val rvrSpuriousCommaAfterAsteriskRegex = Regex("\\*\\s*,")
    private val rvrMissingSpaceAfterAsteriskRegex = Regex("\\*(?=[\\p{L}\\p{Nd}¿¡«\"(])")
    private val myBibleCrossRefTagRegex = Regex("<x>\\s*(\\d+)\\s+([^<]+)</x>", RegexOption.IGNORE_CASE)
    private val anyHtmlTagRegex = Regex("<[^>]*>")
    private val parallelRefLineRegex = Regex("^\\(.*\\d+:\\d+.*\\)$")

    private val tokenPatternWithAsterisk = Regex("(\\[(\\d+)\\])|(\\*)|([\\p{L}\\p{Nd}]+)|([^\\s\\[\\]*\\p{L}\\p{Nd}]+)|(\\s+)")
    private val tokenPatternStandard = Regex("(\\[(\\d+)\\])|([\\p{L}\\p{Nd}]+)|([^\\s\\[\\]\\p{L}\\p{Nd}]+)|(\\s+)")

    fun isRvr1960Version(version: String): Boolean {
        val normalized = version.trim().uppercase().replace(" ", "")
        return normalized.contains("RVR1960") ||
            normalized == "RV1960" ||
            normalized == "RV60" ||
            normalized.contains("REINA-VALERA1960") ||
            normalized.contains("REINAVALERA1960")
    }

    fun isRvrAsteriskMarker(marker: String): Boolean {
        val trimmed = marker.trim()
        return trimmed == "*" || trimmed.startsWith(RVR_NOTE_MARKER_PREFIX)
    }

    fun extractAsteriskOrdinal(marker: String): Int? {
        val trimmed = marker.trim()
        if (!trimmed.startsWith(RVR_NOTE_MARKER_PREFIX)) return null
        return trimmed.removePrefix(RVR_NOTE_MARKER_PREFIX).toIntOrNull()
    }

    /**
     * Limpia etiquetas MyBible/Strong y, para RVR1960, elimina artefactos visuales
     * alrededor del asterisco de nota (`» *`, `*,`, `* ,`) sin dañar la puntuación real.
     */
    fun sanitizeVerseTextForDisplay(rawText: String, version: String): String {
        var cleaned = rawText
            .replace(strongAndHtmlRegex, "")
            .replace(trailingStrongDigitsRegex, "")
            .replace(whitespaceRegex, " ")
            .trim()

        if (isRvr1960Version(version) && cleaned.contains('*')) {
            cleaned = cleaned
                .replace(rvrSpaceBeforeAsteriskRegex, "$1*")
                .replace(rvrSpuriousCommaAfterAsteriskRegex, "*")
                .replace(rvrMissingSpaceAfterAsteriskRegex, "* ")
                .replace(whitespaceRegex, " ")
                .trim()
        }

        return cleaned
    }

    /**
     * Limpia el texto de un versículo para TTS, eliminando asteriscos y marcadores de nota
     * para evitar lecturas artificiales en voz alta.
     */
    fun sanitizeVerseTextForSpeech(rawText: String, version: String): String {
        val base = sanitizeVerseTextForDisplay(rawText, version)
        return DailyVerseCatalog.cleanBibleText(base)
            .replace("*", "")
            .replace(whitespaceRegex, " ")
            .trim()
    }

    /**
     * Tokeniza el versículo en notas `[n]`, asteriscos clicables `*` (solo RVR1960),
     * palabras clicables y puntuación/espacios.
     */
    fun tokenizeVerseText(displayText: String, isRvr1960: Boolean): List<VerseDisplayToken> {
        if (displayText.isEmpty()) return emptyList()
        val tokens = mutableListOf<VerseDisplayToken>()
        var asteriskCounter = 0

        if (isRvr1960) {
            tokenPatternWithAsterisk.findAll(displayText).forEach { match ->
                val bracketMarker = match.groups[2]?.value
                val asterisk = match.groups[3]?.value
                val word = match.groups[4]?.value
                when {
                    bracketMarker != null -> tokens.add(VerseDisplayToken.BracketNote(bracketMarker))
                    asterisk != null -> {
                        asteriskCounter += 1
                        tokens.add(VerseDisplayToken.AsteriskNote(asteriskCounter))
                    }
                    word != null -> tokens.add(VerseDisplayToken.Word(word))
                    else -> tokens.add(VerseDisplayToken.Other(match.value))
                }
            }
        } else {
            tokenPatternStandard.findAll(displayText).forEach { match ->
                val bracketMarker = match.groups[2]?.value
                val word = match.groups[3]?.value
                when {
                    bracketMarker != null -> tokens.add(VerseDisplayToken.BracketNote(bracketMarker))
                    word != null -> tokens.add(VerseDisplayToken.Word(word))
                    else -> tokens.add(VerseDisplayToken.Other(match.value))
                }
            }
        }

        return tokens
    }

    /**
     * Resuelve el archivo SQLite3 de subtítulos empaquetado en `assets` según el idioma
     * de la versión bíblica activa (español -> RVR1960-s, inglés -> KJV-s).
     */
    fun resolveFallbackSubheadingsDbName(version: String): String {
        return if (BibleBooksData.isEnglishVersion(version)) {
            ENGLISH_FALLBACK_SUBHEADINGS_DB
        } else {
            SPANISH_FALLBACK_SUBHEADINGS_DB
        }
    }

    /**
     * Limpia etiquetas HTML/MyBible (incluyendo referencias `<x>book_number cap:ver</x>`)
     * en los subtítulos de pasaje.
     */
    fun cleanSubheadingText(raw: String, isEnglish: Boolean): String {
        if (raw.isBlank()) return ""
        val withResolvedRefs = myBibleCrossRefTagRegex.replace(raw) { match ->
            val myBibleBookNum = match.groupValues[1].toIntOrNull()
            val refTail = match.groupValues[2].trim()
            val canonicalId = myBibleBookNum?.let { BibleBooksData.resolveCanonicalBookId("", it) }
            if (canonicalId != null) {
                val localizedBook = BibleBooksData.getLocalizedBookName(canonicalId, isEnglish)
                "$localizedBook $refTail"
            } else {
                refTail
            }
        }

        return decodeHtmlEntities(withResolvedRefs.replace(anyHtmlTagRegex, ""))
            .replace(whitespaceRegex, " ")
            .trim()
    }

    fun isParallelReferenceSubheading(subheading: String): Boolean {
        return parallelRefLineRegex.matches(subheading.trim())
    }

    /**
     * Alinea de forma segura los subtítulos por número de versículo con los versículos reales
     * del capítulo (evitando fallos o duplicados cuando hay diferencias de numeración como en Salmos).
     */
    fun alignSubheadingsToVerses(
        rawByVerse: Map<Int, List<String>>,
        chapterVerseNumbers: List<Int>
    ): Map<Int, List<String>> {
        if (rawByVerse.isEmpty() || chapterVerseNumbers.isEmpty()) return emptyMap()
        val sortedVerses = chapterVerseNumbers.distinct().sorted()
        val validSet = sortedVerses.toSet()
        val firstVerse = sortedVerses.first()
        val lastVerse = sortedVerses.last()

        val aligned = linkedMapOf<Int, MutableList<String>>()
        for ((rawVerse, titles) in rawByVerse.entries.sortedBy { it.key }) {
            val nonBlank = titles.map { it.trim() }.filter { it.isNotBlank() }
            if (nonBlank.isEmpty()) continue

            val targetVerse = when {
                rawVerse in validSet -> rawVerse
                rawVerse <= firstVerse -> firstVerse
                rawVerse <= lastVerse -> sortedVerses.firstOrNull { it >= rawVerse } ?: sortedVerses.lastOrNull { it <= rawVerse }
                else -> null
            } ?: continue

            val bucket = aligned.getOrPut(targetVerse) { mutableListOf() }
            for (title in nonBlank) {
                if (bucket.none { it.equals(title, ignoreCase = true) }) {
                    bucket.add(title)
                }
            }
        }

        return aligned.mapValues { it.value.toList() }
    }

    /**
     * Prioriza siempre los subtítulos propios del módulo cuando el módulo posee tabla de subtítulos
     * nativa (`stories` / `subheadings`), evitando duplicar con el respaldo de `assets`.
     */
    fun resolveEffectiveChapterSubheadings(
        hasNativeSubheadingsTable: Boolean,
        nativeSubheadingsByVerse: Map<Int, List<String>>,
        fallbackSubheadingsByVerse: Map<Int, List<String>>,
        chapterVerseNumbers: List<Int>
    ): Map<Int, List<String>> {
        val chosen = if (hasNativeSubheadingsTable) {
            nativeSubheadingsByVerse
        } else {
            fallbackSubheadingsByVerse
        }
        return alignSubheadingsToVerses(chosen, chapterVerseNumbers)
    }

    /**
     * Convierte HTML ligero de comentarios/notas MyBible a texto limpio con saltos de párrafo legibles.
     */
    fun formatCommentaryHtml(rawHtml: String): String {
        if (rawHtml.isBlank()) return ""
        var text = rawHtml
            .replace(Regex("(?i)<p\\s*/?>"), "\n\n")
            .replace(Regex("(?i)</p>"), "\n\n")
            .replace(Regex("(?i)<br\\s*/?>"), "\n")
            .replace(Regex("(?i)<li[^>]*>"), "\n• ")
            .replace(Regex("(?i)</li>"), "")
            .replace(Regex("(?i)</?(ul|ol|div|h[1-6])[^>]*>"), "\n")
            .replace(anyHtmlTagRegex, "")

        text = decodeHtmlEntities(text)
            .replace(Regex("[ \\t]+"), " ")

        return text.lines()
            .map { it.trim() }
            .joinToString("\n")
            .replace(Regex("\\n{3,}"), "\n\n")
            .trim()
    }

    private fun decodeHtmlEntities(text: String): String {
        return text
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&apos;", "'")
            .replace("&mdash;", "—")
            .replace("&ndash;", "–")
    }
}

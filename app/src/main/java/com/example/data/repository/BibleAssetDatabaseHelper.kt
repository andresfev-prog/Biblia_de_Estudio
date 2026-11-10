package com.example.data.repository

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.example.data.model.CrossReference
import com.example.data.model.DictionaryEntry
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class BibleAssetDatabaseHelper(private val context: Context) {

    companion object {

        private val extractionLock = Any()

        fun ensureDatabaseExtracted(context: Context, databaseName: String): File {
            val dbFile = context.getDatabasePath(databaseName)

            if (dbFile.exists() && dbFile.length() > 0L) {
                return dbFile
            }

            synchronized(extractionLock) {
                if (dbFile.exists() && dbFile.length() > 0L) {
                    return dbFile
                }

                dbFile.parentFile?.let { parent ->
                    if (!parent.exists()) {
                        parent.mkdirs()
                    }
                }

                val tempFile = File(dbFile.parentFile, "$databaseName.tmp")
                Log.i("BIBLE_DEBUG", "Iniciando extracción atómica de $databaseName...")

                var inputStream: InputStream? = null
                var outputStream: FileOutputStream? = null

                try {
                    inputStream = try {
                        context.assets.open("databases/$databaseName")
                    } catch (e: Exception) {
                        context.assets.open(databaseName)
                    }

                    outputStream = FileOutputStream(tempFile)
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalBytes = 0L

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        totalBytes += bytesRead
                    }
                    outputStream.flush()

                    if (tempFile.renameTo(dbFile)) {
                        Log.i("BIBLE_DEBUG", "Extracción completada: $totalBytes bytes en ${dbFile.path}")
                    } else {
                        tempFile.copyTo(dbFile, overwrite = true)
                        tempFile.delete()
                        Log.i("BIBLE_DEBUG", "Extracción completada vía copyTo: $totalBytes bytes.")
                    }
                } catch (e: Exception) {
                    Log.e("BIBLE_DEBUG", "Fallo al extraer $databaseName desde assets", e)
                    if (tempFile.exists()) tempFile.delete()
                    if (dbFile.exists() && dbFile.length() == 0L) dbFile.delete()
                } finally {
                    try { inputStream?.close() } catch (_: Exception) {}
                    try { outputStream?.close() } catch (_: Exception) {}
                }
            }

            return dbFile
        }

        fun openDatabaseReadOnly(context: Context, databaseName: String): SQLiteDatabase? {
            val dbFile = ensureDatabaseExtracted(context, databaseName)
            if (!dbFile.exists() || dbFile.length() == 0L) {
                Log.e("BIBLE_DEBUG", "No se puede abrir $databaseName: archivo ausente o vacío.")
                return null
            }

            return try {
                SQLiteDatabase.openDatabase(
                    dbFile.path,
                    null,
                    SQLiteDatabase.OPEN_READONLY
                )
            } catch (e: Exception) {
                Log.e("BIBLE_DEBUG", "Fallo al abrir SQLite en ${dbFile.path}", e)
                null
            }
        }

        private val BOOK_NUMBER_MAP: Map<String, Int> = mapOf(
            "gn" to 10, "gen" to 10, "genesis" to 10, "génesis" to 10,
            "ex" to 20, "exo" to 20, "exodo" to 20, "éxodo" to 20, "exodus" to 20,
            "lv" to 30, "lev" to 30, "levitico" to 30, "levítico" to 30, "leviticus" to 30,
            "nm" to 40, "num" to 40, "numeros" to 40, "números" to 40, "numbers" to 40,
            "dt" to 50, "deu" to 50, "deuteronomio" to 50, "deuteronomy" to 50,
            "jos" to 60, "josh" to 60, "josue" to 60, "josué" to 60, "joshua" to 60,
            "jue" to 70, "judg" to 70, "jueces" to 70, "judges" to 70,
            "rt" to 80, "ruth" to 80, "rut" to 80,
            "1s" to 90, "1sam" to 90, "1 samuel" to 90, "1samuel" to 90,
            "2s" to 100, "2sam" to 100, "2 samuel" to 100, "2samuel" to 100,
            "1r" to 110, "1kin" to 110, "1 reyes" to 110, "1reyes" to 110, "1 kings" to 110,
            "2r" to 120, "2kin" to 120, "2 reyes" to 120, "2reyes" to 120, "2 kings" to 120,
            "1cr" to 130, "1chr" to 130, "1 cronicas" to 130, "1 crónicas" to 130, "1 chronicles" to 130,
            "2cr" to 140, "2chr" to 140, "2 cronicas" to 140, "2 crónicas" to 140, "2 chronicles" to 140,
            "esd" to 150, "ezr" to 150, "esdras" to 150, "ezra" to 150,
            "neh" to 160, "nehemias" to 160, "nehemías" to 160, "nehemiah" to 160,
            "est" to 190, "esth" to 190, "ester" to 190, "esther" to 190,
            "job" to 220,
            "sal" to 230, "ps" to 230, "salmos" to 230, "salmo" to 230, "psalms" to 230, "psalm" to 230,
            "pr" to 240, "prov" to 240, "proverbios" to 240, "proverbs" to 240,
            "ec" to 250, "eccl" to 250, "eclesiastes" to 250, "eclesiastés" to 250, "ecclesiastes" to 250,
            "cnt" to 260, "song" to 260, "cantar de los cantares" to 260, "cantares" to 260, "song of solomon" to 260,
            "is" to 290, "isa" to 290, "isaias" to 290, "isaías" to 290, "isaiah" to 290,
            "jer" to 300, "jeremias" to 300, "jeremías" to 300, "jeremiah" to 300,
            "lm" to 310, "lam" to 310, "lamentaciones" to 310, "lamentations" to 310,
            "ez" to 330, "ezek" to 330, "ezequiel" to 330, "ezekiel" to 330,
            "dn" to 340, "dan" to 340, "daniel" to 340,
            "os" to 350, "hos" to 350, "oseas" to 350, "hosea" to 350,
            "jl" to 360, "joel" to 360,
            "am" to 370, "amos" to 370, "amós" to 370,
            "abd" to 380, "oba" to 380, "abdias" to 380, "abdías" to 380, "obadiah" to 380,
            "jon" to 390, "jona" to 390, "jonas" to 390, "jonás" to 390, "jonah" to 390,
            "mi" to 400, "mic" to 400, "miqueas" to 400, "micah" to 400,
            "nah" to 410, "nahum" to 410, "nahúm" to 410,
            "hab" to 420, "habacuc" to 420, "habakkuk" to 420,
            "sof" to 430, "zeph" to 430, "sofonias" to 430, "sofonías" to 430, "zephaniah" to 430,
            "hag" to 440, "hageo" to 440, "haggai" to 440,
            "zac" to 450, "zech" to 450, "zacarias" to 450, "zacarías" to 450, "zechariah" to 450,
            "mal" to 460, "malaquias" to 460, "malaquías" to 460, "malachi" to 460,
            "mt" to 470, "mat" to 470, "mateo" to 470, "matthew" to 470,
            "mr" to 480, "mar" to 480, "marcos" to 480, "mark" to 480,
            "lc" to 490, "luk" to 490, "lucas" to 490, "luke" to 490,
            "jn" to 500, "john" to 500, "juan" to 500,
            "hch" to 510, "acts" to 510, "hechos" to 510, "hechos de los apostoles" to 510, "hechos de los apóstoles" to 510,
            "ro" to 520, "rom" to 520, "romanos" to 520, "romans" to 520,
            "1co" to 530, "1cor" to 530, "1 corintios" to 530, "1corintios" to 530, "1 corinthians" to 530,
            "2co" to 540, "2cor" to 540, "2 corintios" to 540, "2corintios" to 540, "2 corinthians" to 540,
            "ga" to 550, "gal" to 550, "galatas" to 550, "gálatas" to 550, "galatians" to 550,
            "ef" to 560, "eph" to 560, "efesios" to 560, "ephesians" to 560,
            "fil" to 570, "phil" to 570, "filipenses" to 570, "philippians" to 570,
            "col" to 580, "colosenses" to 580, "colossians" to 580,
            "1ts" to 590, "1ths" to 590, "1 tesalonicenses" to 590, "1 thessalonians" to 590,
            "2ts" to 600, "2ths" to 600, "2 tesalonicenses" to 600, "2 thessalonians" to 600,
            "1ti" to 610, "1tim" to 610, "1 timoteo" to 610, "1 timothy" to 610,
            "2ti" to 620, "2tim" to 620, "2 timoteo" to 620, "2 timothy" to 620,
            "tit" to 630, "tito" to 630, "titus" to 630,
            "flm" to 640, "phlm" to 640, "filemon" to 640, "filemón" to 640, "philemon" to 640,
            "he" to 650, "heb" to 650, "hebreos" to 650, "hebrews" to 650,
            "stg" to 660, "jam" to 660, "santiago" to 660, "james" to 660,
            "1p" to 670, "1pet" to 670, "1 pedro" to 670, "1 peter" to 670,
            "2p" to 680, "2pet" to 680, "2 pedro" to 680, "2 peter" to 680,
            "1jn" to 690, "1 juan" to 690, "1 john" to 690,
            "2jn" to 700, "2 juan" to 700, "2 john" to 700,
            "3jn" to 710, "3 juan" to 710, "3 john" to 710,
            "jud" to 720, "judas" to 720, "jude" to 720,
            "ap" to 730, "rev" to 730, "apocalipsis" to 730, "apocalipsis (de juan)" to 730, "revelation" to 730
        )

        private val NUMBER_TO_BOOK_MAP: Map<Int, String> = mapOf(
            10 to "Génesis", 20 to "Éxodo", 30 to "Levítico", 40 to "Números", 50 to "Deuteronomio",
            60 to "Josué", 70 to "Jueces", 80 to "Rut", 90 to "1 Samuel", 100 to "2 Samuel",
            110 to "1 Reyes", 120 to "2 Reyes", 130 to "1 Crónicas", 140 to "2 Crónicas",
            150 to "Esdras", 160 to "Nehemías", 190 to "Ester", 220 to "Job", 230 to "Salmos",
            240 to "Proverbios", 250 to "Eclesiastés", 260 to "Cantares", 290 to "Isaías",
            300 to "Jeremías", 310 to "Lamentaciones", 330 to "Ezequiel", 340 to "Daniel",
            350 to "Oseas", 360 to "Joel", 370 to "Amós", 380 to "Abdías", 390 to "Jonás",
            400 to "Miqueas", 410 to "Nahúm", 420 to "Habacuc", 430 to "Sofonías", 440 to "Hageo",
            450 to "Zacarías", 460 to "Malaquías", 470 to "Mateo", 480 to "Marcos", 490 to "Lucas",
            500 to "Juan", 510 to "Hechos", 520 to "Romanos", 530 to "1 Corintios", 540 to "2 Corintios",
            550 to "Gálatas", 560 to "Efesios", 570 to "Filipenses", 580 to "Colosenses",
            590 to "1 Tesalonicenses", 600 to "2 Tesalonicenses", 610 to "1 Timoteo", 620 to "2 Timoteo",
            630 to "Tito", 640 to "Filemón", 650 to "Hebreos", 660 to "Santiago", 670 to "1 Pedro",
            680 to "2 Pedro", 690 to "1 Juan", 700 to "2 Juan", 710 to "3 Juan", 720 to "Judas",
            730 to "Apocalipsis"
        )

        fun resolveBookNumber(bookNameOrAbbreviation: String): Int {
            val normalized = bookNameOrAbbreviation.trim().lowercase()
            return BOOK_NUMBER_MAP[normalized] ?: 10
        }

        fun resolveBookName(bookNumber: Int): String {
            return NUMBER_TO_BOOK_MAP[bookNumber] ?: ""
        }
    }

    fun getVersesFromAsset(dbName: String, bookNameOrAbbr: String, chapter: Int): List<Pair<Int, String>> {
        val result = mutableListOf<Pair<Int, String>>()
        val database = openDatabaseReadOnly(context, dbName) ?: return emptyList()

        val bookNumber = resolveBookNumber(bookNameOrAbbr)
        val query = "SELECT verse, text FROM verses WHERE book_number = ? AND chapter = ? ORDER BY verse ASC"
        val selectionArgs = arrayOf(bookNumber.toString(), chapter.toString())

        try {
            database.rawQuery(query, selectionArgs).use { cursor ->
                val verseIndex = cursor.getColumnIndex("verse")
                val textIndex = cursor.getColumnIndex("text")

                while (cursor.moveToNext()) {
                    val verseNum = if (verseIndex >= 0) cursor.getInt(verseIndex) else 0
                    val rawText = if (textIndex >= 0) cursor.getString(textIndex) else ""
                    val cleanText = rawText
                        .replace(Regex("<S>.*?</S>|<.*?>"), "")
                        .replace(Regex("(?<=\\p{L})\\d+"), "")
                        .replace(Regex("\\s+"), " ")
                        .trim()
                    result.add(Pair(verseNum, cleanText))
                }
            }
        } catch (e: Exception) {
            Log.e("BIBLE_DEBUG", "Error consultando versículos en $dbName: ${e.message}", e)
        } finally {
            database.close()
        }

        return result
    }

    fun getRawVerseText(dbName: String, bookNameOrAbbr: String, chapter: Int, verse: Int): String? {
        val database = openDatabaseReadOnly(context, dbName) ?: return null
        val bookNumber = resolveBookNumber(bookNameOrAbbr)
        val query = "SELECT text FROM verses WHERE book_number = ? AND chapter = ? AND verse = ? LIMIT 1"
        var rawText: String? = null

        try {
            database.rawQuery(query, arrayOf(bookNumber.toString(), chapter.toString(), verse.toString())).use { cursor ->
                if (cursor.moveToFirst()) {
                    val textIndex = cursor.getColumnIndex("text")
                    if (textIndex >= 0) {
                        rawText = cursor.getString(textIndex)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("BIBLE_DEBUG", "Error obteniendo texto crudo en $dbName: ${e.message}", e)
        } finally {
            database.close()
        }

        return rawText
    }

    fun extractStrongCodeForWord(rawText: String, targetWord: String, isOldTestament: Boolean): String? {
        if (rawText.isBlank() || targetWord.isBlank()) return null

        val escapedWord = Regex.escape(targetWord)
        val pattern = Regex("(?i)$escapedWord(?:<S>([GH]?\\d+)</S>|(\\d+))")
        val match = pattern.find(rawText) ?: return null

        val rawCode = match.groups[1]?.value ?: match.groups[2]?.value ?: return null
        return if (rawCode.startsWith("G", true) || rawCode.startsWith("H", true)) {
            rawCode.uppercase()
        } else {
            if (isOldTestament) "H$rawCode" else "G$rawCode"
        }
    }

    fun getVersesWithComments(dbName: String, bookNameOrAbbr: String, chapter: Int): Set<Int> {
        val result = mutableSetOf<Int>()
        val database = openDatabaseReadOnly(context, dbName) ?: return emptySet()

        val bookNumber = resolveBookNumber(bookNameOrAbbr)
        val query = "SELECT DISTINCT verse_number_from FROM commentaries WHERE book_number = ? AND chapter_number_from = ?"
        val selectionArgs = arrayOf(bookNumber.toString(), chapter.toString())

        try {
            database.rawQuery(query, selectionArgs).use { cursor ->
                val verseIndex = cursor.getColumnIndex("verse_number_from")
                while (cursor.moveToNext()) {
                    if (verseIndex >= 0) {
                        result.add(cursor.getInt(verseIndex))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("BIBLE_DEBUG", "Error consultando versículos con comentarios en $dbName: ${e.message}", e)
        } finally {
            database.close()
        }

        return result
    }

    fun getCommentaryText(dbName: String, bookNameOrAbbr: String, chapter: Int, verse: Int): String? {
        val database = openDatabaseReadOnly(context, dbName) ?: return null

        val bookNumber = resolveBookNumber(bookNameOrAbbr)
        val query = """
            SELECT text FROM commentaries 
            WHERE book_number = ? 
              AND chapter_number_from = ? 
              AND verse_number_from <= ? 
              AND (verse_number_to >= ? OR verse_number_to IS NULL OR verse_number_to = 0)
            LIMIT 1
        """.trimIndent()
        val selectionArgs = arrayOf(
            bookNumber.toString(),
            chapter.toString(),
            verse.toString(),
            verse.toString()
        )

        var commentText: String? = null

        try {
            database.rawQuery(query, selectionArgs).use { cursor ->
                val textIndex = cursor.getColumnIndex("text")
                if (cursor.moveToFirst() && textIndex >= 0) {
                    commentText = cursor.getString(textIndex)
                }
            }
        } catch (e: Exception) {
            Log.e("BIBLE_DEBUG", "Error recuperando nota de comentario: ${e.message}", e)
        } finally {
            database.close()
        }

        return commentText
    }

    fun getVersionNoteText(
        dbName: String,
        bookNameOrAbbr: String,
        chapter: Int,
        verse: Int,
        marker: String
    ): String? {
        val database = openDatabaseReadOnly(context, dbName) ?: return null
        val bookNumber = resolveBookNumber(bookNameOrAbbr)
        val cleanMarker = marker.trim().removeSurrounding("[", "]")

        var noteText: String? = null
        try {
            val queryMarker = """
                SELECT text FROM commentaries 
                WHERE book_number = ? 
                  AND chapter_number_from = ? 
                  AND verse_number_from = ? 
                  AND (marker = ? OR marker = ? OR marker LIKE ?)
                LIMIT 1
            """.trimIndent()

            database.rawQuery(
                queryMarker,
                arrayOf(
                    bookNumber.toString(),
                    chapter.toString(),
                    verse.toString(),
                    cleanMarker,
                    "[$cleanMarker]",
                    "%$cleanMarker%"
                )
            ).use { cursor ->
                if (cursor.moveToFirst()) {
                    val textIndex = cursor.getColumnIndex("text")
                    if (textIndex >= 0) {
                        noteText = cursor.getString(textIndex)
                    }
                }
            }

            if (noteText.isNullOrBlank()) {
                val queryFallback = """
                    SELECT text FROM commentaries 
                    WHERE book_number = ? 
                      AND chapter_number_from = ? 
                      AND verse_number_from = ? 
                    LIMIT 1
                """.trimIndent()

                database.rawQuery(
                    queryFallback,
                    arrayOf(
                        bookNumber.toString(),
                        chapter.toString(),
                        verse.toString()
                    )
                ).use { cursor ->
                    if (cursor.moveToFirst()) {
                        val textIndex = cursor.getColumnIndex("text")
                        if (textIndex >= 0) {
                            noteText = cursor.getString(textIndex)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("BIBLE_DEBUG", "Error consultando nota de versión en $dbName: ${e.message}", e)
        } finally {
            database.close()
        }

        return noteText
    }

    fun getDictionaryEntry(
        term: String,
        strongCode: String? = null,
        dbName: String = "Multi.dictionary.SQLite3"
    ): DictionaryEntry? {
        val database = openDatabaseReadOnly(context, dbName) ?: return null
        val cleanTerm = term.trim()
        if (cleanTerm.isBlank() && strongCode.isNullOrBlank()) {
            database.close()
            return null
        }

        var entry: DictionaryEntry? = null

        try {
            if (!strongCode.isNullOrBlank()) {
                val pureNumber = strongCode.removePrefix("G").removePrefix("H").removePrefix("g").removePrefix("h")
                val strongQuery = """
                    SELECT * FROM dictionary 
                    WHERE topic = ? OR topic = ? OR topic = ?
                    LIMIT 1
                """.trimIndent()
                database.rawQuery(strongQuery, arrayOf(strongCode.uppercase(), strongCode.lowercase(), pureNumber)).use { cursor ->
                    if (cursor.moveToFirst()) {
                        entry = parseDictionaryEntry(cursor, cleanTerm.ifBlank { strongCode })
                    }
                }
            }

            var resolvedTopic = cleanTerm
            if (entry == null) {
                val hasWordsTable = database.rawQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name='words'", null
                ).use { it.moveToFirst() }

                if (hasWordsTable) {
                    try {
                        val wordQuery = "SELECT topic FROM words WHERE LOWER(word) = LOWER(?) OR LOWER(variation) = LOWER(?) LIMIT 1"
                        database.rawQuery(wordQuery, arrayOf(cleanTerm, cleanTerm)).use { cursor ->
                            if (cursor.moveToFirst()) {
                                val topicCol = cursor.getColumnIndex("topic")
                                if (topicCol >= 0) {
                                    resolvedTopic = cursor.getString(topicCol)
                                }
                            }
                        }
                    } catch (_: Exception) {}
                }

                val dictQuery = """
                    SELECT * FROM dictionary 
                    WHERE LOWER(topic) = LOWER(?) 
                       OR LOWER(topic) = LOWER(?)
                       OR topic LIKE ?
                    ORDER BY CASE 
                        WHEN LOWER(topic) = LOWER(?) THEN 1 
                        WHEN topic LIKE ? THEN 2 
                        ELSE 3 
                    END
                    LIMIT 1
                """.trimIndent()

                val likeArg = "$cleanTerm%"
                database.rawQuery(dictQuery, arrayOf(resolvedTopic, cleanTerm, likeArg, resolvedTopic, likeArg)).use { cursor ->
                    if (cursor.moveToFirst()) {
                        entry = parseDictionaryEntry(cursor, cleanTerm)
                    }
                }
            }

            if (entry == null && cleanTerm.length >= 3) {
                val fallbackQuery = """
                    SELECT * FROM dictionary 
                    WHERE topic LIKE ?
                    LIMIT 1
                """.trimIndent()
                database.rawQuery(fallbackQuery, arrayOf("%$cleanTerm%")).use { cursor ->
                    if (cursor.moveToFirst()) {
                        entry = parseDictionaryEntry(cursor, cleanTerm)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("BIBLE_DEBUG", "Error consultando entrada de diccionario en $dbName para '$cleanTerm': ${e.message}", e)
        } finally {
            database.close()
        }

        return entry
    }

    fun searchDictionary(query: String, dbName: String = "Multi.dictionary.SQLite3", limit: Int = 20): List<DictionaryEntry> {
        val database = openDatabaseReadOnly(context, dbName) ?: return emptyList()
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) {
            database.close()
            return emptyList()
        }

        val results = mutableListOf<DictionaryEntry>()
        try {
            val sql = "SELECT * FROM dictionary WHERE topic LIKE ? OR definition LIKE ? LIMIT ?"
            val likeArg = "%$cleanQuery%"
            database.rawQuery(sql, arrayOf(likeArg, likeArg, limit.toString())).use { cursor ->
                while (cursor.moveToNext()) {
                    results.add(parseDictionaryEntry(cursor, cleanQuery))
                }
            }
        } catch (e: Exception) {
            Log.e("BIBLE_DEBUG", "Error buscando en diccionario $dbName: ${e.message}", e)
        } finally {
            database.close()
        }

        return results
    }

    private fun formatMultiDictionaryDefinition(rawDef: String): String {
        if (rawDef.isBlank()) return ""

        var text = rawDef

        text = text.replace(Regex("(?i)<hr\\s*/?>"), "\n\n")
        text = text.replace(Regex("(?i)</?(p|div|section|blockquote)[^>]*>"), "\n\n")
        text = text.replace(Regex("(?i)<br\\s*/?>"), "\n")

        val authorPattern = Regex(
            "(?i)(?:[-_=]{2,}\\s*)?(?:<[^>]+>\\s*)*(?:\\[|\\()? *(?:Diccionario|Léxico)?\\s*(Strong|Chávez|Chavez|Tuggy|Taggart|Vine|Swanson)\\b[^\n:<]*?(?:\\]|\\)|:|<|>)*"
        )

        text = authorPattern.replace(text) { matchResult ->
            val author = matchResult.groupValues[1]
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                .replace("Chavez", "Chávez")
                .replace("Taggart", "Tuggy")
            "\n\n[$author]\n"
        }

        text = text.replace(Regex("<.*?>"), "")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace(Regex("[ \\t]+"), " ")

        return text.lines()
            .map { it.trim() }
            .joinToString("\n")
            .replace(Regex("\\n{3,}"), "\n\n")
            .trim()
    }

    private fun parseDictionaryEntry(cursor: Cursor, fallbackTerm: String): DictionaryEntry {
        val topicIdx = cursor.getColumnIndex("topic")
        val defIdx = cursor.getColumnIndex("definition")
        val lexemeIdx = cursor.getColumnIndex("lexeme")
        val translitIdx = cursor.getColumnIndex("transliteration")
        val pronunIdx = cursor.getColumnIndex("pronunciation")
        val shortDefIdx = cursor.getColumnIndex("short_definition")

        val rawTopic = if (topicIdx >= 0) cursor.getString(topicIdx) ?: "" else ""
        val rawDef = if (defIdx >= 0) cursor.getString(defIdx) ?: "" else ""
        val rawLexeme = if (lexemeIdx >= 0) cursor.getString(lexemeIdx) ?: "" else ""
        val rawTranslit = if (translitIdx >= 0) cursor.getString(translitIdx) ?: "" else ""
        val rawPronun = if (pronunIdx >= 0) cursor.getString(pronunIdx) ?: "" else ""
        val rawShortDef = if (shortDefIdx >= 0) cursor.getString(shortDefIdx) ?: "" else ""

        val cleanDef = formatMultiDictionaryDefinition(rawDef)

        val isStrong = rawTopic.matches(Regex("(?i)^[GH]?\\d+$"))
        val strongCode = if (isStrong) {
            if (!rawTopic.startsWith("G", true) && !rawTopic.startsWith("H", true)) {
                "S$rawTopic"
            } else {
                rawTopic.uppercase()
            }
        } else {
            val strongMatch = Regex("(?i)\\b([GH]\\d{1,5})\\b").find(rawDef)
            strongMatch?.value?.uppercase() ?: "LEX"
        }

        val display = if (isStrong && rawTranslit.isNotBlank()) {
            rawTranslit.replaceFirstChar { it.uppercase() }
        } else if (rawTopic.isNotBlank()) {
            rawTopic.replaceFirstChar { it.uppercase() }
        } else {
            fallbackTerm.replaceFirstChar { it.uppercase() }
        }

        val pronunciation = when {
            rawPronun.isNotBlank() -> rawPronun
            rawTranslit.isNotBlank() -> rawTranslit
            else -> ""
        }

        return DictionaryEntry(
            term = rawTopic.ifBlank { fallbackTerm },
            displayTerm = display,
            definition = cleanDef,
            originalWord = rawLexeme,
            strongCode = strongCode,
            pronunciation = pronunciation,
            theologicalNotes = rawShortDef.replace(Regex("<.*?>"), "").trim()
        )
    }

    // =========================================================================
    // REFERENCIAS CRUZADAS (TSK-x.crossreferences.SQLite3)
    // =========================================================================

    private data class RawCrossRef(
        val bookTo: Int,
        val chapterTo: Int,
        val verseToStart: Int,
        val verseToEnd: Int,
        val votes: Int
    )

    fun getVersesWithCrossReferences(
        bookNameOrAbbr: String,
        chapter: Int,
        dbName: String = "TSK-x.crossreferences.SQLite3"
    ): Set<Int> {
        val result = mutableSetOf<Int>()
        val database = openDatabaseReadOnly(context, dbName) ?: return emptySet()
        val bookNumber = resolveBookNumber(bookNameOrAbbr)
        val query = "SELECT DISTINCT verse FROM cross_references WHERE book = ? AND chapter = ?"

        try {
            database.rawQuery(query, arrayOf(bookNumber.toString(), chapter.toString())).use { cursor ->
                val verseIdx = cursor.getColumnIndex("verse")
                while (cursor.moveToNext()) {
                    if (verseIdx >= 0) {
                        result.add(cursor.getInt(verseIdx))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("BIBLE_DEBUG", "Error consultando versículos con referencias cruzadas en $dbName: ${e.message}", e)
        } finally {
            database.close()
        }

        return result
    }

    fun getCrossReferences(
        bookNameOrAbbr: String,
        chapter: Int,
        verse: Int,
        bibleDbName: String = "RVR1960.SQLite3",
        limit: Int = 30
    ): List<CrossReference> {
        val result = mutableListOf<CrossReference>()
        val tskDbName = "TSK-x.crossreferences.SQLite3"
        val database = openDatabaseReadOnly(context, tskDbName) ?: return emptyList()
        val bookNumber = resolveBookNumber(bookNameOrAbbr)

        val query = """
            SELECT book_to, chapter_to, verse_to_start, verse_to_end, votes 
            FROM cross_references 
            WHERE book = ? AND chapter = ? AND verse = ?
            ORDER BY votes DESC, book_to ASC, chapter_to ASC, verse_to_start ASC
            LIMIT ?
        """.trimIndent()

        val rawList = mutableListOf<RawCrossRef>()
        try {
            database.rawQuery(
                query,
                arrayOf(bookNumber.toString(), chapter.toString(), verse.toString(), limit.toString())
            ).use { cursor ->
                val bToIdx = cursor.getColumnIndex("book_to")
                val cToIdx = cursor.getColumnIndex("chapter_to")
                val vSIdx = cursor.getColumnIndex("verse_to_start")
                val vEIdx = cursor.getColumnIndex("verse_to_end")
                val vtIdx = cursor.getColumnIndex("votes")

                while (cursor.moveToNext()) {
                    val bTo = if (bToIdx >= 0) cursor.getInt(bToIdx) else 0
                    val cTo = if (cToIdx >= 0) cursor.getInt(cToIdx) else 0
                    val vS = if (vSIdx >= 0) cursor.getInt(vSIdx) else 0
                    val vE = if (vEIdx >= 0) cursor.getInt(vEIdx) else 0
                    val votes = if (vtIdx >= 0) cursor.getInt(vtIdx) else 0
                    rawList.add(RawCrossRef(bTo, cTo, vS, vE, votes))
                }
            }
        } catch (e: Exception) {
            Log.e("BIBLE_DEBUG", "Error consultando referencias en $tskDbName: ${e.message}", e)
        } finally {
            database.close()
        }

        if (rawList.isEmpty()) return emptyList()

        val bibleDb = openDatabaseReadOnly(context, bibleDbName)
        try {
            var counter = 1L
            for (raw in rawList) {
                val targetBookName = resolveBookName(raw.bookTo)
                if (targetBookName.isBlank()) continue

                var fetchedText = ""
                if (bibleDb != null) {
                    val sql = if (raw.verseToEnd > raw.verseToStart) {
                        "SELECT text FROM verses WHERE book_number = ? AND chapter = ? AND verse >= ? AND verse <= ? ORDER BY verse ASC"
                    } else {
                        "SELECT text FROM verses WHERE book_number = ? AND chapter = ? AND verse = ? LIMIT 1"
                    }
                    val params = if (raw.verseToEnd > raw.verseToStart) {
                        arrayOf(raw.bookTo.toString(), raw.chapterTo.toString(), raw.verseToStart.toString(), raw.verseToEnd.toString())
                    } else {
                        arrayOf(raw.bookTo.toString(), raw.chapterTo.toString(), raw.verseToStart.toString())
                    }

                    try {
                        bibleDb.rawQuery(sql, params).use { c ->
                            val tIdx = c.getColumnIndex("text")
                            val textList = mutableListOf<String>()
                            while (c.moveToNext()) {
                                if (tIdx >= 0) {
                                    val t = c.getString(tIdx)
                                        .replace(Regex("<S>.*?</S>|<.*?>"), "")
                                        .replace(Regex("(?<=\\p{L})\\d+"), "")
                                        .replace(Regex("\\s+"), " ")
                                        .trim()
                                    textList.add(t)
                                }
                            }
                            fetchedText = textList.joinToString(" ")
                        }
                    } catch (_: Exception) {}
                }

                if (fetchedText.isBlank()) {
                    fetchedText = "Pasaje en $targetBookName ${raw.chapterTo}:${raw.verseToStart}."
                }

                val relation = if (raw.verseToEnd > raw.verseToStart) {
                    "Versículos ${raw.verseToStart} al ${raw.verseToEnd} (Relevancia: ${raw.votes})"
                } else {
                    "Relevancia: ${raw.votes}"
                }

                result.add(
                    CrossReference(
                        id = counter++,
                        sourceBook = resolveBookName(bookNumber),
                        sourceChapter = chapter,
                        sourceVerse = verse,
                        targetBook = targetBookName,
                        targetChapter = raw.chapterTo,
                        targetVerse = raw.verseToStart,
                        targetText = fetchedText,
                        relationNote = relation
                    )
                )
            }
        } finally {
            bibleDb?.close()
        }

        return result
    }
}
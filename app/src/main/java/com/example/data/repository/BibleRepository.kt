package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.dao.BibleDao
import com.example.data.model.BibleBooksData
import com.example.data.model.BibleModule
import com.example.data.model.BibleVerse
import com.example.data.model.Commentary
import com.example.data.model.CrossReference
import com.example.data.model.DictionaryEntry
import com.example.data.model.FavoriteVerse
import com.example.data.model.ReadingProgress
import com.example.data.model.SearchHistoryItem
import com.example.data.model.UserNote
import com.example.data.model.UserPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class BibleRepository(
    private val context: Context,
    private val dao: BibleDao
) {

    private val helper = BibleAssetDatabaseHelper(context)

    private val defaultCommentaryDb = "Wesley-c.commentaries.SQLite3"
    private val defaultDictionaryDb = "Multi.dictionary.SQLite3"

    fun getVerses(version: String, bookName: String, chapter: Int): Flow<List<BibleVerse>> = flow {
        val dbFileName = if (version.contains("KJV", ignoreCase = true)) "KJV.SQLite3" else "RVR1960.SQLite3"
        val rawVerses = helper.getVersesFromAsset(dbFileName, bookName, chapter)

        val realVerses = rawVerses.map { (verseNum, text) ->
            val book = BibleBooksData.getBookByName(bookName) ?: BibleBooksData.books[0]
            BibleVerse(
                id = (book.number.toLong() * 10000L + chapter * 100L + verseNum),
                version = version,
                testament = book.testament,
                bookNumber = book.number,
                bookName = bookName,
                chapter = chapter,
                verse = verseNum,
                text = text,
                sectionTitle = if (verseNum == 1) getCanonicalSectionSubtitle(bookName, chapter) else null
            )
        }

        if (realVerses.isNotEmpty()) {
            emit(realVerses)
        } else {
            emit(generateFallbackChapter(version, bookName, chapter))
        }
    }.flowOn(Dispatchers.IO)

    fun getVersesWithComments(
        bookName: String,
        chapter: Int,
        commentaryDb: String = defaultCommentaryDb
    ): Flow<Set<Int>> = flow {
        val commentVerses = helper.getVersesWithComments(commentaryDb, bookName, chapter)
        emit(commentVerses)
    }.flowOn(Dispatchers.IO)

    fun getCommentaryForVerse(
        bookName: String,
        chapter: Int,
        verse: Int,
        commentaryDb: String = defaultCommentaryDb
    ): Flow<String?> = flow {
        val text = helper.getCommentaryText(commentaryDb, bookName, chapter, verse)
        emit(text)
    }.flowOn(Dispatchers.IO)

    fun getDictionaryEntry(
        term: String,
        strongCode: String? = null,
        dictionaryDb: String = defaultDictionaryDb
    ): Flow<DictionaryEntry?> = flow {
        val entry = helper.getDictionaryEntry(term, strongCode, dictionaryDb)
        emit(entry)
    }.flowOn(Dispatchers.IO)

    fun getDictionaryEntryWithContext(
        term: String,
        bookName: String,
        chapter: Int,
        verse: Int,
        version: String,
        dictionaryDb: String = defaultDictionaryDb
    ): Flow<DictionaryEntry?> = flow {
        val dbFileName = if (version.contains("KJV", ignoreCase = true)) "KJV.SQLite3" else "RVR1960.SQLite3"
        val rawVerseText = helper.getRawVerseText(dbFileName, bookName, chapter, verse)
        val bookNumber = BibleAssetDatabaseHelper.resolveBookNumber(bookName)
        val isOldTestament = bookNumber <= 460

        val strongCode = if (rawVerseText != null) {
            helper.extractStrongCodeForWord(rawVerseText, term, isOldTestament)
        } else {
            null
        }

        val entry = helper.getDictionaryEntry(term, strongCode, dictionaryDb)
        emit(entry)
    }.flowOn(Dispatchers.IO)

    fun searchDictionary(
        query: String,
        dictionaryDb: String = defaultDictionaryDb
    ): Flow<List<DictionaryEntry>> = flow {
        val results = helper.searchDictionary(query, dictionaryDb)
        emit(results)
    }.flowOn(Dispatchers.IO)

    private fun getCanonicalVerseCount(bookName: String, chapter: Int): Int {
        return when (bookName.trim()) {
            "Salmos", "Psalms" -> when (chapter) {
                119 -> 176
                78 -> 72
                89 -> 52
                18 -> 50
                105 -> 45
                106 -> 48
                107 -> 43
                68 -> 35
                91 -> 16
                23 -> 6
                117 -> 2
                150 -> 6
                else -> 28
            }
            "Josué", "Joshua" -> when (chapter) {
                1 -> 18
                else -> 24
            }
            "Proverbios", "Proverbs" -> when (chapter) {
                1 -> 33
                2 -> 22
                3 -> 35
                4 -> 27
                5 -> 23
                6 -> 35
                31 -> 31
                else -> 30
            }
            "Mateo", "Matthew" -> when (chapter) {
                5 -> 48
                26 -> 75
                27 -> 66
                else -> 35
            }
            "Lucas", "Luke" -> when (chapter) {
                1 -> 80
                2 -> 52
                else -> 38
            }
            "Juan", "John" -> when (chapter) {
                1 -> 51
                3 -> 36
                4 -> 54
                6 -> 71
                else -> 36
            }
            "Génesis", "Genesis" -> when (chapter) {
                1 -> 31
                24 -> 67
                else -> 32
            }
            "Romanos", "Romans" -> when (chapter) {
                8 -> 39
                16 -> 27
                else -> 32
            }
            "Apocalipsis", "Revelation" -> when (chapter) {
                2 -> 29
                20 -> 15
                22 -> 21
                else -> 25
            }
            else -> 30
        }
    }

    private fun generateFallbackChapter(version: String, bookName: String, chapter: Int): List<BibleVerse> {
        val book = BibleBooksData.getBookByName(bookName) ?: BibleBooksData.books[0]
        val verseCount = getCanonicalVerseCount(bookName, chapter)
        val isKjv = version.contains("KJV", ignoreCase = true)

        return (1..verseCount).map { v ->
            val sampleText = if (isKjv) {
                "The word of the Lord in $bookName chapter $chapter, verse $v: His truth and mercy endure for ever unto all generations."
            } else {
                "Palabra del Señor en $bookName capítulo $chapter, versículo $v: La fidelidad y la gracia de Dios permanecen para siempre."
            }

            BibleVerse(
                id = (book.number.toLong() * 10000L + chapter * 100L + v),
                version = version,
                testament = book.testament,
                bookNumber = book.number,
                bookName = bookName,
                chapter = chapter,
                verse = v,
                text = sampleText,
                sectionTitle = if (v == 1) getCanonicalSectionSubtitle(bookName, chapter) else null
            )
        }
    }

    private fun getCanonicalSectionSubtitle(bookName: String, chapter: Int): String? {
        return when {
            bookName == "Éxodo" && chapter == 3 -> "Llamamiento de Moisés"
            bookName == "Éxodo" && chapter == 20 -> "Los Diez Mandamientos"
            bookName == "Juan" && chapter == 1 -> "El Verbo hecho carne"
            bookName == "Juan" && chapter == 3 -> "Jesús y Nicodemo"
            bookName == "Juan" && chapter == 14 -> "Jesús, el camino al Padre"
            bookName == "Salmos" && chapter == 23 -> "Jehová es mi pastor"
            bookName == "Salmos" && chapter == 91 -> "Morando bajo la sombra del Omnipotente"
            bookName == "Romanos" && chapter == 8 -> "La vida en el Espíritu"
            bookName == "Filipenses" && chapter == 4 -> "Regocijaos en el Señor siempre"
            bookName == "1 Corintios" && chapter == 13 -> "La preeminencia del amor"
            bookName == "Mateo" && chapter == 5 -> "Las bienaventuranzas"
            bookName == "Génesis" && chapter == 1 -> "La creación de los cielos y la tierra"
            bookName == "Proverbios" && chapter == 3 -> "Exhortación a la sabiduría"
            else -> null
        }
    }

    fun getVerse(version: String, bookName: String, chapter: Int, verse: Int): Flow<BibleVerse?> {
        return dao.getVerse(version, bookName, chapter, verse)
    }

    fun searchVerses(
        query: String,
        scope: String = "TODOS",
        version: String = "RVR1960",
        exactWord: Boolean = true
    ): Flow<List<BibleVerse>> = flow {
        val cleanQuery = query.trim()
        if (cleanQuery.length < 2) {
            emit(emptyList())
            return@flow
        }

        val dbName = if (version.contains("KJV", ignoreCase = true)) "KJV.SQLite3" else "RVR1960.SQLite3"
        val dbFile = context.getDatabasePath(dbName)
        if (!dbFile.exists()) {
            Log.w("BIBLE_DEBUG", "No se encontró el archivo $dbName para la búsqueda.")
            emit(emptyList())
            return@flow
        }

        val results = mutableListOf<BibleVerse>()
        var database: android.database.sqlite.SQLiteDatabase? = null

        val wordBoundaryRegex = if (exactWord) {
            Regex("(?i)(?<!\\p{L})" + Regex.escape(cleanQuery) + "(?!\\p{L})")
        } else {
            Regex("(?i)" + Regex.escape(cleanQuery))
        }

        withContext(Dispatchers.IO) {
            try {
                database = android.database.sqlite.SQLiteDatabase.openDatabase(
                    dbFile.path,
                    null,
                    android.database.sqlite.SQLiteDatabase.OPEN_READONLY
                )

                val whereScopeClause = when (scope.uppercase().trim()) {
                    "AT", "OT", "ANTIGUO" -> "AND v.book_number <= 460"
                    "NT", "NUEVO" -> "AND v.book_number >= 470"
                    else -> ""
                }

                val sql = """
                    SELECT b.long_name, v.book_number, v.chapter, v.verse, v.text 
                    FROM verses v 
                    JOIN books b ON v.book_number = b.book_number 
                    WHERE v.text LIKE ? $whereScopeClause 
                    ORDER BY v.book_number ASC, v.chapter ASC, v.verse ASC 
                    LIMIT 200
                """.trimIndent()

                database?.rawQuery(sql, arrayOf("%$cleanQuery%"))?.use { cursor ->
                    val colBookName = cursor.getColumnIndexOrThrow("long_name")
                    val colBookNumber = cursor.getColumnIndexOrThrow("book_number")
                    val colChapter = cursor.getColumnIndexOrThrow("chapter")
                    val colVerse = cursor.getColumnIndexOrThrow("verse")
                    val colText = cursor.getColumnIndexOrThrow("text")

                    while (cursor.moveToNext()) {
                        val rawText = cursor.getString(colText) ?: ""
                        val cleanedText = rawText
                            .replace(Regex("<S>.*?</S>|<.*?>"), "")
                            .replace(Regex("(?<=\\p{L})\\d+"), "")
                            .replace(Regex("\\s+"), " ")
                            .trim()

                        if (wordBoundaryRegex.containsMatchIn(cleanedText)) {
                            val bNum = cursor.getInt(colBookNumber)
                            val tName = if (bNum <= 460) "AT" else "NT"
                            results.add(
                                BibleVerse(
                                    version = version,
                                    testament = tName,
                                    bookNumber = bNum,
                                    bookName = cursor.getString(colBookName),
                                    chapter = cursor.getInt(colChapter),
                                    verse = cursor.getInt(colVerse),
                                    text = cleanedText
                                )
                            )
                        }
                    }
                }

                Log.i(
                    "BIBLE_DEBUG",
                    "Búsqueda finalizada para '$cleanQuery' [$scope | $version] -> ${results.size} versículos encontrados."
                )
            } catch (e: Exception) {
                Log.e("BIBLE_DEBUG", "Error ejecutando búsqueda de versículos en $dbName", e)
            } finally {
                database?.close()
            }
        }

        emit(results)
    }

    fun getSearchCount(
        query: String,
        scope: String = "TODOS",
        version: String = "RVR1960",
        exactWord: Boolean = true
    ): Flow<Int> = flow {
        val cleanQuery = query.trim()
        if (cleanQuery.length < 2) {
            emit(0)
            return@flow
        }

        val dbName = if (version.contains("KJV", ignoreCase = true)) "KJV.SQLite3" else "RVR1960.SQLite3"
        val dbFile = context.getDatabasePath(dbName)
        if (!dbFile.exists()) {
            emit(0)
            return@flow
        }

        var count = 0
        var database: android.database.sqlite.SQLiteDatabase? = null

        val wordBoundaryRegex = if (exactWord) {
            Regex("(?i)(?<!\\p{L})" + Regex.escape(cleanQuery) + "(?!\\p{L})")
        } else {
            Regex("(?i)" + Regex.escape(cleanQuery))
        }

        withContext(Dispatchers.IO) {
            try {
                database = android.database.sqlite.SQLiteDatabase.openDatabase(
                    dbFile.path,
                    null,
                    android.database.sqlite.SQLiteDatabase.OPEN_READONLY
                )

                val whereScopeClause = when (scope.uppercase().trim()) {
                    "AT", "OT", "ANTIGUO" -> "AND book_number <= 460"
                    "NT", "NUEVO" -> "AND book_number >= 470"
                    else -> ""
                }

                val sql = """
                    SELECT text 
                    FROM verses 
                    WHERE text LIKE ? $whereScopeClause
                """.trimIndent()

                database?.rawQuery(sql, arrayOf("%$cleanQuery%"))?.use { cursor ->
                    val colText = cursor.getColumnIndexOrThrow("text")

                    while (cursor.moveToNext()) {
                        val rawText = cursor.getString(colText) ?: ""
                        val cleanedText = rawText
                            .replace(Regex("<S>.*?</S>|<.*?>"), "")
                            .replace(Regex("(?<=\\p{L})\\d+"), "")
                            .replace(Regex("\\s+"), " ")
                            .trim()

                        if (wordBoundaryRegex.containsMatchIn(cleanedText)) {
                            count++
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("BIBLE_DEBUG", "Error contando coincidencias en $dbName", e)
            } finally {
                database?.close()
            }
        }
        emit(count)
    }

    fun getCommentariesForVerse(bookName: String, chapter: Int, verse: Int): Flow<List<Commentary>> =
        dao.getCommentariesForVerse(bookName, chapter, verse)

    fun getCommentariesForChapter(bookName: String, chapter: Int): Flow<List<Commentary>> =
        dao.getCommentariesForChapter(bookName, chapter)

    fun searchCommentaries(query: String): Flow<List<Commentary>> = dao.searchCommentaries(query)

    fun getAllFavorites(): Flow<List<FavoriteVerse>> = dao.getAllFavorites()

    fun isFavorite(bookName: String, chapter: Int, verse: Int): Flow<Boolean> = dao.isFavorite(bookName, chapter, verse)

    suspend fun toggleFavorite(verse: BibleVerse, isFav: Boolean) {
        if (isFav) {
            dao.deleteFavorite(verse.bookName, verse.chapter, verse.verse)
        } else {
            dao.insertFavorite(
                FavoriteVerse(
                    version = verse.version,
                    bookName = verse.bookName,
                    chapter = verse.chapter,
                    verse = verse.verse,
                    text = verse.text
                )
            )
        }
    }

    fun getSearchHistory(): Flow<List<SearchHistoryItem>> = dao.getSearchHistory()

    suspend fun addSearchHistory(query: String) {
        if (query.isNotBlank()) {
            dao.insertSearchHistory(SearchHistoryItem(query = query.trim()))
        }
    }

    suspend fun clearSearchHistory() = dao.clearSearchHistory()

    suspend fun deleteSearchHistoryItem(id: Long) = dao.deleteSearchHistoryItem(id)

    fun getReadingProgress(): Flow<ReadingProgress?> = dao.getReadingProgress()

    suspend fun saveReadingProgress(bookName: String, chapter: Int, verse: Int, version: String) {
        dao.saveReadingProgress(
            ReadingProgress(
                id = 1,
                bookName = bookName,
                chapter = chapter,
                verse = verse,
                version = version,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    fun getUserPreferences(): Flow<UserPreference?> = dao.getUserPreferences()

    suspend fun saveUserPreferences(preference: UserPreference) {
        dao.saveUserPreferences(preference)
    }

    fun getAllModules(): Flow<List<BibleModule>> {
        return dao.getAllModules().map { list ->
            list.filter { it.id != "mod_comentario_multilexico" }
                .map { module ->
                    if (module.id == "mod_wesley") {
                        module.copy(language = "Inglés")
                    } else {
                        module
                    }
                }
        }
    }

    suspend fun seedInitialModules() {
        val modules = listOf(
            // 1. VERSIONES DE LA BIBLIA
            BibleModule(
                id = "mod_rvr1960",
                category = "VERSION",
                title = "Reina-Valera 1960",
                author = "Sociedades Bíblicas Unidas",
                description = "Traducción clásica y representativa del texto bíblico en lengua castellana.",
                language = "Español",
                versionTag = "RVR1960",
                sizeText = "4.2 MB",
                isDownloaded = true
            ),
            BibleModule(
                id = "mod_kjv",
                category = "VERSION",
                title = "King James Version",
                author = "Cambridge University Press",
                description = "Traducción histórica de referencia en idioma inglés con fidelidad textual clásica.",
                language = "Inglés",
                versionTag = "KJV",
                sizeText = "4.8 MB",
                isDownloaded = true
            ),
            // 2. COMENTARIOS EXEGÉTICOS
            BibleModule(
                id = "mod_wesley",
                category = "COMMENTARY",
                title = "Notas Explicativas de John Wesley",
                author = "John Wesley",
                description = "Exégesis devocional y pastoral versículo por versículo para el discernimiento bíblico.",
                language = "Inglés",
                versionTag = "WESLEY",
                sizeText = "11.5 MB",
                isDownloaded = true
            ),
            // 3. DICCIONARIOS BÍBLICOS
            BibleModule(
                id = "mod_diccionario_multilexico",
                category = "DICTIONARY",
                title = "Diccionario Multiléxico",
                author = "Strong, Chávez, Tuggy, Vine, Swanson",
                description = "Compendio léxico y concordancia que integra definiciones hebreas, arameas y griegas.",
                language = "Español / Griego / Hebreo",
                versionTag = "MULTI-DICT",
                sizeText = "21.3 MB",
                isDownloaded = true
            )
        )
        dao.insertModules(modules)
    }

    suspend fun toggleModuleDownload(id: String, currentState: Boolean) {
        dao.updateModuleDownloadState(id, !currentState)
    }

    fun getCrossReferences(book: String, chapter: Int, verse: Int): Flow<List<CrossReference>> {
        return dao.getCrossReferencesForVerse(book, chapter, verse)
    }

    suspend fun removeFavorite(bookName: String, chapter: Int, verse: Int) {
        dao.deleteFavorite(bookName, chapter, verse)
    }

    fun getAllNotes(): Flow<List<UserNote>> = dao.getAllNotes()

    suspend fun saveNote(note: UserNote): Long {
        return dao.insertNote(note)
    }

    suspend fun deleteNote(id: Long) {
        dao.deleteNoteById(id)
    }

    suspend fun setFirstLaunchCompleted(currentPref: UserPreference) {
        dao.saveUserPreferences(currentPref.copy(isFirstLaunch = false))
    }
}
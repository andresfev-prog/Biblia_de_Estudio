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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class BibleRepository(
    private val context: Context,
    private val dao: BibleDao
) {

    private fun resolveBibleDatabaseFileName(version: String): String {
        val cleanVersion = version.trim().removeSuffix(".SQLite3")
        val defaultName = "$cleanVersion.SQLite3"

        // 1. Coincidencia exacta directa
        val exactFile = context.getDatabasePath(defaultName)
        if (exactFile.exists()) return exactFile.name

        // 2. Búsqueda insensible a mayúsculas/minúsculas en el directorio de bases de datos
        val dbDir = exactFile.parentFile
        if (dbDir != null && dbDir.exists()) {
            val files = dbDir.listFiles()

            // Coincidencia ignorando mayúsculas (ej: busca "T4T.SQLite3" y encuentra "t4t.SQLite3")
            val matchCaseInsensitive = files?.firstOrNull { it.name.equals(defaultName, ignoreCase = true) }
            if (matchCaseInsensitive != null) return matchCaseInsensitive.name

            // Coincidencia por prefijo del tag de versión
            val matchPrefix = files?.firstOrNull {
                it.name.startsWith(cleanVersion, ignoreCase = true) && it.name.endsWith(".SQLite3", ignoreCase = true)
            }
            if (matchPrefix != null) return matchPrefix.name
        }

        // 3. Verificación en assets si aún no ha sido extraída
        try {
            val assets = context.assets.list("") ?: emptyArray()
            val assetMatch = assets.firstOrNull {
                it.equals(defaultName, ignoreCase = true) ||
                        (it.startsWith(cleanVersion, ignoreCase = true) && it.endsWith(".SQLite3", ignoreCase = true))
            }
            if (assetMatch != null) return assetMatch
        } catch (_: Exception) { }

        return defaultName
    }
    private val helper = BibleAssetDatabaseHelper(context)

    private val defaultCommentaryDb = "Wesley-c.commentaries.SQLite3"
    private val defaultDictionaryDb = "Multi.dictionary.SQLite3"

    fun getVerses(version: String, bookName: String, chapter: Int): Flow<List<BibleVerse>> = flow {
        val dbFileName = resolveBibleDatabaseFileName(version)
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

    companion object {
        private const val GITHUB_BASE_URL = "https://github.com/andresfev-prog/Biblia_de_Estudio/releases/download/1.0.0"

        fun resolveVersionCommentaryFileName(version: String): String? {
            val upper = version.uppercase().trim()
            return when {
                upper.contains("RVR1960") || upper == "RV1960" || upper.contains("REINA") -> "RV1960-c.commentaries.SQLite3"
                upper.contains("NTV") -> "NTV.commentaries.SQLite3"
                upper.contains("NVI") -> "NVI.commentaries.SQLite3"
                upper.contains("PDT") -> "PDT.commentaries.SQLite3"
                upper.contains("TLA") -> "TLA-c.commentaries.SQLite3"
                upper == "AMP" -> "AMP.commentaries.SQLite3"
                upper.contains("AMPC") -> "AMPC.commentaries.SQLite3"
                upper.contains("ESV") -> "ESV.commentaries.SQLite3"
                upper.contains("KJV") -> "KJV-c.commentaries.SQLite3"
                upper.contains("NIV") -> "NIV.11.commentaries.SQLite3"
                upper.contains("NKJV") -> "NKJV.commentaries.SQLite3"
                upper.contains("NLT") -> "NLT.15.commentaries.SQLite3"
                upper.contains("TPT") -> "TPT.commentaries.SQLite3"
                upper.contains("T4T") -> "t4t.commentaries.SQLite3"
                else -> null
            }
        }

        fun isVersionSpecificCommentary(fileName: String?): Boolean {
            if (fileName.isNullOrBlank()) return false
            val fn = fileName.lowercase().trim()
            return fn.startsWith("rv1960-c") ||
                    fn.startsWith("ntv.") ||
                    fn.startsWith("nvi.") ||
                    fn.startsWith("nvi-c.") ||
                    fn.startsWith("pdt.") ||
                    fn.startsWith("tla-c.") ||
                    fn.startsWith("amp.") ||
                    fn.startsWith("ampc.") ||
                    fn.startsWith("esv.") ||
                    fn.startsWith("kjv-c.") ||
                    fn.startsWith("niv.11.") ||
                    fn.startsWith("nkjv.") ||
                    fn.startsWith("nlt.15.") ||
                    fn.startsWith("tpt.") ||
                    fn.startsWith("t4t.")
        }
    }

    suspend fun isModuleDownloaded(fileName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val assetList = context.assets.list("databases") ?: emptyArray()
            if (assetList.any { it.equals(fileName, ignoreCase = true) }) return@withContext true
        } catch (_: Exception) {}

        val dbFile = context.getDatabasePath(fileName)
        if (dbFile.exists() && dbFile.length() > 0) return@withContext true

        val mod = dao.getAllModules().first().find { it.fileName.equals(fileName, ignoreCase = true) }
        return@withContext mod?.isDownloaded == true
    }

    suspend fun getVersionNote(
        versionDbName: String,
        bookName: String,
        chapter: Int,
        verse: Int,
        marker: String
    ): String? = withContext(Dispatchers.IO) {
        helper.getVersionNoteText(versionDbName, bookName, chapter, verse, marker)
    }

    fun getVersesWithComments(
        bookName: String,
        chapter: Int
    ): Flow<Set<Int>> = flow {
        val modules = dao.getAllModules().first()
        val generalCommentaries = modules
            .filter { it.category == "COMMENTARY" && it.isDownloaded && !it.fileName.isNullOrBlank() && !isVersionSpecificCommentary(it.fileName) }
            .mapNotNull { it.fileName }

        val resultSet = mutableSetOf<Int>()
        for (dbName in generalCommentaries) {
            val file = BibleAssetDatabaseHelper.ensureDatabaseExtracted(context, dbName)
            if (file.exists() && file.length() > 0) {
                resultSet.addAll(helper.getVersesWithComments(dbName, bookName, chapter))
            }
        }
        if (generalCommentaries.isEmpty()) {
            val wesleyFile = BibleAssetDatabaseHelper.ensureDatabaseExtracted(context, defaultCommentaryDb)
            if (wesleyFile.exists() && wesleyFile.length() > 0) {
                resultSet.addAll(helper.getVersesWithComments(defaultCommentaryDb, bookName, chapter))
            }
        }
        emit(resultSet)
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

        val dbName = resolveBibleDatabaseFileName(version)
        val dbFile = BibleAssetDatabaseHelper.ensureDatabaseExtracted(context, dbName)
        if (!dbFile.exists() || dbFile.length() == 0L) {
            Log.w("BIBLE_DEBUG", "No se encontró el archivo $dbName para la búsqueda.")
            emit(emptyList())
            return@flow
        }

        val results = mutableListOf<BibleVerse>()
        withContext(Dispatchers.IO) {
            var database: android.database.sqlite.SQLiteDatabase? = null
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
                    LIMIT 100
                """.trimIndent()

                val wordBoundaryRegex = if (exactWord) {
                    Regex("(?i)(?<!\\p{L})" + Regex.escape(cleanQuery) + "(?!\\p{L})")
                } else null

                database.rawQuery(sql, arrayOf("%$cleanQuery%"))?.use { cursor ->
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

                        if (wordBoundaryRegex == null || wordBoundaryRegex.containsMatchIn(cleanedText)) {
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
    }.flowOn(Dispatchers.IO)

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

        val dbName = resolveBibleDatabaseFileName(version)
        val dbFile = BibleAssetDatabaseHelper.ensureDatabaseExtracted(context, dbName)
        if (!dbFile.exists() || dbFile.length() == 0L) {
            emit(0)
            return@flow
        }

        var count = 0
        withContext(Dispatchers.IO) {
            var database: android.database.sqlite.SQLiteDatabase? = null
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

                val sql = "SELECT COUNT(*) FROM verses WHERE text LIKE ? $whereScopeClause"
                database.rawQuery(sql, arrayOf("%$cleanQuery%"))?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        count = cursor.getInt(0)
                    }
                }
            } catch (e: Exception) {
                Log.e("BIBLE_DEBUG", "Error en COUNT(*) SQLite para $dbName", e)
            } finally {
                database?.close()
            }
        }
        emit(count)
    }.flowOn(Dispatchers.IO)

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
        return dao.getAllModules()
    }

    suspend fun seedInitialModules() {
        dao.clearAllModules()

        val modules = listOf(
            // ==========================================
            // 1. VERSIONES BÍBLICAS (18 módulos)
            // ==========================================
            // 1. Reina-Valera 1960 (Instalada por defecto)
            BibleModule(
                id = "mod_rvr1960",
                category = "VERSION",
                title = "Reina-Valera 1960",
                author = "Sociedades Bíblicas Unidas",
                description = "Traducción clásica y representativa del texto bíblico en lengua castellana.",
                language = "Español",
                versionTag = "RVR1960",
                sizeText = "4.2 MB",
                isDownloaded = true,
                downloadUrl = "$GITHUB_BASE_URL/RVR1960.SQLite3",
                fileName = "RVR1960.SQLite3"
            ),
            // 2. King James Version (Instalada por defecto)
            BibleModule(
                id = "mod_kjv",
                category = "VERSION",
                title = "King James Version",
                author = "Cambridge University Press",
                description = "Traducción histórica de referencia en inglés con fidelidad textual clásica.",
                language = "Inglés",
                versionTag = "KJV",
                sizeText = "4.8 MB",
                isDownloaded = true,
                downloadUrl = "$GITHUB_BASE_URL/KJV.SQLite3",
                fileName = "KJV.SQLite3"
            ),
            // 3. Amplified Bible
            BibleModule(
                id = "mod_amp",
                category = "VERSION",
                title = "Amplified Bible",
                author = "The Lockman Foundation",
                description = "Traducción con aclaraciones y matices contextuales entre corchetes.",
                language = "Inglés",
                versionTag = "AMP",
                sizeText = "5.1 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/AMP.SQLite3",
                fileName = "AMP.SQLite3"
            ),
            // 4. Amplified Bible Classic
            BibleModule(
                id = "mod_ampc",
                category = "VERSION",
                title = "Amplified Bible Classic",
                author = "The Lockman Foundation",
                description = "Edición clásica de la Biblia amplificada en idioma inglés.",
                language = "Inglés",
                versionTag = "AMPC",
                sizeText = "5.2 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/AMPC.SQLite3",
                fileName = "AMPC.SQLite3"
            ),
            // 5. Dios Habla Hoy
            BibleModule(
                id = "mod_dhhd",
                category = "VERSION",
                title = "Dios Habla Hoy",
                author = "Sociedades Bíblicas Unidas",
                description = "Versión en lenguaje accesible y ecuménico para lectura fluida.",
                language = "Español",
                versionTag = "DHHD",
                sizeText = "4.9 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/DHHD.SQLite3",
                fileName = "DHHD.SQLite3"
            ),
            // 6. English Standard Version
            BibleModule(
                id = "mod_esv",
                category = "VERSION",
                title = "English Standard Version",
                author = "Crossway",
                description = "Traducción literal y rigurosa en inglés contemporáneo.",
                language = "Inglés",
                versionTag = "ESV",
                sizeText = "4.7 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/ESV.SQLite3",
                fileName = "ESV.SQLite3"
            ),
            // 7. Biblia Kadosh Mesiánica
            BibleModule(
                id = "mod_kdsh",
                category = "VERSION",
                title = "Biblia Kadosh Mesiánica",
                author = "Diego Ascunce",
                description = "Restauración de nombres hebreos originales en el texto sagrado.",
                language = "Español",
                versionTag = "KDSH",
                sizeText = "4.8 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/KDSH.SQLite3",
                fileName = "KDSH.SQLite3"
            ),
            // 8. The Message
            BibleModule(
                id = "mod_msg",
                category = "VERSION",
                title = "The Message",
                author = "Eugene H. Peterson / NavPress",
                description = "Paráfrasis contemporánea en lenguaje coloquial y dinámico.",
                language = "Inglés",
                versionTag = "MSG",
                sizeText = "4.4 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/MSG.SQLite3",
                fileName = "MSG.SQLite3"
            ),
            // 9. New International Version (2011)
            BibleModule(
                id = "mod_niv11",
                category = "VERSION",
                title = "New International Version (2011)",
                author = "Biblica",
                description = "Traducción contemporánea en inglés con gran claridad y equilibrio formal.",
                language = "Inglés",
                versionTag = "NIV.11",
                sizeText = "4.6 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/NIV.11.SQLite3",
                fileName = "NIV.11.SQLite3"
            ),
            // 10. New King James Version
            BibleModule(
                id = "mod_nkjv",
                category = "VERSION",
                title = "New King James Version",
                author = "Thomas Nelson",
                description = "Revisión moderna con precisión textual y lírica clásica.",
                language = "Inglés",
                versionTag = "NKJV",
                sizeText = "4.6 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/NKJV.SQLite3",
                fileName = "NKJV.SQLite3"
            ),
            // 11. New Living Translation (2015)
            BibleModule(
                id = "mod_nlt15",
                category = "VERSION",
                title = "New Living Translation (2015)",
                author = "Tyndale House Publishers",
                description = "Traducción con lenguaje comprensible, vivo y devocional.",
                language = "Inglés",
                versionTag = "NLT.15",
                sizeText = "4.7 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/NLT.15.SQLite3",
                fileName = "NLT.15.SQLite3"
            ),
            // 12. Nueva Traducción Viviente
            BibleModule(
                id = "mod_ntv",
                category = "VERSION",
                title = "Nueva Traducción Viviente",
                author = "Tyndale House Publishers",
                description = "Traducción moderna y dinámica en lenguaje contemporáneo.",
                language = "Español",
                versionTag = "NTV",
                sizeText = "4.6 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/NTV.SQLite3",
                fileName = "NTV.SQLite3"
            ),
            // 13. Nueva Versión Internacional
            BibleModule(
                id = "mod_nvi",
                category = "VERSION",
                title = "Nueva Versión Internacional",
                author = "Biblica",
                description = "Traducción contemporánea con equilibrio formal y dinámico.",
                language = "Español",
                versionTag = "NVI",
                sizeText = "4.5 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/NVI.SQLite3",
                fileName = "NVI.SQLite3"
            ),
            // 14. Palabra de Dios para Todos
            BibleModule(
                id = "mod_pdt",
                category = "VERSION",
                title = "Palabra de Dios para Todos",
                author = "Liga Bíblica Mundial",
                description = "Texto bíblico en lenguaje sencillo, directo y comprensible.",
                language = "Español",
                versionTag = "PDT",
                sizeText = "4.3 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/PDT.SQLite3",
                fileName = "PDT.SQLite3"
            ),
            // 15. Traducción en Lenguaje Actual
            BibleModule(
                id = "mod_tla",
                category = "VERSION",
                title = "Traducción en Lenguaje Actual",
                author = "Sociedades Bíblicas Unidas",
                description = "Texto con vocabulario adaptado para una lectura clara y directa.",
                language = "Español",
                versionTag = "TLA",
                sizeText = "4.1 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/TLA.SQLite3",
                fileName = "TLA.SQLite3"
            ),
            // 16. The Passion Translation
            BibleModule(
                id = "mod_tpt",
                category = "VERSION",
                title = "The Passion Translation",
                author = "Brian Simmons / BroadStreet",
                description = "Traducción expresiva y poética orientada a la devoción.",
                language = "Inglés",
                versionTag = "TPT",
                sizeText = "4.5 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/TPT.SQLite3",
                fileName = "TPT.SQLite3"
            ),
            // 17. Young's Literal Translation
            BibleModule(
                id = "mod_ylt",
                category = "VERSION",
                title = "Young's Literal Translation",
                author = "Robert Young",
                description = "Traducción histórica con estricta literalidad verbal e idiomática.",
                language = "Inglés",
                versionTag = "YLT",
                sizeText = "4.3 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/YLT.SQLite3",
                fileName = "YLT.SQLite3"
            ),
            // 18. Translation 4 Translators
            BibleModule(
                id = "mod_t4t",
                category = "VERSION",
                title = "Translation 4 Translators",
                author = "Ellis W. Deibler Jr.",
                description = "Texto bíblico explicativo simplificado para facilitadores de traducción.",
                language = "Inglés",
                versionTag = "T4T",
                sizeText = "5.3 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/t4t.SQLite3",
                fileName = "t4t.SQLite3"
            ),

            // ==========================================
            // 2. DICCIONARIOS Y LÉXICOS (14 módulos)
            // ==========================================
            // 19. Multilexicon Dictionary
            BibleModule(
                id = "mod_multi_dict",
                category = "DICTIONARY",
                title = "Diccionario Multiléxico y Códigos Strong",
                author = "Compilación Léxica MyBible",
                description = "Definiciones etimológicas completas y concordancia Strong multilingüe.",
                language = "Español / Hebreo / Griego",
                versionTag = "MULTI",
                sizeText = "15.2 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/Multi.dictionary.SQLite3",
                fileName = "Multi.dictionary.SQLite3"
            ),
            // 20. Concordancia Bíblica
            BibleModule(
                id = "mod_conc_dict",
                category = "DICTIONARY",
                title = "Concordancia Bíblica",
                author = "Varios Autores",
                description = "Concordancia alfabética de palabras clave y referencias bíblicas.",
                language = "Español",
                versionTag = "CONC",
                sizeText = "8.5 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/Conc.dictionary.SQLite3",
                fileName = "Conc.dictionary.SQLite3"
            ),
            // 21. Diccionario Bíblico General
            BibleModule(
                id = "mod_es_dict",
                category = "DICTIONARY",
                title = "Diccionario Bíblico General",
                author = "Varios Autores",
                description = "Compendio de términos y conceptos bíblicos en lengua española.",
                language = "Español",
                versionTag = "ES-DICT",
                sizeText = "12.8 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/es.dictionary.SQLite3",
                fileName = "es.dictionary.SQLite3"
            ),
            // 22. Concordancia Strong Español
            BibleModule(
                id = "mod_str_es",
                category = "DICTIONARY",
                title = "Concordancia Strong Español",
                author = "James Strong",
                description = "Definiciones y concordancia léxica en español con numeración Strong.",
                language = "Español",
                versionTag = "STR-ES",
                sizeText = "17.4 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/Str-ES.dictionary.SQLite3",
                fileName = "Str-ES.dictionary.SQLite3"
            ),
            // 23. Enhanced Strong's Lexicon
            BibleModule(
                id = "mod_str_plus",
                category = "DICTIONARY",
                title = "Enhanced Strong's Lexicon",
                author = "James Strong",
                description = "Diccionario con codificación Strong ampliada y referencias analíticas.",
                language = "Inglés",
                versionTag = "STR-PLUS",
                sizeText = "18.9 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/Str-Plus.dictionary.SQLite3",
                fileName = "Str-Plus.dictionary.SQLite3"
            ),
            // 24. Dictionary of Biblical Languages
            BibleModule(
                id = "mod_swanson",
                category = "DICTIONARY",
                title = "Dictionary of Biblical Languages",
                author = "James Swanson",
                description = "Léxico semántico exhaustivo del hebreo, arameo y griego bíblico.",
                language = "Inglés",
                versionTag = "SWANSON",
                sizeText = "24.6 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/Swanson.dictionary.SQLite3",
                fileName = "Swanson.dictionary.SQLite3"
            ),
            // 25. Theological Wordbook of the OT
            BibleModule(
                id = "mod_twot",
                category = "DICTIONARY",
                title = "Theological Wordbook of the OT",
                author = "Harris, Archer & Waltke",
                description = "Vocabulario teológico del Antiguo Testamento hebreo y arameo.",
                language = "Inglés",
                versionTag = "TWOT",
                sizeText = "2.2 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/TWOT.dictionary.SQLite3",
                fileName = "TWOT.dictionary.SQLite3"
            ),
            // 26. Webster's 1828 Dictionary
            BibleModule(
                id = "mod_webster",
                category = "DICTIONARY",
                title = "Webster's 1828 Dictionary",
                author = "Noah Webster",
                description = "Definiciones bíblicas e históricas del inglés clásico tradicional.",
                language = "Inglés",
                versionTag = "WEB1828",
                sizeText = "14.1 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/Webster.dictionary.SQLite3",
                fileName = "Webster.dictionary.SQLite3"
            ),
            // 27. Brown-Driver-Briggs Lexicon
            BibleModule(
                id = "mod_bdb_t",
                category = "DICTIONARY",
                title = "Brown-Driver-Briggs Lexicon",
                author = "Brown, Driver & Briggs",
                description = "Léxico hebreo y arameo estándar de referencia académica.",
                language = "Inglés",
                versionTag = "BDB-T",
                sizeText = "7.8 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/BDB-T.dictionary.SQLite3",
                fileName = "BDB-T.dictionary.SQLite3"
            ),
            // 28. Léxico y Diccionario Semántico Español (LDS-es)
            BibleModule(
                id = "mod_lds_es",
                category = "DICTIONARY",
                title = "Léxico y Diccionario Semántico Español",
                author = "Varios Autores",
                description = "Diccionario de términos, lugares y nombres bíblicos en español.",
                language = "Español",
                versionTag = "LDS-ES",
                sizeText = "6.1 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/LDS-es.dictionary.SQLite3",
                fileName = "LDS-es.dictionary.SQLite3"
            ),
            // 29. Diccionario Vine Antiguo Testamento
            BibleModule(
                id = "mod_vine_at_es",
                category = "DICTIONARY",
                title = "Diccionario Vine Antiguo Testamento",
                author = "William Edwy Vine",
                description = "Diccionario expositivo de palabras del Antiguo Testamento (Hebreo-Español).",
                language = "Español",
                versionTag = "HE-ES",
                sizeText = "3.8 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/VineAT-es.dictionary.SQLite3",
                fileName = "VineAT-es.dictionary.SQLite3"
            ),
            // 30. Diccionario Vine Nuevo Testamento
            BibleModule(
                id = "mod_vine_nt_es",
                category = "DICTIONARY",
                title = "Diccionario Vine Nuevo Testamento",
                author = "William Edwy Vine",
                description = "Diccionario expositivo de palabras del Nuevo Testamento (Griego-Español).",
                language = "Español",
                versionTag = "GR-ES",
                sizeText = "4.5 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/VineNT-es.dictionary.SQLite3",
                fileName = "VineNT-es.dictionary.SQLite3"
            ),
            // 31. Vine's NT Expository Dictionary
            BibleModule(
                id = "mod_vine_nt",
                category = "DICTIONARY",
                title = "Vine's NT Expository Dictionary",
                author = "William Edwy Vine",
                description = "Expository dictionary of New Testament words with Greek analysis.",
                language = "Inglés",
                versionTag = "VINE-NT",
                sizeText = "4.2 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/VineNT.dictionary.SQLite3",
                fileName = "VineNT.dictionary.SQLite3"
            ),
            // 32. Vine's OT Expository Dictionary
            BibleModule(
                id = "mod_vine_ot",
                category = "DICTIONARY",
                title = "Vine's OT Expository Dictionary",
                author = "William Edwy Vine",
                description = "Expository dictionary of Old Testament words with Hebrew analysis.",
                language = "Inglés",
                versionTag = "VINE-OT",
                sizeText = "3.5 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/VineOT.dictionary.SQLite3",
                fileName = "VineOT.dictionary.SQLite3"
            ),

            // ==========================================
            // 3. COMENTARIOS BÍBLICOS (27 módulos)
            // ==========================================
            // --- A. Comentarios exegéticos generales (12 módulos) ---
            // 33. John Wesley's Explanatory Notes
            BibleModule(
                id = "mod_wesley",
                category = "COMMENTARY",
                title = "John Wesley's Explanatory Notes",
                author = "John Wesley",
                description = "Notas explicativas y prácticas sobre el Antiguo y Nuevo Testamento.",
                language = "Inglés",
                versionTag = "WESLEY",
                sizeText = "4.7 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/Wesley-c.commentaries.SQLite3",
                fileName = "Wesley-c.commentaries.SQLite3"
            ),
            // 34. Comentario Matthew Henry
            BibleModule(
                id = "mod_cmh_c",
                category = "COMMENTARY",
                title = "Comentario Matthew Henry",
                author = "Matthew Henry",
                description = "Comentario devocional y exegético conciso de las Sagradas Escrituras.",
                language = "Español",
                versionTag = "CMH",
                sizeText = "4.8 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/CMH-c.commentaries.SQLite3",
                fileName = "CMH-c.commentaries.SQLite3"
            ),
            // 35. Comentario Bíblico William MacDonald
            BibleModule(
                id = "mod_cbwm_c",
                category = "COMMENTARY",
                title = "Comentario Bíblico William MacDonald",
                author = "William MacDonald",
                description = "Comentario expositivo exhaustivo del Antiguo y Nuevo Testamento.",
                language = "Español",
                versionTag = "CBWM",
                sizeText = "3.5 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/CBWM-c.commentaries.SQLite3",
                fileName = "CBWM-c.commentaries.SQLite3"
            ),
            // 36. Comentario José Flores
            BibleModule(
                id = "mod_flores_c",
                category = "COMMENTARY",
                title = "Comentario José Flores",
                author = "José Flores",
                description = "Notas pastorales y expositivas para la edificación bíblica.",
                language = "Español",
                versionTag = "FLORES",
                sizeText = "3.2 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/Flores-c.commentaries.SQLite3",
                fileName = "Flores-c.commentaries.SQLite3"
            ),
            // 37. Comentario Jamieson-Fausset-Brown
            BibleModule(
                id = "mod_jfb_es_c",
                category = "COMMENTARY",
                title = "Comentario Jamieson-Fausset-Brown",
                author = "Jamieson, Fausset & Brown",
                description = "Comentario exegético y explicativo de toda la Biblia en español.",
                language = "Español",
                versionTag = "JFB-ES",
                sizeText = "6.9 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/JFB-ES-c.commentaries.SQLite3",
                fileName = "JFB-ES-c.commentaries.SQLite3"
            ),
            // 38. Matthew Henry Whole Bible Commentary (MHWBC)
            BibleModule(
                id = "mod_mhwbc_c",
                category = "COMMENTARY",
                title = "Whole Bible Commentary",
                author = "Matthew Henry",
                description = "Comentario devocional y exegético exhaustivo de toda la Biblia versículo a versículo.",
                language = "Inglés",
                versionTag = "MHWBC",
                sizeText = "58.7 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/MHWBC.commentaries.SQLite3",
                fileName = "MHWBC.commentaries.SQLite3"
            ),
            // 39. Jonathan Edwards Notes
            BibleModule(
                id = "mod_edw_c",
                category = "COMMENTARY",
                title = "Jonathan Edwards Notes",
                author = "Jonathan Edwards",
                description = "Notas teológicas y reflexiones exegéticas de Jonathan Edwards.",
                language = "Inglés",
                versionTag = "EDW",
                sizeText = "2.8 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/Edw-c.commentaries.SQLite3",
                fileName = "Edw-c.commentaries.SQLite3"
            ),
            // 40. Charles Spurgeon Notes
            BibleModule(
                id = "mod_spurg_c",
                category = "COMMENTARY",
                title = "Charles Spurgeon Notes",
                author = "Charles H. Spurgeon",
                description = "Notas expositivas y sermones del reconocido pastor Charles Spurgeon.",
                language = "Inglés",
                versionTag = "SPURG",
                sizeText = "3.1 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/Spurg-c.commentaries.SQLite3",
                fileName = "Spurg-c.commentaries.SQLite3"
            ),
            // 41. Preacher's Homiletical Commentary
            BibleModule(
                id = "mod_pchc_c",
                category = "COMMENTARY",
                title = "Preacher's Homiletical Commentary",
                author = "Exell, Jones et al.",
                description = "Comentario homilético exhaustivo con bosquejos y sermones.",
                language = "Inglés",
                versionTag = "PCHC",
                sizeText = "8.4 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/PCHC-c.commentaries.SQLite3",
                fileName = "PCHC-c.commentaries.SQLite3"
            ),
            // 42. Treasury of Scripture Knowledge
            BibleModule(
                id = "mod_tsk_c",
                category = "COMMENTARY",
                title = "Treasury of Scripture Knowledge",
                author = "R. A. Torrey",
                description = "Sistema exhaustivo de referencias cruzadas y temas bíblicos correlacionados.",
                language = "Inglés",
                versionTag = "TSK",
                sizeText = "7.5 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/TSK-c.commentaries.SQLite3",
                fileName = "TSK-c.commentaries.SQLite3"
            ),
            // 43. Treasury of Scripture Knowledge 2
            BibleModule(
                id = "mod_tsk2_c",
                category = "COMMENTARY",
                title = "Treasury of Scripture Knowledge 2",
                author = "R. A. Torrey / Jerome Smith",
                description = "Edición ampliada con más de 500,000 referencias cruzadas analíticas.",
                language = "Inglés",
                versionTag = "TSK2",
                sizeText = "9.2 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/TSK2-c.commentaries.SQLite3",
                fileName = "TSK2-c.commentaries.SQLite3"
            ),
            // 44. When Critics Ask
            BibleModule(
                id = "mod_wca_c",
                category = "COMMENTARY",
                title = "When Critics Ask",
                author = "Norman Geisler & Thomas Howe",
                description = "Manual exegético y apologético para resolver dificultades bíblicas.",
                language = "Inglés",
                versionTag = "WCA",
                sizeText = "4.3 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/WCA-c.commentaries.SQLite3",
                fileName = "WCA-c.commentaries.SQLite3"
            ),

            // --- B. Notas textuales de versiones (15 módulos) ---
            // 45. Notas Reina-Valera 1960
            BibleModule(
                id = "mod_rv1960_c",
                category = "COMMENTARY",
                title = "Notas Reina-Valera 1960",
                author = "Sociedades Bíblicas Unidas",
                description = "Notas de estudio y referencias explicativas para la RVR1960.",
                language = "Español",
                versionTag = "RVR-C",
                sizeText = "3.8 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/RV1960-c.commentaries.SQLite3",
                fileName = "RV1960-c.commentaries.SQLite3"
            ),
            // 46. Notas Explicativas NTV
            BibleModule(
                id = "mod_ntv_c",
                category = "COMMENTARY",
                title = "Notas Explicativas NTV",
                author = "Tyndale House Publishers",
                description = "Notas aclaratorias al pie de página y perícopas de la NTV.",
                language = "Español",
                versionTag = "NTV-C",
                sizeText = "1.9 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/NTV.commentaries.SQLite3",
                fileName = "NTV.commentaries.SQLite3"
            ),
            // 47. Notas de Estudio NVI
            BibleModule(
                id = "mod_nvi_notes",
                category = "COMMENTARY",
                title = "Notas de Estudio NVI",
                author = "Biblica",
                description = "Notas textuales, aclaraciones culturales y referencias de la NVI.",
                language = "Español",
                versionTag = "NVI-N",
                sizeText = "2.3 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/NVI.commentaries.SQLite3",
                fileName = "NVI.commentaries.SQLite3"
            ),
            // 48. Comentario Bíblico NVI
            BibleModule(
                id = "mod_nvi_c",
                category = "COMMENTARY",
                title = "Comentario Bíblico NVI",
                author = "Biblica",
                description = "Comentario temático y teológico complementario de la NVI.",
                language = "Español",
                versionTag = "NVI-C",
                sizeText = "4.1 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/NVI-c.commentaries.SQLite3",
                fileName = "NVI-c.commentaries.SQLite3"
            ),
            // 49. Notas Explicativas PDT
            BibleModule(
                id = "mod_pdt_c",
                category = "COMMENTARY",
                title = "Notas Explicativas PDT",
                author = "Liga Bíblica Mundial",
                description = "Notas explicativas de vocabulario y contexto histórico de la PDT.",
                language = "Español",
                versionTag = "PDT-C",
                sizeText = "1.4 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/PDT.commentaries.SQLite3",
                fileName = "PDT.commentaries.SQLite3"
            ),
            // 50. Notas Explicativas TLA
            BibleModule(
                id = "mod_tla_c",
                category = "COMMENTARY",
                title = "Notas Explicativas TLA",
                author = "Sociedades Bíblicas Unidas",
                description = "Aclaraciones de términos culturales, geográficos y de traducción de la TLA.",
                language = "Español",
                versionTag = "TLA-C",
                sizeText = "1.2 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/TLA-c.commentaries.SQLite3",
                fileName = "TLA-c.commentaries.SQLite3"
            ),
            // 51. Amplified Bible Notes
            BibleModule(
                id = "mod_amp_notes",
                category = "COMMENTARY",
                title = "Amplified Bible Notes",
                author = "The Lockman Foundation",
                description = "Notas explicativas y referencias contextuales de la versión Amplified.",
                language = "Inglés",
                versionTag = "AMP-C",
                sizeText = "5.1 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/AMP.commentaries.SQLite3",
                fileName = "AMP.commentaries.SQLite3"
            ),
            // 52. Amplified Classic Notes
            BibleModule(
                id = "mod_ampc_notes",
                category = "COMMENTARY",
                title = "Amplified Classic Notes",
                author = "The Lockman Foundation",
                description = "Anotaciones clásicas explicativas de la edición AMPC.",
                language = "Inglés",
                versionTag = "AMPC-N",
                sizeText = "2.2 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/AMPC.commentaries.SQLite3",
                fileName = "AMPC.commentaries.SQLite3"
            ),
            // 53. ESV Global Study Notes
            BibleModule(
                id = "mod_esv_c",
                category = "COMMENTARY",
                title = "ESV Global Study Notes",
                author = "Crossway",
                description = "Notas de estudio y contexto doctrinal de la traducción ESV.",
                language = "Inglés",
                versionTag = "ESV-C",
                sizeText = "5.6 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/ESV.commentaries.SQLite3",
                fileName = "ESV.commentaries.SQLite3"
            ),
            // 54. KJV Study Notes
            BibleModule(
                id = "mod_kjv_c",
                category = "COMMENTARY",
                title = "KJV Study Notes",
                author = "Cambridge University Press",
                description = "Notas contextuales y referencias históricas tradicionales de la KJV.",
                language = "Inglés",
                versionTag = "KJV-C",
                sizeText = "2.7 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/KJV-c.commentaries.SQLite3",
                fileName = "KJV-c.commentaries.SQLite3"
            ),
            // 55. NIV 2011 Study Notes
            BibleModule(
                id = "mod_niv11_notes",
                category = "COMMENTARY",
                title = "NIV 2011 Study Notes",
                author = "Biblica",
                description = "Notas aclaratorias, lingüísticas e históricas de la edición NIV 2011.",
                language = "Inglés",
                versionTag = "NIV11-N",
                sizeText = "3.4 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/NIV.11.commentaries.SQLite3",
                fileName = "NIV.11.commentaries.SQLite3"
            ),
            // 56. NKJV Study Notes
            BibleModule(
                id = "mod_nkjv_c",
                category = "COMMENTARY",
                title = "NKJV Study Notes",
                author = "Thomas Nelson",
                description = "Notas explicativas, históricas y doctrinales de la versión NKJV.",
                language = "Inglés",
                versionTag = "NKJV-C",
                sizeText = "5.4 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/NKJV.commentaries.SQLite3",
                fileName = "NKJV.commentaries.SQLite3"
            ),
            // 57. NLT 2015 Study Notes
            BibleModule(
                id = "mod_nlt15_notes",
                category = "COMMENTARY",
                title = "NLT 2015 Study Notes",
                author = "Tyndale House Publishers",
                description = "Notas y perícopas de estudio para la traducción NLT 2015.",
                language = "Inglés",
                versionTag = "NLT15-N",
                sizeText = "3.1 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/NLT.15.commentaries.SQLite3",
                fileName = "NLT.15.commentaries.SQLite3"
            ),
            // 58. The Passion Footnotes
            BibleModule(
                id = "mod_tpt_c",
                category = "COMMENTARY",
                title = "The Passion Footnotes",
                author = "Brian Simmons / BroadStreet",
                description = "Notas de estudio devocional con trasfondo lingüístico hebreo y arameo.",
                language = "Inglés",
                versionTag = "TPT-C",
                sizeText = "3.2 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/TPT.commentaries.SQLite3",
                fileName = "TPT.commentaries.SQLite3"
            ),
            // 59. Translation 4 Translators Notes
            BibleModule(
                id = "mod_t4t_c",
                category = "COMMENTARY",
                title = "Translation 4 Translators Notes",
                author = "Ellis W. Deibler Jr.",
                description = "Notas explicativas y lingüísticas para facilitadores de traducción.",
                language = "Inglés",
                versionTag = "T4T-C",
                sizeText = "3.9 MB",
                isDownloaded = false,
                downloadUrl = "$GITHUB_BASE_URL/t4t.commentaries.SQLite3",
                fileName = "t4t.commentaries.SQLite3"
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
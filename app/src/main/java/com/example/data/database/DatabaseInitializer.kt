package com.example.data.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.data.dao.BibleDao
import com.example.data.model.BibleModule
import com.example.data.model.BibleVerse
import com.example.data.model.Commentary
import com.example.data.model.DictionaryEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object DatabaseInitializer {

    suspend fun populateInitialData(context: Context, bibleDao: BibleDao) = withContext(Dispatchers.IO) {
        // Si ya existen versículos cargados, no vuelve a ejecutar la inserción
        if (bibleDao.getVerseCount() > 1000) return@withContext

        try {
            // 1. Registrar los tres módulos reales con las categorías exactas de la interfaz
            val initialModules = listOf(
                BibleModule(
                    id = "rvr1960",
                    title = "Reina-Valera 1960 (RVR1960)",
                    author = "Casiodoro de Reina / Cipriano de Valera",
                    category = "Versiones",
                    language = "Español",
                    versionTag = "RVR1960",
                    sizeText = "3.8 MB",
                    isDownloaded = true,
                    description = "Texto bíblico tradicional y fidedigno en lengua española."
                ),
                BibleModule(
                    id = "wesley",
                    title = "Notas Exegéticas de John Wesley",
                    author = "John Wesley",
                    category = "Comentarios",
                    language = "Español",
                    versionTag = "JW",
                    sizeText = "8.5 MB",
                    isDownloaded = true,
                    description = "Explicaciones históricas y teológicas verso por verso."
                ),
                BibleModule(
                    id = "multi",
                    title = "Diccionario Bíblico y Códigos Strong",
                    author = "Compilación Léxica MyBible",
                    category = "Diccionarios",
                    language = "Español / Hebreo / Griego",
                    versionTag = "MULTI",
                    sizeText = "15.0 MB",
                    isDownloaded = true,
                    description = "Definiciones etimológicas completas y concordancia Strong."
                )
            )
            bibleDao.insertModules(initialModules)

            // 2. Extraer y cargar los versículos bíblicos reales de RVR1960.SQLite3
            loadVerses(context, bibleDao, "RVR1960.SQLite3")

            // 3. Extraer y cargar los comentarios reales de Wesley-c.commentaries.SQLite3
            loadCommentaries(context, bibleDao, "Wesley-c.commentaries.SQLite3")

            // 4. Extraer y cargar el diccionario real de Multi.dictionary.SQLite3
            loadDictionary(context, bibleDao, "Multi.dictionary.SQLite3")

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun copyAssetToTemp(context: Context, assetName: String, tempName: String): File {
        val tempFile = File(context.cacheDir, tempName)
        context.assets.open("databases/$assetName").use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }

    private suspend fun loadVerses(context: Context, bibleDao: BibleDao, fileName: String) {
        val tempFile = copyAssetToTemp(context, fileName, "temp_verses.sqlite3")
        val sqliteDb = SQLiteDatabase.openDatabase(tempFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)

        val cursor = sqliteDb.rawQuery("SELECT book_number, chapter, verse, text FROM verses", null)
        val list = ArrayList<BibleVerse>(32000)

        val colBook = cursor.getColumnIndex("book_number")
        val colChapter = cursor.getColumnIndex("chapter")
        val colVerse = cursor.getColumnIndex("verse")
        val colText = cursor.getColumnIndex("text")

        while (cursor.moveToNext()) {
            val bookNum = cursor.getInt(colBook)
            val chap = cursor.getInt(colChapter)
            val ver = cursor.getInt(colVerse)
            val rawText = cursor.getString(colText) ?: ""

            list.add(
                BibleVerse(
                    bookNumber = bookNum,
                    bookName = BibleVerse.getCanonicalBookName(bookNum),
                    testament = if (bookNum < 400) "AT" else "NT",
                    version = "RVR1960",
                    chapter = chap,
                    verse = ver,
                    text = rawText
                )
            )
        }
        cursor.close()
        sqliteDb.close()
        tempFile.delete()

        list.chunked(1000).forEach { bibleDao.insertVerses(it) }
    }

    private suspend fun loadCommentaries(context: Context, bibleDao: BibleDao, fileName: String) {
        try {
            val tempFile = copyAssetToTemp(context, fileName, "temp_comm.sqlite3")
            val sqliteDb = SQLiteDatabase.openDatabase(tempFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)

            val cursor = sqliteDb.rawQuery(
                "SELECT book_number, chapter_number_from, verse_number_from, text FROM commentaries",
                null
            )
            val list = ArrayList<Commentary>()

            val colBook = cursor.getColumnIndex("book_number")
            val colChap = cursor.getColumnIndex("chapter_number_from")
            val colVer = cursor.getColumnIndex("verse_number_from")
            val colText = cursor.getColumnIndex("text")

            while (cursor.moveToNext()) {
                val bookNum = cursor.getInt(colBook)
                val chap = cursor.getInt(colChap)
                val ver = cursor.getInt(colVer)
                val rawText = cursor.getString(colText) ?: ""

                list.add(
                    Commentary(
                        bookName = BibleVerse.getCanonicalBookName(bookNum),
                        chapter = chap,
                        verse = ver,
                        author = "John Wesley",
                        title = "Comentario Exegético",
                        content = rawText.replace(Regex("<[^>]*>"), "").trim()
                    )
                )
            }
            cursor.close()
            sqliteDb.close()
            tempFile.delete()

            list.chunked(500).forEach { bibleDao.insertCommentaries(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun loadDictionary(context: Context, bibleDao: BibleDao, fileName: String) {
        try {
            val tempFile = copyAssetToTemp(context, fileName, "temp_dict.sqlite3")
            val sqliteDb = SQLiteDatabase.openDatabase(tempFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)

            val cursor = sqliteDb.rawQuery("SELECT topic, definition FROM dictionary", null)
            val list = ArrayList<DictionaryEntry>()

            val colTopic = cursor.getColumnIndex("topic")
            val colDef = cursor.getColumnIndex("definition")

            while (cursor.moveToNext()) {
                val topic = cursor.getString(colTopic) ?: ""
                val definition = cursor.getString(colDef) ?: ""

                list.add(
                    DictionaryEntry(
                        term = topic,
                        displayTerm = topic,
                        definition = definition.replace(Regex("<[^>]*>"), "").trim(),
                        strongCode = if (topic.startsWith("H") || topic.startsWith("G")) topic else "",
                        originalWord = "",
                        pronunciation = "",
                        theologicalNotes = ""
                    )
                )
            }
            cursor.close()
            sqliteDb.close()
            tempFile.delete()

            list.chunked(1000).forEach { bibleDao.insertDictionaryEntries(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
package com.example.ui.util

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

data class DailyVerseReference(
    val bookNumber: Int,
    val bookNameEs: String,
    val bookNameEn: String,
    val chapter: Int,
    val verse: Int
)

data class LoadedDailyVerse(
    val bookNumber: Int,
    val bookName: String,
    val chapter: Int,
    val verse: Int,
    val text: String,
    val version: String
)

object DailyVerseCatalog {

    /**
     * Limpia etiquetas HTML, códigos Strong <S>...</S> y números sufijos pegados (Así5620).
     */
    fun cleanBibleText(rawText: String): String {
        return rawText
            .replace(Regex("<S>[^<]*</S>", RegexOption.IGNORE_CASE), "")
            .replace(Regex("<f>[^<]*</f>", RegexOption.IGNORE_CASE), "")
            .replace(Regex("<[^>]+>"), "")
            .replace(Regex("(?<=\\p{L})\\d+"), "") // Elimina números Strong pegados a letras (ej: Así5620 -> Así)
            .replace(Regex("\\{\\d+\\}"), "")
            .replace(Regex("\\[\\d+\\]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    val references = listOf(
        DailyVerseReference(10, "Génesis", "Genesis", 1, 1),
        DailyVerseReference(430, "Juan", "John", 1, 1),
        DailyVerseReference(500, "Filipenses", "Philippians", 4, 13),
        DailyVerseReference(240, "Jeremías", "Jeremiah", 29, 11),
        DailyVerseReference(200, "Proverbios", "Proverbs", 3, 5),
        DailyVerseReference(200, "Proverbios", "Proverbs", 3, 6),
        DailyVerseReference(60, "Josué", "Joshua", 1, 9),
        DailyVerseReference(190, "Salmos", "Psalms", 23, 1),
        DailyVerseReference(190, "Salmos", "Psalms", 23, 4),
        DailyVerseReference(450, "Romanos", "Romans", 8, 28),
        DailyVerseReference(230, "Isaías", "Isaiah", 40, 31),
        DailyVerseReference(230, "Isaías", "Isaiah", 41, 10),
        DailyVerseReference(400, "Mateo", "Matthew", 6, 33),
        DailyVerseReference(490, "Efesios", "Ephesians", 2, 8),
        DailyVerseReference(490, "Efesios", "Ephesians", 2, 9),
        DailyVerseReference(580, "Hebreos", "Hebrews", 11, 1),
        DailyVerseReference(580, "Hebreos", "Hebrews", 11, 6),
        DailyVerseReference(550, "2 Timoteo", "2 Timothy", 1, 7),
        DailyVerseReference(190, "Salmos", "Psalms", 46, 1),
        DailyVerseReference(190, "Salmos", "Psalms", 91, 1),
        DailyVerseReference(190, "Salmos", "Psalms", 91, 2),
        DailyVerseReference(450, "Romanos", "Romans", 12, 2),
        DailyVerseReference(480, "Gálatas", "Galatians", 2, 20),
        DailyVerseReference(470, "2 Corintios", "2 Corinthians", 5, 17),
        DailyVerseReference(600, "1 Pedro", "1 Peter", 5, 7),
        DailyVerseReference(190, "Salmos", "Psalms", 119, 105),
        DailyVerseReference(250, "Lamentaciones", "Lamentations", 3, 22),
        DailyVerseReference(250, "Lamentaciones", "Lamentations", 3, 23),
        DailyVerseReference(330, "Miqueas", "Micah", 6, 8),
        DailyVerseReference(590, "Santiago", "James", 1, 5),
        DailyVerseReference(430, "Juan", "John", 3, 16),
        DailyVerseReference(620, "1 Juan", "1 John", 4, 19),
        DailyVerseReference(460, "1 Corintios", "1 Corinthians", 13, 13),
        DailyVerseReference(450, "Romanos", "Romans", 5, 8),
        DailyVerseReference(430, "Juan", "John", 15, 12),
        DailyVerseReference(510, "Colosenses", "Colossians", 3, 14),
        DailyVerseReference(190, "Salmos", "Psalms", 103, 12),
        DailyVerseReference(450, "Romanos", "Romans", 8, 38),
        DailyVerseReference(450, "Romanos", "Romans", 8, 39),
        DailyVerseReference(480, "Gálatas", "Galatians", 5, 22),
        DailyVerseReference(480, "Gálatas", "Galatians", 5, 23),
        DailyVerseReference(200, "Proverbios", "Proverbs", 4, 23),
        DailyVerseReference(200, "Proverbios", "Proverbs", 16, 3),
        DailyVerseReference(550, "2 Timoteo", "2 Timothy", 3, 16),
        DailyVerseReference(580, "Hebreos", "Hebrews", 4, 12),
        DailyVerseReference(430, "Juan", "John", 8, 32),
        DailyVerseReference(430, "Juan", "John", 14, 6),
        DailyVerseReference(430, "Juan", "John", 10, 10),
        DailyVerseReference(450, "Romanos", "Romans", 5, 1),
        DailyVerseReference(500, "Filipenses", "Philippians", 4, 6),
        DailyVerseReference(500, "Filipenses", "Philippians", 4, 7),
        DailyVerseReference(400, "Mateo", "Matthew", 11, 28),
        DailyVerseReference(230, "Isaías", "Isaiah", 26, 3),
        DailyVerseReference(190, "Salmos", "Psalms", 121, 1),
        DailyVerseReference(190, "Salmos", "Psalms", 121, 2),
        DailyVerseReference(490, "Efesios", "Ephesians", 6, 10),
        DailyVerseReference(490, "Efesios", "Ephesians", 6, 11),
        DailyVerseReference(190, "Salmos", "Psalms", 27, 1),
        DailyVerseReference(190, "Salmos", "Psalms", 37, 4),
        DailyVerseReference(190, "Salmos", "Psalms", 37, 5),
        DailyVerseReference(470, "2 Corintios", "2 Corinthians", 12, 9),
        DailyVerseReference(450, "Romanos", "Romans", 8, 31),
        DailyVerseReference(190, "Salmos", "Psalms", 100, 1),
        DailyVerseReference(190, "Salmos", "Psalms", 100, 2),
        DailyVerseReference(520, "1 Tesalonicenses", "1 Thessalonians", 5, 16),
        DailyVerseReference(520, "1 Tesalonicenses", "1 Thessalonians", 5, 17),
        DailyVerseReference(520, "1 Tesalonicenses", "1 Thessalonians", 5, 18),
        DailyVerseReference(190, "Salmos", "Psalms", 118, 24),
        DailyVerseReference(190, "Salmos", "Psalms", 51, 10),
        DailyVerseReference(500, "Filipenses", "Philippians", 1, 6),
        DailyVerseReference(620, "1 Juan", "1 John", 1, 9),
        DailyVerseReference(510, "Colosenses", "Colossians", 3, 23),
        DailyVerseReference(510, "Colosenses", "Colossians", 3, 24),
        DailyVerseReference(240, "Jeremías", "Jeremiah", 33, 3),
        DailyVerseReference(400, "Mateo", "Matthew", 7, 7),
        DailyVerseReference(580, "Hebreos", "Hebrews", 4, 16),
        DailyVerseReference(230, "Isaías", "Isaiah", 9, 6),
        DailyVerseReference(420, "Lucas", "Luke", 2, 10),
        DailyVerseReference(420, "Lucas", "Luke", 2, 11),
        DailyVerseReference(660, "Apocalipsis", "Revelation", 22, 20)
    )

    fun getTodayReference(): DailyVerseReference {
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val index = (dayOfYear - 1).coerceAtLeast(0)
        return references[index % references.size]
    }

    suspend fun loadTodayVerse(context: Context, isEnglish: Boolean): LoadedDailyVerse = withContext(Dispatchers.IO) {
        val todayRef = getTodayReference()
        val dbFileName = if (isEnglish) "KJV.SQLite3" else "RVR1960.SQLite3"
        val versionName = if (isEnglish) "KJV" else "RVR 1960"
        val bookName = if (isEnglish) todayRef.bookNameEn else todayRef.bookNameEs

        try {
            val tempFile = File(context.cacheDir, "daily_$dbFileName")
            if (!tempFile.exists()) {
                val assetPath = try {
                    if (context.assets.list("databases")?.contains(dbFileName) == true) {
                        "databases/$dbFileName"
                    } else {
                        dbFileName
                    }
                } catch (e: Exception) {
                    dbFileName
                }

                context.assets.open(assetPath).use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }

            val sqliteDb = SQLiteDatabase.openDatabase(tempFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
            val cursor = sqliteDb.rawQuery(
                "SELECT text FROM verses WHERE book_number = ? AND chapter = ? AND verse = ? LIMIT 1",
                arrayOf(todayRef.bookNumber.toString(), todayRef.chapter.toString(), todayRef.verse.toString())
            )

            var rawText = ""
            if (cursor.moveToFirst()) {
                rawText = cursor.getString(0) ?: ""
            }
            cursor.close()
            sqliteDb.close()

            val cleanText = cleanBibleText(rawText)
            if (cleanText.isNotBlank()) {
                return@withContext LoadedDailyVerse(
                    bookNumber = todayRef.bookNumber,
                    bookName = bookName,
                    chapter = todayRef.chapter,
                    verse = todayRef.verse,
                    text = cleanText,
                    version = versionName
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val fallbackText = if (isEnglish) {
            "For God so loved the world, that he gave his only begotten Son, that whosoever believeth in him should not perish, but have everlasting life."
        } else {
            "Porque de tal manera amó Dios al mundo, que ha dado a su Hijo unigénito, para que todo aquel que en él cree, no se pierda, mas tenga vida eterna."
        }
        return@withContext LoadedDailyVerse(
            bookNumber = 430,
            bookName = if (isEnglish) "John" else "Juan",
            chapter = 3,
            verse = 16,
            text = fallbackText,
            version = versionName
        )
    }
}
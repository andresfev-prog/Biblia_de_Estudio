package com.example.data.model

data class BookInfo(
    val number: Int,
    val name: String,
    val abbreviation: String,
    val testament: String, // "AT" or "NT"
    val category: String, // "Pentateuco", "Históricos", "Poéticos", "Profetas Mayores", "Profetas Menores", "Evangelios", "Histórico", "Epístolas Paulinas", "Epístolas Generales", "Profecía"
    val chapterCount: Int,
    val englishName: String = name
)

object BibleBooksData {
    val books: List<BookInfo> = listOf(
        // Antiguo Testamento
        // Pentateuco
        BookInfo(1, "Génesis", "Gén", "AT", "Pentateuco", 50, "Genesis"),
        BookInfo(2, "Éxodo", "Éx", "AT", "Pentateuco", 40, "Exodus"),
        BookInfo(3, "Levítico", "Lev", "AT", "Pentateuco", 27, "Leviticus"),
        BookInfo(4, "Números", "Núm", "AT", "Pentateuco", 36, "Numbers"),
        BookInfo(5, "Deuteronomio", "Deut", "AT", "Pentateuco", 34, "Deuteronomy"),
        // Históricos
        BookInfo(6, "Josué", "Jos", "AT", "Históricos", 24, "Joshua"),
        BookInfo(7, "Jueces", "Jue", "AT", "Históricos", 21, "Judges"),
        BookInfo(8, "Rut", "Rut", "AT", "Históricos", 4, "Ruth"),
        BookInfo(9, "1 Samuel", "1 Sam", "AT", "Históricos", 31, "1 Samuel"),
        BookInfo(10, "2 Samuel", "2 Sam", "AT", "Históricos", 24, "2 Samuel"),
        BookInfo(11, "1 Reyes", "1 Rey", "AT", "Históricos", 22, "1 Kings"),
        BookInfo(12, "2 Reyes", "2 Rey", "AT", "Históricos", 25, "2 Kings"),
        BookInfo(13, "1 Crónicas", "1 Crón", "AT", "Históricos", 29, "1 Chronicles"),
        BookInfo(14, "2 Crónicas", "2 Crón", "AT", "Históricos", 36, "2 Chronicles"),
        BookInfo(15, "Esdras", "Esd", "AT", "Históricos", 10, "Ezra"),
        BookInfo(16, "Nehemías", "Neh", "AT", "Históricos", 13, "Nehemiah"),
        BookInfo(17, "Ester", "Est", "AT", "Históricos", 10, "Esther"),
        // Poéticos
        BookInfo(18, "Job", "Job", "AT", "Poéticos", 42, "Job"),
        BookInfo(19, "Salmos", "Sal", "AT", "Poéticos", 150, "Psalms"),
        BookInfo(20, "Proverbios", "Prov", "AT", "Poéticos", 31, "Proverbs"),
        BookInfo(21, "Eclesiastés", "Ecl", "AT", "Poéticos", 12, "Ecclesiastes"),
        BookInfo(22, "Cantares", "Cant", "AT", "Poéticos", 8, "Song of Solomon"),
        // Profetas Mayores
        BookInfo(23, "Isaías", "Is", "AT", "Profetas Mayores", 66, "Isaiah"),
        BookInfo(24, "Jeremías", "Jer", "AT", "Profetas Mayores", 52, "Jeremiah"),
        BookInfo(25, "Lamentaciones", "Lam", "AT", "Profetas Mayores", 5, "Lamentations"),
        BookInfo(26, "Ezequiel", "Ez", "AT", "Profetas Mayores", 48, "Ezekiel"),
        BookInfo(27, "Daniel", "Dan", "AT", "Profetas Mayores", 12, "Daniel"),
        // Profetas Menores
        BookInfo(28, "Oseas", "Os", "AT", "Profetas Menores", 14, "Hosea"),
        BookInfo(29, "Joel", "Joel", "AT", "Profetas Menores", 3, "Joel"),
        BookInfo(30, "Amós", "Amós", "AT", "Profetas Menores", 9, "Amos"),
        BookInfo(31, "Abdías", "Abd", "AT", "Profetas Menores", 1, "Obadiah"),
        BookInfo(32, "Jonás", "Jon", "AT", "Profetas Menores", 4, "Jonah"),
        BookInfo(33, "Miqueas", "Miq", "AT", "Profetas Menores", 7, "Micah"),
        BookInfo(34, "Nahúm", "Nah", "AT", "Profetas Menores", 3, "Nahum"),
        BookInfo(35, "Habacuc", "Hab", "AT", "Profetas Menores", 3, "Habakkuk"),
        BookInfo(36, "Sofonías", "Sof", "AT", "Profetas Menores", 3, "Zephaniah"),
        BookInfo(37, "Hageo", "Hag", "AT", "Profetas Menores", 2, "Haggai"),
        BookInfo(38, "Zacarías", "Zac", "AT", "Profetas Menores", 14, "Zechariah"),
        BookInfo(39, "Malaquías", "Mal", "AT", "Profetas Menores", 4, "Malachi"),

        // Nuevo Testamento
        // Evangelios
        BookInfo(40, "Mateo", "Mt", "NT", "Evangelios", 28, "Matthew"),
        BookInfo(41, "Marcos", "Mc", "NT", "Evangelios", 16, "Mark"),
        BookInfo(42, "Lucas", "Lc", "NT", "Evangelios", 24, "Luke"),
        BookInfo(43, "Juan", "Jn", "NT", "Evangelios", 21, "John"),
        // Histórico
        BookInfo(44, "Hechos", "Hch", "NT", "Histórico", 28, "Acts"),
        // Epístolas Paulinas
        BookInfo(45, "Romanos", "Rom", "NT", "Epístolas Paulinas", 16, "Romans"),
        BookInfo(46, "1 Corintios", "1 Cor", "NT", "Epístolas Paulinas", 16, "1 Corinthians"),
        BookInfo(47, "2 Corintios", "2 Cor", "NT", "Epístolas Paulinas", 13, "2 Corinthians"),
        BookInfo(48, "Gálatas", "Gál", "NT", "Epístolas Paulinas", 6, "Galatians"),
        BookInfo(49, "Efesios", "Ef", "NT", "Epístolas Paulinas", 6, "Ephesians"),
        BookInfo(50, "Filipenses", "Flp", "NT", "Epístolas Paulinas", 4, "Philippians"),
        BookInfo(51, "Colosenses", "Col", "NT", "Epístolas Paulinas", 4, "Colossians"),
        BookInfo(52, "1 Tesalonicenses", "1 Tes", "NT", "Epístolas Paulinas", 5, "1 Thessalonians"),
        BookInfo(53, "2 Tesalonicenses", "2 Tes", "NT", "Epístolas Paulinas", 3, "2 Thessalonians"),
        BookInfo(54, "1 Timoteo", "1 Tim", "NT", "Epístolas Paulinas", 6, "1 Timothy"),
        BookInfo(55, "2 Timoteo", "2 Tim", "NT", "Epístolas Paulinas", 4, "2 Timothy"),
        BookInfo(56, "Tito", "Tit", "NT", "Epístolas Paulinas", 3, "Titus"),
        BookInfo(57, "Filemón", "Flm", "NT", "Epístolas Paulinas", 1, "Philemon"),
        // Epístolas Generales
        BookInfo(58, "Hebreos", "Heb", "NT", "Epístolas Generales", 13, "Hebrews"),
        BookInfo(59, "Santiago", "Stg", "NT", "Epístolas Generales", 5, "James"),
        BookInfo(60, "1 Pedro", "1 Pe", "NT", "Epístolas Generales", 5, "1 Peter"),
        BookInfo(61, "2 Pedro", "2 Pe", "NT", "Epístolas Generales", 3, "2 Peter"),
        BookInfo(62, "1 Juan", "1 Jn", "NT", "Epístolas Generales", 5, "1 John"),
        BookInfo(63, "2 Juan", "2 Jn", "NT", "Epístolas Generales", 1, "2 John"),
        BookInfo(64, "3 Juan", "3 Jn", "NT", "Epístolas Generales", 1, "3 John"),
        BookInfo(65, "Judas", "Jud", "NT", "Epístolas Generales", 1, "Jude"),
        // Profecía
        BookInfo(66, "Apocalipsis", "Apoc", "NT", "Profecía", 22, "Revelation")
    )

    private val bookAliases: Map<String, Int> = mapOf(
        "cantar de los cantares" to 22,
        "song of songs" to 22,
        "canticles" to 22,
        "hechos de los apostoles" to 44,
        "acts of the apostles" to 44,
        "apocalipsis (de juan)" to 66,
        "revelation of john" to 66,
        "salmo" to 19,
        "psalm" to 19
    )

    private val myBibleBookNumberToCanonical: Map<Int, Int> = mapOf(
        10 to 1, 20 to 2, 30 to 3, 40 to 4, 50 to 5,
        60 to 6, 70 to 7, 80 to 8, 90 to 9, 100 to 10,
        110 to 11, 120 to 12, 130 to 13, 140 to 14, 150 to 15,
        160 to 16, 190 to 17, 220 to 18, 230 to 19, 240 to 20,
        250 to 21, 260 to 22, 290 to 23, 300 to 24, 310 to 25,
        330 to 26, 340 to 27, 350 to 28, 360 to 29, 370 to 30,
        380 to 31, 390 to 32, 400 to 33, 410 to 34, 420 to 35,
        430 to 36, 440 to 37, 450 to 38, 460 to 39, 470 to 40,
        480 to 41, 490 to 42, 500 to 43, 510 to 44, 520 to 45,
        530 to 46, 540 to 47, 550 to 48, 560 to 49, 570 to 50,
        580 to 51, 590 to 52, 600 to 53, 610 to 54, 620 to 55,
        630 to 56, 640 to 57, 650 to 58, 660 to 59, 670 to 60,
        680 to 61, 690 to 62, 700 to 63, 710 to 64, 720 to 65,
        730 to 66
    )

    private val canonicalToMyBibleBookNumberMap: Map<Int, Int> =
        myBibleBookNumberToCanonical.entries.associate { (myBible, canonical) -> canonical to myBible }

    private fun normalizeToken(raw: String): String {
        return java.text.Normalizer.normalize(raw.trim(), java.text.Normalizer.Form.NFD)
            .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
            .lowercase()
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun getBookByName(name: String): BookInfo? {
        val clean = name.trim()
        val exact = books.firstOrNull {
            it.name.equals(clean, ignoreCase = true) ||
            it.englishName.equals(clean, ignoreCase = true) ||
            it.abbreviation.equals(clean, ignoreCase = true)
        }
        if (exact != null) return exact

        val normalized = normalizeToken(clean)
        bookAliases[normalized]?.let { bookId ->
            return getBookByNumber(bookId)
        }
        return books.firstOrNull {
            normalizeToken(it.name) == normalized ||
            normalizeToken(it.englishName) == normalized ||
            normalizeToken(it.abbreviation) == normalized
        }
    }

    fun getBookByNumber(number: Int): BookInfo? {
        return books.firstOrNull { it.number == number }
    }

    fun resolveCanonicalBookId(bookName: String, bookNumber: Int = 0): Int? {
        getBookByName(bookName)?.let { return it.number }
        if (bookNumber in 1..66) return bookNumber
        return myBibleBookNumberToCanonical[bookNumber]
    }

    fun resolveMyBibleBookNumber(canonicalBookId: Int): Int? {
        return canonicalToMyBibleBookNumberMap[canonicalBookId]
    }

    fun getLocalizedBookName(bookNumber: Int, isEnglish: Boolean): String {
        val book = getBookByNumber(bookNumber) ?: return "Libro $bookNumber"
        return if (isEnglish) book.englishName else book.name
    }

    fun getLocalizedBookName(bookName: String, isEnglish: Boolean, bookNumber: Int = 0): String {
        val canonicalId = resolveCanonicalBookId(bookName, bookNumber)
        val book = canonicalId?.let { getBookByNumber(it) } ?: getBookByName(bookName) ?: return bookName
        return if (isEnglish) book.englishName else book.name
    }

    fun normalizeVersionId(version: String): String {
        val clean = version.trim().replace(Regex("\\s+"), "").uppercase()
        return if (clean == "RV1960") "RVR1960" else clean
    }

    fun cleanVerseTextForFavorites(rawText: String): String {
        return rawText
            .replace(Regex("<S>[^<]*</S>", RegexOption.IGNORE_CASE), "")
            .replace(Regex("<f>[^<]*</f>", RegexOption.IGNORE_CASE), "")
            .replace(Regex("<[^>]*>"), "")
            .replace(Regex("(?<=\\p{L})\\d+"), "")
            .replace(Regex("\\{\\d+\\}"), "")
            .replace(Regex("\\[\\d+\\]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun buildFavoriteKey(
        bookName: String,
        chapter: Int,
        verse: Int,
        version: String,
        text: String = "",
        bookNumber: Int = 0
    ): String {
        val bookKey = resolveCanonicalBookId(bookName, bookNumber)?.let { "B$it" }
            ?: normalizeToken(bookName)
        val versionKey = normalizeVersionId(version).ifBlank {
            normalizeToken(cleanVerseTextForFavorites(text))
        }
        return "$bookKey|$chapter|$verse|$versionKey"
    }

    fun formatVerseRanges(sortedVerses: List<Int>): String {
        if (sortedVerses.isEmpty()) return ""
        val ranges = mutableListOf<String>()
        var start = sortedVerses.first()
        var end = start
        for (i in 1 until sortedVerses.size) {
            val current = sortedVerses[i]
            if (current == end + 1) {
                end = current
            } else {
                ranges.add(if (start == end) "$start" else "$start-$end")
                start = current
                end = current
            }
        }
        ranges.add(if (start == end) "$start" else "$start-$end")
        return ranges.joinToString(", ")
    }

    fun buildFormattedPassageText(
        verses: List<BibleVerse>,
        fallbackBookName: String = "",
        fallbackChapter: Int = 1,
        versionTag: String = ""
    ): String {
        if (verses.isEmpty()) return ""
        val sorted = verses.sortedBy { it.verse }
        val first = sorted.first()
        val rawBook = first.bookName.ifBlank { fallbackBookName }
        val chapterNum = if (first.chapter > 0) first.chapter else fallbackChapter
        val effectiveVersion = normalizeVersionId(
            first.version.ifBlank { versionTag }
        ).ifBlank { versionTag }
        val isEnglish = isEnglishVersion(effectiveVersion)
        val localizedBook = getLocalizedBookName(
            bookName = rawBook,
            isEnglish = isEnglish,
            bookNumber = first.bookNumber
        )
        val rangeLabel = formatVerseRanges(sorted.map { it.verse }.distinct())
        val header = buildString {
            append(localizedBook)
            if (chapterNum > 0 && rangeLabel.isNotBlank()) {
                append(" ")
                append(chapterNum)
                append(":")
                append(rangeLabel)
            }
            if (effectiveVersion.isNotBlank()) {
                append(" (")
                append(effectiveVersion)
                append(")")
            }
        }

        return if (sorted.size == 1) {
            val cleanText = cleanVerseTextForFavorites(first.text)
            "$header\n$cleanText".trim()
        } else {
            val body = sorted.joinToString("\n") { verse ->
                val cleanText = cleanVerseTextForFavorites(verse.text)
                "${verse.verse} $cleanText"
            }
            "$header\n$body".trim()
        }
    }

    fun isEnglishVersion(version: String): Boolean {
        val v = version.uppercase().trim()
        return v.contains("KJV") || v.contains("AMP") || v.contains("ESV") ||
                v.contains("NIV") || v.contains("NKJV") || v.contains("NLT") ||
                v.contains("TPT") || v.contains("T4T") || v.contains("NASB") ||
                v.contains("CSB") || v.contains("NET") || v.contains("ASV") ||
                v.contains("WEB") || v.contains("YLT") || v.contains("DARBY") ||
                v.contains("MSG") || v.contains("ENGLISH") || v.contains("INGLES") ||
                v.contains("INGLÉS")
    }
}

/**
 * Catálogo 100 % dinámico construido desde la tabla `verses` del SQLite de una versión bíblica.
 * Garantiza que biblias parciales (ej. TPT, solo NT o solo AT) muestren y naveguen
 * exclusivamente por los libros y capítulos que realmente contienen versículos.
 */
data class ModuleBibleCatalog(
    val version: String,
    val availableBooks: List<BookInfo>,
    val chaptersByBookNumber: Map<Int, List<Int>>
) {
    val oldTestamentBooks: List<BookInfo>
        get() = availableBooks.filter { it.testament == "AT" }

    val newTestamentBooks: List<BookInfo>
        get() = availableBooks.filter { it.testament == "NT" }

    val hasOldTestament: Boolean
        get() = availableBooks.any { it.testament == "AT" }

    val hasNewTestament: Boolean
        get() = availableBooks.any { it.testament == "NT" }

    fun getAvailableChapters(book: BookInfo): List<Int> =
        chaptersByBookNumber[book.number].orEmpty()

    fun getAvailableChapters(bookName: String): List<Int> {
        val canonicalId = BibleBooksData.resolveCanonicalBookId(bookName) ?: return emptyList()
        return chaptersByBookNumber[canonicalId].orEmpty()
    }

    fun hasPassage(bookName: String, chapter: Int): Boolean {
        return chapter in getAvailableChapters(bookName)
    }

    fun findAvailableBook(bookName: String): BookInfo? {
        val canonicalId = BibleBooksData.resolveCanonicalBookId(bookName) ?: return null
        return availableBooks.firstOrNull { it.number == canonicalId }
    }

    /**
     * Retorna el par `(BookInfo, chapter)` anterior disponible dentro del módulo,
     * cruzando únicamente hacia libros y capítulos que realmente existen en el SQLite.
     */
    fun getPreviousPassage(bookName: String, chapter: Int): Pair<BookInfo, Int>? {
        val canonicalId = BibleBooksData.resolveCanonicalBookId(bookName) ?: return null
        val bookIndex = availableBooks.indexOfFirst { it.number == canonicalId }
        if (bookIndex < 0) return null

        val currentBook = availableBooks[bookIndex]
        val chapters = chaptersByBookNumber[canonicalId].orEmpty()
        val prevInSameBook = chapters.lastOrNull { it < chapter }
        if (prevInSameBook != null) {
            return currentBook to prevInSameBook
        }

        for (i in (bookIndex - 1) downTo 0) {
            val prevBook = availableBooks[i]
            val prevChapters = chaptersByBookNumber[prevBook.number].orEmpty()
            val lastChapter = prevChapters.lastOrNull()
            if (lastChapter != null) {
                return prevBook to lastChapter
            }
        }
        return null
    }

    /**
     * Retorna el par `(BookInfo, chapter)` siguiente disponible dentro del módulo,
     * saltando libros o capítulos ausentes en módulos parciales.
     */
    fun getNextPassage(bookName: String, chapter: Int): Pair<BookInfo, Int>? {
        val canonicalId = BibleBooksData.resolveCanonicalBookId(bookName) ?: return null
        val bookIndex = availableBooks.indexOfFirst { it.number == canonicalId }
        if (bookIndex < 0) return null

        val currentBook = availableBooks[bookIndex]
        val chapters = chaptersByBookNumber[canonicalId].orEmpty()
        val nextInSameBook = chapters.firstOrNull { it > chapter }
        if (nextInSameBook != null) {
            return currentBook to nextInSameBook
        }

        for (i in (bookIndex + 1) until availableBooks.size) {
            val nextBook = availableBooks[i]
            val nextChapters = chaptersByBookNumber[nextBook.number].orEmpty()
            val firstChapter = nextChapters.firstOrNull()
            if (firstChapter != null) {
                return nextBook to firstChapter
            }
        }
        return null
    }

    companion object {
        fun fromChaptersMap(
            version: String,
            chaptersByCanonicalBook: Map<Int, List<Int>>
        ): ModuleBibleCatalog {
            val normalizedMap = linkedMapOf<Int, List<Int>>()
            val books = mutableListOf<BookInfo>()

            for (canonicalBook in BibleBooksData.books) {
                val chapters = chaptersByCanonicalBook[canonicalBook.number]
                    ?.filter { it > 0 }
                    ?.distinct()
                    ?.sorted()
                    .orEmpty()
                if (chapters.isNotEmpty()) {
                    normalizedMap[canonicalBook.number] = chapters
                    books.add(canonicalBook.copy(chapterCount = chapters.size))
                }
            }

            return ModuleBibleCatalog(
                version = version,
                availableBooks = books,
                chaptersByBookNumber = normalizedMap
            )
        }
    }
}

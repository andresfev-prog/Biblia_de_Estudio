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

    fun getBookByName(name: String): BookInfo? {
        val clean = name.trim()
        return books.firstOrNull {
            it.name.equals(clean, ignoreCase = true) ||
            it.englishName.equals(clean, ignoreCase = true) ||
            it.abbreviation.equals(clean, ignoreCase = true)
        }
    }

    fun getLocalizedBookName(bookName: String, isEnglish: Boolean): String {
        val book = getBookByName(bookName) ?: return bookName
        return if (isEnglish) book.englishName else book.name
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

package com.example.data.model

data class BookInfo(
    val number: Int,
    val name: String,
    val abbreviation: String,
    val testament: String, // "AT" or "NT"
    val category: String, // "Pentateuco", "Históricos", "Poéticos", "Profetas Mayores", "Profetas Menores", "Evangelios", "Histórico", "Epístolas Paulinas", "Epístolas Generales", "Profecía"
    val chapterCount: Int
)

object BibleBooksData {
    val books: List<BookInfo> = listOf(
        // Antiguo Testamento
        // Pentateuco
        BookInfo(1, "Génesis", "Gén", "AT", "Pentateuco", 50),
        BookInfo(2, "Éxodo", "Éx", "AT", "Pentateuco", 40),
        BookInfo(3, "Levítico", "Lev", "AT", "Pentateuco", 27),
        BookInfo(4, "Números", "Núm", "AT", "Pentateuco", 36),
        BookInfo(5, "Deuteronomio", "Deut", "AT", "Pentateuco", 34),
        // Históricos
        BookInfo(6, "Josué", "Jos", "AT", "Históricos", 24),
        BookInfo(7, "Jueces", "Jue", "AT", "Históricos", 21),
        BookInfo(8, "Rut", "Rut", "AT", "Históricos", 4),
        BookInfo(9, "1 Samuel", "1 Sam", "AT", "Históricos", 31),
        BookInfo(10, "2 Samuel", "2 Sam", "AT", "Históricos", 24),
        BookInfo(11, "1 Reyes", "1 Rey", "AT", "Históricos", 22),
        BookInfo(12, "2 Reyes", "2 Rey", "AT", "Históricos", 25),
        BookInfo(13, "1 Crónicas", "1 Crón", "AT", "Históricos", 29),
        BookInfo(14, "2 Crónicas", "2 Crón", "AT", "Históricos", 36),
        BookInfo(15, "Esdras", "Esd", "AT", "Históricos", 10),
        BookInfo(16, "Nehemías", "Neh", "AT", "Históricos", 13),
        BookInfo(17, "Ester", "Est", "AT", "Históricos", 10),
        // Poéticos
        BookInfo(18, "Job", "Job", "AT", "Poéticos", 42),
        BookInfo(19, "Salmos", "Sal", "AT", "Poéticos", 150),
        BookInfo(20, "Proverbios", "Prov", "AT", "Poéticos", 31),
        BookInfo(21, "Eclesiastés", "Ecl", "AT", "Poéticos", 12),
        BookInfo(22, "Cantares", "Cant", "AT", "Poéticos", 8),
        // Profetas Mayores
        BookInfo(23, "Isaías", "Is", "AT", "Profetas Mayores", 66),
        BookInfo(24, "Jeremías", "Jer", "AT", "Profetas Mayores", 52),
        BookInfo(25, "Lamentaciones", "Lam", "AT", "Profetas Mayores", 5),
        BookInfo(26, "Ezequiel", "Ez", "AT", "Profetas Mayores", 48),
        BookInfo(27, "Daniel", "Dan", "AT", "Profetas Mayores", 12),
        // Profetas Menores
        BookInfo(28, "Oseas", "Os", "AT", "Profetas Menores", 14),
        BookInfo(29, "Joel", "Joel", "AT", "Profetas Menores", 3),
        BookInfo(30, "Amós", "Amós", "AT", "Profetas Menores", 9),
        BookInfo(31, "Abdías", "Abd", "AT", "Profetas Menores", 1),
        BookInfo(32, "Jonás", "Jon", "AT", "Profetas Menores", 4),
        BookInfo(33, "Miqueas", "Miq", "AT", "Profetas Menores", 7),
        BookInfo(34, "Nahúm", "Nah", "AT", "Profetas Menores", 3),
        BookInfo(35, "Habacuc", "Hab", "AT", "Profetas Menores", 3),
        BookInfo(36, "Sofonías", "Sof", "AT", "Profetas Menores", 3),
        BookInfo(37, "Hageo", "Hag", "AT", "Profetas Menores", 2),
        BookInfo(38, "Zacarías", "Zac", "AT", "Profetas Menores", 14),
        BookInfo(39, "Malaquías", "Mal", "AT", "Profetas Menores", 4),

        // Nuevo Testamento
        // Evangelios
        BookInfo(40, "Mateo", "Mt", "NT", "Evangelios", 28),
        BookInfo(41, "Marcos", "Mc", "NT", "Evangelios", 16),
        BookInfo(42, "Lucas", "Lc", "NT", "Evangelios", 24),
        BookInfo(43, "Juan", "Jn", "NT", "Evangelios", 21),
        // Histórico
        BookInfo(44, "Hechos", "Hch", "NT", "Histórico", 28),
        // Epístolas Paulinas
        BookInfo(45, "Romanos", "Rom", "NT", "Epístolas Paulinas", 16),
        BookInfo(46, "1 Corintios", "1 Cor", "NT", "Epístolas Paulinas", 16),
        BookInfo(47, "2 Corintios", "2 Cor", "NT", "Epístolas Paulinas", 13),
        BookInfo(48, "Gálatas", "Gál", "NT", "Epístolas Paulinas", 6),
        BookInfo(49, "Efesios", "Ef", "NT", "Epístolas Paulinas", 6),
        BookInfo(50, "Filipenses", "Flp", "NT", "Epístolas Paulinas", 4),
        BookInfo(51, "Colosenses", "Col", "NT", "Epístolas Paulinas", 4),
        BookInfo(52, "1 Tesalonicenses", "1 Tes", "NT", "Epístolas Paulinas", 5),
        BookInfo(53, "2 Tesalonicenses", "2 Tes", "NT", "Epístolas Paulinas", 3),
        BookInfo(54, "1 Timoteo", "1 Tim", "NT", "Epístolas Paulinas", 6),
        BookInfo(55, "2 Timoteo", "2 Tim", "NT", "Epístolas Paulinas", 4),
        BookInfo(56, "Tito", "Tit", "NT", "Epístolas Paulinas", 3),
        BookInfo(57, "Filemón", "Flm", "NT", "Epístolas Paulinas", 1),
        // Epístolas Generales
        BookInfo(58, "Hebreos", "Heb", "NT", "Epístolas Generales", 13),
        BookInfo(59, "Santiago", "Stg", "NT", "Epístolas Generales", 5),
        BookInfo(60, "1 Pedro", "1 Pe", "NT", "Epístolas Generales", 5),
        BookInfo(61, "2 Pedro", "2 Pe", "NT", "Epístolas Generales", 3),
        BookInfo(62, "1 Juan", "1 Jn", "NT", "Epístolas Generales", 5),
        BookInfo(63, "2 Juan", "2 Jn", "NT", "Epístolas Generales", 1),
        BookInfo(64, "3 Juan", "3 Jn", "NT", "Epístolas Generales", 1),
        BookInfo(65, "Judas", "Jud", "NT", "Epístolas Generales", 1),
        // Profecía
        BookInfo(66, "Apocalipsis", "Apoc", "NT", "Profecía", 22)
    )

    fun getBookByName(name: String): BookInfo? {
        return books.firstOrNull { it.name.equals(name, ignoreCase = true) }
    }
}

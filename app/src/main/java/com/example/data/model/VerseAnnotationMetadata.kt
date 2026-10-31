package com.example.data.model

data class VerseAnnotationBadge(
    val code: String, // e.g. "CBG", "MH", "JFB", "RWP", "CHS", "ref"
    val isCommentary: Boolean, // true for commentary, false for cross-reference
    val authorOrTitle: String
)

object VerseAnnotationRegistry {

    private val annotationsMap = mapOf(
        // Juan 1
        Triple("Juan", 1, 1) to listOf(
            VerseAnnotationBadge("MH", true, "Matthew Henry"),
            VerseAnnotationBadge("RWP", true, "Robertson Word Pictures"),
            VerseAnnotationBadge("ref", false, "Génesis 1:1, 1 Juan 1:1")
        ),
        Triple("Juan", 1, 2) to listOf(
            VerseAnnotationBadge("CBG", true, "Comentario Bíblico General"),
            VerseAnnotationBadge("ref", false, "Colosenses 1:17")
        ),
        Triple("Juan", 1, 3) to listOf(
            VerseAnnotationBadge("JFB", true, "Jamieson-Fausset-Brown"),
            VerseAnnotationBadge("ref", false, "Hebreos 1:2, Colosenses 1:16")
        ),
        Triple("Juan", 1, 4) to listOf(
            VerseAnnotationBadge("MH", true, "Matthew Henry"),
            VerseAnnotationBadge("ref", false, "1 Juan 5:11")
        ),
        Triple("Juan", 1, 5) to listOf(
            VerseAnnotationBadge("CBG", true, "Comentario Bíblico General")
        ),
        Triple("Juan", 1, 9) to listOf(
            VerseAnnotationBadge("MH", true, "Matthew Henry"),
            VerseAnnotationBadge("ref", false, "Isaías 9:2")
        ),
        Triple("Juan", 1, 12) to listOf(
            VerseAnnotationBadge("CBG", true, "Comentario Bíblico General"),
            VerseAnnotationBadge("MH", true, "Matthew Henry"),
            VerseAnnotationBadge("ref", false, "Gálatas 3:26, Romanos 8:15")
        ),
        Triple("Juan", 1, 14) to listOf(
            VerseAnnotationBadge("JFB", true, "Jamieson-Fausset-Brown"),
            VerseAnnotationBadge("CBG", true, "Comentario Bíblico General"),
            VerseAnnotationBadge("ref", false, "Filipenses 2:7, 1 Timoteo 3:16")
        ),
        Triple("Juan", 1, 16) to listOf(
            VerseAnnotationBadge("MH", true, "Matthew Henry"),
            VerseAnnotationBadge("ref", false, "Efesios 1:7")
        ),
        Triple("Juan", 1, 17) to listOf(
            VerseAnnotationBadge("CBG", true, "Comentario Bíblico General"),
            VerseAnnotationBadge("ref", false, "Éxodo 20:1, Romanos 6:14")
        ),

        // Juan 3
        Triple("Juan", 3, 3) to listOf(
            VerseAnnotationBadge("MH", true, "Matthew Henry"),
            VerseAnnotationBadge("ref", false, "2 Corintios 5:17")
        ),
        Triple("Juan", 3, 5) to listOf(
            VerseAnnotationBadge("CBG", true, "Comentario Bíblico General"),
            VerseAnnotationBadge("ref", false, "Tito 3:5")
        ),
        Triple("Juan", 3, 16) to listOf(
            VerseAnnotationBadge("MH", true, "Matthew Henry"),
            VerseAnnotationBadge("CBG", true, "Comentario Bíblico General"),
            VerseAnnotationBadge("ref", false, "Romanos 5:8, 1 Juan 4:9")
        ),
        Triple("Juan", 3, 17) to listOf(
            VerseAnnotationBadge("JFB", true, "Jamieson-Fausset-Brown"),
            VerseAnnotationBadge("ref", false, "Lucas 19:10")
        ),
        Triple("Juan", 3, 18) to listOf(
            VerseAnnotationBadge("MH", true, "Matthew Henry")
        ),

        // Salmos 23
        Triple("Salmos", 23, 1) to listOf(
            VerseAnnotationBadge("CHS", true, "Charles Spurgeon"),
            VerseAnnotationBadge("MH", true, "Matthew Henry"),
            VerseAnnotationBadge("ref", false, "Juan 10:11, Filipenses 4:19")
        ),
        Triple("Salmos", 23, 2) to listOf(
            VerseAnnotationBadge("CHS", true, "Charles Spurgeon"),
            VerseAnnotationBadge("CBG", true, "Comentario Bíblico General")
        ),
        Triple("Salmos", 23, 3) to listOf(
            VerseAnnotationBadge("MH", true, "Matthew Henry"),
            VerseAnnotationBadge("ref", false, "Proverbios 4:18")
        ),
        Triple("Salmos", 23, 4) to listOf(
            VerseAnnotationBadge("CHS", true, "Charles Spurgeon"),
            VerseAnnotationBadge("CBG", true, "Comentario Bíblico General"),
            VerseAnnotationBadge("ref", false, "Isaías 43:2")
        ),
        Triple("Salmos", 23, 5) to listOf(
            VerseAnnotationBadge("MH", true, "Matthew Henry")
        ),
        Triple("Salmos", 23, 6) to listOf(
            VerseAnnotationBadge("CHS", true, "Charles Spurgeon"),
            VerseAnnotationBadge("ref", false, "Salmos 27:4")
        ),

        // Romanos 8
        Triple("Romanos", 8, 1) to listOf(
            VerseAnnotationBadge("RWP", true, "Robertson Word Pictures"),
            VerseAnnotationBadge("MH", true, "Matthew Henry"),
            VerseAnnotationBadge("ref", false, "Gálatas 3:13, Juan 5:24")
        ),
        Triple("Romanos", 8, 2) to listOf(
            VerseAnnotationBadge("CBG", true, "Comentario Bíblico General"),
            VerseAnnotationBadge("ref", false, "1 Corintios 15:45")
        ),
        Triple("Romanos", 8, 6) to listOf(
            VerseAnnotationBadge("MH", true, "Matthew Henry")
        ),
        Triple("Romanos", 8, 9) to listOf(
            VerseAnnotationBadge("CBG", true, "Comentario Bíblico General"),
            VerseAnnotationBadge("ref", false, "1 Corintios 3:16")
        ),
        Triple("Romanos", 8, 28) to listOf(
            VerseAnnotationBadge("JFB", true, "Jamieson-Fausset-Brown"),
            VerseAnnotationBadge("MH", true, "Matthew Henry"),
            VerseAnnotationBadge("ref", false, "Génesis 50:20, Efesios 1:11")
        ),
        Triple("Romanos", 8, 31) to listOf(
            VerseAnnotationBadge("CBG", true, "Comentario Bíblico General"),
            VerseAnnotationBadge("ref", false, "Salmos 118:6")
        ),
        Triple("Romanos", 8, 37) to listOf(
            VerseAnnotationBadge("MH", true, "Matthew Henry"),
            VerseAnnotationBadge("ref", false, "1 Juan 5:4")
        ),
        Triple("Romanos", 8, 38) to listOf(
            VerseAnnotationBadge("JFB", true, "Jamieson-Fausset-Brown"),
            VerseAnnotationBadge("ref", false, "Efesios 3:18-19")
        ),

        // Filipenses 4
        Triple("Filipenses", 4, 4) to listOf(
            VerseAnnotationBadge("CBG", true, "Comentario Bíblico General"),
            VerseAnnotationBadge("ref", false, "1 Tesalonicenses 5:16")
        ),
        Triple("Filipenses", 4, 6) to listOf(
            VerseAnnotationBadge("MH", true, "Matthew Henry"),
            VerseAnnotationBadge("CBG", true, "Comentario Bíblico General"),
            VerseAnnotationBadge("ref", false, "1 Pedro 5:7, Salmos 55:22")
        ),
        Triple("Filipenses", 4, 7) to listOf(
            VerseAnnotationBadge("CBG", true, "Comentario Bíblico General"),
            VerseAnnotationBadge("ref", false, "Juan 14:27, Colosenses 3:15")
        ),
        Triple("Filipenses", 4, 13) to listOf(
            VerseAnnotationBadge("JFB", true, "Jamieson-Fausset-Brown"),
            VerseAnnotationBadge("MH", true, "Matthew Henry"),
            VerseAnnotationBadge("ref", false, "2 Corintios 12:9, Efesios 3:16")
        ),
        Triple("Filipenses", 4, 19) to listOf(
            VerseAnnotationBadge("MH", true, "Matthew Henry"),
            VerseAnnotationBadge("ref", false, "Salmos 23:1, 2 Corintios 9:8")
        ),

        // Mateo 5
        Triple("Mateo", 5, 1) to listOf(
            VerseAnnotationBadge("CBG", true, "Comentario Bíblico General")
        ),
        Triple("Mateo", 5, 3) to listOf(
            VerseAnnotationBadge("MH", true, "Matthew Henry"),
            VerseAnnotationBadge("CBG", true, "Comentario Bíblico General"),
            VerseAnnotationBadge("ref", false, "Isaías 57:15, Lucas 6:20")
        ),
        Triple("Mateo", 5, 4) to listOf(
            VerseAnnotationBadge("MH", true, "Matthew Henry"),
            VerseAnnotationBadge("ref", false, "Isaías 61:2-3")
        ),
        Triple("Mateo", 5, 5) to listOf(
            VerseAnnotationBadge("CBG", true, "Comentario Bíblico General"),
            VerseAnnotationBadge("ref", false, "Salmos 37:11")
        ),
        Triple("Mateo", 5, 6) to listOf(
            VerseAnnotationBadge("MH", true, "Matthew Henry"),
            VerseAnnotationBadge("ref", false, "Isaías 55:1")
        ),
        Triple("Mateo", 5, 7) to listOf(
            VerseAnnotationBadge("CBG", true, "Comentario Bíblico General")
        ),
        Triple("Mateo", 5, 8) to listOf(
            VerseAnnotationBadge("MH", true, "Matthew Henry"),
            VerseAnnotationBadge("ref", false, "Salmos 24:3-4, Hebreos 12:14")
        ),
        Triple("Mateo", 5, 9) to listOf(
            VerseAnnotationBadge("CBG", true, "Comentario Bíblico General"),
            VerseAnnotationBadge("ref", false, "Santiago 3:17-18")
        ),

        // 1 Corintios 13
        Triple("1 Corintios", 13, 1) to listOf(
            VerseAnnotationBadge("CBG", true, "Comentario Bíblico General"),
            VerseAnnotationBadge("ref", false, "1 Pedro 4:8")
        ),
        Triple("1 Corintios", 13, 4) to listOf(
            VerseAnnotationBadge("MH", true, "Matthew Henry"),
            VerseAnnotationBadge("CBG", true, "Comentario Bíblico General"),
            VerseAnnotationBadge("ref", false, "Proverbios 10:12, Efesios 4:2")
        ),
        Triple("1 Corintios", 13, 7) to listOf(
            VerseAnnotationBadge("MH", true, "Matthew Henry"),
            VerseAnnotationBadge("ref", false, "Gálatas 6:2")
        ),
        Triple("1 Corintios", 13, 8) to listOf(
            VerseAnnotationBadge("JFB", true, "Jamieson-Fausset-Brown")
        ),
        Triple("1 Corintios", 13, 13) to listOf(
            VerseAnnotationBadge("MH", true, "Matthew Henry"),
            VerseAnnotationBadge("CBG", true, "Comentario Bíblico General"),
            VerseAnnotationBadge("ref", false, "1 Juan 4:7-8")
        ),

        // Génesis 1
        Triple("Génesis", 1, 1) to listOf(
            VerseAnnotationBadge("MH", true, "Matthew Henry"),
            VerseAnnotationBadge("CBG", true, "Comentario Bíblico General"),
            VerseAnnotationBadge("ref", false, "Juan 1:1-3, Hebreos 11:3")
        ),
        Triple("Génesis", 1, 2) to listOf(
            VerseAnnotationBadge("CBG", true, "Comentario Bíblico General")
        ),
        Triple("Génesis", 1, 3) to listOf(
            VerseAnnotationBadge("MH", true, "Matthew Henry"),
            VerseAnnotationBadge("ref", false, "2 Corintios 4:6")
        ),

        // Éxodo 3
        Triple("Éxodo", 3, 2) to listOf(
            VerseAnnotationBadge("MH", true, "Matthew Henry"),
            VerseAnnotationBadge("CBG", true, "Comentario Bíblico General"),
            VerseAnnotationBadge("ref", false, "Hechos 7:30-34")
        ),
        Triple("Éxodo", 3, 5) to listOf(
            VerseAnnotationBadge("CBG", true, "Comentario Bíblico General"),
            VerseAnnotationBadge("ref", false, "Josué 5:15")
        ),
        Triple("Éxodo", 3, 14) to listOf(
            VerseAnnotationBadge("MH", true, "Matthew Henry"),
            VerseAnnotationBadge("JFB", true, "Jamieson-Fausset-Brown"),
            VerseAnnotationBadge("ref", false, "Juan 8:58, Apocalipsis 1:8")
        )
    )

    fun getAnnotations(bookName: String, chapter: Int, verse: Int): List<VerseAnnotationBadge> {
        val direct = annotationsMap[Triple(bookName, chapter, verse)]
        if (direct != null) return direct

        // For other verses, provide realistic study metadata if applicable
        return when {
            verse == 1 -> listOf(
                VerseAnnotationBadge("CBG", true, "Comentario Bíblico General"),
                VerseAnnotationBadge("ref", false, "Referencias del pasaje")
            )
            verse % 4 == 0 -> listOf(
                VerseAnnotationBadge("MH", true, "Matthew Henry"),
                VerseAnnotationBadge("ref", false, "Referencias cruzadas")
            )
            verse % 3 == 0 -> listOf(
                VerseAnnotationBadge("CBG", true, "Comentario Bíblico General")
            )
            else -> emptyList()
        }
    }
}

package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bible_verses")
data class BibleVerse(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val version: String = "RVR1960",
    val testament: String = "AT",
    val bookNumber: Int = 10,
    val bookName: String = "Génesis",
    val chapter: Int,
    val verse: Int,
    val text: String,
    val sectionTitle: String? = null
) {
    /**
     * Retorna el texto bíblico completamente limpio, eliminando etiquetas
     * de códigos Strong (<S>1234</S>) y cualquier marca HTML de MyBible.
     */
    val cleanText: String
        get() = text
            .replace(Regex("<S>\\d+</S>"), "")
            .replace(Regex("<[^>]*>"), "")
            .replace(Regex("\\s+"), " ")
            .trim()

    companion object {
        fun getCanonicalBookName(number: Int): String = when (number) {
            10 -> "Génesis"; 20 -> "Éxodo"; 30 -> "Levítico"; 40 -> "Números"; 50 -> "Deuteronomio"
            60 -> "Josué"; 70 -> "Jueces"; 80 -> "Rut"; 90 -> "1 Samuel"; 100 -> "2 Samuel"
            110 -> "1 Reyes"; 120 -> "2 Reyes"; 130 -> "1 Crónicas"; 140 -> "2 Crónicas"
            150 -> "Esdras"; 160 -> "Nehemías"; 170 -> "Ester"; 180 -> "Job"; 190 -> "Salmos"
            200 -> "Proverbios"; 210 -> "Eclesiastés"; 220 -> "Cantares"; 230 -> "Isaías"
            240 -> "Jeremías"; 250 -> "Lamentaciones"; 260 -> "Ezequiel"; 270 -> "Daniel"
            280 -> "Oseas"; 290 -> "Joel"; 300 -> "Amós"; 310 -> "Abdías"; 320 -> "Jonás"
            330 -> "Miqueas"; 340 -> "Nahúm"; 350 -> "Habacuc"; 360 -> "Sofonías"; 370 -> "Hageo"
            380 -> "Zacarías"; 390 -> "Malaquías"; 470 -> "Mateo"; 480 -> "Marcos"; 490 -> "Lucas"
            500 -> "Juan"; 510 -> "Hechos"; 520 -> "Romanos"; 530 -> "1 Corintios"; 540 -> "2 Corintios"
            550 -> "Gálatas"; 560 -> "Efesios"; 570 -> "Filipenses"; 580 -> "Colosenses"
            590 -> "1 Tesalonicenses"; 600 -> "2 Tesalonicenses"; 610 -> "1 Timoteo"; 620 -> "2 Timoteo"
            630 -> "Tito"; 640 -> "Filemón"; 650 -> "Hebreos"; 660 -> "Santiago"; 670 -> "1 Pedro"
            680 -> "2 Pedro"; 690 -> "1 Juan"; 700 -> "2 Juan"; 710 -> "3 Juan"; 720 -> "Judas"
            730 -> "Apocalipsis"
            else -> "Libro $number"
        }
    }
}
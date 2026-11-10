package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dictionary_entries")
data class DictionaryEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val term: String = "",
    val displayTerm: String = "",
    val definition: String = "",
    val originalWord: String = "",
    val strongCode: String = "",
    val pronunciation: String = "",
    val theologicalNotes: String = ""
)

/**
 * Representa una coincidencia de término dentro de un módulo de diccionario específico.
 */
data class DictionaryMatch(
    val module: BibleModule,
    val entry: DictionaryEntry
)
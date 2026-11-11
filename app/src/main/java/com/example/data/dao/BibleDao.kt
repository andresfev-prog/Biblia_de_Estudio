package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.BibleModule
import com.example.data.model.BibleVerse
import com.example.data.model.Commentary
import com.example.data.model.DictionaryEntry
import com.example.data.model.FavoriteVerse
import com.example.data.model.ReadingProgress
import com.example.data.model.SearchHistoryItem
import com.example.data.model.UserPreference
import kotlinx.coroutines.flow.Flow

@Dao
interface BibleDao {

    // --- Verses ---
    @Query("SELECT * FROM bible_verses WHERE version = :version AND bookName = :bookName AND chapter = :chapter ORDER BY verse ASC")
    fun getVerses(version: String, bookName: String, chapter: Int): Flow<List<BibleVerse>>

    @Query("SELECT * FROM bible_verses WHERE version = :version AND bookName = :bookName AND chapter = :chapter AND verse = :verse LIMIT 1")
    fun getVerse(version: String, bookName: String, chapter: Int, verse: Int): Flow<BibleVerse?>

    @Query("SELECT * FROM bible_verses WHERE text LIKE '%' || :query || '%' OR bookName LIKE '%' || :query || '%' LIMIT 100")
    fun searchVerses(query: String): Flow<List<BibleVerse>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerses(verses: List<BibleVerse>)

    @Query("SELECT COUNT(*) FROM bible_verses")
    suspend fun getVerseCount(): Int

    // --- Commentaries ---
    @Query("SELECT * FROM commentaries WHERE bookName = :bookName AND chapter = :chapter AND verse = :verse")
    fun getCommentariesForVerse(bookName: String, chapter: Int, verse: Int): Flow<List<Commentary>>

    @Query("SELECT * FROM commentaries WHERE bookName = :bookName AND chapter = :chapter")
    fun getCommentariesForChapter(bookName: String, chapter: Int): Flow<List<Commentary>>

    @Query("SELECT * FROM commentaries WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%' LIMIT 50")
    fun searchCommentaries(query: String): Flow<List<Commentary>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommentaries(commentaries: List<Commentary>)

    // --- Dictionary ---
    @Query("SELECT * FROM dictionary_entries WHERE term = :term OR displayTerm LIKE :term LIMIT 1")
    fun getDictionaryEntry(term: String): Flow<DictionaryEntry?>

    @Query("SELECT * FROM dictionary_entries WHERE term LIKE '%' || :query || '%' OR displayTerm LIKE '%' || :query || '%' OR definition LIKE '%' || :query || '%' OR originalWord LIKE '%' || :query || '%' LIMIT 50")
    fun searchDictionary(query: String): Flow<List<DictionaryEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDictionaryEntries(entries: List<DictionaryEntry>)

    // --- Favorites ---
    @Query("SELECT * FROM favorite_verses ORDER BY timestamp DESC")
    fun getAllFavorites(): Flow<List<FavoriteVerse>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_verses WHERE bookName = :bookName AND chapter = :chapter AND verse = :verse)")
    fun isFavorite(bookName: String, chapter: Int, verse: Int): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteVerse)

    @Query("DELETE FROM favorite_verses WHERE bookName = :bookName AND chapter = :chapter AND verse = :verse")
    suspend fun deleteFavorite(bookName: String, chapter: Int, verse: Int)

    // --- Search History ---
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT 20")
    fun getSearchHistory(): Flow<List<SearchHistoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchHistory(item: SearchHistoryItem)

    @Query("DELETE FROM search_history")
    suspend fun clearSearchHistory()

    @Query("DELETE FROM search_history WHERE id = :id")
    suspend fun deleteSearchHistoryItem(id: Long)

    // --- Reading Progress ---
    @Query("SELECT * FROM reading_progress WHERE id = 1 LIMIT 1")
    fun getReadingProgress(): Flow<ReadingProgress?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveReadingProgress(progress: ReadingProgress)

    // --- User Preferences ---
    @Query("SELECT * FROM user_preferences WHERE id = 1 LIMIT 1")
    fun getUserPreferences(): Flow<UserPreference?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserPreferences(preference: UserPreference)

    // --- Modules ---
    @Query("SELECT * FROM bible_modules ORDER BY category, title")
    fun getAllModules(): Flow<List<BibleModule>>

    @Query("SELECT * FROM bible_modules WHERE isDownloaded = 1 ORDER BY category, title")
    suspend fun getDownloadedModules(): List<BibleModule>

    @Query("UPDATE bible_modules SET isDownloaded = :isDownloaded WHERE id = :id")
    suspend fun updateModuleDownloadState(id: String, isDownloaded: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModules(modules: List<BibleModule>)

    // Consulta para limpiar la tabla antes de reinsertar los módulos oficiales
    @Query("DELETE FROM bible_modules")
    suspend fun clearAllModules()

    // --- Cross References ---
    @Query("SELECT * FROM cross_references WHERE sourceBook = :book AND sourceChapter = :chapter AND sourceVerse = :verse")
    fun getCrossReferencesForVerse(book: String, chapter: Int, verse: Int): Flow<List<com.example.data.model.CrossReference>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossReferences(refs: List<com.example.data.model.CrossReference>)

    // --- User Notes ---
    @Query("SELECT * FROM user_notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<com.example.data.model.UserNote>>

    @Query("SELECT * FROM user_notes WHERE id = :id LIMIT 1")
    fun getNoteById(id: Long): Flow<com.example.data.model.UserNote?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: com.example.data.model.UserNote): Long

    @Query("DELETE FROM user_notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)
}

package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.BibleDao
import com.example.data.model.BibleModule
import com.example.data.model.BibleVerse
import com.example.data.model.Commentary
import com.example.data.model.DictionaryEntry
import com.example.data.model.FavoriteVerse
import com.example.data.model.ReadingProgress
import com.example.data.model.SearchHistoryItem
import com.example.data.model.UserPreference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        BibleVerse::class,
        Commentary::class,
        DictionaryEntry::class,
        FavoriteVerse::class,
        SearchHistoryItem::class,
        ReadingProgress::class,
        UserPreference::class,
        BibleModule::class,
        com.example.data.model.CrossReference::class,
        com.example.data.model.UserNote::class
    ],
    version = 8,
    exportSchema = false
)
abstract class BibleDatabase : RoomDatabase() {

    abstract fun bibleDao(): BibleDao

    companion object {
        @Volatile
        private var INSTANCE: BibleDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): BibleDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BibleDatabase::class.java,
                    "bible_study.db"
                )
                    .addCallback(BibleDatabaseCallback(context.applicationContext, scope))
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class BibleDatabaseCallback(
            private val context: Context,
            private val scope: CoroutineScope
        ) : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        DatabaseInitializer.populateInitialData(context, database.bibleDao())
                    }
                }
            }
        }
    }
}
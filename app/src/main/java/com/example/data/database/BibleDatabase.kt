package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
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
    version = 10,
    exportSchema = false
)
abstract class BibleDatabase : RoomDatabase() {

    abstract fun bibleDao(): BibleDao

    companion object {
        @Volatile
        private var INSTANCE: BibleDatabase? = null

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE user_preferences ADD COLUMN selectedLightTheme TEXT NOT NULL DEFAULT 'CRISP_LIGHT'"
                )
                db.execSQL(
                    "ALTER TABLE user_preferences ADD COLUMN selectedDarkTheme TEXT NOT NULL DEFAULT 'MIDNIGHT_BLUE'"
                )
            }
        }

        internal val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Añadir cloudId estable a notas locales existentes
                db.execSQL(
                    "ALTER TABLE user_notes ADD COLUMN cloudId TEXT NOT NULL DEFAULT ''"
                )
                db.execSQL(
                    "UPDATE user_notes SET cloudId = 'legacy_' || id || '_' || timestamp WHERE cloudId = ''"
                )

                // Reconstruir user_preferences sin ubicación, usuario ni contraseña heredados
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS user_preferences_new (
                        id INTEGER NOT NULL PRIMARY KEY,
                        userName TEXT NOT NULL,
                        email TEXT NOT NULL,
                        interfaceLanguage TEXT NOT NULL,
                        themeMode TEXT NOT NULL,
                        selectedLightTheme TEXT NOT NULL DEFAULT 'CRISP_LIGHT',
                        selectedDarkTheme TEXT NOT NULL DEFAULT 'MIDNIGHT_BLUE',
                        fontFamily TEXT NOT NULL,
                        fontSizePt REAL NOT NULL,
                        isFirstLaunch INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO user_preferences_new (
                        id, userName, email, interfaceLanguage, themeMode,
                        selectedLightTheme, selectedDarkTheme, fontFamily, fontSizePt, isFirstLaunch
                    )
                    SELECT
                        id,
                        CASE
                            WHEN userName = 'Andrés Felipe' AND email = 'andresfev@gmail.com' THEN ''
                            ELSE userName
                        END,
                        '',
                        interfaceLanguage,
                        themeMode,
                        selectedLightTheme,
                        selectedDarkTheme,
                        fontFamily,
                        fontSizePt,
                        isFirstLaunch
                    FROM user_preferences
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE user_preferences")
                db.execSQL("ALTER TABLE user_preferences_new RENAME TO user_preferences")
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): BibleDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BibleDatabase::class.java,
                    "bible_study.db"
                )
                    .addMigrations(MIGRATION_8_9, MIGRATION_9_10)
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
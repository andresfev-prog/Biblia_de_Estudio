package com.example

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.BibleDatabase
import com.example.data.model.BibleBooksData
import com.example.data.model.UserNote
import com.example.data.model.UserPreference
import com.example.ui.components.TutorialStep
import com.example.ui.util.StringsLocale
import com.example.ui.viewmodel.AppDestination
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CloudProfileAndTutorialUnitTest {

    @Test
    fun `tutorial has 10 steps with step 7 on settings profile`() {
        val steps = TutorialStep.entries
        assertEquals(10, steps.size)
        steps.forEachIndexed { index, step ->
            assertEquals(index + 1, step.stepNumber)
        }

        assertEquals(TutorialStep.SETTINGS_PROFILE, steps[6])
        assertEquals(AppDestination.SETTINGS, TutorialStep.SETTINGS_PROFILE.destination)
        assertEquals(TutorialStep.SETTINGS_FAVORITES, steps[7])
        assertEquals(TutorialStep.SETTINGS_THEMES, steps[8])
        assertEquals(TutorialStep.SETTINGS_TYPOGRAPHY, steps[9])

        var current: TutorialStep? = TutorialStep.HOME_VERSE_OF_DAY
        val visited = mutableListOf<TutorialStep>()
        while (current != null) {
            visited.add(current)
            current = current.next()
        }
        assertEquals(steps, visited)
        assertNull(TutorialStep.HOME_VERSE_OF_DAY.previous())
        assertEquals(TutorialStep.SETTINGS_PROFILE, TutorialStep.SETTINGS_FAVORITES.previous())
    }

    @Test
    fun `strings locale resolves profile cloud sync and tutorial keys in ES and EN`() {
        val keys = listOf(
            "profile_backup_title",
            "profile_backup_desc",
            "profile_guest_name",
            "profile_guest_subtitle",
            "profile_local_subtitle",
            "profile_cloud_active",
            "profile_cloud_syncing",
            "profile_cloud_error",
            "profile_name_label",
            "profile_name_placeholder",
            "profile_cloud_section",
            "continue_with_google",
            "signing_in_google",
            "profile_backup_synced_detail",
            "profile_backup_syncing_detail",
            "profile_backup_error_detail",
            "sync_now",
            "syncing_now",
            "unlink_account",
            "profile_privacy_notice",
            "tutorial_step7_title",
            "tutorial_step7_desc",
            "tutorial_step8_title",
            "tutorial_step8_desc",
            "tutorial_step9_title",
            "tutorial_step9_desc",
            "tutorial_step10_title",
            "tutorial_step10_desc"
        )

        keys.forEach { key ->
            val es = StringsLocale.get(key, "Español")
            val en = StringsLocale.get(key, "English")
            assertNotEquals("Missing ES translation for $key", key, es)
            assertNotEquals("Missing EN translation for $key", key, en)
            assertTrue("Blank ES translation for $key", es.isNotBlank())
            assertTrue("Blank EN translation for $key", en.isNotBlank())
            assertNotEquals("Expected ES != EN for $key", es, en)
        }
    }

    @Test
    fun `user preference defaults to guest without legacy credentials`() {
        val pref = UserPreference()
        assertEquals("", pref.userName)
        assertEquals("", pref.email)
        assertEquals("AUTO", pref.themeMode)
        assertEquals("CRISP_LIGHT", pref.selectedLightTheme)
        assertEquals("MIDNIGHT_BLUE", pref.selectedDarkTheme)
    }

    @Test
    fun `user note generates unique cloud ids and favorite key canonicalizes cross-language`() {
        val note1 = UserNote(title = "T1", content = "C1", biblicalReference = "Jn 3:16")
        val note2 = UserNote(title = "T1", content = "C1", biblicalReference = "Jn 3:16")
        assertTrue(note1.cloudId.isNotBlank())
        assertTrue(note2.cloudId.isNotBlank())
        assertNotEquals(note1.cloudId, note2.cloudId)

        val keyEs = BibleBooksData.buildFavoriteKey("Juan", 3, 16, "RV1960")
        val keyEn = BibleBooksData.buildFavoriteKey("John", 3, 16, "RVR1960")
        assertEquals(keyEs, keyEn)
    }

    @Test
    fun `migration 9 to 10 preserves custom name clears legacy demo defaults and backfills note cloudId`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbName = "test_migration_9_10_${System.nanoTime()}.db"

        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(dbName)
            .callback(object : SupportSQLiteOpenHelper.Callback(9) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        CREATE TABLE user_preferences (
                            id INTEGER NOT NULL PRIMARY KEY,
                            userName TEXT NOT NULL,
                            email TEXT NOT NULL,
                            location TEXT NOT NULL,
                            usernameHandle TEXT NOT NULL,
                            password TEXT NOT NULL,
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
                        INSERT INTO user_preferences VALUES (
                            1, 'Andrés Felipe', 'andresfev@gmail.com', 'Cali, Colombia', 'andresfev', 'secret',
                            'Español', 'AUTO', 'CRISP_LIGHT', 'MIDNIGHT_BLUE', 'Google Sans Flex', 13.0, 0
                        )
                        """.trimIndent()
                    )
                    db.execSQL(
                        """
                        CREATE TABLE user_notes (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            title TEXT NOT NULL,
                            content TEXT NOT NULL,
                            biblicalReference TEXT NOT NULL,
                            timestamp INTEGER NOT NULL
                        )
                        """.trimIndent()
                    )
                    db.execSQL(
                        "INSERT INTO user_notes (id, title, content, biblicalReference, timestamp) VALUES (42, 'Nota', 'Contenido', 'Salmos 23:1', 1700000000)"
                    )
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
                    BibleDatabase.MIGRATION_9_10.migrate(db)
                }
            })
            .build()

        val helper = FrameworkSQLiteOpenHelperFactory().create(config)
        val dbV9 = helper.writableDatabase
        BibleDatabase.MIGRATION_9_10.migrate(dbV9)

        dbV9.query("SELECT userName, email, themeMode, selectedLightTheme, selectedDarkTheme FROM user_preferences WHERE id = 1")
            .use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals("", cursor.getString(0))
                assertEquals("", cursor.getString(1))
                assertEquals("AUTO", cursor.getString(2))
                assertEquals("CRISP_LIGHT", cursor.getString(3))
                assertEquals("MIDNIGHT_BLUE", cursor.getString(4))
            }

        dbV9.query("SELECT cloudId FROM user_notes WHERE id = 42").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("legacy_42_1700000000", cursor.getString(0))
        }

        // Verificar que nombres personalizados se conservan durante la migración
        dbV9.close()
        helper.close()
        context.deleteDatabase(dbName)
    }

    @Test
    fun `migration 9 to 10 preserves custom user name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbName = "test_migration_custom_name_${System.nanoTime()}.db"

        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(dbName)
            .callback(object : SupportSQLiteOpenHelper.Callback(9) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        CREATE TABLE user_preferences (
                            id INTEGER NOT NULL PRIMARY KEY,
                            userName TEXT NOT NULL,
                            email TEXT NOT NULL,
                            location TEXT NOT NULL,
                            usernameHandle TEXT NOT NULL,
                            password TEXT NOT NULL,
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
                        INSERT INTO user_preferences VALUES (
                            1, 'María José', '', 'Bogotá', 'majo', '',
                            'Español', 'LIGHT', 'WARM_PARCHMENT', 'MIDNIGHT_BLUE', 'Serif', 15.0, 0
                        )
                        """.trimIndent()
                    )
                    db.execSQL(
                        """
                        CREATE TABLE user_notes (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            title TEXT NOT NULL,
                            content TEXT NOT NULL,
                            biblicalReference TEXT NOT NULL,
                            timestamp INTEGER NOT NULL
                        )
                        """.trimIndent()
                    )
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
            .build()

        val helper = FrameworkSQLiteOpenHelperFactory().create(config)
        val db = helper.writableDatabase
        BibleDatabase.MIGRATION_9_10.migrate(db)

        db.query("SELECT userName, email, themeMode, selectedLightTheme FROM user_preferences WHERE id = 1").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals("María José", c.getString(0))
            assertEquals("", c.getString(1))
            assertEquals("LIGHT", c.getString(2))
            assertEquals("WARM_PARCHMENT", c.getString(3))
        }

        db.close()
        helper.close()
        context.deleteDatabase(dbName)
    }
}

package com.example

import android.app.Application
import android.content.Context
import android.os.Looper
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.BibleDatabase
import com.example.data.model.BibleBooksData
import com.example.data.model.BibleVerse
import com.example.data.model.UserNote
import com.example.data.model.UserPreference
import com.example.data.repository.BibleRepository
import com.example.data.repository.CloudSyncRepository
import com.example.ui.components.NotesBottomSheet
import com.example.ui.components.TutorialStep
import com.example.ui.theme.AppThemeIds
import com.example.ui.theme.BibleStudyTheme
import com.example.ui.theme.DarkMidnightBg
import com.example.ui.theme.PureWhite
import com.example.ui.theme.ThemePreferenceCache
import com.example.ui.theme.isDarkThemeEffective
import com.example.ui.theme.resolveAppColors
import com.example.ui.util.StringsLocale
import com.example.ui.viewmodel.AppDestination
import com.example.ui.viewmodel.BibleViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h915dp")
class CloudProfileAndTutorialUnitTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `tutorial has 10 steps with step 7 on settings profile`() {
        val steps = TutorialStep.entries
        assertEquals(10, steps.size)
        steps.forEachIndexed { index, step ->
            assertEquals(index + 1, step.stepNumber)
        }

        assertEquals(TutorialStep.HOME_CONTINUE_NOTES, steps[1])
        assertEquals(AppDestination.HOME, TutorialStep.HOME_CONTINUE_NOTES.destination)
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
    fun `strings locale resolves profile cloud sync notes and tutorial keys in ES and EN`() {
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
            "note_title_label",
            "note_reference_label",
            "note_reference_placeholder",
            "note_content_label",
            "insert_selected_verses",
            "no_additional_content",
            "tutorial_step2_title",
            "tutorial_step2_desc",
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

        assertEquals(
            "Crea tus notas con tres campos: Título, Texto bíblico o referencia (opcional) y Nota. Ningún versículo se añade automáticamente: puedes dejar el segundo campo en blanco, escribir solo la cita (ej. Éxodo 1:5) o pegar los versículos completos que hayas copiado.",
            StringsLocale.get("tutorial_step2_desc", "Español")
        )
        assertEquals(
            "Create your notes with three fields: Title, Biblical text or reference (optional), and Note. No verse is attached automatically: you can leave the second field blank, type just the reference (e.g., Exodus 1:5), or paste full verses you have copied.",
            StringsLocale.get("tutorial_step2_desc", "English")
        )
        assertEquals(
            "Vincula tu cuenta de Google para respaldar tus notas y favoritos en la nube y recuperarlos si cambias de teléfono, lo pierdes o reinstalas la app. Si prefieres no usar Google, escribe un nombre local solo para el saludo inicial, pero ten en cuenta que sin cuenta vinculada no tendrás copia de seguridad si desinstalas la app o pierdes el dispositivo.",
            StringsLocale.get("tutorial_step7_desc", "Español")
        )
        assertEquals(
            "Link your Google account to back up your notes and favorites in the cloud and restore them if you switch phones, lose your device, or reinstall the app. If you prefer not to use Google, enter a local name just for the home greeting, keeping in mind that without a linked account you will have no backup if you uninstall the app or lose your device.",
            StringsLocale.get("tutorial_step7_desc", "English")
        )
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

    private fun createTestViewModel(
        app: Application,
        db: BibleDatabase
    ): Pair<ViewModelStore, BibleViewModel> {
        val store = ViewModelStore()
        val repository = BibleRepository(app, db.bibleDao())
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return BibleViewModel(app, repository, CloudSyncRepository()) as T
            }
        }
        val vm = ViewModelProvider(store, factory)[BibleViewModel::class.java]
        return store to vm
    }

    private fun tearDownTestViewModelAndDb(
        store: ViewModelStore,
        viewModel: BibleViewModel,
        db: BibleDatabase
    ) {
        val vmJob = viewModel.viewModelScope.coroutineContext[Job]
        store.clear()
        val deadline = System.currentTimeMillis() + 3_000L
        while (vmJob != null && vmJob.children.any { !it.isCompleted } && System.currentTimeMillis() < deadline) {
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(10)
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        db.close()
    }

    @Test
    fun `saveNote persists blank biblicalReference without auto-assigning active verse`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val db = Room.inMemoryDatabaseBuilder(app, BibleDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val (store, viewModel) = createTestViewModel(app, db)
        try {
            // Mover al lector con un versículo resaltado activo para confirmar que saveNote NO lo asigna automáticamente
            viewModel.navigateToPassage("Éxodo", 1, 5)
            Shadows.shadowOf(Looper.getMainLooper()).idle()

            viewModel.saveNote(
                title = "  Promesa personal  ",
                content = "Reflexión sin cita asignada automáticamente.",
                reference = ""
            )
            val deadline1 = System.currentTimeMillis() + 5_000L
            while (System.currentTimeMillis() < deadline1) {
                Shadows.shadowOf(Looper.getMainLooper()).idle()
                val notes = runBlocking { db.bibleDao().getAllNotesOnce() }
                if (notes.isNotEmpty()) break
                Thread.sleep(25)
            }

            viewModel.saveNote(
                title = "",
                content = "Nota con espacios en referencia.",
                reference = "   "
            )
            val deadline2 = System.currentTimeMillis() + 5_000L
            while (System.currentTimeMillis() < deadline2) {
                Shadows.shadowOf(Looper.getMainLooper()).idle()
                val notes = runBlocking { db.bibleDao().getAllNotesOnce() }
                if (notes.size >= 2) break
                Thread.sleep(25)
            }

            viewModel.saveNote(
                title = "Éxodo",
                content = "Apunte con cita escrita manualmente.",
                reference = "  Éxodo 1:5  "
            )
            val deadline3 = System.currentTimeMillis() + 5_000L
            while (System.currentTimeMillis() < deadline3) {
                Shadows.shadowOf(Looper.getMainLooper()).idle()
                val notes = runBlocking { db.bibleDao().getAllNotesOnce() }
                if (notes.size >= 3) break
                Thread.sleep(25)
            }

            val saved = runBlocking { db.bibleDao().getAllNotesOnce() }
            assertEquals(3, saved.size)

            val noteWithoutRef = saved.first { it.title == "Promesa personal" }
            assertEquals("", noteWithoutRef.biblicalReference)
            assertEquals("Reflexión sin cita asignada automáticamente.", noteWithoutRef.content)

            val noteBlankRef = saved.first { it.content == "Nota con espacios en referencia." }
            assertEquals("", noteBlankRef.biblicalReference)
            assertEquals("Nota de Estudio", noteBlankRef.title)

            val noteManualRef = saved.first { it.title == "Éxodo" }
            assertEquals("Éxodo 1:5", noteManualRef.biblicalReference)
        } finally {
            tearDownTestViewModelAndDb(store, viewModel, db)
        }
    }

    @Test
    fun `selectedVerses state clears outside reader and formats passage text`() {
        val sampleVerses = listOf(
            BibleVerse(
                id = 1L,
                version = "RVR1960",
                testament = "NT",
                bookNumber = 500,
                bookName = "Juan",
                chapter = 3,
                verse = 16,
                text = "Porque de tal manera amó Dios al mundo..."
            ),
            BibleVerse(
                id = 2L,
                version = "RVR1960",
                testament = "NT",
                bookNumber = 500,
                bookName = "Juan",
                chapter = 3,
                verse = 17,
                text = "Porque no envió Dios a su Hijo al mundo para condenar al mundo..."
            )
        )

        val singleFormatted = BibleBooksData.buildFormattedPassageText(listOf(sampleVerses.first()))
        assertEquals("Juan 3:16 (RVR1960)\nPorque de tal manera amó Dios al mundo...", singleFormatted)

        val multiFormatted = BibleBooksData.buildFormattedPassageText(sampleVerses)
        assertEquals(
            "Juan 3:16-17 (RVR1960)\n16 Porque de tal manera amó Dios al mundo...\n17 Porque no envió Dios a su Hijo al mundo para condenar al mundo...",
            multiFormatted
        )

        val app = ApplicationProvider.getApplicationContext<Application>()
        val db = Room.inMemoryDatabaseBuilder(app, BibleDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val (store, viewModel) = createTestViewModel(app, db)
        try {
            viewModel.navigateTo(AppDestination.BIBLE)
            viewModel.setSelectedVerses(sampleVerses)
            assertEquals(sampleVerses, viewModel.uiState.value.selectedVerses)

            viewModel.navigateTo(AppDestination.HOME)
            assertTrue(viewModel.uiState.value.selectedVerses.isEmpty())
        } finally {
            tearDownTestViewModelAndDb(store, viewModel, db)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun `notes editor keeps second field empty and conditionally renders insert verses chip`() {
        var capturedSave: Triple<String, String, String>? = null

        composeTestRule.setContent {
            BibleStudyTheme(themeMode = "LIGHT") {
                NotesBottomSheet(
                    notes = emptyList(),
                    editingNote = null,
                    selectedVerses = emptyList(),
                    interfaceLanguage = "Español",
                    onSaveNote = { title, content, reference, _ ->
                        capturedSave = Triple(title, content, reference)
                    },
                    onDeleteNote = {},
                    onSelectNoteToEdit = {},
                    onDismiss = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("new_note_button").performClick()
        composeTestRule.waitForIdle()

        // 3 campos visibles y el chip de insertar versículos oculto cuando no hay selección activa
        composeTestRule.onNodeWithTag("note_title_input").assertIsDisplayed()
        composeTestRule.onNodeWithTag("note_reference_input").assertIsDisplayed()
        composeTestRule.onNodeWithTag("note_content_input").assertIsDisplayed()
        composeTestRule.onNodeWithTag("insert_selected_verses_button").assertDoesNotExist()

        composeTestRule.onNodeWithTag("note_title_input").performTextInput("Sin pasaje")
        composeTestRule.onNodeWithTag("note_content_input").performTextInput("Solo meditación")
        composeTestRule.onNodeWithTag("save_note_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        assertEquals(Triple("Sin pasaje", "Solo meditación", ""), capturedSave)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun `notes editor inserts selected verses only on tap and list displays non-blank quote`() {
        val sampleSelected = listOf(
            BibleVerse(
                id = 10L,
                version = "RVR1960",
                testament = "AT",
                bookNumber = 20,
                bookName = "Éxodo",
                chapter = 1,
                verse = 5,
                text = "Todas las personas que le nacieron a Jacob fueron setenta."
            )
        )
        var capturedSave: Triple<String, String, String>? = null
        val savedNotes = listOf(
            UserNote(
                id = 1L,
                title = "Nota libre",
                content = "Contenido sin cita",
                biblicalReference = "",
                timestamp = 1700000000L
            ),
            UserNote(
                id = 2L,
                title = "Con cita",
                content = "Observación importante",
                biblicalReference = "Éxodo 1:5 (RVR1960)\nTodas las personas que le nacieron a Jacob fueron setenta.",
                timestamp = 1700001000L
            )
        )

        composeTestRule.setContent {
            BibleStudyTheme(themeMode = "LIGHT") {
                NotesBottomSheet(
                    notes = savedNotes,
                    editingNote = null,
                    selectedVerses = sampleSelected,
                    interfaceLanguage = "Español",
                    onSaveNote = { title, content, reference, _ ->
                        capturedSave = Triple(title, content, reference)
                    },
                    onDeleteNote = {},
                    onSelectNoteToEdit = {},
                    onDismiss = {}
                )
            }
        }

        // Lista de notas: oculta referencia en nota 1 y muestra bloque en nota 2
        composeTestRule.onNodeWithTag("note_item_reference_1", useUnmergedTree = true).assertDoesNotExist()
        composeTestRule.onNodeWithTag("note_item_reference_2", useUnmergedTree = true).assertIsDisplayed()

        // Abrir editor con selección activa: el campo 2 NO se rellena solo, pero aparece el chip opcional
        composeTestRule.onNodeWithTag("new_note_button").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("note_reference_input")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString("")))
        composeTestRule.onNodeWithTag("insert_selected_verses_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("note_title_input").performTextInput("Estudio de Éxodo")
        composeTestRule.onNodeWithTag("insert_selected_verses_button").performClick()
        composeTestRule.onNodeWithTag("save_note_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        assertEquals(
            Triple(
                "Estudio de Éxodo",
                "",
                "Éxodo 1:5 (RVR1960)\nTodas las personas que le nacieron a Jacob fueron setenta."
            ),
            capturedSave
        )
    }

    @Test
    fun `theme preference cache persists and restores theme choices across recreated instances`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val rawPrefs = context.getSharedPreferences(ThemePreferenceCache.PREFS_NAME, Context.MODE_PRIVATE)
        rawPrefs.edit().clear().commit()
        try {
            val initialCache = ThemePreferenceCache(context)
            assertEquals(
                Triple("AUTO", AppThemeIds.CRISP_LIGHT, AppThemeIds.MIDNIGHT_BLUE),
                initialCache.loadInitialTheme()
            )

            initialCache.saveTheme(
                themeMode = "DARK",
                selectedLightTheme = AppThemeIds.WARM_PARCHMENT,
                selectedDarkTheme = AppThemeIds.MIDNIGHT_BLUE
            )

            val recreatedCache = ThemePreferenceCache(context)
            assertEquals(
                Triple("DARK", AppThemeIds.WARM_PARCHMENT, AppThemeIds.MIDNIGHT_BLUE),
                recreatedCache.loadInitialTheme()
            )

            recreatedCache.saveTheme(
                themeMode = "AUTOMATIC",
                selectedLightTheme = AppThemeIds.SERENE_OLIVE,
                selectedDarkTheme = AppThemeIds.OBSIDIAN_ONYX
            )

            val reloadedCache = ThemePreferenceCache(context)
            assertEquals(
                Triple("AUTO", AppThemeIds.SERENE_OLIVE, AppThemeIds.OBSIDIAN_ONYX),
                reloadedCache.loadInitialTheme()
            )
        } finally {
            rawPrefs.edit().clear().commit()
        }
    }

    @Test
    fun `bible view model initializes ui state synchronously from theme cache and keeps cache in sync`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val rawPrefs = app.getSharedPreferences(ThemePreferenceCache.PREFS_NAME, Context.MODE_PRIVATE)
        rawPrefs.edit().clear().commit()
        ThemePreferenceCache(app).saveTheme(
            themeMode = "DARK",
            selectedLightTheme = AppThemeIds.SERENE_OLIVE,
            selectedDarkTheme = AppThemeIds.MIDNIGHT_BLUE
        )

        val db = Room.inMemoryDatabaseBuilder(app, BibleDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val (store, viewModel) = createTestViewModel(app, db)
        try {
            // El primer estado síncrono debe reflejar la caché sin esperar la emisión inicial de Room
            val initialPrefs = viewModel.uiState.value.userPreferences
            assertEquals("DARK", initialPrefs.themeMode)
            assertEquals(AppThemeIds.SERENE_OLIVE, initialPrefs.selectedLightTheme)
            assertEquals(AppThemeIds.MIDNIGHT_BLUE, initialPrefs.selectedDarkTheme)

            viewModel.updateThemeMode("LIGHT")
            assertEquals(
                Triple("LIGHT", AppThemeIds.SERENE_OLIVE, AppThemeIds.MIDNIGHT_BLUE),
                ThemePreferenceCache(app).loadInitialTheme()
            )

            viewModel.updateSelectedLightTheme(AppThemeIds.WARM_PARCHMENT, activateMode = true)
            viewModel.updateSelectedDarkTheme(AppThemeIds.DEEP_DARK, activateMode = false)
            assertEquals(
                Triple("LIGHT", AppThemeIds.WARM_PARCHMENT, AppThemeIds.DEEP_DARK),
                ThemePreferenceCache(app).loadInitialTheme()
            )

            // Cuando Room emite un cambio persistido, la caché síncrona también se actualiza
            runBlocking {
                db.bibleDao().saveUserPreferences(
                    UserPreference(
                        id = 1,
                        themeMode = "DARK",
                        selectedLightTheme = AppThemeIds.CRISP_LIGHT,
                        selectedDarkTheme = AppThemeIds.OBSIDIAN_ONYX
                    )
                )
            }
            val deadline = System.currentTimeMillis() + 5_000L
            while (System.currentTimeMillis() < deadline) {
                Shadows.shadowOf(Looper.getMainLooper()).idle()
                if (viewModel.uiState.value.userPreferences.selectedDarkTheme == AppThemeIds.OBSIDIAN_ONYX) break
                Thread.sleep(20)
            }
            Shadows.shadowOf(Looper.getMainLooper()).idle()

            assertEquals("DARK", viewModel.uiState.value.userPreferences.themeMode)
            assertEquals(
                Triple("DARK", AppThemeIds.CRISP_LIGHT, AppThemeIds.OBSIDIAN_ONYX),
                ThemePreferenceCache(app).loadInitialTheme()
            )
        } finally {
            tearDownTestViewModelAndDb(store, viewModel, db)
            rawPrefs.edit().clear().commit()
        }
    }

    @Test
    fun `resolveAppColors determines dark effective mode and midnight blue palette synchronously`() {
        assertTrue(isDarkThemeEffective("DARK", systemInDarkTheme = false))
        assertFalse(isDarkThemeEffective("LIGHT", systemInDarkTheme = true))
        assertFalse(isDarkThemeEffective("AUTO", systemInDarkTheme = false))
        assertTrue(isDarkThemeEffective("AUTOMATIC", systemInDarkTheme = true))

        val darkOverrideInLightSystem = resolveAppColors(
            themeMode = "DARK",
            selectedLightTheme = AppThemeIds.CRISP_LIGHT,
            selectedDarkTheme = AppThemeIds.MIDNIGHT_BLUE,
            systemInDarkTheme = false
        )
        assertTrue(darkOverrideInLightSystem.isDark)
        assertEquals(AppThemeIds.MIDNIGHT_BLUE, darkOverrideInLightSystem.themeId)
        assertEquals(DarkMidnightBg, darkOverrideInLightSystem.background)

        val autoInLightSystem = resolveAppColors(
            themeMode = "AUTO",
            selectedLightTheme = AppThemeIds.CRISP_LIGHT,
            selectedDarkTheme = AppThemeIds.MIDNIGHT_BLUE,
            systemInDarkTheme = false
        )
        assertFalse(autoInLightSystem.isDark)
        assertEquals(AppThemeIds.CRISP_LIGHT, autoInLightSystem.themeId)
        assertEquals(PureWhite, autoInLightSystem.background)
    }
}


package com.example

import android.app.Application
import android.os.Looper
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.BibleDatabase
import com.example.data.model.ReadingProgress
import com.example.data.model.UserPreference
import com.example.data.repository.BibleRepository
import com.example.data.repository.CloudSyncRepository
import com.example.data.repository.ReadingStateCache
import com.example.ui.screens.bible.BibleScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.modules.ModulesScreen
import com.example.ui.screens.search.SearchScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.theme.BibleStudyTheme
import com.example.ui.viewmodel.AppDestination
import com.example.ui.viewmodel.BibleViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h915dp")
class Phase3Step15PolishUnitTest {

    @get:Rule
    val composeTestRule = createComposeRule()

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

    private fun waitUntil(timeoutMs: Long = 5_000L, condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (!condition() && System.currentTimeMillis() < deadline) {
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(15)
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun `fresh install starts in welcome state with Genesis 1 preloaded in Spanish and English`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        ReadingStateCache(app).clear()
        val db = Room.inMemoryDatabaseBuilder(app, BibleDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val (store, viewModel) = createTestViewModel(app, db)
        try {
            waitUntil { !viewModel.uiState.value.isChapterLoading && viewModel.uiState.value.verses.isNotEmpty() }

            val state = viewModel.uiState.value
            assertFalse("Instalación nueva debe arrancar con hasStartedReading = false", state.hasStartedReading)
            assertEquals("Génesis", state.currentBook)
            assertEquals(1, state.currentChapter)
            assertTrue("Génesis 1 debe quedar precargado para apertura instantánea", state.verses.isNotEmpty())
            assertEquals(1, state.verses.first().bookNumber)

            // Validar estado inicial en español
            composeTestRule.setContent {
                val liveState by viewModel.uiState.collectAsState()
                BibleStudyTheme(themeMode = "LIGHT") {
                    HomeScreen(uiState = liveState, viewModel = viewModel)
                }
            }
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithTag("continue_reading_card").performScrollTo().assertIsDisplayed()
            composeTestRule.onNodeWithText("Comienza tu lectura").assertIsDisplayed()
            composeTestRule.onNodeWithText("Abre las Escrituras y explora cualquier libro a tu ritmo.").assertIsDisplayed()
            composeTestRule.onNodeWithText("Empezar a leer").assertIsDisplayed()
            assertEquals(
                0,
                composeTestRule.onAllNodesWithText("Continúa donde quedaste").fetchSemanticsNodes().size
            )

            // Validar estado inicial en inglés
            viewModel.updateInterfaceLanguage("English")
            composeTestRule.waitForIdle()
            waitUntil { viewModel.uiState.value.userPreferences.interfaceLanguage == "English" }
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText("Start your reading").assertIsDisplayed()
            composeTestRule.onNodeWithText("Open the Scriptures and explore any book at your own pace.").assertIsDisplayed()
            composeTestRule.onNodeWithText("Start reading").assertIsDisplayed()
        } finally {
            ReadingStateCache(app).clear()
            tearDownTestViewModelAndDb(store, viewModel, db)
        }
    }

    @Test
    fun `starting reading transitions to continue where left with passage reference and sanitized preview`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        ReadingStateCache(app).clear()
        val db = Room.inMemoryDatabaseBuilder(app, BibleDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val (store, viewModel) = createTestViewModel(app, db)
        try {
            waitUntil { !viewModel.uiState.value.isChapterLoading && viewModel.uiState.value.verses.isNotEmpty() }
            assertFalse(viewModel.uiState.value.hasStartedReading)

            // Al tocar la tarjeta de bienvenida se abre Génesis 1 inmediatamente y cambia el estado
            viewModel.startReadingFromHome()
            waitUntil { viewModel.uiState.value.hasStartedReading && viewModel.uiState.value.continueReadingPreviewText.isNotBlank() }

            val startedState = viewModel.uiState.value
            assertTrue(startedState.hasStartedReading)
            assertEquals(AppDestination.BIBLE, startedState.currentDestination)
            assertEquals("Génesis", startedState.currentBook)
            assertEquals(1, startedState.currentChapter)
            assertTrue(startedState.continueReadingPreviewText.contains("En el principio", ignoreCase = true))
            assertFalse(startedState.continueReadingPreviewText.contains("<"))
            assertFalse(startedState.continueReadingPreviewText.contains("*"))

            // Simular que el usuario desplazó hasta el versículo 14
            viewModel.updateVisibleReadingVerse(14)
            waitUntil { viewModel.uiState.value.readingProgress?.verse == 14 }

            val scrolledState = viewModel.uiState.value
            assertEquals(14, scrolledState.currentVerse)
            assertTrue(scrolledState.continueReadingPreviewText.isNotBlank())
            assertFalse(scrolledState.continueReadingPreviewText.contains("<"))
            assertFalse(scrolledState.continueReadingPreviewText.contains("*"))

            composeTestRule.setContent {
                val liveState by viewModel.uiState.collectAsState()
                BibleStudyTheme(themeMode = "LIGHT") {
                    HomeScreen(uiState = liveState, viewModel = viewModel)
                }
            }
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithTag("continue_reading_card").performScrollTo().assertIsDisplayed()
            composeTestRule.onNodeWithText("Continúa donde quedaste", useUnmergedTree = true).performScrollTo().assertIsDisplayed()
            composeTestRule.onNodeWithTag("continue_reading_version_pill", useUnmergedTree = true).performScrollTo().assertIsDisplayed()
            composeTestRule.onNodeWithTag("continue_reading_reference", useUnmergedTree = true).performScrollTo().assertIsDisplayed()
            composeTestRule.onNodeWithText("Génesis 1:14", useUnmergedTree = true).performScrollTo().assertIsDisplayed()
            composeTestRule.onNodeWithTag("continue_reading_preview", useUnmergedTree = true).performScrollTo().assertIsDisplayed()
            composeTestRule.onNodeWithText("Continuar leyendo", useUnmergedTree = true).performScrollTo().assertIsDisplayed()
        } finally {
            ReadingStateCache(app).clear()
            tearDownTestViewModelAndDb(store, viewModel, db)
        }
    }

    @Test
    fun `single verse preview sanitizes HTML tags Strong codes and stray RVR1960 asterisks`() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val db = Room.inMemoryDatabaseBuilder(app, BibleDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val repo = BibleRepository(app, db.bibleDao())
            val rvrGen43 = repo.getSingleVersePreview("RVR1960", "Génesis", 4, 3)
            assertTrue("RVR1960 Génesis 4:3 debe devolver texto no vacío", rvrGen43.isNotBlank())
            assertFalse("No debe incluir etiquetas HTML/Strong", rvrGen43.contains("<") || rvrGen43.contains(">"))
            assertFalse("No debe incluir asteriscos sueltos `*` en el adelanto", rvrGen43.contains("*"))

            val kjvGen11 = repo.getSingleVersePreview("KJV", "Genesis", 1, 1)
            assertTrue("KJV Genesis 1:1 debe devolver texto en inglés", kjvGen11.contains("In the beginning", ignoreCase = true))
            assertFalse("KJV preview no debe incluir HTML ni Strong", kjvGen11.contains("<") || kjvGen11.contains(">"))
        } finally {
            db.close()
        }
    }

    @Test
    fun `existing Room reading progress automatically sets hasStartedReading true for upgrading users`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        ReadingStateCache(app).clear()
        val db = Room.inMemoryDatabaseBuilder(app, BibleDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        runBlocking {
            db.bibleDao().saveReadingProgress(
                ReadingProgress(
                    id = 1,
                    bookName = "Salmos",
                    chapter = 23,
                    verse = 4,
                    version = "RVR1960"
                )
            )
        }
        val (store, viewModel) = createTestViewModel(app, db)
        try {
            waitUntil {
                viewModel.uiState.value.hasStartedReading &&
                    viewModel.uiState.value.readingProgress?.bookName == "Salmos" &&
                    viewModel.uiState.value.continueReadingPreviewText.isNotBlank()
            }
            val state = viewModel.uiState.value
            assertTrue("Usuario con ReadingProgress previo debe activar hasStartedReading automáticamente", state.hasStartedReading)
            assertEquals("Salmos", state.readingProgress?.bookName)
            assertEquals(23, state.readingProgress?.chapter)
            assertEquals(4, state.readingProgress?.verse)
            assertTrue(state.continueReadingPreviewText.isNotBlank())
            assertFalse(state.continueReadingPreviewText.contains("<"))
            assertFalse(state.continueReadingPreviewText.contains("*"))
        } finally {
            ReadingStateCache(app).clear()
            tearDownTestViewModelAndDb(store, viewModel, db)
        }
    }

    @Test
    fun `controls across Home Bible Search and Settings remain interactive without indicator arrows`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        ReadingStateCache(app).clear()
        val db = Room.inMemoryDatabaseBuilder(app, BibleDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val (store, viewModel) = createTestViewModel(app, db)
        try {
            waitUntil { !viewModel.uiState.value.isChapterLoading && viewModel.uiState.value.verses.isNotEmpty() }

            // 1. HomeScreen: tarjeta de bienvenida y botón Crear nota siguen interactivos
            composeTestRule.setContent {
                val liveState by viewModel.uiState.collectAsState()
                BibleStudyTheme(themeMode = "LIGHT") {
                    when (liveState.currentDestination) {
                        AppDestination.HOME -> HomeScreen(uiState = liveState, viewModel = viewModel)
                        AppDestination.BIBLE -> BibleScreen(uiState = liveState, viewModel = viewModel)
                        AppDestination.SEARCH -> SearchScreen(uiState = liveState, viewModel = viewModel)
                        AppDestination.SETTINGS -> SettingsScreen(uiState = liveState, viewModel = viewModel)
                        AppDestination.MODULES -> ModulesScreen(uiState = liveState, viewModel = viewModel)
                    }
                }
            }
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag("continue_reading_card").performScrollTo().assertHasClickAction()
            composeTestRule.onNodeWithTag("home_create_note_button").performScrollTo().assertHasClickAction()

            // Tocar tarjeta de bienvenida navega al lector con Génesis 1
            composeTestRule.onNodeWithTag("continue_reading_card").performScrollTo().performClick()
            composeTestRule.waitForIdle()
            assertEquals(AppDestination.BIBLE, viewModel.uiState.value.currentDestination)
            assertTrue(viewModel.uiState.value.hasStartedReading)

            // 2. BibleScreen (sencillo): selector de pasaje y versión interactivos sin flechas
            composeTestRule.onNodeWithTag("passage_selector_button").assertIsDisplayed().assertHasClickAction()
            composeTestRule.onNodeWithTag("version_selector_button").assertIsDisplayed().assertHasClickAction()
            assertEquals(0, composeTestRule.onAllNodesWithContentDescription("Cambiar pasaje izquierdo").fetchSemanticsNodes().size)
            assertEquals(0, composeTestRule.onAllNodesWithContentDescription("Cambiar versión panel izquierdo").fetchSemanticsNodes().size)

            // 3. ModulesScreen: píldora de filtro de idioma sin triángulo/flecha e interactiva
            viewModel.navigateTo(AppDestination.MODULES)
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag("module_language_filter_button").assertIsDisplayed().assertHasClickAction()
            composeTestRule.onNodeWithText("Idioma: Todos").assertIsDisplayed()
            assertEquals(0, composeTestRule.onAllNodesWithContentDescription("Desplegar idiomas").fetchSemanticsNodes().size)
            for (forbiddenArrow in listOf("▾", "⌄", "▼", "→")) {
                assertEquals(
                    0,
                    composeTestRule.onAllNodesWithText(forbiddenArrow, substring = true).fetchSemanticsNodes().size
                )
            }
            // Verificar que el menú desplegable sigue abriendo y aplicando el filtro
            composeTestRule.onNodeWithTag("module_language_filter_button").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Español").assertIsDisplayed().performClick()
            composeTestRule.waitForIdle()
            assertEquals("Español", viewModel.uiState.value.moduleLanguageFilter)
            composeTestRule.onNodeWithTag("module_language_filter_button").assertTextEquals("Idioma: Español")

            // 4. SearchScreen: selector de versión interactivo sin flecha desplegable
            viewModel.navigateTo(AppDestination.SEARCH)
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag("search_version_selector").assertIsDisplayed().assertHasClickAction()
            assertEquals(0, composeTestRule.onAllNodesWithContentDescription("Cambiar versión").fetchSemanticsNodes().size)

            // 5. SettingsScreen: Favoritos, Idioma y Fuente interactivos sin flechas
            viewModel.navigateTo(AppDestination.SETTINGS)
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag("settings_favorites_item").performScrollTo().assertIsDisplayed().assertHasClickAction()
            composeTestRule.onNodeWithTag("language_dropdown_button").performScrollTo().assertIsDisplayed().assertHasClickAction()
            composeTestRule.onNodeWithTag("font_family_dropdown_button").performScrollTo().assertIsDisplayed().assertHasClickAction()
        } finally {
            ReadingStateCache(app).clear()
            tearDownTestViewModelAndDb(store, viewModel, db)
        }
    }
}

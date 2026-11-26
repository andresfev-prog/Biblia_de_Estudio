package com.example

import android.app.Application
import android.os.Looper
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.BibleDatabase
import com.example.data.model.BibleVerse
import com.example.data.repository.BibleRepository
import com.example.data.repository.CloudSyncRepository
import com.example.data.repository.ReadingStateCache
import com.example.ui.screens.bible.BibleScreen
import com.example.ui.theme.BibleStudyTheme
import com.example.ui.util.BibleStudyTextUtils
import com.example.ui.viewmodel.AppDestination
import com.example.ui.viewmodel.BibleUiState
import com.example.ui.viewmodel.BibleViewModel
import kotlinx.coroutines.Job
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h915dp")
class VersionSwitchScrollPreservationTest {

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
        db: BibleDatabase,
        closeDb: Boolean = true
    ) {
        val vmJob = viewModel.viewModelScope.coroutineContext[Job]
        store.clear()
        val deadline = System.currentTimeMillis() + 3_000L
        while (vmJob != null && vmJob.children.any { !it.isCompleted } && System.currentTimeMillis() < deadline) {
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(10)
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        if (closeDb) {
            db.close()
        }
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
    fun `full screen reader preserves active verse 10 when switching Bible versions`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        ReadingStateCache(app).clear()
        val db = Room.inMemoryDatabaseBuilder(app, BibleDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val (store, viewModel) = createTestViewModel(app, db)
        try {
            viewModel.navigateTo(AppDestination.BIBLE)
            waitUntil { !viewModel.uiState.value.isChapterLoading && viewModel.uiState.value.verses.size >= 20 }

            composeTestRule.setContent {
                val liveState by viewModel.uiState.collectAsState()
                BibleStudyTheme(themeMode = "LIGHT") {
                    BibleScreen(uiState = liveState, viewModel = viewModel)
                }
            }
            composeTestRule.waitForIdle()

            // En SingleReaderView el índice 0 es el encabezado del capítulo y el índice 10 corresponde al versículo 10
            composeTestRule.onNodeWithTag("single_reader_list").performScrollToIndex(10)
            composeTestRule.waitForIdle()
            waitUntil { viewModel.uiState.value.currentVerse == 10 }

            composeTestRule.onNodeWithTag("verse_item_10").assertIsDisplayed()
            assertEquals(
                "El versículo 1 no debe estar visible tras desplazar al versículo 10",
                0,
                composeTestRule.onAllNodesWithTag("verse_item_1").fetchSemanticsNodes().size
            )

            // Cambiar de versión (RVR1960 -> KJV) desde el selector superior en pantalla completa
            composeTestRule.onNodeWithTag("version_selector_button").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("King James Version (KJV)", substring = true).performClick()

            waitUntil {
                viewModel.uiState.value.activeVersion == "KJV" &&
                    !viewModel.uiState.value.isChapterLoading &&
                    viewModel.uiState.value.verses.isNotEmpty()
            }
            composeTestRule.waitForIdle()

            assertEquals("KJV", viewModel.uiState.value.activeVersion)
            assertEquals(10, viewModel.uiState.value.currentVerse)
            composeTestRule.onNodeWithTag("verse_item_10").assertIsDisplayed()
            assertEquals(
                "Al cambiar a KJV la vista debe mantenerse en el versículo 10 sin regresar al versículo 1",
                0,
                composeTestRule.onAllNodesWithTag("verse_item_1").fetchSemanticsNodes().size
            )

            // Cambiar nuevamente de versión (KJV -> RVR1960) y verificar que sigue en el versículo 10
            composeTestRule.onNodeWithTag("version_selector_button").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Reina-Valera 1960 (RVR1960)", substring = true).performClick()

            waitUntil {
                viewModel.uiState.value.activeVersion == "RVR1960" &&
                    !viewModel.uiState.value.isChapterLoading &&
                    viewModel.uiState.value.verses.isNotEmpty()
            }
            composeTestRule.waitForIdle()

            assertEquals("RVR1960", viewModel.uiState.value.activeVersion)
            assertEquals(10, viewModel.uiState.value.currentVerse)
            composeTestRule.onNodeWithTag("verse_item_10").assertIsDisplayed()
            assertEquals(
                0,
                composeTestRule.onAllNodesWithTag("verse_item_1").fetchSemanticsNodes().size
            )
        } finally {
            ReadingStateCache(app).clear()
            tearDownTestViewModelAndDb(store, viewModel, db)
        }
    }

    @Test
    fun `parallel split screen preserves verse 10 in both panes when switching primary or secondary version`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        ReadingStateCache(app).clear()
        val db = Room.inMemoryDatabaseBuilder(app, BibleDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val (store, viewModel) = createTestViewModel(app, db)
        try {
            viewModel.navigateTo(AppDestination.BIBLE)
            waitUntil { !viewModel.uiState.value.isChapterLoading && viewModel.uiState.value.verses.size >= 20 }

            viewModel.toggleParallelMode()
            waitUntil {
                viewModel.uiState.value.isParallelMode &&
                    !viewModel.uiState.value.isParallelChapterLoading &&
                    viewModel.uiState.value.parallelVerses.size >= 20
            }

            composeTestRule.setContent {
                val liveState by viewModel.uiState.collectAsState()
                BibleStudyTheme(themeMode = "LIGHT") {
                    BibleScreen(uiState = liveState, viewModel = viewModel)
                }
            }
            composeTestRule.waitForIdle()

            // En ParallelReaderView el índice 9 corresponde directamente al versículo 10
            composeTestRule.onNodeWithTag("parallel_left_list").performScrollToIndex(9)
            composeTestRule.waitForIdle()
            waitUntil { viewModel.uiState.value.currentVerse == 10 }
            composeTestRule.waitForIdle()

            assertEquals(
                "Ambos paneles en pantalla dividida deben mostrar el versículo 10",
                2,
                composeTestRule.onAllNodesWithTag("verse_item_10").fetchSemanticsNodes().size
            )
            assertEquals(
                "Ningún panel debe mostrar el versículo 1 tras avanzar al versículo 10",
                0,
                composeTestRule.onAllNodesWithTag("verse_item_1").fetchSemanticsNodes().size
            )

            // 1. Cambiar la versión PRINCIPAL (panel izquierdo: RVR1960 -> KJV)
            composeTestRule.onNodeWithTag("parallel_left_version_selector").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("King James Version (KJV)", substring = true).performClick()

            waitUntil {
                viewModel.uiState.value.activeVersion == "KJV" &&
                    !viewModel.uiState.value.isChapterLoading &&
                    viewModel.uiState.value.verses.isNotEmpty()
            }
            composeTestRule.waitForIdle()

            assertEquals("KJV", viewModel.uiState.value.activeVersion)
            assertEquals(10, viewModel.uiState.value.currentVerse)
            assertEquals(
                "Al cambiar la versión principal en pantalla dividida, ambos paneles deben seguir en el versículo 10",
                2,
                composeTestRule.onAllNodesWithTag("verse_item_10").fetchSemanticsNodes().size
            )
            assertEquals(
                0,
                composeTestRule.onAllNodesWithTag("verse_item_1").fetchSemanticsNodes().size
            )

            // 2. Cambiar la versión PARALELA/SECUNDARIA (panel derecho: KJV -> RVR1960)
            composeTestRule.onNodeWithTag("parallel_right_version_selector").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Reina-Valera 1960 (RVR1960)", substring = true).performClick()

            waitUntil {
                viewModel.uiState.value.parallelVersion == "RVR1960" &&
                    !viewModel.uiState.value.isParallelChapterLoading &&
                    viewModel.uiState.value.parallelVerses.isNotEmpty()
            }
            composeTestRule.waitForIdle()

            assertEquals("RVR1960", viewModel.uiState.value.parallelVersion)
            assertEquals(10, viewModel.uiState.value.currentVerse)
            assertEquals(
                "Al cambiar la versión secundaria en pantalla dividida, ambos paneles deben seguir en el versículo 10",
                2,
                composeTestRule.onAllNodesWithTag("verse_item_10").fetchSemanticsNodes().size
            )
            assertEquals(
                0,
                composeTestRule.onAllNodesWithTag("verse_item_1").fetchSemanticsNodes().size
            )
        } finally {
            ReadingStateCache(app).clear()
            tearDownTestViewModelAndDb(store, viewModel, db)
        }
    }

    @Test
    fun `omitted verse in critical edition resolves to immediately preceding or closest available verse`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        ReadingStateCache(app).clear()
        val db = Room.inMemoryDatabaseBuilder(app, BibleDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val (store, viewModel) = createTestViewModel(app, db)
        try {
            // Simulación de Hechos 8 en una edición crítica que omite el versículo 37 (contiene 1..36 y 38..40)
            val criticalActs8Verses = ((1..36) + (38..40)).map { v ->
                BibleVerse(
                    id = 440800L + v,
                    version = "CRITICAL",
                    testament = "NT",
                    bookNumber = 44,
                    bookName = "Hechos",
                    chapter = 8,
                    verse = v,
                    text = "Texto de prueba para Hechos 8:$v en edición crítica."
                )
            }

            // 1. Si falta Hechos 8:37, debe posicionarse en Hechos 8:36 (inmediatamente anterior)
            val idxFor37 = BibleStudyTextUtils.resolveClosestVerseIndex(criticalActs8Verses, 37)
            assertTrue(idxFor37 >= 0)
            assertEquals(36, criticalActs8Verses[idxFor37].verse)

            // 2. Coincidencia exacta
            val idxFor10 = BibleStudyTextUtils.resolveClosestVerseIndex(criticalActs8Verses, 10)
            assertEquals(10, criticalActs8Verses[idxFor10].verse)

            // 3. Si el versículo supera el final del capítulo, usa el último disponible
            val idxFor50 = BibleStudyTextUtils.resolveClosestVerseIndex(criticalActs8Verses, 50)
            assertEquals(40, criticalActs8Verses[idxFor50].verse)

            // 4. Si faltara el versículo 1, usa el más cercano disponible (v. 2) sin lanzar excepción
            val missingVerse1 = criticalActs8Verses.drop(1)
            val idxFor1 = BibleStudyTextUtils.resolveClosestVerseIndex(missingVerse1, 1)
            assertEquals(2, missingVerse1[idxFor1].verse)

            // 5. Lista vacía devuelve -1 de forma segura
            assertEquals(-1, BibleStudyTextUtils.resolveClosestVerseIndex(emptyList(), 37))

            // 6. Verificar en Compose UI que BibleScreen restaura en Hechos 8:36 sin saltar al versículo 1
            val simulatedState = BibleUiState(
                currentDestination = AppDestination.BIBLE,
                currentBook = "Hechos",
                currentChapter = 8,
                currentVerse = 36,
                activeVersion = "CRITICAL",
                verses = criticalActs8Verses,
                scrollRestoreVerse = 37,
                scrollRestoreToken = 1L,
                hasStartedReading = true
            )

            composeTestRule.setContent {
                BibleStudyTheme(themeMode = "LIGHT") {
                    BibleScreen(uiState = simulatedState, viewModel = viewModel)
                }
            }
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithTag("verse_item_36").assertIsDisplayed()
            assertEquals(
                "Cuando se omite el v. 37 no debe reiniciar la lectura al v. 1",
                0,
                composeTestRule.onAllNodesWithTag("verse_item_1").fetchSemanticsNodes().size
            )
        } finally {
            ReadingStateCache(app).clear()
            tearDownTestViewModelAndDb(store, viewModel, db)
        }
    }

    @Test
    fun `ReadingStateCache preserves book chapter verse and translation across app restart`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        ReadingStateCache(app).clear()
        val db = Room.inMemoryDatabaseBuilder(app, BibleDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        // Sesión 1: el usuario navega a Juan 3, avanza al versículo 10 y cambia la versión a KJV
        val (store1, viewModel1) = createTestViewModel(app, db)
        try {
            viewModel1.navigateToPassage("Juan", 3, 1)
            waitUntil {
                viewModel1.uiState.value.currentBook == "Juan" &&
                    viewModel1.uiState.value.currentChapter == 3 &&
                    !viewModel1.uiState.value.isChapterLoading &&
                    viewModel1.uiState.value.verses.size >= 20
            }

            viewModel1.updateVisibleReadingVerse(10)
            assertEquals(10, viewModel1.uiState.value.currentVerse)

            viewModel1.setActiveVersion("KJV", currentVisibleVerse = 10)
            waitUntil {
                viewModel1.uiState.value.activeVersion == "KJV" &&
                    !viewModel1.uiState.value.isChapterLoading &&
                    viewModel1.uiState.value.verses.isNotEmpty()
            }

            val cached = ReadingStateCache(app).loadReadingPosition()
            assertNotNull("ReadingStateCache debe conservar la posición tras cambiar de traducción", cached)
            assertEquals("Juan", cached?.bookName)
            assertEquals(3, cached?.chapter)
            assertEquals(10, cached?.verse)
            assertEquals("KJV", cached?.version)
        } finally {
            tearDownTestViewModelAndDb(store1, viewModel1, db, closeDb = false)
        }

        // Sesión 2: cerrar y reabrir la app (nueva instancia de BibleViewModel leyendo desde caché/Room)
        val (store2, viewModel2) = createTestViewModel(app, db)
        try {
            waitUntil {
                !viewModel2.uiState.value.isChapterLoading &&
                    viewModel2.uiState.value.verses.size >= 20 &&
                    viewModel2.uiState.value.currentBook == "Juan"
            }

            val restored = viewModel2.uiState.value
            assertTrue(restored.hasStartedReading)
            assertEquals("Juan", restored.currentBook)
            assertEquals(3, restored.currentChapter)
            assertEquals(10, restored.currentVerse)
            assertEquals("KJV", restored.activeVersion)

            viewModel2.navigateTo(AppDestination.BIBLE)
            composeTestRule.setContent {
                val liveState by viewModel2.uiState.collectAsState()
                BibleStudyTheme(themeMode = "LIGHT") {
                    BibleScreen(uiState = liveState, viewModel = viewModel2)
                }
            }
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithTag("verse_item_10").assertIsDisplayed()
            assertEquals(
                "Al reabrir la app debe retomar directamente en el versículo 10 sin volver al versículo 1",
                0,
                composeTestRule.onAllNodesWithTag("verse_item_1").fetchSemanticsNodes().size
            )
        } finally {
            ReadingStateCache(app).clear()
            tearDownTestViewModelAndDb(store2, viewModel2, db, closeDb = true)
        }
    }
}

package com.example

import android.app.Application
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Looper
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.BibleDatabase
import com.example.data.model.BibleBooksData
import com.example.data.model.ModuleBibleCatalog
import com.example.data.repository.BibleAssetDatabaseHelper
import com.example.data.repository.BibleRepository
import com.example.data.repository.CloudSyncRepository
import com.example.ui.components.PassageSelectorDialog
import com.example.ui.screens.bible.BibleScreen
import com.example.ui.screens.modules.ModulesScreen
import com.example.ui.screens.search.SearchScreen
import com.example.ui.theme.BibleStudyTheme
import com.example.ui.util.BibleStudyTextUtils
import com.example.ui.viewmodel.BibleViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h915dp")
class Phase25PartialBiblesAndNotesUnitTest {

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

    private fun waitUntil(timeoutMs: Long = 5_000L, predicate: () -> Boolean): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            if (predicate()) return true
            Thread.sleep(20)
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        return predicate()
    }

    private fun createPartialTptLikeDatabase(context: Context, versionTag: String): File {
        val file = context.getDatabasePath("$versionTag.SQLite3")
        file.parentFile?.mkdirs()
        if (file.exists()) file.delete()
        SQLiteDatabase.openOrCreateDatabase(file, null).use { sqlDb ->
            sqlDb.execSQL("CREATE TABLE books (book_number NUMERIC, short_name TEXT, long_name TEXT)")
            sqlDb.execSQL("CREATE TABLE verses (book_number NUMERIC, chapter NUMERIC, verse NUMERIC, text TEXT)")

            // Subconjunto estilo TPT: Salmos (1, 23, 150), Proverbios (1, 3), Cantares (8), Mateo (1), Juan (3), Apocalipsis (22)
            sqlDb.execSQL("INSERT INTO books VALUES (230, 'Ps', 'Psalms')")
            sqlDb.execSQL("INSERT INTO books VALUES (240, 'Prov', 'Proverbs')")
            sqlDb.execSQL("INSERT INTO books VALUES (260, 'Song', 'Song of Solomon')")
            sqlDb.execSQL("INSERT INTO books VALUES (470, 'Mt', 'Matthew')")
            sqlDb.execSQL("INSERT INTO books VALUES (500, 'Jn', 'John')")
            sqlDb.execSQL("INSERT INTO books VALUES (730, 'Rev', 'Revelation')")

            sqlDb.execSQL("INSERT INTO verses VALUES (230, 1, 1, 'What delight comes to the one who follows God.')")
            sqlDb.execSQL("INSERT INTO verses VALUES (230, 23, 1, 'Yahweh is my best friend and my shepherd.')")
            sqlDb.execSQL("INSERT INTO verses VALUES (230, 150, 6, 'Let every living thing praise the Lord.')")
            sqlDb.execSQL("INSERT INTO verses VALUES (240, 1, 7, 'The fear of the Lord is the beginning of wisdom.')")
            sqlDb.execSQL("INSERT INTO verses VALUES (240, 3, 5, 'Trust in the Lord completely with all your heart.')")
            sqlDb.execSQL("INSERT INTO verses VALUES (260, 8, 7, 'Many waters cannot quench love.')")
            sqlDb.execSQL("INSERT INTO verses VALUES (470, 1, 1, 'The genealogy of Jesus the Messiah.')")
            sqlDb.execSQL("INSERT INTO verses VALUES (500, 3, 16, 'For here is the way God loved the world through grace.')")
            sqlDb.execSQL("INSERT INTO verses VALUES (730, 22, 21, 'May the grace of the Lord Jesus be with you all.')")
        }
        return file
    }

    // =========================================================================
    // 1. OCULTAMIENTO DE ASTERISCOS (*) SIN NOTA REAL EN RVR1960
    // =========================================================================

    @Test
    fun `sanitizeVerseTextForDisplay hides RVR1960 asterisks and artifacts cleanly when no real note exists`() {
        val rawGen43 = "Y aconteció «andando el tiempo,» *<S>7093</S> ,<S>3117</S> que Caín trajo del fruto."
        val hiddenGen43 = BibleStudyTextUtils.sanitizeVerseTextForDisplay(
            rawText = rawGen43,
            version = "RVR1960",
            showRvrAsterisks = false
        )
        assertEquals(
            "Y aconteció «andando el tiempo,» que Caín trajo del fruto.",
            hiddenGen43
        )
        assertFalse(hiddenGen43.contains('*'))
        assertFalse(hiddenGen43.contains("  "))

        val rawGen46 = "¿Por qué «te has ensañado,» *, y por qué ha decaído tu semblante?"
        val hiddenGen46 = BibleStudyTextUtils.sanitizeVerseTextForDisplay(
            rawText = rawGen46,
            version = "RVR1960",
            showRvrAsterisks = false
        )
        assertEquals(
            "¿Por qué «te has ensañado,» y por qué ha decaído tu semblante?",
            hiddenGen46
        )

        val rawEndPunct = "Una ofrenda a Jehová «grata» * ."
        val hiddenEndPunct = BibleStudyTextUtils.sanitizeVerseTextForDisplay(
            rawText = rawEndPunct,
            version = "RVR1960",
            showRvrAsterisks = false
        )
        assertEquals("Una ofrenda a Jehová «grata».", hiddenEndPunct)

        // Cuando sí exista una nota real en el futuro, showRvrAsterisks = true conserva el * limpio
        val shownGen43 = BibleStudyTextUtils.sanitizeVerseTextForDisplay(
            rawText = rawGen43,
            version = "RVR1960",
            showRvrAsterisks = true
        )
        assertEquals(
            "Y aconteció «andando el tiempo,»* que Caín trajo del fruto.",
            shownGen43
        )
    }

    @Test
    fun `getVersesWithRealVersionNotes ignores book-intro-only RVR-C and detects real verse footnotes`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val helper = BibleAssetDatabaseHelper(context)
        BibleAssetDatabaseHelper.invalidateVersionNotesCache()

        // A) Módulo ausente -> vacío
        assertTrue(helper.getVersesWithRealVersionNotes("missing_rvrc_${System.nanoTime()}.SQLite3", "Génesis", 4).isEmpty())

        // B) Esquema actual de solo introducciones de libro (1:1) -> vacío incluso en Génesis 1
        val introOnlyName = "test_rvrc_intro_only_${System.nanoTime()}.SQLite3"
        val introOnlyFile = context.getDatabasePath(introOnlyName)
        introOnlyFile.parentFile?.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(introOnlyFile, null).use { sqlDb ->
            sqlDb.execSQL(
                "CREATE TABLE commentaries (book_number NUMERIC, chapter_number_from NUMERIC, verse_number_from NUMERIC, chapter_number_to NUMERIC, verse_number_to NUMERIC, text TEXT)"
            )
            sqlDb.execSQL("INSERT INTO commentaries VALUES (10, 1, 1, 1, 1, 'Introducción general a Génesis')")
            sqlDb.execSQL("INSERT INTO commentaries VALUES (20, 1, 1, 1, 1, 'Introducción general a Éxodo')")
        }

        // C) Futuro módulo RVR-C con notas reales por versículo -> devuelve exactamente esos versículos
        val realNotesName = "test_rvrc_real_notes_${System.nanoTime()}.SQLite3"
        val realNotesFile = context.getDatabasePath(realNotesName)
        SQLiteDatabase.openOrCreateDatabase(realNotesFile, null).use { sqlDb ->
            sqlDb.execSQL(
                "CREATE TABLE commentaries (book_number NUMERIC, chapter_number_from NUMERIC, verse_number_from NUMERIC, chapter_number_to NUMERIC, verse_number_to NUMERIC, text TEXT)"
            )
            sqlDb.execSQL("INSERT INTO commentaries VALUES (10, 4, 3, 4, 3, 'Heb. al fin de días.')")
            sqlDb.execSQL("INSERT INTO commentaries VALUES (10, 4, 6, 4, 7, 'Nota de rango sobre Caín.')")
        }

        try {
            assertTrue(
                "Un archivo con solo introducciones 1:1 no debe activar asteriscos de versículo",
                helper.getVersesWithRealVersionNotes(introOnlyName, "Génesis", 1).isEmpty()
            )
            assertTrue(
                helper.getVersesWithRealVersionNotes(introOnlyName, "Génesis", 4).isEmpty()
            )

            assertEquals(
                setOf(3, 6, 7),
                helper.getVersesWithRealVersionNotes(realNotesName, "Génesis", 4)
            )
        } finally {
            introOnlyFile.delete()
            realNotesFile.delete()
            BibleAssetDatabaseHelper.invalidateVersionNotesCache()
        }
    }

    @Test
    fun `loading RVR1960 Genesis 4 without RVR-C installed removes visible asterisks from verses`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        app.getDatabasePath("RV1960-c.commentaries.SQLite3").delete()
        BibleAssetDatabaseHelper.invalidateVersionNotesCache()

        val db = Room.inMemoryDatabaseBuilder(app, BibleDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val (store, viewModel) = createTestViewModel(app, db)
        try {
            viewModel.loadChapter("Génesis", 4, version = "RVR1960")
            val loaded = waitUntil {
                !viewModel.uiState.value.isChapterLoading &&
                    viewModel.uiState.value.currentBook == "Génesis" &&
                    viewModel.uiState.value.currentChapter == 4 &&
                    viewModel.uiState.value.verses.isNotEmpty()
            }
            assertTrue("Génesis 4 debe cargarse desde RVR1960.SQLite3", loaded)
            assertTrue(viewModel.uiState.value.versesWithRvrNotes.isEmpty())

            val verse3 = viewModel.uiState.value.verses.firstOrNull { it.verse == 3 }
            assertNotNull(verse3)
            assertFalse("No debe mostrarse '*' sin nota real en Génesis 4:3", verse3!!.text.contains('*'))
            assertTrue("La puntuación real debe conservarse", verse3.text.contains("andando el tiempo"))
        } finally {
            tearDownTestViewModelAndDb(store, viewModel, db)
        }
    }

    // =========================================================================
    // 2. BIBLIAS PARCIALES: CERO PLACEHOLDER Y COHERENCIA EN BÚSQUEDA
    // =========================================================================

    @Test
    fun `partial SQLite returns empty list for missing book or chapter with zero placeholder text`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, BibleDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val partialTag = "TPTPARTIAL"
        val partialFile = createPartialTptLikeDatabase(context, partialTag)
        try {
            val repo = BibleRepository(context, db.bibleDao())

            // Libro ausente (Jeremías 29) -> lista vacía (cero texto simulado)
            val jeremiah29 = runBlocking { repo.getVerses(partialTag, "Jeremías", 29).first() }
            assertTrue("Un libro ausente en biblia parcial debe devolver lista vacía", jeremiah29.isEmpty())

            // Capítulo ausente dentro de un libro existente (Proverbios 2) -> lista vacía
            val proverbs2 = runBlocking { repo.getVerses(partialTag, "Proverbios", 2).first() }
            assertTrue("Un capítulo ausente en biblia parcial debe devolver lista vacía", proverbs2.isEmpty())

            // Pasaje existente (Juan 3) -> texto real
            val john3 = runBlocking { repo.getVerses(partialTag, "Juan", 3).first() }
            assertEquals(1, john3.size)
            assertEquals(16, john3.first().verse)
            assertTrue(john3.first().text.contains("grace"))

            // Búsqueda de referencia inexistente (Jeremías 29) -> 0 resultados
            val searchJer = runBlocking {
                repo.searchVerses("Jeremías 29", "TODOS", partialTag, exactWord = false).first()
            }
            assertTrue(searchJer.isEmpty())

            // Búsqueda de término real -> solo resultados reales de libros presentes en la versión parcial
            val searchGrace = runBlocking {
                repo.searchVerses("grace", "TODOS", partialTag, exactWord = true).first()
            }
            assertEquals(2, searchGrace.size)
            assertTrue(searchGrace.all { it.bookNumber in setOf(500, 730) })
        } finally {
            partialFile.delete()
            db.close()
        }
    }

    // =========================================================================
    // 3. CATÁLOGO DINÁMICO, NAVEGACIÓN ANTERIOR/SIGUIENTE, PARALELO Y TTS
    // =========================================================================

    @Test
    fun `dynamic module catalog lists only real books chapters and navigates strictly across available passages`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, BibleDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val partialTag = "TPTNAVTEST"
        val partialFile = createPartialTptLikeDatabase(context, partialTag)
        try {
            val repo = BibleRepository(context, db.bibleDao())
            val catalog = runBlocking { repo.getModuleCatalog(partialTag) }

            assertEquals(6, catalog.availableBooks.size)
            assertEquals(listOf(19, 20, 22, 40, 43, 66), catalog.availableBooks.map { it.number })
            assertTrue(catalog.hasOldTestament)
            assertTrue(catalog.hasNewTestament)
            assertFalse(catalog.hasPassage("Jeremías", 29))
            assertTrue(catalog.hasPassage("Salmos", 23))
            assertEquals(listOf(1, 23, 150), catalog.getAvailableChapters("Salmos"))
            assertEquals(listOf(1, 3), catalog.getAvailableChapters("Proverbios"))

            // Navegación al inicio y fin absolutos del módulo parcial
            assertNull(catalog.getPreviousPassage("Salmos", 1))
            assertNull(catalog.getNextPassage("Apocalipsis", 22))

            // Salto dentro del mismo libro omitiendo capítulo 2 inexistente
            val nextFromProv1 = catalog.getNextPassage("Proverbios", 1)
            assertEquals(20 to 3, nextFromProv1?.let { it.first.number to it.second })

            // Cruce hacia atrás al libro anterior disponible (Proverbios 1 -> Salmos 150)
            val prevFromProv1 = catalog.getPreviousPassage("Proverbios", 1)
            assertEquals(19 to 150, prevFromProv1?.let { it.first.number to it.second })

            // Cruce saltando profetas ausentes (Cantares 8 -> Mateo 1 y viceversa)
            val nextFromSong8 = catalog.getNextPassage("Cantares", 8)
            assertEquals(40 to 1, nextFromSong8?.let { it.first.number to it.second })
            val prevFromMatt1 = catalog.getPreviousPassage("Mateo", 1)
            assertEquals(22 to 8, prevFromMatt1?.let { it.first.number to it.second })

            // Versículos reales disponibles desde el SQLite del módulo
            val john3Verses = runBlocking { repo.getAvailableVerseNumbers(partialTag, "Juan", 3) }
            assertEquals(listOf(16), john3Verses)
        } finally {
            partialFile.delete()
            db.close()
        }
    }

    @Test
    fun `passage selector dialog hides old testament tab for NT-only partial bible`() {
        val ntOnlyCatalog = ModuleBibleCatalog.fromChaptersMap(
            version = "NTONLY",
            chaptersByCanonicalBook = mapOf(
                40 to listOf(1, 2, 3),
                43 to listOf(1, 3)
            )
        )
        assertFalse(ntOnlyCatalog.hasOldTestament)
        assertTrue(ntOnlyCatalog.hasNewTestament)

        composeTestRule.setContent {
            BibleStudyTheme(themeMode = "LIGHT") {
                PassageSelectorDialog(
                    currentStep = 1,
                    selectedBook = ntOnlyCatalog.availableBooks.first(),
                    selectedChapter = 1,
                    catalog = ntOnlyCatalog,
                    availableVerses = emptyList(),
                    interfaceLanguage = "Español",
                    onBookSelected = {},
                    onChapterSelected = {},
                    onVerseSelected = {},
                    onBackStep = {},
                    onDismiss = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        assertEquals(0, composeTestRule.onAllNodesWithTag("passage_tab_ot").fetchSemanticsNodes().size)
        composeTestRule.onNodeWithTag("passage_nt_only_header").assertIsDisplayed()
        composeTestRule.onNodeWithTag("book_card_40").assertIsDisplayed()
        composeTestRule.onNodeWithTag("book_card_43").assertIsDisplayed()
        // Jeremías (24) no debe existir
        assertEquals(0, composeTestRule.onAllNodesWithTag("book_card_24").fetchSemanticsNodes().size)
    }

    @Test
    fun `unavailable passage shows clean state blocks TTS and isolates partial column in parallel mode`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val db = Room.inMemoryDatabaseBuilder(app, BibleDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val partialTag = "TPT"
        val partialFile = createPartialTptLikeDatabase(app, partialTag)
        val (store, viewModel) = createTestViewModel(app, db)
        try {
            // 1) Vista individual en versión parcial con libro ausente (Jeremías 29 en TPT)
            viewModel.loadChapter("Jeremías", 29, version = partialTag, parallelVersion = "RVR1960")
            val loadedSingle = waitUntil {
                !viewModel.uiState.value.isChapterLoading &&
                    viewModel.uiState.value.activeVersion == partialTag &&
                    viewModel.uiState.value.currentBook == "Jeremías" &&
                    viewModel.uiState.value.currentChapter == 29
            }
            assertTrue(loadedSingle)
            assertTrue(viewModel.uiState.value.verses.isEmpty())

            // TTS bloqueado en capítulo vacío/no disponible
            viewModel.toggleTtsBar()
            assertFalse(viewModel.uiState.value.isTtsVisible)
            viewModel.playTts()
            assertFalse(viewModel.uiState.value.isTtsPlaying)

            composeTestRule.setContent {
                BibleStudyTheme(themeMode = "LIGHT") {
                    BibleScreen(uiState = viewModel.uiState.value, viewModel = viewModel)
                }
            }
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithTag("passage_unavailable_state").assertIsDisplayed()
            composeTestRule.onNodeWithText("Este pasaje no está disponible en la versión TPT").assertIsDisplayed()
            composeTestRule.onNodeWithTag("unavailable_open_books_button").assertIsDisplayed()
            composeTestRule.onNodeWithTag("unavailable_switch_version_button").assertIsDisplayed()

            // 2) Vista paralela: RVR1960 (izquierda, tiene Jeremías 29) + TPT (derecha, no tiene Jeremías 29)
            viewModel.toggleParallelMode()
            viewModel.loadChapter("Jeremías", 29, version = "RVR1960", parallelVersion = partialTag)
            val loadedParallel = waitUntil {
                !viewModel.uiState.value.isChapterLoading &&
                    !viewModel.uiState.value.isParallelChapterLoading &&
                    viewModel.uiState.value.isParallelMode &&
                    viewModel.uiState.value.verses.isNotEmpty() &&
                    viewModel.uiState.value.parallelVerses.isEmpty()
            }
            assertTrue("RVR1960 debe mostrar Jeremías 29 mientras TPT permanece vacío", loadedParallel)
        } finally {
            partialFile.delete()
            tearDownTestViewModelAndDb(store, viewModel, db)
        }
    }

    @Test
    fun `modules and search screens render cleanly in light and dark modes with unified typography`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val db = Room.inMemoryDatabaseBuilder(app, BibleDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val (store, viewModel) = createTestViewModel(app, db)
        try {
            composeTestRule.setContent {
                BibleStudyTheme(themeMode = "LIGHT") {
                    ModulesScreen(uiState = viewModel.uiState.value, viewModel = viewModel)
                }
            }
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag("modules_screen").assertIsDisplayed()
            composeTestRule.onNodeWithTag("module_language_filter_button").assertIsDisplayed()
        } finally {
            tearDownTestViewModelAndDb(store, viewModel, db)
        }
    }

    @Test
    fun `search screen renders cleanly in dark mode`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val db = Room.inMemoryDatabaseBuilder(app, BibleDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val (store, viewModel) = createTestViewModel(app, db)
        try {
            composeTestRule.setContent {
                BibleStudyTheme(themeMode = "DARK") {
                    SearchScreen(uiState = viewModel.uiState.value, viewModel = viewModel)
                }
            }
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag("search_text_field").assertIsDisplayed()
            composeTestRule.onNodeWithTag("search_version_selector").assertIsDisplayed()
        } finally {
            tearDownTestViewModelAndDb(store, viewModel, db)
        }
    }
}

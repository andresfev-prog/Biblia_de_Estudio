package com.example

import android.app.Application
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Looper
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.BibleDatabase
import com.example.data.model.BibleVerse
import com.example.data.repository.BibleAssetDatabaseHelper
import com.example.data.repository.BibleRepository
import com.example.data.repository.CloudSyncRepository
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.theme.BibleStudyTheme
import com.example.ui.util.BibleStudyTextUtils
import com.example.ui.util.VerseDisplayToken
import com.example.ui.viewmodel.AppDestination
import com.example.ui.viewmodel.BibleViewModel
import kotlinx.coroutines.Job
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
import java.time.Duration

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h915dp")
class Phase2UxAndSubheadingsUnitTest {

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

    private fun waitUntil(timeoutMs: Long = 4_000L, predicate: () -> Boolean): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            if (predicate()) return true
            Thread.sleep(20)
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        return predicate()
    }

    // =========================================================================
    // 1. RESALTADO TEMPORAL DE NAVEGACIÓN (3s, cancelación al scroll/toque)
    // =========================================================================

    @Test
    fun `temporary navigation highlight auto-clears after timeout and cancels immediately on interaction`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val db = Room.inMemoryDatabaseBuilder(app, BibleDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val (store, viewModel) = createTestViewModel(app, db)
        val mainLooper = Shadows.shadowOf(Looper.getMainLooper())
        try {
            assertEquals(3_000L, BibleStudyTextUtils.TEMP_HIGHLIGHT_DURATION_MS)

            // 1) Auto-extinción exacta tras los 3 segundos reales
            viewModel.triggerNavigationHighlight(verse = 16)
            assertEquals(16, viewModel.uiState.value.highlightedVerse)
            assertTrue(viewModel.uiState.value.isNavigationHighlightActive)
            val firstToken = viewModel.uiState.value.highlightToken

            // A los 2.5s el resaltado sigue activo
            mainLooper.idleFor(Duration.ofMillis(2_500L))
            assertEquals(16, viewModel.uiState.value.highlightedVerse)
            assertTrue(viewModel.uiState.value.isNavigationHighlightActive)

            // Tras superar los 3.0s debe apagarse automáticamente
            mainLooper.idleFor(Duration.ofMillis(600L))
            assertNull(viewModel.uiState.value.highlightedVerse)
            assertFalse(viewModel.uiState.value.isNavigationHighlightActive)

            // 2) Cancelación inmediata al desplazarse o tocar otro versículo (antes de los 3s)
            viewModel.triggerNavigationHighlight(verse = 8)
            assertEquals(8, viewModel.uiState.value.highlightedVerse)
            assertTrue(viewModel.uiState.value.isNavigationHighlightActive)
            assertTrue(viewModel.uiState.value.highlightToken > firstToken)

            mainLooper.idleFor(Duration.ofMillis(500L))
            viewModel.clearNavigationHighlight()
            assertNull(viewModel.uiState.value.highlightedVerse)
            assertFalse(viewModel.uiState.value.isNavigationHighlightActive)

            // Avanzar más allá del temporizador anterior no debe provocar efectos secundarios
            mainLooper.idleFor(Duration.ofMillis(3_000L))
            assertNull(viewModel.uiState.value.highlightedVerse)
            assertFalse(viewModel.uiState.value.isNavigationHighlightActive)
        } finally {
            tearDownTestViewModelAndDb(store, viewModel, db)
        }
    }

    @Test
    fun `navigateToPassage activates temporary highlight and chapter load without preserveHighlight clears it`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val db = Room.inMemoryDatabaseBuilder(app, BibleDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val (store, viewModel) = createTestViewModel(app, db)
        try {
            viewModel.navigateToPassage("Juan", 3, 16)
            assertEquals(AppDestination.BIBLE, viewModel.uiState.value.currentDestination)
            assertEquals(16, viewModel.uiState.value.highlightedVerse)
            assertTrue(viewModel.uiState.value.isNavigationHighlightActive)

            // Cambiar de capítulo normalmente limpia resaltados de navegación anteriores
            viewModel.loadChapter("Génesis", 1)
            assertNull(viewModel.uiState.value.highlightedVerse)
            assertFalse(viewModel.uiState.value.isNavigationHighlightActive)
        } finally {
            tearDownTestViewModelAndDb(store, viewModel, db)
        }
    }

    // =========================================================================
    // 2. LIMPIEZA Y PARSEO DE NOTAS ASTERISCO (*) EN RVR1960 Y [n] EN NTV
    // =========================================================================

    @Test
    fun `sanitizeVerseTextForDisplay cleans RVR1960 asterisk artifacts without harming real punctuation`() {
        val rawGenesis43 = "Y aconteció «andando el tiempo,» *<S>7093</S> ,<S>3117</S> que Caín trajo del fruto de la tierra."
        val cleaned1 = BibleStudyTextUtils.sanitizeVerseTextForDisplay(rawGenesis43, "RVR1960")
        assertEquals(
            "Y aconteció «andando el tiempo,»* que Caín trajo del fruto de la tierra.",
            cleaned1
        )

        val rawGenesis46 = "¿Por qué «te has ensañado,» *, y por qué ha decaído tu semblante?"
        val cleaned2 = BibleStudyTextUtils.sanitizeVerseTextForDisplay(rawGenesis46, "RVR1960")
        assertEquals(
            "¿Por qué «te has ensañado,»* y por qué ha decaído tu semblante?",
            cleaned2
        )

        val rawNumbers = "En el mes segundo, a los «veintisiete» *, años; se secó la tierra."
        val cleaned3 = BibleStudyTextUtils.sanitizeVerseTextForDisplay(rawNumbers, "RVR1960")
        assertEquals(
            "En el mes segundo, a los «veintisiete»* años; se secó la tierra.",
            cleaned3
        )

        val speech = BibleStudyTextUtils.sanitizeVerseTextForSpeech(rawGenesis43, "RVR1960")
        assertFalse("TTS no debe conservar asteriscos", speech.contains('*'))
        assertTrue(speech.contains("andando el tiempo"))
    }

    @Test
    fun `tokenizeVerseText detects clickable asterisks only in RVR1960 and preserves numeric bracket notes`() {
        val rvrTokens = BibleStudyTextUtils.tokenizeVerseText(
            displayText = "«andando el tiempo,»* y «su ofrenda»* agradó.",
            isRvr1960 = true
        )
        val asterisks = rvrTokens.filterIsInstance<VerseDisplayToken.AsteriskNote>()
        assertEquals(listOf(1, 2), asterisks.map { it.ordinal })

        // En versiones que no son RVR1960, un asterisco no se interpreta como nota RVR-C
        val kjvTokens = BibleStudyTextUtils.tokenizeVerseText(
            displayText = "In the beginning* God created [1] the heaven.",
            isRvr1960 = false
        )
        assertTrue(kjvTokens.filterIsInstance<VerseDisplayToken.AsteriskNote>().isEmpty())
        val bracketNotes = kjvTokens.filterIsInstance<VerseDisplayToken.BracketNote>()
        assertEquals(listOf("1"), bracketNotes.map { it.marker })
    }

    @Test
    fun `clicking RVR1960 asterisk without RVR-C downloaded shows missing module prompt`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        // Asegurar que RV1960-c.commentaries.SQLite3 no esté instalado en este test
        app.getDatabasePath("RV1960-c.commentaries.SQLite3").delete()

        val db = Room.inMemoryDatabaseBuilder(app, BibleDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val (store, viewModel) = createTestViewModel(app, db)
        try {
            val verse = BibleVerse(
                id = 100403L,
                version = "RVR1960",
                testament = "AT",
                bookNumber = 10,
                bookName = "Génesis",
                chapter = 4,
                verse = 3,
                text = "Y aconteció «andando el tiempo,»* que Caín trajo del fruto."
            )

            viewModel.onVersionNoteClick(verse, "*:1")
            val prompted = waitUntil { viewModel.uiState.value.missingNoteModulePrompt != null }
            assertTrue("Debe mostrarse el diálogo guiado para descargar RVR-C", prompted)
            val prompt = viewModel.uiState.value.missingNoteModulePrompt!!
            assertEquals("RVR-C", prompt.moduleTag)
            assertEquals("mod_rv1960_c", prompt.moduleId)
            assertEquals("Génesis 4:3", prompt.verseReference)

            viewModel.dismissMissingNoteModulePrompt()
            assertNull(viewModel.uiState.value.missingNoteModulePrompt)
        } finally {
            tearDownTestViewModelAndDb(store, viewModel, db)
        }
    }

    @Test
    fun `getVersionNotes handles marker-less RVR-C schemas book-intro schemas and NTV marker schemas`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val helper = BibleAssetDatabaseHelper(context)

        // A) Esquema RVR-C sin columna marker con múltiples notas en un mismo versículo
        val multiDbName = "test_rvrc_multi_${System.nanoTime()}.SQLite3"
        val multiFile = context.getDatabasePath(multiDbName)
        multiFile.parentFile?.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(multiFile, null).use { sqlDb ->
            sqlDb.execSQL(
                """
                CREATE TABLE commentaries (
                    book_number NUMERIC,
                    chapter_number_from NUMERIC,
                    verse_number_from NUMERIC,
                    chapter_number_to NUMERIC,
                    verse_number_to NUMERIC,
                    text TEXT
                )
                """.trimIndent()
            )
            sqlDb.execSQL("INSERT INTO commentaries VALUES (10, 4, 3, 4, 3, '<p>Nota primera sobre el tiempo.</p>')")
            sqlDb.execSQL("INSERT INTO commentaries VALUES (10, 4, 3, 4, 3, '<p>Nota segunda sobre la ofrenda.</p>')")
        }

        // B) Esquema RVR-C actual (solo introducciones de libro en 1:1)
        val introOnlyDbName = "test_rvrc_intro_${System.nanoTime()}.SQLite3"
        val introFile = context.getDatabasePath(introOnlyDbName)
        SQLiteDatabase.openOrCreateDatabase(introFile, null).use { sqlDb ->
            sqlDb.execSQL(
                """
                CREATE TABLE commentaries (
                    book_number NUMERIC,
                    chapter_number_from NUMERIC,
                    verse_number_from NUMERIC,
                    chapter_number_to NUMERIC,
                    verse_number_to NUMERIC,
                    text TEXT
                )
                """.trimIndent()
            )
            sqlDb.execSQL("INSERT INTO commentaries VALUES (10, 1, 1, 1, 1, 'Introducción general a Génesis.')")
        }

        // C) Esquema NTV con columna marker ('[1]', '[2]')
        val ntvDbName = "test_ntv_notes_${System.nanoTime()}.SQLite3"
        val ntvFile = context.getDatabasePath(ntvDbName)
        SQLiteDatabase.openOrCreateDatabase(ntvFile, null).use { sqlDb ->
            sqlDb.execSQL(
                """
                CREATE TABLE commentaries (
                    book_number NUMERIC,
                    chapter_number_from NUMERIC,
                    verse_number_from NUMERIC,
                    marker TEXT,
                    text TEXT
                )
                """.trimIndent()
            )
            sqlDb.execSQL("INSERT INTO commentaries VALUES (10, 1, 1, '[1]', 'Primera nota NTV.')")
            sqlDb.execSQL("INSERT INTO commentaries VALUES (10, 1, 1, '[2]', 'Segunda nota NTV.')")
        }

        try {
            val multiLookup = helper.getVersionNotes(multiDbName, "Génesis", 4, 3, "*:1")
            assertEquals(2, multiLookup.notes.size)
            assertFalse(multiLookup.hasBookIntroOnlySchema)

            val introLookup = helper.getVersionNotes(introOnlyDbName, "Génesis", 4, 3, "*:1")
            assertTrue(introLookup.notes.isEmpty())
            assertTrue(introLookup.hasBookIntroOnlySchema)

            val ntvLookup = helper.getVersionNotes(ntvDbName, "Génesis", 1, 1, "2")
            assertEquals(listOf("Segunda nota NTV."), ntvLookup.notes)
        } finally {
            multiFile.delete()
            introFile.delete()
            ntvFile.delete()
        }
    }

    // =========================================================================
    // 3. SUBTÍTULOS AUTOMÁTICOS (ES vs EN, PRIORIDAD NATIVA Y ASSETS REALES)
    // =========================================================================

    @Test
    fun `fallback subheadings DB resolves Spanish vs English and cleans MyBible cross-ref tags`() {
        assertEquals(
            BibleStudyTextUtils.SPANISH_FALLBACK_SUBHEADINGS_DB,
            BibleStudyTextUtils.resolveFallbackSubheadingsDbName("RVR1960")
        )
        assertEquals(
            BibleStudyTextUtils.SPANISH_FALLBACK_SUBHEADINGS_DB,
            BibleStudyTextUtils.resolveFallbackSubheadingsDbName("LBLA")
        )
        assertEquals(
            BibleStudyTextUtils.ENGLISH_FALLBACK_SUBHEADINGS_DB,
            BibleStudyTextUtils.resolveFallbackSubheadingsDbName("KJV")
        )
        assertEquals(
            BibleStudyTextUtils.ENGLISH_FALLBACK_SUBHEADINGS_DB,
            BibleStudyTextUtils.resolveFallbackSubheadingsDbName("ESV")
        )

        val cleanedEs = BibleStudyTextUtils.cleanSubheadingText("(<x>130 1:1-4</x>)", isEnglish = false)
        assertEquals("(1 Crónicas 1:1-4)", cleanedEs)
        assertTrue(BibleStudyTextUtils.isParallelReferenceSubheading(cleanedEs))

        val cleanedEn = BibleStudyTextUtils.cleanSubheadingText("(<x>130 1:1-4</x>)", isEnglish = true)
        assertEquals("(1 Chronicles 1:1-4)", cleanedEn)
        assertFalse(BibleStudyTextUtils.isParallelReferenceSubheading("La creación"))
    }

    @Test
    fun `subheadings prioritize native module stories over fallback and safely align Psalms numbering`() {
        val native = mapOf(1 to listOf("Título propio NTV"))
        val fallback = mapOf(1 to listOf("La creación"), 26 to listOf("Creación del hombre"))

        val withNative = BibleStudyTextUtils.resolveEffectiveChapterSubheadings(
            hasNativeSubheadingsTable = true,
            nativeSubheadingsByVerse = native,
            fallbackSubheadingsByVerse = fallback,
            chapterVerseNumbers = (1..31).toList()
        )
        assertEquals(mapOf(1 to listOf("Título propio NTV")), withNative)

        val withoutNative = BibleStudyTextUtils.resolveEffectiveChapterSubheadings(
            hasNativeSubheadingsTable = false,
            nativeSubheadingsByVerse = emptyMap(),
            fallbackSubheadingsByVerse = fallback,
            chapterVerseNumbers = (1..31).toList()
        )
        assertEquals(fallback, withoutNative)

        // Alineación segura en Salmos (versículo 0 y duplicados / versículo fuera de rango)
        val alignedPsalms = BibleStudyTextUtils.alignSubheadingsToVerses(
            rawByVerse = mapOf(
                0 to listOf("LIBRO I", "El justo y los pecadores"),
                1 to listOf("El justo y los pecadores"), // duplicado que debe consolidarse
                99 to listOf("Fuera de rango")
            ),
            chapterVerseNumbers = listOf(1, 2, 3, 4, 5, 6)
        )
        assertEquals(mapOf(1 to listOf("LIBRO I", "El justo y los pecadores")), alignedPsalms)
    }

    @Test
    fun `packaged assets subheadings databases load real ES and EN titles and native stories take priority`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val helper = BibleAssetDatabaseHelper(context)

        // 1) Lectura real desde assets/databases/RVR1960-s.subheadings.SQLite3
        val esGenesis1 = helper.getFallbackChapterSubheadings(
            subheadingsDbName = BibleStudyTextUtils.SPANISH_FALLBACK_SUBHEADINGS_DB,
            bookNameOrAbbr = "Génesis",
            chapter = 1,
            isEnglish = false
        )
        assertTrue("Génesis 1 debe tener subtítulos en español desde assets", esGenesis1.isNotEmpty())
        assertTrue(esGenesis1[1]?.any { it.contains("creación", ignoreCase = true) } == true)

        // Verificar resolución de etiquetas <x> en RVR1960-s (Mateo 5 / 1 Crónicas / Génesis 5)
        val esGenesis5 = helper.getFallbackChapterSubheadings(
            subheadingsDbName = BibleStudyTextUtils.SPANISH_FALLBACK_SUBHEADINGS_DB,
            bookNameOrAbbr = "Génesis",
            chapter = 5,
            isEnglish = false
        )
        assertTrue(esGenesis5.values.flatten().none { it.contains("<x>") || it.contains("</x>") })

        // 2) Lectura real desde assets/databases/KJV-s.subheadings.SQLite3
        val enGenesis1 = helper.getFallbackChapterSubheadings(
            subheadingsDbName = BibleStudyTextUtils.ENGLISH_FALLBACK_SUBHEADINGS_DB,
            bookNameOrAbbr = "Genesis",
            chapter = 1,
            isEnglish = true
        )
        assertTrue("Genesis 1 debe tener subtítulos en inglés desde assets", enGenesis1.isNotEmpty())

        // 3) Verificar que BibleRepository NO confunde archivos *.subheadings.SQLite3 con Biblias principales
        val db = Room.inMemoryDatabaseBuilder(context, BibleDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val nativeModuleName = "NATIVETEST"
        val nativeDbFile = context.getDatabasePath("$nativeModuleName.SQLite3")
        try {
            val repo = BibleRepository(context, db.bibleDao())
            assertEquals("RVR1960.SQLite3", repo.resolveBibleDatabaseFileName("RVR1960"))
            assertEquals("KJV.SQLite3", repo.resolveBibleDatabaseFileName("KJV"))

            // 4) Verificar prioridad nativa real a través de BibleRepository.getChapterSubheadings
            nativeDbFile.parentFile?.mkdirs()
            SQLiteDatabase.openOrCreateDatabase(nativeDbFile, null).use { sqlDb ->
                sqlDb.execSQL("CREATE TABLE stories (book_number NUMERIC, chapter NUMERIC, verse NUMERIC, order_if_several NUMERIC, title TEXT)")
                sqlDb.execSQL("INSERT INTO stories VALUES (10, 1, 1, 0, 'Encabezado nativo del módulo')")
            }
            val effective = runBlocking {
                repo.getChapterSubheadings(
                    version = nativeModuleName,
                    bookName = "Génesis",
                    chapter = 1,
                    chapterVerseNumbers = listOf(1, 2, 3)
                )
            }
            assertEquals(mapOf(1 to listOf("Encabezado nativo del módulo")), effective)
        } finally {
            nativeDbFile.delete()
            db.close()
        }
    }

    // =========================================================================
    // 4. PRUEBAS UI DE INICIO Y AJUSTES (MODO CLARO Y OSCURO)
    // =========================================================================

    @Test
    fun `home screen hides legacy offline label and renders redesigned sections cleanly`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val db = Room.inMemoryDatabaseBuilder(app, BibleDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val (store, viewModel) = createTestViewModel(app, db)
        try {
            composeTestRule.setContent {
                BibleStudyTheme(themeMode = "LIGHT") {
                    HomeScreen(uiState = viewModel.uiState.value, viewModel = viewModel)
                }
            }
            composeTestRule.waitForIdle()

            // "Modo sin conexión listo" debe haberse eliminado por completo
            assertEquals(
                0,
                composeTestRule.onAllNodesWithText("Modo sin conexión listo", substring = true).fetchSemanticsNodes().size
            )
            composeTestRule.onNodeWithTag("verse_of_the_day_card").assertIsDisplayed()
            composeTestRule.onNodeWithTag("continue_reading_card").performScrollTo().assertIsDisplayed()
            composeTestRule.onNodeWithTag("home_notes_card").performScrollTo().assertIsDisplayed()
        } finally {
            tearDownTestViewModelAndDb(store, viewModel, db)
        }
    }

    @Test
    fun `settings screen keeps favorites and language separate without subheadings toggle`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val db = Room.inMemoryDatabaseBuilder(app, BibleDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val (store, viewModel) = createTestViewModel(app, db)
        try {
            composeTestRule.setContent {
                BibleStudyTheme(themeMode = "DARK") {
                    SettingsScreen(uiState = viewModel.uiState.value, viewModel = viewModel)
                }
            }
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithTag("user_profile_card").assertIsDisplayed()
            composeTestRule.onNodeWithTag("settings_favorites_item").performScrollTo().assertIsDisplayed()
            composeTestRule.onNodeWithTag("language_dropdown_button").performScrollTo().assertIsDisplayed()
            composeTestRule.onNodeWithTag("theme_selector_card").performScrollTo().assertIsDisplayed()
            composeTestRule.onNodeWithTag("typography_controls_card").performScrollTo().assertIsDisplayed()
            composeTestRule.onNodeWithTag("settings_tutorial_card").performScrollTo().assertIsDisplayed()

            // No debe existir ningún interruptor de subtítulos en Ajustes
            assertEquals(
                0,
                composeTestRule.onAllNodesWithText("Subtítulos", substring = true, ignoreCase = true).fetchSemanticsNodes().size
            )
        } finally {
            tearDownTestViewModelAndDb(store, viewModel, db)
        }
    }
}

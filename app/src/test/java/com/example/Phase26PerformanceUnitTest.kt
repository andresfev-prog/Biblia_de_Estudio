package com.example

import android.app.Application
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.BibleDatabase
import com.example.data.database.DatabaseInitializer
import com.example.data.repository.BibleAssetDatabaseHelper
import com.example.data.repository.BibleRepository
import com.example.data.repository.CloudSyncRepository
import com.example.data.repository.ModuleCatalogDiskCache
import com.example.ui.util.BibleStudyTextUtils
import com.example.ui.viewmodel.BibleViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import java.io.File
import kotlin.system.measureTimeMillis

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h915dp")
class Phase26PerformanceUnitTest {

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
            Thread.sleep(15)
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        return predicate()
    }

    private fun createPartialBibleDb(
        context: Context,
        versionTag: String,
        includeRevelation: Boolean
    ): File {
        val file = context.getDatabasePath("$versionTag.SQLite3")
        file.parentFile?.mkdirs()
        BibleAssetDatabaseHelper.closeAndEvictDatabase(file.name)
        if (file.exists()) file.delete()
        SQLiteDatabase.openOrCreateDatabase(file, null).use { sqlDb ->
            sqlDb.execSQL("CREATE TABLE books (book_number NUMERIC, short_name TEXT, long_name TEXT)")
            sqlDb.execSQL("CREATE TABLE verses (book_number NUMERIC, chapter NUMERIC, verse NUMERIC, text TEXT, PRIMARY KEY (book_number, chapter, verse))")
            sqlDb.execSQL("INSERT INTO books VALUES (230, 'Ps', 'Psalms')")
            sqlDb.execSQL("INSERT INTO books VALUES (500, 'Jn', 'John')")
            sqlDb.execSQL("INSERT INTO verses VALUES (230, 23, 1, 'Yahweh is my best friend and my shepherd.')")
            sqlDb.execSQL("INSERT INTO verses VALUES (500, 1, 1, 'In the very beginning the Living Expression was already there.')")
            if (includeRevelation) {
                sqlDb.execSQL("INSERT INTO books VALUES (730, 'Rev', 'Revelation')")
                sqlDb.execSQL("INSERT INTO verses VALUES (730, 22, 21, 'May the grace of the Lord Jesus be with you all.')")
            }
        }
        return file
    }

    @Test
    fun `chapter loading with subheadings and RVR note check completes in milliseconds`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, BibleDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val repo = BibleRepository(context, db.bibleDao())

            // Pre-extraer assets base una vez (simulando arranque normal tras primera instalación)
            BibleAssetDatabaseHelper.ensureDatabaseExtracted(context, "RVR1960.SQLite3")
            BibleAssetDatabaseHelper.ensureDatabaseExtracted(context, "KJV.SQLite3")
            BibleAssetDatabaseHelper.ensureDatabaseExtracted(context, "RVR1960-s.subheadings.SQLite3")
            BibleAssetDatabaseHelper.ensureDatabaseExtracted(context, "KJV-s.subheadings.SQLite3")

            // Calentar pool de conexiones
            runBlocking {
                repo.getChapterVersesOnce("RVR1960", "Juan", 1)
                repo.getChapterVersesOnce("KJV", "Juan", 1)
            }

            var rvrGen4Count = 0
            var rvrSubheadingCount = 0
            var gen4Verse3Text = ""
            val rvrElapsedMs = measureTimeMillis {
                runBlocking {
                    val notes = repo.getVersesWithRealVersionNotes("RVR1960", "Génesis", 4)
                    val verses = repo.getChapterVersesOnce("RVR1960", "Génesis", 4, rvrNotesVerses = notes)
                    val headings = repo.getChapterSubheadings("RVR1960", "Génesis", 4, verses.map { it.verse })
                    rvrGen4Count = verses.size
                    rvrSubheadingCount = headings.size
                    gen4Verse3Text = verses.first { it.verse == 3 }.text
                }
            }

            var kjvJohn1Count = 0
            var kjvSubheadingCount = 0
            val kjvElapsedMs = measureTimeMillis {
                runBlocking {
                    val verses = repo.getChapterVersesOnce("KJV", "Juan", 1)
                    val headings = repo.getChapterSubheadings("KJV", "Juan", 1, verses.map { it.verse })
                    kjvJohn1Count = verses.size
                    kjvSubheadingCount = headings.size
                }
            }

            assertTrue("RVR1960 Génesis 4 debe cargarse con versículos reales", rvrGen4Count >= 20)
            assertTrue("RVR1960 Génesis 4 debe resolver subtítulos automáticos", rvrSubheadingCount > 0)
            assertFalse("Sin RVR-C instalado, Génesis 4:3 no debe mostrar '*'", gen4Verse3Text.contains('*'))
            assertTrue("KJV Juan 1 debe cargarse con versículos reales", kjvJohn1Count >= 40)
            assertTrue("KJV Juan 1 debe resolver subtítulos automáticos", kjvSubheadingCount > 0)
            assertTrue(
                "La carga de capítulo RVR1960 + subtítulos + notas debe tardar milisegundos (fue ${rvrElapsedMs} ms)",
                rvrElapsedMs < 250L
            )
            assertTrue(
                "La carga de capítulo KJV + subtítulos debe tardar milisegundos (fue ${kjvElapsedMs} ms)",
                kjvElapsedMs < 250L
            )
        } finally {
            db.close()
        }
    }

    @Test
    fun `module catalog uses covering index and persistent disk cache avoids SQLite rescans on restart`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, BibleDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val diskCache = ModuleCatalogDiskCache(context)
        val dynamicTag = "PERFDISKTEST"
        var dynamicFile: File? = null
        try {
            BibleAssetDatabaseHelper.ensureDatabaseExtracted(context, "RVR1960.SQLite3")
            diskCache.invalidate()
            BibleAssetDatabaseHelper.catalogSqliteScanCount.set(0)

            // 1) Primera carga en frío de RVR1960 (escaneo rápido vía índice cubriente)
            val repoCold = BibleRepository(context, db.bibleDao())
            lateinit var coldCatalog: com.example.data.model.ModuleBibleCatalog
            val coldMs = measureTimeMillis {
                coldCatalog = runBlocking { repoCold.getModuleCatalog("RVR1960") }
            }
            assertEquals(66, coldCatalog.availableBooks.size)
            assertEquals(1189, coldCatalog.chaptersByBookNumber.values.sumOf { it.size })
            assertEquals(50, coldCatalog.getAvailableChapters("Génesis").size)
            assertEquals(40, coldCatalog.getAvailableChapters("Éxodo").size)
            assertEquals(27, coldCatalog.getAvailableChapters("Levítico").size)
            assertEquals(36, coldCatalog.getAvailableChapters("Números").size)
            assertEquals(34, coldCatalog.getAvailableChapters("Deuteronomio").size)
            assertEquals(24, coldCatalog.getAvailableChapters("Josué").size)
            assertEquals(22, coldCatalog.getAvailableChapters("Apocalipsis").size)
            assertEquals("Debe ejecutar exactamente 1 escaneo SQLite en frío", 1, BibleAssetDatabaseHelper.catalogSqliteScanCount.get())
            assertTrue("El escaneo con índice cubriente debe completar en milisegundos (fue ${coldMs} ms)", coldMs < 500L)

            // 2) Simular reinicio completo de app con una nueva instancia de BibleRepository (caché RAM vacía)
            val repoRestart = BibleRepository(context, db.bibleDao())
            lateinit var warmDiskCatalog: com.example.data.model.ModuleBibleCatalog
            val diskHitMs = measureTimeMillis {
                warmDiskCatalog = runBlocking { repoRestart.getModuleCatalog("RVR1960") }
            }
            assertEquals(66, warmDiskCatalog.availableBooks.size)
            assertEquals(1189, warmDiskCatalog.chaptersByBookNumber.values.sumOf { it.size })
            assertEquals(
                "Al reiniciar con caché de disco persistente NO debe re-escanear SQLite",
                1,
                BibleAssetDatabaseHelper.catalogSqliteScanCount.get()
            )
            assertTrue("El hit de caché en disco debe ser casi instantáneo (fue ${diskHitMs} ms)", diskHitMs < 100L)

            // 3) Invalidación automática si el archivo .SQLite3 cambia en tamaño o fecha de modificación
            dynamicFile = createPartialBibleDb(context, dynamicTag, includeRevelation = false)
            val repoMod1 = BibleRepository(context, db.bibleDao())
            val cat1 = runBlocking { repoMod1.getModuleCatalog(dynamicTag) }
            assertEquals(2, cat1.availableBooks.size)
            assertEquals(2, BibleAssetDatabaseHelper.catalogSqliteScanCount.get())

            // Nueva instancia sin cambios en archivo -> hit en disco (0 escaneos adicionales)
            val repoMod2 = BibleRepository(context, db.bibleDao())
            val cat2 = runBlocking { repoMod2.getModuleCatalog(dynamicTag) }
            assertEquals(2, cat2.availableBooks.size)
            assertEquals(2, BibleAssetDatabaseHelper.catalogSqliteScanCount.get())

            // Reemplazar archivo con un libro adicional -> la huella cambia y se re-escanea automáticamente
            dynamicFile = createPartialBibleDb(context, dynamicTag, includeRevelation = true)
            dynamicFile.setLastModified(System.currentTimeMillis() + 5_000L)
            val repoMod3 = BibleRepository(context, db.bibleDao())
            val cat3 = runBlocking { repoMod3.getModuleCatalog(dynamicTag) }
            assertEquals(3, cat3.availableBooks.size)
            assertEquals(3, BibleAssetDatabaseHelper.catalogSqliteScanCount.get())
        } finally {
            dynamicFile?.let {
                BibleAssetDatabaseHelper.closeAndEvictDatabase(it.name)
                it.delete()
            }
            diskCache.invalidate("$dynamicTag.SQLite3")
            db.close()
        }
    }

    @Test
    fun `switching from RVR1960 to partial version TPT atomically clears stale verses before IO finishes`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val db = Room.inMemoryDatabaseBuilder(app, BibleDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val partialTag = "TPT"
        val partialFile = createPartialBibleDb(app, partialTag, includeRevelation = true)
        val (store, viewModel) = createTestViewModel(app, db)
        try {
            // 1) Cargar Jeremías 29 en RVR1960 (pasaje existente en español)
            viewModel.loadChapter("Jeremías", 29, version = "RVR1960")
            val rvrLoaded = waitUntil {
                !viewModel.uiState.value.isChapterLoading &&
                    viewModel.uiState.value.activeVersion == "RVR1960" &&
                    viewModel.uiState.value.currentBook == "Jeremías" &&
                    viewModel.uiState.value.currentChapter == 29 &&
                    viewModel.uiState.value.verses.isNotEmpty()
            }
            assertTrue("Jeremías 29 debe cargarse en RVR1960", rvrLoaded)
            assertTrue(
                "Los tokens precalculados deben estar disponibles para cada versículo",
                viewModel.uiState.value.verseTokens.size == viewModel.uiState.value.verses.size
            )

            // 2) Cambiar a versión parcial TPT (que NO contiene Jeremías)
            viewModel.setActiveVersion(partialTag)

            // Verificación SÍNCRONA inmediata (antes de completar I/O): cero destello de texto RVR1960 bajo TPT
            val immediateState = viewModel.uiState.value
            assertEquals(partialTag, immediateState.activeVersion)
            assertTrue(
                "Al cambiar a TPT, los versículos previos de RVR1960 deben limpiarse de inmediato",
                immediateState.verses.isEmpty()
            )
            assertTrue(
                "Los subtítulos previos de RVR1960 deben limpiarse atómicamente al cambiar de versión",
                immediateState.chapterSubheadings.isEmpty()
            )
            assertTrue(
                "Los tokens previos deben limpiarse atómicamente al cambiar de versión",
                immediateState.verseTokens.isEmpty()
            )
            assertTrue(
                "isChapterLoading debe activarse de inmediato durante la transición",
                immediateState.isChapterLoading
            )

            // 3) Al finalizar I/O debe asentarse en estado vacío limpio de TPT
            val tptSettled = waitUntil {
                !viewModel.uiState.value.isChapterLoading &&
                    viewModel.uiState.value.activeVersion == partialTag
            }
            assertTrue(tptSettled)
            assertTrue(viewModel.uiState.value.verses.isEmpty())
            assertTrue(viewModel.uiState.value.chapterSubheadings.isEmpty())

            // 4) Cambios rápidos consecutivos cancelan tareas previas y se asientan en la última versión
            viewModel.loadChapter("Juan", 1, version = "RVR1960")
            viewModel.setActiveVersion("KJV")
            viewModel.setActiveVersion(partialTag)
            viewModel.setActiveVersion("RVR1960")

            val settledFinal = waitUntil {
                !viewModel.uiState.value.isChapterLoading &&
                    viewModel.uiState.value.activeVersion == "RVR1960" &&
                    viewModel.uiState.value.currentBook == "Juan" &&
                    viewModel.uiState.value.currentChapter == 1 &&
                    viewModel.uiState.value.verses.isNotEmpty()
            }
            assertTrue("Tras cambios rápidos debe asentarse limpiamente en RVR1960 Juan 1", settledFinal)

            val firstVerse = viewModel.uiState.value.verses.first()
            val expectedTokens = BibleStudyTextUtils.tokenizeVerseText(firstVerse.text, isRvr1960 = false)
            assertEquals(expectedTokens, viewModel.uiState.value.verseTokens[firstVerse.id])
        } finally {
            BibleAssetDatabaseHelper.closeAndEvictDatabase(partialFile.name)
            partialFile.delete()
            tearDownTestViewModelAndDb(store, viewModel, db)
        }
    }

    @Test
    fun `DatabaseInitializer completes immediately without blocking Room with dead 31k verse insert`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, BibleDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val elapsedMs = measureTimeMillis {
                runBlocking {
                    DatabaseInitializer.populateInitialData(context, db.bibleDao())
                }
            }
            val downloadedMods = runBlocking { db.bibleDao().getDownloadedModules() }
            val verseRowsInRoom = runBlocking { db.bibleDao().getVerseCount() }

            assertNotNull(downloadedMods.firstOrNull { it.id == "mod_rvr1960" })
            assertNotNull(downloadedMods.firstOrNull { it.id == "mod_kjv" })
            assertEquals("No debe insertar 31 102 versículos obsoletos en Room", 0, verseRowsInRoom)
            assertTrue("DatabaseInitializer debe completar casi al instante (fue ${elapsedMs} ms)", elapsedMs < 500L)
        } finally {
            db.close()
        }
    }
}

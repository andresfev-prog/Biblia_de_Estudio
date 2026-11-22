package com.example

import android.app.Application
import android.os.Looper
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
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
import com.example.data.repository.BibleRepository
import com.example.data.repository.CloudSyncRepository
import com.example.data.repository.SyncStatus
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.theme.AppThemeIds
import com.example.ui.theme.BibleStudyTheme
import com.example.ui.theme.resolveAppColors
import com.example.ui.viewmodel.BibleViewModel
import kotlinx.coroutines.Job
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h915dp")
class Phase27ThemeHarmonizationUnitTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun contrastRatio(foreground: Color, background: Color): Double {
        val fg = if (foreground.alpha < 1f) foreground.compositeOver(background) else foreground
        val l1 = maxOf(fg.luminance().toDouble(), background.luminance().toDouble())
        val l2 = minOf(fg.luminance().toDouble(), background.luminance().toDouble())
        return (l1 + 0.05) / (l2 + 0.05)
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
    fun `all six themes resolve exact harmonized hero palettes and never stay frozen on classic blue`() {
        val crisp = resolveAppColors(themeMode = "LIGHT", selectedLightTheme = AppThemeIds.CRISP_LIGHT)
        val parchment = resolveAppColors(themeMode = "LIGHT", selectedLightTheme = AppThemeIds.WARM_PARCHMENT)
        val olive = resolveAppColors(themeMode = "LIGHT", selectedLightTheme = AppThemeIds.SERENE_OLIVE)
        val midnight = resolveAppColors(themeMode = "DARK", selectedDarkTheme = AppThemeIds.MIDNIGHT_BLUE)
        val deepDark = resolveAppColors(themeMode = "DARK", selectedDarkTheme = AppThemeIds.DEEP_DARK)
        val obsidian = resolveAppColors(themeMode = "DARK", selectedDarkTheme = AppThemeIds.OBSIDIAN_ONYX)

        // 1. Claro Clásico (CRISP_LIGHT)
        assertEquals(Color(0xFF0F1D33), crisp.heroCard.gradientStart)
        assertEquals(Color(0xFF08111E), crisp.heroCard.gradientEnd)
        assertEquals(Color(0xFF1E3354), crisp.heroCard.pillBg)

        // 2. Claro Pergamino (WARM_PARCHMENT)
        assertEquals(Color(0xFF3B281E), parchment.heroCard.gradientStart)
        assertEquals(Color(0xFF241711), parchment.heroCard.gradientEnd)
        assertEquals(Color(0xFF543A2C), parchment.heroCard.pillBg)
        assertEquals(Color(0xFFFAF6F0), parchment.heroCard.textPrimary)

        // 3. Claro Oliva (SERENE_OLIVE)
        assertEquals(Color(0xFF1B3527), olive.heroCard.gradientStart)
        assertEquals(Color(0xFF0F2117), olive.heroCard.gradientEnd)
        assertEquals(Color(0xFF2B4F3B), olive.heroCard.pillBg)
        assertEquals(Color(0xFFF4F9F6), olive.heroCard.textPrimary)

        // 4. Oscuro Clásico (MIDNIGHT_BLUE)
        assertEquals(Color(0xFF0F1D33), midnight.heroCard.gradientStart)
        assertEquals(Color(0xFF08111E), midnight.heroCard.gradientEnd)
        assertEquals(Color(0xFF1E3354), midnight.heroCard.pillBg)
        assertEquals(Color(0xFF263650), midnight.heroCard.border)

        // 5. Oscuro Neutro / Deep Dark (DEEP_DARK - acento blanco/plata)
        assertEquals(Color(0xFF1F2225), deepDark.heroCard.gradientStart)
        assertEquals(Color(0xFF141618), deepDark.heroCard.gradientEnd)
        assertEquals(Color(0xFF30343A), deepDark.heroCard.border)
        assertEquals(Color(0xFF2E3338), deepDark.heroCard.pillBg)

        // 6. Oscuro Obsidiana (OBSIDIAN_ONYX - acento dorado)
        assertEquals(Color(0xFF1B1710), obsidian.heroCard.gradientStart)
        assertEquals(Color(0xFF0F0D09), obsidian.heroCard.gradientEnd)
        assertEquals(Color(0xFF3D3119), obsidian.heroCard.border)
        assertEquals(Color(0xFF2E2514), obsidian.heroCard.pillBg)
        assertEquals(Color(0xFFE5B84B), obsidian.heroCard.accentDetail)

        // Ninguno de los 4 temas alternativos puede quedarse congelado en azul clásico
        val classicBlueStart = Color(0xFF0F1D33)
        listOf(parchment, olive, deepDark, obsidian).forEach { palette ->
            assertNotEquals(
                "El tema ${palette.themeId} no debe permanecer en azul clásico",
                classicBlueStart,
                palette.heroCard.gradientStart
            )
            assertNotEquals(
                "La píldora del tema ${palette.themeId} no debe permanecer en azul clásico",
                Color(0xFF1E3354),
                palette.heroCard.pillBg
            )
        }

        // Los 5 grupos cromáticos deben tener fondos degradados distintos entre sí
        val distinctStarts = setOf(
            crisp.heroCard.gradientStart,
            parchment.heroCard.gradientStart,
            olive.heroCard.gradientStart,
            deepDark.heroCard.gradientStart,
            obsidian.heroCard.gradientStart
        )
        assertEquals(5, distinctStarts.size)
    }

    @Test
    fun `all six hero card palettes maintain high WCAG contrast and translucent neutral controls`() {
        val allThemes = listOf(
            resolveAppColors("LIGHT", selectedLightTheme = AppThemeIds.CRISP_LIGHT),
            resolveAppColors("LIGHT", selectedLightTheme = AppThemeIds.WARM_PARCHMENT),
            resolveAppColors("LIGHT", selectedLightTheme = AppThemeIds.SERENE_OLIVE),
            resolveAppColors("DARK", selectedDarkTheme = AppThemeIds.MIDNIGHT_BLUE),
            resolveAppColors("DARK", selectedDarkTheme = AppThemeIds.DEEP_DARK),
            resolveAppColors("DARK", selectedDarkTheme = AppThemeIds.OBSIDIAN_ONYX)
        )

        for (palette in allThemes) {
            val hero = palette.heroCard

            val primaryOnStart = contrastRatio(hero.textPrimary, hero.gradientStart)
            val primaryOnEnd = contrastRatio(hero.textPrimary, hero.gradientEnd)
            val secondaryOnStart = contrastRatio(hero.textSecondary, hero.gradientStart)
            val pillContrast = contrastRatio(hero.pillText, hero.pillBg)
            val controlBgComposite = hero.controlContainer.compositeOver(hero.gradientStart)
            val controlIconContrast = contrastRatio(hero.controlContent, controlBgComposite)
            val badgeBgComposite = hero.badgeBg.compositeOver(hero.gradientStart)
            val badgeContrast = contrastRatio(hero.badgeText, badgeBgComposite)

            assertTrue("Texto principal en ${palette.themeId} debe superar 10:1 (fue $primaryOnStart)", primaryOnStart >= 10.0)
            assertTrue("Texto principal en ${palette.themeId} (fondo) debe superar 12:1 (fue $primaryOnEnd)", primaryOnEnd >= 12.0)
            assertTrue("Texto secundario en ${palette.themeId} debe superar 7:1 (fue $secondaryOnStart)", secondaryOnStart >= 7.0)
            assertTrue("Botón píldora en ${palette.themeId} debe superar 7:1 (fue $pillContrast)", pillContrast >= 7.0)
            assertTrue("Ícono de control (lápiz/avatar) en ${palette.themeId} debe superar 7:1 (fue $controlIconContrast)", controlIconContrast >= 7.0)
            assertTrue("Insignia versión en ${palette.themeId} debe superar 6:1 (fue $badgeContrast)", badgeContrast >= 6.0)
            assertTrue(
                "El fondo circular del botón lápiz/avatar en ${palette.themeId} debe ser translúcido discreto",
                hero.controlContainer.alpha in 0.08f..0.22f
            )
        }
    }

    @Test
    fun `home and settings hero cards render cleanly in guest and signed-in states with preserved profile structure`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val db = Room.inMemoryDatabaseBuilder(app, BibleDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val (store, viewModel) = createTestViewModel(app, db)
        try {
            // 1) HomeScreen en Claro Pergamino
            composeTestRule.setContent {
                BibleStudyTheme(themeMode = "LIGHT", selectedLightTheme = AppThemeIds.WARM_PARCHMENT) {
                    HomeScreen(uiState = viewModel.uiState.value, viewModel = viewModel)
                }
            }
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag("verse_of_the_day_card").assertIsDisplayed()
            composeTestRule.onNodeWithTag("read_full_chapter_button").assertIsDisplayed()
            composeTestRule.onNodeWithTag("votd_favorite_button").assertIsDisplayed()
            composeTestRule.onNodeWithTag("votd_copy_button").assertIsDisplayed()
            composeTestRule.onNodeWithTag("votd_share_button").assertIsDisplayed()
        } finally {
            tearDownTestViewModelAndDb(store, viewModel, db)
        }
    }

    @Test
    fun `settings profile card keeps single-row structure and separate favorites and language cards in guest mode`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val db = Room.inMemoryDatabaseBuilder(app, BibleDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val (store, viewModel) = createTestViewModel(app, db)
        try {
            composeTestRule.setContent {
                BibleStudyTheme(themeMode = "LIGHT", selectedLightTheme = AppThemeIds.SERENE_OLIVE) {
                    SettingsScreen(uiState = viewModel.uiState.value, viewModel = viewModel)
                }
            }
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithTag("user_profile_card").assertIsDisplayed()
            composeTestRule.onNodeWithTag("edit_profile_button").assertIsDisplayed()
            composeTestRule.onNodeWithText("Invitado").assertIsDisplayed()
            // Favoritos e Idioma siguen siendo tarjetas separadas
            composeTestRule.onNodeWithTag("settings_favorites_item").assertIsDisplayed()
            composeTestRule.onNodeWithTag("language_dropdown_button").assertIsDisplayed()
            // Perfil NO debe incluir botones de Versículo del día
            assertEquals(0, composeTestRule.onAllNodesWithTag("read_full_chapter_button").fetchSemanticsNodes().size)
            assertEquals(0, composeTestRule.onAllNodesWithTag("votd_copy_button").fetchSemanticsNodes().size)
            assertEquals(0, composeTestRule.onAllNodesWithTag("votd_share_button").fetchSemanticsNodes().size)
        } finally {
            tearDownTestViewModelAndDb(store, viewModel, db)
        }
    }

    @Test
    fun `settings profile card renders signed-in google user cleanly under obsidian dark theme`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val db = Room.inMemoryDatabaseBuilder(app, BibleDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val (store, viewModel) = createTestViewModel(app, db)
        try {
            val signedInState = viewModel.uiState.value.copy(
                googleUid = "uid_test_123",
                googleDisplayName = "Andrés Felipe",
                googleEmail = "andres@example.com",
                syncStatus = SyncStatus.SYNCED
            )
            composeTestRule.setContent {
                BibleStudyTheme(themeMode = "DARK", selectedDarkTheme = AppThemeIds.OBSIDIAN_ONYX) {
                    SettingsScreen(uiState = signedInState, viewModel = viewModel)
                }
            }
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithTag("user_profile_card").assertIsDisplayed()
            composeTestRule.onNodeWithText("Andrés Felipe").assertIsDisplayed()
            composeTestRule.onNodeWithText("andres@example.com").assertIsDisplayed()
            composeTestRule.onNodeWithText("AF").assertIsDisplayed()
            composeTestRule.onNodeWithTag("edit_profile_button").assertIsDisplayed()
        } finally {
            tearDownTestViewModelAndDb(store, viewModel, db)
        }
    }
}

package com.example.ui.theme

import android.content.Context
import android.content.SharedPreferences

class ThemePreferenceCache(context: Context) {

    private val prefs: SharedPreferences = (context.applicationContext ?: context)
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadInitialTheme(): Triple<String, String, String> {
        val rawMode = prefs.getString(KEY_THEME_MODE, DEFAULT_THEME_MODE).orEmpty()
        val mode = normalizeThemeMode(rawMode)
        val lightTheme = prefs.getString(KEY_SELECTED_LIGHT_THEME, DEFAULT_LIGHT_THEME)
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: DEFAULT_LIGHT_THEME
        val darkTheme = prefs.getString(KEY_SELECTED_DARK_THEME, DEFAULT_DARK_THEME)
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: DEFAULT_DARK_THEME
        return Triple(mode, lightTheme, darkTheme)
    }

    fun saveTheme(
        themeMode: String,
        selectedLightTheme: String,
        selectedDarkTheme: String
    ) {
        val normalizedMode = normalizeThemeMode(themeMode)
        val normalizedLight = selectedLightTheme.trim().ifBlank { DEFAULT_LIGHT_THEME }
        val normalizedDark = selectedDarkTheme.trim().ifBlank { DEFAULT_DARK_THEME }

        prefs.edit()
            .putString(KEY_THEME_MODE, normalizedMode)
            .putString(KEY_SELECTED_LIGHT_THEME, normalizedLight)
            .putString(KEY_SELECTED_DARK_THEME, normalizedDark)
            .apply()
    }

    companion object {
        const val PREFS_NAME = "biblia_theme_cache"
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_SELECTED_LIGHT_THEME = "selected_light_theme"
        const val KEY_SELECTED_DARK_THEME = "selected_dark_theme"

        const val DEFAULT_THEME_MODE = "AUTO"
        const val DEFAULT_LIGHT_THEME = AppThemeIds.CRISP_LIGHT
        const val DEFAULT_DARK_THEME = AppThemeIds.MIDNIGHT_BLUE

        internal fun normalizeThemeMode(raw: String): String {
            return when (raw.trim().uppercase()) {
                "LIGHT" -> "LIGHT"
                "DARK" -> "DARK"
                "", "AUTO", "AUTOMATIC" -> DEFAULT_THEME_MODE
                else -> DEFAULT_THEME_MODE
            }
        }
    }
}

package com.example.ui.screens.settings

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.favorites.FavoritesDialog
import com.example.ui.theme.AppTheme
import com.example.ui.util.StringsLocale
import com.example.ui.viewmodel.BibleUiState
import com.example.ui.viewmodel.BibleViewModel

@Composable
fun SettingsScreen(
    uiState: BibleUiState,
    viewModel: BibleViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var languageMenuOpen by remember { mutableStateOf(false) }
    var fontMenuOpen by remember { mutableStateOf(false) }
    val lang = uiState.userPreferences.interfaceLanguage
    val appColors = AppTheme.colors

    val isEn = lang.contains("English", ignoreCase = true)
    // Opciones: English y Spanish en inglés, o Español e Inglés en español
    val languages = if (isEn) listOf("English", "Spanish") else listOf("Español", "Inglés")
    val fontFamilies = listOf("Untitled Serif", "Aktiv Grotesk", "Serif", "Monospace")

    val currentLanguageDisplay = if (isEn) "English" else "Español"

    // Dynamic initials from user name
    val initials = remember(uiState.userPreferences.userName) {
        val parts = uiState.userPreferences.userName.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
        if (parts.size >= 2) {
            "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
        } else if (parts.isNotEmpty()) {
            parts[0].take(2).uppercase()
        } else {
            "AF"
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(appColors.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("settings_screen")
    ) {
        Text(
            text = StringsLocale.get("settings_title", lang),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = appColors.titleColor,
            maxLines = 1,
            softWrap = false
        )
        Text(
            text = StringsLocale.get("settings_subtitle", lang),
            style = MaterialTheme.typography.bodySmall,
            color = appColors.textSecondary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 1. User Profile Card (Interactive, opens Edit Profile dialog)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .clickable { viewModel.showEditProfileDialog(true) }
                .testTag("user_profile_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = appColors.cardBg),
            border = if (appColors.isDark) BorderStroke(1.dp, appColors.border) else null,
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar with dynamic Initials
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(if (appColors.isDark) appColors.accent else appColors.titleColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        color = if (appColors.isDark) appColors.background else appColors.surface,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = uiState.userPreferences.userName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = appColors.titleColor,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = "@${uiState.userPreferences.usernameHandle}",
                        style = MaterialTheme.typography.bodySmall,
                        color = appColors.textSecondary,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        softWrap = false
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = appColors.accent,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = uiState.userPreferences.location,
                            style = MaterialTheme.typography.labelMedium,
                            color = appColors.accent,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                // Edit Profile Icon Button
                IconButton(
                    onClick = { viewModel.showEditProfileDialog(true) },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(appColors.surfaceVariant)
                        .testTag("edit_profile_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = StringsLocale.get("edit_profile", lang),
                        tint = appColors.accent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // "Favoritos" item in Settings (situated above modules and preferences)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .clickable { viewModel.showFavoritesDialog(true) }
                .testTag("settings_favorites_item"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = appColors.cardBg),
            border = if (appColors.isDark) BorderStroke(1.dp, appColors.border) else null,
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(appColors.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                            tint = appColors.accent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = StringsLocale.get("favorites_option", lang),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = appColors.titleColor,
                            maxLines = 1,
                            softWrap = false
                        )
                        Text(
                            text = String.format(StringsLocale.get("saved_favorites_count", lang), uiState.favorites.size),
                            style = MaterialTheme.typography.bodySmall,
                            color = appColors.textSecondary,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = appColors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Interface Language Dropdown (Requirement 5: Only Español & English)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = appColors.cardBg),
            border = if (appColors.isDark) BorderStroke(1.dp, appColors.border) else null,
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Language,
                            contentDescription = null,
                            tint = appColors.accent
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = StringsLocale.get("interface_language", lang),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = appColors.titleColor,
                                maxLines = 1,
                                softWrap = false
                            )
                            Text(
                                text = StringsLocale.get("interface_language_sub", lang),
                                style = MaterialTheme.typography.bodySmall,
                                color = appColors.textSecondary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box {
                        Surface(
                            shape = CircleShape,
                            color = appColors.pillBg,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { languageMenuOpen = true }
                                .testTag("language_dropdown_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = currentLanguageDisplay,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = appColors.pillText,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Filled.ArrowDropDown,
                                    contentDescription = null,
                                    tint = appColors.pillText
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = languageMenuOpen,
                            onDismissRequest = { languageMenuOpen = false },
                            shape = RoundedCornerShape(16.dp),
                            containerColor = MaterialTheme.colorScheme.background,
                            tonalElevation = 0.dp,
                            shadowElevation = 8.dp,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                        ) {
                            languages.forEach { languageName ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = languageName,
                                            fontWeight = if (currentLanguageDisplay == languageName) FontWeight.Bold else FontWeight.Normal,
                                            color = if (currentLanguageDisplay == languageName) appColors.accent else appColors.textPrimary,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    },
                                    onClick = {
                                        val targetLanguage = when (languageName) {
                                            "Spanish" -> "Español"
                                            "English", "Inglés" -> "English"
                                            else -> languageName
                                        }
                                        viewModel.updateInterfaceLanguage(targetLanguage)
                                        languageMenuOpen = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Theme Selector (Claro, Oscuro, Automático) - Requirement 6
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("theme_selector_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = appColors.cardBg),
            border = if (appColors.isDark) BorderStroke(1.dp, appColors.border) else null,
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Palette,
                        contentDescription = null,
                        tint = appColors.accent
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = StringsLocale.get("app_theme", lang),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = appColors.titleColor,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val themes = listOf(
                        Triple("LIGHT", StringsLocale.get("theme_light", lang), Icons.Filled.LightMode),
                        Triple("DARK", StringsLocale.get("theme_dark", lang), Icons.Filled.DarkMode),
                        Triple("AUTOMATIC", StringsLocale.get("theme_auto", lang), Icons.Filled.SettingsBrightness)
                    )

                    themes.forEach { (modeKey, modeName, icon) ->
                        val isSelected = uiState.userPreferences.themeMode == modeKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(50))
                                .background(if (isSelected) appColors.pillBg else appColors.surfaceVariant)
                                .clickable { viewModel.updateThemeMode(modeKey) }
                                .padding(vertical = 12.dp)
                                .testTag("theme_button_$modeKey"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) appColors.pillText else appColors.textSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = modeName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) appColors.pillText else appColors.textPrimary,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Typography Controls (Font Size Slider 8 pt to 20 pt)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("typography_controls_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = appColors.cardBg),
            border = if (appColors.isDark) BorderStroke(1.dp, appColors.border) else null,
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FormatColorText,
                            contentDescription = null,
                            tint = appColors.accent
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        val isEn = lang.contains("English", ignoreCase = true)
                        Text(
                            text = StringsLocale.get("typography_title", lang),
                            style = if (isEn) MaterialTheme.typography.titleSmall.copy(fontSize = 12.5.sp) else MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                            fontWeight = FontWeight.Bold,
                            color = appColors.titleColor,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Box {
                        Surface(
                            shape = CircleShape,
                            color = appColors.pillBg,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { fontMenuOpen = true }
                                .testTag("font_family_dropdown_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = uiState.userPreferences.fontFamily,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = appColors.pillText,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Icon(
                                    imageVector = Icons.Filled.ArrowDropDown,
                                    contentDescription = null,
                                    tint = appColors.pillText
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = fontMenuOpen,
                            onDismissRequest = { fontMenuOpen = false },
                            shape = RoundedCornerShape(16.dp),
                            containerColor = MaterialTheme.colorScheme.background,
                            tonalElevation = 0.dp,
                            shadowElevation = 8.dp,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                        ) {
                            fontFamilies.forEach { font ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = font,
                                            fontWeight = if (uiState.userPreferences.fontFamily == font) FontWeight.Bold else FontWeight.Normal,
                                            color = if (uiState.userPreferences.fontFamily == font) appColors.accent else appColors.textPrimary,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    },
                                    onClick = {
                                        viewModel.updateFontFamily(font)
                                        Log.i("BIBLE_DEBUG", "Fuente seleccionada en Ajustes: $font")
                                        fontMenuOpen = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Font Size Slider 8 pt to 20 pt
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${StringsLocale.get("font_size", lang)}: ${uiState.userPreferences.fontSizePt.toInt()} pt",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = appColors.textPrimary,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = StringsLocale.get("font_range", lang),
                        style = MaterialTheme.typography.labelSmall,
                        color = appColors.textSecondary,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Slider(
                    value = uiState.userPreferences.fontSizePt,
                    onValueChange = { viewModel.updateFontSizePt(it) },
                    valueRange = 8f..20f,
                    steps = 11,
                    colors = SliderDefaults.colors(
                        thumbColor = appColors.accent,
                        activeTrackColor = appColors.accent,
                        inactiveTrackColor = appColors.border
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("font_size_slider")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Live Preview
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = appColors.surfaceVariant,
                    border = BorderStroke(1.dp, appColors.border),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = StringsLocale.get("reader_preview", lang),
                            style = MaterialTheme.typography.labelSmall,
                            color = appColors.accent,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val sampleText = if (isEn) {
                            "For God so loved the world, that he gave his only begotten Son, that whosoever believeth in him should not perish, but have everlasting life. — John 3:16"
                        } else {
                            "1 En el principio era el Verbo, y el Verbo era con Dios, y el Verbo era Dios."
                        }
                        Text(
                            text = sampleText,
                            fontSize = (uiState.userPreferences.fontSizePt * 1.35f).sp,
                            lineHeight = (uiState.userPreferences.fontSizePt * 1.9f).sp,
                            fontFamily = resolveBibleFont(uiState.userPreferences.fontFamily), // <-- Vinculación de fuente en vivo
                            color = appColors.textPrimary,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // =========================================================================
        // BOTÓN CERRAR SESIÓN (Sin botones redundantes intermedios)
        // =========================================================================
        OutlinedButton(
            onClick = {
                viewModel.showLogoutDialog(true)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("logout_button"),
            shape = CircleShape,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = StringsLocale.get("logout", lang),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = StringsLocale.get("logout", lang),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                softWrap = false
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Favorites Dialog
        if (uiState.showFavoritesDialog) {
            FavoritesDialog(
                uiState = uiState,
                viewModel = viewModel,
                onDismiss = { viewModel.showFavoritesDialog(false) }
            )
        }

        // Logout Confirmation Dialog
        if (uiState.showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.showLogoutDialog(false) },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.ExitToApp,
                        contentDescription = null,
                        tint = appColors.accent
                    )
                },
                title = {
                    Text(
                        text = StringsLocale.get("logout_confirm_title", lang),
                        fontWeight = FontWeight.Bold,
                        color = appColors.titleColor,
                        maxLines = 1,
                        softWrap = false
                    )
                },
                text = {
                    Text(
                        text = StringsLocale.get("logout_confirm_desc", lang),
                        color = appColors.textPrimary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.confirmLogout() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = appColors.pillBg,
                            contentColor = appColors.pillText
                        ),
                        shape = CircleShape,
                        modifier = Modifier.testTag("confirm_logout_button")
                    ) {
                        Text(
                            text = StringsLocale.get("logout", lang),
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { viewModel.showLogoutDialog(false) },
                        shape = CircleShape,
                        border = BorderStroke(1.dp, appColors.border),
                        modifier = Modifier.testTag("cancel_logout_button")
                    ) {
                        Text(
                            text = StringsLocale.get("cancel", lang),
                            color = appColors.textPrimary,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                },
                containerColor = appColors.surface
            )
        }

        // Edit Profile Modal Dialog
        if (uiState.showEditProfileDialog) {
            EditProfileDialog(
                userPreference = uiState.userPreferences,
                interfaceLanguage = lang,
                onSave = { fullName, location, username, password ->
                    viewModel.updateUserProfile(fullName, location, username, password)
                },
                onDismiss = { viewModel.showEditProfileDialog(false) }
            )
        }
    }
}

// Proveedor oficial de fuentes de Google Fonts
private val googleFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = com.example.R.array.com_google_android_gms_fonts_certs
)

// Tipografía Slab Serif (remates rectangulares gruesos de alto contraste)
val RobotoSlabFont = GoogleFont("Roboto Slab")
val SlabSerifFontFamily = FontFamily(
    Font(googleFont = RobotoSlabFont, fontProvider = googleFontProvider, weight = FontWeight.Normal),
    Font(googleFont = RobotoSlabFont, fontProvider = googleFontProvider, weight = FontWeight.Bold)
)

/**
 * Resuelve la familia tipográfica para la lectura bíblica con 4 opciones
 * de contraste visual radical y comprobado.
 */
fun resolveBibleFont(fontName: String): FontFamily {
    return com.example.ui.theme.resolveBibleFontFamily(fontName)
}

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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.SyncStatus
import com.example.ui.components.TutorialStep
import com.example.ui.components.tutorialTarget
import com.example.ui.screens.favorites.FavoritesDialog
import com.example.ui.theme.AppTheme
import com.example.ui.theme.AppThemeIds
import com.example.ui.util.StringsLocale
import com.example.ui.viewmodel.BibleUiState
import com.example.ui.viewmodel.BibleViewModel

private val SettingsCrispLightBg = Color(0xFFF6F7F9)
private val SettingsCrispTitleNavy = Color(0xFF0D2240)
private val SettingsCrispBorder = Color(0xFFE5E9F0)
private val SettingsCrispMutedText = Color(0xFF566376)
private val SettingsCrispSubtitleHeader = Color(0xFF475569)
private val SettingsCrispSoftChipBg = Color(0xFFD7DEE8)
private val SettingsCrispUnselectedPillBg = Color(0xFFF0F2F6)
private val SettingsCrispPreviewBg = Color(0xFFF1F3F7)
private val SettingsCrispPreviewBorder = Color(0xFFDDE3EC)

@Composable
fun SettingsScreen(
    uiState: BibleUiState,
    viewModel: BibleViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Auto-scroll para el tutorial en vivo
    LaunchedEffect(uiState.activeTutorialStep) {
        when (uiState.activeTutorialStep) {
            TutorialStep.SETTINGS_PROFILE -> {
                scrollState.animateScrollTo(0)
            }
            TutorialStep.SETTINGS_FAVORITES -> {
                scrollState.animateScrollTo(180)
            }
            TutorialStep.SETTINGS_THEMES -> {
                scrollState.animateScrollTo(420)
            }
            TutorialStep.SETTINGS_TYPOGRAPHY -> {
                scrollState.animateScrollTo(720)
            }
            else -> {}
        }
    }
    var languageMenuOpen by remember { mutableStateOf(false) }
    var fontMenuOpen by remember { mutableStateOf(false) }
    var lightThemeMenuOpen by remember { mutableStateOf(false) }
    var darkThemeMenuOpen by remember { mutableStateOf(false) }
    val lang = uiState.userPreferences.interfaceLanguage
    val appColors = AppTheme.colors
    val heroColors = appColors.heroCard
    val isCrispLight = !appColors.isDark && appColors.themeId == AppThemeIds.CRISP_LIGHT

    val screenBg = if (isCrispLight) SettingsCrispLightBg else appColors.background
    val headingColor = if (isCrispLight) SettingsCrispTitleNavy else appColors.titleColor
    val headerSubtitleColor = if (isCrispLight) SettingsCrispSubtitleHeader else appColors.textSecondary
    val mutedTextColor = if (isCrispLight) SettingsCrispMutedText else appColors.textSecondary
    val iconTint = if (isCrispLight) SettingsCrispTitleNavy else appColors.accent
    val cardBorderColor = if (isCrispLight) SettingsCrispBorder else appColors.border
    val cardElevation = if (appColors.isDark) 0.dp else 1.5.dp
    val cardShape = RoundedCornerShape(22.dp)

    val softChipBg = when {
        isCrispLight -> SettingsCrispSoftChipBg
        !appColors.isDark -> appColors.primaryContainer
        else -> appColors.surfaceVariant
    }
    val softChipText = when {
        isCrispLight -> SettingsCrispTitleNavy
        !appColors.isDark -> appColors.onPrimaryContainer
        else -> appColors.textPrimary
    }
    val softChipBorder = if (appColors.isDark) BorderStroke(1.dp, appColors.border) else null

    val selectedPillBg = if (isCrispLight) SettingsCrispTitleNavy else appColors.pillBg
    val selectedPillText = if (isCrispLight) Color.White else appColors.pillText
    val unselectedPillBg = if (isCrispLight) SettingsCrispUnselectedPillBg else appColors.surfaceVariant
    val previewBg = if (isCrispLight) SettingsCrispPreviewBg else appColors.surfaceVariant
    val previewBorder = if (isCrispLight) SettingsCrispPreviewBorder else appColors.border

    val sliderActive = if (isCrispLight) SettingsCrispTitleNavy else appColors.accent
    val sliderInactive = if (isCrispLight) SettingsCrispSoftChipBg else appColors.border
    val sliderActiveTick = if (isCrispLight) Color.White.copy(alpha = 0.85f) else appColors.cardBg.copy(alpha = 0.85f)
    val sliderInactiveTick = if (isCrispLight) SettingsCrispTitleNavy.copy(alpha = 0.65f) else appColors.textSecondary.copy(alpha = 0.65f)

    val isEn = lang.contains("English", ignoreCase = true)
    // Opciones: English y Spanish en inglés, o Español e Inglés en español
    val languages = if (isEn) listOf("English", "Spanish") else listOf("Español", "Inglés")
    val availableFonts = listOf(
        "Google Sans Flex",
        "Aktiv Grotesk",
        "Untitled Serif",
        "Serif",
        "Monospace"
    )

    val currentFontDisplay = if (uiState.userPreferences.fontFamily.equals("Google Sans", ignoreCase = true)) {
        "Google Sans Flex"
    } else {
        uiState.userPreferences.fontFamily
    }

    val currentLanguageDisplay = if (isEn) "English" else "Español"

    val isGoogleLinked = uiState.googleUid != null
    val activeProfileName = remember(uiState.googleDisplayName, uiState.userPreferences.userName, isGoogleLinked) {
        if (isGoogleLinked) {
            uiState.googleDisplayName.trim().ifBlank { uiState.userPreferences.userName.trim() }
        } else {
            uiState.userPreferences.userName.trim()
        }
    }

    // Dynamic initials from active profile name (empty if Guest)
    val initials = remember(activeProfileName) {
        val parts = activeProfileName.split("\\s+".toRegex()).filter { it.isNotBlank() }
        when {
            parts.size >= 2 -> "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
            parts.isNotEmpty() -> parts[0].take(2).uppercase()
            else -> ""
        }
    }

    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(screenBg)
            .verticalScroll(scrollState)
            .padding(
                start = 20.dp,
                end = 20.dp,
                top = 18.dp,
                bottom = 140.dp + navBarBottom
            )
            .testTag("settings_screen")
    ) {
        Text(
            text = StringsLocale.get("settings_title", lang),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 27.sp,
                letterSpacing = (-0.4).sp
            ),
            fontWeight = FontWeight.Bold,
            color = headingColor,
            maxLines = 1,
            softWrap = false
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = StringsLocale.get("settings_subtitle", lang),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.5.sp),
            color = headerSubtitleColor
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 1. Tarjeta protagonista de Perfil (mismo estilo degradado y tokens del tema activo que Versículo del día)
        Card(
            onClick = { viewModel.showEditProfileDialog(true) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("user_profile_card")
                .tutorialTarget(TutorialStep.SETTINGS_PROFILE),
            shape = cardShape,
            colors = CardDefaults.cardColors(containerColor = heroColors.gradientStart),
            border = BorderStroke(1.dp, heroColors.border),
            elevation = CardDefaults.cardElevation(defaultElevation = if (appColors.isDark) 2.dp else 3.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(heroColors.gradientStart, heroColors.gradientEnd)
                        )
                    )
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar circular neutro translúcido con iniciales o ícono claro
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(heroColors.controlContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (initials.isNotBlank()) {
                        Text(
                            text = initials,
                            color = heroColors.textPrimary,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            tint = heroColors.controlContent,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    when {
                        isGoogleLinked -> {
                            Text(
                                text = activeProfileName.ifBlank { uiState.googleEmail },
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                                fontWeight = FontWeight.SemiBold,
                                color = heroColors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (uiState.googleEmail.isNotBlank()) {
                                Text(
                                    text = uiState.googleEmail,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                                    color = heroColors.textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            val (statusColor, statusLabel) = when (uiState.syncStatus) {
                                SyncStatus.SYNCING -> heroColors.accentDetail to StringsLocale.get("profile_cloud_syncing", lang)
                                SyncStatus.ERROR -> Color(0xFFFCA5A5) to StringsLocale.get("profile_cloud_error", lang)
                                SyncStatus.SYNCED, SyncStatus.IDLE -> Color(0xFF4ADE80) to StringsLocale.get("profile_cloud_active", lang)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                when (uiState.syncStatus) {
                                    SyncStatus.SYNCING -> CircularProgressIndicator(
                                        modifier = Modifier.size(13.dp),
                                        strokeWidth = 1.8.dp,
                                        color = statusColor
                                    )
                                    SyncStatus.ERROR -> Icon(
                                        imageVector = Icons.Outlined.CloudOff,
                                        contentDescription = null,
                                        tint = statusColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    SyncStatus.SYNCED, SyncStatus.IDLE -> Icon(
                                        imageVector = Icons.Filled.CloudDone,
                                        contentDescription = null,
                                        tint = statusColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = statusLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = statusColor,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        activeProfileName.isNotBlank() -> {
                            Text(
                                text = activeProfileName,
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                                fontWeight = FontWeight.SemiBold,
                                color = heroColors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.CloudOff,
                                    contentDescription = null,
                                    tint = heroColors.textSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = StringsLocale.get("profile_local_subtitle", lang),
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                                    color = heroColors.textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        else -> {
                            Text(
                                text = StringsLocale.get("profile_guest_name", lang),
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                                fontWeight = FontWeight.SemiBold,
                                color = heroColors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = StringsLocale.get("profile_guest_subtitle", lang),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 13.sp,
                                    lineHeight = 17.sp
                                ),
                                color = heroColors.textSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Botón circular del lápiz en tono neutro translúcido/discreto con ícono claro legible
                IconButton(
                    onClick = { viewModel.showEditProfileDialog(true) },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(heroColors.controlContainer)
                        .testTag("edit_profile_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = StringsLocale.get("profile_backup_title", lang),
                        tint = heroColors.controlContent,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. "Favoritos" — tarjeta independiente (separada de Idioma)
        Card(
            onClick = { viewModel.showFavoritesDialog(true) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("settings_favorites_item")
                .tutorialTarget(TutorialStep.SETTINGS_FAVORITES),
            shape = cardShape,
            colors = CardDefaults.cardColors(containerColor = appColors.cardBg),
            border = BorderStroke(1.dp, cardBorderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = cardElevation)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = StringsLocale.get("favorites_option", lang),
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.5.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = headingColor,
                            maxLines = 1,
                            softWrap = false
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = String.format(StringsLocale.get("saved_favorites_count", lang), uiState.favorites.size),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                            color = mutedTextColor,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Interface Language Dropdown — tarjeta independiente (separada de Favoritos)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = cardShape,
            colors = CardDefaults.cardColors(containerColor = appColors.cardBg),
            border = BorderStroke(1.dp, cardBorderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = cardElevation)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Language,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = StringsLocale.get("interface_language", lang),
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.5.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = headingColor,
                            maxLines = 1,
                            softWrap = false
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = StringsLocale.get("interface_language_sub", lang),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                            color = mutedTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box {
                    Surface(
                        shape = CircleShape,
                        color = softChipBg,
                        border = softChipBorder,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { languageMenuOpen = true }
                            .testTag("language_dropdown_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = currentLanguageDisplay,
                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.5.sp),
                                fontWeight = FontWeight.Medium,
                                color = softChipText,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = languageMenuOpen,
                        onDismissRequest = { languageMenuOpen = false },
                        shape = RoundedCornerShape(16.dp),
                        containerColor = appColors.surface,
                        tonalElevation = 0.dp,
                        shadowElevation = 8.dp,
                        border = BorderStroke(
                            1.dp,
                            appColors.border.copy(alpha = if (appColors.isDark) 0.85f else 0.75f)
                        )
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

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Bloque unificado de Apariencia (Tema de la Aplicación + Tipografía y lectura)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = cardShape,
            colors = CardDefaults.cardColors(containerColor = appColors.cardBg),
            border = BorderStroke(1.dp, cardBorderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = cardElevation)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // 4a. Selector de Tema (Claro, Oscuro, Automático)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 18.dp)
                        .testTag("theme_selector_card")
                        .tutorialTarget(TutorialStep.SETTINGS_THEMES)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Palette,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = StringsLocale.get("app_theme", lang),
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = headingColor,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val themes = listOf(
                            Triple("LIGHT", StringsLocale.get("theme_light", lang), Icons.Filled.LightMode),
                            Triple("DARK", StringsLocale.get("theme_dark", lang), Icons.Filled.DarkMode),
                            Triple("AUTO", StringsLocale.get("theme_auto", lang), Icons.Filled.SettingsBrightness)
                        )

                        themes.forEach { (modeKey, modeName, icon) ->
                            val isSelected = if (modeKey == "AUTO") {
                                uiState.userPreferences.themeMode.equals("AUTO", ignoreCase = true) ||
                                    uiState.userPreferences.themeMode.equals("AUTOMATIC", ignoreCase = true)
                            } else {
                                uiState.userPreferences.themeMode.equals(modeKey, ignoreCase = true)
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(50))
                                        .background(if (isSelected) selectedPillBg else unselectedPillBg)
                                        .clickable {
                                            when (modeKey) {
                                                "LIGHT" -> {
                                                    viewModel.updateThemeMode("LIGHT")
                                                    lightThemeMenuOpen = true
                                                }
                                                "DARK" -> {
                                                    viewModel.updateThemeMode("DARK")
                                                    darkThemeMenuOpen = true
                                                }
                                                else -> {
                                                    viewModel.updateThemeMode("AUTO")
                                                }
                                            }
                                        }
                                        .padding(vertical = 11.dp)
                                        .testTag("theme_button_$modeKey"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = if (isSelected) selectedPillText else mutedTextColor,
                                            modifier = Modifier.size(19.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = modeName,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                            color = if (isSelected) selectedPillText else headingColor,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }

                                if (modeKey == "LIGHT") {
                                    val lightOptions = listOf(
                                        AppThemeIds.CRISP_LIGHT to StringsLocale.get("theme_crisp_light", lang),
                                        AppThemeIds.WARM_PARCHMENT to StringsLocale.get("theme_warm_parchment", lang),
                                        AppThemeIds.SERENE_OLIVE to StringsLocale.get("theme_serene_olive", lang)
                                    )
                                    DropdownMenu(
                                        expanded = lightThemeMenuOpen,
                                        onDismissRequest = { lightThemeMenuOpen = false },
                                        shape = RoundedCornerShape(16.dp),
                                        containerColor = appColors.surface,
                                        tonalElevation = 0.dp,
                                        shadowElevation = 8.dp,
                                        border = BorderStroke(
                                            1.dp,
                                            appColors.border.copy(alpha = if (appColors.isDark) 0.85f else 0.75f)
                                        )
                                    ) {
                                        lightOptions.forEach { (themeKey, themeLabel) ->
                                            val isOptionSelected = uiState.userPreferences.selectedLightTheme == themeKey
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = themeLabel,
                                                        fontWeight = if (isOptionSelected) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isOptionSelected) appColors.accent else appColors.textPrimary,
                                                        maxLines = 1,
                                                        softWrap = false
                                                    )
                                                },
                                                trailingIcon = if (isOptionSelected) {
                                                    {
                                                        Icon(
                                                            imageVector = Icons.Filled.Check,
                                                            contentDescription = null,
                                                            tint = appColors.accent,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                } else null,
                                                onClick = {
                                                    viewModel.updateSelectedLightTheme(themeKey, activateMode = true)
                                                    lightThemeMenuOpen = false
                                                },
                                                modifier = Modifier.testTag("theme_option_$themeKey")
                                            )
                                        }
                                    }
                                } else if (modeKey == "DARK") {
                                    val darkOptions = listOf(
                                        AppThemeIds.MIDNIGHT_BLUE to StringsLocale.get("theme_midnight_blue", lang),
                                        AppThemeIds.DEEP_DARK to StringsLocale.get("theme_deep_dark", lang),
                                        AppThemeIds.OBSIDIAN_ONYX to StringsLocale.get("theme_obsidian_onyx", lang)
                                    )
                                    DropdownMenu(
                                        expanded = darkThemeMenuOpen,
                                        onDismissRequest = { darkThemeMenuOpen = false },
                                        shape = RoundedCornerShape(16.dp),
                                        containerColor = appColors.surface,
                                        tonalElevation = 0.dp,
                                        shadowElevation = 8.dp,
                                        border = BorderStroke(
                                            1.dp,
                                            appColors.border.copy(alpha = if (appColors.isDark) 0.85f else 0.75f)
                                        )
                                    ) {
                                        darkOptions.forEach { (themeKey, themeLabel) ->
                                            val isOptionSelected = uiState.userPreferences.selectedDarkTheme == themeKey
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = themeLabel,
                                                        fontWeight = if (isOptionSelected) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isOptionSelected) appColors.accent else appColors.textPrimary,
                                                        maxLines = 1,
                                                        softWrap = false
                                                    )
                                                },
                                                trailingIcon = if (isOptionSelected) {
                                                    {
                                                        Icon(
                                                            imageVector = Icons.Filled.Check,
                                                            contentDescription = null,
                                                            tint = appColors.accent,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                } else null,
                                                onClick = {
                                                    viewModel.updateSelectedDarkTheme(themeKey, activateMode = true)
                                                    darkThemeMenuOpen = false
                                                },
                                                modifier = Modifier.testTag("theme_option_$themeKey")
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 18.dp),
                    thickness = 1.dp,
                    color = cardBorderColor
                )

                // 4b. Tipografía y lectura (Fuente, Slider 8–20 pt y Vista previa en lector)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 18.dp)
                        .testTag("typography_controls_card")
                        .tutorialTarget(TutorialStep.SETTINGS_TYPOGRAPHY)
                ) {
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
                                tint = iconTint,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = StringsLocale.get("typography_title", lang),
                                style = if (isEn) {
                                    MaterialTheme.typography.titleSmall.copy(fontSize = 13.5.sp)
                                } else {
                                    MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp)
                                },
                                fontWeight = FontWeight.SemiBold,
                                color = headingColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Box {
                            Surface(
                                shape = CircleShape,
                                color = softChipBg,
                                border = softChipBorder,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { fontMenuOpen = true }
                                    .testTag("font_family_dropdown_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = currentFontDisplay,
                                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.5.sp),
                                        fontWeight = FontWeight.Medium,
                                        color = softChipText,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = fontMenuOpen,
                                onDismissRequest = { fontMenuOpen = false },
                                shape = RoundedCornerShape(16.dp),
                                containerColor = appColors.surface,
                                tonalElevation = 0.dp,
                                shadowElevation = 8.dp,
                                border = BorderStroke(
                                    1.dp,
                                    appColors.border.copy(alpha = if (appColors.isDark) 0.85f else 0.75f)
                                )
                            ) {
                                availableFonts.forEach { font ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = font,
                                                fontWeight = if (currentFontDisplay == font) FontWeight.Bold else FontWeight.Normal,
                                                color = if (currentFontDisplay == font) appColors.accent else appColors.textPrimary,
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
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = headingColor,
                            maxLines = 1,
                            softWrap = false
                        )
                        Text(
                            text = StringsLocale.get("font_range", lang),
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.5.sp),
                            color = mutedTextColor,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Slider(
                        value = uiState.userPreferences.fontSizePt,
                        onValueChange = { viewModel.updateFontSizePt(it) },
                        valueRange = 8f..20f,
                        steps = 11,
                        colors = SliderDefaults.colors(
                            thumbColor = sliderActive,
                            activeTrackColor = sliderActive,
                            inactiveTrackColor = sliderInactive,
                            activeTickColor = sliderActiveTick,
                            inactiveTickColor = sliderInactiveTick
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("font_size_slider")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Live Preview Box
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = previewBg,
                        border = BorderStroke(1.dp, previewBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = StringsLocale.get("reader_preview", lang),
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                                color = if (isCrispLight) SettingsCrispSubtitleHeader else appColors.accent,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                softWrap = false
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            val sampleAnnotated = remember(isEn, headingColor, appColors.textPrimary) {
                                buildAnnotatedString {
                                    if (isEn) {
                                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = headingColor)) {
                                            append("16 ")
                                        }
                                        append("For God so loved the world, that he gave his only begotten Son, that whosoever believeth in him should not perish, but have everlasting life.")
                                    } else {
                                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = headingColor)) {
                                            append("1 ")
                                        }
                                        append("En el principio era el Verbo, y el Verbo era con Dios, y el Verbo era Dios.")
                                    }
                                }
                            }
                            Text(
                                text = sampleAnnotated,
                                fontSize = (uiState.userPreferences.fontSizePt * 1.35f).sp,
                                lineHeight = (uiState.userPreferences.fontSizePt * 1.9f).sp,
                                fontFamily = resolveBibleFont(currentFontDisplay), // <-- Vinculación de fuente en vivo
                                color = appColors.textPrimary,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 5. Botón de Acceso al Tutorial de Uso
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("settings_tutorial_card"),
            shape = cardShape,
            colors = CardDefaults.cardColors(containerColor = appColors.cardBg),
            border = BorderStroke(1.dp, cardBorderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = cardElevation)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(23.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = StringsLocale.get("tutorial_card_title", lang),
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.5.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = headingColor,
                            maxLines = 1,
                            softWrap = false
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = StringsLocale.get("tutorial_card_subtitle", lang),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                            color = mutedTextColor,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Surface(
                    shape = CircleShape,
                    color = selectedPillBg,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { viewModel.startTutorial() }
                        .testTag("start_tutorial_button")
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = StringsLocale.get("tutorial_card_start", lang),
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = selectedPillText,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }

        // Margen adicional de seguridad para garantizar cero solapamiento con la barra inferior flotante
        Spacer(modifier = Modifier.height(12.dp))

        // Favorites Dialog
        if (uiState.showFavoritesDialog) {
            FavoritesDialog(
                uiState = uiState,
                viewModel = viewModel,
                onDismiss = { viewModel.showFavoritesDialog(false) }
            )
        }

        // Profile & Cloud Backup Modal Dialog
        if (uiState.showEditProfileDialog) {
            ProfileBackupDialog(
                uiState = uiState,
                interfaceLanguage = lang,
                onSaveLocalName = { name -> viewModel.updateLocalProfileName(name) },
                onContinueWithGoogle = { viewModel.signInWithGoogle(context) },
                onSyncNow = { viewModel.syncNow() },
                onUnlinkAccount = { viewModel.unlinkGoogleAccount(context) },
                onDismiss = { viewModel.showEditProfileDialog(false) },
                onCancelGoogleAuth = { viewModel.cancelGoogleSignInIfInProgress() }
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

package com.example.ui.screens.modules

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BibleModule
import com.example.ui.theme.AppTheme
import com.example.ui.theme.ModuleDeleteRed
import com.example.ui.theme.ModuleInstalledGreen
import com.example.ui.util.StringsLocale
import com.example.ui.viewmodel.BibleUiState
import com.example.ui.viewmodel.BibleViewModel
import androidx.compose.material.icons.outlined.ArrowCircleDown
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ModulesScreen(
    uiState: BibleUiState,
    viewModel: BibleViewModel,
    modifier: Modifier = Modifier
) {
    var languageMenuOpen by remember { mutableStateOf(false) }
    var onlyDownloaded by remember { mutableStateOf(false) }

    val languages = listOf("Todos", "Español", "Inglés", "Griego / Hebreo")
    val lang = uiState.userPreferences.interfaceLanguage
    val isEn = lang.contains("English", ignoreCase = true)
    val appColors = AppTheme.colors

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(appColors.background)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag("modules_screen")
    ) {
        // Encabezado
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = StringsLocale.get("modules_title", lang),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = appColors.titleColor,
                maxLines = 1,
                softWrap = false
            )
            Text(
                text = StringsLocale.get("modules_subtitle", lang),
                style = MaterialTheme.typography.bodySmall,
                color = appColors.textSecondary
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Fila de Filtros: Idioma y Solo Descargados
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Filtro por Idioma
            Box {
                Surface(
                    shape = CircleShape,
                    color = appColors.pillBg,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { languageMenuOpen = true }
                        .testTag("module_language_filter_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${StringsLocale.get("language_label", lang)}: ${uiState.moduleLanguageFilter}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = appColors.pillText,
                            fontSize = 12.5.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = "Desplegar idiomas",
                            tint = appColors.pillText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = languageMenuOpen,
                    onDismissRequest = { languageMenuOpen = false }
                ) {
                    languages.forEach { languageItem ->
                        val isSelected = uiState.moduleLanguageFilter == languageItem
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = languageItem,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) appColors.accent else appColors.textPrimary,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            },
                            onClick = {
                                viewModel.setModuleLanguageFilter(languageItem)
                                languageMenuOpen = false
                            }
                        )
                    }
                }
            }

            // Filtro conmutador "Solo descargados"
            Surface(
                shape = CircleShape,
                color = if (onlyDownloaded) appColors.accent else (if (appColors.isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)),
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onlyDownloaded = !onlyDownloaded }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.CloudDone,
                        contentDescription = null,
                        tint = if (onlyDownloaded) Color.White else appColors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isEn) "Downloaded" else "Descargados",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (onlyDownloaded) Color.White else appColors.textPrimary,
                        fontSize = 12.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Pestañas de categorías
        val tabs = listOf(
            StringsLocale.get("tab_versions", lang),
            StringsLocale.get("tab_commentaries", lang),
            StringsLocale.get("tab_dictionaries", lang)
        )

        PrimaryTabRow(
            selectedTabIndex = uiState.moduleSelectedTab,
            modifier = Modifier.fillMaxWidth(),
            containerColor = appColors.surface
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = uiState.moduleSelectedTab == index,
                    onClick = { viewModel.setModuleTab(index) },
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.5.sp,
                            maxLines = 1,
                            softWrap = false,
                            fontWeight = if (uiState.moduleSelectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (uiState.moduleSelectedTab == index) appColors.accent else appColors.textSecondary
                        )
                    },
                    modifier = Modifier.testTag("module_tab_$index")
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        val categoryKey = when (uiState.moduleSelectedTab) {
            0 -> "VERSION"
            1 -> "COMMENTARY"
            else -> "DICTIONARY"
        }

        val baseFiltered = uiState.modules.filter { module ->
            module.category == categoryKey &&
                    (uiState.moduleLanguageFilter == "Todos" || module.language == uiState.moduleLanguageFilter)
        }

        val downloadedModules = baseFiltered.filter { it.isDownloaded }
        val availableModules = baseFiltered.filter { !it.isDownloaded }

        if (onlyDownloaded && downloadedModules.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isEn) "No downloaded modules in this category." else "No hay módulos descargados en esta categoría.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = appColors.textSecondary,
                    maxLines = 1,
                    softWrap = false
                )
            }
        } else if (baseFiltered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isEn) "No modules available." else "No se encontraron módulos disponibles.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = appColors.textSecondary,
                    maxLines = 1,
                    softWrap = false
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("modules_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Recursos instalados en la parte superior
                items(downloadedModules, key = { it.id }) { item ->
                    ModuleItemCard(
                        module = item,
                        lang = lang,
                        isEn = isEn,
                        progress = uiState.downloadProgress[item.id],
                        onToggleDownload = { viewModel.toggleModuleDownload(item) }
                    )
                }

                // 2. Divisor centrado (Opción 2) y recursos pendientes de descarga
                if (!onlyDownloaded && availableModules.isNotEmpty()) {
                    if (downloadedModules.isNotEmpty()) {
                        item(key = "divider_available_section") {
                            LabeledDivider(
                                label = if (isEn) "Available (${availableModules.size})" else "Disponibles (${availableModules.size})"
                            )
                        }
                    }

                    items(availableModules, key = { it.id }) { item ->
                        ModuleItemCard(
                            module = item,
                            lang = lang,
                            isEn = isEn,
                            progress = uiState.downloadProgress[item.id],
                            onToggleDownload = { viewModel.toggleModuleDownload(item) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(50.dp))
                }
            }
        }
    }
}

@Composable
private fun LabeledDivider(
    label: String,
    modifier: Modifier = Modifier
) {
    val appColors = AppTheme.colors
    val dividerColor = if (appColors.isDark) Color(0xFF1E293B) else Color(0xFFCBD5E1)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = dividerColor,
            thickness = 1.dp
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = appColors.textSecondary,
            fontSize = 11.5.sp,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = dividerColor,
            thickness = 1.dp
        )
    }
}

@Composable
private fun ModuleItemCard(
    module: BibleModule,
    lang: String,
    isEn: Boolean,
    progress: Float? = null,
    onToggleDownload: () -> Unit
) {
    val appColors = AppTheme.colors
    val isDownloading = progress != null

    val roleLabel = when (module.category) {
        "VERSION" -> if (isEn) "Distributor" else "Distribuidor"
        else -> if (module.author.contains(",")) {
            if (isEn) "Authors" else "Autores"
        } else {
            if (isEn) "Author" else "Autor"
        }
    }

    val localizedModuleLanguage = if (isEn) {
        when {
            module.language.contains("Español", true) && (module.language.contains("Griego", true) || module.language.contains("Hebreo", true)) ->
                "Spanish / Greek / Hebrew"
            module.language.equals("Español", true) -> "Spanish"
            module.language.equals("Inglés", true) || module.language.equals("English", true) -> "English"
            module.language.contains("Griego", true) || module.language.contains("Hebreo", true) -> "Greek / Hebrew"
            else -> module.language
        }
    } else {
        module.language
    }

    // Vinculación de la insignia directamente al flujo tipográfico del título
    val inlineBadgeId = "badge_${module.id}"
    val titleAnnotated = buildAnnotatedString {
        append(module.title.trim())
        append("  ")
        appendInlineContent(inlineBadgeId, "[${module.versionTag}]")
    }

    val inlineContent = mapOf(
        inlineBadgeId to InlineTextContent(
            Placeholder(
                width = (module.versionTag.length * 7f + 18f).sp,
                height = 18.sp,
                placeholderVerticalAlign = PlaceholderVerticalAlign.Center
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(if (appColors.isDark) Color(0xFF0F2744) else Color(0xFFE8EEF5)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = module.versionTag,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = appColors.accent,
                    fontSize = 10.sp,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("module_card_${module.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.cardBg),
        border = if (appColors.isDark) androidx.compose.foundation.BorderStroke(1.dp, appColors.border) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        // Fila principal: Centra verticalmente la columna de textos y el botón de acción
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Título con etiqueta incrustada contigua al nombre
                Text(
                    text = titleAnnotated,
                    inlineContent = inlineContent,
                    style = MaterialTheme.typography.titleSmall,
                    fontSize = 13.5.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = appColors.titleColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "$roleLabel: ${module.author}",
                    style = MaterialTheme.typography.bodySmall,
                    color = appColors.textSecondary,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    softWrap = false
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = module.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = appColors.textPrimary,
                    lineHeight = 18.sp,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Progreso durante la descarga activa
                if (isDownloading && progress != null) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isEn) "Downloading..." else "Descargando...",
                                style = MaterialTheme.typography.labelSmall,
                                color = appColors.accent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "${(progress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = appColors.accent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = appColors.accent,
                            trackColor = if (appColors.isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Metadatos de idioma, tamaño y estado de instalación
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "${StringsLocale.get("language_label", lang)}: $localizedModuleLanguage",
                        style = MaterialTheme.typography.labelSmall,
                        color = appColors.textSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        softWrap = false
                    )

                    Text(
                        text = "${StringsLocale.get("size", lang)}: ${module.sizeText}",
                        style = MaterialTheme.typography.labelSmall,
                        color = appColors.textSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        softWrap = false
                    )

                    if (module.isDownloaded && !isDownloading) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = ModuleInstalledGreen,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = StringsLocale.get("installed", lang),
                                style = MaterialTheme.typography.labelSmall,
                                color = ModuleInstalledGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Botón de acción minimalista centrado verticalmente
            IconButton(
                onClick = onToggleDownload,
                enabled = !isDownloading,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("module_action_button_${module.id}")
            ) {
                if (isDownloading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = appColors.accent
                    )
                } else {
                    Icon(
                        imageVector = if (module.isDownloaded) Icons.Outlined.DeleteOutline else Icons.Outlined.ArrowCircleDown,
                        contentDescription = if (module.isDownloaded) StringsLocale.get("remove", lang) else StringsLocale.get("download", lang),
                        tint = if (module.isDownloaded) ModuleDeleteRed else ModuleInstalledGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
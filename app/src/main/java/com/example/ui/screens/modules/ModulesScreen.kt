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
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModulesScreen(
    uiState: BibleUiState,
    viewModel: BibleViewModel,
    modifier: Modifier = Modifier
) {
    var languageMenuOpen by remember { mutableStateOf(false) }
    val lang = uiState.userPreferences.interfaceLanguage
    val isEn = lang.contains("English", ignoreCase = true)
    val appColors = AppTheme.colors

    // Opciones del selector de idioma adaptadas según el idioma de la aplicación
    val filterOptions = if (isEn) {
        listOf("All", "Spanish", "English", "Greek / Hebrew")
    } else {
        listOf("Todos", "Español", "Inglés", "Griego / Hebreo")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(appColors.background)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag("modules_screen")
    ) {
        // Cabecera
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

        // Selector de idioma (Botón píldora)
        Box(modifier = Modifier.fillMaxWidth()) {
            Surface(
                shape = CircleShape,
                color = appColors.pillBg,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { languageMenuOpen = true }
                    .testTag("module_language_filter_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    val currentDisplayFilter = resolveFilterLabel(uiState.moduleLanguageFilter, isEn)
                    Text(
                        text = "${StringsLocale.get("language_label", lang)}: $currentDisplayFilter",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = appColors.pillText,
                        fontSize = 13.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = if (isEn) "Select language" else "Desplegar idiomas",
                        tint = appColors.pillText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            DropdownMenu(
                expanded = languageMenuOpen,
                onDismissRequest = { languageMenuOpen = false }
            ) {
                filterOptions.forEach { option ->
                    val isSelected = resolveFilterLabel(uiState.moduleLanguageFilter, isEn) == option
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) appColors.accent else appColors.textPrimary,
                                maxLines = 1,
                                softWrap = false
                            )
                        },
                        onClick = {
                            viewModel.setModuleLanguageFilter(option)
                            languageMenuOpen = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Pestañas (Versiones, Comentarios, Diccionarios) traducidas
        val tabs = if (isEn) {
            listOf("Versions", "Commentaries", "Dictionaries")
        } else {
            listOf("Versiones", "Comentarios", "Diccionarios")
        }

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

        // Filtrado por categoría e idioma seleccionado
        val categoryKey = when (uiState.moduleSelectedTab) {
            0 -> "VERSION"
            1 -> "COMMENTARY"
            else -> "DICTIONARY"
        }

        val filteredModules = uiState.modules.filter { module ->
            val matchesCategory = module.category == categoryKey
            val filter = uiState.moduleLanguageFilter
            val matchesLanguage = when {
                filter.equals("Todos", true) || filter.equals("All", true) -> true
                filter.equals("Español", true) || filter.equals("Spanish", true) ->
                    module.language.contains("Español", true) || module.language.contains("Spanish", true)
                filter.equals("Inglés", true) || filter.equals("English", true) ->
                    module.language.contains("Inglés", true) || module.language.contains("English", true)
                filter.contains("Griego", true) || filter.contains("Greek", true) || filter.contains("Hebreo", true) || filter.contains("Hebrew", true) ->
                    module.language.contains("Griego", true) || module.language.contains("Greek", true) ||
                            module.language.contains("Hebreo", true) || module.language.contains("Hebrew", true)
                else -> true
            }
            matchesCategory && matchesLanguage
        }

        if (filteredModules.isEmpty()) {
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
                items(filteredModules, key = { it.id }) { item ->
                    ModuleItemCard(
                        module = item,
                        lang = lang,
                        isEn = isEn,
                        onToggleDownload = { viewModel.toggleModuleDownload(item) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(50.dp))
                }
            }
        }
    }
}

@Composable
private fun ModuleItemCard(
    module: BibleModule,
    lang: String,
    isEn: Boolean,
    onToggleDownload: () -> Unit
) {
    val appColors = AppTheme.colors

    // Etiqueta del rol (Distribuidor / Distributor para versiones; Autor/Autores para comentarios y diccionarios)
    val roleLabel = when (module.category) {
        "VERSION" -> if (isEn) "Distributor" else "Distribuidor"
        else -> {
            if (module.author.contains(",")) {
                if (isEn) "Authors" else "Autores"
            } else {
                if (isEn) "Author" else "Autor"
            }
        }
    }

    // Descripción traducida dinámicamente si el sistema está en inglés
    val localizedDescription = if (isEn) {
        when (module.id) {
            "mod_rvr1960" -> "Classic and most widely read Spanish translation, faithful to traditional manuscripts."
            "mod_kjv" -> "Historic English reference translation with classic textual fidelity."
            "mod_wesley" -> "Devotional and pastoral verse-by-verse commentary for biblical discernment."
            "mod_diccionario_multilexico" -> "Lexical compendium and concordance integrating Hebrew, Aramaic, and Greek definitions."
            else -> module.description
        }
    } else {
        module.description
    }

    // Idioma del módulo traducido para visualización
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("module_card_${module.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = appColors.cardBg
        ),
        border = if (appColors.isDark) androidx.compose.foundation.BorderStroke(1.dp, appColors.border) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = module.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontSize = 13.5.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = appColors.titleColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (appColors.isDark) Color(0xFF0F2744) else Color(0xFFE8EEF5))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
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
                    text = localizedDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = appColors.textPrimary,
                    lineHeight = 18.sp,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

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

                    if (module.isDownloaded) {
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

            IconButton(
                onClick = onToggleDownload,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        if (module.isDownloaded) Color(0xFFFEE2E2) else Color(0xFFDCFCE7)
                    )
                    .testTag("module_action_button_${module.id}")
            ) {
                Icon(
                    imageVector = if (module.isDownloaded) Icons.Filled.DeleteOutline else Icons.Filled.CloudDownload,
                    contentDescription = if (module.isDownloaded) StringsLocale.get("remove", lang) else StringsLocale.get("download", lang),
                    tint = if (module.isDownloaded) ModuleDeleteRed else ModuleInstalledGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Normaliza la etiqueta visual del filtro según el idioma de la aplicación
 */
private fun resolveFilterLabel(filter: String, isEn: Boolean): String {
    return when {
        filter.equals("Todos", true) || filter.equals("All", true) -> if (isEn) "All" else "Todos"
        filter.equals("Español", true) || filter.equals("Spanish", true) -> if (isEn) "Spanish" else "Español"
        filter.equals("Inglés", true) || filter.equals("English", true) -> if (isEn) "English" else "Inglés"
        filter.contains("Griego", true) || filter.contains("Greek", true) || filter.contains("Hebreo", true) || filter.contains("Hebrew", true) ->
            if (isEn) "Greek / Hebrew" else "Griego / Hebreo"
        else -> filter
    }
}
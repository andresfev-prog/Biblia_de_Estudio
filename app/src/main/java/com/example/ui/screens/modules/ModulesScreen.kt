package com.example.ui.screens.modules

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

    val lang = uiState.userPreferences.interfaceLanguage
    val isEn = lang.contains("English", ignoreCase = true)
    val languages = if (isEn) listOf("All", "Spanish", "English") else listOf("Todos", "Español", "Inglés")
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
                val currentDisplayLang = when (uiState.moduleLanguageFilter) {
                    "Todos", "All" -> if (isEn) "All" else "Todos"
                    "Español", "Spanish" -> if (isEn) "Spanish" else "Español"
                    "Inglés", "English" -> if (isEn) "English" else "Inglés"
                    else -> uiState.moduleLanguageFilter
                }

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
                            text = "${StringsLocale.get("language_label", lang)}: $currentDisplayLang",
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
                    onDismissRequest = { languageMenuOpen = false },
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp,
                    shadowElevation = 6.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                ) {
                    languages.forEach { languageItem ->
                        val isSelected = currentDisplayLang == languageItem
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
            val matchesCategory = module.category == categoryKey
            val matchesLanguage = when (uiState.moduleLanguageFilter) {
                "Todos", "All" -> true
                "Español", "Spanish" -> module.language.contains("Español", ignoreCase = true)
                "Inglés", "English" -> module.language.contains("Inglés", ignoreCase = true) || module.language.contains("English", ignoreCase = true)
                else -> module.language == uiState.moduleLanguageFilter
            }
            matchesCategory && matchesLanguage
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
                // 1. Recursos instalados en la parte superior con divisor de sección
                if (downloadedModules.isNotEmpty()) {
                    item(key = "divider_installed_section") {
                        LabeledDivider(
                            label = if (isEn) "Installed (${downloadedModules.size})" else "Instalados (${downloadedModules.size})"
                        )
                    }
                }

                items(downloadedModules, key = { it.id }) { item ->
                    ModuleItemCard(
                        module = item,
                        lang = lang,
                        isEn = isEn,
                        progress = uiState.downloadProgress[item.id],
                        onToggleDownload = { viewModel.toggleModuleDownload(item) }
                    )
                }

                // 2. Divisor centrado y recursos pendientes de descarga
                if (!onlyDownloaded && availableModules.isNotEmpty()) {
                    item(key = "divider_available_section") {
                        LabeledDivider(
                            label = if (isEn) "Available (${availableModules.size})" else "Disponibles (${availableModules.size})"
                        )
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
        border = BorderStroke(1.dp, if (appColors.isDark) appColors.border else appColors.divider.copy(alpha = 0.5f)),
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
                    text = getLocalizedModuleDescription(module, isEn),
                    style = MaterialTheme.typography.bodySmall,
                    color = appColors.textPrimary,
                    lineHeight = 18.sp,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Progreso durante la descarga activa
                if (progress != null) {
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

/**
 * Traduce las descripciones del catálogo de módulos al idioma inglés cuando la interfaz está en dicho idioma.
 */
private fun getLocalizedModuleDescription(module: BibleModule, isEn: Boolean): String {
    if (!isEn) return module.description
    return when (module.id) {
        "mod_rvr1960" -> "Classic and representative translation of the biblical text in Spanish."
        "mod_kjv" -> "Historical benchmark English translation with classic textual fidelity."
        "mod_amp" -> "Translation with clarifying contextual nuances in brackets."
        "mod_ampc" -> "Classic edition of the Amplified Bible in English."
        "mod_dhhd" -> "Accessible and ecumenical version for smooth, modern reading."
        "mod_esv" -> "Rigorous and literal translation in contemporary English."
        "mod_kdsh" -> "Restoration of original Hebrew names throughout the sacred text."
        "mod_msg" -> "Contemporary paraphrase in vibrant, conversational language."
        "mod_niv11" -> "Contemporary English translation combining clarity and formal balance."
        "mod_nkjv" -> "Modern revision maintaining textual precision and classic cadence."
        "mod_nlt15" -> "Translation with lively, devotional, and understandable language."
        "mod_ntv" -> "Modern and dynamic translation in contemporary Spanish."
        "mod_nvi" -> "Contemporary translation offering formal and dynamic balance."
        "mod_pdt" -> "Biblical text in simple, direct, and universally clear language."
        "mod_tla" -> "Text adapted with clear, direct vocabulary for modern readers."
        "mod_tpt" -> "Expressive and poetic translation crafted for devotional passion."
        "mod_ylt" -> "Historical translation with strict verbal and idiomatic literalness."
        "mod_t4t" -> "Simplified explanatory biblical text for translation facilitators."
        "mod_multi_dict" -> "Comprehensive etymological definitions and multilingual Strong's concordance."
        "mod_conc_dict" -> "Alphabetical concordance of key biblical words and references."
        "mod_es_dict" -> "Compendium of biblical terms and concepts in Spanish."
        "mod_str_es" -> "Lexical definitions and Spanish concordance keyed to Strong's numbers."
        "mod_str_plus" -> "Enhanced Strong's dictionary with analytical references."
        "mod_swanson" -> "Exhaustive semantic lexicon of biblical Hebrew, Aramaic, and Greek."
        "mod_twot" -> "Theological wordbook of the Old Testament Hebrew and Aramaic."
        "mod_webster" -> "Traditional classic English biblical and historical definitions."
        "mod_bdb_t" -> "Standard academic Hebrew and Aramaic reference lexicon."
        "mod_lds_es" -> "Dictionary of biblical terms, places, and names in Spanish."
        "mod_vine_at_es" -> "Expository dictionary of Old Testament words (Hebrew-Spanish)."
        "mod_vine_nt_es" -> "Expository dictionary of New Testament words (Greek-Spanish)."
        "mod_vine_nt" -> "Expository dictionary of New Testament words with Greek analysis."
        "mod_vine_ot" -> "Expository dictionary of Old Testament words with Hebrew analysis."
        "mod_wesley" -> "Explanatory and practical notes on the Old and New Testaments."
        "mod_cmh_c" -> "Concise devotional and exegetical commentary on Scripture."
        "mod_cbwm_c" -> "Comprehensive expository commentary on the Old and New Testaments."
        "mod_flores_c" -> "Pastoral and expository notes for biblical edification."
        "mod_jfb_es_c" -> "Exegetical and explanatory commentary on the whole Bible in Spanish."
        "mod_mhwbc_c" -> "Comprehensive verse-by-verse devotional and exegetical commentary."
        "mod_edw_c" -> "Theological notes and exegetical reflections of Jonathan Edwards."
        "mod_spurg_c" -> "Expository notes and sermons by renowned pastor Charles Spurgeon."
        "mod_pchc_c" -> "Comprehensive homiletical commentary with outlines and sermon points."
        "mod_tsk_c" -> "Exhaustive cross-reference system and correlated biblical themes."
        "mod_tsk2_c" -> "Expanded edition with over 500,000 analytical cross-references."
        "mod_wca_c" -> "Exegetical and apologetic handbook addressing biblical difficulties."
        "mod_rv1960_c" -> "Study notes and explanatory references for the RVR1960."
        "mod_ntv_c" -> "Footnotes, explanatory notes, and section headings for the NTV."
        "mod_nvi_notes" -> "Textual notes, cultural background, and cross-references for the NVI."
        "mod_nvi_c" -> "Thematic and theological commentary complementing the NVI."
        "mod_pdt_c" -> "Explanatory notes on vocabulary and historical context for the PDT."
        "mod_tla_c" -> "Clarifications of cultural, geographical, and translational terms for the TLA."
        "mod_amp_notes" -> "Explanatory notes and contextual references for the Amplified Bible."
        "mod_ampc_notes" -> "Classic explanatory annotations for the AMPC edition."
        "mod_esv_c" -> "Study notes and doctrinal context for the ESV translation."
        "mod_kjv_c" -> "Contextual notes and traditional historical references for the KJV."
        "mod_niv11_notes" -> "Explanatory, linguistic, and historical notes for the NIV 2011 edition."
        "mod_nkjv_c" -> "Explanatory, historical, and doctrinal notes for the NKJV."
        "mod_nlt15_notes" -> "Study notes and section headings for the NLT 2015 translation."
        "mod_tpt_c" -> "Devotional study notes with Hebrew and Aramaic linguistic background."
        "mod_t4t_c" -> "Explanatory and linguistic notes for translation facilitators."
        else -> module.description
    }
}
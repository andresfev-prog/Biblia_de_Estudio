package com.example.ui.components

import android.graphics.Typeface
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.HtmlCompat
import com.example.data.model.DictionaryEntry
import com.example.data.model.DictionaryMatch
import com.example.ui.theme.AppIcon
import com.example.ui.theme.AppIcons
import com.example.ui.theme.AppTheme
import com.example.ui.util.StringsLocale
import com.example.ui.util.rememberTranslateTextToolbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionaryBottomSheet(
    searchedWord: String,
    entry: DictionaryEntry? = null,
    matches: List<DictionaryMatch> = emptyList(),
    isLoading: Boolean,
    interfaceLanguage: String,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val appColors = AppTheme.colors
    val isEnglish = interfaceLanguage.contains("English", ignoreCase = true)

    // Si matches viene vacío pero hay un entry legacy, crear un match fallback
    val resolvedMatches = remember(matches, entry) {
        if (matches.isNotEmpty()) {
            matches
        } else if (entry != null) {
            listOf(
                DictionaryMatch(
                    module = com.example.data.model.BibleModule(
                        id = "mod_multi_dict",
                        category = "DICTIONARY",
                        title = if (isEnglish) "Multi-Lexical Dictionary" else "Diccionario multiléxico",
                        author = "Strong, Chavez, Taggart, Vine",
                        description = "Definiciones etimológicas completas y concordancia Strong.",
                        language = "Español",
                        versionTag = "MULTI",
                        sizeText = "12.4 MB",
                        fileName = "Multi.dictionary.SQLite3",
                        isDownloaded = true
                    ),
                    entry = entry
                )
            )
        } else {
            emptyList()
        }
    }

    // Nivel actual: null = Nivel 1 (Selector de diccionarios), no-nulo = Nivel 2 (Detalle/Definición)
    var selectedMatch by remember(searchedWord) { mutableStateOf<DictionaryMatch?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.testTag("dictionary_bottom_sheet"),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = appColors.surface
    ) {
        val customTextToolbar = rememberTranslateTextToolbar()

        CompositionLocalProvider(LocalTextToolbar provides customTextToolbar) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // =========================================================================
                // ENCABEZADO: NIVEL 1 vs NIVEL 2
                // =========================================================================
                val current = selectedMatch
                if (current == null) {
                    // --- NIVEL 1: Lista de diccionarios disponibles ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isEnglish) "Available Dictionaries" else "Diccionarios disponibles",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = appColors.titleColor,
                                maxLines = 1,
                                softWrap = false
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isEnglish) "Select a dictionary for «$searchedWord»" else "Selecciona un diccionario para «$searchedWord»",
                                style = MaterialTheme.typography.bodySmall,
                                color = appColors.textSecondary,
                                lineHeight = 16.sp,
                                maxLines = 2
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("close_dictionary_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Cerrar",
                                tint = appColors.textSecondary
                            )
                        }
                    }
                } else {
                    // --- NIVEL 2: Definición con botón de retroceso (U+E947) ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { selectedMatch = null },
                            modifier = Modifier.testTag("back_to_dictionaries_button")
                        ) {
                            AppIcon(
                                glyph = AppIcons.BackArrow,
                                contentDescription = if (isEnglish) "Back" else "Atrás",
                                tint = appColors.accent,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = current.module.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = appColors.titleColor,
                                maxLines = 1,
                                softWrap = false
                            )
                            Text(
                                text = current.module.author.ifBlank { current.module.versionTag },
                                style = MaterialTheme.typography.bodySmall,
                                color = appColors.textSecondary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("close_dictionary_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Cerrar",
                                tint = appColors.textSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // =========================================================================
                // CONTENIDO
                // =========================================================================
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(36.dp), color = appColors.accent)
                    }
                } else if (current == null) {
                    // --- CONTENIDO NIVEL 1 ---
                    if (resolvedMatches.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = appColors.surfaceVariant),
                            border = if (appColors.isDark) BorderStroke(1.dp, appColors.border) else null,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (isEnglish) "No entries available for «$searchedWord»" else "No hay entradas disponibles para «$searchedWord»",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = appColors.titleColor,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (isEnglish)
                                        "None of your downloaded dictionaries contain an entry for this word. Download additional dictionaries from the Modules tab to expand your study library."
                                    else
                                        "Ninguno de los diccionarios descargados contiene este término. Descarga más diccionarios y concordancias desde la pestaña Módulos para ampliar tu biblioteca de consulta.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = appColors.textSecondary,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            resolvedMatches.forEach { match ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { selectedMatch = match }
                                        .testTag("dict_match_${match.module.versionTag}"),
                                    colors = CardDefaults.cardColors(containerColor = appColors.cardBg),
                                    border = if (appColors.isDark) BorderStroke(1.dp, appColors.border) else BorderStroke(1.dp, appColors.divider.copy(alpha = 0.5f)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = match.module.title,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = appColors.titleColor,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "${match.module.author} · ${match.module.language}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = appColors.textSecondary,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                            if (match.entry.strongCode.isNotBlank() || match.entry.originalWord.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = listOf(
                                                        match.entry.strongCode.takeIf { it.isNotBlank() },
                                                        match.entry.originalWord.takeIf { it.isNotBlank() }
                                                    ).filterNotNull().joinToString(" · "),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = appColors.textSecondary,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        AppIcon(
                                            glyph = AppIcons.ForwardArrow,
                                            contentDescription = null,
                                            tint = appColors.textSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // --- CONTENIDO NIVEL 2: Definición completa en HTML ---
                    val entryItem = current.entry
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dictionary_entry_card"),
                        colors = CardDefaults.cardColors(containerColor = appColors.cardBg),
                        border = if (appColors.isDark) BorderStroke(1.dp, appColors.border) else null,
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = entryItem.displayTerm.ifBlank { searchedWord },
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = appColors.titleColor,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                if (entryItem.strongCode.isNotBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (appColors.isDark) appColors.accent.copy(alpha = 0.25f) else appColors.accent)
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = entryItem.strongCode,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (appColors.isDark) appColors.accent else androidx.compose.ui.graphics.Color.White,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }

                            if (entryItem.originalWord.isNotBlank() || entryItem.pronunciation.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${StringsLocale.get("original_language", interfaceLanguage)} ",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = appColors.textSecondary,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    if (entryItem.originalWord.isNotBlank()) {
                                        Text(
                                            text = entryItem.originalWord,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = appColors.titleColor,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                    if (entryItem.pronunciation.isNotBlank()) {
                                        Text(
                                            text = " (${entryItem.pronunciation})",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = appColors.textSecondary,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = appColors.divider)
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = StringsLocale.get("theological_def", interfaceLanguage),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = appColors.titleColor,
                                maxLines = 1,
                                softWrap = false
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            SelectionContainer {
                                Column {
                                    HtmlDefinitionText(
                                        html = entryItem.definition,
                                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                        color = appColors.textPrimary
                                    )

                                    if (!entryItem.theologicalNotes.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Text(
                                            text = StringsLocale.get("notes_refs", interfaceLanguage),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = appColors.titleColor,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        HtmlDefinitionText(
                                            html = entryItem.theologicalNotes,
                                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                                            color = appColors.textSecondary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedButton(
                                onClick = { selectedMatch = null },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = appColors.accent)
                            ) {
                                AppIcon(
                                    glyph = AppIcons.BackArrow,
                                    modifier = Modifier.size(16.dp),
                                    tint = appColors.accent
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = if (isEnglish) "Back to dictionary list" else "Volver a la lista de diccionarios")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

/**
 * Renderiza texto enriquecido HTML convirtiendo etiquetas comunes en AnnotatedString de Compose.
 */
@Composable
fun HtmlDefinitionText(
    html: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier
) {
    val annotatedString = remember(html) {
        val formattedHtml = html.replace("\n", "<br/>")
        val spanned = HtmlCompat.fromHtml(formattedHtml, HtmlCompat.FROM_HTML_MODE_COMPACT)
        val plainText = spanned.toString()
        val builder = AnnotatedString.Builder(plainText)

        val spans = spanned.getSpans(0, spanned.length, Any::class.java)
        for (span in spans) {
            val start = spanned.getSpanStart(span)
            val end = spanned.getSpanEnd(span)
            if (start in 0..plainText.length && end in start..plainText.length) {
                when (span) {
                    is StyleSpan -> {
                        when (span.style) {
                            Typeface.BOLD -> builder.addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
                            Typeface.ITALIC -> builder.addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
                            Typeface.BOLD_ITALIC -> builder.addStyle(
                                SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic),
                                start,
                                end
                            )
                        }
                    }
                    is UnderlineSpan -> {
                        builder.addStyle(SpanStyle(textDecoration = TextDecoration.Underline), start, end)
                    }
                }
            }
        }
        builder.toAnnotatedString()
    }

    Text(
        text = annotatedString,
        style = style,
        color = color,
        modifier = modifier
    )
}
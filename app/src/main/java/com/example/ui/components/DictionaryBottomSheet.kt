package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DictionaryEntry
import com.example.ui.theme.AppTheme
import com.example.ui.util.StringsLocale
import com.example.ui.util.rememberTranslateTextToolbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionaryBottomSheet(
    searchedWord: String,
    entry: DictionaryEntry?,
    isLoading: Boolean,
    interfaceLanguage: String,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val appColors = AppTheme.colors

    val isEnglish = interfaceLanguage.contains("English", ignoreCase = true)
    val dictionaryTitle = if (isEnglish) "Multi-Lexical Dictionary" else "Diccionario multiléxico"
    val dictionaryAuthors = if (isEnglish) {
        "Authors: Strong, Chavez, Taggart, Vine, Swanson"
    } else {
        "Autores: Strong, Chavez, Taggart, Vine, Swanson"
    }

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = dictionaryTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = appColors.titleColor,
                            maxLines = 1,
                            softWrap = false
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = dictionaryAuthors,
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

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(36.dp), color = appColors.accent)
                    }
                } else if (entry != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dictionary_entry_card"),
                        colors = CardDefaults.cardColors(
                            containerColor = appColors.cardBg
                        ),
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
                                    text = entry.displayTerm,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = appColors.titleColor,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                if (entry.strongCode.isNotBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(if (appColors.isDark) appColors.surfaceVariant else androidx.compose.ui.graphics.Color(0xFFE2E8F0))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = entry.strongCode,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = appColors.accent,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }

                            if (entry.originalWord.isNotBlank() || entry.pronunciation.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${StringsLocale.get("original_language", interfaceLanguage)} ",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = appColors.textSecondary,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    if (entry.originalWord.isNotBlank()) {
                                        Text(
                                            text = entry.originalWord,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = appColors.titleColor,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                    if (entry.pronunciation.isNotBlank()) {
                                        Text(
                                            text = " (${entry.pronunciation})",
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
                                    Text(
                                        text = entry.definition,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = appColors.textPrimary,
                                        lineHeight = 22.sp
                                    )

                                    if (!entry.theologicalNotes.isNullOrBlank()) {
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
                                        Text(
                                            text = entry.theologicalNotes,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = appColors.textSecondary,
                                            lineHeight = 20.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = appColors.surfaceVariant),
                        border = if (appColors.isDark) BorderStroke(1.dp, appColors.border) else null,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Término \"$searchedWord\" en análisis morfológico.",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = appColors.titleColor,
                                maxLines = 1,
                                softWrap = false
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Consulta las concordancias léxicas hebreas y griegas en la pestaña Módulos.",
                                style = MaterialTheme.typography.bodySmall,
                                color = appColors.textSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BibleVerse
import com.example.data.model.Commentary
import com.example.ui.theme.AppIcon
import com.example.ui.theme.AppIcons
import com.example.ui.theme.AppTheme
import com.example.ui.util.rememberTranslateTextToolbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentaryBottomSheet(
    verse: BibleVerse,
    commentaries: List<Commentary>,
    interfaceLanguage: String,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val appColors = AppTheme.colors

    val isEnglish = interfaceLanguage.contains("English", ignoreCase = true)
    val isVersionNote = commentaries.size == 1 && commentaries.first().author.contains("Nota", ignoreCase = true)

    // Nivel actual: null = Nivel 1 (Selector de comentarios), no-nulo = Nivel 2 (Lectura de comentario)
    var selectedCommentary by remember(verse, commentaries) {
        mutableStateOf<Commentary?>(if (isVersionNote && commentaries.size == 1) commentaries.first() else null)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.testTag("commentary_bottom_sheet"),
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
                val current = selectedCommentary
                if (current == null) {
                    // --- NIVEL 1: Encabezado del listado ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isEnglish) "Biblical Commentary" else "Comentarios bíblicos",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = appColors.titleColor,
                                maxLines = 1,
                                softWrap = false
                            )
                            Text(
                                text = "${verse.bookName} ${verse.chapter}:${verse.verse}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = appColors.textSecondary,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("close_commentary_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Cerrar",
                                tint = appColors.textSecondary
                            )
                        }
                    }
                } else {
                    // --- NIVEL 2: Encabezado con botón de retroceso (\uE947) ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { selectedCommentary = null },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("back_to_commentaries_button")
                            ) {
                                AppIcon(
                                    glyph = AppIcons.BackArrow,
                                    contentDescription = "Volver",
                                    tint = appColors.accent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = current.author,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = appColors.titleColor,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Text(
                                    text = "${verse.bookName} ${verse.chapter}:${verse.verse}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = appColors.textSecondary,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("close_commentary_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Cerrar",
                                tint = appColors.textSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tarjeta del versículo citado
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = appColors.surfaceVariant
                    ),
                    border = if (appColors.isDark) BorderStroke(1.dp, appColors.border) else null,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    SelectionContainer {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "«${verse.text}»",
                                style = MaterialTheme.typography.bodyMedium,
                                color = appColors.textPrimary,
                                lineHeight = 22.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "— ${verse.version}",
                                style = MaterialTheme.typography.labelSmall,
                                color = appColors.accent,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // =========================================================================
                // CONTENIDO: NIVEL 1 vs NIVEL 2
                // =========================================================================
                if (current == null) {
                    // --- NIVEL 1: Listado de tarjetas de comentarios disponibles ---
                    if (commentaries.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = appColors.surfaceVariant),
                            border = if (appColors.isDark) BorderStroke(1.dp, appColors.border) else null,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (isEnglish) "No commentaries available for this verse." else "No hay comentarios disponibles para este versículo.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = appColors.textPrimary
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            commentaries.forEachIndexed { index, com ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { selectedCommentary = com }
                                        .testTag("commentary_card_$index"),
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
                                                text = com.author,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = appColors.titleColor,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            val preview = com.content.take(110).replace("\n", " ").trim()
                                            Text(
                                                text = preview + if (com.content.length > 110) "…" else "",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = appColors.textSecondary,
                                                maxLines = 2,
                                                lineHeight = 18.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        AppIcon(
                                            glyph = AppIcons.ForwardArrow,
                                            contentDescription = null,
                                            tint = appColors.accent,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // --- NIVEL 2: Contenido completo del comentario seleccionado ---
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .testTag("commentary_content_card"),
                        colors = CardDefaults.cardColors(
                            containerColor = appColors.cardBg
                        ),
                        border = if (appColors.isDark) BorderStroke(1.dp, appColors.border) else null,
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            if (current.title.isNotBlank() && current.title != current.author) {
                                Text(
                                    text = current.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = appColors.accent,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = appColors.divider)
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            SelectionContainer {
                                Text(
                                    text = current.content,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = appColors.textPrimary,
                                    lineHeight = 23.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
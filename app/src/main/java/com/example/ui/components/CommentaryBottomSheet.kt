package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BibleVerse
import com.example.data.model.Commentary
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
    val modalMainTitle = if (isVersionNote) {
        if (isEnglish) "Translation Notes" else "Notas explicativas"
    } else {
        if (isEnglish) "Biblical Commentary" else "Comentario bíblico"
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = modalMainTitle,
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

                Spacer(modifier = Modifier.height(14.dp))

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
                                text = if (isEnglish) "No commentary available for this verse." else "No hay notas disponibles para este versículo.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = appColors.textPrimary
                            )
                        }
                    }
                } else {
                    commentaries.forEachIndexed { index, com ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .testTag("commentary_item_$index"),
                            colors = CardDefaults.cardColors(
                                containerColor = appColors.cardBg
                            ),
                            border = if (appColors.isDark) BorderStroke(1.dp, appColors.border) else null,
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = com.author,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = appColors.titleColor,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = appColors.divider)
                                Spacer(modifier = Modifier.height(10.dp))

                                SelectionContainer {
                                    Text(
                                        text = com.content,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = appColors.textPrimary,
                                        lineHeight = 22.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
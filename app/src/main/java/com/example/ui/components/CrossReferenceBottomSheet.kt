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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
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
import com.example.data.model.CrossReference
import com.example.ui.theme.AppIcon
import com.example.ui.theme.AppIcons
import com.example.ui.theme.AppTheme
import com.example.ui.util.StringsLocale
import com.example.ui.util.rememberTranslateTextToolbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrossReferenceBottomSheet(
    verse: BibleVerse,
    crossReferences: List<CrossReference>,
    interfaceLanguage: String,
    onNavigateToTarget: (String, Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val appColors = AppTheme.colors

    // Nivel actual: null = Nivel 1 (Listado de referencias), no-nulo = Nivel 2 (Detalle de referencia)
    var selectedReference by remember(verse, crossReferences) {
        mutableStateOf<CrossReference?>(null)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        properties = ModalBottomSheetProperties(shouldDismissOnBackPress = false),
        modifier = Modifier.testTag("cross_reference_bottom_sheet"),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = appColors.surface
    ) {
        ModalBackHandler {
            if (selectedReference != null) {
                selectedReference = null
            } else {
                onDismiss()
            }
        }

        val customTextToolbar = rememberTranslateTextToolbar()

        CompositionLocalProvider(LocalTextToolbar provides customTextToolbar) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                val currentRef = selectedReference
                if (currentRef == null) {
                    // --- NIVEL 1: Cabecera del listado ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = StringsLocale.get("cross_references_title", interfaceLanguage),
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
                            modifier = Modifier.testTag("close_cross_ref_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Cerrar",
                                tint = appColors.textSecondary
                            )
                        }
                    }
                } else {
                    // --- NIVEL 2: Cabecera del detalle con flecha de retroceso ---
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
                                onClick = { selectedReference = null },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("back_to_cross_refs_button")
                            ) {
                                AppIcon(
                                    glyph = AppIcons.BackArrow,
                                    contentDescription = StringsLocale.get("back_to_references", interfaceLanguage),
                                    tint = appColors.accent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${currentRef.targetBook} ${currentRef.targetChapter}:${currentRef.targetVerse}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = appColors.titleColor,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Text(
                                    text = "${StringsLocale.get("cross_ref_from", interfaceLanguage)} ${verse.bookName} ${verse.chapter}:${verse.verse}",
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
                            modifier = Modifier.testTag("close_cross_ref_button")
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

                // Cita del versículo actual
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = appColors.surfaceVariant),
                    border = if (appColors.isDark || appColors.background != appColors.cardBg) BorderStroke(1.dp, appColors.border) else null,
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
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (currentRef == null) {
                    if (crossReferences.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = appColors.surfaceVariant),
                            border = if (appColors.isDark || appColors.background != appColors.cardBg) BorderStroke(1.dp, appColors.border) else null,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = StringsLocale.get("no_cross_refs", interfaceLanguage),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = appColors.textPrimary
                                )
                            }
                        }
                    } else {
                        crossReferences.forEachIndexed { index, ref ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { selectedReference = ref }
                                    .testTag("cross_ref_item_$index"),
                                colors = CardDefaults.cardColors(containerColor = appColors.cardBg),
                                border = if (appColors.isDark || appColors.background != appColors.cardBg) BorderStroke(1.dp, appColors.border) else null,
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${ref.targetBook} ${ref.targetChapter}:${ref.targetVerse}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = appColors.titleColor,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                        Button(
                                            onClick = { onNavigateToTarget(ref.targetBook, ref.targetChapter, ref.targetVerse) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = appColors.pillBg,
                                                contentColor = appColors.pillText
                                            ),
                                            shape = CircleShape,
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = StringsLocale.get("view_passage", interfaceLanguage),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    HorizontalDivider(color = appColors.divider)
                                    Spacer(modifier = Modifier.height(6.dp))

                                    SelectionContainer {
                                        Text(
                                            text = ref.targetText,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = appColors.textPrimary,
                                            lineHeight = 20.sp
                                        )
                                    }

                                    if (!ref.relationNote.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = ref.relationNote,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = appColors.textSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // --- NIVEL 2: Detalle completo de la referencia cruzada ---
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .testTag("cross_ref_detail_card"),
                        colors = CardDefaults.cardColors(containerColor = appColors.cardBg),
                        border = if (appColors.isDark || appColors.background != appColors.cardBg) BorderStroke(1.dp, appColors.border) else null,
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            SelectionContainer {
                                Text(
                                    text = currentRef.targetText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = appColors.textPrimary,
                                    lineHeight = 23.sp
                                )
                            }

                            if (!currentRef.relationNote.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = appColors.divider)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = currentRef.relationNote,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = appColors.textSecondary,
                                    lineHeight = 19.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    onNavigateToTarget(
                                        currentRef.targetBook,
                                        currentRef.targetChapter,
                                        currentRef.targetVerse
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("cross_ref_open_passage_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = appColors.pillBg,
                                    contentColor = appColors.pillText
                                ),
                                shape = CircleShape
                            ) {
                                Text(
                                    text = StringsLocale.get("view_passage", interfaceLanguage),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
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
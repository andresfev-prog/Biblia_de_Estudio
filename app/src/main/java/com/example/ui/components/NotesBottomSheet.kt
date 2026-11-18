package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BibleBooksData
import com.example.data.model.BibleVerse
import com.example.data.model.UserNote
import com.example.ui.theme.AppIcon
import com.example.ui.theme.AppIcons
import com.example.ui.theme.AppTheme
import com.example.ui.util.StringsLocale
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesBottomSheet(
    notes: List<UserNote>,
    editingNote: UserNote?,
    interfaceLanguage: String,
    selectedVerses: List<BibleVerse> = emptyList(),
    onSaveNote: (title: String, content: String, reference: String, noteId: Long) -> Unit,
    onDeleteNote: (id: Long) -> Unit,
    onSelectNoteToEdit: (UserNote?) -> Unit,
    onGetActiveVerseSnippet: () -> Pair<String, String> = { Pair("", "") },
    onGetLastCommentarySnippet: () -> String = { "" },
    onGetLastDictionarySnippet: () -> String = { "" },
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val appColors = AppTheme.colors
    var isEditingMode by remember { mutableStateOf(editingNote != null) }
    var editorSessionKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(editingNote) {
        if (editingNote != null) {
            isEditingMode = true
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = appColors.surface,
        scrimColor = Color.Black.copy(alpha = 0.5f)
    ) {
        AnimatedContent(
            targetState = isEditingMode,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "notes_view_transition"
        ) { editing ->
            if (editing) {
                NoteEditorView(
                    note = editingNote,
                    sessionKey = editorSessionKey,
                    selectedVerses = selectedVerses,
                    interfaceLanguage = interfaceLanguage,
                    onSave = { title, content, ref, id ->
                        onSaveNote(title, content, ref, id)
                        isEditingMode = false
                        onSelectNoteToEdit(null)
                    },
                    onDelete = { id ->
                        onDeleteNote(id)
                        isEditingMode = false
                        onSelectNoteToEdit(null)
                    },
                    onBack = {
                        isEditingMode = false
                        onSelectNoteToEdit(null)
                    }
                )
            } else {
                NotesListView(
                    notes = notes,
                    interfaceLanguage = interfaceLanguage,
                    onNewNote = {
                        editorSessionKey++
                        onSelectNoteToEdit(null)
                        isEditingMode = true
                    },
                    onOpenNote = { note ->
                        editorSessionKey++
                        onSelectNoteToEdit(note)
                        isEditingMode = true
                    },
                    onClose = onDismiss
                )
            }
        }
    }
}

@Composable
private fun NotesListView(
    notes: List<UserNote>,
    interfaceLanguage: String,
    onNewNote: () -> Unit,
    onOpenNote: (UserNote) -> Unit,
    onClose: () -> Unit
) {
    val appColors = AppTheme.colors
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .testTag("notes_list_view")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(appColors.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    AppIcon(
                        glyph = AppIcons.MyNotes,
                        contentDescription = null,
                        tint = appColors.accent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = StringsLocale.get("notes_title", interfaceLanguage),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = appColors.titleColor,
                        maxLines = 1
                    )
                    val isEn = interfaceLanguage.contains("English", ignoreCase = true)
                    val noteCountText = if (isEn) {
                        "${notes.size} ${if (notes.size == 1) "note" else "notes"}"
                    } else {
                        "${notes.size} ${if (notes.size == 1) "nota" else "notas"}"
                    }
                    Text(
                        text = noteCountText,
                        style = MaterialTheme.typography.labelSmall,
                        color = appColors.textSecondary
                    )
                }
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier.testTag("close_notes_sheet_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = StringsLocale.get("cancel", interfaceLanguage),
                    tint = appColors.textSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón "Nueva nota" con contraste garantizado en los 6 temas
        Button(
            onClick = onNewNote,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("new_note_button"),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = appColors.pillBg,
                contentColor = appColors.pillText
            )
        ) {
            Text(
                text = StringsLocale.get("new_note", interfaceLanguage).replace("+", "").trim(),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge,
                color = appColors.pillText
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (notes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 36.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(appColors.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Description,
                            contentDescription = null,
                            tint = appColors.textSecondary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = StringsLocale.get("no_notes_sheet", interfaceLanguage),
                        style = MaterialTheme.typography.bodyMedium,
                        color = appColors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 180.dp, max = 460.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(notes, key = { it.id }) { note ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenNote(note) }
                            .testTag("note_item_${note.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = appColors.cardBg
                        ),
                        border = if (appColors.isDark || appColors.background != appColors.cardBg) BorderStroke(1.dp, appColors.border) else null,
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = note.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = appColors.titleColor,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = dateFormat.format(Date(note.timestamp)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = appColors.textSecondary,
                                    fontSize = 11.sp
                                )
                            }

                            if (note.biblicalReference.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(appColors.primaryContainer.copy(alpha = if (appColors.isDark) 0.45f else 0.65f))
                                        .border(
                                            width = 1.dp,
                                            color = appColors.accent.copy(alpha = 0.25f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .drawBehind {
                                            drawRect(
                                                color = appColors.accent,
                                                topLeft = Offset.Zero,
                                                size = Size(3.5.dp.toPx(), size.height)
                                            )
                                        }
                                        .padding(start = 13.dp, end = 10.dp, top = 8.dp, bottom = 8.dp)
                                        .testTag("note_item_reference_${note.id}")
                                ) {
                                    Text(
                                        text = note.biblicalReference,
                                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                                        fontWeight = FontWeight.Medium,
                                        color = appColors.onPrimaryContainer
                                    )
                                }
                            }

                            if (note.content.isNotBlank() || note.biblicalReference.isBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = note.content.ifBlank { StringsLocale.get("no_additional_content", interfaceLanguage) },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = appColors.textSecondary,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun NoteEditorView(
    note: UserNote?,
    sessionKey: Int,
    selectedVerses: List<BibleVerse>,
    interfaceLanguage: String,
    onSave: (title: String, content: String, reference: String, id: Long) -> Unit,
    onDelete: (id: Long) -> Unit,
    onBack: () -> Unit
) {
    val appColors = AppTheme.colors
    var title by remember(note, sessionKey) { mutableStateOf(note?.title ?: "") }
    var reference by remember(note, sessionKey) { mutableStateOf(note?.biblicalReference ?: "") }
    var content by remember(note, sessionKey) { mutableStateOf(note?.content ?: "") }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = appColors.accent,
        unfocusedBorderColor = appColors.border,
        focusedContainerColor = appColors.cardBg,
        unfocusedContainerColor = appColors.cardBg,
        focusedTextColor = appColors.textPrimary,
        unfocusedTextColor = appColors.textPrimary,
        focusedLabelColor = appColors.accent,
        unfocusedLabelColor = appColors.textSecondary
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .testTag("note_editor_view")
    ) {
        // Cabecera limpia: Solo flecha de regreso y título (sin referencia automática)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("editor_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = StringsLocale.get("tutorial_previous", interfaceLanguage),
                    tint = appColors.titleColor
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (note == null) StringsLocale.get("new_note_clean_title", interfaceLanguage) else StringsLocale.get("edit_note", interfaceLanguage),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = appColors.titleColor,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Campo 1: Título
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("note_title_input"),
            label = { Text(StringsLocale.get("note_title_label", interfaceLanguage)) },
            placeholder = { Text(StringsLocale.get("note_title_placeholder", interfaceLanguage)) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = fieldColors
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Campo 2: Texto bíblico o referencia (opcional) — siempre inicia vacío en notas nuevas
        OutlinedTextField(
            value = reference,
            onValueChange = { reference = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("note_reference_input"),
            label = { Text(StringsLocale.get("note_reference_label", interfaceLanguage)) },
            placeholder = { Text(StringsLocale.get("note_reference_placeholder", interfaceLanguage)) },
            trailingIcon = if (reference.isNotEmpty()) {
                {
                    IconButton(onClick = { reference = "" }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = StringsLocale.get("clear", interfaceLanguage),
                            tint = appColors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else null,
            singleLine = false,
            minLines = 2,
            maxLines = 6,
            shape = RoundedCornerShape(14.dp),
            colors = fieldColors
        )

        // Chip opcional visible únicamente si hay versículos seleccionados activamente en el lector
        if (selectedVerses.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            AssistChip(
                onClick = {
                    val formatted = BibleBooksData.buildFormattedPassageText(selectedVerses)
                    if (formatted.isNotBlank()) {
                        reference = formatted
                    }
                },
                label = {
                    Text(
                        text = StringsLocale.get("insert_selected_verses", interfaceLanguage),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.FormatQuote,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                shape = CircleShape,
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = appColors.primaryContainer.copy(alpha = 0.75f),
                    labelColor = appColors.onPrimaryContainer,
                    leadingIconContentColor = appColors.onPrimaryContainer
                ),
                border = AssistChipDefaults.assistChipBorder(
                    enabled = true,
                    borderColor = appColors.accent.copy(alpha = 0.35f)
                ),
                modifier = Modifier.testTag("insert_selected_verses_button")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Campo 3: Nota (apuntes y reflexiones principales)
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 160.dp, max = 240.dp)
                .testTag("note_content_input"),
            label = { Text(StringsLocale.get("note_content_label", interfaceLanguage)) },
            placeholder = { Text(StringsLocale.get("note_content_placeholder", interfaceLanguage)) },
            singleLine = false,
            minLines = 5,
            shape = RoundedCornerShape(16.dp),
            colors = fieldColors
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Botones de acción: Guardar y Eliminar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    onSave(title, content, reference.trim(), note?.id ?: 0L)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("save_note_button"),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = appColors.pillBg,
                    contentColor = appColors.pillText
                )
            ) {
                Text(
                    text = StringsLocale.get("save_note", interfaceLanguage),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    softWrap = false
                )
            }

            if (note != null) {
                OutlinedButton(
                    onClick = { onDelete(note.id) },
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("delete_note_button"),
                    shape = CircleShape,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFDC2626)
                    ),
                    border = BorderStroke(1.dp, Color(0xFFDC2626))
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = StringsLocale.get("delete_note", interfaceLanguage),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
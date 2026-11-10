package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.AppIcon
import com.example.ui.theme.AppIcons
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserNote
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
                        onSelectNoteToEdit(null)
                        isEditingMode = true
                    },
                    onOpenNote = { note ->
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
                    contentDescription = "Cerrar",
                    tint = appColors.textSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón "Nueva nota" limpio con contraste absoluto (Color.White)
        Button(
            onClick = onNewNote,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("new_note_button"),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = appColors.accent,
                contentColor = Color.White
            )
        ) {
            Text(
                text = StringsLocale.get("new_note", interfaceLanguage).replace("+", "").trim(),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White
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
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp),
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
                        border = if (appColors.isDark) androidx.compose.foundation.BorderStroke(1.dp, appColors.border) else null,
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
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (note.biblicalReference.isNotBlank()) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (appColors.isDark) Color(0xFF0F2744) else Color(0xFFE8EEF5)
                                    ) {
                                        Text(
                                            text = note.biblicalReference,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = appColors.accent,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                            maxLines = 1
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.width(1.dp))
                                }

                                Text(
                                    text = dateFormat.format(Date(note.timestamp)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = appColors.textSecondary,
                                    fontSize = 11.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = note.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = appColors.titleColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = note.content.ifBlank { "Sin contenido adicional." },
                                style = MaterialTheme.typography.bodySmall,
                                color = appColors.textSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 18.sp
                            )
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
    interfaceLanguage: String,
    onSave: (title: String, content: String, reference: String, id: Long) -> Unit,
    onDelete: (id: Long) -> Unit,
    onBack: () -> Unit
) {
    val appColors = AppTheme.colors
    var title by remember(note) { mutableStateOf(note?.title ?: "") }
    var content by remember(note) { mutableStateOf(note?.content ?: "") }
    val reference = remember(note) { note?.biblicalReference ?: "" }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .testTag("note_editor_view")
    ) {
        // Cabecera limpia: Solo flecha de regreso y título (sin badge "Juan 1:1" a la derecha)
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
                    contentDescription = "Volver",
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

        // Campo: Título de la nota
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("note_title_input"),
            placeholder = { Text(StringsLocale.get("note_title_placeholder", interfaceLanguage)) },
            singleLine = true,
            shape = CircleShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = appColors.accent,
                unfocusedBorderColor = appColors.border,
                focusedContainerColor = appColors.cardBg,
                unfocusedContainerColor = appColors.cardBg,
                focusedTextColor = appColors.textPrimary,
                unfocusedTextColor = appColors.textPrimary
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Campo: Cuerpo de la nota ampliado (permite copiar y pegar libremente)
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .testTag("note_content_input"),
            placeholder = { Text(StringsLocale.get("note_content_placeholder", interfaceLanguage)) },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = appColors.accent,
                unfocusedBorderColor = appColors.border,
                focusedContainerColor = appColors.cardBg,
                unfocusedContainerColor = appColors.cardBg,
                focusedTextColor = appColors.textPrimary,
                unfocusedTextColor = appColors.textPrimary
            )
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
                    onSave(title, content, reference, note?.id ?: 0L)
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
                    text = "Guardar",
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
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDC2626))
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
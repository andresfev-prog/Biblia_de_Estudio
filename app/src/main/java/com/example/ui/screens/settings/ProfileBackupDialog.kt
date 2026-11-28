package com.example.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.LinkOff
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.repository.SyncStatus
import com.example.ui.components.ModalBackHandler
import com.example.ui.components.rememberHideImeAction
import com.example.ui.components.rememberIsImeVisible
import com.example.ui.theme.AppTheme
import com.example.ui.util.StringsLocale
import com.example.ui.viewmodel.BibleUiState

@Composable
fun ProfileBackupDialog(
    uiState: BibleUiState,
    interfaceLanguage: String,
    onSaveLocalName: (String) -> Unit,
    onContinueWithGoogle: () -> Unit,
    onSyncNow: () -> Unit,
    onUnlinkAccount: () -> Unit,
    onDismiss: () -> Unit,
    onCancelGoogleAuth: () -> Unit = {}
) {
    val appColors = AppTheme.colors
    val lang = interfaceLanguage
    val isLinked = uiState.googleUid != null
    val isImeVisible by rememberIsImeVisible()

    var localName by remember(uiState.userPreferences.userName) {
        mutableStateOf(uiState.userPreferences.userName)
    }

    Dialog(
        onDismissRequest = {
            if (!uiState.isGoogleAuthInProgress) {
                onDismiss()
            }
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = !uiState.isGoogleAuthInProgress
        )
    ) {
        val focusManager = LocalFocusManager.current
        val hideIme = rememberHideImeAction()
        var wasImeVisible by remember { mutableStateOf(false) }

        LaunchedEffect(isImeVisible) {
            if (wasImeVisible && !isImeVisible) {
                focusManager.clearFocus(force = true)
            }
            wasImeVisible = isImeVisible
        }

        ModalBackHandler {
            when {
                isImeVisible -> {
                    hideIme()
                }
                uiState.isGoogleAuthInProgress -> {
                    onCancelGoogleAuth()
                }
                else -> {
                    onDismiss()
                }
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .clip(RoundedCornerShape(24.dp))
                .testTag("profile_backup_dialog"),
            color = appColors.surface,
            border = BorderStroke(1.dp, appColors.border),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Encabezado del modal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(appColors.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isLinked) Icons.Filled.CloudDone else Icons.Filled.Person,
                                contentDescription = null,
                                tint = appColors.accent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = StringsLocale.get("profile_backup_title", lang),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = appColors.titleColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = StringsLocale.get("profile_backup_desc", lang),
                                style = MaterialTheme.typography.bodySmall,
                                color = appColors.textSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_edit_profile_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = StringsLocale.get("cancel", lang),
                            tint = appColors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (!isLinked) {
                    // =================================================================
                    // ESTADO A: No vinculado (Perfil local / Invitado + Google Sign-In)
                    // =================================================================
                    OutlinedTextField(
                        value = localName,
                        onValueChange = { localName = it },
                        label = { Text(StringsLocale.get("profile_name_label", lang)) },
                        placeholder = { Text(StringsLocale.get("profile_name_placeholder", lang)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = null,
                                tint = appColors.accent
                            )
                        },
                        trailingIcon = if (localName.isNotEmpty()) {
                            {
                                IconButton(onClick = { localName = "" }) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = StringsLocale.get("clear", lang),
                                        tint = appColors.textSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        } else null,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = appColors.accent,
                            unfocusedBorderColor = appColors.border,
                            focusedTextColor = appColors.textPrimary,
                            unfocusedTextColor = appColors.textPrimary,
                            focusedLabelColor = appColors.accent,
                            unfocusedLabelColor = appColors.textSecondary,
                            focusedContainerColor = appColors.cardBg,
                            unfocusedContainerColor = appColors.cardBg
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_profile_name_input")
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = StringsLocale.get("profile_cloud_section", lang),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = appColors.titleColor
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = onContinueWithGoogle,
                        enabled = !uiState.isGoogleAuthInProgress,
                        shape = CircleShape,
                        border = BorderStroke(1.dp, appColors.border),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = appColors.cardBg,
                            contentColor = appColors.textPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("continue_with_google_button")
                    ) {
                        if (uiState.isGoogleAuthInProgress) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = appColors.accent
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = StringsLocale.get("signing_in_google", lang),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else {
                            GoogleGIcon(modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = StringsLocale.get("continue_with_google", lang),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    PrivacyNoticeBox(
                        text = StringsLocale.get("profile_privacy_notice", lang)
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = CircleShape,
                            border = BorderStroke(1.dp, appColors.border),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("cancel_edit_profile_button")
                        ) {
                            Text(
                                text = StringsLocale.get("cancel", lang),
                                color = appColors.textPrimary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        Button(
                            onClick = { onSaveLocalName(localName) },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = appColors.pillBg,
                                contentColor = appColors.pillText
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("save_profile_button")
                        ) {
                            Text(
                                text = StringsLocale.get("save", lang),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                } else {
                    // =================================================================
                    // ESTADO B: Cuenta vinculada con Google + Respaldo Firestore
                    // =================================================================
                    val displayName = uiState.googleDisplayName.ifBlank {
                        uiState.googleEmail.substringBefore("@").ifBlank {
                            StringsLocale.get("profile_guest_name", lang)
                        }
                    }
                    val initials = remember(displayName) {
                        val parts = displayName.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
                        when {
                            parts.size >= 2 -> "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
                            parts.isNotEmpty() -> parts[0].take(2).uppercase()
                            else -> "G"
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = appColors.surfaceVariant,
                        border = BorderStroke(1.dp, appColors.border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(appColors.pillBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = initials,
                                        color = appColors.pillText,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = displayName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = appColors.titleColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (uiState.googleEmail.isNotBlank()) {
                                        Text(
                                            text = uiState.googleEmail,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = appColors.textSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))
                                GoogleGIcon(modifier = Modifier.size(20.dp))
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            val syncedGreen = if (appColors.isDark) Color(0xFF34D399) else Color(0xFF16A34A)
                            val (statusTint, statusText) = when (uiState.syncStatus) {
                                SyncStatus.SYNCING -> appColors.accent to StringsLocale.get("profile_backup_syncing_detail", lang)
                                SyncStatus.SYNCED -> syncedGreen to StringsLocale.get("profile_backup_synced_detail", lang)
                                SyncStatus.ERROR -> MaterialTheme.colorScheme.error to StringsLocale.get("profile_backup_error_detail", lang)
                                SyncStatus.IDLE -> appColors.accent to StringsLocale.get("profile_cloud_active", lang)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                when (uiState.syncStatus) {
                                    SyncStatus.SYNCING -> CircularProgressIndicator(
                                        modifier = Modifier.size(15.dp),
                                        strokeWidth = 2.dp,
                                        color = statusTint
                                    )
                                    SyncStatus.SYNCED -> Icon(
                                        imageVector = Icons.Filled.CloudDone,
                                        contentDescription = null,
                                        tint = statusTint,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    SyncStatus.ERROR -> Icon(
                                        imageVector = Icons.Outlined.CloudOff,
                                        contentDescription = null,
                                        tint = statusTint,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    SyncStatus.IDLE -> Icon(
                                        imageVector = Icons.Filled.CloudQueue,
                                        contentDescription = null,
                                        tint = statusTint,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = statusText,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = statusTint,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    val isSyncing = uiState.syncStatus == SyncStatus.SYNCING
                    Button(
                        onClick = onSyncNow,
                        enabled = !isSyncing,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = appColors.pillBg,
                            contentColor = appColors.pillText
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("sync_now_button")
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = appColors.pillText
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = StringsLocale.get("syncing_now", lang),
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Sync,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = StringsLocale.get("sync_now", lang),
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = onUnlinkAccount,
                        enabled = !uiState.isGoogleAuthInProgress,
                        shape = CircleShape,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("unlink_google_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LinkOff,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = StringsLocale.get("unlink_account", lang),
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    PrivacyNoticeBox(
                        text = StringsLocale.get("profile_privacy_notice", lang)
                    )
                }
            }
        }
    }
}

@Composable
private fun PrivacyNoticeBox(text: String) {
    val appColors = AppTheme.colors
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = appColors.surfaceVariant.copy(alpha = 0.7f),
        border = BorderStroke(1.dp, appColors.border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Outlined.Shield,
                contentDescription = null,
                tint = appColors.accent,
                modifier = Modifier
                    .padding(top = 1.dp)
                    .size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                color = appColors.textSecondary
            )
        }
    }
}

@Composable
private fun GoogleGIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val s = size.minDimension
        val stroke = s * 0.21f
        val inset = stroke / 2f
        val arcTopLeft = Offset(inset, inset)
        val arcSize = Size(s - stroke, s - stroke)

        val googleBlue = Color(0xFF4285F4)
        val googleRed = Color(0xFFEA4335)
        val googleYellow = Color(0xFFFBBC05)
        val googleGreen = Color(0xFF34A853)

        // Arco amarillo (izquierda)
        drawArc(
            color = googleYellow,
            startAngle = 148f,
            sweepAngle = 64f,
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Butt)
        )
        // Arco rojo (arriba)
        drawArc(
            color = googleRed,
            startAngle = -150f,
            sweepAngle = 108f,
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Butt)
        )
        // Arco verde (abajo)
        drawArc(
            color = googleGreen,
            startAngle = 42f,
            sweepAngle = 108f,
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Butt)
        )
        // Arco azul (derecha inferior)
        drawArc(
            color = googleBlue,
            startAngle = -6f,
            sweepAngle = 50f,
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Butt)
        )
        // Barra horizontal azul característica de la G
        drawRect(
            color = googleBlue,
            topLeft = Offset(s * 0.50f, s * 0.50f - stroke / 2f),
            size = Size(s * 0.48f, stroke)
        )
    }
}

package com.example.ui.screens.settings

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.UserPreference
import com.example.ui.theme.AppTheme
import com.example.ui.util.StringsLocale

@Composable
fun EditProfileDialog(
    userPreference: UserPreference,
    interfaceLanguage: String,
    onSave: (fullName: String, location: String, username: String, password: String) -> Unit,
    onDismiss: () -> Unit
) {
    val appColors = AppTheme.colors
    val lang = interfaceLanguage

    var fullName by remember { mutableStateOf(userPreference.userName) }
    var location by remember { mutableStateOf(userPreference.location) }
    var username by remember { mutableStateOf(userPreference.usernameHandle) }
    var password by remember { mutableStateOf(userPreference.password) }
    var passwordVisible by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .clip(RoundedCornerShape(24.dp))
                .testTag("edit_profile_dialog"),
            color = appColors.surface,
            border = if (appColors.isDark) BorderStroke(1.dp, appColors.border) else null,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with title and close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = null,
                            tint = appColors.accent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = StringsLocale.get("edit_profile", lang),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = appColors.titleColor,
                                maxLines = 1,
                                softWrap = false
                            )
                            Text(
                                text = StringsLocale.get("edit_profile_desc", lang),
                                style = MaterialTheme.typography.bodySmall,
                                color = appColors.textSecondary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_edit_profile_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Cerrar",
                            tint = appColors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Field 1: Nombre completo
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text(StringsLocale.get("full_name", lang)) },
                    placeholder = { Text("Andrés Felipe") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = appColors.accent
                        )
                    },
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

                Spacer(modifier = Modifier.height(14.dp))

                // Field 2: Ubicación / Ciudad (por defecto "Cali, Colombia")
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text(StringsLocale.get("location_city", lang)) },
                    placeholder = { Text("Cali, Colombia") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = appColors.accent
                        )
                    },
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
                        .testTag("edit_profile_location_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Field 3: Nombre de usuario
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text(StringsLocale.get("username", lang)) },
                    placeholder = { Text("andresfev") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.AlternateEmail,
                            contentDescription = null,
                            tint = appColors.accent
                        )
                    },
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
                        .testTag("edit_profile_username_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Field 4: Contraseña con icono de ojo para mostrar/ocultar
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(StringsLocale.get("password", lang)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = appColors.accent
                        )
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = { passwordVisible = !passwordVisible },
                            modifier = Modifier.testTag("toggle_password_visibility_button")
                        ) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                                tint = appColors.textSecondary
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
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
                        .testTag("edit_profile_password_input")
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons: "Guardar" y "Cancelar"
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
                            .height(50.dp)
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

                    val isEnglish = interfaceLanguage.contains("English", ignoreCase = true)
                    val saveLabel = if (isEnglish) "Save" else "Guardar"

                    Button(
                        onClick = {
                            val finalName = if (fullName.isNotBlank()) fullName.trim() else "Andrés Felipe"
                            val finalLoc = if (location.isNotBlank()) location.trim() else "Cali, Colombia"
                            val finalUser = if (username.isNotBlank()) username.trim() else "andresfev"
                            onSave(finalName, finalLoc, finalUser, password)
                        },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = appColors.pillBg,
                            contentColor = appColors.pillText
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("save_profile_button")
                    ) {
                        Text(
                            text = saveLabel,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}

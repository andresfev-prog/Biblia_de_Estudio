package com.example.ui.screens.favorites

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.FavoriteVerse
import com.example.ui.theme.AppTheme
import com.example.ui.util.StringsLocale
import com.example.ui.viewmodel.BibleUiState
import com.example.ui.viewmodel.BibleViewModel

@Composable
fun FavoritesDialog(
    uiState: BibleUiState,
    viewModel: BibleViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lang = uiState.userPreferences.interfaceLanguage
    val appColors = AppTheme.colors

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .clip(RoundedCornerShape(24.dp))
                .testTag("favorites_dialog"),
            color = appColors.surface,
            border = if (appColors.isDark) BorderStroke(1.dp, appColors.border) else null
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("close_favorites_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = appColors.titleColor
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = StringsLocale.get("favorites_screen_title", lang),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = appColors.titleColor,
                                maxLines = 1,
                                softWrap = false
                            )
                            Text(
                                text = String.format(StringsLocale.get("saved_favorites_count", lang), uiState.favorites.size),
                                style = MaterialTheme.typography.bodySmall,
                                color = appColors.textSecondary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_favorites_dialog_button")
                    ) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Cerrar", tint = appColors.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (uiState.favorites.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.Favorite,
                                contentDescription = null,
                                tint = appColors.accent,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = StringsLocale.get("no_favorites_yet", lang),
                                style = MaterialTheme.typography.bodyMedium,
                                color = appColors.textSecondary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("favorites_list"),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.favorites, key = { it.id }) { fav ->
                            FavoriteCardItem(
                                fav = fav,
                                lang = lang,
                                onNavigate = {
                                    onDismiss()
                                    viewModel.navigateToPassage(fav.bookName, fav.chapter, fav.verse)
                                },
                                onDelete = { viewModel.removeFavorite(fav) },
                                onCopy = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Versículo", "«${fav.text}» - ${fav.bookName} ${fav.chapter}:${fav.verse}")
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, StringsLocale.get("copied_clipboard", lang), Toast.LENGTH_SHORT).show()
                                },
                                onShare = {
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, "«${fav.text}» - ${fav.bookName} ${fav.chapter}:${fav.verse} (${fav.version})")
                                    }
                                    context.startActivity(Intent.createChooser(intent, StringsLocale.get("share_verse", lang)))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteCardItem(
    fav: FavoriteVerse,
    lang: String,
    onNavigate: () -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    val appColors = AppTheme.colors

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("favorite_item_${fav.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.cardBg),
        border = if (appColors.isDark) BorderStroke(1.dp, appColors.border) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${fav.bookName} ${fav.chapter}:${fav.verse}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = appColors.titleColor,
                    maxLines = 1,
                    softWrap = false
                )
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(appColors.surfaceVariant)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = fav.version,
                        style = MaterialTheme.typography.labelSmall,
                        color = appColors.accent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "«${fav.text}»",
                style = MaterialTheme.typography.bodyMedium,
                color = appColors.textPrimary,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onNavigate,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = appColors.pillBg,
                        contentColor = appColors.pillText
                    ),
                    shape = CircleShape
                ) {
                    Text(
                        text = "Leer en Biblia",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Row {
                    IconButton(onClick = onCopy) {
                        Icon(imageVector = Icons.Filled.ContentCopy, contentDescription = "Copiar", tint = appColors.textSecondary)
                    }
                    IconButton(onClick = onShare) {
                        Icon(imageVector = Icons.Filled.Share, contentDescription = "Compartir", tint = appColors.textSecondary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(imageVector = Icons.Filled.DeleteOutline, contentDescription = "Eliminar", tint = if (appColors.isDark) androidx.compose.ui.graphics.Color(0xFFF87171) else com.example.ui.theme.ModuleDeleteRed)
                    }
                }
            }
        }
    }
}

package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.BookInfo
import com.example.data.model.ModuleBibleCatalog
import com.example.ui.theme.AppTheme
import com.example.ui.theme.AppThemeIds

private val PickerCrispTitleNavy = Color(0xFF0D2240)
private val PickerCrispMutedText = Color(0xFF566376)

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun PassageSelectorDialog(
    currentStep: Int,
    selectedBook: BookInfo,
    selectedChapter: Int,
    catalog: ModuleBibleCatalog? = null,
    availableVerses: List<Int> = emptyList(),
    interfaceLanguage: String = "Español",
    onBookSelected: (BookInfo) -> Unit,
    onChapterSelected: (Int) -> Unit,
    onVerseSelected: (Int) -> Unit,
    onBackStep: () -> Unit,
    onDismiss: () -> Unit
) {
    val appColors = AppTheme.colors
    val isCrispLight = !appColors.isDark && appColors.themeId == AppThemeIds.CRISP_LIGHT
    val headingColor = if (isCrispLight) PickerCrispTitleNavy else appColors.titleColor
    val mutedTextColor = if (isCrispLight) PickerCrispMutedText else appColors.textSecondary
    val isEn = interfaceLanguage.contains("English", ignoreCase = true)
    val bookDisplayName = if (isEn) selectedBook.englishName else selectedBook.name

    val otBooks = remember(catalog) { catalog?.oldTestamentBooks.orEmpty() }
    val ntBooks = remember(catalog) { catalog?.newTestamentBooks.orEmpty() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
                .clip(RoundedCornerShape(24.dp))
                .testTag("passage_selector_dialog"),
            color = appColors.surface,
            border = BorderStroke(1.dp, appColors.border.copy(alpha = if (appColors.isDark) 1f else 0.75f)),
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
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
                        if (currentStep > 1) {
                            IconButton(
                                onClick = onBackStep,
                                modifier = Modifier.testTag("passage_picker_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = if (isEn) "Back" else "Volver",
                                    tint = headingColor
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            val headerTitle = when (currentStep) {
                                1 -> if (isEn) "Select Book" else "Selecciona Libro"
                                2 -> "$bookDisplayName • ${if (isEn) "Chapter" else "Capítulo"}"
                                else -> "$bookDisplayName $selectedChapter • ${if (isEn) "Verse" else "Versículo"}"
                            }
                            Text(
                                text = headerTitle,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 17.sp,
                                    letterSpacing = (-0.2).sp
                                ),
                                fontWeight = FontWeight.SemiBold,
                                color = headingColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (isEn) "Step $currentStep of 3" else "Paso $currentStep de 3",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                                color = mutedTextColor,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("passage_picker_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = if (isEn) "Close" else "Cerrar",
                            tint = mutedTextColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                when (currentStep) {
                    1 -> {
                        if (catalog == null) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    color = appColors.accent,
                                    strokeWidth = 2.5.dp
                                )
                            }
                        } else if (otBooks.isEmpty() && ntBooks.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isEn) {
                                        "No books available in ${catalog.version}."
                                    } else {
                                        "No hay libros disponibles en ${catalog.version}."
                                    },
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                    color = mutedTextColor,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            // Paso 1: Libros 100 % dinámicos (oculta pestaña AT o NT si el módulo no tiene libros allí)
                            val showBothTestaments = otBooks.isNotEmpty() && ntBooks.isNotEmpty()
                            val defaultTab = if (selectedBook.testament == "NT" && ntBooks.isNotEmpty()) 1 else 0
                            var testamentTab by remember(catalog.version, selectedBook.number, showBothTestaments) {
                                mutableIntStateOf(if (showBothTestaments) defaultTab else 0)
                            }

                            if (showBothTestaments) {
                                PrimaryTabRow(
                                    selectedTabIndex = testamentTab,
                                    modifier = Modifier.fillMaxWidth(),
                                    containerColor = appColors.surface,
                                    contentColor = appColors.accent
                                ) {
                                    Tab(
                                        selected = testamentTab == 0,
                                        onClick = { testamentTab = 0 },
                                        modifier = Modifier.testTag("passage_tab_ot"),
                                        text = {
                                            Text(
                                                text = if (isEn) "Old Testament (${otBooks.size})" else "Antiguo T. (${otBooks.size})",
                                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                                                fontWeight = if (testamentTab == 0) FontWeight.SemiBold else FontWeight.Medium,
                                                color = if (testamentTab == 0) appColors.accent else mutedTextColor,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    )
                                    Tab(
                                        selected = testamentTab == 1,
                                        onClick = { testamentTab = 1 },
                                        modifier = Modifier.testTag("passage_tab_nt"),
                                        text = {
                                            Text(
                                                text = if (isEn) "New Testament (${ntBooks.size})" else "Nuevo T. (${ntBooks.size})",
                                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                                                fontWeight = if (testamentTab == 1) FontWeight.SemiBold else FontWeight.Medium,
                                                color = if (testamentTab == 1) appColors.accent else mutedTextColor,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            } else {
                                val singleTestamentLabel = if (ntBooks.isNotEmpty()) {
                                    if (isEn) "New Testament (${ntBooks.size})" else "Nuevo Testamento (${ntBooks.size})"
                                } else {
                                    if (isEn) "Old Testament (${otBooks.size})" else "Antiguo Testamento (${otBooks.size})"
                                }
                                Text(
                                    text = singleTestamentLabel,
                                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                                    fontWeight = FontWeight.SemiBold,
                                    color = appColors.accent,
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp, vertical = 4.dp)
                                        .testTag(if (ntBooks.isNotEmpty()) "passage_nt_only_header" else "passage_ot_only_header")
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            val filteredBooks = when {
                                showBothTestaments -> if (testamentTab == 0) otBooks else ntBooks
                                ntBooks.isNotEmpty() -> ntBooks
                                else -> otBooks
                            }

                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 88.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(filteredBooks, key = { it.number }) { book ->
                                    val isSelected = book.number == selectedBook.number
                                    val realChaptersCount = catalog.getAvailableChapters(book).size
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp)
                                            .clip(CircleShape)
                                            .clickable { onBookSelected(book) }
                                            .testTag("book_card_${book.number}"),
                                        shape = CircleShape,
                                        color = if (isSelected) appColors.pillBg else appColors.surfaceVariant,
                                        tonalElevation = 0.dp
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = if (isEn) book.englishName else book.name,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (isSelected) appColors.pillText else appColors.textPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                textAlign = TextAlign.Center
                                            )
                                            Text(
                                                text = "$realChaptersCount ${if (isEn) "ch." else "cap."}",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                                color = if (isSelected) appColors.pillText.copy(alpha = 0.85f) else mutedTextColor,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // Paso 2: Cuadrícula dinámica con únicamente los capítulos presentes en el SQLite del módulo
                        Text(
                            text = if (isEn) "Available chapters in $bookDisplayName:" else "Capítulos disponibles en ${selectedBook.name}:",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                            color = mutedTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        val chapterList = remember(catalog, selectedBook.number) {
                            catalog?.getAvailableChapters(selectedBook).orEmpty()
                        }

                        if (chapterList.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isEn) {
                                        "No chapters available for $bookDisplayName."
                                    } else {
                                        "No hay capítulos disponibles para ${selectedBook.name}."
                                    },
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                    color = mutedTextColor,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(5),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("passage_chapters_grid"),
                                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 88.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(chapterList, key = { it }) { chap ->
                                    val isSelected = chap == selectedChapter
                                    Surface(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .clickable { onChapterSelected(chap) }
                                            .testTag("chapter_button_$chap"),
                                        shape = CircleShape,
                                        color = if (isSelected) appColors.pillBg else appColors.surfaceVariant,
                                        tonalElevation = 0.dp
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "$chap",
                                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (isSelected) appColors.pillText else appColors.textPrimary,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    3 -> {
                        // Paso 3: Cuadrícula dinámica con los versículos reales del módulo activo
                        Text(
                            text = if (isEn) "Verses in $bookDisplayName $selectedChapter:" else "Versículos en ${selectedBook.name} $selectedChapter:",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                            color = mutedTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        if (availableVerses.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(28.dp),
                                    color = appColors.accent,
                                    strokeWidth = 2.5.dp
                                )
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(5),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("passage_verses_grid"),
                                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 88.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(
                                    items = availableVerses,
                                    key = { v -> "picker_v_$v" }
                                ) { v ->
                                    Surface(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .clickable { onVerseSelected(v) }
                                            .testTag("verse_button_$v"),
                                        shape = CircleShape,
                                        color = appColors.surfaceVariant,
                                        tonalElevation = 0.dp
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "$v",
                                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                                                fontWeight = FontWeight.SemiBold,
                                                color = appColors.textPrimary,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
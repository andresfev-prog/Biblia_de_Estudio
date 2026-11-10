package com.example.ui.screens.bible

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.VerticalSplit
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.LinkOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BibleBooksData
import com.example.data.model.BibleModule
import com.example.data.model.BibleVerse
import com.example.data.model.BookInfo
import com.example.ui.components.CommentaryBottomSheet
import com.example.ui.components.CrossReferenceBottomSheet
import com.example.ui.components.DictionaryBottomSheet
import com.example.ui.components.NotesBottomSheet
import com.example.ui.components.PassageSelectorDialog
import com.example.ui.components.TtsPlayerBar
import com.example.ui.screens.settings.resolveBibleFont
import com.example.ui.theme.AppColors
import com.example.ui.theme.AppTheme
import com.example.ui.util.StringsLocale
import com.example.ui.viewmodel.BibleUiState
import com.example.ui.viewmodel.BibleViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibleScreen(
    uiState: BibleUiState,
    viewModel: BibleViewModel,
    modifier: Modifier = Modifier
) {
    val leftListState = rememberLazyListState()
    val rightListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var rightBookTitle by remember { mutableStateOf(uiState.currentBook) }
    var rightChapter by remember { mutableIntStateOf(uiState.currentChapter) }
    var rightVerse by remember { mutableIntStateOf(1) }

    LaunchedEffect(uiState.currentBook, uiState.currentChapter, uiState.isParallelLinked) {
        if (uiState.isParallelLinked) {
            rightBookTitle = uiState.currentBook
            rightChapter = uiState.currentChapter
        }
    }

    LaunchedEffect(
        uiState.currentBook,
        uiState.currentChapter,
        uiState.highlightedVerse,
        uiState.verses,
        uiState.parallelVerses
    ) {
        val targetVerse = uiState.highlightedVerse ?: return@LaunchedEffect

        val leftHasChapter = uiState.verses.isNotEmpty() &&
                uiState.verses.first().chapter == uiState.currentChapter
        if (leftHasChapter) {
            val idx = uiState.verses.indexOfFirst { it.verse == targetVerse }
            val targetIdx = if (idx >= 0) idx else (targetVerse - 1).coerceIn(0, uiState.verses.lastIndex)
            leftListState.scrollToItem(targetIdx)
        }

        if (uiState.isParallelMode && uiState.isParallelLinked) {
            val rightHasChapter = uiState.parallelVerses.isNotEmpty() &&
                    uiState.parallelVerses.first().chapter == uiState.currentChapter
            if (rightHasChapter) {
                val idx = uiState.parallelVerses.indexOfFirst { it.verse == targetVerse }
                val targetIdx = if (idx >= 0) idx else (targetVerse - 1).coerceIn(0, uiState.parallelVerses.lastIndex)
                rightListState.scrollToItem(targetIdx)
            }
        }
    }

    LaunchedEffect(leftListState.isScrollInProgress, uiState.isParallelLinked) {
        if (uiState.isParallelLinked && leftListState.isScrollInProgress && !rightListState.isScrollInProgress) {
            snapshotFlow { Pair(leftListState.firstVisibleItemIndex, leftListState.firstVisibleItemScrollOffset) }
                .collect { (idx, offset) ->
                    rightListState.scrollToItem(idx, offset)
                }
        }
    }

    LaunchedEffect(rightListState.isScrollInProgress, uiState.isParallelLinked) {
        if (uiState.isParallelLinked && rightListState.isScrollInProgress && !leftListState.isScrollInProgress) {
            snapshotFlow { Pair(rightListState.firstVisibleItemIndex, rightListState.firstVisibleItemScrollOffset) }
                .collect { (idx, offset) ->
                    leftListState.scrollToItem(idx, offset)
                }
        }
    }

    val lang = uiState.userPreferences.interfaceLanguage
    val appColors = AppTheme.colors

    var isTopBarsVisible by remember { mutableStateOf(true) }
    val chosenFontFamily = resolveBibleFont(uiState.userPreferences.fontFamily)

    val baseFontSizeSp = (uiState.userPreferences.fontSizePt * 1.35f).sp
    val lineHeightSp = (uiState.userPreferences.fontSizePt * 1.9f).sp

    val downloadedVersions = uiState.modules.filter { it.category == "VERSION" && it.isDownloaded }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -8f) {
                    isTopBarsVisible = false
                    viewModel.setBottomBarVisible(false)
                } else if (available.y > 8f) {
                    isTopBarsVisible = true
                    viewModel.setBottomBarVisible(true)
                }
                return Offset.Zero
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(appColors.background)
            .testTag("bible_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AnimatedVisibility(
                visible = isTopBarsVisible,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    ReaderTopControls(
                        currentBook = uiState.currentBook,
                        currentChapter = uiState.currentChapter,
                        activeVersion = uiState.activeVersion,
                        availableVersions = downloadedVersions,
                        isParallelMode = uiState.isParallelMode,
                        isParallelLinked = uiState.isParallelLinked,
                        interfaceLanguage = lang,
                        onOpenPassagePicker = { viewModel.openPassagePicker() },
                        onSelectActiveVersion = { viewModel.setActiveVersion(it) },
                        onToggleParallelMode = { viewModel.toggleParallelMode() },
                        onToggleParallelLinked = { viewModel.toggleParallelLinked() },
                        onToggleTts = { viewModel.toggleTtsBar() },
                        onOpenNotes = { viewModel.openNotesSheet() },
                        appColors = appColors
                    )
                    HorizontalDivider(color = appColors.divider)
                }
            }

            if (uiState.isParallelMode) {
                ParallelReaderView(
                    uiState = uiState,
                    viewModel = viewModel,
                    leftListState = leftListState,
                    rightListState = rightListState,
                    rightBookTitle = rightBookTitle,
                    rightChapter = rightChapter,
                    rightVerse = rightVerse,
                    onUpdateRightPassage = { newBook, newChapter, newVerse ->
                        rightBookTitle = newBook
                        rightChapter = newChapter
                        rightVerse = newVerse
                        viewModel.loadParallelChapter(newBook, newChapter, uiState.parallelVersion)
                        coroutineScope.launch {
                            val targetIndex = (newVerse - 1).coerceAtLeast(0)
                            rightListState.animateScrollToItem(targetIndex)
                        }
                    },
                    fontFamily = chosenFontFamily,
                    fontSize = baseFontSizeSp,
                    lineHeight = lineHeightSp,
                    appColors = appColors,
                    modifier = Modifier
                        .weight(1f)
                        .nestedScroll(nestedScrollConnection)
                )
            } else {
                SingleReaderView(
                    uiState = uiState,
                    viewModel = viewModel,
                    fontFamily = chosenFontFamily,
                    fontSize = baseFontSizeSp,
                    lineHeight = lineHeightSp,
                    listState = leftListState,
                    modifier = Modifier
                        .weight(1f)
                        .nestedScroll(nestedScrollConnection)
                )
            }
        }

        TtsPlayerBar(
            isVisible = uiState.isTtsVisible,
            isPlaying = uiState.isTtsPlaying,
            bookName = uiState.currentBook,
            chapter = uiState.currentChapter,
            currentVerse = if (uiState.verses.isNotEmpty()) uiState.verses.getOrNull(uiState.ttsVerseIndex)?.verse ?: 1 else 1,
            totalVerses = uiState.verses.size,
            onPlay = { viewModel.playTts() },
            onPause = { viewModel.pauseTts() },
            onNext = { viewModel.nextTtsVerse() },
            onPrev = { viewModel.prevTtsVerse() },
            onClose = { viewModel.stopTts() },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        if (uiState.isPassagePickerVisible) {
            PassageSelectorDialog(
                currentStep = uiState.pickerStep,
                selectedBook = uiState.pickerSelectedBook,
                selectedChapter = uiState.pickerSelectedChapter,
                onBookSelected = { viewModel.onPickerBookSelected(it) },
                onChapterSelected = { viewModel.onPickerChapterSelected(it) },
                onVerseSelected = { viewModel.onPickerVerseSelected(it) },
                onBackStep = { viewModel.onPickerBackStep() },
                onDismiss = { viewModel.dismissPassagePicker() }
            )
        }

        uiState.selectedCommentaryVerse?.let { verse ->
            CommentaryBottomSheet(
                verse = verse,
                commentaries = uiState.currentVerseCommentaries,
                interfaceLanguage = lang,
                onDismiss = { viewModel.dismissCommentary() }
            )
        }

        uiState.selectedCrossReferenceVerse?.let { verse ->
            CrossReferenceBottomSheet(
                verse = verse,
                crossReferences = uiState.currentVerseCrossReferences,
                interfaceLanguage = lang,
                onNavigateToTarget = { book, chap, v ->
                    viewModel.dismissCrossReferences()
                    viewModel.navigateToPassage(book, chap, v)
                },
                onDismiss = { viewModel.dismissCrossReferences() }
            )
        }

        uiState.selectedDictionaryWord?.let { word ->
            DictionaryBottomSheet(
                searchedWord = word,
                entry = uiState.currentDictionaryEntry,
                isLoading = uiState.isWordLookupLoading,
                interfaceLanguage = lang,
                onDismiss = { viewModel.dismissDictionary() }
            )
        }

        if (uiState.isNotesSheetOpen) {
            NotesBottomSheet(
                notes = uiState.notes,
                editingNote = uiState.editingNote,
                interfaceLanguage = lang,
                onSaveNote = { title, content, ref, id ->
                    viewModel.saveNote(title, content, ref, id)
                },
                onDeleteNote = { id ->
                    viewModel.deleteNote(id)
                },
                onSelectNoteToEdit = { note ->
                    viewModel.setEditingNote(note)
                },
                onDismiss = { viewModel.closeNotesSheet() }
            )
        }
    }
}

@Composable
private fun ReaderTopControls(
    currentBook: String,
    currentChapter: Int,
    activeVersion: String,
    availableVersions: List<BibleModule>,
    isParallelMode: Boolean,
    isParallelLinked: Boolean,
    interfaceLanguage: String,
    onOpenPassagePicker: () -> Unit,
    onSelectActiveVersion: (String) -> Unit,
    onToggleParallelMode: () -> Unit,
    onToggleParallelLinked: () -> Unit,
    onToggleTts: () -> Unit,
    onOpenNotes: () -> Unit,
    appColors: AppColors,
    modifier: Modifier = Modifier
) {
    var versionMenuExpanded by remember { mutableStateOf(false) }
    var overflowMenuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(if (isParallelMode) 42.dp else 56.dp)
            .padding(horizontal = 12.dp, vertical = if (isParallelMode) 2.dp else 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (!isParallelMode) {
            Surface(
                shape = CircleShape,
                color = appColors.surfaceVariant,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onOpenPassagePicker() }
                    .testTag("passage_selector_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$currentBook $currentChapter",
                        style = MaterialTheme.typography.labelLarge,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = appColors.textPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = appColors.textSecondary
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.width(8.dp))
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isParallelMode) {
                IconButton(
                    onClick = onToggleParallelLinked,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isParallelLinked) Icons.Rounded.Link else Icons.Rounded.LinkOff,
                        contentDescription = if (isParallelLinked) "Modo Enlazado" else "Modo Libre",
                        tint = if (isParallelLinked) appColors.accent else appColors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }

            if (!isParallelMode) {
                Box {
                    Surface(
                        shape = CircleShape,
                        color = appColors.surfaceVariant,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { versionMenuExpanded = true }
                            .testTag("version_selector_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = activeVersion,
                                style = MaterialTheme.typography.labelLarge,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = appColors.textPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = appColors.textSecondary
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = versionMenuExpanded,
                        onDismissRequest = { versionMenuExpanded = false }
                    ) {
                        if (availableVersions.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Reina-Valera 1960 (RVR1960)", color = appColors.textPrimary) },
                                onClick = {
                                    onSelectActiveVersion("RVR1960")
                                    versionMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("King James Version (KJV)", color = appColors.textPrimary) },
                                onClick = {
                                    onSelectActiveVersion("KJV")
                                    versionMenuExpanded = false
                                }
                            )
                        } else {
                            availableVersions.forEach { module ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "${module.title} (${module.versionTag})",
                                            color = if (activeVersion == module.versionTag) appColors.accent else appColors.textPrimary,
                                            fontWeight = if (activeVersion == module.versionTag) FontWeight.Bold else FontWeight.Normal,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    },
                                    onClick = {
                                        onSelectActiveVersion(module.versionTag)
                                        versionMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.width(4.dp))
            }

            Box {
                IconButton(
                    onClick = { overflowMenuExpanded = true },
                    modifier = Modifier.size(36.dp).testTag("bible_overflow_menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "Más opciones",
                        tint = appColors.textPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                DropdownMenu(
                    expanded = overflowMenuExpanded,
                    onDismissRequest = { overflowMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null,
                                tint = appColors.accent
                            )
                        },
                        text = {
                            Text(
                                text = StringsLocale.get("overflow_audio", interfaceLanguage),
                                color = appColors.textPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        onClick = {
                            overflowMenuExpanded = false
                            onToggleTts()
                        },
                        modifier = Modifier.testTag("overflow_option_audio")
                    )
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.VerticalSplit,
                                contentDescription = null,
                                tint = if (isParallelMode) appColors.accent else appColors.textSecondary
                            )
                        },
                        text = {
                            Text(
                                text = StringsLocale.get("overflow_split", interfaceLanguage),
                                color = appColors.textPrimary,
                                fontWeight = if (isParallelMode) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        onClick = {
                            overflowMenuExpanded = false
                            onToggleParallelMode()
                        },
                        modifier = Modifier.testTag("overflow_option_split")
                    )
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.EditNote,
                                contentDescription = null,
                                tint = appColors.accent
                            )
                        },
                        text = {
                            Text(
                                text = StringsLocale.get("overflow_notes", interfaceLanguage),
                                color = appColors.textPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        onClick = {
                            overflowMenuExpanded = false
                            onOpenNotes()
                        },
                        modifier = Modifier.testTag("overflow_option_notes")
                    )
                }
            }
        }
    }
}

@Composable
private fun SingleReaderView(
    uiState: BibleUiState,
    viewModel: BibleViewModel,
    fontFamily: FontFamily,
    fontSize: TextUnit,
    lineHeight: TextUnit,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    val appColors = AppTheme.colors

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp)
            .testTag("single_reader_list")
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 10.dp)
            ) {
                Text(
                    text = "${uiState.currentBook} ${uiState.currentChapter}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = appColors.titleColor,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }

        items(
            count = uiState.verses.size,
            key = { index -> "${uiState.verses[index].bookName}_${uiState.verses[index].chapter}_${uiState.verses[index].verse}_$index" }
        ) { index ->
            val verse = uiState.verses[index]
            val hasCommentary = verse.verse in uiState.versesWithComments
            val hasCrossReference = verse.verse in uiState.versesWithCrossReferences

            VerseRowItem(
                verse = verse,
                hasCommentary = hasCommentary,
                hasCrossReference = hasCrossReference,
                isHighlighted = uiState.highlightedVerse == verse.verse,
                fontFamily = fontFamily,
                fontSize = fontSize,
                lineHeight = lineHeight,
                onCommentaryClick = { code -> viewModel.openCommentary(verse, code) },
                onCrossRefClick = { viewModel.openCrossReferences(verse) },
                onWordClick = { word -> viewModel.lookupWord(word, verse) },
                onNoteClick = { marker -> viewModel.onVersionNoteClick(verse, marker) },
                onFavoriteToggle = { viewModel.toggleFavorite(verse) }
            )
        }

        item {
            val totalChapters = BibleBooksData.getBookByName(uiState.currentBook)?.chapterCount ?: 1
            ChapterBottomNavigationControls(
                currentChapter = uiState.currentChapter,
                totalChapters = totalChapters,
                onPreviousChapter = {
                    if (uiState.currentChapter > 1) {
                        viewModel.loadChapter(uiState.currentBook, uiState.currentChapter - 1)
                    }
                },
                onNextChapter = {
                    if (uiState.currentChapter < totalChapters) {
                        viewModel.loadChapter(uiState.currentBook, uiState.currentChapter + 1)
                    }
                }
            )
            Spacer(modifier = Modifier.height(90.dp))
        }
    }
}

@Composable
private fun ParallelReaderView(
    uiState: BibleUiState,
    viewModel: BibleViewModel,
    leftListState: LazyListState,
    rightListState: LazyListState,
    rightBookTitle: String,
    rightChapter: Int,
    rightVerse: Int,
    onUpdateRightPassage: (String, Int, Int) -> Unit,
    fontFamily: FontFamily,
    fontSize: TextUnit,
    lineHeight: TextUnit,
    appColors: AppColors,
    modifier: Modifier = Modifier
) {
    val downloadedVersions = uiState.modules.filter { it.category == "VERSION" && it.isDownloaded }
    val isLinked = uiState.isParallelLinked

    var isLeftVersionMenuOpen by remember { mutableStateOf(false) }
    var isRightVersionMenuOpen by remember { mutableStateOf(false) }

    var isRightPickerOpen by remember { mutableStateOf(false) }
    var rightPickerStep by remember { mutableIntStateOf(1) }
    var rightBookInfo by remember {
        mutableStateOf<BookInfo?>(
            BibleBooksData.getBookByName(rightBookTitle)
                ?: BibleBooksData.books.firstOrNull { it.name.equals(rightBookTitle, ignoreCase = true) }
                ?: BibleBooksData.books.first()
        )
    }
    var rightPickerChapter by remember { mutableIntStateOf(rightChapter) }

    Row(modifier = modifier.fillMaxSize()) {
        // =====================================================================
        // PANEL IZQUIERDO (COLUMNA IZQUIERDA)
        // =====================================================================
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { viewModel.openPassagePicker() }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${uiState.currentBook} ${uiState.currentChapter}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = appColors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Cambiar pasaje izquierdo",
                        modifier = Modifier.size(16.dp),
                        tint = appColors.textSecondary
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Box {
                    Surface(
                        shape = CircleShape,
                        color = appColors.surfaceVariant,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { isLeftVersionMenuOpen = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = uiState.activeVersion,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 11.sp,
                                maxLines = 1,
                                softWrap = false,
                                color = appColors.accent,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Cambiar versión panel izquierdo",
                                modifier = Modifier.size(14.dp),
                                tint = appColors.accent
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = isLeftVersionMenuOpen,
                        onDismissRequest = { isLeftVersionMenuOpen = false }
                    ) {
                        if (downloadedVersions.isEmpty()) {
                            listOf("RVR1960" to "Reina-Valera 1960 (RVR1960)", "KJV" to "King James Version (KJV)").forEach { (tag, label) ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = label,
                                            fontWeight = if (uiState.activeVersion == tag) FontWeight.Bold else FontWeight.Normal,
                                            color = if (uiState.activeVersion == tag) appColors.accent else appColors.textPrimary
                                        )
                                    },
                                    onClick = {
                                        isLeftVersionMenuOpen = false
                                        viewModel.setActiveVersion(tag)
                                    }
                                )
                            }
                        } else {
                            downloadedVersions.forEach { module ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "${module.title} (${module.versionTag})",
                                            fontWeight = if (uiState.activeVersion == module.versionTag) FontWeight.Bold else FontWeight.Normal,
                                            color = if (uiState.activeVersion == module.versionTag) appColors.accent else appColors.textPrimary,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    },
                                    onClick = {
                                        isLeftVersionMenuOpen = false
                                        viewModel.setActiveVersion(module.versionTag)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = appColors.divider.copy(alpha = 0.5f), thickness = 0.8.dp)

            LazyColumn(
                state = leftListState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 10.dp,
                    top = 8.dp,
                    bottom = 16.dp
                )
            ) {
                items(
                    count = uiState.verses.size,
                    key = { index ->
                        val v = uiState.verses[index]
                        "L_${v.bookName}_${v.chapter}_${v.verse}_$index"
                    }
                ) { index ->
                    val verse = uiState.verses[index]
                    val hasCommentary = verse.verse in uiState.versesWithComments
                    val hasCrossReference = verse.verse in uiState.versesWithCrossReferences

                    VerseRowItem(
                        verse = verse,
                        hasCommentary = hasCommentary,
                        hasCrossReference = hasCrossReference,
                        isHighlighted = uiState.highlightedVerse == verse.verse,
                        fontFamily = fontFamily,
                        fontSize = fontSize,
                        lineHeight = lineHeight,
                        onCommentaryClick = { code -> viewModel.openCommentary(verse, code) },
                        onCrossRefClick = { viewModel.openCrossReferences(verse) },
                        onWordClick = { word -> viewModel.lookupWord(word, verse) },
                        onNoteClick = { marker -> viewModel.onVersionNoteClick(verse, marker) },
                        onFavoriteToggle = { viewModel.toggleFavorite(verse) }
                    )
                }

                item {
                    val totalChapters = BibleBooksData.getBookByName(uiState.currentBook)?.chapterCount ?: 1
                    ChapterBottomNavigationControls(
                        currentChapter = uiState.currentChapter,
                        totalChapters = totalChapters,
                        onPreviousChapter = {
                            if (uiState.currentChapter > 1) {
                                viewModel.loadChapter(uiState.currentBook, uiState.currentChapter - 1)
                            }
                        },
                        onNextChapter = {
                            if (uiState.currentChapter < totalChapters) {
                                viewModel.loadChapter(uiState.currentBook, uiState.currentChapter + 1)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(90.dp))
                }
            }
        }

        VerticalDivider(
            color = appColors.divider,
            thickness = 1.dp,
            modifier = Modifier.fillMaxHeight()
        )

        // =====================================================================
        // PANEL DERECHO (COLUMNA DERECHA)
        // =====================================================================
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            val displayRightTitle = if (isLinked) uiState.currentBook else rightBookTitle
            val displayRightChapter = if (isLinked) uiState.currentChapter else rightChapter

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(enabled = !isLinked) {
                            rightBookInfo = BibleBooksData.getBookByName(displayRightTitle)
                                ?: BibleBooksData.books.firstOrNull { it.name.equals(displayRightTitle, ignoreCase = true) }
                                        ?: BibleBooksData.books.first()
                            rightPickerChapter = displayRightChapter
                            rightPickerStep = 1
                            isRightPickerOpen = true
                        }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$displayRightTitle $displayRightChapter",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = appColors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!isLinked) {
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Cambiar pasaje derecho",
                            modifier = Modifier.size(16.dp),
                            tint = appColors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Box {
                    Surface(
                        shape = CircleShape,
                        color = appColors.surfaceVariant,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { isRightVersionMenuOpen = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = uiState.parallelVersion,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 11.sp,
                                maxLines = 1,
                                softWrap = false,
                                color = appColors.accent,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Cambiar versión panel derecho",
                                modifier = Modifier.size(14.dp),
                                tint = appColors.accent
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = isRightVersionMenuOpen,
                        onDismissRequest = { isRightVersionMenuOpen = false }
                    ) {
                        if (downloadedVersions.isEmpty()) {
                            listOf("RVR1960" to "Reina-Valera 1960 (RVR1960)", "KJV" to "King James Version (KJV)").forEach { (tag, label) ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = label,
                                            fontWeight = if (uiState.parallelVersion == tag) FontWeight.Bold else FontWeight.Normal,
                                            color = if (uiState.parallelVersion == tag) appColors.accent else appColors.textPrimary
                                        )
                                    },
                                    onClick = {
                                        isRightVersionMenuOpen = false
                                        if (uiState.isParallelLinked) {
                                            viewModel.setParallelVersion(tag)
                                        } else {
                                            viewModel.loadParallelChapter(
                                                bookName = rightBookTitle,
                                                chapter = rightChapter,
                                                version = tag
                                            )
                                        }
                                    }
                                )
                            }
                        } else {
                            downloadedVersions.forEach { module ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "${module.title} (${module.versionTag})",
                                            fontWeight = if (uiState.parallelVersion == module.versionTag) FontWeight.Bold else FontWeight.Normal,
                                            color = if (uiState.parallelVersion == module.versionTag) appColors.accent else appColors.textPrimary,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    },
                                    onClick = {
                                        isRightVersionMenuOpen = false
                                        if (uiState.isParallelLinked) {
                                            viewModel.setParallelVersion(module.versionTag)
                                        } else {
                                            viewModel.loadParallelChapter(
                                                bookName = rightBookTitle,
                                                chapter = rightChapter,
                                                version = module.versionTag
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = appColors.divider.copy(alpha = 0.5f), thickness = 0.8.dp)

            LazyColumn(
                state = rightListState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 10.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 16.dp
                )
            ) {
                items(
                    count = uiState.parallelVerses.size,
                    key = { index ->
                        val v = uiState.parallelVerses[index]
                        "R_${v.bookName}_${v.chapter}_${v.verse}_$index"
                    }
                ) { index ->
                    val verse = uiState.parallelVerses[index]
                    val hasCommentary = if (isLinked) {
                        verse.verse in uiState.versesWithComments
                    } else {
                        verse.verse in uiState.parallelVersesWithComments
                    }
                    val hasCrossReference = if (isLinked) {
                        verse.verse in uiState.versesWithCrossReferences
                    } else {
                        verse.verse in uiState.parallelVersesWithCrossReferences
                    }

                    VerseRowItem(
                        verse = verse,
                        hasCommentary = hasCommentary,
                        hasCrossReference = hasCrossReference,
                        isHighlighted = rightVerse == verse.verse,
                        fontFamily = fontFamily,
                        fontSize = fontSize,
                        lineHeight = lineHeight,
                        onCommentaryClick = { code -> viewModel.openCommentary(verse, code) },
                        onCrossRefClick = { viewModel.openCrossReferences(verse) },
                        onWordClick = { word -> viewModel.lookupWord(word, verse) },
                        onNoteClick = { marker -> viewModel.onVersionNoteClick(verse, marker) },
                        onFavoriteToggle = { /* Sin acción en columna paralela */ }
                    )
                }

                item {
                    val rightBook = if (isLinked) uiState.currentBook else (rightBookInfo?.name ?: rightBookTitle)
                    val activeRightChapter = if (isLinked) uiState.currentChapter else rightChapter
                    val totalChapters = BibleBooksData.getBookByName(rightBook)?.chapterCount ?: 1
                    ChapterBottomNavigationControls(
                        currentChapter = activeRightChapter,
                        totalChapters = totalChapters,
                        onPreviousChapter = {
                            if (activeRightChapter > 1) {
                                if (isLinked) {
                                    viewModel.loadChapter(uiState.currentBook, activeRightChapter - 1)
                                } else {
                                    onUpdateRightPassage(rightBook, activeRightChapter - 1, 1)
                                }
                            }
                        },
                        onNextChapter = {
                            if (activeRightChapter < totalChapters) {
                                if (isLinked) {
                                    viewModel.loadChapter(uiState.currentBook, activeRightChapter + 1)
                                } else {
                                    onUpdateRightPassage(rightBook, activeRightChapter + 1, 1)
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(90.dp))
                }
            }
        }
    }

    if (isRightPickerOpen && !isLinked) {
        PassageSelectorDialog(
            currentStep = rightPickerStep,
            selectedBook = rightBookInfo ?: BibleBooksData.books.first(),
            selectedChapter = rightPickerChapter,
            onBookSelected = { selected ->
                rightBookInfo = selected
                rightPickerStep = 2
            },
            onChapterSelected = { ch ->
                rightPickerChapter = ch
                rightPickerStep = 3
            },
            onVerseSelected = { v ->
                val selectedBookName = rightBookInfo?.name ?: rightBookTitle
                isRightPickerOpen = false
                rightPickerStep = 1
                onUpdateRightPassage(selectedBookName, rightPickerChapter, v)
            },
            onBackStep = {
                if (rightPickerStep > 1) {
                    rightPickerStep--
                } else {
                    isRightPickerOpen = false
                }
            },
            onDismiss = {
                isRightPickerOpen = false
                rightPickerStep = 1
            }
        )
    }
}

@Composable
private fun ChapterBottomNavigationControls(
    currentChapter: Int,
    totalChapters: Int,
    onPreviousChapter: () -> Unit,
    onNextChapter: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appColors = AppTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (currentChapter > 1) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = appColors.surfaceVariant,
                border = BorderStroke(1.dp, appColors.border),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onPreviousChapter() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "‹",
                        color = appColors.accent,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Capítulo ${currentChapter - 1}",
                        color = appColors.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.width(1.dp))
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (currentChapter < totalChapters) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = appColors.surfaceVariant,
                border = BorderStroke(1.dp, appColors.border),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onNextChapter() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Capítulo ${currentChapter + 1}",
                        color = appColors.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "›",
                        color = appColors.accent,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.width(1.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun VerseRowItem(
    verse: BibleVerse,
    hasCommentary: Boolean,
    hasCrossReference: Boolean,
    isHighlighted: Boolean,
    fontFamily: FontFamily,
    fontSize: TextUnit,
    lineHeight: TextUnit,
    onCommentaryClick: (code: String) -> Unit,
    onCrossRefClick: () -> Unit,
    onWordClick: (String) -> Unit,
    onNoteClick: (marker: String) -> Unit,
    onFavoriteToggle: () -> Unit
) {
    val appColors = AppTheme.colors
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val rawCleanText = remember(verse.text) {
        verse.text
            .replace(Regex("<S>.*?</S>|<.*?>"), "")
            .replace(Regex("(?<=\\p{L})\\d+"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    val copyVerseToClipboard = {
        val formattedText = "${verse.bookName} ${verse.chapter}:${verse.verse}\n$rawCleanText"
        clipboardManager.setText(AnnotatedString(formattedText))
        Toast.makeText(
            context,
            "Copiado: ${verse.bookName} ${verse.chapter}:${verse.verse}",
            Toast.LENGTH_SHORT
        ).show()
    }

    val annotatedString = remember(
        verse.verse,
        rawCleanText,
        appColors.verseNumberColor,
        appColors.textPrimary,
        appColors.accent,
        fontSize
    ) {
        buildAnnotatedString {
            // Número del versículo al inicio en la misma línea visual base
            pushStringAnnotation(tag = "VERSE_NUM", annotation = "${verse.verse}")
            withStyle(
                SpanStyle(
                    color = appColors.verseNumberColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = fontSize
                )
            ) {
                append("${verse.verse} ")
            }
            pop()

            // Tokenizar texto en: notas de versión [n], palabras interactuables y puntuación/espacios
            val tokenPattern = Regex("(\\[(\\d+)\\])|([\\p{L}\\p{Nd}]+)|([^\\s\\[\\]\\p{L}\\p{Nd}]+)|(\\s+)")
            val noteStyle = SpanStyle(
                color = appColors.accent,
                fontSize = (fontSize.value * 0.70f).sp,
                baselineShift = BaselineShift.Superscript,
                fontWeight = FontWeight.Bold
            )
            val wordStyle = SpanStyle(
                color = appColors.textPrimary
            )

            tokenPattern.findAll(rawCleanText).forEach { match ->
                val noteMarker = match.groups[2]?.value
                val word = match.groups[3]?.value
                when {
                    noteMarker != null -> {
                        pushStringAnnotation(tag = "NOTE", annotation = noteMarker)
                        withStyle(noteStyle) {
                            append("[$noteMarker]")
                        }
                        pop()
                    }
                    word != null -> {
                        pushStringAnnotation(tag = "WORD", annotation = word)
                        withStyle(wordStyle) {
                            append(word)
                        }
                        pop()
                    }
                    else -> {
                        withStyle(wordStyle) {
                            append(match.value)
                        }
                    }
                }
            }
        }
    }

    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isHighlighted) appColors.surfaceVariant
                else androidx.compose.ui.graphics.Color.Transparent
            )
            .combinedClickable(
                onClick = { },
                onLongClick = { copyVerseToClipboard() }
            )
            .padding(vertical = 4.dp, horizontal = 2.dp)
            .testTag("verse_item_${verse.verse}")
    ) {
        if (!verse.sectionTitle.isNullOrBlank()) {
            Text(
                text = verse.sectionTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = appColors.titleColor,
                fontFamily = fontFamily,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        // Renderizado unificado y continuo en una sola pasada (sin descomposición FlowRow)
        Text(
            text = annotatedString,
            fontSize = fontSize,
            lineHeight = lineHeight,
            fontFamily = fontFamily,
            onTextLayout = { layoutResult = it },
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(annotatedString) {
                    detectTapGestures(
                        onLongPress = { copyVerseToClipboard() },
                        onTap = { pos ->
                            layoutResult?.let { layout ->
                                val offset = layout.getOffsetForPosition(pos)
                                val noteAnnotation = annotatedString.getStringAnnotations("NOTE", offset, offset).firstOrNull()
                                if (noteAnnotation != null) {
                                    onNoteClick(noteAnnotation.item)
                                    return@detectTapGestures
                                }
                                val verseNumAnnotation = annotatedString.getStringAnnotations("VERSE_NUM", offset, offset).firstOrNull()
                                if (verseNumAnnotation != null) {
                                    onFavoriteToggle()
                                    return@detectTapGestures
                                }
                                val wordAnnotation = annotatedString.getStringAnnotations("WORD", offset, offset).firstOrNull()
                                if (wordAnnotation != null && wordAnnotation.item.isNotBlank()) {
                                    onWordClick(wordAnnotation.item)
                                    return@detectTapGestures
                                }
                            }
                        }
                    )
                }
        )

        // Píldoras compactas COM y REF alineadas a la derecha al cierre del versículo
        if (hasCommentary || hasCrossReference) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp, end = 2.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (hasCommentary) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (appColors.isDark) {
                            androidx.compose.ui.graphics.Color(0xFF0284C7).copy(alpha = 0.22f)
                        } else {
                            androidx.compose.ui.graphics.Color(0xFFE0F2FE)
                        },
                        border = BorderStroke(
                            0.6.dp,
                            if (appColors.isDark) androidx.compose.ui.graphics.Color(0xFF38BDF8)
                            else androidx.compose.ui.graphics.Color(0xFF0284C7)
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onCommentaryClick("COM") }
                            .testTag("badge_com_${verse.verse}")
                    ) {
                        Text(
                            text = "COM",
                            color = if (appColors.isDark) {
                                androidx.compose.ui.graphics.Color(0xFF38BDF8)
                            } else {
                                androidx.compose.ui.graphics.Color(0xFF0369A1)
                            },
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }

                if (hasCrossReference) {
                    if (hasCommentary) {
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (appColors.isDark) {
                            androidx.compose.ui.graphics.Color(0xFF047857).copy(alpha = 0.22f)
                        } else {
                            androidx.compose.ui.graphics.Color(0xFFD1FAE5)
                        },
                        border = BorderStroke(
                            0.6.dp,
                            if (appColors.isDark) androidx.compose.ui.graphics.Color(0xFF34D399)
                            else androidx.compose.ui.graphics.Color(0xFF059669)
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onCrossRefClick() }
                            .testTag("badge_ref_${verse.verse}")
                    ) {
                        Text(
                            text = "REF",
                            color = if (appColors.isDark) {
                                androidx.compose.ui.graphics.Color(0xFF34D399)
                            } else {
                                androidx.compose.ui.graphics.Color(0xFF047857)
                            },
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }
        }
    }
}
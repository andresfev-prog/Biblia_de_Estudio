package com.example.ui.screens.bible

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VerticalSplit
import androidx.compose.material.icons.outlined.FavoriteBorder
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
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
import com.example.ui.components.TutorialStep
import com.example.ui.components.tutorialTarget
import com.example.ui.screens.settings.resolveBibleFont
import com.example.ui.theme.AppColors
import com.example.ui.theme.AppIcon
import com.example.ui.theme.AppIcons
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
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var selectedVerses by remember { mutableStateOf(emptySet<Int>()) }
    var selectedIsRightPane by remember { mutableStateOf(false) }

    LaunchedEffect(
        uiState.currentBook,
        uiState.currentChapter,
        uiState.activeVersion,
        uiState.parallelVersion,
        uiState.isParallelMode
    ) {
        selectedVerses = emptySet()
        selectedIsRightPane = false
    }

    LaunchedEffect(rightBookTitle, rightChapter, uiState.isParallelLinked) {
        if (selectedIsRightPane) {
            selectedVerses = emptySet()
            selectedIsRightPane = false
        }
    }

    BackHandler(enabled = selectedVerses.isNotEmpty()) {
        selectedVerses = emptySet()
        selectedIsRightPane = false
    }

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

    // Sincronización de visibilidad y scroll para el tutorial en vivo
    LaunchedEffect(uiState.activeTutorialStep) {
        if (uiState.activeTutorialStep == TutorialStep.BIBLE_TOP_CONTROLS ||
            uiState.activeTutorialStep == TutorialStep.BIBLE_VERSES_INTERACTION) {
            isTopBarsVisible = true
            viewModel.setBottomBarVisible(true)
            if (uiState.activeTutorialStep == TutorialStep.BIBLE_VERSES_INTERACTION) {
                leftListState.animateScrollToItem(0)
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
                Column(
                    modifier = Modifier.tutorialTarget(TutorialStep.BIBLE_TOP_CONTROLS)
                ) {
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
                    selectedVerses = selectedVerses,
                    selectedIsRightPane = selectedIsRightPane,
                    onLeftVerseClick = { verseNum ->
                        if (!selectedIsRightPane) {
                            selectedVerses = if (verseNum in selectedVerses) {
                                selectedVerses - verseNum
                            } else {
                                selectedVerses + verseNum
                            }
                        } else {
                            selectedIsRightPane = false
                            selectedVerses = setOf(verseNum)
                        }
                    },
                    onLeftVerseLongClick = { verseNum ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (!selectedIsRightPane) {
                            selectedVerses = selectedVerses + verseNum
                        } else {
                            selectedIsRightPane = false
                            selectedVerses = setOf(verseNum)
                        }
                    },
                    onRightVerseClick = { verseNum ->
                        if (selectedIsRightPane) {
                            val updated = if (verseNum in selectedVerses) {
                                selectedVerses - verseNum
                            } else {
                                selectedVerses + verseNum
                            }
                            selectedVerses = updated
                            if (updated.isEmpty()) selectedIsRightPane = false
                        } else {
                            selectedIsRightPane = true
                            selectedVerses = setOf(verseNum)
                        }
                    },
                    onRightVerseLongClick = { verseNum ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (selectedIsRightPane) {
                            selectedVerses = selectedVerses + verseNum
                        } else {
                            selectedIsRightPane = true
                            selectedVerses = setOf(verseNum)
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
                    selectedVerses = if (selectedIsRightPane) emptySet() else selectedVerses,
                    onVerseClick = { verseNum ->
                        selectedIsRightPane = false
                        selectedVerses = if (verseNum in selectedVerses) {
                            selectedVerses - verseNum
                        } else {
                            selectedVerses + verseNum
                        }
                    },
                    onVerseLongClick = { verseNum ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedIsRightPane = false
                        selectedVerses = selectedVerses + verseNum
                    },
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

        val ttsBottomPadding by animateDpAsState(
            targetValue = if (uiState.isBottomBarVisible) 88.dp else 4.dp,
            label = "ttsBottomPadding"
        )
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
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = ttsBottomPadding)
        )

        val activeBookRaw = if (selectedIsRightPane) {
            if (uiState.isParallelLinked) uiState.currentBook else rightBookTitle
        } else {
            uiState.currentBook
        }
        val activeChapterNum = if (selectedIsRightPane) {
            if (uiState.isParallelLinked) uiState.currentChapter else rightChapter
        } else {
            uiState.currentChapter
        }
        val activeVersionTag = if (selectedIsRightPane) uiState.parallelVersion else uiState.activeVersion
        val activeVerseList = if (selectedIsRightPane) uiState.parallelVerses else uiState.verses

        val selectedVerseObjects = remember(selectedVerses, activeVerseList) {
            if (selectedVerses.isEmpty()) {
                emptyList()
            } else {
                activeVerseList.filter { it.verse in selectedVerses }.sortedBy { it.verse }
            }
        }

        val allSelectedAreFavorites = remember(selectedVerseObjects, uiState.favoriteKeys) {
            selectedVerseObjects.isNotEmpty() &&
                selectedVerseObjects.all { viewModel.isFavorite(it, uiState.favoriteKeys) }
        }

        val selectionPillBottomPadding by animateDpAsState(
            targetValue = when {
                uiState.isTtsVisible && uiState.isBottomBarVisible -> 196.dp
                uiState.isTtsVisible -> 112.dp
                uiState.isBottomBarVisible -> 96.dp
                else -> 16.dp
            },
            label = "selectionPillBottomPadding"
        )

        AnimatedVisibility(
            visible = selectedVerseObjects.isNotEmpty(),
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = selectionPillBottomPadding, start = 16.dp, end = 16.dp)
        ) {
            ContextualSelectionPill(
                allSelectedAreFavorites = allSelectedAreFavorites,
                interfaceLanguage = lang,
                onToggleFavorites = {
                    if (allSelectedAreFavorites) {
                        viewModel.removeFavorites(selectedVerseObjects)
                    } else {
                        viewModel.addFavorites(selectedVerseObjects)
                    }
                },
                onCopy = {
                    val formattedPassage = buildFormattedPassageText(
                        verses = selectedVerseObjects,
                        fallbackBookName = activeBookRaw,
                        fallbackChapter = activeChapterNum,
                        versionTag = activeVersionTag
                    )
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText(
                        StringsLocale.get("copy_action", lang),
                        formattedPassage
                    )
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(
                        context,
                        StringsLocale.get("copied_clipboard", lang),
                        Toast.LENGTH_SHORT
                    ).show()
                    selectedVerses = emptySet()
                    selectedIsRightPane = false
                },
                onShare = {
                    val formattedPassage = buildFormattedPassageText(
                        verses = selectedVerseObjects,
                        fallbackBookName = activeBookRaw,
                        fallbackChapter = activeChapterNum,
                        versionTag = activeVersionTag
                    )
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, formattedPassage)
                    }
                    try {
                        context.startActivity(
                            Intent.createChooser(
                                sendIntent,
                                StringsLocale.get("share_verse", lang)
                            )
                        )
                    } catch (_: Exception) { }
                    selectedVerses = emptySet()
                    selectedIsRightPane = false
                },
                onClose = {
                    selectedVerses = emptySet()
                    selectedIsRightPane = false
                }
            )
        }

        if (uiState.isPassagePickerVisible) {
            PassageSelectorDialog(
                currentStep = uiState.pickerStep,
                selectedBook = uiState.pickerSelectedBook,
                selectedChapter = uiState.pickerSelectedChapter,
                interfaceLanguage = lang,
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
                matches = uiState.dictionaryMatches,
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

    val isVersionEn = BibleBooksData.isEnglishVersion(activeVersion)
    val bookInfo = BibleBooksData.getBookByName(currentBook)
    val bookId = bookInfo?.number ?: 43
    val bookName = BibleBooksData.getLocalizedBookName(bookId, isVersionEn)

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
                        text = "$bookName $currentChapter",
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
                        onDismissRequest = { versionMenuExpanded = false },
                        shape = RoundedCornerShape(16.dp),
                        containerColor = appColors.surface,
                        tonalElevation = 0.dp,
                        shadowElevation = 8.dp,
                        border = BorderStroke(1.dp, appColors.border.copy(alpha = if (appColors.isDark) 0.85f else 0.75f))
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
                    AppIcon(
                        glyph = AppIcons.ThreeDotsMenu,
                        contentDescription = "Más opciones",
                        tint = appColors.textPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                DropdownMenu(
                    expanded = overflowMenuExpanded,
                    onDismissRequest = { overflowMenuExpanded = false },
                    shape = RoundedCornerShape(16.dp),
                    containerColor = appColors.surface,
                    tonalElevation = 0.dp,
                    shadowElevation = 8.dp,
                    border = BorderStroke(1.dp, appColors.border.copy(alpha = if (appColors.isDark) 0.85f else 0.75f))
                ) {
                    DropdownMenuItem(
                        leadingIcon = {
                            AppIcon(
                                glyph = AppIcons.TtsAudio,
                                contentDescription = null,
                                tint = appColors.accent,
                                modifier = Modifier.size(24.dp)
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
                            AppIcon(
                                glyph = AppIcons.SplitScreen,
                                contentDescription = null,
                                tint = if (isParallelMode) appColors.accent else appColors.textSecondary,
                                modifier = Modifier.size(24.dp)
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
                            AppIcon(
                                glyph = AppIcons.MyNotes,
                                contentDescription = null,
                                tint = appColors.accent,
                                modifier = Modifier.size(24.dp)
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
    selectedVerses: Set<Int>,
    onVerseClick: (Int) -> Unit,
    onVerseLongClick: (Int) -> Unit,
    fontFamily: FontFamily,
    fontSize: TextUnit,
    lineHeight: TextUnit,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    val appColors = AppTheme.colors
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val isSelectionMode = selectedVerses.isNotEmpty()

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(
            start = 18.dp,
            end = 18.dp,
            bottom = 96.dp + navBarBottom
        ),
        modifier = modifier
            .fillMaxSize()
            .testTag("single_reader_list")
    ) {
        item {
            val isVersionEn = BibleBooksData.isEnglishVersion(uiState.activeVersion)
            val bookInfo = BibleBooksData.getBookByName(uiState.currentBook)
            val bookId = bookInfo?.number ?: 43
            val displayBookName = BibleBooksData.getLocalizedBookName(bookId, isVersionEn)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 10.dp)
            ) {
                Text(
                    text = "$displayBookName ${uiState.currentChapter}",
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
            key = { index -> uiState.verses[index].id }
        ) { index ->
            val verse = uiState.verses[index]
            val hasCommentary = true // La etiqueta COM debe mantenerse siempre visible en todos los versículos
            val hasCrossReference = verse.verse in uiState.versesWithCrossReferences

            Box(
                modifier = if (index == 0) Modifier.tutorialTarget(TutorialStep.BIBLE_VERSES_INTERACTION) else Modifier
            ) {
                VerseRowItem(
                    verse = verse,
                    hasCommentary = hasCommentary,
                    hasCrossReference = hasCrossReference,
                    isHighlighted = uiState.highlightedVerse == verse.verse,
                    isSelected = verse.verse in selectedVerses,
                    isSelectionMode = isSelectionMode,
                    fontFamily = fontFamily,
                    fontSize = fontSize,
                    lineHeight = lineHeight,
                    onCommentaryClick = { code -> viewModel.openCommentary(verse, code) },
                    onCrossRefClick = { viewModel.openCrossReferences(verse) },
                    onWordClick = { word -> viewModel.lookupWord(word, verse) },
                    onNoteClick = { marker -> viewModel.onVersionNoteClick(verse, marker) },
                    onVerseClick = { onVerseClick(verse.verse) },
                    onVerseLongClick = { onVerseLongClick(verse.verse) }
                )
            }
        }

        item {
            val totalChapters = BibleBooksData.getBookByName(uiState.currentBook)?.chapterCount ?: 1
            val isVersionEn = BibleBooksData.isEnglishVersion(uiState.activeVersion)
            ChapterBottomNavigationControls(
                currentChapter = uiState.currentChapter,
                totalChapters = totalChapters,
                isEnglish = isVersionEn,
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
    selectedVerses: Set<Int>,
    selectedIsRightPane: Boolean,
    onLeftVerseClick: (Int) -> Unit,
    onLeftVerseLongClick: (Int) -> Unit,
    onRightVerseClick: (Int) -> Unit,
    onRightVerseLongClick: (Int) -> Unit,
    fontFamily: FontFamily,
    fontSize: TextUnit,
    lineHeight: TextUnit,
    appColors: AppColors,
    modifier: Modifier = Modifier
) {
    val downloadedVersions = uiState.modules.filter { it.category == "VERSION" && it.isDownloaded }
    val isLinked = uiState.isParallelLinked
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val isSelectionMode = selectedVerses.isNotEmpty()

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
                    val isLeftVersionEn = BibleBooksData.isEnglishVersion(uiState.activeVersion)
                    val leftBookInfo = BibleBooksData.getBookByName(uiState.currentBook)
                    val leftBookId = leftBookInfo?.number ?: 43
                    val displayBookName = BibleBooksData.getLocalizedBookName(leftBookId, isLeftVersionEn)
                    Text(
                        text = "$displayBookName ${uiState.currentChapter}",
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
                        onDismissRequest = { isLeftVersionMenuOpen = false },
                        shape = RoundedCornerShape(16.dp),
                        containerColor = appColors.surface,
                        tonalElevation = 0.dp,
                        shadowElevation = 8.dp,
                        border = BorderStroke(1.dp, appColors.border.copy(alpha = if (appColors.isDark) 0.85f else 0.75f))
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
                    bottom = 96.dp + navBarBottom
                )
            ) {
                items(
                    count = uiState.verses.size,
                    key = { index -> "L_${uiState.verses[index].id}" }
                ) { index ->
                    val verse = uiState.verses[index]
                    val hasCommentary = true
                    val hasCrossReference = verse.verse in uiState.versesWithCrossReferences

                    VerseRowItem(
                        verse = verse,
                        hasCommentary = hasCommentary,
                        hasCrossReference = hasCrossReference,
                        isHighlighted = uiState.highlightedVerse == verse.verse,
                        isSelected = !selectedIsRightPane && verse.verse in selectedVerses,
                        isSelectionMode = isSelectionMode,
                        fontFamily = fontFamily,
                        fontSize = fontSize,
                        lineHeight = lineHeight,
                        onCommentaryClick = { code -> viewModel.openCommentary(verse, code) },
                        onCrossRefClick = { viewModel.openCrossReferences(verse) },
                        onWordClick = { word -> viewModel.lookupWord(word, verse) },
                        onNoteClick = { marker -> viewModel.onVersionNoteClick(verse, marker) },
                        onVerseClick = { onLeftVerseClick(verse.verse) },
                        onVerseLongClick = { onLeftVerseLongClick(verse.verse) }
                    )
                }

                item {
                    val totalChapters = BibleBooksData.getBookByName(uiState.currentBook)?.chapterCount ?: 1
                    val isVersionEn = BibleBooksData.isEnglishVersion(uiState.activeVersion)
                    ChapterBottomNavigationControls(
                        currentChapter = uiState.currentChapter,
                        totalChapters = totalChapters,
                        isEnglish = isVersionEn,
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
            val isRightEn = BibleBooksData.isEnglishVersion(uiState.parallelVersion)
            val rightBookInfoResolved = BibleBooksData.getBookByName(displayRightTitle)
            val rightBookId = rightBookInfoResolved?.number ?: 43
            val localizedRightBook = BibleBooksData.getLocalizedBookName(rightBookId, isRightEn)

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
                        text = "$localizedRightBook $displayRightChapter",
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
                        onDismissRequest = { isRightVersionMenuOpen = false },
                        shape = RoundedCornerShape(16.dp),
                        containerColor = appColors.surface,
                        tonalElevation = 0.dp,
                        shadowElevation = 8.dp,
                        border = BorderStroke(1.dp, appColors.border.copy(alpha = if (appColors.isDark) 0.85f else 0.75f))
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
                    bottom = 96.dp + navBarBottom
                )
            ) {
                items(
                    count = uiState.parallelVerses.size,
                    key = { index -> "R_${uiState.parallelVerses[index].id}" }
                ) { index ->
                    val verse = uiState.parallelVerses[index]
                    val hasCommentary = true
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
                        isSelected = selectedIsRightPane && verse.verse in selectedVerses,
                        isSelectionMode = isSelectionMode,
                        fontFamily = fontFamily,
                        fontSize = fontSize,
                        lineHeight = lineHeight,
                        onCommentaryClick = { code -> viewModel.openCommentary(verse, code) },
                        onCrossRefClick = { viewModel.openCrossReferences(verse) },
                        onWordClick = { word -> viewModel.lookupWord(word, verse) },
                        onNoteClick = { marker -> viewModel.onVersionNoteClick(verse, marker) },
                        onVerseClick = { onRightVerseClick(verse.verse) },
                        onVerseLongClick = { onRightVerseLongClick(verse.verse) }
                    )
                }

                item {
                    val rightBook = if (isLinked) uiState.currentBook else (rightBookInfo?.name ?: rightBookTitle)
                    val activeRightChapter = if (isLinked) uiState.currentChapter else rightChapter
                    val totalChapters = BibleBooksData.getBookByName(rightBook)?.chapterCount ?: 1
                    val isRightVersionEn = BibleBooksData.isEnglishVersion(uiState.parallelVersion)
                    ChapterBottomNavigationControls(
                        currentChapter = activeRightChapter,
                        totalChapters = totalChapters,
                        isEnglish = isRightVersionEn,
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
                }
            }
        }
    }

    if (isRightPickerOpen && !isLinked) {
        PassageSelectorDialog(
            currentStep = rightPickerStep,
            selectedBook = rightBookInfo ?: BibleBooksData.books.first(),
            selectedChapter = rightPickerChapter,
            interfaceLanguage = uiState.userPreferences.interfaceLanguage,
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
    isEnglish: Boolean = false,
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
                        text = if (isEnglish) "Chapter ${currentChapter - 1}" else "Capítulo ${currentChapter - 1}",
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
                        text = if (isEnglish) "Chapter ${currentChapter + 1}" else "Capítulo ${currentChapter + 1}",
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
    isSelected: Boolean,
    isSelectionMode: Boolean,
    fontFamily: FontFamily,
    fontSize: TextUnit,
    lineHeight: TextUnit,
    onCommentaryClick: (code: String) -> Unit,
    onCrossRefClick: () -> Unit,
    onWordClick: (String) -> Unit,
    onNoteClick: (marker: String) -> Unit,
    onVerseClick: () -> Unit,
    onVerseLongClick: () -> Unit
) {
    val appColors = AppTheme.colors
    val selectionBgColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)

    val rawCleanText = remember(verse.text) {
        verse.text
            .replace(Regex("<S>.*?</S>|<.*?>"), "")
            .replace(Regex("(?<=\\p{L})\\d+"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    val currentIsSelectionMode by rememberUpdatedState(isSelectionMode)
    val currentOnVerseClick by rememberUpdatedState(onVerseClick)
    val currentOnVerseLongClick by rememberUpdatedState(onVerseLongClick)
    val currentOnCommentaryClick by rememberUpdatedState(onCommentaryClick)
    val currentOnCrossRefClick by rememberUpdatedState(onCrossRefClick)
    val currentOnWordClick by rememberUpdatedState(onWordClick)
    val currentOnNoteClick by rememberUpdatedState(onNoteClick)

    val annotatedString = remember(
        verse.verse,
        rawCleanText,
        appColors.verseNumberColor,
        appColors.textPrimary,
        appColors.accent,
        fontSize,
        hasCommentary,
        hasCrossReference
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
                color = appColors.accent.copy(alpha = 0.85f),
                fontSize = (fontSize.value * 0.65f).sp,
                baselineShift = BaselineShift.Superscript,
                fontWeight = FontWeight.Medium
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

            // Bloque compacto e indivisible de etiquetas COM y REF en línea al cierre del versículo
            if (hasCommentary || hasCrossReference) {
                withStyle(SpanStyle(fontSize = (fontSize.value * 0.45f).sp)) {
                    append(" ")
                }
                when {
                    hasCommentary && hasCrossReference -> {
                        appendInlineContent(id = "study_badges_both", alternateText = "[COM][REF]")
                    }
                    hasCommentary -> {
                        pushStringAnnotation(tag = "COM", annotation = "COM")
                        appendInlineContent(id = "study_badge_com", alternateText = "[COM]")
                        pop()
                    }
                    hasCrossReference -> {
                        pushStringAnnotation(tag = "REF", annotation = "REF")
                        appendInlineContent(id = "study_badge_ref", alternateText = "[REF]")
                        pop()
                    }
                }
            }
        }
    }

    val inlineContent = remember(appColors, fontFamily, verse.verse) {
        mapOf(
            "study_badges_both" to InlineTextContent(
                Placeholder(
                    width = 58.sp,
                    height = 14.sp,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StudyBadgePill(
                        label = "COM",
                        isCommentary = true,
                        appColors = appColors,
                        fontFamily = fontFamily,
                        onClick = {
                            if (currentIsSelectionMode) currentOnVerseClick()
                            else currentOnCommentaryClick("COM")
                        },
                        onLongClick = { currentOnVerseLongClick() },
                        modifier = Modifier.testTag("badge_com_${verse.verse}")
                    )
                    StudyBadgePill(
                        label = "REF",
                        isCommentary = false,
                        appColors = appColors,
                        fontFamily = fontFamily,
                        onClick = {
                            if (currentIsSelectionMode) currentOnVerseClick()
                            else currentOnCrossRefClick()
                        },
                        onLongClick = { currentOnVerseLongClick() },
                        modifier = Modifier.testTag("badge_ref_${verse.verse}")
                    )
                }
            },
            "study_badge_com" to InlineTextContent(
                Placeholder(
                    width = 29.sp,
                    height = 14.sp,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                )
            ) {
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier.fillMaxSize()
                ) {
                    StudyBadgePill(
                        label = "COM",
                        isCommentary = true,
                        appColors = appColors,
                        fontFamily = fontFamily,
                        onClick = {
                            if (currentIsSelectionMode) currentOnVerseClick()
                            else currentOnCommentaryClick("COM")
                        },
                        onLongClick = { currentOnVerseLongClick() },
                        modifier = Modifier.testTag("badge_com_${verse.verse}")
                    )
                }
            },
            "study_badge_ref" to InlineTextContent(
                Placeholder(
                    width = 28.sp,
                    height = 14.sp,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                )
            ) {
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier.fillMaxSize()
                ) {
                    StudyBadgePill(
                        label = "REF",
                        isCommentary = false,
                        appColors = appColors,
                        fontFamily = fontFamily,
                        onClick = {
                            if (currentIsSelectionMode) currentOnVerseClick()
                            else currentOnCrossRefClick()
                        },
                        onLongClick = { currentOnVerseLongClick() },
                        modifier = Modifier.testTag("badge_ref_${verse.verse}")
                    )
                }
            }
        )
    }

    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    isSelected -> selectionBgColor
                    isHighlighted -> appColors.surfaceVariant
                    else -> Color.Transparent
                }
            )
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    if (currentIsSelectionMode) {
                        currentOnVerseClick()
                    }
                },
                onLongClick = { currentOnVerseLongClick() }
            )
            .padding(vertical = 4.dp, horizontal = 4.dp)
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

        // Renderizado unificado y continuo en una sola pasada con píldoras atómicas y notas integradas
        Text(
            text = annotatedString,
            inlineContent = inlineContent,
            fontSize = fontSize,
            lineHeight = lineHeight,
            fontFamily = fontFamily,
            onTextLayout = { layoutResult = it },
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(annotatedString) {
                    val hitBoxExpandPx = 20.dp.toPx()
                    detectTapGestures(
                        onLongPress = { currentOnVerseLongClick() },
                        onTap = { pos ->
                            if (currentIsSelectionMode) {
                                currentOnVerseClick()
                                return@detectTapGestures
                            }

                            layoutResult?.let { layout ->
                                // 1. Hit-box ampliada (mínimo 40x40 dp) para notas de versión [n]
                                val noteAnnotations = annotatedString.getStringAnnotations("NOTE", 0, annotatedString.length)
                                var bestNote: String? = null
                                var minDistance = Float.MAX_VALUE

                                for (annotation in noteAnnotations) {
                                    if (annotation.start < layout.layoutInput.text.length && annotation.end <= layout.layoutInput.text.length) {
                                        val startBox = layout.getBoundingBox(annotation.start)
                                        val endBox = layout.getBoundingBox((annotation.end - 1).coerceAtLeast(annotation.start))
                                        val left = minOf(startBox.left, endBox.left) - hitBoxExpandPx
                                        val right = maxOf(startBox.right, endBox.right) + hitBoxExpandPx
                                        val top = minOf(startBox.top, endBox.top) - hitBoxExpandPx
                                        val bottom = maxOf(startBox.bottom, endBox.bottom) + hitBoxExpandPx

                                        if (pos.x in left..right && pos.y in top..bottom) {
                                            val centerX = (left + right) / 2f
                                            val centerY = (top + bottom) / 2f
                                            val dist = (pos.x - centerX) * (pos.x - centerX) + (pos.y - centerY) * (pos.y - centerY)
                                            if (dist < minDistance) {
                                                minDistance = dist
                                                bestNote = annotation.item
                                            }
                                        }
                                    }
                                }

                                if (bestNote != null) {
                                    currentOnNoteClick(bestNote)
                                    return@detectTapGestures
                                }

                                val offset = layout.getOffsetForPosition(pos)

                                // 2. Fallback para etiquetas individuales si el toque cae en el margen del Placeholder
                                val comAnnotation = annotatedString.getStringAnnotations("COM", offset, offset).firstOrNull()
                                if (comAnnotation != null) {
                                    currentOnCommentaryClick("COM")
                                    return@detectTapGestures
                                }
                                val refAnnotation = annotatedString.getStringAnnotations("REF", offset, offset).firstOrNull()
                                if (refAnnotation != null) {
                                    currentOnCrossRefClick()
                                    return@detectTapGestures
                                }

                                // 3. Búsqueda de palabra en diccionario
                                val wordAnnotation = annotatedString.getStringAnnotations("WORD", offset, offset).firstOrNull()
                                if (wordAnnotation != null && wordAnnotation.item.isNotBlank()) {
                                    currentOnWordClick(wordAnnotation.item)
                                    return@detectTapGestures
                                }
                            }
                        }
                    )
                }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun StudyBadgePill(
    label: String,
    isCommentary: Boolean,
    appColors: AppColors,
    fontFamily: FontFamily,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val containerColor = if (isCommentary) appColors.comBadgeBg else appColors.refBadgeBg
    val borderColor = if (isCommentary) appColors.comBadgeBorder else appColors.refBadgeBorder
    val textColor = if (isCommentary) appColors.comBadgeText else appColors.refBadgeText

    Surface(
        shape = CircleShape,
        color = containerColor,
        border = BorderStroke(0.6.dp, borderColor),
        modifier = modifier
            .clip(CircleShape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 4.5.dp, vertical = 1.dp)
        ) {
            Text(
                text = label,
                color = textColor,
                fontSize = 8.sp,
                lineHeight = 9.5.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.2.sp,
                fontFamily = fontFamily,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
private fun ContextualSelectionPill(
    allSelectedAreFavorites: Boolean,
    interfaceLanguage: String,
    onToggleFavorites: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val favLabel = StringsLocale.get("favorite_action", interfaceLanguage)
    val copyLabel = StringsLocale.get("copy_action", interfaceLanguage)
    val shareLabel = StringsLocale.get("share_action", interfaceLanguage)

    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 8.dp,
        tonalElevation = 0.dp,
        modifier = modifier.testTag("contextual_selection_pill")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SelectionPillActionButton(
                icon = if (allSelectedAreFavorites) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                label = favLabel,
                iconTint = if (allSelectedAreFavorites) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                labelColor = if (allSelectedAreFavorites) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                onClick = onToggleFavorites,
                modifier = Modifier.testTag("pill_action_favorite")
            )

            SelectionPillActionButton(
                icon = Icons.Default.ContentCopy,
                label = copyLabel,
                iconTint = MaterialTheme.colorScheme.onSurface,
                labelColor = MaterialTheme.colorScheme.onSurface,
                onClick = onCopy,
                modifier = Modifier.testTag("pill_action_copy")
            )

            SelectionPillActionButton(
                icon = Icons.Default.Share,
                label = shareLabel,
                iconTint = MaterialTheme.colorScheme.onSurface,
                labelColor = MaterialTheme.colorScheme.onSurface,
                onClick = onShare,
                modifier = Modifier.testTag("pill_action_share")
            )

            VerticalDivider(
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .height(28.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("pill_action_close")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = StringsLocale.get("cancel", interfaceLanguage),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun SelectionPillActionButton(
    icon: ImageVector,
    label: String,
    iconTint: Color,
    labelColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            lineHeight = 13.sp,
            fontWeight = FontWeight.Medium,
            color = labelColor,
            maxLines = 1,
            softWrap = false
        )
    }
}

private fun formatVerseRanges(sortedVerses: List<Int>): String {
    if (sortedVerses.isEmpty()) return ""
    val ranges = mutableListOf<String>()
    var start = sortedVerses.first()
    var end = start
    for (i in 1 until sortedVerses.size) {
        val current = sortedVerses[i]
        if (current == end + 1) {
            end = current
        } else {
            ranges.add(if (start == end) "$start" else "$start-$end")
            start = current
            end = current
        }
    }
    ranges.add(if (start == end) "$start" else "$start-$end")
    return ranges.joinToString(", ")
}

private fun buildFormattedPassageText(
    verses: List<BibleVerse>,
    fallbackBookName: String,
    fallbackChapter: Int,
    versionTag: String
): String {
    if (verses.isEmpty()) return ""
    val sorted = verses.sortedBy { it.verse }
    val first = sorted.first()
    val rawBook = first.bookName.ifBlank { fallbackBookName }
    val chapterNum = if (first.chapter > 0) first.chapter else fallbackChapter
    val effectiveVersion = BibleBooksData.normalizeVersionId(
        first.version.ifBlank { versionTag }
    ).ifBlank { versionTag }
    val isEnglish = BibleBooksData.isEnglishVersion(effectiveVersion)
    val localizedBook = BibleBooksData.getLocalizedBookName(
        bookName = rawBook,
        isEnglish = isEnglish,
        bookNumber = first.bookNumber
    )
    val rangeLabel = formatVerseRanges(sorted.map { it.verse }.distinct())
    val header = "$localizedBook $chapterNum:$rangeLabel ($effectiveVersion)"

    return if (sorted.size == 1) {
        val cleanText = BibleBooksData.cleanVerseTextForFavorites(first.text)
        "$header\n$cleanText"
    } else {
        val body = sorted.joinToString("\n") { verse ->
            val cleanText = BibleBooksData.cleanVerseTextForFavorites(verse.text)
            "${verse.verse} $cleanText"
        }
        "$header\n$body"
    }
}
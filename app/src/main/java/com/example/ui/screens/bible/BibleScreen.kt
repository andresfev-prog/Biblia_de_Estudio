package com.example.ui.screens.bible

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.interaction.DragInteraction
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
import androidx.compose.material.icons.automirrored.outlined.MenuBook
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.runtime.withFrameNanos
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BibleBooksData
import com.example.data.model.BibleModule
import com.example.data.model.BibleVerse
import com.example.data.model.BookInfo
import com.example.data.model.ModuleBibleCatalog
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
import com.example.ui.theme.AppThemeIds
import com.example.ui.util.BibleStudyTextUtils
import com.example.ui.util.StringsLocale
import com.example.ui.util.VerseDisplayToken
import com.example.ui.viewmodel.BibleUiState
import com.example.ui.viewmodel.BibleViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private val ReaderCrispTitleNavy = Color(0xFF0D2240)
private val ReaderCrispMutedText = Color(0xFF566376)

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
    var rightIndependentVerse by remember { mutableIntStateOf(1) }
    var rightTempHighlightedVerse by remember { mutableStateOf<Int?>(null) }
    var isProgrammaticScroll by remember { mutableStateOf(false) }
    var lastHandledMainRestoreToken by remember { mutableStateOf(0L) }
    var lastHandledParallelRestoreToken by remember { mutableStateOf(0L) }

    fun captureMainVisibleVerse(): Int {
        if (uiState.scrollRestoreVerse != null &&
            uiState.scrollRestoreToken != 0L &&
            uiState.scrollRestoreToken != lastHandledMainRestoreToken
        ) {
            return uiState.scrollRestoreVerse.coerceAtLeast(1)
        }
        val verses = uiState.verses
        if (!uiState.isChapterLoading && verses.isNotEmpty()) {
            val rawIdx = leftListState.firstVisibleItemIndex
            val verseIdx = if (uiState.isParallelMode) {
                rawIdx.coerceIn(0, verses.lastIndex)
            } else {
                if (rawIdx <= 0) 0 else (rawIdx - 1).coerceIn(0, verses.lastIndex)
            }
            return verses[verseIdx].verse
        }
        if (uiState.isParallelMode && uiState.isParallelLinked &&
            !uiState.isParallelChapterLoading && uiState.parallelVerses.isNotEmpty()
        ) {
            val rightIdx = rightListState.firstVisibleItemIndex.coerceIn(0, uiState.parallelVerses.lastIndex)
            return uiState.parallelVerses[rightIdx].verse
        }
        return uiState.currentVerse.coerceAtLeast(1)
    }

    fun captureRightVisibleVerse(fallbackVerse: Int = 1): Int {
        if (uiState.parallelScrollRestoreVerse != null &&
            uiState.parallelScrollRestoreToken != 0L &&
            uiState.parallelScrollRestoreToken != lastHandledParallelRestoreToken
        ) {
            return uiState.parallelScrollRestoreVerse.coerceAtLeast(1)
        }
        val pVerses = uiState.parallelVerses
        if (!uiState.isParallelChapterLoading && pVerses.isNotEmpty()) {
            val rightIdx = rightListState.firstVisibleItemIndex.coerceIn(0, pVerses.lastIndex)
            return pVerses[rightIdx].verse
        }
        return fallbackVerse.coerceAtLeast(1)
    }

    LaunchedEffect(uiState.currentBook, uiState.currentChapter, uiState.isParallelLinked) {
        if (uiState.isParallelLinked) {
            rightBookTitle = uiState.currentBook
            rightChapter = uiState.currentChapter
            rightTempHighlightedVerse = null
        }
    }

    LaunchedEffect(rightTempHighlightedVerse) {
        if (rightTempHighlightedVerse != null) {
            delay(BibleStudyTextUtils.TEMP_HIGHLIGHT_DURATION_MS)
            rightTempHighlightedVerse = null
        }
    }

    LaunchedEffect(
        uiState.currentBook,
        uiState.currentChapter,
        uiState.highlightedVerse,
        uiState.highlightToken,
        uiState.verses,
        uiState.parallelVerses
    ) {
        val targetVerse = uiState.highlightedVerse ?: return@LaunchedEffect

        val leftHasChapter = uiState.verses.isNotEmpty() &&
                uiState.verses.first().chapter == uiState.currentChapter
        isProgrammaticScroll = true
        try {
            if (leftHasChapter) {
                val targetIdx = BibleStudyTextUtils.resolveClosestVerseIndex(uiState.verses, targetVerse)
                if (targetIdx >= 0) {
                    // En SingleReaderView el encabezado del capítulo ocupa el índice 0
                    val scrollIdx = if (uiState.isParallelMode || (targetVerse <= 1 && targetIdx == 0)) {
                        targetIdx
                    } else {
                        targetIdx + 1
                    }
                    leftListState.scrollToItem(scrollIdx)
                }
            }

            if (uiState.isParallelMode && uiState.isParallelLinked) {
                val rightHasChapter = uiState.parallelVerses.isNotEmpty() &&
                        uiState.parallelVerses.first().chapter == uiState.currentChapter
                if (rightHasChapter) {
                    val targetIdx = BibleStudyTextUtils.resolveClosestVerseIndex(uiState.parallelVerses, targetVerse)
                    if (targetIdx >= 0) {
                        rightListState.scrollToItem(targetIdx)
                    }
                }
            }
        } finally {
            isProgrammaticScroll = false
        }
    }

    // Restauración automática del versículo activo tras cambio de versión o reapertura (pantalla completa y dividida)
    LaunchedEffect(
        uiState.scrollRestoreToken,
        uiState.verses,
        uiState.isChapterLoading,
        uiState.isParallelMode
    ) {
        val targetVerse = uiState.scrollRestoreVerse ?: return@LaunchedEffect
        val token = uiState.scrollRestoreToken
        if (token == 0L || token == lastHandledMainRestoreToken) return@LaunchedEffect
        if (uiState.isChapterLoading || uiState.verses.isEmpty()) return@LaunchedEffect

        val verseIdx = BibleStudyTextUtils.resolveClosestVerseIndex(uiState.verses, targetVerse)
        if (verseIdx < 0) return@LaunchedEffect

        val scrollIdx = if (uiState.isParallelMode) {
            verseIdx
        } else {
            if (targetVerse <= 1 && verseIdx == 0) 0 else verseIdx + 1
        }

        isProgrammaticScroll = true
        try {
            leftListState.scrollToItem(scrollIdx)
            if (uiState.isParallelMode && uiState.isParallelLinked &&
                !uiState.isParallelChapterLoading && uiState.parallelVerses.isNotEmpty()
            ) {
                val rightIdx = BibleStudyTextUtils.resolveClosestVerseIndex(uiState.parallelVerses, targetVerse)
                if (rightIdx >= 0) {
                    rightListState.scrollToItem(rightIdx)
                }
            }
            lastHandledMainRestoreToken = token
        } finally {
            isProgrammaticScroll = false
        }
    }

    // Restauración automática del panel secundario (derecho) en vista paralela tras cambiar su traducción
    LaunchedEffect(
        uiState.parallelScrollRestoreToken,
        uiState.parallelVerses,
        uiState.isParallelChapterLoading,
        uiState.isParallelMode
    ) {
        if (!uiState.isParallelMode) return@LaunchedEffect
        val targetVerse = uiState.parallelScrollRestoreVerse ?: return@LaunchedEffect
        val token = uiState.parallelScrollRestoreToken
        if (token == 0L || token == lastHandledParallelRestoreToken) return@LaunchedEffect
        if (uiState.isParallelChapterLoading || uiState.parallelVerses.isEmpty()) return@LaunchedEffect

        val rightIdx = BibleStudyTextUtils.resolveClosestVerseIndex(uiState.parallelVerses, targetVerse)
        if (rightIdx < 0) return@LaunchedEffect

        isProgrammaticScroll = true
        try {
            rightListState.scrollToItem(rightIdx)
            lastHandledParallelRestoreToken = token
        } finally {
            isProgrammaticScroll = false
        }
    }

    // Cancelar de inmediato el resaltado temporal en cuanto el usuario haga scroll/arrastre
    LaunchedEffect(leftListState, uiState.isNavigationHighlightActive) {
        if (!uiState.isNavigationHighlightActive) return@LaunchedEffect
        leftListState.interactionSource.interactions.collect { interaction ->
            if (interaction is DragInteraction.Start) {
                viewModel.clearNavigationHighlight()
            }
        }
    }

    LaunchedEffect(rightListState, uiState.isNavigationHighlightActive, rightTempHighlightedVerse) {
        if (!uiState.isNavigationHighlightActive && rightTempHighlightedVerse == null) return@LaunchedEffect
        rightListState.interactionSource.interactions.collect { interaction ->
            if (interaction is DragInteraction.Start) {
                if (uiState.isNavigationHighlightActive) viewModel.clearNavigationHighlight()
                rightTempHighlightedVerse = null
            }
        }
    }

    val latestIsNavigationHighlightActive by rememberUpdatedState(uiState.isNavigationHighlightActive)
    val latestRightTempHighlight by rememberUpdatedState(rightTempHighlightedVerse)
    val latestVerses by rememberUpdatedState(uiState.verses)
    val latestParallelVerses by rememberUpdatedState(uiState.parallelVerses)
    val latestVersesSize by rememberUpdatedState(uiState.verses.size)
    val latestParallelVersesSize by rememberUpdatedState(uiState.parallelVerses.size)
    val latestIsParallelMode by rememberUpdatedState(uiState.isParallelMode)
    val latestIsParallelLinked by rememberUpdatedState(uiState.isParallelLinked)
    val latestIsChapterLoading by rememberUpdatedState(uiState.isChapterLoading)
    val latestIsParallelChapterLoading by rememberUpdatedState(uiState.isParallelChapterLoading)
    val latestHasStartedReading by rememberUpdatedState(uiState.hasStartedReading)
    val latestScrollRestoreVerse by rememberUpdatedState(uiState.scrollRestoreVerse)
    val latestScrollRestoreToken by rememberUpdatedState(uiState.scrollRestoreToken)
    val latestParallelScrollRestoreVerse by rememberUpdatedState(uiState.parallelScrollRestoreVerse)
    val latestParallelScrollRestoreToken by rememberUpdatedState(uiState.parallelScrollRestoreToken)

    // Observar scroll vía snapshotFlow para evitar recomponer todo BibleScreen en cada inicio/fin de desplazamiento
    LaunchedEffect(leftListState, rightListState) {
        snapshotFlow { leftListState.isScrollInProgress to rightListState.isScrollInProgress }
            .collect { (leftScrolling, rightScrolling) ->
                if (!isProgrammaticScroll && (leftScrolling || rightScrolling)) {
                    if (latestIsNavigationHighlightActive) {
                        viewModel.clearNavigationHighlight()
                    }
                    if (latestRightTempHighlight != null && rightScrolling) {
                        rightTempHighlightedVerse = null
                    }
                }
            }
    }

    // Rastrear el versículo activo visible al asentarse el desplazamiento (pantalla única y ambos paneles de vista dividida)
    LaunchedEffect(leftListState, rightListState) {
        var lastObservedLeftIndex = -1
        var lastObservedRightIndex = -1
        snapshotFlow {
            listOf(
                if (leftListState.isScrollInProgress) 1 else 0,
                leftListState.firstVisibleItemIndex,
                if (rightListState.isScrollInProgress) 1 else 0,
                rightListState.firstVisibleItemIndex
            )
        }.collect { stateTuple ->
            val leftScrolling = stateTuple[0] == 1
            val leftFirstIdx = stateTuple[1]
            val rightScrolling = stateTuple[2] == 1
            val rightFirstIdx = stateTuple[3]

            if (isProgrammaticScroll || leftScrolling || rightScrolling) return@collect

            val hasPendingMainRestore = latestScrollRestoreVerse != null &&
                latestScrollRestoreToken != 0L &&
                latestScrollRestoreToken != lastHandledMainRestoreToken
            val hasPendingParallelRestore = latestParallelScrollRestoreVerse != null &&
                latestParallelScrollRestoreToken != 0L &&
                latestParallelScrollRestoreToken != lastHandledParallelRestoreToken
            if (hasPendingMainRestore || hasPendingParallelRestore || latestIsNavigationHighlightActive) {
                return@collect
            }

            val verses = latestVerses
            val pVerses = latestParallelVerses

            if (latestIsParallelMode && !latestIsParallelLinked &&
                !latestIsParallelChapterLoading && pVerses.isNotEmpty() &&
                rightFirstIdx != lastObservedRightIndex
            ) {
                lastObservedRightIndex = rightFirstIdx
                val rIdx = rightFirstIdx.coerceIn(0, pVerses.lastIndex)
                rightIndependentVerse = pVerses[rIdx].verse
            }

            if (latestIsParallelMode && latestIsParallelLinked &&
                !latestIsParallelChapterLoading && pVerses.isNotEmpty() &&
                rightFirstIdx != lastObservedRightIndex && leftFirstIdx == lastObservedLeftIndex
            ) {
                lastObservedRightIndex = rightFirstIdx
                val rIdx = rightFirstIdx.coerceIn(0, pVerses.lastIndex)
                val rightVerse = pVerses[rIdx].verse
                if (!latestIsChapterLoading && verses.isNotEmpty()) {
                    val matchLeftIdx = BibleStudyTextUtils.resolveClosestVerseIndex(verses, rightVerse)
                    if (matchLeftIdx >= 0 && leftListState.firstVisibleItemIndex != matchLeftIdx) {
                        withFrameNanos { }
                        isProgrammaticScroll = true
                        try {
                            leftListState.scrollToItem(matchLeftIdx)
                            lastObservedLeftIndex = matchLeftIdx
                        } finally {
                            isProgrammaticScroll = false
                        }
                    }
                }
                if (latestHasStartedReading || rightVerse > 1 || rightFirstIdx > 0) {
                    viewModel.updateVisibleReadingVerse(rightVerse)
                }
                return@collect
            }

            if (!latestIsChapterLoading && verses.isNotEmpty() && leftFirstIdx != lastObservedLeftIndex) {
                lastObservedLeftIndex = leftFirstIdx
                lastObservedRightIndex = rightFirstIdx
                // En SingleReaderView el encabezado del capítulo ocupa el índice 0
                val verseIdx = if (latestIsParallelMode) {
                    leftFirstIdx.coerceIn(0, verses.lastIndex)
                } else {
                    if (leftFirstIdx <= 0) 0 else (leftFirstIdx - 1).coerceIn(0, verses.lastIndex)
                }
                val visibleVerse = verses[verseIdx].verse
                if (latestIsParallelMode && latestIsParallelLinked &&
                    !latestIsParallelChapterLoading && pVerses.isNotEmpty()
                ) {
                    val matchRightIdx = BibleStudyTextUtils.resolveClosestVerseIndex(pVerses, visibleVerse)
                    if (matchRightIdx >= 0 && rightListState.firstVisibleItemIndex != matchRightIdx) {
                        withFrameNanos { }
                        isProgrammaticScroll = true
                        try {
                            rightListState.scrollToItem(matchRightIdx)
                            lastObservedRightIndex = matchRightIdx
                        } finally {
                            isProgrammaticScroll = false
                        }
                    }
                }
                if (latestHasStartedReading || visibleVerse > 1 || leftFirstIdx > 0) {
                    viewModel.updateVisibleReadingVerse(visibleVerse)
                }
            }
        }
    }

    LaunchedEffect(leftListState, rightListState, uiState.isParallelMode, uiState.isParallelLinked) {
        if (!uiState.isParallelMode || !uiState.isParallelLinked) return@LaunchedEffect
        launch {
            snapshotFlow {
                if (leftListState.isScrollInProgress && !rightListState.isScrollInProgress) {
                    Pair(leftListState.firstVisibleItemIndex, leftListState.firstVisibleItemScrollOffset)
                } else {
                    null
                }
            }.collectLatest { pos ->
                if (pos == null) return@collectLatest
                withFrameNanos { }
                val verses = latestVerses
                val pVerses = latestParallelVerses
                if (verses.isNotEmpty() && pVerses.isNotEmpty()) {
                    val leftIdx = pos.first.coerceIn(0, verses.lastIndex)
                    val targetIdx = BibleStudyTextUtils.resolveClosestVerseIndex(pVerses, verses[leftIdx].verse)
                    if (targetIdx >= 0) {
                        rightListState.scrollToItem(targetIdx, pos.second)
                    }
                }
            }
        }
        launch {
            snapshotFlow {
                if (rightListState.isScrollInProgress && !leftListState.isScrollInProgress) {
                    Pair(rightListState.firstVisibleItemIndex, rightListState.firstVisibleItemScrollOffset)
                } else {
                    null
                }
            }.collectLatest { pos ->
                if (pos == null) return@collectLatest
                withFrameNanos { }
                val verses = latestVerses
                val pVerses = latestParallelVerses
                if (verses.isNotEmpty() && pVerses.isNotEmpty()) {
                    val rightIdx = pos.first.coerceIn(0, pVerses.lastIndex)
                    val targetIdx = BibleStudyTextUtils.resolveClosestVerseIndex(verses, pVerses[rightIdx].verse)
                    if (targetIdx >= 0) {
                        leftListState.scrollToItem(targetIdx, pos.second)
                    }
                }
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

    val downloadedVersions = remember(uiState.modules) {
        uiState.modules.filter { it.category == "VERSION" && it.isDownloaded }
    }

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
                        hasReadableVerses = uiState.verses.isNotEmpty(),
                        interfaceLanguage = lang,
                        onOpenPassagePicker = { viewModel.openPassagePicker() },
                        onSelectActiveVersion = { viewModel.setActiveVersion(it, captureMainVisibleVerse()) },
                        onToggleParallelMode = { viewModel.toggleParallelMode(captureMainVisibleVerse()) },
                        onToggleParallelLinked = { viewModel.toggleParallelLinked(captureMainVisibleVerse()) },
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
                    rightHighlightedVerse = rightTempHighlightedVerse,
                    onClearRightHighlight = { rightTempHighlightedVerse = null },
                    onSelectLeftVersion = { tag ->
                        viewModel.setActiveVersion(tag, captureMainVisibleVerse())
                    },
                    onSelectRightVersion = { tag ->
                        if (uiState.isParallelLinked) {
                            viewModel.setParallelVersion(tag, captureMainVisibleVerse())
                        } else {
                            val preservedRightVerse = captureRightVisibleVerse(rightIndependentVerse)
                            rightIndependentVerse = preservedRightVerse
                            viewModel.loadParallelChapter(
                                bookName = rightBookTitle,
                                chapter = rightChapter,
                                version = tag,
                                targetVerse = preservedRightVerse,
                                restoreScrollPosition = true
                            )
                        }
                    },
                    onUpdateRightPassage = { newBook, newChapter, newVerse ->
                        rightBookTitle = newBook
                        rightChapter = newChapter
                        rightIndependentVerse = newVerse
                        rightTempHighlightedVerse = newVerse
                        viewModel.loadParallelChapter(
                            bookName = newBook,
                            chapter = newChapter,
                            version = uiState.parallelVersion,
                            targetVerse = newVerse,
                            restoreScrollPosition = true
                        )
                        coroutineScope.launch {
                            isProgrammaticScroll = true
                            try {
                                val targetIndex = (newVerse - 1).coerceAtLeast(0)
                                rightListState.animateScrollToItem(targetIndex)
                            } finally {
                                isProgrammaticScroll = false
                            }
                        }
                    },
                    selectedVerses = selectedVerses,
                    selectedIsRightPane = selectedIsRightPane,
                    onLeftVerseClick = { verseNum ->
                        if (uiState.isNavigationHighlightActive) viewModel.clearNavigationHighlight()
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
                        if (uiState.isNavigationHighlightActive) viewModel.clearNavigationHighlight()
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (!selectedIsRightPane) {
                            selectedVerses = selectedVerses + verseNum
                        } else {
                            selectedIsRightPane = false
                            selectedVerses = setOf(verseNum)
                        }
                    },
                    onRightVerseClick = { verseNum ->
                        if (uiState.isNavigationHighlightActive) viewModel.clearNavigationHighlight()
                        rightTempHighlightedVerse = null
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
                        if (uiState.isNavigationHighlightActive) viewModel.clearNavigationHighlight()
                        rightTempHighlightedVerse = null
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
                    availableVersions = downloadedVersions,
                    selectedVerses = if (selectedIsRightPane) emptySet() else selectedVerses,
                    onOpenPassagePicker = { viewModel.openPassagePicker() },
                    onSelectVersion = { viewModel.setActiveVersion(it, captureMainVisibleVerse()) },
                    onVerseClick = { verseNum ->
                        if (uiState.isNavigationHighlightActive) viewModel.clearNavigationHighlight()
                        selectedIsRightPane = false
                        selectedVerses = if (verseNum in selectedVerses) {
                            selectedVerses - verseNum
                        } else {
                            selectedVerses + verseNum
                        }
                    },
                    onVerseLongClick = { verseNum ->
                        if (uiState.isNavigationHighlightActive) viewModel.clearNavigationHighlight()
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
            isVisible = uiState.isTtsVisible && uiState.verses.isNotEmpty(),
            isPlaying = uiState.isTtsPlaying,
            bookName = uiState.currentBook,
            chapter = uiState.currentChapter,
            currentVerse = if (uiState.verses.isNotEmpty()) uiState.verses.getOrNull(uiState.ttsVerseIndex)?.verse ?: 1 else 1,
            totalVerses = uiState.verses.size,
            interfaceLanguage = lang,
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

        LaunchedEffect(selectedVerseObjects) {
            viewModel.setSelectedVerses(selectedVerseObjects)
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
                    val formattedPassage = BibleBooksData.buildFormattedPassageText(
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
                    val formattedPassage = BibleBooksData.buildFormattedPassageText(
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
                catalog = uiState.activeVersionCatalog,
                availableVerses = uiState.pickerAvailableVerses,
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

        uiState.missingNoteModulePrompt?.let { prompt ->
            val isEn = lang.contains("English", ignoreCase = true)
            AlertDialog(
                onDismissRequest = { viewModel.dismissMissingNoteModulePrompt() },
                containerColor = appColors.surface,
                titleContentColor = appColors.titleColor,
                textContentColor = appColors.textPrimary,
                shape = RoundedCornerShape(22.dp),
                title = {
                    Text(
                        text = if (isEn) "Reina-Valera 1960 Notes" else "Notas de Reina-Valera 1960",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = appColors.titleColor
                    )
                },
                text = {
                    Text(
                        text = if (isEn) {
                            "To read the notes marked with (*) in RVR1960 (${prompt.verseReference}), you need the module \"${prompt.moduleTitle} (${prompt.moduleTag})\"."
                        } else {
                            "Las notas marcadas con (*) en RVR1960 (${prompt.verseReference}) requieren el módulo «${prompt.moduleTitle} (${prompt.moduleTag})»."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = appColors.textSecondary,
                        lineHeight = 21.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.openAndDownloadMissingNoteModule() },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = appColors.pillBg,
                            contentColor = appColors.pillText
                        ),
                        modifier = Modifier.testTag("download_rvrc_module_button")
                    ) {
                        Text(
                            text = if (isEn) "Download RVR-C" else "Descargar RVR-C",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { viewModel.dismissMissingNoteModulePrompt() },
                        modifier = Modifier.testTag("dismiss_rvrc_dialog_button")
                    ) {
                        Text(
                            text = StringsLocale.get("cancel", lang),
                            color = appColors.textSecondary
                        )
                    }
                }
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
                selectedVerses = uiState.selectedVerses,
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
    hasReadableVerses: Boolean = true,
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

    val isCrispLight = !appColors.isDark && appColors.themeId == AppThemeIds.CRISP_LIGHT
    val headingColor = if (isCrispLight) ReaderCrispTitleNavy else appColors.titleColor
    val mutedTextColor = if (isCrispLight) ReaderCrispMutedText else appColors.textSecondary

    val isVersionEn = BibleBooksData.isEnglishVersion(activeVersion)
    val bookInfo = BibleBooksData.getBookByName(currentBook)
    val bookId = bookInfo?.number ?: 1
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
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$bookName $currentChapter",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.5.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = headingColor
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
                        tint = if (isParallelLinked) appColors.accent else mutedTextColor,
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
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = activeVersion,
                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                                fontWeight = FontWeight.SemiBold,
                                color = headingColor
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
                                text = {
                                    Text(
                                        "Reina-Valera 1960 (RVR1960)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = appColors.textPrimary
                                    )
                                },
                                onClick = {
                                    onSelectActiveVersion("RVR1960")
                                    versionMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "King James Version (KJV)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = appColors.textPrimary
                                    )
                                },
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
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (activeVersion == module.versionTag) appColors.accent else appColors.textPrimary,
                                            fontWeight = if (activeVersion == module.versionTag) FontWeight.SemiBold else FontWeight.Normal,
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
                        tint = headingColor,
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
                        enabled = hasReadableVerses,
                        leadingIcon = {
                            AppIcon(
                                glyph = AppIcons.TtsAudio,
                                contentDescription = null,
                                tint = if (hasReadableVerses) appColors.accent else mutedTextColor.copy(alpha = 0.45f),
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        text = {
                            Text(
                                text = StringsLocale.get("overflow_audio", interfaceLanguage),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (hasReadableVerses) appColors.textPrimary else mutedTextColor.copy(alpha = 0.45f),
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
                                tint = if (isParallelMode) appColors.accent else mutedTextColor,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        text = {
                            Text(
                                text = StringsLocale.get("overflow_split", interfaceLanguage),
                                style = MaterialTheme.typography.bodyMedium,
                                color = appColors.textPrimary,
                                fontWeight = if (isParallelMode) FontWeight.SemiBold else FontWeight.Medium
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
                                style = MaterialTheme.typography.bodyMedium,
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
    availableVersions: List<BibleModule>,
    selectedVerses: Set<Int>,
    onOpenPassagePicker: () -> Unit,
    onSelectVersion: (String) -> Unit,
    onVerseClick: (Int) -> Unit,
    onVerseLongClick: (Int) -> Unit,
    fontFamily: FontFamily,
    fontSize: TextUnit,
    lineHeight: TextUnit,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    val appColors = AppTheme.colors
    val isCrispLight = !appColors.isDark && appColors.themeId == AppThemeIds.CRISP_LIGHT
    val headingColor = if (isCrispLight) ReaderCrispTitleNavy else appColors.titleColor
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val isSelectionMode = selectedVerses.isNotEmpty()
    val isVersionEn = BibleBooksData.isEnglishVersion(uiState.activeVersion)

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
        item(key = "single_chapter_header", contentType = "chapter_header") {
            val bookInfo = BibleBooksData.getBookByName(uiState.currentBook)
            val bookId = bookInfo?.number ?: 1
            val displayBookName = BibleBooksData.getLocalizedBookName(bookId, isVersionEn)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 10.dp)
            ) {
                Text(
                    text = "$displayBookName ${uiState.currentChapter}",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 27.sp,
                        letterSpacing = (-0.4).sp
                    ),
                    fontWeight = FontWeight.Bold,
                    color = headingColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (uiState.isChapterLoading && uiState.verses.isEmpty()) {
            item(key = "single_loading", contentType = "loading") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(30.dp),
                        color = appColors.accent,
                        strokeWidth = 2.5.dp
                    )
                }
            }
        } else if (uiState.verses.isEmpty()) {
            item(key = "single_unavailable", contentType = "unavailable") {
                PassageUnavailableState(
                    versionCode = uiState.activeVersion,
                    interfaceLanguage = uiState.userPreferences.interfaceLanguage,
                    availableVersions = availableVersions,
                    onOpenAvailableBooks = onOpenPassagePicker,
                    onSelectVersion = onSelectVersion,
                    compact = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp, bottom = 32.dp)
                        .testTag("passage_unavailable_state")
                )
            }
        } else {
            items(
                count = uiState.verses.size,
                key = { index -> uiState.verses[index].id },
                contentType = { "verse" }
            ) { index ->
                val verse = uiState.verses[index]
                val hasCommentary = true // La etiqueta COM debe mantenerse siempre visible en todos los versículos
                val hasCrossReference = verse.verse in uiState.versesWithCrossReferences
                val hasRvrVersionNote = verse.verse in uiState.versesWithRvrNotes

                Box(
                    modifier = if (index == 0) Modifier.tutorialTarget(TutorialStep.BIBLE_VERSES_INTERACTION) else Modifier
                ) {
                    VerseRowItem(
                        verse = verse,
                        precomputedTokens = uiState.verseTokens[verse.id],
                        subheadings = uiState.chapterSubheadings[verse.verse].orEmpty(),
                        hasCommentary = hasCommentary,
                        hasCrossReference = hasCrossReference,
                        hasRvrVersionNote = hasRvrVersionNote,
                        isHighlighted = uiState.highlightedVerse == verse.verse,
                        isSelected = verse.verse in selectedVerses,
                        isSelectionMode = isSelectionMode,
                        fontFamily = fontFamily,
                        fontSize = fontSize,
                        lineHeight = lineHeight,
                        onCommentaryClick = { code ->
                            if (uiState.isNavigationHighlightActive) viewModel.clearNavigationHighlight()
                            viewModel.openCommentary(verse, code)
                        },
                        onCrossRefClick = {
                            if (uiState.isNavigationHighlightActive) viewModel.clearNavigationHighlight()
                            viewModel.openCrossReferences(verse)
                        },
                        onWordClick = { word ->
                            if (uiState.isNavigationHighlightActive) viewModel.clearNavigationHighlight()
                            viewModel.lookupWord(word, verse)
                        },
                        onNoteClick = { marker ->
                            if (uiState.isNavigationHighlightActive) viewModel.clearNavigationHighlight()
                            viewModel.onVersionNoteClick(verse, marker)
                        },
                        onAnyVerseTap = {
                            if (uiState.isNavigationHighlightActive && uiState.highlightedVerse != verse.verse) {
                                viewModel.clearNavigationHighlight()
                            }
                        },
                        onVerseClick = { onVerseClick(verse.verse) },
                        onVerseLongClick = { onVerseLongClick(verse.verse) }
                    )
                }
            }

            item(key = "single_chapter_nav", contentType = "chapter_nav") {
                val catalog = uiState.activeVersionCatalog
                val currentCanonicalId = BibleBooksData.resolveCanonicalBookId(uiState.currentBook)
                val prevPassage = catalog?.getPreviousPassage(uiState.currentBook, uiState.currentChapter)
                val nextPassage = catalog?.getNextPassage(uiState.currentBook, uiState.currentChapter)

                val prevLabel = prevPassage?.let { (book, chap) ->
                    if (book.number == currentCanonicalId) {
                        if (isVersionEn) "Chapter $chap" else "Capítulo $chap"
                    } else {
                        "${if (isVersionEn) book.englishName else book.name} $chap"
                    }
                }
                val nextLabel = nextPassage?.let { (book, chap) ->
                    if (book.number == currentCanonicalId) {
                        if (isVersionEn) "Chapter $chap" else "Capítulo $chap"
                    } else {
                        "${if (isVersionEn) book.englishName else book.name} $chap"
                    }
                }

                ChapterBottomNavigationControls(
                    previousLabel = prevLabel,
                    nextLabel = nextLabel,
                    onPreviousChapter = { viewModel.navigateToPreviousChapter() },
                    onNextChapter = { viewModel.navigateToNextChapter() }
                )
            }
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
    rightHighlightedVerse: Int?,
    onClearRightHighlight: () -> Unit,
    onSelectLeftVersion: (String) -> Unit,
    onSelectRightVersion: (String) -> Unit,
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
    val downloadedVersions = remember(uiState.modules) {
        uiState.modules.filter { it.category == "VERSION" && it.isDownloaded }
    }
    val isLinked = uiState.isParallelLinked
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val isSelectionMode = selectedVerses.isNotEmpty()
    val coroutineScope = rememberCoroutineScope()

    val isCrispLight = !appColors.isDark && appColors.themeId == AppThemeIds.CRISP_LIGHT
    val headingColor = if (isCrispLight) ReaderCrispTitleNavy else appColors.titleColor
    val mutedTextColor = if (isCrispLight) ReaderCrispMutedText else appColors.textSecondary

    var isLeftVersionMenuOpen by remember { mutableStateOf(false) }
    var isRightVersionMenuOpen by remember { mutableStateOf(false) }

    var isRightPickerOpen by remember { mutableStateOf(false) }
    var rightPickerStep by remember { mutableIntStateOf(1) }
    var rightPickerCatalog by remember { mutableStateOf<ModuleBibleCatalog?>(uiState.parallelVersionCatalog) }
    var rightPickerAvailableVerses by remember { mutableStateOf<List<Int>>(emptyList()) }
    var rightBookInfo by remember {
        mutableStateOf<BookInfo?>(
            BibleBooksData.getBookByName(rightBookTitle)
                ?: BibleBooksData.books.firstOrNull { it.name.equals(rightBookTitle, ignoreCase = true) }
                ?: BibleBooksData.books.first()
        )
    }
    var rightPickerChapter by remember { mutableIntStateOf(rightChapter) }

    LaunchedEffect(uiState.parallelVersionCatalog) {
        if (uiState.parallelVersionCatalog != null) {
            rightPickerCatalog = uiState.parallelVersionCatalog
        }
    }

    fun openRightIndependentPicker(currentRightTitle: String, currentRightChap: Int) {
        val currentCatalog = uiState.parallelVersionCatalog
        rightPickerCatalog = currentCatalog
        val initialBook = currentCatalog?.findAvailableBook(currentRightTitle)
            ?: currentCatalog?.availableBooks?.firstOrNull()
            ?: BibleBooksData.getBookByName(currentRightTitle)
            ?: BibleBooksData.books.first()
        val chapters = currentCatalog?.getAvailableChapters(initialBook).orEmpty()
        rightBookInfo = initialBook
        rightPickerChapter = if (currentRightChap in chapters) currentRightChap else (chapters.firstOrNull() ?: currentRightChap)
        rightPickerAvailableVerses = emptyList()
        rightPickerStep = 1
        isRightPickerOpen = true

        coroutineScope.launch {
            val loaded = viewModel.getModuleCatalogForVersion(uiState.parallelVersion)
            rightPickerCatalog = loaded
            val resolvedBook = loaded.findAvailableBook(currentRightTitle)
                ?: loaded.availableBooks.firstOrNull()
                ?: rightBookInfo
            if (resolvedBook != null) {
                rightBookInfo = resolvedBook
                val loadedChapters = loaded.getAvailableChapters(resolvedBook)
                if (rightPickerChapter !in loadedChapters) {
                    rightPickerChapter = loadedChapters.firstOrNull() ?: 1
                }
            }
        }
    }

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
                    val leftBookId = leftBookInfo?.number ?: 1
                    val displayBookName = BibleBooksData.getLocalizedBookName(leftBookId, isLeftVersionEn)
                    Text(
                        text = "$displayBookName ${uiState.currentChapter}",
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.5.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = headingColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
                            .testTag("parallel_left_version_selector")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = uiState.activeVersion,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                                maxLines = 1,
                                softWrap = false,
                                color = appColors.accent,
                                fontWeight = FontWeight.SemiBold
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
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (uiState.activeVersion == tag) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (uiState.activeVersion == tag) appColors.accent else appColors.textPrimary
                                        )
                                    },
                                    onClick = {
                                        isLeftVersionMenuOpen = false
                                        onSelectLeftVersion(tag)
                                    }
                                )
                            }
                        } else {
                            downloadedVersions.forEach { module ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "${module.title} (${module.versionTag})",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (uiState.activeVersion == module.versionTag) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (uiState.activeVersion == module.versionTag) appColors.accent else appColors.textPrimary,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    },
                                    onClick = {
                                        isLeftVersionMenuOpen = false
                                        onSelectLeftVersion(module.versionTag)
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
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("parallel_left_list"),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 10.dp,
                    top = 8.dp,
                    bottom = 96.dp + navBarBottom
                )
            ) {
                if (uiState.isChapterLoading && uiState.verses.isEmpty()) {
                    item(key = "left_loading", contentType = "loading") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = appColors.accent,
                                strokeWidth = 2.dp
                            )
                        }
                    }
                } else if (uiState.verses.isEmpty()) {
                    item(key = "left_unavailable", contentType = "unavailable") {
                        PassageUnavailableState(
                            versionCode = uiState.activeVersion,
                            interfaceLanguage = uiState.userPreferences.interfaceLanguage,
                            availableVersions = downloadedVersions,
                            onOpenAvailableBooks = { viewModel.openPassagePicker() },
                            onSelectVersion = { onSelectLeftVersion(it) },
                            compact = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp)
                                .testTag("left_passage_unavailable_state")
                        )
                    }
                } else {
                    items(
                        count = uiState.verses.size,
                        key = { index -> "L_${uiState.verses[index].id}" },
                        contentType = { "verse" }
                    ) { index ->
                        val verse = uiState.verses[index]
                        val hasCommentary = true
                        val hasCrossReference = verse.verse in uiState.versesWithCrossReferences
                        val hasRvrVersionNote = verse.verse in uiState.versesWithRvrNotes

                        VerseRowItem(
                            verse = verse,
                            precomputedTokens = uiState.verseTokens[verse.id],
                            subheadings = uiState.chapterSubheadings[verse.verse].orEmpty(),
                            hasCommentary = hasCommentary,
                            hasCrossReference = hasCrossReference,
                            hasRvrVersionNote = hasRvrVersionNote,
                            isHighlighted = uiState.highlightedVerse == verse.verse,
                            isSelected = !selectedIsRightPane && verse.verse in selectedVerses,
                            isSelectionMode = isSelectionMode,
                            fontFamily = fontFamily,
                            fontSize = fontSize,
                            lineHeight = lineHeight,
                            onCommentaryClick = { code ->
                                if (uiState.isNavigationHighlightActive) viewModel.clearNavigationHighlight()
                                viewModel.openCommentary(verse, code)
                            },
                            onCrossRefClick = {
                                if (uiState.isNavigationHighlightActive) viewModel.clearNavigationHighlight()
                                viewModel.openCrossReferences(verse)
                            },
                            onWordClick = { word ->
                                if (uiState.isNavigationHighlightActive) viewModel.clearNavigationHighlight()
                                viewModel.lookupWord(word, verse)
                            },
                            onNoteClick = { marker ->
                                if (uiState.isNavigationHighlightActive) viewModel.clearNavigationHighlight()
                                viewModel.onVersionNoteClick(verse, marker)
                            },
                            onAnyVerseTap = {
                                if (uiState.isNavigationHighlightActive && uiState.highlightedVerse != verse.verse) {
                                    viewModel.clearNavigationHighlight()
                                }
                            },
                            onVerseClick = { onLeftVerseClick(verse.verse) },
                            onVerseLongClick = { onLeftVerseLongClick(verse.verse) }
                        )
                    }

                    item(key = "left_chapter_nav", contentType = "chapter_nav") {
                        val isVersionEn = BibleBooksData.isEnglishVersion(uiState.activeVersion)
                        val catalog = uiState.activeVersionCatalog
                        val currentCanonicalId = BibleBooksData.resolveCanonicalBookId(uiState.currentBook)
                        val prevPassage = catalog?.getPreviousPassage(uiState.currentBook, uiState.currentChapter)
                        val nextPassage = catalog?.getNextPassage(uiState.currentBook, uiState.currentChapter)

                        val prevLabel = prevPassage?.let { (book, chap) ->
                            if (book.number == currentCanonicalId) {
                                if (isVersionEn) "Ch. $chap" else "Cap. $chap"
                            } else {
                                "${book.abbreviation} $chap"
                            }
                        }
                        val nextLabel = nextPassage?.let { (book, chap) ->
                            if (book.number == currentCanonicalId) {
                                if (isVersionEn) "Ch. $chap" else "Cap. $chap"
                            } else {
                                "${book.abbreviation} $chap"
                            }
                        }

                        ChapterBottomNavigationControls(
                            previousLabel = prevLabel,
                            nextLabel = nextLabel,
                            onPreviousChapter = { viewModel.navigateToPreviousChapter() },
                            onNextChapter = { viewModel.navigateToNextChapter() }
                        )
                    }
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
            val rightBookId = rightBookInfoResolved?.number ?: 1
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
                            openRightIndependentPicker(displayRightTitle, displayRightChapter)
                        }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$localizedRightBook $displayRightChapter",
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.5.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = headingColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Box {
                    Surface(
                        shape = CircleShape,
                        color = appColors.surfaceVariant,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { isRightVersionMenuOpen = true }
                            .testTag("parallel_right_version_selector")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = uiState.parallelVersion,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                                maxLines = 1,
                                softWrap = false,
                                color = appColors.accent,
                                fontWeight = FontWeight.SemiBold
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
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (uiState.parallelVersion == tag) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (uiState.parallelVersion == tag) appColors.accent else appColors.textPrimary
                                        )
                                    },
                                    onClick = {
                                        isRightVersionMenuOpen = false
                                        onSelectRightVersion(tag)
                                    }
                                )
                            }
                        } else {
                            downloadedVersions.forEach { module ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "${module.title} (${module.versionTag})",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (uiState.parallelVersion == module.versionTag) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (uiState.parallelVersion == module.versionTag) appColors.accent else appColors.textPrimary,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    },
                                    onClick = {
                                        isRightVersionMenuOpen = false
                                        onSelectRightVersion(module.versionTag)
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
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("parallel_right_list"),
                contentPadding = PaddingValues(
                    start = 10.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 96.dp + navBarBottom
                )
            ) {
                if (uiState.isParallelChapterLoading && uiState.parallelVerses.isEmpty()) {
                    item(key = "right_loading", contentType = "loading") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = appColors.accent,
                                strokeWidth = 2.dp
                            )
                        }
                    }
                } else if (uiState.parallelVerses.isEmpty()) {
                    item(key = "right_unavailable", contentType = "unavailable") {
                        PassageUnavailableState(
                            versionCode = uiState.parallelVersion,
                            interfaceLanguage = uiState.userPreferences.interfaceLanguage,
                            availableVersions = downloadedVersions,
                            onOpenAvailableBooks = {
                                if (isLinked) {
                                    viewModel.openPassagePicker()
                                } else {
                                    openRightIndependentPicker(displayRightTitle, displayRightChapter)
                                }
                            },
                            onSelectVersion = { tag ->
                                onSelectRightVersion(tag)
                            },
                            compact = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp)
                                .testTag("right_passage_unavailable_state")
                        )
                    }
                } else {
                    items(
                        count = uiState.parallelVerses.size,
                        key = { index -> "R_${uiState.parallelVerses[index].id}" },
                        contentType = { "verse" }
                    ) { index ->
                        val verse = uiState.parallelVerses[index]
                        val hasCommentary = true
                        val hasCrossReference = if (isLinked) {
                            verse.verse in uiState.versesWithCrossReferences
                        } else {
                            verse.verse in uiState.parallelVersesWithCrossReferences
                        }
                        val hasRvrVersionNote = verse.verse in uiState.parallelVersesWithRvrNotes

                        VerseRowItem(
                            verse = verse,
                            precomputedTokens = uiState.parallelVerseTokens[verse.id],
                            subheadings = uiState.parallelChapterSubheadings[verse.verse].orEmpty(),
                            hasCommentary = hasCommentary,
                            hasCrossReference = hasCrossReference,
                            hasRvrVersionNote = hasRvrVersionNote,
                            isHighlighted = if (isLinked) {
                                uiState.highlightedVerse == verse.verse
                            } else {
                                rightHighlightedVerse == verse.verse
                            },
                            isSelected = selectedIsRightPane && verse.verse in selectedVerses,
                            isSelectionMode = isSelectionMode,
                            fontFamily = fontFamily,
                            fontSize = fontSize,
                            lineHeight = lineHeight,
                            onCommentaryClick = { code ->
                                if (uiState.isNavigationHighlightActive) viewModel.clearNavigationHighlight()
                                onClearRightHighlight()
                                viewModel.openCommentary(verse, code)
                            },
                            onCrossRefClick = {
                                if (uiState.isNavigationHighlightActive) viewModel.clearNavigationHighlight()
                                onClearRightHighlight()
                                viewModel.openCrossReferences(verse)
                            },
                            onWordClick = { word ->
                                if (uiState.isNavigationHighlightActive) viewModel.clearNavigationHighlight()
                                onClearRightHighlight()
                                viewModel.lookupWord(word, verse)
                            },
                            onNoteClick = { marker ->
                                if (uiState.isNavigationHighlightActive) viewModel.clearNavigationHighlight()
                                onClearRightHighlight()
                                viewModel.onVersionNoteClick(verse, marker)
                            },
                            onAnyVerseTap = {
                                if (uiState.isNavigationHighlightActive && uiState.highlightedVerse != verse.verse) {
                                    viewModel.clearNavigationHighlight()
                                }
                                if (rightHighlightedVerse != null && rightHighlightedVerse != verse.verse) {
                                    onClearRightHighlight()
                                }
                            },
                            onVerseClick = { onRightVerseClick(verse.verse) },
                            onVerseLongClick = { onRightVerseLongClick(verse.verse) }
                        )
                    }

                    item(key = "right_chapter_nav", contentType = "chapter_nav") {
                        val rightBook = if (isLinked) uiState.currentBook else (rightBookInfo?.name ?: rightBookTitle)
                        val activeRightChapter = if (isLinked) uiState.currentChapter else rightChapter
                        val rightCatalog = if (isLinked) uiState.activeVersionCatalog else uiState.parallelVersionCatalog
                        val currentRightCanonical = BibleBooksData.resolveCanonicalBookId(rightBook)
                        val prevRight = rightCatalog?.getPreviousPassage(rightBook, activeRightChapter)
                        val nextRight = rightCatalog?.getNextPassage(rightBook, activeRightChapter)
                        val isRightVersionEn = BibleBooksData.isEnglishVersion(uiState.parallelVersion)

                        val prevLabel = prevRight?.let { (book, chap) ->
                            if (book.number == currentRightCanonical) {
                                if (isRightVersionEn) "Ch. $chap" else "Cap. $chap"
                            } else {
                                "${book.abbreviation} $chap"
                            }
                        }
                        val nextLabel = nextRight?.let { (book, chap) ->
                            if (book.number == currentRightCanonical) {
                                if (isRightVersionEn) "Ch. $chap" else "Cap. $chap"
                            } else {
                                "${book.abbreviation} $chap"
                            }
                        }

                        ChapterBottomNavigationControls(
                            previousLabel = prevLabel,
                            nextLabel = nextLabel,
                            onPreviousChapter = {
                                prevRight?.let { (book, chap) ->
                                    if (isLinked) {
                                        viewModel.loadChapter(book.name, chap)
                                    } else {
                                        onUpdateRightPassage(book.name, chap, 1)
                                    }
                                }
                            },
                            onNextChapter = {
                                nextRight?.let { (book, chap) ->
                                    if (isLinked) {
                                        viewModel.loadChapter(book.name, chap)
                                    } else {
                                        onUpdateRightPassage(book.name, chap, 1)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (isRightPickerOpen && !isLinked) {
        PassageSelectorDialog(
            currentStep = rightPickerStep,
            selectedBook = rightBookInfo ?: BibleBooksData.books.first(),
            selectedChapter = rightPickerChapter,
            catalog = rightPickerCatalog,
            availableVerses = rightPickerAvailableVerses,
            interfaceLanguage = uiState.userPreferences.interfaceLanguage,
            onBookSelected = { selected ->
                val defaultChap = rightPickerCatalog?.getAvailableChapters(selected)?.firstOrNull() ?: 1
                rightBookInfo = selected
                rightPickerChapter = defaultChap
                rightPickerAvailableVerses = emptyList()
                rightPickerStep = 2
            },
            onChapterSelected = { ch ->
                rightPickerChapter = ch
                rightPickerAvailableVerses = emptyList()
                rightPickerStep = 3
                val selectedName = rightBookInfo?.name ?: rightBookTitle
                coroutineScope.launch {
                    rightPickerAvailableVerses = viewModel.getAvailableVersesForVersion(
                        version = uiState.parallelVersion,
                        bookName = selectedName,
                        chapter = ch
                    )
                }
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
private fun PassageUnavailableState(
    versionCode: String,
    interfaceLanguage: String,
    availableVersions: List<BibleModule>,
    onOpenAvailableBooks: () -> Unit,
    onSelectVersion: (String) -> Unit,
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val appColors = AppTheme.colors
    val isCrispLight = !appColors.isDark && appColors.themeId == AppThemeIds.CRISP_LIGHT
    val headingColor = if (isCrispLight) ReaderCrispTitleNavy else appColors.titleColor
    val mutedTextColor = if (isCrispLight) ReaderCrispMutedText else appColors.textSecondary
    val isEn = interfaceLanguage.contains("English", ignoreCase = true)
    var versionMenuExpanded by remember { mutableStateOf(false) }
    val otherVersions = remember(availableVersions, versionCode) {
        val list = availableVersions.filter { !it.versionTag.equals(versionCode, ignoreCase = true) }
        list
    }

    val message = if (isEn) {
        "This passage is not available in $versionCode"
    } else {
        "Este pasaje no está disponible en la versión $versionCode"
    }

    Column(
        modifier = modifier.padding(horizontal = if (compact) 8.dp else 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.MenuBook,
            contentDescription = null,
            tint = mutedTextColor.copy(alpha = 0.7f),
            modifier = Modifier.size(if (compact) 28.dp else 36.dp)
        )
        Spacer(modifier = Modifier.height(if (compact) 10.dp else 14.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.titleSmall.copy(
                fontSize = if (compact) 13.sp else 15.5.sp,
                lineHeight = if (compact) 18.sp else 22.sp
            ),
            fontWeight = FontWeight.SemiBold,
            color = headingColor,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(if (compact) 12.dp else 16.dp))

        if (compact) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenAvailableBooks,
                    shape = CircleShape,
                    border = BorderStroke(1.dp, appColors.border),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("unavailable_open_books_button")
                ) {
                    Text(
                        text = if (isEn) "Available books" else "Ver libros",
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = headingColor,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Box {
                    TextButton(
                        onClick = { versionMenuExpanded = true },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("unavailable_switch_version_button")
                    ) {
                        Text(
                            text = if (isEn) "Switch version" else "Cambiar versión",
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                            fontWeight = FontWeight.Medium,
                            color = appColors.accent,
                            maxLines = 1,
                            softWrap = false
                        )
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
                        if (otherVersions.isEmpty()) {
                            listOf("RVR1960", "KJV").filter { !it.equals(versionCode, ignoreCase = true) }.forEach { tag ->
                                DropdownMenuItem(
                                    text = { Text(tag, style = MaterialTheme.typography.bodyMedium, color = appColors.textPrimary) },
                                    onClick = {
                                        versionMenuExpanded = false
                                        onSelectVersion(tag)
                                    }
                                )
                            }
                        } else {
                            otherVersions.forEach { module ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "${module.title} (${module.versionTag})",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = appColors.textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    onClick = {
                                        versionMenuExpanded = false
                                        onSelectVersion(module.versionTag)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onOpenAvailableBooks,
                    shape = CircleShape,
                    border = BorderStroke(1.dp, appColors.border),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("unavailable_open_books_button")
                ) {
                    Text(
                        text = if (isEn) "Available books" else "Ver libros disponibles",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = headingColor,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Box {
                    TextButton(
                        onClick = { versionMenuExpanded = true },
                        modifier = Modifier.testTag("unavailable_switch_version_button")
                    ) {
                        Text(
                            text = if (isEn) "Switch version" else "Cambiar versión",
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = appColors.accent,
                            maxLines = 1,
                            softWrap = false
                        )
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
                        if (otherVersions.isEmpty()) {
                            listOf("RVR1960", "KJV").filter { !it.equals(versionCode, ignoreCase = true) }.forEach { tag ->
                                DropdownMenuItem(
                                    text = { Text(tag, style = MaterialTheme.typography.bodyMedium, color = appColors.textPrimary) },
                                    onClick = {
                                        versionMenuExpanded = false
                                        onSelectVersion(tag)
                                    }
                                )
                            }
                        } else {
                            otherVersions.forEach { module ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "${module.title} (${module.versionTag})",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = appColors.textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    onClick = {
                                        versionMenuExpanded = false
                                        onSelectVersion(module.versionTag)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChapterBottomNavigationControls(
    previousLabel: String?,
    nextLabel: String?,
    onPreviousChapter: () -> Unit,
    onNextChapter: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appColors = AppTheme.colors
    val isCrispLight = !appColors.isDark && appColors.themeId == AppThemeIds.CRISP_LIGHT
    val headingColor = if (isCrispLight) ReaderCrispTitleNavy else appColors.textPrimary

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!previousLabel.isNullOrBlank()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = appColors.surfaceVariant,
                border = BorderStroke(1.dp, appColors.border),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onPreviousChapter() }
                    .testTag("chapter_prev_button")
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
                        text = previousLabel,
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.5.sp),
                        color = headingColor,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.width(1.dp))
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (!nextLabel.isNullOrBlank()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = appColors.surfaceVariant,
                border = BorderStroke(1.dp, appColors.border),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onNextChapter() }
                    .testTag("chapter_next_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = nextLabel,
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.5.sp),
                        color = headingColor,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
    precomputedTokens: List<VerseDisplayToken>? = null,
    subheadings: List<String> = emptyList(),
    hasCommentary: Boolean,
    hasCrossReference: Boolean,
    hasRvrVersionNote: Boolean = false,
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
    onAnyVerseTap: () -> Unit = {},
    onVerseClick: () -> Unit,
    onVerseLongClick: () -> Unit
) {
    val appColors = AppTheme.colors
    val isCrispLight = !appColors.isDark && appColors.themeId == AppThemeIds.CRISP_LIGHT
    val headingColor = if (isCrispLight) ReaderCrispTitleNavy else appColors.titleColor
    val mutedTextColor = if (isCrispLight) ReaderCrispMutedText else appColors.textSecondary
    val selectionBgColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    val tempHighlightColor = appColors.accent.copy(alpha = if (appColors.isDark) 0.22f else 0.14f)

    val targetBgColor = when {
        isSelected -> selectionBgColor
        isHighlighted -> tempHighlightColor
        else -> Color.Transparent
    }
    val animatedBgColor by animateColorAsState(
        targetValue = targetBgColor,
        animationSpec = tween(durationMillis = if (isHighlighted || isSelected) 220 else 550),
        label = "verseHighlightBg"
    )

    // Cuando el ViewModel entrega tokens preprocesados en Dispatchers.Default,
    // el hilo principal evita cualquier ejecución de Regex durante el scroll.
    val resolvedTokens = remember(precomputedTokens, verse.text, verse.version, hasRvrVersionNote) {
        precomputedTokens ?: run {
            val isRvr1960 = BibleStudyTextUtils.isRvr1960Version(verse.version)
            val showRvrAsterisks = isRvr1960 && hasRvrVersionNote
            val rawCleanText = BibleStudyTextUtils.sanitizeVerseTextForDisplay(
                rawText = verse.text,
                version = verse.version,
                showRvrAsterisks = showRvrAsterisks
            )
            BibleStudyTextUtils.tokenizeVerseText(rawCleanText, showRvrAsterisks)
        }
    }

    val effectiveSubheadings = remember(subheadings, verse.sectionTitle) {
        if (subheadings.isNotEmpty()) {
            subheadings
        } else {
            listOfNotNull(verse.sectionTitle?.takeIf { it.isNotBlank() })
        }
    }

    val currentIsSelectionMode by rememberUpdatedState(isSelectionMode)
    val currentOnVerseClick by rememberUpdatedState(onVerseClick)
    val currentOnVerseLongClick by rememberUpdatedState(onVerseLongClick)
    val currentOnCommentaryClick by rememberUpdatedState(onCommentaryClick)
    val currentOnCrossRefClick by rememberUpdatedState(onCrossRefClick)
    val currentOnWordClick by rememberUpdatedState(onWordClick)
    val currentOnNoteClick by rememberUpdatedState(onNoteClick)
    val currentOnAnyVerseTap by rememberUpdatedState(onAnyVerseTap)

    val annotatedString = remember(
        verse.verse,
        resolvedTokens,
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

            val bracketNoteStyle = SpanStyle(
                color = appColors.accent.copy(alpha = 0.85f),
                fontSize = (fontSize.value * 0.65f).sp,
                baselineShift = BaselineShift.Superscript,
                fontWeight = FontWeight.Medium
            )
            val asteriskNoteStyle = SpanStyle(
                color = appColors.accent,
                fontSize = (fontSize.value * 0.78f).sp,
                baselineShift = BaselineShift.Superscript,
                fontWeight = FontWeight.Bold
            )
            val wordStyle = SpanStyle(
                color = appColors.textPrimary
            )

            resolvedTokens.forEach { token ->
                when (token) {
                    is VerseDisplayToken.BracketNote -> {
                        pushStringAnnotation(tag = "NOTE", annotation = token.marker)
                        withStyle(bracketNoteStyle) {
                            append("[${token.marker}]")
                        }
                        pop()
                    }
                    is VerseDisplayToken.AsteriskNote -> {
                        pushStringAnnotation(
                            tag = "NOTE",
                            annotation = "${BibleStudyTextUtils.RVR_NOTE_MARKER_PREFIX}${token.ordinal}"
                        )
                        withStyle(asteriskNoteStyle) {
                            append("*")
                        }
                        pop()
                    }
                    is VerseDisplayToken.Word -> {
                        pushStringAnnotation(tag = "WORD", annotation = token.text)
                        withStyle(wordStyle) {
                            append(token.text)
                        }
                        pop()
                    }
                    is VerseDisplayToken.Other -> {
                        withStyle(wordStyle) {
                            append(token.text)
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

    Column(modifier = Modifier.fillMaxWidth()) {
        // Subtítulos de pasaje automáticos con tipografía unificada de interfaz (fuera del bloque resaltable/seleccionable)
        if (effectiveSubheadings.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 4.dp,
                        end = 4.dp,
                        top = if (verse.verse == 1) 4.dp else 14.dp,
                        bottom = 4.dp
                    )
                    .testTag("verse_subheading_${verse.verse}")
            ) {
                effectiveSubheadings.forEachIndexed { idx, heading ->
                    val isParallelRef = BibleStudyTextUtils.isParallelReferenceSubheading(heading)
                    if (isParallelRef) {
                        Text(
                            text = heading,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = (fontSize.value * 0.78f).coerceIn(11.5f, 15.5f).sp,
                                lineHeight = (fontSize.value * 1.15f).coerceIn(16f, 22f).sp,
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.Normal
                            ),
                            color = mutedTextColor,
                            modifier = Modifier.padding(top = if (idx > 0) 2.dp else 0.dp)
                        )
                    } else {
                        Text(
                            text = heading,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = (fontSize.value * 0.96f).coerceIn(14.5f, 21f).sp,
                                lineHeight = (fontSize.value * 1.28f).coerceIn(19f, 27f).sp,
                                letterSpacing = (-0.15).sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = headingColor,
                            modifier = Modifier.padding(top = if (idx > 0) 4.dp else 0.dp)
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(animatedBgColor)
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        currentOnAnyVerseTap()
                        if (currentIsSelectionMode) {
                            currentOnVerseClick()
                        }
                    },
                    onLongClick = { currentOnVerseLongClick() }
                )
                .padding(vertical = 4.dp, horizontal = 4.dp)
                .testTag("verse_item_${verse.verse}")
        ) {
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
                                currentOnAnyVerseTap()
                                if (currentIsSelectionMode) {
                                    currentOnVerseClick()
                                    return@detectTapGestures
                                }

                                layoutResult?.let { layout ->
                                    // 1. Hit-box ampliada (mínimo 40x40 dp) para notas de versión [n] y asteriscos (*)
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
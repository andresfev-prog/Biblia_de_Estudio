package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.BibleBooksData
import com.example.data.model.BibleModule
import com.example.data.model.BibleVerse
import com.example.data.model.BookInfo
import com.example.data.model.Commentary
import com.example.data.model.CrossReference
import com.example.data.model.DictionaryEntry
import com.example.data.model.DictionaryMatch
import com.example.data.model.FavoriteVerse
import com.example.data.model.ModuleBibleCatalog
import com.example.data.model.ReadingProgress
import com.example.data.model.SearchHistoryItem
import com.example.data.model.UserNote
import com.example.data.model.UserPreference
import com.example.data.repository.BibleAssetDatabaseHelper
import com.example.data.repository.BibleRepository
import com.example.data.repository.CloudSyncRepository
import com.example.data.repository.ReadingStateCache
import com.example.data.repository.SyncStatus
import com.example.data.repository.UserCancelledSignInException
import com.example.ui.components.TutorialStep
import com.example.ui.theme.ThemePreferenceCache
import com.example.ui.util.BibleStudyTextUtils
import com.example.ui.util.DailyVerseCatalog
import com.example.ui.util.VerseDisplayToken
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.UUID
import com.example.data.util.ModuleDownloader

enum class AppDestination {
    HOME,
    BIBLE,
    MODULES,
    SEARCH,
    SETTINGS
}

data class MissingNoteModulePrompt(
    val moduleId: String,
    val moduleTag: String,
    val moduleTitle: String,
    val version: String,
    val verseReference: String
)

data class BibleUiState(
    val currentDestination: AppDestination = AppDestination.HOME,
    val currentBook: String = "Génesis",
    val currentChapter: Int = 1,
    val currentVerse: Int = 1,
    val activeVersion: String = "RVR1960",
    val parallelVersion: String = "KJV",
    val isParallelMode: Boolean = false,
    val isParallelLinked: Boolean = true,
    val verses: List<BibleVerse> = emptyList(),
    val parallelVerses: List<BibleVerse> = emptyList(),
    val verseTokens: Map<Long, List<VerseDisplayToken>> = emptyMap(),
    val parallelVerseTokens: Map<Long, List<VerseDisplayToken>> = emptyMap(),
    val isChapterLoading: Boolean = false,
    val isParallelChapterLoading: Boolean = false,
    val activeVersionCatalog: ModuleBibleCatalog? = null,
    val parallelVersionCatalog: ModuleBibleCatalog? = null,
    val chapterSubheadings: Map<Int, List<String>> = emptyMap(),
    val parallelChapterSubheadings: Map<Int, List<String>> = emptyMap(),
    val versesWithRvrNotes: Set<Int> = emptySet(),
    val parallelVersesWithRvrNotes: Set<Int> = emptySet(),
    val selectedVerses: List<BibleVerse> = emptyList(),
    val highlightedVerse: Int? = null,
    val highlightToken: Long = 0L,
    val isNavigationHighlightActive: Boolean = false,
    // Comentarios
    val versesWithComments: Set<Int> = emptySet(),
    val parallelVersesWithComments: Set<Int> = emptySet(),
    // Modules
    val modules: List<BibleModule> = emptyList(),
    val moduleLanguageFilter: String = "Todos",
    val moduleSelectedTab: Int = 0,
    val downloadProgress: Map<String, Float> = emptyMap(), // Única línea nueva agregada
    // Referencias cruzadas dinámicas TSK
    val versesWithCrossReferences: Set<Int> = emptySet(),
    val parallelVersesWithCrossReferences: Set<Int> = emptySet(),
    // Selector de pasaje
    val isPassagePickerVisible: Boolean = false,
    val pickerStep: Int = 1,
    val pickerSelectedBook: BookInfo = BibleBooksData.books.first { it.number == 1 },
    val pickerSelectedChapter: Int = 1,
    val pickerAvailableVerses: List<Int> = emptyList(),
    // Modales de estudio
    val selectedCommentaryVerse: BibleVerse? = null,
    val currentVerseCommentaries: List<Commentary> = emptyList(),
    val missingNoteModulePrompt: MissingNoteModulePrompt? = null,
    val selectedDictionaryWord: String? = null,
    val currentDictionaryEntry: DictionaryEntry? = null,
    val dictionaryMatches: List<DictionaryMatch> = emptyList(),
    val isWordLookupLoading: Boolean = false,
    val selectedCrossReferenceVerse: BibleVerse? = null,
    val currentVerseCrossReferences: List<CrossReference> = emptyList(),
    // Audio TTS
    val isTtsVisible: Boolean = false,
    val isTtsPlaying: Boolean = false,
    val ttsVerseIndex: Int = 0,
    // Búsqueda
    val searchQuery: String = "",
    val searchScope: String = "TODOS",
    val searchVersion: String = "RVR1960",
    val searchExactWord: Boolean = true,
    val searchResults: List<BibleVerse> = emptyList(),
    val isSearching: Boolean = false,
    val searchCountAll: Int = 0,
    val searchCountOt: Int = 0,
    val searchCountNt: Int = 0,
    val searchFilter: String = "TODOS",
    val searchVerseResults: List<BibleVerse> = emptyList(),
    val searchCommentaryResults: List<Commentary> = emptyList(),
    val searchDictionaryResults: List<DictionaryEntry> = emptyList(),
    val searchHistory: List<SearchHistoryItem> = emptyList(),
    // Preferencias y Datos de usuario
    val userPreferences: UserPreference = UserPreference(),
    val readingProgress: ReadingProgress? = null,
    val hasStartedReading: Boolean = false,
    val continueReadingPreviewText: String = "",
    val favorites: List<FavoriteVerse> = emptyList(),
    val favoriteKeys: Set<String> = emptySet(),
    val notes: List<UserNote> = emptyList(),
    val isNotesSheetOpen: Boolean = false,
    val editingNote: UserNote? = null,
    val lastConsultedCommentary: Commentary? = null,
    val lastConsultedDictionary: DictionaryEntry? = null,
    // UI general
    val isBottomBarVisible: Boolean = true,
    val showFavoritesDialog: Boolean = false,
    val showEditProfileDialog: Boolean = false,
    val toastMessage: String? = null,
    val activeTutorialStep: TutorialStep? = null,
    // Perfil de Google y respaldo en la nube
    val googleUid: String? = null,
    val googleDisplayName: String = "",
    val googleEmail: String = "",
    val syncStatus: SyncStatus = SyncStatus.IDLE,
    val isGoogleAuthInProgress: Boolean = false
)

class BibleViewModel(
    application: Application,
    private val repository: BibleRepository,
    private val cloudSyncRepository: CloudSyncRepository = CloudSyncRepository()
) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    private val downloader = ModuleDownloader(application)
    private val favoriteMutex = Mutex()
    private val syncMutex = Mutex()
    private val themeCache = ThemePreferenceCache(application)
    private val initialTheme = themeCache.loadInitialTheme()
    private val readingStateCache = ReadingStateCache(application)
    private val initialHasStartedReading = readingStateCache.hasStartedReading()

    private val _uiState = MutableStateFlow(
        BibleUiState(
            userPreferences = UserPreference(
                themeMode = initialTheme.first,
                selectedLightTheme = initialTheme.second,
                selectedDarkTheme = initialTheme.third
            ),
            hasStartedReading = initialHasStartedReading,
            continueReadingPreviewText = if (initialHasStartedReading) {
                readingStateCache.loadLastPreviewText()
            } else {
                ""
            }
        )
    )
    val uiState: StateFlow<BibleUiState> = _uiState.asStateFlow()

    private fun syncThemeCache(pref: UserPreference) {
        themeCache.saveTheme(
            themeMode = pref.themeMode,
            selectedLightTheme = pref.selectedLightTheme,
            selectedDarkTheme = pref.selectedDarkTheme
        )
    }

    private val assetHelper = BibleAssetDatabaseHelper(application)

    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false
    private var hasSpokenChapterIntro = false
    private var hasUserNavigatedInSession = false
    private var navigationHighlightJob: Job? = null
    private var mainChapterJob: Job? = null
    private var parallelChapterJob: Job? = null
    private var mainCatalogJob: Job? = null
    private var parallelCatalogJob: Job? = null
    private var crossRefsJob: Job? = null
    private var parallelCrossRefsJob: Job? = null
    private var commentaryPillsJob: Job? = null
    private var previewJob: Job? = null
    private var saveProgressJob: Job? = null
    private var lastGeneralCommentaryFiles: Set<String>? = null

    private val _searchQueryInput = MutableStateFlow("")

    init {
        initTts(application)
        observeData()
        observeSearchQuery()
        // Precargar Génesis 1 en memoria para apertura instantánea sin marcar lectura iniciada en instalación nueva
        loadChapter(_uiState.value.currentBook, _uiState.value.currentChapter, targetVerse = 1, persistProgress = false)
    }

    private fun initTts(application: Application) {
        tts = TextToSpeech(application, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val spanishLocale = Locale("es", "ES")
            val langResult = tts?.setLanguage(spanishLocale)

            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                val fallbackLocale = Locale.getDefault()
                tts?.setLanguage(fallbackLocale)
            }

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _uiState.value = _uiState.value.copy(isTtsPlaying = true)
                }

                override fun onDone(utteranceId: String?) {
                    val nextIdx = _uiState.value.ttsVerseIndex + 1
                    if (nextIdx < _uiState.value.verses.size) {
                        _uiState.value = _uiState.value.copy(ttsVerseIndex = nextIdx)
                        playTts()
                    } else {
                        _uiState.value = _uiState.value.copy(isTtsPlaying = false)
                    }
                }

                override fun onError(utteranceId: String?) {
                    _uiState.value = _uiState.value.copy(isTtsPlaying = false)
                }
            })
        }
    }

    private fun configureNaturalTtsVoice(textToSpeak: String = "") {
        val engine = tts ?: return
        engine.setSpeechRate(0.90f)
        engine.setPitch(0.98f)

        val isKjv = _uiState.value.activeVersion.contains("KJV", ignoreCase = true)
        val selectedLocale = if (isKjv) Locale("en", "US") else Locale("es", "ES")
        engine.setLanguage(selectedLocale)

        try {
            val voiceList = engine.voices
            if (voiceList != null) {
                for (v in voiceList) {
                    val lang = v.locale.language
                    val matchesLang = if (isKjv) lang.equals("en", true) else lang.equals("es", true)
                    if (matchesLang && v.quality >= Voice.QUALITY_HIGH) {
                        engine.voice = v
                        break
                    }
                }
            }
        } catch (_: Exception) {}
    }

    private fun observeData() {
        viewModelScope.launch {
            repository.getAllModules().collect { mods ->
                if (mods.isEmpty() || mods.size < 59 || mods.none { it.id == "mod_rvr1960" } || mods.none { it.id == "mod_kjv" }) {
                    repository.seedInitialModules()
                } else {
                    _uiState.value = _uiState.value.copy(modules = mods)
                    val generalCommentaries = mods
                        .filter {
                            it.category == "COMMENTARY" &&
                                it.isDownloaded &&
                                !it.fileName.isNullOrBlank() &&
                                !BibleRepository.isVersionSpecificCommentary(it.fileName)
                        }
                        .mapNotNull { it.fileName }
                        .toSet()
                    if (generalCommentaries != lastGeneralCommentaryFiles) {
                        lastGeneralCommentaryFiles = generalCommentaries
                        if (generalCommentaries.isNotEmpty()) {
                            refreshCommentaryPills()
                        }
                    }
                }
            }
        }

        viewModelScope.launch {
            repository.getReadingProgress().collect { progress ->
                if (progress != null) {
                    readingStateCache.setHasStartedReading(true)
                    val safeVerse = progress.verse.coerceAtLeast(1)
                    val current = _uiState.value
                    val cachedPreview = readingStateCache.loadPreviewFor(
                        version = progress.version,
                        bookName = progress.bookName,
                        chapter = progress.chapter,
                        verse = safeVerse
                    )
                    val inMemoryPreview = extractPreviewFromLoadedVerses(
                        verses = current.verses,
                        loadedVersion = current.activeVersion,
                        loadedBook = current.currentBook,
                        loadedChapter = current.currentChapter,
                        targetVersion = progress.version,
                        targetBook = progress.bookName,
                        targetChapter = progress.chapter,
                        targetVerse = safeVerse
                    )
                    val immediatePreview = when {
                        inMemoryPreview.isNotBlank() -> inMemoryPreview
                        cachedPreview.isNotBlank() -> cachedPreview
                        else -> current.continueReadingPreviewText
                    }
                    if (inMemoryPreview.isNotBlank()) {
                        readingStateCache.savePreview(
                            version = progress.version,
                            bookName = progress.bookName,
                            chapter = progress.chapter,
                            verse = safeVerse,
                            cleanText = inMemoryPreview
                        )
                    }
                    _uiState.value = current.copy(
                        readingProgress = progress,
                        hasStartedReading = true,
                        continueReadingPreviewText = immediatePreview
                    )
                    if (inMemoryPreview.isBlank() && cachedPreview.isBlank()) {
                        refreshContinueReadingPreview(
                            version = progress.version,
                            bookName = progress.bookName,
                            chapter = progress.chapter,
                            verse = safeVerse
                        )
                    }
                    if (!hasUserNavigatedInSession) {
                        val samePassage = current.currentBook.equals(progress.bookName, ignoreCase = true) &&
                            current.currentChapter == progress.chapter &&
                            current.activeVersion.equals(progress.version, ignoreCase = true)
                        if (!samePassage) {
                            loadChapter(
                                bookName = progress.bookName,
                                chapter = progress.chapter,
                                version = progress.version,
                                targetVerse = safeVerse,
                                persistProgress = false
                            )
                        } else if (current.currentVerse != safeVerse) {
                            _uiState.value = _uiState.value.copy(currentVerse = safeVerse)
                        }
                    }
                } else {
                    _uiState.value = _uiState.value.copy(readingProgress = null)
                }
            }
        }

        viewModelScope.launch {
            repository.getAllFavorites().collect { favs ->
                val keys = favs.map { repository.buildFavoriteKey(it) }.toSet()
                _uiState.value = _uiState.value.copy(
                    favorites = favs,
                    favoriteKeys = keys
                )
            }
        }

        viewModelScope.launch {
            repository.getSearchHistory().collect { history ->
                _uiState.value = _uiState.value.copy(searchHistory = history)
            }
        }

        viewModelScope.launch {
            repository.getAllNotes().collect { list ->
                _uiState.value = _uiState.value.copy(notes = list)
            }
        }

        viewModelScope.launch {
            repository.getUserPreferences().collect { pref ->
                if (pref != null) {
                    val normalizedFont = if (pref.fontFamily.equals("Google Sans", ignoreCase = true)) {
                        "Google Sans Flex"
                    } else {
                        pref.fontFamily
                    }
                    val normalizedMode = if (pref.themeMode.equals("AUTOMATIC", ignoreCase = true) || pref.themeMode.isBlank()) {
                        "AUTO"
                    } else {
                        pref.themeMode.trim().uppercase()
                    }
                    val normalizedLight = pref.selectedLightTheme.trim().ifBlank { "CRISP_LIGHT" }
                    val normalizedDark = pref.selectedDarkTheme.trim().ifBlank { "MIDNIGHT_BLUE" }
                    val normalized = pref.copy(
                        fontFamily = normalizedFont,
                        themeMode = normalizedMode,
                        selectedLightTheme = normalizedLight,
                        selectedDarkTheme = normalizedDark
                    )
                    syncThemeCache(normalized)
                    _uiState.value = _uiState.value.copy(userPreferences = normalized)
                }
            }
        }

        viewModelScope.launch {
            cloudSyncRepository.syncStatus.collect { status ->
                _uiState.value = _uiState.value.copy(syncStatus = status)
            }
        }

        cloudSyncRepository.getCurrentUser()?.let { cloudUser ->
            _uiState.value = _uiState.value.copy(
                googleUid = cloudUser.uid,
                googleDisplayName = cloudUser.displayName,
                googleEmail = cloudUser.email
            )
            viewModelScope.launch {
                syncWithCloudInternal(uid = cloudUser.uid, isUserInitiated = false, showToast = false)
            }
        }
    }

    private fun sanitizePreviewText(rawOrCleanText: String, version: String): String {
        return BibleStudyTextUtils.sanitizeVerseTextForDisplay(
            rawText = rawOrCleanText,
            version = version,
            showRvrAsterisks = false
        ).replace(Regex("\\s+"), " ").trim()
    }

    private fun extractPreviewFromLoadedVerses(
        verses: List<BibleVerse>,
        loadedVersion: String,
        loadedBook: String,
        loadedChapter: Int,
        targetVersion: String,
        targetBook: String,
        targetChapter: Int,
        targetVerse: Int
    ): String {
        if (verses.isEmpty()) return ""
        if (!loadedVersion.equals(targetVersion, ignoreCase = true) ||
            !loadedBook.equals(targetBook, ignoreCase = true) ||
            loadedChapter != targetChapter
        ) {
            return ""
        }
        val safeVerse = targetVerse.coerceAtLeast(1)
        val verseObj = verses.firstOrNull { it.verse == safeVerse } ?: verses.firstOrNull() ?: return ""
        return sanitizePreviewText(verseObj.text, targetVersion)
    }

    private fun refreshContinueReadingPreview(
        version: String,
        bookName: String,
        chapter: Int,
        verse: Int
    ) {
        val safeVerse = verse.coerceAtLeast(1)
        val current = _uiState.value
        val inMemory = extractPreviewFromLoadedVerses(
            verses = current.verses,
            loadedVersion = current.activeVersion,
            loadedBook = current.currentBook,
            loadedChapter = current.currentChapter,
            targetVersion = version,
            targetBook = bookName,
            targetChapter = chapter,
            targetVerse = safeVerse
        )
        if (inMemory.isNotBlank()) {
            readingStateCache.savePreview(version, bookName, chapter, safeVerse, inMemory)
            _uiState.value = _uiState.value.copy(continueReadingPreviewText = inMemory)
            return
        }
        val cached = readingStateCache.loadPreviewFor(version, bookName, chapter, safeVerse)
        if (cached.isNotBlank()) {
            _uiState.value = _uiState.value.copy(continueReadingPreviewText = cached)
            return
        }
        previewJob?.cancel()
        previewJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val preview = repository.getSingleVersePreview(version, bookName, chapter, safeVerse)
                if (preview.isNotBlank()) {
                    readingStateCache.savePreview(version, bookName, chapter, safeVerse, preview)
                    withContext(Dispatchers.Main) {
                        val latest = _uiState.value
                        val progress = latest.readingProgress
                        val matchesProgress = progress == null || (
                            progress.version.equals(version, ignoreCase = true) &&
                                progress.bookName.equals(bookName, ignoreCase = true) &&
                                progress.chapter == chapter
                            )
                        if (latest.hasStartedReading && matchesProgress) {
                            _uiState.value = latest.copy(continueReadingPreviewText = preview)
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {}
        }
    }

    private fun markReadingStarted(
        bookName: String = _uiState.value.currentBook,
        chapter: Int = _uiState.value.currentChapter,
        verse: Int = _uiState.value.currentVerse.coerceAtLeast(1),
        version: String = _uiState.value.activeVersion
    ) {
        val safeVerse = verse.coerceAtLeast(1)
        hasUserNavigatedInSession = true
        readingStateCache.setHasStartedReading(true)
        val current = _uiState.value
        val inMemory = extractPreviewFromLoadedVerses(
            verses = current.verses,
            loadedVersion = current.activeVersion,
            loadedBook = current.currentBook,
            loadedChapter = current.currentChapter,
            targetVersion = version,
            targetBook = bookName,
            targetChapter = chapter,
            targetVerse = safeVerse
        )
        val cached = if (inMemory.isBlank()) {
            readingStateCache.loadPreviewFor(version, bookName, chapter, safeVerse)
        } else {
            ""
        }
        val resolvedPreview = when {
            inMemory.isNotBlank() -> {
                readingStateCache.savePreview(version, bookName, chapter, safeVerse, inMemory)
                inMemory
            }
            cached.isNotBlank() -> cached
            else -> current.continueReadingPreviewText
        }
        val updatedProgress = ReadingProgress(
            id = 1,
            bookName = bookName,
            chapter = chapter,
            verse = safeVerse,
            version = version
        )
        _uiState.value = current.copy(
            hasStartedReading = true,
            currentVerse = safeVerse,
            readingProgress = updatedProgress,
            continueReadingPreviewText = resolvedPreview
        )
        persistReadingProgressAsync(bookName, chapter, safeVerse, version)
        if (resolvedPreview.isBlank()) {
            refreshContinueReadingPreview(version, bookName, chapter, safeVerse)
        }
    }

    private fun persistReadingProgressAsync(
        bookName: String,
        chapter: Int,
        verse: Int,
        version: String
    ) {
        saveProgressJob?.cancel()
        saveProgressJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.saveReadingProgress(bookName, chapter, verse.coerceAtLeast(1), version)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {}
        }
    }

    fun startReadingFromHome() {
        val state = _uiState.value
        val targetBook = "Génesis"
        val targetChapter = 1
        val targetVerse = 1
        val targetVersion = state.activeVersion
        markReadingStarted(
            bookName = targetBook,
            chapter = targetChapter,
            verse = targetVerse,
            version = targetVersion
        )
        _uiState.value = _uiState.value.copy(
            currentDestination = AppDestination.BIBLE,
            selectedVerses = emptyList()
        )
        val alreadyPreloaded = _uiState.value.verses.isNotEmpty() &&
            _uiState.value.currentBook.equals(targetBook, ignoreCase = true) &&
            _uiState.value.currentChapter == targetChapter &&
            _uiState.value.activeVersion.equals(targetVersion, ignoreCase = true)
        if (!alreadyPreloaded && !_uiState.value.isChapterLoading) {
            loadChapter(
                bookName = targetBook,
                chapter = targetChapter,
                version = targetVersion,
                targetVerse = targetVerse,
                persistProgress = true
            )
        }
    }

    fun continueReadingFromHome() {
        val state = _uiState.value
        val progress = state.readingProgress
        val targetBook = progress?.bookName ?: state.currentBook
        val targetChapter = progress?.chapter ?: state.currentChapter
        val targetVerse = (progress?.verse ?: state.currentVerse).coerceAtLeast(1)
        val targetVersion = progress?.version ?: state.activeVersion

        markReadingStarted(
            bookName = targetBook,
            chapter = targetChapter,
            verse = targetVerse,
            version = targetVersion
        )
        _uiState.value = _uiState.value.copy(
            currentDestination = AppDestination.BIBLE,
            selectedVerses = emptyList()
        )
        val alreadyLoaded = _uiState.value.verses.isNotEmpty() &&
            _uiState.value.currentBook.equals(targetBook, ignoreCase = true) &&
            _uiState.value.currentChapter == targetChapter &&
            _uiState.value.activeVersion.equals(targetVersion, ignoreCase = true)
        if (targetVerse > 1) {
            triggerNavigationHighlight(targetVerse)
        }
        if (!alreadyLoaded) {
            loadChapter(
                bookName = targetBook,
                chapter = targetChapter,
                version = targetVersion,
                preserveHighlight = targetVerse > 1,
                targetVerse = targetVerse,
                persistProgress = true
            )
        }
    }

    fun updateVisibleReadingVerse(verse: Int) {
        val safeVerse = verse.coerceAtLeast(1)
        val state = _uiState.value
        if (state.isChapterLoading || state.verses.isEmpty()) return
        val existingVerse = state.readingProgress?.verse ?: state.currentVerse
        if (state.hasStartedReading &&
            existingVerse == safeVerse &&
            state.continueReadingPreviewText.isNotBlank() &&
            state.readingProgress?.bookName.equals(state.currentBook, ignoreCase = true) &&
            state.readingProgress?.chapter == state.currentChapter &&
            state.readingProgress?.version.equals(state.activeVersion, ignoreCase = true)
        ) {
            return
        }
        val verseObj = state.verses.firstOrNull { it.verse == safeVerse } ?: state.verses.firstOrNull() ?: return
        val cleanPreview = sanitizePreviewText(verseObj.text, state.activeVersion)
        hasUserNavigatedInSession = true
        readingStateCache.setHasStartedReading(true)
        if (cleanPreview.isNotBlank()) {
            readingStateCache.savePreview(
                version = state.activeVersion,
                bookName = state.currentBook,
                chapter = state.currentChapter,
                verse = safeVerse,
                cleanText = cleanPreview
            )
        }
        val updatedProgress = ReadingProgress(
            id = 1,
            bookName = state.currentBook,
            chapter = state.currentChapter,
            verse = safeVerse,
            version = state.activeVersion
        )
        _uiState.value = state.copy(
            hasStartedReading = true,
            currentVerse = safeVerse,
            readingProgress = updatedProgress,
            continueReadingPreviewText = cleanPreview.ifBlank { state.continueReadingPreviewText }
        )
        persistReadingProgressAsync(state.currentBook, state.currentChapter, safeVerse, state.activeVersion)
    }

    fun setSelectedVerses(verses: List<BibleVerse>) {
        if (_uiState.value.selectedVerses != verses) {
            _uiState.value = _uiState.value.copy(selectedVerses = verses)
        }
    }

    fun navigateTo(destination: AppDestination) {
        if (destination == AppDestination.BIBLE && !_uiState.value.hasStartedReading) {
            markReadingStarted(
                bookName = _uiState.value.currentBook,
                chapter = _uiState.value.currentChapter,
                verse = _uiState.value.currentVerse.coerceAtLeast(1),
                version = _uiState.value.activeVersion
            )
        }
        _uiState.value = _uiState.value.copy(
            currentDestination = destination,
            selectedVerses = if (destination == AppDestination.BIBLE) _uiState.value.selectedVerses else emptyList()
        )
        if (destination == AppDestination.BIBLE && _uiState.value.verses.isEmpty() && !_uiState.value.isChapterLoading) {
            loadChapter(
                bookName = _uiState.value.currentBook,
                chapter = _uiState.value.currentChapter,
                version = _uiState.value.activeVersion,
                targetVerse = _uiState.value.currentVerse.coerceAtLeast(1),
                persistProgress = true
            )
        }
    }

    fun startTutorial() {
        _uiState.value = _uiState.value.copy(
            activeTutorialStep = TutorialStep.HOME_VERSE_OF_DAY,
            currentDestination = AppDestination.HOME,
            selectedVerses = emptyList(),
            isBottomBarVisible = true,
            isPassagePickerVisible = false,
            isNotesSheetOpen = false,
            selectedCommentaryVerse = null,
            selectedDictionaryWord = null,
            selectedCrossReferenceVerse = null,
            showFavoritesDialog = false,
            showEditProfileDialog = false
        )
    }

    fun nextTutorialStep() {
        val current = _uiState.value.activeTutorialStep ?: return
        val next = current.next()
        if (next != null) {
            _uiState.value = _uiState.value.copy(
                activeTutorialStep = next,
                currentDestination = next.destination,
                selectedVerses = emptyList(),
                isBottomBarVisible = true,
                isPassagePickerVisible = false,
                isNotesSheetOpen = false,
                selectedCommentaryVerse = null,
                selectedDictionaryWord = null,
                selectedCrossReferenceVerse = null
            )
        } else {
            finishTutorial()
        }
    }

    fun previousTutorialStep() {
        val current = _uiState.value.activeTutorialStep ?: return
        val prev = current.previous()
        if (prev != null) {
            _uiState.value = _uiState.value.copy(
                activeTutorialStep = prev,
                currentDestination = prev.destination,
                selectedVerses = emptyList(),
                isBottomBarVisible = true,
                isPassagePickerVisible = false,
                isNotesSheetOpen = false,
                selectedCommentaryVerse = null,
                selectedDictionaryWord = null,
                selectedCrossReferenceVerse = null
            )
        }
    }

    fun finishTutorial() {
        _uiState.value = _uiState.value.copy(activeTutorialStep = null)
    }

    private fun precomputeVerseTokens(
        verses: List<BibleVerse>,
        version: String,
        rvrNotesSet: Set<Int>
    ): Map<Long, List<VerseDisplayToken>> {
        if (verses.isEmpty()) return emptyMap()
        val isRvr1960 = BibleStudyTextUtils.isRvr1960Version(version)
        val map = HashMap<Long, List<VerseDisplayToken>>(verses.size * 2)
        for (verse in verses) {
            val showAsterisk = isRvr1960 && verse.verse in rvrNotesSet
            map[verse.id] = BibleStudyTextUtils.tokenizeVerseText(verse.text, showAsterisk)
        }
        return map
    }

    private fun launchMainCatalogLoadIfNeeded(version: String) {
        val currentCatalog = _uiState.value.activeVersionCatalog
        if (currentCatalog != null &&
            currentCatalog.version.equals(version, ignoreCase = true) &&
            currentCatalog.availableBooks.isNotEmpty()
        ) {
            return
        }
        mainCatalogJob?.cancel()
        mainCatalogJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val loaded = repository.getModuleCatalog(version)
                withContext(Dispatchers.Main) {
                    if (_uiState.value.activeVersion.equals(version, ignoreCase = true)) {
                        _uiState.value = _uiState.value.copy(activeVersionCatalog = loaded)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("BIBLE_DEBUG", "Error cargando catálogo en segundo plano para $version: ${e.message}", e)
            }
        }
    }

    private fun launchParallelCatalogLoadIfNeeded(version: String) {
        val currentCatalog = _uiState.value.parallelVersionCatalog
        if (currentCatalog != null &&
            currentCatalog.version.equals(version, ignoreCase = true) &&
            currentCatalog.availableBooks.isNotEmpty()
        ) {
            return
        }
        parallelCatalogJob?.cancel()
        parallelCatalogJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val loaded = repository.getModuleCatalog(version)
                withContext(Dispatchers.Main) {
                    if (_uiState.value.parallelVersion.equals(version, ignoreCase = true)) {
                        _uiState.value = _uiState.value.copy(parallelVersionCatalog = loaded)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("BIBLE_DEBUG", "Error cargando catálogo paralelo en segundo plano para $version: ${e.message}", e)
            }
        }
    }

    fun loadChapter(
        bookName: String,
        chapter: Int,
        version: String = _uiState.value.activeVersion,
        parallelVersion: String = _uiState.value.parallelVersion,
        preserveHighlight: Boolean = false,
        forceClearMain: Boolean = false,
        forceClearParallel: Boolean = false,
        targetVerse: Int = 1,
        persistProgress: Boolean = true
    ) {
        val safeTargetVerse = targetVerse.coerceAtLeast(1)
        hasSpokenChapterIntro = false
        if (!preserveHighlight) {
            clearNavigationHighlight()
        }

        // Cancelar de inmediato cualquier consulta previa pendiente para evitar condiciones de carrera
        mainChapterJob?.cancel()
        crossRefsJob?.cancel()

        val prev = _uiState.value
        val shouldReloadParallel = prev.isParallelMode && prev.isParallelLinked
        val sameActiveVersion = prev.activeVersion.equals(version, ignoreCase = true)
        val sameParallelVersion = prev.parallelVersion.equals(parallelVersion, ignoreCase = true)
        val mainTargetChanged = !prev.currentBook.equals(bookName, ignoreCase = true) ||
            prev.currentChapter != chapter ||
            !sameActiveVersion
        val shouldClearMain = mainTargetChanged || forceClearMain || prev.verses.isEmpty()

        val parallelTargetChanged = !prev.currentBook.equals(bookName, ignoreCase = true) ||
            prev.currentChapter != chapter ||
            !sameParallelVersion
        val shouldClearParallel = shouldReloadParallel &&
            (parallelTargetChanged || forceClearParallel || prev.parallelVerses.isEmpty())

        if (shouldReloadParallel) {
            parallelChapterJob?.cancel()
            parallelCrossRefsJob?.cancel()
        }

        val immediateMainCatalog = if (sameActiveVersion && prev.activeVersionCatalog != null) {
            prev.activeVersionCatalog
        } else {
            repository.getCachedModuleCatalogInMemory(version)
        }
        val immediateParallelCatalog = if (sameParallelVersion && prev.parallelVersionCatalog != null) {
            prev.parallelVersionCatalog
        } else {
            repository.getCachedModuleCatalogInMemory(parallelVersion)
        }

        if (persistProgress) {
            hasUserNavigatedInSession = true
            readingStateCache.setHasStartedReading(true)
        }
        val immediateProgress = if (persistProgress) {
            ReadingProgress(
                id = 1,
                bookName = bookName,
                chapter = chapter,
                verse = safeTargetVerse,
                version = version
            )
        } else {
            prev.readingProgress
        }
        val cachedPreview = if (persistProgress) {
            readingStateCache.loadPreviewFor(version, bookName, chapter, safeTargetVerse)
                .ifBlank { if (mainTargetChanged) "" else prev.continueReadingPreviewText }
        } else {
            prev.continueReadingPreviewText
        }

        // Actualización atómica ANTES de lanzar I/O: elimina cualquier destello de versículos previos
        _uiState.value = prev.copy(
            currentBook = bookName,
            currentChapter = chapter,
            currentVerse = safeTargetVerse,
            activeVersion = version,
            parallelVersion = parallelVersion,
            hasStartedReading = if (persistProgress) true else prev.hasStartedReading,
            readingProgress = immediateProgress,
            continueReadingPreviewText = cachedPreview,
            ttsVerseIndex = 0,
            selectedVerses = emptyList(),
            verses = if (shouldClearMain) emptyList() else prev.verses,
            verseTokens = if (shouldClearMain) emptyMap() else prev.verseTokens,
            chapterSubheadings = if (shouldClearMain) emptyMap() else prev.chapterSubheadings,
            versesWithRvrNotes = if (shouldClearMain) emptySet() else prev.versesWithRvrNotes,
            versesWithCrossReferences = if (mainTargetChanged) emptySet() else prev.versesWithCrossReferences,
            activeVersionCatalog = immediateMainCatalog,
            isChapterLoading = true,
            parallelVerses = if (shouldClearParallel) emptyList() else prev.parallelVerses,
            parallelVerseTokens = if (shouldClearParallel) emptyMap() else prev.parallelVerseTokens,
            parallelChapterSubheadings = if (shouldClearParallel) emptyMap() else prev.parallelChapterSubheadings,
            parallelVersesWithRvrNotes = if (shouldClearParallel) emptySet() else prev.parallelVersesWithRvrNotes,
            parallelVersionCatalog = immediateParallelCatalog,
            isParallelChapterLoading = if (shouldReloadParallel) true else prev.isParallelChapterLoading
        )

        // 1. Ruta crítica desacoplada: consultar únicamente el capítulo activo (0 bloqueos en Room / catálogos)
        mainChapterJob = viewModelScope.launch {
            try {
                val (cleanList, rvrNotesSet, subheadings) = withContext(Dispatchers.IO) {
                    val notesSet = if (BibleStudyTextUtils.isRvr1960Version(version)) {
                        repository.getVersesWithRealVersionNotes(version, bookName, chapter)
                    } else {
                        emptySet()
                    }
                    val verses = repository.getChapterVersesOnce(
                        version = version,
                        bookName = bookName,
                        chapter = chapter,
                        rvrNotesVerses = notesSet
                    )
                    val headings = if (verses.isNotEmpty()) {
                        repository.getChapterSubheadings(
                            version = version,
                            bookName = bookName,
                            chapter = chapter,
                            chapterVerseNumbers = verses.map { it.verse }
                        )
                    } else {
                        emptyMap()
                    }
                    Triple(verses, notesSet, headings)
                }

                // Pre-tokenización fuera del hilo principal para scroll fluido en Compose
                val tokensMap = withContext(Dispatchers.Default) {
                    precomputeVerseTokens(cleanList, version, rvrNotesSet)
                }

                if (cleanList.isEmpty() && (_uiState.value.isTtsPlaying || _uiState.value.isTtsVisible)) {
                    stopTts()
                }

                val latestState = _uiState.value
                val previewVerseNum = if (persistProgress) {
                    safeTargetVerse
                } else {
                    (latestState.readingProgress?.verse ?: safeTargetVerse).coerceAtLeast(1)
                }
                val shouldUpdatePreview = persistProgress || (
                    latestState.hasStartedReading &&
                        (latestState.readingProgress == null || (
                            latestState.readingProgress.bookName.equals(bookName, ignoreCase = true) &&
                                latestState.readingProgress.chapter == chapter &&
                                latestState.readingProgress.version.equals(version, ignoreCase = true)
                            ))
                    )
                val resolvedPreview = if (shouldUpdatePreview) {
                    val vObj = cleanList.firstOrNull { it.verse == previewVerseNum } ?: cleanList.firstOrNull()
                    if (vObj != null) {
                        val clean = sanitizePreviewText(vObj.text, version)
                        if (clean.isNotBlank()) {
                            readingStateCache.savePreview(version, bookName, chapter, previewVerseNum, clean)
                        }
                        clean
                    } else {
                        ""
                    }
                } else {
                    latestState.continueReadingPreviewText
                }

                _uiState.value = latestState.copy(
                    currentBook = bookName,
                    currentChapter = chapter,
                    activeVersion = version,
                    verses = cleanList,
                    verseTokens = tokensMap,
                    chapterSubheadings = subheadings,
                    versesWithRvrNotes = rvrNotesSet,
                    continueReadingPreviewText = resolvedPreview,
                    isChapterLoading = false
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("BIBLE_DEBUG", "Error cargando capítulo $bookName $chapter ($version): ${e.message}", e)
                _uiState.value = _uiState.value.copy(isChapterLoading = false)
            }
        }

        // 2. Catálogo dinámico desacoplado en segundo plano (hit en caché de disco <1 ms / 0 escaneos SQLite)
        launchMainCatalogLoadIfNeeded(version)

        // 3. Etiquetas REF desde TSK-x.crossreferences.SQLite3 en segundo plano (sin bloquear el texto)
        crossRefsJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val refsSet = assetHelper.getVersesWithCrossReferences(bookName, chapter)
                withContext(Dispatchers.Main) {
                    val current = _uiState.value
                    if (current.currentBook.equals(bookName, ignoreCase = true) && current.currentChapter == chapter) {
                        _uiState.value = current.copy(
                            versesWithCrossReferences = refsSet,
                            parallelVersesWithCrossReferences = if (!current.isParallelMode || current.isParallelLinked) {
                                refsSet
                            } else {
                                current.parallelVersesWithCrossReferences
                            }
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("BIBLE_DEBUG", "Error cargando referencias cruzadas: ${e.message}", e)
            }
        }

        // 4. Panel paralelo (solo cuando la vista dividida está activa y enlazada)
        if (shouldReloadParallel) {
            parallelChapterJob = viewModelScope.launch {
                try {
                    val (cleanParallelList, parallelRvrNotes, parallelSubheadings) = withContext(Dispatchers.IO) {
                        val notesSet = if (BibleStudyTextUtils.isRvr1960Version(parallelVersion)) {
                            repository.getVersesWithRealVersionNotes(parallelVersion, bookName, chapter)
                        } else {
                            emptySet()
                        }
                        val verses = repository.getChapterVersesOnce(
                            version = parallelVersion,
                            bookName = bookName,
                            chapter = chapter,
                            rvrNotesVerses = notesSet
                        )
                        val headings = if (verses.isNotEmpty()) {
                            repository.getChapterSubheadings(
                                version = parallelVersion,
                                bookName = bookName,
                                chapter = chapter,
                                chapterVerseNumbers = verses.map { it.verse }
                            )
                        } else {
                            emptyMap()
                        }
                        Triple(verses, notesSet, headings)
                    }

                    val parallelTokens = withContext(Dispatchers.Default) {
                        precomputeVerseTokens(cleanParallelList, parallelVersion, parallelRvrNotes)
                    }

                    _uiState.value = _uiState.value.copy(
                        parallelVersion = parallelVersion,
                        parallelVerses = cleanParallelList,
                        parallelVerseTokens = parallelTokens,
                        parallelChapterSubheadings = parallelSubheadings,
                        parallelVersesWithRvrNotes = parallelRvrNotes,
                        isParallelChapterLoading = false
                    )
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e("BIBLE_DEBUG", "Error cargando capítulo paralelo $bookName $chapter ($parallelVersion): ${e.message}", e)
                    _uiState.value = _uiState.value.copy(isParallelChapterLoading = false)
                }
            }
            launchParallelCatalogLoadIfNeeded(parallelVersion)
        }

        if (persistProgress) {
            persistReadingProgressAsync(bookName, chapter, safeTargetVerse, version)
        }
    }

    fun loadParallelChapter(bookName: String, chapter: Int, version: String = _uiState.value.parallelVersion) {
        parallelChapterJob?.cancel()
        parallelCrossRefsJob?.cancel()

        val prev = _uiState.value
        val sameVersion = prev.parallelVersion.equals(version, ignoreCase = true)
        val immediateCatalog = if (sameVersion && prev.parallelVersionCatalog != null) {
            prev.parallelVersionCatalog
        } else {
            repository.getCachedModuleCatalogInMemory(version)
        }

        // Limpieza atómica para evitar mostrar texto obsoleto bajo la nueva versión paralela
        _uiState.value = prev.copy(
            parallelVersion = version,
            parallelVerses = emptyList(),
            parallelVerseTokens = emptyMap(),
            parallelChapterSubheadings = emptyMap(),
            parallelVersesWithRvrNotes = emptySet(),
            parallelVersionCatalog = immediateCatalog,
            isParallelChapterLoading = true
        )

        parallelChapterJob = viewModelScope.launch {
            try {
                val (cleanedList, parallelRvrNotes, parallelSubheadings) = withContext(Dispatchers.IO) {
                    val notesSet = if (BibleStudyTextUtils.isRvr1960Version(version)) {
                        repository.getVersesWithRealVersionNotes(version, bookName, chapter)
                    } else {
                        emptySet()
                    }
                    val verses = repository.getChapterVersesOnce(
                        version = version,
                        bookName = bookName,
                        chapter = chapter,
                        rvrNotesVerses = notesSet
                    )
                    val headings = if (verses.isNotEmpty()) {
                        repository.getChapterSubheadings(
                            version = version,
                            bookName = bookName,
                            chapter = chapter,
                            chapterVerseNumbers = verses.map { it.verse }
                        )
                    } else {
                        emptyMap()
                    }
                    Triple(verses, notesSet, headings)
                }

                val parallelTokens = withContext(Dispatchers.Default) {
                    precomputeVerseTokens(cleanedList, version, parallelRvrNotes)
                }

                _uiState.value = _uiState.value.copy(
                    parallelVerses = cleanedList,
                    parallelVerseTokens = parallelTokens,
                    parallelVersion = version,
                    parallelChapterSubheadings = parallelSubheadings,
                    parallelVersesWithRvrNotes = parallelRvrNotes,
                    isParallelChapterLoading = false
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("BIBLE_DEBUG", "Error cargando panel paralelo $bookName $chapter ($version): ${e.message}", e)
                _uiState.value = _uiState.value.copy(isParallelChapterLoading = false)
            }
        }

        launchParallelCatalogLoadIfNeeded(version)

        parallelCrossRefsJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val refsSet = assetHelper.getVersesWithCrossReferences(bookName, chapter)
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(parallelVersesWithCrossReferences = refsSet)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {}
        }
    }

    fun toggleParallelLinked() {
        val newLinked = !_uiState.value.isParallelLinked
        _uiState.value = _uiState.value.copy(isParallelLinked = newLinked)
        if (newLinked && _uiState.value.isParallelMode) {
            loadParallelChapter(
                bookName = _uiState.value.currentBook,
                chapter = _uiState.value.currentChapter,
                version = _uiState.value.parallelVersion
            )
        }
    }

    fun navigateToPreviousChapter() {
        val state = _uiState.value
        val existingCatalog = state.activeVersionCatalog?.takeIf {
            it.version.equals(state.activeVersion, ignoreCase = true)
        }
        if (existingCatalog != null) {
            val prev = existingCatalog.getPreviousPassage(state.currentBook, state.currentChapter)
            if (prev != null) {
                loadChapter(prev.first.name, prev.second)
            }
            return
        }
        viewModelScope.launch {
            val catalog = repository.getModuleCatalog(state.activeVersion)
            if (_uiState.value.activeVersion.equals(state.activeVersion, ignoreCase = true)) {
                _uiState.value = _uiState.value.copy(activeVersionCatalog = catalog)
                val prev = catalog.getPreviousPassage(_uiState.value.currentBook, _uiState.value.currentChapter)
                if (prev != null) {
                    loadChapter(prev.first.name, prev.second)
                }
            }
        }
    }

    fun navigateToNextChapter() {
        val state = _uiState.value
        val existingCatalog = state.activeVersionCatalog?.takeIf {
            it.version.equals(state.activeVersion, ignoreCase = true)
        }
        if (existingCatalog != null) {
            val next = existingCatalog.getNextPassage(state.currentBook, state.currentChapter)
            if (next != null) {
                loadChapter(next.first.name, next.second)
            }
            return
        }
        viewModelScope.launch {
            val catalog = repository.getModuleCatalog(state.activeVersion)
            if (_uiState.value.activeVersion.equals(state.activeVersion, ignoreCase = true)) {
                _uiState.value = _uiState.value.copy(activeVersionCatalog = catalog)
                val next = catalog.getNextPassage(_uiState.value.currentBook, _uiState.value.currentChapter)
                if (next != null) {
                    loadChapter(next.first.name, next.second)
                }
            }
        }
    }

    suspend fun getModuleCatalogForVersion(version: String): ModuleBibleCatalog {
        return repository.getModuleCatalog(version)
    }

    suspend fun getAvailableVersesForVersion(version: String, bookName: String, chapter: Int): List<Int> {
        return repository.getAvailableVerseNumbers(version, bookName, chapter)
    }

    fun triggerNavigationHighlight(
        verse: Int,
        durationMs: Long = BibleStudyTextUtils.TEMP_HIGHLIGHT_DURATION_MS
    ) {
        navigationHighlightJob?.cancel()
        val nextToken = _uiState.value.highlightToken + 1L
        _uiState.value = _uiState.value.copy(
            currentVerse = verse,
            highlightedVerse = verse,
            highlightToken = nextToken,
            isNavigationHighlightActive = true
        )
        if (durationMs > 0L) {
            navigationHighlightJob = viewModelScope.launch {
                delay(durationMs)
                if (_uiState.value.highlightToken == nextToken && _uiState.value.isNavigationHighlightActive) {
                    clearNavigationHighlight()
                }
            }
        }
    }

    fun clearNavigationHighlight() {
        navigationHighlightJob?.cancel()
        navigationHighlightJob = null
        val state = _uiState.value
        if (state.isNavigationHighlightActive || (!state.isTtsPlaying && state.highlightedVerse != null)) {
            _uiState.value = state.copy(
                highlightedVerse = if (state.isTtsPlaying) state.highlightedVerse else null,
                isNavigationHighlightActive = false
            )
        }
    }

    fun navigateToPassage(bookName: String, chapter: Int, verse: Int = 1) {
        _uiState.value = _uiState.value.copy(
            currentDestination = AppDestination.BIBLE
        )
        triggerNavigationHighlight(verse)
        loadChapter(bookName, chapter, preserveHighlight = true, targetVerse = verse, persistProgress = true)
    }

    fun setActiveVersion(version: String) {
        val state = _uiState.value
        val changed = !state.activeVersion.equals(version, ignoreCase = true)
        _uiState.value = state.copy(searchVersion = version)
        loadChapter(
            bookName = state.currentBook,
            chapter = state.currentChapter,
            version = version,
            forceClearMain = changed,
            targetVerse = state.currentVerse.coerceAtLeast(1),
            persistProgress = state.hasStartedReading
        )
        configureNaturalTtsVoice()
    }

    fun setParallelVersion(version: String) {
        val state = _uiState.value
        if (state.isParallelMode) {
            loadParallelChapter(
                bookName = state.currentBook,
                chapter = state.currentChapter,
                version = version
            )
        } else {
            _uiState.value = state.copy(
                parallelVersion = version,
                parallelVerses = emptyList(),
                parallelVerseTokens = emptyMap(),
                parallelChapterSubheadings = emptyMap(),
                parallelVersesWithRvrNotes = emptySet()
            )
        }
    }

    fun toggleParallelMode() {
        val newMode = !_uiState.value.isParallelMode
        _uiState.value = _uiState.value.copy(isParallelMode = newMode)
        if (newMode) {
            val state = _uiState.value
            loadParallelChapter(
                bookName = state.currentBook,
                chapter = state.currentChapter,
                version = state.parallelVersion
            )
        }
    }

    // --- Selector de Pasajes (100 % dinámico según catálogo SQLite del módulo activo) ---
    fun openPassagePicker() {
        val state = _uiState.value
        val existingCatalog = state.activeVersionCatalog?.takeIf {
            it.version.equals(state.activeVersion, ignoreCase = true)
        }
        val initialBook = existingCatalog?.findAvailableBook(state.currentBook)
            ?: existingCatalog?.availableBooks?.firstOrNull()
            ?: BibleBooksData.getBookByName(state.currentBook)
            ?: BibleBooksData.books.first()
        val initialChapters = existingCatalog?.getAvailableChapters(initialBook).orEmpty()
        val initialChapter = if (state.currentChapter in initialChapters) {
            state.currentChapter
        } else {
            initialChapters.firstOrNull() ?: state.currentChapter
        }

        _uiState.value = state.copy(
            isPassagePickerVisible = true,
            pickerStep = 1,
            pickerSelectedBook = initialBook,
            pickerSelectedChapter = initialChapter,
            pickerAvailableVerses = emptyList()
        )

        viewModelScope.launch {
            val loadedCatalog = repository.getModuleCatalog(_uiState.value.activeVersion)
            val resolvedBook = loadedCatalog.findAvailableBook(_uiState.value.currentBook)
                ?: loadedCatalog.availableBooks.firstOrNull()
                ?: _uiState.value.pickerSelectedBook
            val chapters = loadedCatalog.getAvailableChapters(resolvedBook)
            val resolvedChapter = if (_uiState.value.currentChapter in chapters) {
                _uiState.value.currentChapter
            } else {
                chapters.firstOrNull() ?: 1
            }
            _uiState.value = _uiState.value.copy(
                activeVersionCatalog = loadedCatalog,
                pickerSelectedBook = resolvedBook,
                pickerSelectedChapter = resolvedChapter
            )
        }
    }

    fun dismissPassagePicker() {
        _uiState.value = _uiState.value.copy(isPassagePickerVisible = false)
    }

    fun onPickerBookSelected(book: BookInfo) {
        val chapters = _uiState.value.activeVersionCatalog?.getAvailableChapters(book).orEmpty()
        val defaultChapter = chapters.firstOrNull() ?: 1
        _uiState.value = _uiState.value.copy(
            pickerSelectedBook = book,
            pickerSelectedChapter = defaultChapter,
            pickerStep = 2,
            pickerAvailableVerses = emptyList()
        )
    }

    fun onPickerChapterSelected(chapter: Int) {
        val selectedBook = _uiState.value.pickerSelectedBook
        val version = _uiState.value.activeVersion
        _uiState.value = _uiState.value.copy(
            pickerSelectedChapter = chapter,
            pickerStep = 3,
            pickerAvailableVerses = emptyList()
        )
        viewModelScope.launch {
            val verses = repository.getAvailableVerseNumbers(version, selectedBook.name, chapter)
            if (_uiState.value.pickerSelectedBook.number == selectedBook.number &&
                _uiState.value.pickerSelectedChapter == chapter
            ) {
                _uiState.value = _uiState.value.copy(pickerAvailableVerses = verses)
            }
        }
    }

    fun onPickerVerseSelected(verse: Int) {
        val book = _uiState.value.pickerSelectedBook
        val chapter = _uiState.value.pickerSelectedChapter
        _uiState.value = _uiState.value.copy(
            isPassagePickerVisible = false
        )
        triggerNavigationHighlight(verse)
        loadChapter(book.name, chapter, preserveHighlight = true, targetVerse = verse, persistProgress = true)
    }

    fun onPickerBackStep() {
        val currentStep = _uiState.value.pickerStep
        if (currentStep > 1) {
            _uiState.value = _uiState.value.copy(pickerStep = currentStep - 1)
        } else {
            dismissPassagePicker()
        }
    }

    // --- Comentarios y Notas de Versión ---
    fun openCommentary(verse: BibleVerse, preferredCode: String? = null) {
        val cleanVerseText = DailyVerseCatalog.cleanBibleText(verse.text)
        val cleanVerse = verse.copy(text = cleanVerseText)

        viewModelScope.launch {
            val modules = repository.getAllModules().first()
            val generalCommentaries = modules
                .filter { it.category == "COMMENTARY" && it.isDownloaded && !it.fileName.isNullOrBlank() && !BibleRepository.isVersionSpecificCommentary(it.fileName) }

            val isEn = _uiState.value.userPreferences.interfaceLanguage.contains("English", ignoreCase = true)
            if (generalCommentaries.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    toastMessage = if (isEn) {
                        "No commentaries downloaded. Go to the Modules tab to download biblical commentaries."
                    } else {
                        "No hay comentarios descargados. Ve a la sección Módulos para descargar comentarios bíblicos."
                    }
                )
                return@launch
            }

            val list = mutableListOf<Commentary>()
            withContext(Dispatchers.IO) {
                for (mod in generalCommentaries) {
                    val fn = mod.fileName ?: continue
                    val commentText = assetHelper.getCommentaryText(fn, cleanVerse.bookName, cleanVerse.chapter, cleanVerse.verse)
                    if (!commentText.isNullOrBlank()) {
                        val clean = commentText.replace(Regex("<.*?>"), "").replace(Regex("\\s+"), " ").trim()
                        list.add(
                            Commentary(
                                bookName = cleanVerse.bookName,
                                chapter = cleanVerse.chapter,
                                verse = cleanVerse.verse,
                                author = mod.author.ifBlank { mod.title },
                                title = mod.title,
                                content = clean
                            )
                        )
                    }
                }

                if (list.isEmpty()) {
                    val fallbackText = assetHelper.getCommentaryText("Wesley-c.commentaries.SQLite3", cleanVerse.bookName, cleanVerse.chapter, cleanVerse.verse)
                    if (!fallbackText.isNullOrBlank()) {
                        val clean = fallbackText.replace(Regex("<.*?>"), "").replace(Regex("\\s+"), " ").trim()
                        list.add(
                            Commentary(
                                bookName = cleanVerse.bookName,
                                chapter = cleanVerse.chapter,
                                verse = cleanVerse.verse,
                                author = "John Wesley",
                                title = "Notas de John Wesley",
                                content = clean
                            )
                        )
                    }
                }
            }

            _uiState.value = _uiState.value.copy(
                selectedCommentaryVerse = cleanVerse,
                currentVerseCommentaries = list,
                lastConsultedCommentary = list.firstOrNull() ?: _uiState.value.lastConsultedCommentary
            )
        }
    }

    fun dismissMissingNoteModulePrompt() {
        _uiState.value = _uiState.value.copy(missingNoteModulePrompt = null)
    }

    fun openAndDownloadMissingNoteModule() {
        val prompt = _uiState.value.missingNoteModulePrompt ?: return
        val isEn = _uiState.value.userPreferences.interfaceLanguage.contains("English", ignoreCase = true)
        val targetModule = _uiState.value.modules.firstOrNull {
            it.id == prompt.moduleId || it.versionTag.equals(prompt.moduleTag, ignoreCase = true)
        }

        _uiState.value = _uiState.value.copy(
            missingNoteModulePrompt = null,
            currentDestination = AppDestination.MODULES,
            moduleSelectedTab = 1, // Pestaña Comentarios
            moduleLanguageFilter = if (isEn) "All" else "Todos",
            isBottomBarVisible = true
        )

        if (targetModule != null && !targetModule.isDownloaded) {
            downloadModule(targetModule)
        }
    }

    fun onVersionNoteClick(verse: BibleVerse, marker: String) {
        val versionDbName = BibleRepository.resolveVersionCommentaryFileName(verse.version) ?: return
        val isRvrAsterisk = BibleStudyTextUtils.isRvrAsteriskMarker(marker) ||
            BibleStudyTextUtils.isRvr1960Version(verse.version)
        val isEn = _uiState.value.userPreferences.interfaceLanguage.contains("English", ignoreCase = true)

        viewModelScope.launch(Dispatchers.IO) {
            val isDownloaded = repository.isModuleDownloaded(versionDbName)
            if (!isDownloaded) {
                val catalogMod = _uiState.value.modules.firstOrNull {
                    it.fileName.equals(versionDbName, ignoreCase = true)
                }
                withContext(Dispatchers.Main) {
                    if (isRvrAsterisk) {
                        _uiState.value = _uiState.value.copy(
                            missingNoteModulePrompt = MissingNoteModulePrompt(
                                moduleId = catalogMod?.id ?: "mod_rv1960_c",
                                moduleTag = catalogMod?.versionTag ?: "RVR-C",
                                moduleTitle = catalogMod?.title ?: "Notas Reina-Valera 1960",
                                version = verse.version,
                                verseReference = "${verse.bookName} ${verse.chapter}:${verse.verse}"
                            )
                        )
                    } else {
                        Toast.makeText(
                            getApplication(),
                            if (isEn) {
                                "Download the ${verse.version} notes module to view note [$marker]"
                            } else {
                                "Descarga el módulo de notas de ${verse.version} para consultar la nota [$marker]"
                            },
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                return@launch
            }

            val lookup = repository.getVersionNotes(
                versionDbName = versionDbName,
                bookName = verse.bookName,
                chapter = verse.chapter,
                verse = verse.verse,
                marker = marker
            )

            withContext(Dispatchers.Main) {
                val formattedNotes = lookup.notes
                    .map { BibleStudyTextUtils.formatCommentaryHtml(it) }
                    .filter { it.isNotBlank() }

                val cleanVerse = verse.copy(
                    text = BibleStudyTextUtils.sanitizeVerseTextForDisplay(verse.text, verse.version)
                )

                when {
                    formattedNotes.isNotEmpty() -> {
                        val combinedContent = if (formattedNotes.size == 1) {
                            formattedNotes.first()
                        } else {
                            formattedNotes.mapIndexed { idx, body ->
                                val heading = if (isEn) "Note ${idx + 1}" else "Nota ${idx + 1}"
                                "$heading\n$body"
                            }.joinToString("\n\n")
                        }
                        val noteAuthor = if (isRvrAsterisk) {
                            "Notas Reina-Valera 1960 (RVR-C)"
                        } else {
                            "Notas ${verse.version}"
                        }
                        val noteTitle = when {
                            isRvrAsterisk && formattedNotes.size > 1 ->
                                if (isEn) "Verse Notes (*)" else "Notas del versículo (*)"
                            isRvrAsterisk ->
                                if (isEn) "Verse Note (*)" else "Nota de versículo (*)"
                            else ->
                                if (isEn) "Note [$marker]" else "Nota [$marker]"
                        }
                        val noteCommentary = Commentary(
                            bookName = cleanVerse.bookName,
                            chapter = cleanVerse.chapter,
                            verse = cleanVerse.verse,
                            author = noteAuthor,
                            title = noteTitle,
                            content = combinedContent
                        )

                        _uiState.value = _uiState.value.copy(
                            selectedCommentaryVerse = cleanVerse,
                            currentVerseCommentaries = listOf(noteCommentary),
                            lastConsultedCommentary = noteCommentary
                        )
                    }

                    isRvrAsterisk && lookup.hasBookIntroOnlySchema -> {
                        val infoContent = if (isEn) {
                            "The installed module \"Notas Reina-Valera 1960 (RVR-C)\" does not include a verse-level footnote for ${cleanVerse.bookName} ${cleanVerse.chapter}:${cleanVerse.verse} (this module edition currently contains general book introductions indexed at 1:1)."
                        } else {
                            "El módulo instalado «Notas Reina-Valera 1960 (RVR-C)» no incluye una nota específica al pie para ${cleanVerse.bookName} ${cleanVerse.chapter}:${cleanVerse.verse} (esta edición del módulo contiene únicamente introducciones generales por libro en 1:1)."
                        }
                        val infoCommentary = Commentary(
                            bookName = cleanVerse.bookName,
                            chapter = cleanVerse.chapter,
                            verse = cleanVerse.verse,
                            author = "Notas Reina-Valera 1960 (RVR-C)",
                            title = if (isEn) "Verse Note (*)" else "Nota de versículo (*)",
                            content = infoContent
                        )
                        _uiState.value = _uiState.value.copy(
                            selectedCommentaryVerse = cleanVerse,
                            currentVerseCommentaries = listOf(infoCommentary)
                        )
                    }

                    else -> {
                        val msg = if (isRvrAsterisk) {
                            if (isEn) {
                                "No RVR-C note found for ${cleanVerse.bookName} ${cleanVerse.chapter}:${cleanVerse.verse}"
                            } else {
                                "No se encontró nota RVR-C para ${cleanVerse.bookName} ${cleanVerse.chapter}:${cleanVerse.verse}"
                            }
                        } else {
                            if (isEn) {
                                "No content found for note [$marker]"
                            } else {
                                "No se encontró contenido para la nota [$marker]"
                            }
                        }
                        Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    fun refreshCommentaryPills() {
        val state = _uiState.value
        val bookName = state.currentBook
        val chapter = state.currentChapter
        if (bookName.isBlank() || chapter <= 0) return

        val generalCommentaries = state.modules
            .filter {
                it.category == "COMMENTARY" &&
                    it.isDownloaded &&
                    !it.fileName.isNullOrBlank() &&
                    !BibleRepository.isVersionSpecificCommentary(it.fileName)
            }
            .mapNotNull { it.fileName }
        if (generalCommentaries.isEmpty()) return

        commentaryPillsJob?.cancel()
        commentaryPillsJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val resultSet = mutableSetOf<Int>()
                for (dbName in generalCommentaries) {
                    val file = BibleAssetDatabaseHelper.ensureDatabaseExtracted(getApplication(), dbName)
                    if (file.exists() && file.length() > 0L) {
                        resultSet.addAll(assetHelper.getVersesWithComments(dbName, bookName, chapter))
                    }
                }

                withContext(Dispatchers.Main) {
                    val current = _uiState.value
                    if (current.currentBook.equals(bookName, ignoreCase = true) && current.currentChapter == chapter) {
                        _uiState.value = current.copy(
                            versesWithComments = resultSet,
                            parallelVersesWithComments = if (!current.isParallelMode || current.isParallelLinked) {
                                resultSet
                            } else {
                                current.parallelVersesWithComments
                            }
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {}
        }
    }

    fun dismissCommentary() {
        _uiState.value = _uiState.value.copy(selectedCommentaryVerse = null, currentVerseCommentaries = emptyList())
    }

    // --- Referencias Cruzadas (TSK Real) ---
    fun openCrossReferences(verse: BibleVerse) {
        val cleanVerseText = DailyVerseCatalog.cleanBibleText(verse.text)
        val cleanVerse = verse.copy(text = cleanVerseText)

        viewModelScope.launch {
            val bibleDb = if (_uiState.value.activeVersion.contains("KJV", ignoreCase = true)) "KJV.SQLite3" else "RVR1960.SQLite3"
            val realRefs = withContext(Dispatchers.IO) {
                assetHelper.getCrossReferences(
                    bookNameOrAbbr = cleanVerse.bookName,
                    chapter = cleanVerse.chapter,
                    verse = cleanVerse.verse,
                    bibleDbName = bibleDb,
                    limit = 35
                )
            }

            _uiState.value = _uiState.value.copy(
                selectedCrossReferenceVerse = cleanVerse,
                currentVerseCrossReferences = realRefs
            )
        }
    }

    fun dismissCrossReferences() {
        _uiState.value = _uiState.value.copy(selectedCrossReferenceVerse = null, currentVerseCrossReferences = emptyList())
    }

    // --- Diccionario ---
    fun lookupWord(rawWord: String, verse: BibleVerse? = null) {
        val cleanWord = rawWord.replace(Regex("[^a-zA-ZáéíóúÁÉÍÓÚñÑüÜ]"), "").trim()
        if (cleanWord.isBlank()) return

        val searchLower = cleanWord.lowercase()
        val displayWord = cleanWord.replaceFirstChar { it.uppercase() }

        _uiState.value = _uiState.value.copy(
            selectedDictionaryWord = displayWord,
            isWordLookupLoading = true,
            dictionaryMatches = emptyList(),
            currentDictionaryEntry = null
        )

        viewModelScope.launch {
            repository.getDictionaryMatchesForWord(
                term = searchLower,
                bookName = verse?.bookName,
                chapter = verse?.chapter,
                verse = verse?.verse,
                version = verse?.version
            ).collect { matches ->
                _uiState.value = _uiState.value.copy(
                    dictionaryMatches = matches,
                    currentDictionaryEntry = matches.firstOrNull()?.entry,
                    lastConsultedDictionary = matches.firstOrNull()?.entry ?: _uiState.value.lastConsultedDictionary,
                    isWordLookupLoading = false
                )
            }
        }
    }

    fun dismissDictionary() {
        _uiState.value = _uiState.value.copy(
            selectedDictionaryWord = null,
            currentDictionaryEntry = null,
            dictionaryMatches = emptyList(),
            isWordLookupLoading = false
        )
    }

    // --- Text-To-Speech ---
    fun toggleTtsBar() {
        val willShow = !_uiState.value.isTtsVisible
        if (willShow && _uiState.value.verses.isEmpty()) {
            val isEn = _uiState.value.userPreferences.interfaceLanguage.contains("English", ignoreCase = true)
            _uiState.value = _uiState.value.copy(
                isTtsVisible = false,
                isTtsPlaying = false,
                toastMessage = if (isEn) {
                    "This passage is not available for audio reading"
                } else {
                    "Este pasaje no está disponible para lectura de voz"
                }
            )
            return
        }
        _uiState.value = _uiState.value.copy(isTtsVisible = willShow)
        if (!willShow) {
            stopTts()
        }
    }

    fun playTts() {
        val verses = _uiState.value.verses
        if (verses.isEmpty()) {
            _uiState.value = _uiState.value.copy(isTtsPlaying = false)
            return
        }

        val index = _uiState.value.ttsVerseIndex.coerceIn(0, verses.lastIndex)
        val verse = verses[index]

        val cleanVerseText = BibleStudyTextUtils.sanitizeVerseTextForSpeech(verse.text, verse.version)
        val cleanSectionTitle = verse.sectionTitle?.let { DailyVerseCatalog.cleanBibleText(it) }?.trim() ?: ""

        configureNaturalTtsVoice(cleanVerseText)
        navigationHighlightJob?.cancel()
        navigationHighlightJob = null

        _uiState.value = _uiState.value.copy(
            isTtsPlaying = true,
            isTtsVisible = true,
            highlightedVerse = verse.verse,
            isNavigationHighlightActive = false
        )

        val isEnglish = _uiState.value.activeVersion.contains("KJV", ignoreCase = true) ||
                _uiState.value.userPreferences.interfaceLanguage.contains("English", ignoreCase = true)

        val sb = StringBuilder()
        if (!hasSpokenChapterIntro) {
            val intro = if (isEnglish) "${verse.bookName}, chapter ${verse.chapter}. " else "${verse.bookName}, capítulo ${verse.chapter}. "
            sb.append(intro)
            hasSpokenChapterIntro = true
        }

        if (cleanSectionTitle.isNotBlank()) {
            sb.append("$cleanSectionTitle. ")
        }
        sb.append(cleanVerseText)

        val textToSpeak = sb.toString().trim()
        val utteranceId = "verse_${verse.verse}"
        val params = android.os.Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }
        tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    fun pauseTts() {
        tts?.stop()
        _uiState.value = _uiState.value.copy(isTtsPlaying = false)
    }

    fun stopTts() {
        tts?.stop()
        hasSpokenChapterIntro = false
        val state = _uiState.value
        _uiState.value = state.copy(
            isTtsPlaying = false,
            isTtsVisible = false,
            highlightedVerse = if (state.isNavigationHighlightActive) state.highlightedVerse else null
        )
    }

    fun nextTtsVerse() {
        val verses = _uiState.value.verses
        if (verses.isEmpty()) return
        val nextIdx = (_uiState.value.ttsVerseIndex + 1).coerceAtMost(verses.lastIndex)
        navigationHighlightJob?.cancel()
        navigationHighlightJob = null
        _uiState.value = _uiState.value.copy(
            ttsVerseIndex = nextIdx,
            highlightedVerse = verses[nextIdx].verse,
            isNavigationHighlightActive = false
        )
        if (_uiState.value.isTtsPlaying) {
            playTts()
        }
    }

    fun prevTtsVerse() {
        val verses = _uiState.value.verses
        if (verses.isEmpty()) return
        val prevIdx = (_uiState.value.ttsVerseIndex - 1).coerceAtLeast(0)
        navigationHighlightJob?.cancel()
        navigationHighlightJob = null
        _uiState.value = _uiState.value.copy(
            ttsVerseIndex = prevIdx,
            highlightedVerse = verses[prevIdx].verse,
            isNavigationHighlightActive = false
        )
        if (_uiState.value.isTtsPlaying) {
            playTts()
        }
    }

    // --- Favoritos ---
    fun isFavorite(verse: BibleVerse, keys: Set<String> = _uiState.value.favoriteKeys): Boolean {
        return repository.buildFavoriteKey(verse) in keys
    }

    fun toggleFavorite(verse: BibleVerse) {
        viewModelScope.launch {
            val (addedFavorite, removedKey) = favoriteMutex.withLock {
                withContext(Dispatchers.IO) {
                    repository.toggleFavoriteReturningChange(verse)
                }
            }
            val nowAdded = addedFavorite != null

            val uid = _uiState.value.googleUid
            if (uid != null) {
                viewModelScope.launch(Dispatchers.IO) {
                    if (addedFavorite != null) {
                        cloudSyncRepository.pushFavorite(uid, addedFavorite, repository.buildFavoriteKey(addedFavorite))
                    } else if (removedKey != null) {
                        cloudSyncRepository.deleteFavorite(uid, removedKey)
                    }
                }
            }

            val lang = _uiState.value.userPreferences.interfaceLanguage
            val isEn = lang.contains("English", ignoreCase = true)
            _uiState.value = _uiState.value.copy(
                toastMessage = if (nowAdded) {
                    if (isEn) "Saved to favorites" else "Guardado en favoritos"
                } else {
                    if (isEn) "Removed from favorites" else "Eliminado de favoritos"
                }
            )
        }
    }

    fun addFavorites(verses: List<BibleVerse>) {
        if (verses.isEmpty()) return
        viewModelScope.launch {
            val inserted = favoriteMutex.withLock {
                withContext(Dispatchers.IO) {
                    repository.addFavoritesReturningInserted(verses)
                }
            }
            val addedCount = inserted.size
            if (addedCount > 0) {
                val uid = _uiState.value.googleUid
                if (uid != null) {
                    viewModelScope.launch(Dispatchers.IO) {
                        val payload = inserted.map { fav -> repository.buildFavoriteKey(fav) to fav }
                        cloudSyncRepository.pushFavorites(uid, payload)
                    }
                }

                val lang = _uiState.value.userPreferences.interfaceLanguage
                val isEn = lang.contains("English", ignoreCase = true)
                _uiState.value = _uiState.value.copy(
                    toastMessage = if (isEn) {
                        "Saved to favorites"
                    } else if (addedCount > 1) {
                        "Guardados en favoritos"
                    } else {
                        "Guardado en favoritos"
                    }
                )
            }
        }
    }

    fun removeFavorites(verses: List<BibleVerse>) {
        if (verses.isEmpty()) return
        viewModelScope.launch {
            val removedKeys = favoriteMutex.withLock {
                withContext(Dispatchers.IO) {
                    repository.removeFavoritesReturningKeys(verses)
                }
            }
            val removedCount = removedKeys.size
            if (removedCount > 0) {
                val uid = _uiState.value.googleUid
                if (uid != null) {
                    viewModelScope.launch(Dispatchers.IO) {
                        cloudSyncRepository.deleteFavorites(uid, removedKeys)
                    }
                }

                val lang = _uiState.value.userPreferences.interfaceLanguage
                val isEn = lang.contains("English", ignoreCase = true)
                _uiState.value = _uiState.value.copy(
                    toastMessage = if (isEn) {
                        "Removed from favorites"
                    } else if (removedCount > 1) {
                        "Eliminados de favoritos"
                    } else {
                        "Eliminado de favoritos"
                    }
                )
            }
        }
    }

    fun removeFavorite(fav: FavoriteVerse) {
        viewModelScope.launch {
            val favKey = repository.buildFavoriteKey(fav)
            favoriteMutex.withLock {
                withContext(Dispatchers.IO) {
                    repository.removeFavorite(fav)
                }
            }
            val uid = _uiState.value.googleUid
            if (uid != null) {
                viewModelScope.launch(Dispatchers.IO) {
                    cloudSyncRepository.deleteFavorite(uid, favKey)
                }
            }

            val isEn = _uiState.value.userPreferences.interfaceLanguage.contains("English", ignoreCase = true)
            _uiState.value = _uiState.value.copy(
                toastMessage = if (isEn) "Removed from favorites" else "Eliminado de favoritos"
            )
        }
    }

    fun showFavoritesDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showFavoritesDialog = show)
    }

    // --- Búsqueda Optimizada ---
    private var searchJob: Job? = null
    private var searchCountJob: Job? = null

    @OptIn(FlowPreview::class)
    private fun observeSearchQuery() {
        viewModelScope.launch {
            _searchQueryInput
                .debounce(300L)
                .distinctUntilChanged()
                .collect { query ->
                    val clean = query.trim()
                    if (clean.length < 2) {
                        searchJob?.cancel()
                        searchCountJob?.cancel()
                        _uiState.value = _uiState.value.copy(
                            searchResults = emptyList(),
                            searchVerseResults = emptyList(),
                            searchCommentaryResults = emptyList(),
                            searchDictionaryResults = emptyList(),
                            isSearching = false,
                            searchCountAll = 0,
                            searchCountOt = 0,
                            searchCountNt = 0
                        )
                    } else {
                        performSearch()
                        updateSearchCounts()
                    }
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        _searchQueryInput.value = query
    }

    fun setSearchVersion(version: String) {
        if (_uiState.value.searchVersion != version) {
            _uiState.value = _uiState.value.copy(searchVersion = version)
            performSearch()
            updateSearchCounts()
        }
    }

    fun setSearchScope(scope: String) {
        if (_uiState.value.searchScope != scope) {
            _uiState.value = _uiState.value.copy(searchScope = scope)
            performSearch()
        }
    }

    fun toggleSearchExactWord() {
        val newExact = !_uiState.value.searchExactWord
        _uiState.value = _uiState.value.copy(searchExactWord = newExact)
        performSearch()
        updateSearchCounts()
    }

    fun performSearch() {
        val query = _uiState.value.searchQuery.trim()
        val scope = _uiState.value.searchScope
        val version = _uiState.value.searchVersion
        val exactWord = _uiState.value.searchExactWord

        searchJob?.cancel()

        if (query.length < 2) {
            _uiState.value = _uiState.value.copy(
                searchResults = emptyList(),
                searchVerseResults = emptyList(),
                isSearching = false
            )
            return
        }

        searchJob = viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(isSearching = true)
            try {
                // 1. Búsqueda nativa SQLite en versículos (LIMIT 100)
                val versesDeferred = async {
                    try {
                        repository.searchVerses(
                            query = query,
                            scope = scope,
                            version = version,
                            exactWord = exactWord
                        ).first()
                    } catch (e: Exception) {
                        if (e !is kotlinx.coroutines.CancellationException) {
                            Log.e("BIBLE_DEBUG", "Error en searchVerses", e)
                        }
                        emptyList()
                    }
                }

                // 2. Búsqueda en comentarios
                val commDeferred = async {
                    try {
                        repository.searchCommentaries(query).first()
                    } catch (_: Exception) {
                        emptyList()
                    }
                }

                // 3. Búsqueda en diccionario
                val dictDeferred = async {
                    try {
                        repository.searchDictionary(query).first()
                    } catch (_: Exception) {
                        emptyList()
                    }
                }

                val verses = versesDeferred.await()
                val comms = commDeferred.await()
                val dicts = dictDeferred.await()

                _uiState.value = _uiState.value.copy(
                    searchResults = verses,
                    searchVerseResults = verses,
                    searchCommentaryResults = comms,
                    searchDictionaryResults = dicts
                )
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    Log.e("BIBLE_DEBUG", "Error en performSearch", e)
                }
            } finally {
                // Blindaje absoluto: restablecer isSearching = false siempre
                _uiState.value = _uiState.value.copy(isSearching = false)
            }
        }
    }

    private fun updateSearchCounts() {
        val query = _uiState.value.searchQuery.trim()
        val version = _uiState.value.searchVersion
        val exactWord = _uiState.value.searchExactWord

        searchCountJob?.cancel()
        if (query.length < 2) return

        searchCountJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val countOtDeferred = async { repository.getSearchCount(query, "AT", version, exactWord).first() }
                val countNtDeferred = async { repository.getSearchCount(query, "NT", version, exactWord).first() }

                val countOt = countOtDeferred.await()
                val countNt = countNtDeferred.await()
                val countAll = countOt + countNt

                _uiState.value = _uiState.value.copy(
                    searchCountAll = countAll,
                    searchCountOt = countOt,
                    searchCountNt = countNt
                )
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    Log.e("BIBLE_DEBUG", "Error calculando conteos de búsqueda para $version", e)
                }
            }
        }
    }

    fun submitSearch(query: String) {
        val clean = query.trim()
        if (clean.isNotBlank()) {
            viewModelScope.launch {
                repository.addSearchHistory(clean)
            }
            _uiState.value = _uiState.value.copy(searchQuery = clean)
            _searchQueryInput.value = clean
            performSearch()
            updateSearchCounts()
        }
    }

    fun setSearchFilter(filter: String) {
        _uiState.value = _uiState.value.copy(searchFilter = filter)
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            repository.clearSearchHistory()
            val isEn = _uiState.value.userPreferences.interfaceLanguage.contains("English", ignoreCase = true)
            _uiState.value = _uiState.value.copy(toastMessage = if (isEn) "Search history cleared" else "Historial borrado")
        }
    }

    fun deleteSearchHistoryItem(id: Long) {
        viewModelScope.launch {
            repository.deleteSearchHistoryItem(id)
        }
    }

    // --- Modules ---
    fun setModuleLanguageFilter(lang: String) {
        _uiState.value = _uiState.value.copy(moduleLanguageFilter = lang)
    }

    fun setModuleTab(tabIndex: Int) {
        _uiState.value = _uiState.value.copy(moduleSelectedTab = tabIndex)
    }

    fun toggleModuleDownload(module: BibleModule) {
        if (module.isDownloaded) {
            deleteModule(module)
        } else {
            downloadModule(module)
        }
    }

    private fun downloadModule(module: BibleModule) {
        val fileName = module.fileName ?: return
        val downloadUrl = module.downloadUrl ?: return

        // Evita lanzar descargas simultáneas del mismo recurso
        if (_uiState.value.downloadProgress.containsKey(module.id)) return

        // Inicia el progreso visual en 1% de inmediato
        _uiState.value = _uiState.value.copy(
            downloadProgress = _uiState.value.downloadProgress + (module.id to 0.01f)
        )

        viewModelScope.launch {
            val result = downloader.downloadFile(downloadUrl, fileName) { progress ->
                _uiState.value = _uiState.value.copy(
                    downloadProgress = _uiState.value.downloadProgress + (module.id to progress)
                )
            }

            val updatedProgress = _uiState.value.downloadProgress - module.id
            val isEn = _uiState.value.userPreferences.interfaceLanguage.contains("English", ignoreCase = true)

            if (result.isSuccess) {
                // Actualiza en Room a isDownloaded = true
                repository.toggleModuleDownload(module.id, false)
                repository.invalidateModuleCatalogCache()
                BibleAssetDatabaseHelper.invalidateVersionNotesCache(module.fileName)
                refreshCommentaryPills()
                val affectsActiveOrParallel = module.category == "VERSION" ||
                    module.fileName.equals("RV1960-c.commentaries.SQLite3", ignoreCase = true)
                if (affectsActiveOrParallel) {
                    loadChapter(_uiState.value.currentBook, _uiState.value.currentChapter, preserveHighlight = true)
                }
                _uiState.value = _uiState.value.copy(
                    downloadProgress = updatedProgress,
                    toastMessage = if (isEn) "Module downloaded successfully" else "Módulo descargado correctamente"
                )
            } else {
                val errorException = result.exceptionOrNull()
                val errorMsg = errorException?.message ?: "Error desconocido"

                // Registra el error detallado con traza en LogCat bajo BIBLE_DEBUG
                android.util.Log.e("BIBLE_DEBUG", "Fallo al descargar ${module.fileName}: $errorMsg", errorException)

                _uiState.value = _uiState.value.copy(
                    downloadProgress = updatedProgress,
                    toastMessage = if (isEn) "Download error: $errorMsg" else "Error al descargar: $errorMsg"
                )
            }
        }
    }

    private fun deleteModule(module: BibleModule) {
        viewModelScope.launch {
            // Elimina el archivo binario del almacenamiento interno
            module.fileName?.let { downloader.deleteDownloadedFile(it) }

            // Actualiza en Room a isDownloaded = false
            repository.toggleModuleDownload(module.id, true)
            repository.invalidateModuleCatalogCache()
            BibleAssetDatabaseHelper.invalidateVersionNotesCache(module.fileName)
            val affectsActiveOrParallel = module.category == "VERSION" ||
                module.fileName.equals("RV1960-c.commentaries.SQLite3", ignoreCase = true)
            if (affectsActiveOrParallel) {
                loadChapter(_uiState.value.currentBook, _uiState.value.currentChapter, preserveHighlight = true)
            }

            val isEn = _uiState.value.userPreferences.interfaceLanguage.contains("English", ignoreCase = true)
            _uiState.value = _uiState.value.copy(
                toastMessage = if (isEn) "Module uninstalled" else "Módulo desinstalado"
            )
        }
    }

    // --- Preferencias ---
    fun saveUserPreferences(preference: UserPreference = _uiState.value.userPreferences) {
        val normalizedFont = if (preference.fontFamily.equals("Google Sans", ignoreCase = true)) {
            "Google Sans Flex"
        } else {
            preference.fontFamily
        }
        val normalizedMode = if (preference.themeMode.equals("AUTOMATIC", ignoreCase = true) || preference.themeMode.isBlank()) {
            "AUTO"
        } else {
            preference.themeMode.trim().uppercase()
        }
        val normalized = preference.copy(
            fontFamily = normalizedFont,
            themeMode = normalizedMode,
            selectedLightTheme = preference.selectedLightTheme.trim().ifBlank { "CRISP_LIGHT" },
            selectedDarkTheme = preference.selectedDarkTheme.trim().ifBlank { "MIDNIGHT_BLUE" }
        )
        syncThemeCache(normalized)
        _uiState.value = _uiState.value.copy(userPreferences = normalized)
        viewModelScope.launch {
            repository.saveUserPreferences(normalized)
        }
    }

    fun updateThemeMode(mode: String) {
        val normalizedMode = if (mode.equals("AUTOMATIC", ignoreCase = true) || mode.isBlank()) {
            "AUTO"
        } else {
            mode.trim().uppercase()
        }
        val updated = _uiState.value.userPreferences.copy(themeMode = normalizedMode)
        syncThemeCache(updated)
        _uiState.value = _uiState.value.copy(userPreferences = updated)
        viewModelScope.launch { repository.saveUserPreferences(updated) }
    }

    fun updateSelectedLightTheme(themeKey: String, activateMode: Boolean = false) {
        val current = _uiState.value.userPreferences
        val updated = current.copy(
            themeMode = if (activateMode) "LIGHT" else current.themeMode,
            selectedLightTheme = themeKey.trim().ifBlank { "CRISP_LIGHT" }
        )
        syncThemeCache(updated)
        _uiState.value = _uiState.value.copy(userPreferences = updated)
        viewModelScope.launch { repository.saveUserPreferences(updated) }
    }

    fun updateSelectedDarkTheme(themeKey: String, activateMode: Boolean = false) {
        val current = _uiState.value.userPreferences
        val updated = current.copy(
            themeMode = if (activateMode) "DARK" else current.themeMode,
            selectedDarkTheme = themeKey.trim().ifBlank { "MIDNIGHT_BLUE" }
        )
        syncThemeCache(updated)
        _uiState.value = _uiState.value.copy(userPreferences = updated)
        viewModelScope.launch { repository.saveUserPreferences(updated) }
    }

    fun updateFontFamily(font: String) {
        val normalizedFont = if (font.equals("Google Sans", ignoreCase = true)) "Google Sans Flex" else font
        val updated = _uiState.value.userPreferences.copy(fontFamily = normalizedFont)
        _uiState.value = _uiState.value.copy(userPreferences = updated)
        viewModelScope.launch { repository.saveUserPreferences(updated) }
    }

    fun updateFontSizePt(sizePt: Float) {
        val clamped = sizePt.coerceIn(8f, 20f)
        val updated = _uiState.value.userPreferences.copy(fontSizePt = clamped)
        _uiState.value = _uiState.value.copy(userPreferences = updated)
        viewModelScope.launch { repository.saveUserPreferences(updated) }
    }

    fun updateInterfaceLanguage(lang: String) {
        val updated = _uiState.value.userPreferences.copy(interfaceLanguage = lang)
        _uiState.value = _uiState.value.copy(userPreferences = updated)
        viewModelScope.launch { repository.saveUserPreferences(updated) }
        configureNaturalTtsVoice()
    }

    fun showEditProfileDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showEditProfileDialog = show)
    }

    fun updateLocalProfileName(name: String) {
        val current = _uiState.value.userPreferences
        val updated = current.copy(userName = name.trim())
        val isEn = current.interfaceLanguage.contains("English", ignoreCase = true)
        _uiState.value = _uiState.value.copy(
            userPreferences = updated,
            showEditProfileDialog = false,
            toastMessage = if (isEn) "Profile updated" else "Perfil actualizado"
        )
        viewModelScope.launch { repository.saveUserPreferences(updated) }
    }

    fun signInWithGoogle(activityContext: Context) {
        if (_uiState.value.isGoogleAuthInProgress) return
        _uiState.value = _uiState.value.copy(isGoogleAuthInProgress = true)

        viewModelScope.launch {
            val signInResult = cloudSyncRepository.signInWithGoogle(activityContext)
            val isEn = _uiState.value.userPreferences.interfaceLanguage.contains("English", ignoreCase = true)

            signInResult.fold(
                onSuccess = { cloudUser ->
                    _uiState.value = _uiState.value.copy(
                        googleUid = cloudUser.uid,
                        googleDisplayName = cloudUser.displayName,
                        googleEmail = cloudUser.email,
                        isGoogleAuthInProgress = false
                    )
                    val mergeResult = syncWithCloudInternal(
                        uid = cloudUser.uid,
                        isUserInitiated = true,
                        showToast = false
                    )
                    val syncedOk = mergeResult.isSuccess
                    _uiState.value = _uiState.value.copy(
                        toastMessage = if (syncedOk) {
                            if (isEn) "Google account linked & backup synced" else "Cuenta de Google vinculada y respaldo sincronizado"
                        } else {
                            if (isEn) "Google account linked" else "Cuenta de Google vinculada"
                        }
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(isGoogleAuthInProgress = false)
                    if (error !is UserCancelledSignInException) {
                        val detail = error.localizedMessage?.takeIf { it.isNotBlank() }
                        val msg = if (isEn) {
                            if (detail != null) "Could not sign in with Google: $detail" else "Could not sign in with Google"
                        } else {
                            if (detail != null) "No se pudo iniciar sesión con Google: $detail" else "No se pudo iniciar sesión con Google"
                        }
                        _uiState.value = _uiState.value.copy(toastMessage = msg)
                    }
                }
            )
        }
    }

    fun syncNow() {
        val uid = _uiState.value.googleUid ?: return
        viewModelScope.launch {
            syncWithCloudInternal(uid = uid, isUserInitiated = true, showToast = true)
        }
    }

    fun unlinkGoogleAccount(context: Context) {
        val isEn = _uiState.value.userPreferences.interfaceLanguage.contains("English", ignoreCase = true)
        viewModelScope.launch {
            cloudSyncRepository.unlinkAccount(context)
            _uiState.value = _uiState.value.copy(
                googleUid = null,
                googleDisplayName = "",
                googleEmail = "",
                syncStatus = SyncStatus.IDLE,
                isGoogleAuthInProgress = false,
                toastMessage = if (isEn) {
                    "Account unlinked. Your local notes and favorites are preserved."
                } else {
                    "Cuenta desvinculada. Tus notas y favoritos locales se conservan."
                }
            )
        }
    }

    private suspend fun syncWithCloudInternal(
        uid: String,
        isUserInitiated: Boolean,
        showToast: Boolean
    ): Result<Unit> = syncMutex.withLock {
        val localFavorites = favoriteMutex.withLock {
            withContext(Dispatchers.IO) { repository.getAllFavoritesOnce() }
        }
        val localNotes = withContext(Dispatchers.IO) { repository.getAllNotesOnce() }

        val mergeResult = withContext(Dispatchers.IO) {
            cloudSyncRepository.syncAndMerge(
                uid = uid,
                localFavorites = localFavorites,
                localNotes = localNotes,
                isUserInitiated = isUserInitiated
            )
        }

        val isEn = _uiState.value.userPreferences.interfaceLanguage.contains("English", ignoreCase = true)

        return mergeResult.fold(
            onSuccess = { delta ->
                if (delta.favoritesToInsert.isNotEmpty()) {
                    favoriteMutex.withLock {
                        withContext(Dispatchers.IO) {
                            val currentKeys = repository.getAllFavoritesOnce()
                                .map { repository.buildFavoriteKey(it) }
                                .toSet()
                            val missing = delta.favoritesToInsert.filter { fav ->
                                repository.buildFavoriteKey(fav) !in currentKeys
                            }
                            if (missing.isNotEmpty()) {
                                repository.insertFavorites(missing)
                            }
                        }
                    }
                }
                if (delta.notesToUpsert.isNotEmpty()) {
                    withContext(Dispatchers.IO) {
                        val existingNotes = repository.getAllNotesOnce()
                        val existingCloudIds = existingNotes.map { it.cloudId }.toSet()
                        val toUpsert = delta.notesToUpsert.filter { note ->
                            note.id > 0L || note.cloudId !in existingCloudIds
                        }
                        if (toUpsert.isNotEmpty()) {
                            repository.insertNotes(toUpsert)
                        }
                    }
                }
                if (showToast) {
                    _uiState.value = _uiState.value.copy(
                        toastMessage = if (isEn) "Cloud backup synced" else "Respaldo sincronizado"
                    )
                }
                Result.success(Unit)
            },
            onFailure = { error ->
                if (showToast) {
                    _uiState.value = _uiState.value.copy(
                        toastMessage = if (isEn) {
                            "Could not sync right now. Check your connection."
                        } else {
                            "No se pudo sincronizar ahora. Verifica tu conexión."
                        }
                    )
                }
                Result.failure(error)
            }
        )
    }

    fun clearToast() {
        _uiState.value = _uiState.value.copy(toastMessage = null)
    }

    fun setBottomBarVisible(visible: Boolean) {
        if (_uiState.value.isBottomBarVisible != visible) {
            _uiState.value = _uiState.value.copy(isBottomBarVisible = visible)
        }
    }

    fun completeOnboarding() {
        val currentPref = _uiState.value.userPreferences
        val updated = currentPref.copy(isFirstLaunch = false)
        _uiState.value = _uiState.value.copy(userPreferences = updated)
        viewModelScope.launch { repository.saveUserPreferences(updated) }
    }

    // --- Notas ---
    fun openNotesSheet(note: UserNote? = null) {
        _uiState.value = _uiState.value.copy(isNotesSheetOpen = true, editingNote = note)
    }

    fun closeNotesSheet() {
        _uiState.value = _uiState.value.copy(isNotesSheetOpen = false, editingNote = null)
    }

    fun setEditingNote(note: UserNote?) {
        _uiState.value = _uiState.value.copy(editingNote = note)
    }

    fun saveNote(title: String, content: String, reference: String, noteId: Long = 0) {
        val isEn = _uiState.value.userPreferences.interfaceLanguage.contains("English", ignoreCase = true)
        val uid = _uiState.value.googleUid
        viewModelScope.launch {
            val savedNote = withContext(Dispatchers.IO) {
                val existingCloudId = if (noteId > 0L) {
                    repository.getNoteByIdOnce(noteId)?.cloudId?.takeIf { it.isNotBlank() }
                } else null
                val note = UserNote(
                    id = noteId,
                    title = title.trim().ifBlank { if (isEn) "Study Note" else "Nota de Estudio" },
                    content = content,
                    biblicalReference = reference.trim(),
                    timestamp = System.currentTimeMillis(),
                    cloudId = existingCloudId ?: UUID.randomUUID().toString()
                )
                val insertedId = repository.saveNote(note)
                note.copy(id = if (noteId > 0L) noteId else insertedId)
            }

            _uiState.value = _uiState.value.copy(
                editingNote = null,
                toastMessage = if (isEn) "Note saved successfully" else "Nota guardada correctamente"
            )

            if (uid != null) {
                viewModelScope.launch(Dispatchers.IO) {
                    cloudSyncRepository.pushNote(uid, savedNote)
                }
            }
        }
    }

    fun deleteNote(id: Long) {
        val isEn = _uiState.value.userPreferences.interfaceLanguage.contains("English", ignoreCase = true)
        val uid = _uiState.value.googleUid
        viewModelScope.launch {
            val existing = withContext(Dispatchers.IO) {
                val target = repository.getNoteByIdOnce(id)
                repository.deleteNote(id)
                target
            }

            _uiState.value = _uiState.value.copy(
                editingNote = null,
                toastMessage = if (isEn) "Note deleted" else "Nota eliminada"
            )

            if (uid != null && existing != null) {
                val cloudId = existing.cloudId.ifBlank { "legacy_${existing.id}_${existing.timestamp}" }
                viewModelScope.launch(Dispatchers.IO) {
                    cloudSyncRepository.deleteNote(uid, cloudId)
                }
            }
        }
    }

    fun getActiveVerseSnippet(): Pair<String, String> {
        val state = _uiState.value
        val targetVerseNum = state.highlightedVerse ?: state.currentVerse
        val verseObj = state.verses.find { it.verse == targetVerseNum } ?: state.verses.firstOrNull()
        val ref = if (verseObj != null) "${verseObj.bookName} ${verseObj.chapter}:${verseObj.verse}" else "${state.currentBook} ${state.currentChapter}:$targetVerseNum"
        val text = verseObj?.text ?: "Palabra de Dios para meditación y edificación espiritual."
        return Pair(ref, "«$text» ($ref)")
    }

    fun getLastCommentarySnippet(): String {
        val state = _uiState.value
        val comm = state.lastConsultedCommentary ?: state.currentVerseCommentaries.firstOrNull()
        return if (comm != null) {
            "[${comm.author}]\n${comm.content}"
        } else {
            "[John Wesley]\nEl texto bíblico revela la fidelidad, el llamamiento y la gracia divina como ancla de fe para todo creyente."
        }
    }

    fun getLastDictionarySnippet(): String {
        val state = _uiState.value
        val dict = state.lastConsultedDictionary ?: state.currentDictionaryEntry
        return if (dict != null) {
            "[Léxico/Diccionario: ${dict.displayTerm} (${dict.originalWord})]\n${dict.definition}"
        } else {
            "[Léxico/Diccionario: Logos (λόγος)]\nG3056: La Palabra eterna, expresión y manifestación viva de Dios."
        }
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
    }
}
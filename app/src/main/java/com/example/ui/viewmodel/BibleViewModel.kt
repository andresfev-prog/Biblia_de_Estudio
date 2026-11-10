package com.example.ui.viewmodel

import android.app.Application
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.BibleBooksData
import com.example.data.model.BibleModule
import com.example.data.model.BibleVerse
import com.example.data.model.BookInfo
import com.example.data.model.Commentary
import com.example.data.model.CrossReference
import com.example.data.model.DictionaryEntry
import com.example.data.model.FavoriteVerse
import com.example.data.model.ReadingProgress
import com.example.data.model.SearchHistoryItem
import com.example.data.model.UserNote
import com.example.data.model.UserPreference
import com.example.data.repository.BibleAssetDatabaseHelper
import com.example.data.repository.BibleRepository
import com.example.ui.util.DailyVerseCatalog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import com.example.data.util.ModuleDownloader

enum class AppDestination {
    HOME,
    BIBLE,
    MODULES,
    SEARCH,
    SETTINGS
}

data class BibleUiState(
    val currentDestination: AppDestination = AppDestination.HOME,
    val currentBook: String = "Juan",
    val currentChapter: Int = 1,
    val currentVerse: Int = 1,
    val activeVersion: String = "RVR1960",
    val parallelVersion: String = "KJV",
    val isParallelMode: Boolean = false,
    val isParallelLinked: Boolean = true,
    val verses: List<BibleVerse> = emptyList(),
    val parallelVerses: List<BibleVerse> = emptyList(),
    val highlightedVerse: Int? = null,
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
    val pickerSelectedBook: BookInfo = BibleBooksData.books.first { it.name == "Juan" },
    val pickerSelectedChapter: Int = 1,
    // Modales de estudio
    val selectedCommentaryVerse: BibleVerse? = null,
    val currentVerseCommentaries: List<Commentary> = emptyList(),
    val selectedDictionaryWord: String? = null,
    val currentDictionaryEntry: DictionaryEntry? = null,
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
    val favorites: List<FavoriteVerse> = emptyList(),
    val notes: List<UserNote> = emptyList(),
    val isNotesSheetOpen: Boolean = false,
    val editingNote: UserNote? = null,
    val lastConsultedCommentary: Commentary? = null,
    val lastConsultedDictionary: DictionaryEntry? = null,
    // UI general
    val isBottomBarVisible: Boolean = true,
    val showLogoutDialog: Boolean = false,
    val showFavoritesDialog: Boolean = false,
    val showEditProfileDialog: Boolean = false,
    val toastMessage: String? = null
)

class BibleViewModel(
    application: Application,
    private val repository: BibleRepository
) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    private val downloader = ModuleDownloader(application)


    private val _uiState = MutableStateFlow(BibleUiState())
    val uiState: StateFlow<BibleUiState> = _uiState.asStateFlow()

    private val assetHelper = BibleAssetDatabaseHelper(application)

    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false
    private var hasSpokenChapterIntro = false

    private val _searchQueryInput = MutableStateFlow("")

    init {
        initTts(application)
        observeData()
        observeSearchQuery()
        loadChapter(_uiState.value.currentBook, _uiState.value.currentChapter)
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
                if (mods.isEmpty() || mods.none { it.id == "mod_rvr1960" } || mods.any { it.id == "mod_wesley" && it.language != "Inglés" }) {
                    repository.seedInitialModules()
                } else {
                    _uiState.value = _uiState.value.copy(modules = mods)
                }
            }
        }

        viewModelScope.launch {
            repository.getReadingProgress().collect { progress ->
                _uiState.value = _uiState.value.copy(readingProgress = progress)
            }
        }

        viewModelScope.launch {
            repository.getAllFavorites().collect { favs ->
                _uiState.value = _uiState.value.copy(favorites = favs)
            }
        }

        viewModelScope.launch {
            repository.getSearchHistory().collect { history ->
                _uiState.value = _uiState.value.copy(searchHistory = history)
            }
        }

        viewModelScope.launch {
            repository.getAllModules().collect { mods ->
                _uiState.value = _uiState.value.copy(modules = mods)
            }
        }

        viewModelScope.launch {
            repository.getAllNotes().collect { list ->
                _uiState.value = _uiState.value.copy(notes = list)
            }
        }

        loadChapter("Juan", 1, "RVR1960", "KJV")
    }

    fun navigateTo(destination: AppDestination) {
        _uiState.value = _uiState.value.copy(currentDestination = destination)
    }

    fun loadChapter(
        bookName: String,
        chapter: Int,
        version: String = _uiState.value.activeVersion,
        parallelVersion: String = _uiState.value.parallelVersion
    ) {
        hasSpokenChapterIntro = false
        _uiState.value = _uiState.value.copy(ttsVerseIndex = 0)

        // 1. Cargar versículos del panel principal
        viewModelScope.launch {
            repository.getVerses(version, bookName, chapter).collect { verseList ->
                val cleanList = ArrayList<BibleVerse>()
                for (verse in verseList) {
                    val cleanText = verse.text
                        .replace(Regex("<S>.*?</S>|<.*?>"), "")
                        .replace(Regex("(?<=\\p{L})\\d+"), "")
                        .replace(Regex("\\s+"), " ")
                        .trim()
                    cleanList.add(verse.copy(text = cleanText))
                }

                _uiState.value = _uiState.value.copy(
                    currentBook = bookName,
                    currentChapter = chapter,
                    activeVersion = version,
                    verses = cleanList
                )
            }
        }

        // 2. Cargar etiquetas COM reales
        viewModelScope.launch {
            repository.getVersesWithComments(bookName, chapter).collect { commentsSet ->
                _uiState.value = _uiState.value.copy(
                    versesWithComments = commentsSet,
                    parallelVersesWithComments = if (!_uiState.value.isParallelMode || _uiState.value.isParallelLinked) {
                        commentsSet
                    } else {
                        _uiState.value.parallelVersesWithComments
                    }
                )
            }
        }

        // 3. Cargar etiquetas REF reales desde TSK-x.crossreferences.SQLite3
        viewModelScope.launch(Dispatchers.IO) {
            val refsSet = assetHelper.getVersesWithCrossReferences(bookName, chapter)
            withContext(Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(
                    versesWithCrossReferences = refsSet,
                    parallelVersesWithCrossReferences = if (!_uiState.value.isParallelMode || _uiState.value.isParallelLinked) {
                        refsSet
                    } else {
                        _uiState.value.parallelVersesWithCrossReferences
                    }
                )
            }
        }

        // 4. Cargar panel paralelo
        if (!_uiState.value.isParallelMode || _uiState.value.isParallelLinked) {
            viewModelScope.launch {
                repository.getVerses(parallelVersion, bookName, chapter).collect { pList ->
                    val cleanParallelList = ArrayList<BibleVerse>()
                    for (verse in pList) {
                        val cleanText = verse.text
                            .replace(Regex("<S>.*?</S>|<.*?>"), "")
                            .replace(Regex("(?<=\\p{L})\\d+"), "")
                            .replace(Regex("\\s+"), " ")
                            .trim()
                        cleanParallelList.add(verse.copy(text = cleanText))
                    }

                    _uiState.value = _uiState.value.copy(
                        parallelVersion = parallelVersion,
                        parallelVerses = cleanParallelList
                    )
                }
            }
        }

        viewModelScope.launch {
            repository.saveReadingProgress(bookName, chapter, 1, version)
        }
    }

    fun loadParallelChapter(bookName: String, chapter: Int, version: String = _uiState.value.parallelVersion) {
        viewModelScope.launch {
            repository.getVerses(version, bookName, chapter).collect { parallelList ->
                val cleanedList = ArrayList<BibleVerse>()
                for (item in parallelList) {
                    val cleanText = DailyVerseCatalog.cleanBibleText(item.text)
                    cleanedList.add(item.copy(text = cleanText))
                }
                _uiState.value = _uiState.value.copy(
                    parallelVerses = cleanedList,
                    parallelVersion = version
                )
            }
        }

        viewModelScope.launch {
            repository.getVersesWithComments(bookName, chapter).collect { commentsSet ->
                _uiState.value = _uiState.value.copy(parallelVersesWithComments = commentsSet)
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            val refsSet = assetHelper.getVersesWithCrossReferences(bookName, chapter)
            withContext(Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(parallelVersesWithCrossReferences = refsSet)
            }
        }
    }

    fun toggleParallelLinked() {
        val newLinked = !_uiState.value.isParallelLinked
        _uiState.value = _uiState.value.copy(isParallelLinked = newLinked)
        if (newLinked && _uiState.value.isParallelMode) {
            loadChapter(_uiState.value.currentBook, _uiState.value.currentChapter)
        }
    }

    fun navigateToPassage(bookName: String, chapter: Int, verse: Int = 1) {
        _uiState.value = _uiState.value.copy(
            currentDestination = AppDestination.BIBLE,
            highlightedVerse = verse
        )
        loadChapter(bookName, chapter)
    }

    fun setActiveVersion(version: String) {
        _uiState.value = _uiState.value.copy(activeVersion = version, searchVersion = version)
        configureNaturalTtsVoice()
        loadChapter(_uiState.value.currentBook, _uiState.value.currentChapter, version = version)
    }

    fun setParallelVersion(version: String) {
        _uiState.value = _uiState.value.copy(parallelVersion = version)
        loadChapter(_uiState.value.currentBook, _uiState.value.currentChapter, parallelVersion = version)
    }

    fun toggleParallelMode() {
        val newMode = !_uiState.value.isParallelMode
        _uiState.value = _uiState.value.copy(isParallelMode = newMode)
        if (newMode) {
            loadChapter(_uiState.value.currentBook, _uiState.value.currentChapter)
        }
    }

    // --- Selector de Pasajes ---
    fun openPassagePicker() {
        val currentBookObj = BibleBooksData.getBookByName(_uiState.value.currentBook)
            ?: BibleBooksData.books.first()
        _uiState.value = _uiState.value.copy(
            isPassagePickerVisible = true,
            pickerStep = 1,
            pickerSelectedBook = currentBookObj,
            pickerSelectedChapter = _uiState.value.currentChapter
        )
    }

    fun dismissPassagePicker() {
        _uiState.value = _uiState.value.copy(isPassagePickerVisible = false)
    }

    fun onPickerBookSelected(book: BookInfo) {
        _uiState.value = _uiState.value.copy(pickerSelectedBook = book, pickerStep = 2)
    }

    fun onPickerChapterSelected(chapter: Int) {
        _uiState.value = _uiState.value.copy(pickerSelectedChapter = chapter, pickerStep = 3)
    }

    fun onPickerVerseSelected(verse: Int) {
        val book = _uiState.value.pickerSelectedBook
        val chapter = _uiState.value.pickerSelectedChapter
        _uiState.value = _uiState.value.copy(
            isPassagePickerVisible = false,
            highlightedVerse = verse
        )
        loadChapter(book.name, chapter)
    }

    fun onPickerBackStep() {
        val currentStep = _uiState.value.pickerStep
        if (currentStep > 1) {
            _uiState.value = _uiState.value.copy(pickerStep = currentStep - 1)
        } else {
            dismissPassagePicker()
        }
    }

    // --- Comentarios ---
    fun openCommentary(verse: BibleVerse, preferredCode: String? = null) {
        val cleanVerseText = DailyVerseCatalog.cleanBibleText(verse.text)
        val cleanVerse = verse.copy(text = cleanVerseText)

        viewModelScope.launch {
            repository.getCommentaryForVerse(cleanVerse.bookName, cleanVerse.chapter, cleanVerse.verse).collect { realCommentText ->
                val fullList = if (!realCommentText.isNullOrBlank()) {
                    val cleanComment = realCommentText
                        .replace(Regex("<.*?>"), "")
                        .replace(Regex("\\s+"), " ")
                        .trim()
                    listOf(
                        Commentary(
                            bookName = cleanVerse.bookName,
                            chapter = cleanVerse.chapter,
                            verse = cleanVerse.verse,
                            author = "John Wesley",
                            title = "",
                            content = cleanComment
                        )
                    )
                } else {
                    listOf(
                        Commentary(
                            bookName = cleanVerse.bookName,
                            chapter = cleanVerse.chapter,
                            verse = cleanVerse.verse,
                            author = "John Wesley",
                            title = "",
                            content = "«${cleanVerse.text}»\n\nExégesis pastoral orientada al discernimiento bíblico y la santidad práctica."
                        )
                    )
                }

                _uiState.value = _uiState.value.copy(
                    selectedCommentaryVerse = cleanVerse,
                    currentVerseCommentaries = fullList,
                    lastConsultedCommentary = fullList.firstOrNull() ?: _uiState.value.lastConsultedCommentary
                )
            }
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
            isWordLookupLoading = true
        )

        viewModelScope.launch {
            val flowToCollect = if (verse != null) {
                repository.getDictionaryEntryWithContext(
                    term = searchLower,
                    bookName = verse.bookName,
                    chapter = verse.chapter,
                    verse = verse.verse,
                    version = verse.version
                )
            } else {
                repository.getDictionaryEntry(searchLower)
            }

            flowToCollect.collect { entry ->
                _uiState.value = _uiState.value.copy(
                    currentDictionaryEntry = entry,
                    lastConsultedDictionary = entry ?: _uiState.value.lastConsultedDictionary,
                    isWordLookupLoading = false
                )
            }
        }
    }

    fun dismissDictionary() {
        _uiState.value = _uiState.value.copy(
            selectedDictionaryWord = null,
            currentDictionaryEntry = null,
            isWordLookupLoading = false
        )
    }

    // --- Text-To-Speech ---
    fun toggleTtsBar() {
        val willShow = !_uiState.value.isTtsVisible
        _uiState.value = _uiState.value.copy(isTtsVisible = willShow)
        if (!willShow) {
            stopTts()
        }
    }

    fun playTts() {
        val verses = _uiState.value.verses
        if (verses.isEmpty()) return

        val index = _uiState.value.ttsVerseIndex.coerceIn(0, verses.lastIndex)
        val verse = verses[index]

        val cleanVerseText = DailyVerseCatalog.cleanBibleText(verse.text)
        val cleanSectionTitle = verse.sectionTitle?.let { DailyVerseCatalog.cleanBibleText(it) }?.trim() ?: ""

        configureNaturalTtsVoice(cleanVerseText)

        _uiState.value = _uiState.value.copy(
            isTtsPlaying = true,
            isTtsVisible = true,
            highlightedVerse = verse.verse
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
        _uiState.value = _uiState.value.copy(isTtsPlaying = false, isTtsVisible = false)
    }

    fun nextTtsVerse() {
        val verses = _uiState.value.verses
        if (verses.isEmpty()) return
        val nextIdx = (_uiState.value.ttsVerseIndex + 1).coerceAtMost(verses.lastIndex)
        _uiState.value = _uiState.value.copy(
            ttsVerseIndex = nextIdx,
            highlightedVerse = verses[nextIdx].verse
        )
        if (_uiState.value.isTtsPlaying) {
            playTts()
        }
    }

    fun prevTtsVerse() {
        val verses = _uiState.value.verses
        if (verses.isEmpty()) return
        val prevIdx = (_uiState.value.ttsVerseIndex - 1).coerceAtLeast(0)
        _uiState.value = _uiState.value.copy(
            ttsVerseIndex = prevIdx,
            highlightedVerse = verses[prevIdx].verse
        )
        if (_uiState.value.isTtsPlaying) {
            playTts()
        }
    }

    // --- Favoritos ---
    fun toggleFavorite(verse: BibleVerse) {
        viewModelScope.launch {
            val cleanBook = verse.bookName.trim()
            val isFav = _uiState.value.favorites.any {
                it.bookName.trim().equals(cleanBook, ignoreCase = true) &&
                        it.chapter == verse.chapter &&
                        it.verse == verse.verse
            }

            val cleanVerse = verse.copy(bookName = cleanBook)
            repository.toggleFavorite(cleanVerse, isFav)

            val lang = _uiState.value.userPreferences.interfaceLanguage
            val isEn = lang.contains("English", ignoreCase = true)
            _uiState.value = _uiState.value.copy(
                toastMessage = if (isFav) {
                    if (isEn) "Removed from favorites" else "Eliminado de favoritos"
                } else {
                    if (isEn) "Saved to favorites" else "Guardado en favoritos"
                }
            )
        }
    }

    fun removeFavorite(fav: FavoriteVerse) {
        viewModelScope.launch {
            repository.removeFavorite(fav.bookName, fav.chapter, fav.verse)
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

            val isEn = _uiState.value.userPreferences.interfaceLanguage.contains("English", ignoreCase = true)
            _uiState.value = _uiState.value.copy(
                toastMessage = if (isEn) "Module uninstalled" else "Módulo desinstalado"
            )
        }
    }

    // --- Preferencias ---
    fun saveUserPreferences(preference: UserPreference = _uiState.value.userPreferences) {
        _uiState.value = _uiState.value.copy(userPreferences = preference)
        viewModelScope.launch {
            repository.saveUserPreferences(preference)
        }
    }

    fun updateThemeMode(mode: String) {
        val updated = _uiState.value.userPreferences.copy(themeMode = mode)
        _uiState.value = _uiState.value.copy(userPreferences = updated)
        viewModelScope.launch { repository.saveUserPreferences(updated) }
    }

    fun updateFontFamily(font: String) {
        val updated = _uiState.value.userPreferences.copy(fontFamily = font)
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

    fun updateUserProfile(fullName: String, location: String, username: String, password: String) {
        val current = _uiState.value.userPreferences
        val updated = current.copy(
            userName = fullName.trim(),
            location = location.trim(),
            usernameHandle = username.trim().removePrefix("@"),
            password = password
        )
        _uiState.value = _uiState.value.copy(
            userPreferences = updated,
            showEditProfileDialog = false
        )
        viewModelScope.launch { repository.saveUserPreferences(updated) }
    }

    fun showLogoutDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showLogoutDialog = show)
    }

    fun confirmLogout() {
        val isEn = _uiState.value.userPreferences.interfaceLanguage.contains("English", ignoreCase = true)
        _uiState.value = _uiState.value.copy(
            showLogoutDialog = false,
            toastMessage = if (isEn) "Logged out successfully" else "Sesión cerrada correctamente"
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
        val defaultRef = "${_uiState.value.currentBook} ${_uiState.value.currentChapter}:${_uiState.value.highlightedVerse ?: _uiState.value.currentVerse}"
        val note = UserNote(
            id = noteId,
            title = title.ifBlank { if (isEn) "Study Note" else "Nota de Estudio" },
            content = content,
            biblicalReference = reference.ifBlank { defaultRef },
            timestamp = System.currentTimeMillis()
        )
        viewModelScope.launch {
            repository.saveNote(note)
            _uiState.value = _uiState.value.copy(
                editingNote = null,
                toastMessage = if (isEn) "Note saved successfully" else "Nota guardada correctamente"
            )
        }
    }

    fun deleteNote(id: Long) {
        val isEn = _uiState.value.userPreferences.interfaceLanguage.contains("English", ignoreCase = true)
        viewModelScope.launch {
            repository.deleteNote(id)
            _uiState.value = _uiState.value.copy(
                editingNote = null,
                toastMessage = if (isEn) "Note deleted" else "Nota eliminada"
            )
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
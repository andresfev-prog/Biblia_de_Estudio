package com.example

import android.content.Context
import androidx.activity.ComponentDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.AnnotatedString
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.BibleBooksData
import com.example.data.model.BibleModule
import com.example.data.model.BibleVerse
import com.example.data.model.Commentary
import com.example.data.model.CrossReference
import com.example.data.model.DictionaryEntry
import com.example.data.model.DictionaryMatch
import com.example.data.model.ModuleBibleCatalog
import com.example.data.model.UserNote
import com.example.data.repository.NoteDraftCache
import com.example.ui.components.CommentaryBottomSheet
import com.example.ui.components.CrossReferenceBottomSheet
import com.example.ui.components.DictionaryBottomSheet
import com.example.ui.components.LocalHideImeOverride
import com.example.ui.components.LocalImeVisibleOverride
import com.example.ui.components.NotesBottomSheet
import com.example.ui.components.PassageSelectorDialog
import com.example.ui.screens.settings.ProfileBackupDialog
import com.example.ui.theme.BibleStudyTheme
import com.example.ui.viewmodel.BibleUiState
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowDialog

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h915dp")
class BackNavigationAndNoteDraftTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var appContext: Context
    private lateinit var draftCache: NoteDraftCache

    private val sampleVerse = BibleVerse(
        id = 1L,
        version = "RVR1960",
        testament = "NT",
        bookNumber = 500,
        bookName = "Juan",
        chapter = 3,
        verse = 16,
        text = "Porque de tal manera amó Dios al mundo."
    )

    @Before
    fun setUp() {
        appContext = ApplicationProvider.getApplicationContext()
        draftCache = NoteDraftCache(appContext)
        draftCache.clearAll()
    }

    @After
    fun tearDown() {
        draftCache.clearAll()
    }

    private fun pressSystemBackOnLatestDialog() {
        composeTestRule.waitForIdle()
        val latestDialog = ShadowDialog.getLatestDialog()
        assertNotNull("Expected an active Dialog/ModalBottomSheet window", latestDialog)
        assertTrue(
            "Expected ComponentDialog backing Compose modal, got ${latestDialog?.javaClass?.name}",
            latestDialog is ComponentDialog
        )
        composeTestRule.runOnUiThread {
            (latestDialog as ComponentDialog).onBackPressedDispatcher.onBackPressed()
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun `commentary sheet system back steps from detail back to list before dismissing`() {
        var dismissed = false
        val commentaries = listOf(
            Commentary(
                id = 1L,
                bookName = "Juan",
                chapter = 3,
                verse = 16,
                author = "Matthew Henry",
                title = "El amor de Dios",
                content = "Comentario completo de Matthew Henry sobre Juan 3:16."
            ),
            Commentary(
                id = 2L,
                bookName = "Juan",
                chapter = 3,
                verse = 16,
                author = "Jamieson-Fausset-Brown",
                title = "Salvación",
                content = "Comentario completo de JFB sobre Juan 3:16."
            )
        )

        composeTestRule.setContent {
            BibleStudyTheme(themeMode = "LIGHT") {
                if (!dismissed) {
                    CommentaryBottomSheet(
                        verse = sampleVerse,
                        commentaries = commentaries,
                        interfaceLanguage = "Español",
                        onDismiss = { dismissed = true }
                    )
                }
            }
        }

        // Nivel 1: listado de comentarios
        composeTestRule.onNodeWithTag("commentary_card_0").assertIsDisplayed()
        composeTestRule.onNodeWithTag("commentary_card_0").performClick()
        composeTestRule.waitForIdle()

        // Nivel 2: lectura del comentario
        composeTestRule.onNodeWithTag("commentary_content_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("back_to_commentaries_button").assertIsDisplayed()

        // Primer Atrás del sistema: vuelve al Nivel 1 sin cerrar el panel
        pressSystemBackOnLatestDialog()
        assertFalse("System Back from Level 2 must not dismiss the commentary sheet", dismissed)
        composeTestRule.onNodeWithTag("commentary_card_0").assertIsDisplayed()
        composeTestRule.onNodeWithTag("commentary_content_card").assertDoesNotExist()

        // Segundo Atrás del sistema en la raíz: cierra el panel limpiamente
        pressSystemBackOnLatestDialog()
        assertTrue("System Back from Level 1 root must dismiss the commentary sheet", dismissed)
    }

    @Test
    fun `dictionary sheet system back steps from definition back to dictionary list before dismissing`() {
        var dismissed = false
        val strongModule = BibleModule(
            id = "dict_strong",
            category = "DICTIONARY",
            title = "Diccionario Strong",
            author = "James Strong",
            description = "Concordancia Strong",
            language = "Español",
            versionTag = "STRONG",
            sizeText = "5 MB",
            fileName = "Strong.dictionary.SQLite3",
            isDownloaded = true
        )
        val vineModule = strongModule.copy(
            id = "dict_vine",
            title = "Diccionario Vine",
            author = "W. E. Vine",
            versionTag = "VINE",
            fileName = "Vine.dictionary.SQLite3"
        )
        val matches = listOf(
            DictionaryMatch(
                module = strongModule,
                entry = DictionaryEntry(
                    term = "amor",
                    displayTerm = "Amor",
                    strongCode = "G26",
                    originalWord = "ἀγάπη",
                    pronunciation = "agápe",
                    definition = "Amor divino e incondicional."
                )
            ),
            DictionaryMatch(
                module = vineModule,
                entry = DictionaryEntry(
                    term = "amor",
                    displayTerm = "Amor",
                    strongCode = "G25",
                    originalWord = "ἀγαπάω",
                    pronunciation = "agapáo",
                    definition = "Amar con entrega deliberada."
                )
            )
        )

        composeTestRule.setContent {
            BibleStudyTheme(themeMode = "LIGHT") {
                if (!dismissed) {
                    DictionaryBottomSheet(
                        searchedWord = "Amor",
                        matches = matches,
                        isLoading = false,
                        interfaceLanguage = "Español",
                        onDismiss = { dismissed = true }
                    )
                }
            }
        }

        // Nivel 1: lista de diccionarios
        composeTestRule.onNodeWithTag("dict_match_STRONG").assertIsDisplayed()
        composeTestRule.onNodeWithTag("dict_match_STRONG").performClick()
        composeTestRule.waitForIdle()

        // Nivel 2: definición detallada
        composeTestRule.onNodeWithTag("dictionary_entry_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("back_to_dictionaries_button").assertIsDisplayed()

        // Primer Atrás del sistema: vuelve a la lista sin cerrar
        pressSystemBackOnLatestDialog()
        assertFalse("System Back from Level 2 must not dismiss the dictionary sheet", dismissed)
        composeTestRule.onNodeWithTag("dict_match_STRONG").assertIsDisplayed()
        composeTestRule.onNodeWithTag("dictionary_entry_card").assertDoesNotExist()

        // Segundo Atrás del sistema en la raíz: cierra el panel
        pressSystemBackOnLatestDialog()
        assertTrue("System Back from Level 1 root must dismiss the dictionary sheet", dismissed)
    }

    @Test
    fun `cross reference sheet system back steps from reference detail back to list before dismissing`() {
        var dismissed = false
        val refs = listOf(
            CrossReference(
                sourceBook = "Juan",
                sourceChapter = 3,
                sourceVerse = 16,
                targetBook = "Romanos",
                targetChapter = 5,
                targetVerse = 8,
                targetText = "Mas Dios muestra su amor para con nosotros.",
                relationNote = "Amor redentor"
            ),
            CrossReference(
                sourceBook = "Juan",
                sourceChapter = 3,
                sourceVerse = 16,
                targetBook = "1 Juan",
                targetChapter = 4,
                targetVerse = 9,
                targetText = "En esto se mostró el amor de Dios para con nosotros."
            )
        )

        composeTestRule.setContent {
            BibleStudyTheme(themeMode = "LIGHT") {
                if (!dismissed) {
                    CrossReferenceBottomSheet(
                        verse = sampleVerse,
                        crossReferences = refs,
                        interfaceLanguage = "Español",
                        onNavigateToTarget = { _, _, _ -> },
                        onDismiss = { dismissed = true }
                    )
                }
            }
        }

        // Nivel 1: listado de referencias
        composeTestRule.onNodeWithTag("cross_ref_item_0").assertIsDisplayed()
        composeTestRule.onNodeWithTag("cross_ref_item_0").performClick()
        composeTestRule.waitForIdle()

        // Nivel 2: detalle de referencia seleccionada
        composeTestRule.onNodeWithTag("cross_ref_detail_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("back_to_cross_refs_button").assertIsDisplayed()

        // Primer Atrás del sistema: regresa a la lista de referencias
        pressSystemBackOnLatestDialog()
        assertFalse("System Back from detail must not dismiss the cross-reference sheet", dismissed)
        composeTestRule.onNodeWithTag("cross_ref_item_0").assertIsDisplayed()
        composeTestRule.onNodeWithTag("cross_ref_detail_card").assertDoesNotExist()

        // Segundo Atrás del sistema: cierra el panel
        pressSystemBackOnLatestDialog()
        assertTrue("System Back from root must dismiss the cross-reference sheet", dismissed)
    }

    @Test
    fun `passage selector dialog system back steps through 3-step picker before dismissing`() {
        var step by mutableIntStateOf(3)
        var dismissed by mutableStateOf(false)
        val genesis = BibleBooksData.books.first()
        val catalog = ModuleBibleCatalog(
            version = "RVR1960",
            availableBooks = listOf(genesis),
            chaptersByBookNumber = mapOf(genesis.number to listOf(1, 2, 3))
        )

        composeTestRule.setContent {
            BibleStudyTheme(themeMode = "LIGHT") {
                if (!dismissed) {
                    PassageSelectorDialog(
                        currentStep = step,
                        selectedBook = genesis,
                        selectedChapter = 1,
                        catalog = catalog,
                        availableVerses = listOf(1, 2, 3),
                        interfaceLanguage = "Español",
                        onBookSelected = {},
                        onChapterSelected = {},
                        onVerseSelected = {},
                        onBackStep = { if (step > 1) step-- },
                        onDismiss = { dismissed = true }
                    )
                }
            }
        }

        assertEquals(3, step)
        pressSystemBackOnLatestDialog()
        assertEquals(2, step)
        assertFalse(dismissed)

        pressSystemBackOnLatestDialog()
        assertEquals(1, step)
        assertFalse(dismissed)

        pressSystemBackOnLatestDialog()
        assertTrue(dismissed)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun `notes editor system back hides IME first without closing editor, then steps back and dismisses`() {
        var dismissed by mutableStateOf(false)
        var imeVisible by mutableStateOf(true)
        var hideKeyboardCalls = 0

        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalImeVisibleOverride provides imeVisible,
                LocalHideImeOverride provides {
                    hideKeyboardCalls++
                    imeVisible = false
                }
            ) {
                BibleStudyTheme(themeMode = "LIGHT") {
                    if (!dismissed) {
                        NotesBottomSheet(
                            notes = emptyList(),
                            editingNote = null,
                            interfaceLanguage = "Español",
                            onSaveNote = { _, _, _, _ -> },
                            onDeleteNote = {},
                            onSelectNoteToEdit = {},
                            onDismiss = { dismissed = true },
                            draftCache = draftCache
                        )
                    }
                }
            }
        }

        // Abrir editor de nueva nota y escribir contenido
        composeTestRule.onNodeWithTag("new_note_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("note_editor_view").assertIsDisplayed()
        composeTestRule.onNodeWithTag("note_content_input").performTextInput("Reflexión en curso")
        composeTestRule.waitForIdle()

        // 1er Atrás con teclado abierto: solo oculta el teclado, el editor sigue abierto con el texto intacto
        pressSystemBackOnLatestDialog()
        assertEquals("Keyboard hide() should be triggered when IME is visible", 1, hideKeyboardCalls)
        assertFalse("Editor sheet must stay open when hiding IME", dismissed)
        composeTestRule.onNodeWithTag("note_editor_view").assertIsDisplayed()
        composeTestRule.onNodeWithTag("note_content_input")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString("Reflexión en curso")))

        // 2do Atrás con teclado ya oculto: retrocede un paso a la lista de notas (conservando el borrador)
        pressSystemBackOnLatestDialog()
        assertFalse("System Back from editor should step back to list, not close whole sheet", dismissed)
        composeTestRule.onNodeWithTag("notes_list_view").assertIsDisplayed()
        assertEquals("Reflexión en curso", draftCache.loadDraft(0L)?.content)

        // 3er Atrás en la raíz (lista de notas): cierra la hoja limpiamente
        pressSystemBackOnLatestDialog()
        assertTrue("System Back from notes list root should dismiss the sheet", dismissed)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun `new note draft survives closing without saving, restores on reopen, and clears only on discard or save`() {
        var showSheet by mutableStateOf(true)
        var savedTriple: Triple<String, String, String>? = null

        composeTestRule.setContent {
            BibleStudyTheme(themeMode = "LIGHT") {
                if (showSheet) {
                    NotesBottomSheet(
                        notes = emptyList(),
                        editingNote = null,
                        interfaceLanguage = "Español",
                        onSaveNote = { title, content, reference, _ ->
                            savedTriple = Triple(title, content, reference)
                        },
                        onDeleteNote = {},
                        onSelectNoteToEdit = {},
                        onDismiss = { showSheet = false },
                        draftCache = draftCache
                    )
                }
            }
        }

        // Sesión 1: escribir nota sin pulsar Guardar y cerrar abruptamente la ventana
        composeTestRule.onNodeWithTag("new_note_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("recovered_draft_banner").assertDoesNotExist()
        composeTestRule.onNodeWithTag("note_title_input").performTextInput("Promesa fiel")
        composeTestRule.onNodeWithTag("note_reference_input").performTextInput("Salmos 23:1")
        composeTestRule.onNodeWithTag("note_content_input").performTextInput("Jehová es mi pastor; nada me faltará.")
        composeTestRule.waitForIdle()

        showSheet = false
        composeTestRule.waitForIdle()

        // Confirmar persistencia física en NoteDraftCache
        val persisted = draftCache.loadDraft(0L)
        assertNotNull("Unsaved new note draft must persist in NoteDraftCache", persisted)
        assertEquals("Promesa fiel", persisted?.title)
        assertEquals("Salmos 23:1", persisted?.reference)
        assertEquals("Jehová es mi pastor; nada me faltará.", persisted?.content)

        // Sesión 2: reabrir y verificar restauración exacta + opción discreta de descartar
        showSheet = true
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("new_note_button").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("recovered_draft_banner").assertIsDisplayed()
        composeTestRule.onNodeWithTag("note_title_input")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString("Promesa fiel")))
        composeTestRule.onNodeWithTag("note_reference_input")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString("Salmos 23:1")))
        composeTestRule.onNodeWithTag("note_content_input")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString("Jehová es mi pastor; nada me faltará.")))

        // Descartar explícitamente el borrador lo limpia y restaura campos vacíos
        composeTestRule.onNodeWithTag("discard_draft_button").performClick()
        composeTestRule.waitForIdle()
        assertNull("Discarding draft must clear NoteDraftCache", draftCache.loadDraft(0L))
        composeTestRule.onNodeWithTag("recovered_draft_banner").assertDoesNotExist()
        composeTestRule.onNodeWithTag("note_title_input")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString("")))

        // Escribir nuevo borrador, cerrar sin guardar, reabrir y Guardar definitivamente
        composeTestRule.onNodeWithTag("note_title_input").performTextInput("Nota definitiva")
        composeTestRule.onNodeWithTag("note_content_input").performTextInput("Contenido final")
        composeTestRule.waitForIdle()
        showSheet = false
        composeTestRule.waitForIdle()
        assertNotNull(draftCache.loadDraft(0L))

        showSheet = true
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("new_note_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("recovered_draft_banner").assertIsDisplayed()
        composeTestRule.onNodeWithTag("save_note_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        assertEquals(Triple("Nota definitiva", "Contenido final", ""), savedTriple)
        assertNull("Saving note must clear its temporary draft", draftCache.loadDraft(0L))
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun `existing note edit draft is isolated by noteId, restored on reopen, and cleared on delete`() {
        val existingNote = UserNote(
            id = 42L,
            title = "Estudio Romanos",
            content = "Texto original",
            biblicalReference = "Romanos 8:28",
            timestamp = 1700000000L
        )
        var showSheet by mutableStateOf(true)
        var deletedId: Long? = null

        composeTestRule.setContent {
            BibleStudyTheme(themeMode = "DARK") {
                if (showSheet) {
                    NotesBottomSheet(
                        notes = listOf(existingNote),
                        editingNote = null,
                        interfaceLanguage = "Español",
                        onSaveNote = { _, _, _, _ -> },
                        onDeleteNote = { id -> deletedId = id },
                        onSelectNoteToEdit = {},
                        onDismiss = { showSheet = false },
                        draftCache = draftCache
                    )
                }
            }
        }

        // Abrir nota existente #42 y modificar contenido sin guardar
        composeTestRule.onNodeWithTag("note_item_42").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("note_content_input").performTextInput(" + apuntes nuevos")
        composeTestRule.waitForIdle()

        // Cerrar sin guardar
        showSheet = false
        composeTestRule.waitForIdle()

        assertNull("New-note draft slot must remain empty when editing note 42", draftCache.loadDraft(0L))
        val savedDraft = draftCache.loadDraft(42L)
        assertNotNull("Draft for note 42 must be persisted", savedDraft)
        assertTrue(savedDraft!!.content.contains("apuntes nuevos"))

        // Reabrir nota #42 y comprobar recuperación del borrador
        showSheet = true
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("note_item_42").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("recovered_draft_banner").assertIsDisplayed()
        composeTestRule.onNodeWithTag("note_content_input")
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.EditableText,
                    AnnotatedString(savedDraft.content)
                )
            )

        // Eliminar la nota debe limpiar su borrador temporal
        composeTestRule.onNodeWithTag("delete_note_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        assertEquals(42L, deletedId)
        assertNull("Deleting a note must clear its temporary draft", draftCache.loadDraft(42L))
    }

    @Test
    fun `profile dialog system back hides IME first, cancels in-flight google auth, then dismisses cleanly`() {
        var imeVisible by mutableStateOf(true)
        var isAuthInProgress by mutableStateOf(true)
        var dismissCount = 0
        var cancelAuthCount = 0
        var hideKeyboardCount = 0

        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalImeVisibleOverride provides imeVisible,
                LocalHideImeOverride provides {
                    hideKeyboardCount++
                    imeVisible = false
                }
            ) {
                BibleStudyTheme(themeMode = "LIGHT") {
                    if (dismissCount == 0) {
                        ProfileBackupDialog(
                            uiState = BibleUiState(isGoogleAuthInProgress = isAuthInProgress),
                            interfaceLanguage = "Español",
                            onSaveLocalName = {},
                            onContinueWithGoogle = {},
                            onSyncNow = {},
                            onUnlinkAccount = {},
                            onDismiss = { dismissCount++ },
                            onCancelGoogleAuth = {
                                cancelAuthCount++
                                isAuthInProgress = false
                            }
                        )
                    }
                }
            }
        }

        // 1er Atrás con teclado visible: solo oculta el teclado
        pressSystemBackOnLatestDialog()
        assertEquals(1, hideKeyboardCount)
        assertEquals(0, cancelAuthCount)
        assertEquals(0, dismissCount)

        // 2do Atrás durante autenticación de Google: cancela la autenticación sin cerrar de golpe
        pressSystemBackOnLatestDialog()
        assertEquals(1, cancelAuthCount)
        assertEquals(0, dismissCount)

        // 3er Atrás en estado raíz sin teclado ni autenticación pendiente: cierra el diálogo
        pressSystemBackOnLatestDialog()
        assertEquals(1, dismissCount)
    }
}

package com.example.ui.screens.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BibleBooksData
import com.example.data.model.BibleVerse
import com.example.ui.components.NotesBottomSheet
import com.example.ui.components.TutorialStep
import com.example.ui.components.tutorialTarget
import com.example.ui.theme.AppIcon
import com.example.ui.theme.AppIcons
import com.example.ui.theme.AppTheme
import com.example.ui.theme.AppThemeIds
import com.example.ui.util.BibleStudyTextUtils
import com.example.ui.util.DailyVerseCatalog
import com.example.ui.util.StringsLocale
import com.example.ui.viewmodel.BibleUiState
import com.example.ui.viewmodel.BibleViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val HomeCrispLightBg = Color(0xFFF6F7F9)
private val HomeCrispTitleNavy = Color(0xFF0D2240)
private val HomeCrispBorder = Color(0xFFE5E9F0)
private val HomeCrispMutedText = Color(0xFF647082)
private val HomeCrispInnerBox = Color(0xFFF2F4F8)
private val HeroMidnightTop = Color(0xFF0F1D33)
private val HeroMidnightBottom = Color(0xFF08111E)
private val HeroPillBg = Color(0xFF1A3154)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: BibleUiState,
    viewModel: BibleViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val lang = uiState.userPreferences.interfaceLanguage
    val appColors = AppTheme.colors
    val isCrispLight = !appColors.isDark && appColors.themeId == AppThemeIds.CRISP_LIGHT

    val screenBg = if (isCrispLight) HomeCrispLightBg else appColors.background
    val headingColor = if (isCrispLight) HomeCrispTitleNavy else appColors.titleColor
    val mutedTextColor = if (isCrispLight) HomeCrispMutedText else appColors.textSecondary
    val cardBorderColor = if (isCrispLight) HomeCrispBorder else appColors.border
    val innerPreviewBg = if (isCrispLight) HomeCrispInnerBox else appColors.surfaceVariant
    val softBadgeBg = if (isCrispLight) HomeCrispInnerBox else appColors.primaryContainer
    val softBadgeText = if (isCrispLight) HomeCrispTitleNavy else appColors.onPrimaryContainer

    // Auto-scroll para el tutorial en vivo
    LaunchedEffect(uiState.activeTutorialStep) {
        when (uiState.activeTutorialStep) {
            TutorialStep.HOME_VERSE_OF_DAY -> {
                scrollState.animateScrollTo(0)
            }
            TutorialStep.HOME_CONTINUE_NOTES -> {
                scrollState.animateScrollTo(360)
            }
            else -> {}
        }
    }

    // Dynamic greeting based on time of day
    val greeting = remember(lang) {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> StringsLocale.get("greeting_morning", lang)
            in 12..18 -> StringsLocale.get("greeting_afternoon", lang)
            else -> StringsLocale.get("greeting_night", lang)
        }
    }

    // Verse of the Day
    val verseOfTheDay = remember {
        BibleVerse(
            id = 500406,
            version = "RVR1960",
            testament = "NT",
            bookNumber = 50,
            bookName = "Filipenses",
            chapter = 4,
            verse = 6,
            text = "Por nada estéis afanosos, sino sean conocidas vuestras peticiones delante de Dios en toda oración y ruego, con acción de gracias. Y la paz de Dios, que sobrepasa todo entendimiento, guardará vuestros corazones y vuestros pensamientos en Cristo Jesús."
        )
    }

    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(screenBg)
            .verticalScroll(scrollState)
            .padding(
                start = 20.dp,
                end = 20.dp,
                top = 18.dp,
                bottom = 118.dp + navBarBottom
            )
            .testTag("home_screen")
    ) {
        // Encabezado con jerarquía tipográfica limpia
        val effectiveDisplayName = remember(uiState.googleDisplayName, uiState.userPreferences.userName) {
            uiState.googleDisplayName.trim().ifBlank {
                uiState.userPreferences.userName.trim()
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            if (effectiveDisplayName.isNotBlank()) {
                Text(
                    text = "$greeting,",
                    fontSize = 23.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.Normal,
                    color = mutedTextColor,
                    maxLines = 1,
                    softWrap = false
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = effectiveDisplayName,
                    fontSize = 28.sp,
                    lineHeight = 34.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = headingColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(
                    text = greeting,
                    fontSize = 28.sp,
                    lineHeight = 34.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = headingColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = StringsLocale.get("home_subtitle", lang),
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Normal,
                color = mutedTextColor
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // =========================================================================
        // Versículo del Día: Detección Robusta de Idioma (Español vs English)
        // =========================================================================
        val prefLang = uiState.userPreferences.interfaceLanguage
        val effectiveLang = if (prefLang.isNotBlank()) prefLang else lang

        val isEnglish = effectiveLang.startsWith("en", ignoreCase = true) ||
                effectiveLang.contains("English", ignoreCase = true) ||
                effectiveLang.contains("Inglés", ignoreCase = true)

        val normalizedLang = if (isEnglish) "en" else "es"
        val versionBadge = if (isEnglish) "KJV" else "RVR 1960"
        val targetDbName = if (isEnglish) "KJV.SQLite3" else "RVR1960.SQLite3"

        val todayRef = remember { DailyVerseCatalog.getTodayReference() }
        val todayBookName = if (isEnglish) todayRef.bookNameEn else todayRef.bookNameEs

        val authenticDefaultText = if (isEnglish) {
            "And ye shall know the truth, and the truth shall make you free."
        } else {
            "Y conoceréis la verdad, y la verdad os hará libres."
        }

        var verseTextState by remember(isEnglish) { mutableStateOf(authenticDefaultText) }

        LaunchedEffect(isEnglish) {
            var retrievedText = ""
            var attempts = 0
            while (retrievedText.isBlank() && attempts < 10) {
                retrievedText = withContext(Dispatchers.Default) {
                    queryDailyVerseFromDatabase(
                        context = context,
                        databaseName = targetDbName,
                        bookName = todayBookName,
                        chapter = todayRef.chapter,
                        verse = todayRef.verse
                    )
                }
                if (retrievedText.isBlank()) {
                    delay(200)
                    attempts++
                }
            }
            if (retrievedText.isNotBlank()) {
                verseTextState = retrievedText
            }
        }

        val dailyVerse = remember(verseTextState, isEnglish, verseOfTheDay) {
            verseOfTheDay.copy(
                bookName = todayBookName,
                chapter = todayRef.chapter,
                verse = todayRef.verse,
                text = verseTextState,
                version = versionBadge
            )
        }

        val isCurrentVotdFav = viewModel.isFavorite(dailyVerse, uiState.favoriteKeys)

        // Tarjeta protagonista «Versículo del día» — Azul medianoche (#0F1D33) con tipografía serif
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("verse_of_the_day_card")
                .tutorialTarget(TutorialStep.HOME_VERSE_OF_DAY),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = HeroMidnightTop),
            border = if (appColors.isDark) BorderStroke(1.dp, appColors.border) else null,
            elevation = CardDefaults.cardElevation(defaultElevation = if (appColors.isDark) 2.dp else 3.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(HeroMidnightTop, HeroMidnightBottom)
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEnglish) "Verse of the Day" else "Versículo del día",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFFD2DCEB),
                        maxLines = 1,
                        softWrap = false
                    )
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.14f))
                            .padding(horizontal = 11.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = versionBadge,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFE2E8F0),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "«${dailyVerse.text}»",
                    fontFamily = FontFamily.Serif,
                    fontSize = 18.sp,
                    lineHeight = 27.sp,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Normal,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "${dailyVerse.bookName} ${dailyVerse.chapter}:${dailyVerse.verse}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFFCBD5E1),
                    maxLines = 1,
                    softWrap = false
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            viewModel.navigateToPassage(dailyVerse.bookName, dailyVerse.chapter, dailyVerse.verse)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HeroPillBg,
                            contentColor = Color.White
                        ),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
                        shape = CircleShape,
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                        modifier = Modifier.testTag("read_full_chapter_button")
                    ) {
                        Text(
                            text = if (isEnglish) "Read full chapter" else "Leer capítulo completo",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.toggleFavorite(dailyVerse) },
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("votd_favorite_button")
                        ) {
                            Icon(
                                imageVector = if (isCurrentVotdFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Guardar en favoritos",
                                tint = if (isCurrentVotdFav) Color(0xFFF87171) else Color(0xFFCBD5E1),
                                modifier = Modifier.size(21.dp)
                            )
                        }
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText(
                                    "Versículo",
                                    "«${dailyVerse.text}» - ${dailyVerse.bookName} ${dailyVerse.chapter}:${dailyVerse.verse}"
                                )
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, StringsLocale.get("copied_clipboard", normalizedLang), Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("votd_copy_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = "Copiar versículo",
                                tint = Color(0xFFCBD5E1),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "«${dailyVerse.text}»\n— ${dailyVerse.bookName} ${dailyVerse.chapter}:${dailyVerse.verse} (${dailyVerse.version})"
                                    )
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, StringsLocale.get("share_verse", normalizedLang)))
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("votd_share_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = "Compartir versículo",
                                tint = Color(0xFFCBD5E1),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Bloque unificado: Lectura reciente y Notas
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .tutorialTarget(TutorialStep.HOME_CONTINUE_NOTES)
        ) {
            // Tarjeta «Continúa donde quedaste» — completa tocable, sin «Modo sin conexión listo» ni botón gigante
            val lastBookRaw = uiState.readingProgress?.bookName ?: "Juan"
            val lastChapter = uiState.readingProgress?.chapter ?: 1
            val lastVersion = uiState.readingProgress?.version ?: "RVR1960"

            val isEnglishVersion = BibleBooksData.isEnglishVersion(lastVersion)
            val shouldShowInEnglish = isEnglish || isEnglishVersion
            val bookInfo = BibleBooksData.getBookByName(lastBookRaw)
            val bookNumber = bookInfo?.number ?: 43
            val displayLastBook = BibleBooksData.getLocalizedBookName(bookNumber, shouldShowInEnglish)

            Card(
                onClick = { viewModel.navigateToPassage(lastBookRaw, lastChapter) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("continue_reading_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = appColors.cardBg),
                border = BorderStroke(1.dp, cardBorderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = StringsLocale.get("continue_where_left", lang),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal,
                                color = mutedTextColor,
                                maxLines = 1,
                                softWrap = false
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$displayLastBook $lastChapter",
                                fontSize = 23.sp,
                                lineHeight = 28.sp,
                                fontWeight = FontWeight.Medium,
                                color = headingColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = lastVersion,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal,
                                color = mutedTextColor,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = null,
                            tint = headingColor.copy(alpha = 0.75f),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .testTag("continue_reading_button"),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = StringsLocale.get("continue_reading", lang),
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = headingColor,
                            maxLines = 1,
                            softWrap = false
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = headingColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tarjeta «Mis Notas» — limpia con enlace inferior centrado en lugar de botón sólido gigante
            val latestNote = uiState.notes.firstOrNull()
            val noteDateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_notes_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = appColors.cardBg),
                border = BorderStroke(1.dp, cardBorderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    // Header Row
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
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (isCrispLight) HomeCrispTitleNavy else appColors.pillBg),
                                contentAlignment = Alignment.Center
                            ) {
                                AppIcon(
                                    glyph = AppIcons.MyNotes,
                                    contentDescription = null,
                                    tint = if (isCrispLight) Color.White else appColors.pillText,
                                    modifier = Modifier.size(21.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = StringsLocale.get("notes_title", lang),
                                    fontSize = 16.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = headingColor,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                val noteCount = uiState.notes.size
                                val notesCountText = if (isEnglish) {
                                    "$noteCount ${if (noteCount == 1) "note saved" else "notes saved"}"
                                } else {
                                    "$noteCount ${if (noteCount == 1) "nota guardada" else "notas guardadas"}"
                                }
                                Text(
                                    text = notesCountText,
                                    fontSize = 13.sp,
                                    color = mutedTextColor
                                )
                            }
                        }

                        if (latestNote != null && latestNote.biblicalReference.isNotBlank()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            val badgeLabel = remember(latestNote.biblicalReference) {
                                latestNote.biblicalReference.lineSequence()
                                    .firstOrNull { it.isNotBlank() }
                                    ?.trim()
                                    .orEmpty()
                            }
                            Surface(
                                shape = CircleShape,
                                color = softBadgeBg,
                                modifier = Modifier.widthIn(max = 150.dp)
                            ) {
                                Text(
                                    text = badgeLabel,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = softBadgeText,
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (latestNote != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(innerPreviewBg)
                                .clickable { viewModel.openNotesSheet(latestNote) }
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                                .testTag("home_latest_note_preview")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = latestNote.title,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = headingColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = noteDateFormat.format(Date(latestNote.timestamp)),
                                    fontSize = 11.5.sp,
                                    color = mutedTextColor
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = latestNote.content.ifBlank { StringsLocale.get("no_additional_content", lang) },
                                fontSize = 13.sp,
                                color = mutedTextColor,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { viewModel.openNotesSheet(null) }
                                .padding(vertical = 6.dp)
                                .testTag("home_view_all_notes_button"),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = StringsLocale.get("view_all_notes", lang),
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = headingColor,
                                maxLines = 1,
                                softWrap = false
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = headingColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        Text(
                            text = StringsLocale.get("no_notes_home", lang),
                            style = MaterialTheme.typography.bodyMedium,
                            color = mutedTextColor,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { viewModel.openNotesSheet() }
                                .padding(vertical = 6.dp)
                                .testTag("home_create_note_button"),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = StringsLocale.get("create_note", lang).replace("+", "").trim(),
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = headingColor,
                                maxLines = 1,
                                softWrap = false
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = headingColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Notes Sheet on Home
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
                onGetActiveVerseSnippet = { viewModel.getActiveVerseSnippet() },
                onGetLastCommentarySnippet = { viewModel.getLastCommentarySnippet() },
                onGetLastDictionarySnippet = { viewModel.getLastDictionarySnippet() },
                onDismiss = { viewModel.closeNotesSheet() }
            )
        }
    }
}

/**
 * Consulta en la base de datos física SQLite el texto real del versículo del día.
 * Valida la existencia e integridad del archivo y resuelve el book_number canónico MyBible.
 */
private fun queryDailyVerseFromDatabase(
    context: Context,
    databaseName: String,
    bookName: String,
    chapter: Int,
    verse: Int
): String {
    val dbFile = context.getDatabasePath(databaseName)

    // 1. Garantiza que la extracción física desde Assets haya finalizado (> 5 MB)
    if (!dbFile.exists() || dbFile.length() < 5_000_000L) {
        return ""
    }

    var cleanVerseText = ""
    var database: SQLiteDatabase? = null
    try {
        database = SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READONLY)

        // 2. Mapeo canónico internacional MyBible (Hebreos = 650)
        var bookNumber = getCanonicalBookNumber(bookName)

        // Si no se reconoció por nombre común, consultar la tabla 'books' de SQLite
        if (bookNumber == -1) {
            database.rawQuery(
                "SELECT book_number FROM books WHERE long_name = ? OR short_name = ? LIMIT 1",
                arrayOf(bookName.trim(), bookName.trim())
            ).use { cursor ->
                if (cursor.moveToFirst()) {
                    bookNumber = cursor.getInt(0)
                }
            }
        }

        // 3. Consulta indexada del versículo exacto
        if (bookNumber != -1) {
            database.rawQuery(
                "SELECT text FROM verses WHERE book_number = ? AND chapter = ? AND verse = ? LIMIT 1",
                arrayOf(bookNumber.toString(), chapter.toString(), verse.toString())
            ).use { cursor ->
                if (cursor.moveToFirst()) {
                    val rawText = cursor.getString(0) ?: ""
                    val versionHint = if (databaseName.contains("KJV", ignoreCase = true)) "KJV" else "RVR1960"
                    cleanVerseText = BibleStudyTextUtils.sanitizeVerseTextForSpeech(rawText, versionHint)
                    Log.i(
                        "BIBLE_DEBUG",
                        "Versículo del Día ($bookName [$bookNumber] $chapter:$verse) obtenido con éxito de $databaseName: $cleanVerseText"
                    )
                }
            }
        }
    } catch (e: Exception) {
        Log.e("BIBLE_DEBUG", "Error consultando Versículo del Día en $databaseName", e)
    } finally {
        database?.close()
    }
    return cleanVerseText
}

/**
 * Mapeo canónico MyBible de libros bíblicos a book_number numérico (10 a 730).
 */
private fun getCanonicalBookNumber(name: String): Int {
    val s = name.lowercase().trim()
    return when {
        s.startsWith("gén") || s.startsWith("gen") -> 10
        s.startsWith("éx") || s.startsWith("ex") -> 20
        s.startsWith("lev") -> 30
        s.startsWith("núm") || s.startsWith("num") -> 40
        s.startsWith("deu") -> 50
        s.startsWith("jos") -> 60
        s.startsWith("jue") || s.startsWith("judg") -> 70
        s.startsWith("rut") -> 80
        s.startsWith("1 s") || s.startsWith("1s") -> 90
        s.startsWith("2 s") || s.startsWith("2s") -> 100
        s.startsWith("1 r") || s.startsWith("1r") || s.startsWith("1 k") -> 110
        s.startsWith("2 r") || s.startsWith("2r") || s.startsWith("2 k") -> 120
        s.startsWith("1 cr") || s.startsWith("1cr") || s.startsWith("1 ch") -> 130
        s.startsWith("2 cr") || s.startsWith("2cr") || s.startsWith("2 ch") -> 140
        s.startsWith("esd") || s.startsWith("ezr") -> 150
        s.startsWith("neh") -> 160
        s.startsWith("est") -> 190
        s == "job" -> 220
        s.startsWith("sal") || s.startsWith("psa") -> 230
        s.startsWith("pro") -> 240
        s.startsWith("ecl") -> 250
        s.startsWith("can") || s.startsWith("son") -> 260
        s.startsWith("isa") -> 290
        s.startsWith("jer") -> 300
        s.startsWith("lam") -> 310
        s.startsWith("eze") -> 330
        s.startsWith("dan") -> 340
        s.startsWith("ose") || s.startsWith("hos") -> 350
        s.startsWith("joe") -> 360
        s.startsWith("am") -> 370
        s.startsWith("abd") || s.startsWith("oba") -> 380
        s.startsWith("jon") -> 390
        s.startsWith("miq") || s.startsWith("mic") -> 400
        s.startsWith("nah") -> 410
        s.startsWith("hab") -> 420
        s.startsWith("sof") || s.startsWith("zep") -> 430
        s.startsWith("hag") -> 440
        s.startsWith("zac") || s.startsWith("zec") -> 450
        s.startsWith("mal") -> 460
        s.startsWith("mat") -> 470
        s.startsWith("mar") -> 480
        s.startsWith("luc") || s.startsWith("luk") -> 490
        s.startsWith("jua") || s.startsWith("joh") -> 500
        s.startsWith("hec") || s.startsWith("act") -> 510
        s.startsWith("rom") -> 520
        s.startsWith("1 co") || s.startsWith("1co") -> 530
        s.startsWith("2 co") || s.startsWith("2co") -> 540
        s.startsWith("gál") || s.startsWith("gal") -> 550
        s.startsWith("efe") || s.startsWith("eph") -> 560
        s.startsWith("fil") && !s.contains("m") || s.startsWith("phi") -> 570
        s.startsWith("col") -> 580
        s.startsWith("1 te") || s.startsWith("1te") || s.startsWith("1 th") -> 590
        s.startsWith("2 te") || s.startsWith("2te") || s.startsWith("2 th") -> 600
        s.startsWith("1 ti") || s.startsWith("1ti") -> 610
        s.startsWith("2 ti") || s.startsWith("2ti") -> 620
        s.startsWith("tit") -> 630
        s.startsWith("flm") || s.startsWith("phm") || (s.startsWith("fil") && s.contains("m")) -> 640
        s.startsWith("heb") -> 650
        s.startsWith("san") || s.startsWith("jam") -> 660
        s.startsWith("1 p") || s.startsWith("1p") -> 670
        s.startsWith("2 p") || s.startsWith("2p") -> 680
        s.startsWith("1 j") || s.startsWith("1j") -> 690
        s.startsWith("2 j") || s.startsWith("2j") -> 700
        s.startsWith("3 j") || s.startsWith("3j") -> 710
        s.startsWith("jud") -> 720
        s.startsWith("apo") || s.startsWith("rev") -> 730
        else -> -1
    }
}

package com.example.ui.components

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.BibleBooksData
import com.example.data.model.BookInfo
import com.example.ui.theme.AppTheme


@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun PassageSelectorDialog(
    currentStep: Int,
    selectedBook: BookInfo,
    selectedChapter: Int,
    onBookSelected: (BookInfo) -> Unit,
    onChapterSelected: (Int) -> Unit,
    onVerseSelected: (Int) -> Unit,
    onBackStep: () -> Unit,
    onDismiss: () -> Unit
) {
    val appColors = AppTheme.colors

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
            border = if (appColors.isDark) BorderStroke(1.dp, appColors.border) else null,
            tonalElevation = 6.dp
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
                                    contentDescription = "Volver",
                                    tint = appColors.titleColor
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            val headerTitle = when (currentStep) {
                                1 -> "Selecciona Libro"
                                2 -> "${selectedBook.name} • Capítulo"
                                else -> "${selectedBook.name} $selectedChapter • Versículo"
                            }
                            Text(
                                text = headerTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = appColors.titleColor,
                                maxLines = 1,
                                softWrap = false
                            )
                            Text(
                                text = "Paso $currentStep de 3",
                                style = MaterialTheme.typography.bodySmall,
                                color = appColors.textSecondary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("passage_picker_close_button")
                    ) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Cerrar", tint = appColors.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                when (currentStep) {
                    1 -> {
                        // Step 1: Books with AT / NT Tabs
                        var testamentTab by remember { mutableIntStateOf(0) }
                        PrimaryTabRow(
                            selectedTabIndex = testamentTab,
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = appColors.surface
                        ) {
                            Tab(
                                selected = testamentTab == 0,
                                onClick = { testamentTab = 0 },
                                text = {
                                    Text(
                                        text = "Antiguo T. (39)",
                                        fontWeight = if (testamentTab == 0) FontWeight.Bold else FontWeight.Normal,
                                        color = if (testamentTab == 0) appColors.accent else appColors.textSecondary,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            )
                            Tab(
                                selected = testamentTab == 1,
                                onClick = { testamentTab = 1 },
                                text = {
                                    Text(
                                        text = "Nuevo T. (27)",
                                        fontWeight = if (testamentTab == 1) FontWeight.Bold else FontWeight.Normal,
                                        color = if (testamentTab == 1) appColors.accent else appColors.textSecondary,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val filteredBooks = if (testamentTab == 0) {
                            BibleBooksData.books.filter { it.testament == "AT" }
                        } else {
                            BibleBooksData.books.filter { it.testament == "NT" }
                        }

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3), // O GridCells.Adaptive / LazyColumn
                            modifier = Modifier.fillMaxSize(),
                            // AGREGAR O AJUSTAR ESTE PADDING INFERIOR:
                            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 88.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredBooks) { book ->
                                val isSelected = book.number == selectedBook.number
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(64.dp)
                                        .clickable { onBookSelected(book) }
                                        .testTag("book_card_${book.number}"),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) appColors.pillBg else appColors.surfaceVariant
                                    ),
                                    border = if (appColors.isDark && !isSelected) BorderStroke(1.dp, appColors.border) else null,
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(6.dp),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = book.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isSelected) appColors.pillText else appColors.textPrimary,
                                            maxLines = 1,
                                            softWrap = false,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = "${book.chapterCount} cap.",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) appColors.pillText.copy(alpha = 0.8f) else appColors.textSecondary,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // Step 2: Chapters Numeric Grid
                        Text(
                            text = "Capítulos disponibles en ${selectedBook.name}:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = appColors.textSecondary,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        val chapterList = (1..selectedBook.chapterCount).toList()

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(5),
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("passage_chapters_grid"),
                            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 88.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(chapterList) { chap ->
                                val isSelected = chap == selectedChapter
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) appColors.pillBg
                                            else appColors.surfaceVariant
                                        )
                                        .clickable { onChapterSelected(chap) }
                                        .testTag("chapter_button_$chap"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$chap",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) appColors.pillText else appColors.textPrimary,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }

                    3 -> {
                        // Paso 3: Cuadrícula Numérica de Versículos Dinámica
                        val context = LocalContext.current
                        val bookTitle = selectedBook?.name ?: ""

                        Text(
                            text = "Versículos en $bookTitle $selectedChapter:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = appColors.textSecondary,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Consulta SQLite directa y recordada para este libro y capítulo
                        val verseCount = remember(bookTitle, selectedChapter) {
                            getRealVerseCount(context, bookTitle, selectedChapter)
                        }
                        val verseList = remember(verseCount) { (1..verseCount).toList() }

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
                                count = verseList.size,
                                key = { index -> "picker_v_${verseList[index]}" }
                            ) { index ->
                                val v = verseList[index]
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(appColors.surfaceVariant)
                                        .clickable { onVerseSelected(v) }
                                        .testTag("verse_button_$v"),
                                    contentAlignment = androidx.compose.ui.BiasAlignment(0f, 0f)
                                ) {
                                    Text(
                                        text = "$v",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = appColors.titleColor,
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

/**
 * Consulta en tiempo real a la base de datos física SQLite (RVR1960 / KJV)
 * el total exacto de versículos que contiene el capítulo solicitado.
 */
private fun getRealVerseCount(context: Context, bookName: String, chapter: Int): Int {
    val cleanName = bookName.trim()
    var count = 0

    // 1. Obtener la ruta de la base de datos física extraída en el dispositivo
    val dbFile = context.getDatabasePath("RVR1960.SQLite3").takeIf { it.exists() }
        ?: context.getDatabasePath("KJV.SQLite3").takeIf { it.exists() }

    if (dbFile != null) {
        var db: SQLiteDatabase? = null
        try {
            db = SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READONLY)

            // 2. Mapear el nombre del libro a su book_number canónico (10 a 730)
            var bookNumber = getCanonicalBookNumber(cleanName)

            // Si no se reconoció por prefijo, consultar la tabla 'books' de SQLite
            if (bookNumber == -1) {
                db.rawQuery(
                    "SELECT book_number FROM books WHERE long_name = ? OR short_name = ? LIMIT 1",
                    arrayOf(cleanName, cleanName)
                ).use { cursor ->
                    if (cursor.moveToFirst()) {
                        bookNumber = cursor.getInt(0)
                    }
                }
            }

            // 3. Consultar el número máximo de versículos para ese capítulo
            if (bookNumber != -1) {
                db.rawQuery(
                    "SELECT MAX(verse) FROM verses WHERE book_number = ? AND chapter = ?",
                    arrayOf(bookNumber.toString(), chapter.toString())
                ).use { cursor ->
                    if (cursor.moveToFirst()) {
                        count = cursor.getInt(0)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("BIBLE_DEBUG", "Error consultando total de versículos para $cleanName $chapter", e)
        } finally {
            db?.close()
        }
    }

    // 4. Si la base de datos entregó el conteo real, retornarlo; si no, contingencia segura
    return if (count > 0) count else 30
}

/**
 * Mapeo canónico internacional MyBible de los 66 libros a su book_number numérico.
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
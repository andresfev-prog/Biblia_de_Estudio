package com.example.ui.screens.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BibleVerse
import com.example.ui.viewmodel.BibleUiState
import com.example.ui.viewmodel.BibleViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    uiState: BibleUiState,
    viewModel: BibleViewModel,
    onNavigateToVerse: (String, Int, Int) -> Unit = { book, chapter, verse ->
        viewModel.navigateToPassage(book, chapter, verse)
    },
    modifier: Modifier = Modifier
) {
    val isEnglish = uiState.userPreferences.interfaceLanguage.contains("English", ignoreCase = true)
    val hasQuery = uiState.searchQuery.trim().length >= 2
    var isVersionMenuOpen by remember { mutableStateOf(false) }
    val availableVersions = listOf("RVR1960", "KJV")
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 8.dp)
    ) {
        // =========================================================================
        // 1. BARRA SUPERIOR: CAMPO DE BÚSQUEDA + SELECTOR DE VERSIÓN
        // =========================================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("search_text_field"),
                placeholder = {
                    Text(
                        text = if (isEnglish) "Search word..." else "Buscar palabra...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        maxLines = 1
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Borrar texto",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        keyboardController?.hide()
                        viewModel.submitSearch(uiState.searchQuery)
                    }
                ),
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Selector de versión
            Box {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { isVersionMenuOpen = true }
                        .testTag("search_version_selector")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = uiState.searchVersion,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            softWrap = false
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Cambiar versión",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                DropdownMenu(
                    expanded = isVersionMenuOpen,
                    onDismissRequest = { isVersionMenuOpen = false }
                ) {
                    availableVersions.forEach { version ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = version,
                                    fontWeight = if (uiState.searchVersion == version) FontWeight.Bold else FontWeight.Normal,
                                    color = if (uiState.searchVersion == version) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            },
                            onClick = {
                                isVersionMenuOpen = false
                                viewModel.setSearchVersion(version)
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // =========================================================================
        // 2. FILTRO DE ÁMBITO (TODOS / AT / NT) + TOGGLE DE PALABRA EXACTA
        // =========================================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SearchScopeFilterBar(
                currentScope = uiState.searchScope,
                countAll = uiState.searchCountAll,
                countOt = uiState.searchCountOt,
                countNt = uiState.searchCountNt,
                showCount = hasQuery,
                onScopeSelected = { viewModel.setSearchScope(it) },
                isEnglish = isEnglish,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Chip conmutador de Palabra Exacta
            FilterChip(
                selected = uiState.searchExactWord,
                onClick = { viewModel.toggleSearchExactWord() },
                label = {
                    Text(
                        text = if (isEnglish) "Exact" else "Exacta",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                leadingIcon = if (uiState.searchExactWord) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                } else null,
                shape = CircleShape,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                border = BorderStroke(
                    0.8.dp,
                    if (uiState.searchExactWord) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier.height(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), thickness = 0.8.dp)

        // =========================================================================
        // 3. CONTENEDOR DE RESULTADOS / HISTORIAL / ESTADOS VACÍOS
        // =========================================================================
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isSearching -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(36.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp
                    )
                }

                hasQuery && uiState.searchResults.isEmpty() -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        Text(
                            text = if (isEnglish) {
                                "No matches found in ${uiState.searchVersion}."
                            } else {
                                "No se encontraron coincidencias en ${uiState.searchVersion}."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (uiState.searchExactWord) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isEnglish) {
                                    "Try unchecking 'Exact' to broaden your search."
                                } else {
                                    "Prueba desactivando 'Exacta' para ampliar la búsqueda."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                !hasQuery -> {
                    // Vista previa: Historial de búsquedas o instrucción
                    if (uiState.searchHistory.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isEnglish) "Recent searches" else "Búsquedas recientes",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                TextButton(onClick = { viewModel.clearSearchHistory() }) {
                                    Text(
                                        text = if (isEnglish) "Clear" else "Borrar",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                uiState.searchHistory.forEach { item ->
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .clickable {
                                                viewModel.onSearchQueryChanged(item.query)
                                            }
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.History,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = item.query,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Text(
                            text = if (isEnglish) {
                                "Type at least 2 letters to search."
                            } else {
                                "Escribe al menos 2 letras para buscar."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = uiState.searchResults,
                            key = { verse -> "${verse.bookName}_${verse.chapter}_${verse.verse}" }
                        ) { verse ->
                            SearchResultItem(
                                verse = verse,
                                searchQuery = uiState.searchQuery,
                                exactWord = uiState.searchExactWord,
                                onClick = {
                                    onNavigateToVerse(verse.bookName, verse.chapter, verse.verse)
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
private fun SearchScopeFilterBar(
    currentScope: String,
    countAll: Int,
    countOt: Int,
    countNt: Int,
    showCount: Boolean,
    onScopeSelected: (String) -> Unit,
    isEnglish: Boolean,
    modifier: Modifier = Modifier
) {
    val filters = listOf(
        Triple("TODOS", if (isEnglish) "All" else "Todos", countAll),
        Triple("AT", if (isEnglish) "OT" else "AT", countOt),
        Triple("NT", if (isEnglish) "NT" else "NT", countNt)
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        filters.forEach { (scopeKey, baseLabel, count) ->
            val isSelected = currentScope.equals(scopeKey, ignoreCase = true)
            val labelText = if (showCount) "$baseLabel ($count)" else baseLabel

            Surface(
                shape = CircleShape,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                modifier = Modifier
                    .weight(1f)
                    .clip(CircleShape)
                    .clickable { onScopeSelected(scopeKey) }
                    .testTag("scope_${scopeKey.lowercase()}")
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = labelText,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = if (showCount) 10.sp else 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchResultItem(
    verse: BibleVerse,
    searchQuery: String,
    exactWord: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val highlightedText = remember(verse.text, searchQuery, exactWord) {
        buildHighlightedVerseText(
            fullText = verse.text,
            query = searchQuery.trim(),
            exactWord = exactWord
        )
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "${verse.bookName} ${verse.chapter}:${verse.verse}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = highlightedText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp
            )
        }
    }
}

/**
 * Resalta en negrita y color primario las apariciones del término buscado dentro del versículo.
 */
private fun buildHighlightedVerseText(
    fullText: String,
    query: String,
    exactWord: Boolean
): AnnotatedString {
    if (query.isBlank()) return AnnotatedString(fullText)

    val pattern = if (exactWord) {
        Regex("(?i)(?<!\\p{L})" + Regex.escape(query) + "(?!\\p{L})")
    } else {
        Regex("(?i)" + Regex.escape(query))
    }

    val matches = pattern.findAll(fullText)
    if (!matches.any()) return AnnotatedString(fullText)

    return buildAnnotatedString {
        var lastIndex = 0
        matches.forEach { match ->
            val start = match.range.first
            val end = match.range.last + 1

            if (start > lastIndex) {
                append(fullText.substring(lastIndex, start))
            }

            pushStyle(
                SpanStyle(
                    fontWeight = FontWeight.ExtraBold,
                    color = androidx.compose.ui.graphics.Color(0xFF0284C7)
                )
            )
            append(fullText.substring(start, end))
            pop()

            lastIndex = end
        }

        if (lastIndex < fullText.length) {
            append(fullText.substring(lastIndex))
        }
    }
}
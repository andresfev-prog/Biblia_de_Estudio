package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.database.BibleDatabase
import com.example.data.repository.BibleRepository
import com.example.ui.components.OnboardingDialog
import com.example.ui.screens.bible.BibleScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.modules.ModulesScreen
import com.example.ui.screens.search.SearchScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.theme.AppTheme
import com.example.ui.theme.BibleStudyTheme
import com.example.ui.util.StringsLocale
import com.example.ui.viewmodel.AppDestination
import com.example.ui.viewmodel.BibleViewModel
import com.example.ui.viewmodel.BibleViewModelFactory
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ViewModule
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.clip


class MainActivity : ComponentActivity() {

    private lateinit var viewModel: BibleViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val applicationScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob())
        val database = BibleDatabase.getDatabase(applicationContext, applicationScope)
        val repository = BibleRepository(this, database.bibleDao())
        val factory = BibleViewModelFactory(application, repository)
        viewModel = ViewModelProvider(this, factory)[BibleViewModel::class.java]

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val snackbarHostState = remember { SnackbarHostState() }
            val lang = uiState.userPreferences.interfaceLanguage

            // Toast / Snackbar listener
            LaunchedEffect(uiState.toastMessage) {
                uiState.toastMessage?.let { msg ->
                    snackbarHostState.showSnackbar(msg)
                    viewModel.clearToast()
                }
            }

            // Root dynamic theme switcher
            BibleStudyTheme(themeMode = uiState.userPreferences.themeMode) {
                val appColors = AppTheme.colors

                if (uiState.currentDestination != AppDestination.HOME) {
                    BackHandler {
                        viewModel.navigateTo(AppDestination.HOME)
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = appColors.background,
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    bottomBar = {
                        AnimatedVisibility(
                            visible = uiState.isBottomBarVisible || uiState.currentDestination != AppDestination.BIBLE,
                            enter = slideInVertically(initialOffsetY = { it }),
                            exit = slideOutVertically(targetOffsetY = { it })
                        ) {
                            BottomNavBar(
                                currentDestination = uiState.currentDestination,
                                interfaceLanguage = lang,
                                onDestinationSelected = { dest ->
                                    viewModel.navigateTo(dest)
                                    viewModel.setBottomBarVisible(true)
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    when (uiState.currentDestination) {
                        AppDestination.HOME -> {
                            HomeScreen(
                                uiState = uiState,
                                viewModel = viewModel,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        AppDestination.BIBLE -> {
                            BibleScreen(
                                uiState = uiState,
                                viewModel = viewModel,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        AppDestination.MODULES -> {
                            ModulesScreen(
                                uiState = uiState,
                                viewModel = viewModel,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        AppDestination.SEARCH -> {
                            SearchScreen(
                                uiState = uiState,
                                viewModel = viewModel,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        AppDestination.SETTINGS -> {
                            SettingsScreen(
                                uiState = uiState,
                                viewModel = viewModel,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }

                // First-launch 3-step Onboarding Tutorial
                // Tutorial desactivado temporalmente

            }
        }
    }
}

@Composable
fun BibleClosedCrossIcon(
    tint: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height

        // 1. Cubierta del libro cerrado con bordes suaves
        drawRoundRect(
            color = tint,
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.16f, h * 0.10f),
            size = androidx.compose.ui.geometry.Size(w * 0.68f, h * 0.80f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.10f, w * 0.10f)
        )

        // 2. Detalle lateral de las páginas
        drawRoundRect(
            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.40f),
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.74f, h * 0.16f),
            size = androidx.compose.ui.geometry.Size(w * 0.07f, h * 0.68f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.03f, w * 0.03f)
        )

        // 3. Cruz en la portada
        val crossColor = androidx.compose.ui.graphics.Color.White
        val strokeW = w * 0.075f

        // Línea vertical
        drawLine(
            color = crossColor,
            start = androidx.compose.ui.geometry.Offset(w * 0.45f, h * 0.28f),
            end = androidx.compose.ui.geometry.Offset(w * 0.45f, h * 0.64f),
            strokeWidth = strokeW,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )

        // Línea horizontal
        drawLine(
            color = crossColor,
            start = androidx.compose.ui.geometry.Offset(w * 0.32f, h * 0.40f),
            end = androidx.compose.ui.geometry.Offset(w * 0.58f, h * 0.40f),
            strokeWidth = strokeW,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}

@Composable
fun BottomNavBar(
    currentDestination: AppDestination,
    interfaceLanguage: String,
    onDestinationSelected: (AppDestination) -> Unit
) {
    val appColors = AppTheme.colors
    val navShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(navShape)
            .border(
                width = 1.dp,
                color = appColors.divider.copy(alpha = if (appColors.isDark) 0.6f else 0.85f),
                shape = navShape
            ),
        color = appColors.background, // Mismo fondo de la app en modo claro y oscuro
        tonalElevation = 0.dp
    ) {
        NavigationBar(
            modifier = Modifier.testTag("main_bottom_nav_bar"),
            containerColor = appColors.background,
            tonalElevation = 0.dp
        ) {
            val navColors = NavigationBarItemDefaults.colors(
                selectedIconColor = appColors.accent,
                selectedTextColor = appColors.accent,
                unselectedIconColor = appColors.textSecondary,
                unselectedTextColor = appColors.textSecondary,
                indicatorColor = androidx.compose.ui.graphics.Color.Transparent
            )

            // Lista con íconos redondeados
            val navItems = listOf(
                Triple(AppDestination.HOME, "nav_home", Icons.Rounded.Home),
                Triple(AppDestination.BIBLE, "nav_bible", Icons.Rounded.Home), // Se reemplaza por el ícono vectorial exclusivo abajo
                Triple(AppDestination.MODULES, "nav_modules", Icons.Rounded.ViewModule),
                Triple(AppDestination.SEARCH, "nav_search", Icons.Rounded.Search),
                Triple(AppDestination.SETTINGS, "nav_settings", Icons.Rounded.Settings)
            )

            navItems.forEach { (destination, stringKey, icon) ->
                val isSelected = currentDestination == destination
                val labelText = StringsLocale.get(stringKey, interfaceLanguage)

                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onDestinationSelected(destination) },
                    icon = {
                        if (destination == AppDestination.BIBLE) {
                            // Dibuja el libro cerrado con la cruz
                            BibleClosedCrossIcon(
                                tint = if (isSelected) appColors.accent else appColors.textSecondary
                            )
                        } else {
                            Icon(imageVector = icon, contentDescription = labelText)
                        }
                    },
                    label = {
                        Text(
                            text = labelText,
                            fontSize = 10.sp,
                            maxLines = 1,
                            softWrap = false,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = navColors,
                    modifier = Modifier.testTag("nav_item_${destination.name.lowercase()}")
                )
            }
        }
    }
}


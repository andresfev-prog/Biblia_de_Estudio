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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import com.example.ui.theme.AppIcon
import com.example.ui.theme.AppIcons
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.ripple
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow


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
fun BottomNavBar(
    currentDestination: AppDestination,
    interfaceLanguage: String,
    onDestinationSelected: (AppDestination) -> Unit
) {
    val appColors = AppTheme.colors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 24.dp, end = 24.dp, bottom = 20.dp)
            .testTag("main_bottom_nav_bar"),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(
                    elevation = 10.dp,
                    shape = RoundedCornerShape(32.dp),
                    spotColor = Color.Black.copy(alpha = if (appColors.isDark) 0.5f else 0.15f)
                ),
            shape = RoundedCornerShape(32.dp),
            color = appColors.cardBg,
            tonalElevation = 6.dp,
            border = BorderStroke(
                width = 1.dp,
                color = appColors.divider.copy(alpha = if (appColors.isDark) 0.35f else 0.7f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val navItems = listOf(
                    Pair(AppDestination.HOME, AppIcons.Home),
                    Pair(AppDestination.BIBLE, AppIcons.Bible),
                    Pair(AppDestination.MODULES, AppIcons.Modules),
                    Pair(AppDestination.SEARCH, AppIcons.Search),
                    Pair(AppDestination.SETTINGS, AppIcons.Settings)
                )

                navItems.forEach { (destination, glyph) ->
                    val isSelected = currentDestination == destination
                    val label = destination.name.lowercase()

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = false, radius = 28.dp)
                            ) {
                                onDestinationSelected(destination)
                            }
                            .testTag("nav_item_$label"),
                        contentAlignment = Alignment.Center
                    ) {
                        AppIcon(
                            glyph = glyph,
                            contentDescription = label,
                            tint = if (isSelected) appColors.accent else appColors.textSecondary.copy(alpha = 0.65f),
                            fontSize = 24.sp,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}



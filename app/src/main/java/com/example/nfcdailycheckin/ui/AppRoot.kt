package com.example.nfcdailycheckin.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.nfcdailycheckin.ui.screens.AddEditTaskScreen
import com.example.nfcdailycheckin.ui.screens.HistoryScreen
import com.example.nfcdailycheckin.ui.screens.HomeScreen
import com.example.nfcdailycheckin.ui.screens.SettingsScreen
import com.example.nfcdailycheckin.ui.screens.TechnicalNamesScreen
import com.example.nfcdailycheckin.ui.theme.ThemeMode

private object Routes {
    const val HOME = "home"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val ADD = "add"
    const val TECH = "tech"
}

private data class TabItem(val route: String, val label: String, val icon: @Composable () -> Unit)

@Composable
fun AppRoot(
    viewModel: HomeViewModel,
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit
) {
    val nav = rememberNavController()

    val tabs = listOf(
        TabItem(Routes.HOME, "Home") { Icon(Icons.Default.Home, contentDescription = "Home") },
        TabItem(Routes.HISTORY, "Verlauf") { Icon(Icons.Default.DateRange, contentDescription = "Verlauf") },
        TabItem(Routes.SETTINGS, "Settings") { Icon(Icons.Default.Settings, contentDescription = "Settings") }
    )

    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    // Untere Leiste nur auf den Haupt-Tabs zeigen, nicht auf Add/Tech-Unterseiten.
    val showBottomBar = currentRoute in listOf(Routes.HOME, Routes.HISTORY, Routes.SETTINGS)

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = Color.Transparent) {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                if (currentRoute != tab.route) {
                                    nav.navigate(tab.route) {
                                        popUpTo(Routes.HOME) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = tab.icon,
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = nav,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    stateFlow = viewModel.state,
                    events = viewModel.events,
                    onAdd = { nav.navigate(Routes.ADD) },
                    onOpenTechNames = { nav.navigate(Routes.TECH) },
                    onOpenSettings = { nav.navigate(Routes.SETTINGS) },
                    onMenuResetToday = { viewModel.resetSelectedToday(it) },
                    onMenuDelete = { viewModel.deleteTask(it) }
                )
            }
            composable(Routes.HISTORY) {
                HistoryScreen(
                    stateFlow = viewModel.state
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    themeMode = themeMode,
                    onThemeChange = onThemeChange,
                    onBack = { nav.navigate(Routes.HOME) }
                )
            }
            composable(Routes.ADD) {
                AddEditTaskScreen(
                    onSave = { draft ->
                        viewModel.saveTask(draft)
                        nav.popBackStack()
                    },
                    onCancel = { nav.popBackStack() }
                )
            }
            composable(Routes.TECH) {
                val state by viewModel.state.collectAsState()
                TechnicalNamesScreen(
                    tasks = state.tasks,
                    onBack = { nav.popBackStack() }
                )
            }
        }
    }
}
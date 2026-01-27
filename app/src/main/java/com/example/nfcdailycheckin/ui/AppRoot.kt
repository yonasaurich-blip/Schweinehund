package com.example.nfcdailycheckin.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.nfcdailycheckin.ui.screens.AddEditTaskScreen
import com.example.nfcdailycheckin.ui.screens.HomeScreen
import com.example.nfcdailycheckin.ui.screens.TechnicalNamesScreen

private object Routes {
    const val HOME = "home"
    const val ADD = "add"
    const val TECH = "tech"
}

@Composable
fun AppRoot(viewModel: HomeViewModel) {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                stateFlow = viewModel.state,
                events = viewModel.events,
                onAdd = { nav.navigate(Routes.ADD) },
                onOpenTechNames = { nav.navigate(Routes.TECH) },
                onMenuResetToday = { viewModel.resetSelectedToday(it) },
                onMenuDelete = { viewModel.deleteTask(it) }
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

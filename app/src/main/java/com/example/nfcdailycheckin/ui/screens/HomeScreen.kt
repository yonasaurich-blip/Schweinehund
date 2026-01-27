package com.example.nfcdailycheckin.ui.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nfcdailycheckin.data.ScheduleType
import com.example.nfcdailycheckin.data.TaskEntity
import com.example.nfcdailycheckin.data.TaskRules
import com.example.nfcdailycheckin.ui.*
import com.example.nfcdailycheckin.ui.theme.lavenderCardColor
import com.example.nfcdailycheckin.util.DateTime
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.format.TextStyle
import java.util.Locale

private const val PREFS = "schweinehund_prefs"
private const val KEY_ONBOARDING_SHOWN = "onboarding_shown"

private enum class TaskListMode { TODAY, ALL }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    stateFlow: StateFlow<HomeState>,
    events: SharedFlow<HomeEvent>,
    onAdd: () -> Unit,
    onOpenTechNames: () -> Unit,
    onMenuResetToday: (Long) -> Unit,
    onMenuDelete: (Long) -> Unit,
) {
    val state by stateFlow.collectAsState()
    val snackHost = remember { SnackbarHostState() }
    val ctx = LocalContext.current

    var mode by rememberSaveable { mutableStateOf(TaskListMode.TODAY) }
    var showOnboarding by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_ONBOARDING_SHOWN, false)) {
            showOnboarding = true
        }

        events.collect {
            if (it is HomeEvent.Snack) snackHost.showSnackbar(it.message)
        }
    }

    if (showOnboarding) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("🐗 Schweinehund-Modus") },
            text = {
                Text(
                    "Aufgaben können NICHT manuell erledigt werden.\n\n" +
                            "Nur ein NFC-Scan besiegt den Schweinehund.\n\n" +
                            "Mach es richtig – dann scanne. 💪"
                )
            },
            confirmButton = {
                Button(onClick = {
                    ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                        .edit().putBoolean(KEY_ONBOARDING_SHOWN, true).apply()
                    showOnboarding = false
                }) { Text("Los geht’s 🚀") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Schweinehund") },
                actions = {
                    IconButton(onClick = onAdd) { Icon(Icons.Default.Add, null) }
                    IconButton(onClick = onOpenTechNames) { Icon(Icons.Default.Info, null) }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackHost) }
    ) { pad ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item {
                StatsCard(
                    todayDone = state.todayDone,
                    todayTotal = state.todayTotal,
                    streak = state.streak,
                    days = state.days
                )
            }

            item {
                ModeToggle(mode = mode, onChange = { mode = it })
            }

            item {
                Text(
                    if (mode == TaskListMode.TODAY) "Heute" else "Alle Aufgaben",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            val today = DateTime.today()

            when (mode) {
                TaskListMode.TODAY -> {
                    items(state.uiTasksToday, key = { it.id }) { t ->
                        TaskCard(
                            task = t,
                            subtitle = "⏰ ${t.reminder}",
                            isDimmed = false,
                            onResetToday = { onMenuResetToday(t.id) },
                            onDelete = { onMenuDelete(t.id) }
                        )
                    }
                }

                TaskListMode.ALL -> {
                    items(state.tasks.filter { it.isActive }) { t ->
                        val dueToday = TaskRules.isDueOn(t, today)
                        val hh = TaskRules.reminderHour(t).toString().padStart(2, '0')
                        val mm = TaskRules.reminderMinute(t).toString().padStart(2, '0')

                        TaskCard(
                            task = UiTask(
                                id = t.id,
                                title = t.title,
                                reminder = "$hh:$mm",
                                isDoneToday = false,
                                nfcText = t.nfcText
                            ),
                            subtitle = if (dueToday) "Heute fällig" else labelFor(t),
                            isDimmed = !dueToday,
                            onResetToday = {},
                            onDelete = { onMenuDelete(t.id) }
                        )
                    }
                }
            }
        }
    }
}

/* ---------------- Sub-Composables ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModeToggle(mode: TaskListMode, onChange: (TaskListMode) -> Unit) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        SegmentedButton(
            selected = mode == TaskListMode.TODAY,
            onClick = { onChange(TaskListMode.TODAY) }
        ) { Text("Heute") }

        SegmentedButton(
            selected = mode == TaskListMode.ALL,
            onClick = { onChange(TaskListMode.ALL) }
        ) { Text("Alle") }
    }
}

private fun labelFor(t: TaskEntity): String =
    when (t.scheduleType) {
        ScheduleType.DAILY -> "Täglich"
        ScheduleType.WEEKLY -> "Wöchentlich"
        ScheduleType.MONTHLY -> "Monatlich"
        ScheduleType.ONCE -> "Einmalig"
    }

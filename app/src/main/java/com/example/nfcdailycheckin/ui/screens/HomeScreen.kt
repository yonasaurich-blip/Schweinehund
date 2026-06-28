package com.example.nfcdailycheckin.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nfcdailycheckin.ui.DayStatus
import com.example.nfcdailycheckin.ui.HomeEvent
import com.example.nfcdailycheckin.ui.HomeState
import com.example.nfcdailycheckin.ui.UiTask
import com.example.nfcdailycheckin.ui.theme.lavenderCardColor
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.format.TextStyle
import java.util.Locale

private const val PREFS = "schweinehund_prefs"
private const val KEY_ONBOARDING_SHOWN = "onboarding_shown"

private enum class TaskFilter { TODAY, ALL }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    stateFlow: StateFlow<HomeState>,
    events: SharedFlow<HomeEvent>,
    onAdd: () -> Unit,
    onOpenTechNames: () -> Unit,
    onOpenSettings: () -> Unit,
    onMenuResetToday: (Long) -> Unit,
    onMenuDelete: (Long) -> Unit,
) {
    val state by stateFlow.collectAsState()
    val snackHost = remember { SnackbarHostState() }

    val ctx = LocalContext.current
    var showOnboarding by remember { mutableStateOf(false) }

    // Filter: Heute / Alle
    var filter by remember { mutableStateOf(TaskFilter.TODAY) }

    LaunchedEffect(Unit) {
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_ONBOARDING_SHOWN, false)) showOnboarding = true

        events.collect { ev ->
            when (ev) {
                is HomeEvent.Snack -> snackHost.showSnackbar(ev.message)
            }
        }
    }

    if (showOnboarding) {
        AlertDialog(
            onDismissRequest = { /* nicht wegklickbar */ },
            title = { Text("🐗 Schweinehund-Modus") },
            text = {
                Text(
                    "Willkommen! 💪\n\n" +
                            "In dieser App wird nichts manuell abgehakt.\n" +
                            "✅ Aufgaben gelten nur als erledigt, wenn du den passenden NFC-Tag scannst.\n\n" +
                            "So überlistest du den inneren Schweinehund. 🧠⚡"
                )
            },
            confirmButton = {
                Button(onClick = {
                    ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                        .edit()
                        .putBoolean(KEY_ONBOARDING_SHOWN, true)
                        .apply()
                    showOnboarding = false
                }) { Text("Los geht’s 🚀") }
            }
        )
    }

    // ✅ Mo–So: wir zeigen nur 7 Tage. (Du lieferst days bereits, wir nehmen die letzten 7)
    val weekDays: List<DayStatus> = remember(state.days) {
        if (state.days.size <= 7) state.days else state.days.takeLast(7)
    }

    val tasksToShow: List<UiTask> = remember(filter, state.uiTasksToday, state.tasks) {
        when (filter) {
            TaskFilter.TODAY -> state.uiTasksToday
            TaskFilter.ALL -> {
                // ALL = alle aktiven Tasks, sortiert nach Titel
                state.tasks
                    .filter { it.isActive }
                    .sortedBy { it.title.lowercase(Locale.getDefault()) }
                    .map { t ->
                        val hh = (t.reminderTimeMinutes / 60).toString().padStart(2, '0')
                        val mm = (t.reminderTimeMinutes % 60).toString().padStart(2, '0')
                        UiTask(
                            id = t.id,
                            title = t.title,
                            reminder = "$hh:$mm",
                            isDoneToday = state.uiTasksToday.firstOrNull { it.id == t.id }?.isDoneToday == true,
                            nfcText = t.nfcText
                        )
                    }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Check-in") },
                actions = {
                    IconButton(onClick = onAdd) { Icon(Icons.Default.Add, contentDescription = "Hinzufügen") }
                    IconButton(onClick = onOpenTechNames) { Icon(Icons.Default.Info, contentDescription = "Info") }
                    IconButton(onClick = onOpenSettings) { Icon(Icons.Default.Settings, contentDescription = "Einstellungen") }
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
                    days = weekDays
                )
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Heute",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.weight(1f))

                    SingleChoiceSegmentedButtonRow {
                        SegmentedButton(
                            selected = filter == TaskFilter.TODAY,
                            onClick = { filter = TaskFilter.TODAY },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                            label = { Text("Heute") }
                        )
                        SegmentedButton(
                            selected = filter == TaskFilter.ALL,
                            onClick = { filter = TaskFilter.ALL },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                            label = { Text("Alle") }
                        )
                    }
                }
            }

            items(tasksToShow, key = { it.id }) { t ->
                TaskCard(
                    task = t,
                    onResetToday = { onMenuResetToday(t.id) },
                    onDelete = { onMenuDelete(t.id) }
                )
            }

            item { Spacer(modifier = Modifier.height(6.dp)) }
        }
    }
}

@Composable
private fun StatsCard(
    todayDone: Int,
    todayTotal: Int,
    streak: Int,
    days: List<DayStatus>
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Stats 🐗", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.weight(1f))
                Text("$todayDone/$todayTotal done", style = MaterialTheme.typography.labelLarge)
            }

            Text("Streak 🔥 $streak", style = MaterialTheme.typography.bodyLarge)

            Text("Übersicht (Mo–So)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)

            // ✅ Box + Label pro Tag, dadurch sind Labels immer korrekt unter der Box zentriert
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                days.forEach { d ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        DayBox(d)
                        val label = d.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.GERMAN).take(2)
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(28.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayBox(day: DayStatus) {
    val bg = when {
        day.isFuture -> MaterialTheme.colorScheme.surfaceVariant
        day.isFulfilled -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.errorContainer
    }
    val text = when {
        day.isFuture -> ""
        day.isFulfilled -> "✓"
        else -> "✕"
    }
    Box(
        modifier = Modifier
            .size(28.dp)
            .background(bg, shape = MaterialTheme.shapes.small),
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun TaskCard(
    task: UiTask,
    onResetToday: () -> Unit,
    onDelete: () -> Unit,
) {
    var menu by remember { mutableStateOf(false) }
    var showTech by remember { mutableStateOf(false) }

    if (showTech) {
        AlertDialog(
            onDismissRequest = { showTech = false },
            title = { Text("🔧 Technischer Name") },
            text = { Text("nfctext::${task.nfcText}") },
            confirmButton = { TextButton(onClick = { showTech = false }) { Text("OK") } }
        )
    }

    val cardColor = if (task.isDoneToday) lavenderCardColor() else MaterialTheme.colorScheme.surface

    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(task.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusPill(isDone = task.isDoneToday)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Reminder: ${task.reminder}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.weight(1f))

                Box {
                    IconButton(onClick = { menu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menü")
                    }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        DropdownMenuItem(
                            text = { Text("Technischer Name anzeigen") },
                            onClick = { menu = false; showTech = true }
                        )
                        DropdownMenuItem(
                            text = { Text("Zurücksetzen (heute)") },
                            onClick = { menu = false; onResetToday() }
                        )
                        DropdownMenuItem(
                            text = { Text("Löschen") },
                            onClick = { menu = false; onDelete() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusPill(isDone: Boolean) {
    val container = if (isDone) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer
    val label = if (isDone) "Erledigt" else "Offen"
    Surface(color = container, shape = MaterialTheme.shapes.large) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(if (isDone) "✓" else "○", style = MaterialTheme.typography.labelLarge)
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

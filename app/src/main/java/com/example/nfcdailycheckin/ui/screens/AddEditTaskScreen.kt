package com.example.nfcdailycheckin.ui.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.nfcdailycheckin.data.ScheduleType
import com.example.nfcdailycheckin.ui.TaskDraft
import com.example.nfcdailycheckin.ui.theme.slugify
import java.time.DayOfWeek
import java.time.LocalTime
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskScreen(
    onSave: (TaskDraft) -> Unit,
    onCancel: () -> Unit,
) {
    val ctx = LocalContext.current

    var title by remember { mutableStateOf("") }
    var scheduleType by remember { mutableStateOf(ScheduleType.DAILY) }
    var weeklyDay by remember { mutableStateOf(1) }
    var monthlyDay by remember { mutableStateOf("5") }
    var onceDateIso by remember { mutableStateOf("2026-03-05") }

    // Reminder als echte Time-Values
    var reminderHour by remember { mutableStateOf(20) }
    var reminderMinute by remember { mutableStateOf(30) }

    var nfcText by remember { mutableStateOf("") }
    var scheduleMenu by remember { mutableStateOf(false) }
    var weekdayMenu by remember { mutableStateOf(false) }

    LaunchedEffect(title) {
        if (nfcText.isBlank() && title.isNotBlank()) {
            nfcText = slugify(title)
        }
    }

    fun minutesOfDay(h: Int, m: Int) = h * 60 + m
    val minutes = minutesOfDay(reminderHour, reminderMinute)

    val timeLabel = remember(reminderHour, reminderMinute) {
        "%02d:%02d".format(Locale.GERMAN, reminderHour, reminderMinute)
    }

    val canSave = title.trim().isNotEmpty() && nfcText.trim().isNotEmpty()

    val timePicker = remember(reminderHour, reminderMinute) {
        TimePickerDialog(
            ctx,
            { _, h, m ->
                reminderHour = h
                reminderMinute = m
            },
            reminderHour,
            reminderMinute,
            true
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Aufgabe hinzufügen") },
                navigationIcon = {
                    IconButton(onClick = onCancel) { Icon(Icons.Default.Close, contentDescription = "Abbrechen") }
                },
                actions = {
                    IconButton(onClick = {
                        if (!canSave) return@IconButton
                        val draft = TaskDraft(
                            title = title.trim(),
                            scheduleType = scheduleType,
                            weeklyDay = if (scheduleType == ScheduleType.WEEKLY) weeklyDay else null,
                            monthlyDay = if (scheduleType == ScheduleType.MONTHLY) monthlyDay.toIntOrNull() else null,
                            onceDateIso = if (scheduleType == ScheduleType.ONCE) onceDateIso.trim() else null,
                            reminderMinutes = minutes,
                            nfcText = nfcText.trim(),
                        )
                        onSave(draft)
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Speichern")
                    }
                }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = scheduleMenu,
                onExpandedChange = { scheduleMenu = !scheduleMenu }
            ) {
                OutlinedTextField(
                    value = when (scheduleType) {
                        ScheduleType.DAILY -> "Täglich"
                        ScheduleType.WEEKLY -> "Wöchentlich"
                        ScheduleType.MONTHLY -> "Monatlich"
                        ScheduleType.ONCE -> "Einmalig"
                    },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Wiederholung") },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = scheduleMenu, onDismissRequest = { scheduleMenu = false }) {
                    DropdownMenuItem(text = { Text("Täglich") }, onClick = { scheduleType = ScheduleType.DAILY; scheduleMenu = false })
                    DropdownMenuItem(text = { Text("Wöchentlich") }, onClick = { scheduleType = ScheduleType.WEEKLY; scheduleMenu = false })
                    DropdownMenuItem(text = { Text("Monatlich") }, onClick = { scheduleType = ScheduleType.MONTHLY; scheduleMenu = false })
                    DropdownMenuItem(text = { Text("Einmalig") }, onClick = { scheduleType = ScheduleType.ONCE; scheduleMenu = false })
                }
            }

            if (scheduleType == ScheduleType.WEEKLY) {
                ExposedDropdownMenuBox(expanded = weekdayMenu, onExpandedChange = { weekdayMenu = !weekdayMenu }) {
                    val label = DayOfWeek.of(weeklyDay).getDisplayName(java.time.format.TextStyle.FULL, Locale.GERMAN)
                    OutlinedTextField(
                        value = label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Wochentag") },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = weekdayMenu, onDismissRequest = { weekdayMenu = false }) {
                        (1..7).forEach { d ->
                            val txt = DayOfWeek.of(d).getDisplayName(java.time.format.TextStyle.FULL, Locale.GERMAN)
                            DropdownMenuItem(text = { Text(txt) }, onClick = { weeklyDay = d; weekdayMenu = false })
                        }
                    }
                }
            }

            if (scheduleType == ScheduleType.MONTHLY) {
                OutlinedTextField(
                    value = monthlyDay,
                    onValueChange = { monthlyDay = it.filter { ch -> ch.isDigit() }.take(2) },
                    label = { Text("Tag im Monat (1–31)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "Falls der Tag im Monat nicht existiert (z.B. 31. im Februar), wird automatisch der letzte Tag des Monats genutzt.",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (scheduleType == ScheduleType.ONCE) {
                OutlinedTextField(
                    value = onceDateIso,
                    onValueChange = { onceDateIso = it },
                    label = { Text("Datum (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ✅ Uhrzeit per Picker
            OutlinedTextField(
                value = timeLabel,
                onValueChange = {},
                readOnly = true,
                label = { Text("Erinnerung") },
                supportingText = { Text("Tippen, um Uhrzeit auszuwählen.") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    TextButton(onClick = { timePicker.show() }) { Text("⏰") }
                }
            )

            OutlinedTextField(
                value = nfcText,
                onValueChange = { nfcText = it },
                label = { Text("NFC-Text (TEXT-Teil)") },
                supportingText = { Text("Der NFC-Tag muss den NDEF-Text nfctext::$nfcText enthalten.") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (!canSave) return@Button
                    val draft = TaskDraft(
                        title = title.trim(),
                        scheduleType = scheduleType,
                        weeklyDay = if (scheduleType == ScheduleType.WEEKLY) weeklyDay else null,
                        monthlyDay = if (scheduleType == ScheduleType.MONTHLY) monthlyDay.toIntOrNull() else null,
                        onceDateIso = if (scheduleType == ScheduleType.ONCE) onceDateIso.trim() else null,
                        reminderMinutes = minutes,
                        nfcText = nfcText.trim(),
                    )
                    onSave(draft)
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Speichern")
            }
        }
    }
}

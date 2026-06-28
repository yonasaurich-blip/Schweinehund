package com.example.nfcdailycheckin.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nfcdailycheckin.ui.DayStatus
import com.example.nfcdailycheckin.ui.HomeState
import kotlinx.coroutines.flow.StateFlow
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    stateFlow: StateFlow<HomeState>
) {
    val state by stateFlow.collectAsState()

    // Nur vergangene & heutige Tage, neueste zuerst.
    val pastDays = state.days
        .filter { !it.isFuture }
        .reversed()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Verlauf") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { pad ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = "Deine letzten Tage",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            if (pastDays.isEmpty()) {
                item {
                    Text(
                        text = "Noch keine Einträge. Scanne deinen ersten Tag! 🐗",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                items(pastDays) { day ->
                    HistoryDayRow(day)
                }
            }
        }
    }
}

@Composable
private fun HistoryDayRow(day: DayStatus) {
    val fmt = DateTimeFormatter.ofPattern("EEEE, dd.MM.yyyy", Locale.GERMAN)
    val dateLabel = day.date.format(fmt)

    val statusText = if (day.isFulfilled) "Geschafft ✅" else "Offen geblieben ✕"
    val statusColor =
        if (day.isFulfilled) MaterialTheme.colorScheme.tertiaryContainer
        else MaterialTheme.colorScheme.errorContainer

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dateLabel,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
package com.example.nfcdailycheckin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.example.nfcdailycheckin.data.TaskEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechnicalNamesScreen(
    tasks: List<TaskEntity>,
    onBack: () -> Unit,
) {
    val clipboard = LocalClipboardManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Technische Namen") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        }
    ) { pad ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Diese Texte müssen auf dem NFC-Tag als NDEF-Text stehen:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(6.dp))
            }

            items(tasks, key = { it.id }) { t ->
                val full = "nfctext::${t.nfcText}"
                Card(shape = MaterialTheme.shapes.extraLarge) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(t.title, style = MaterialTheme.typography.titleMedium)
                        Text(full, style = MaterialTheme.typography.bodyMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AssistChip(
                                onClick = { clipboard.setText(AnnotatedString(full)) },
                                label = { Text("Kopieren") }
                            )
                        }
                    }
                }
            }
        }
    }
}

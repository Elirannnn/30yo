package com.shas.counter

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp

/** Searchable tractate selector. Only unfinished tractates can be chosen. */
@Composable
fun TractatePicker(progress: List<Int>, onDismiss: () -> Unit, onPick: (Int) -> Unit) {
    var q by remember { mutableStateOf("") }
    val fr = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { fr.requestFocus() } }

    val query = q.trim()
    val items = Shas.list.indices
        .filter { progress[it] < Shas.list[it].dapim && (query.isEmpty() || Shas.list[it].name.contains(query)) }
        .sortedBy { if (progress[it] > 0) 0 else 1 } // tractates in study first

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("ביטול") } },
        title = { Text("איזו מסכת למדת?") },
        text = {
            Column {
                OutlinedTextField(
                    value = q, onValueChange = { q = it }, singleLine = true,
                    placeholder = { Text("חיפוש מסכת…") },
                    modifier = Modifier.fillMaxWidth().focusRequester(fr)
                )
                Spacer(Modifier.height(8.dp))
                if (items.isEmpty()) {
                    Text(if (query.isEmpty()) "סיימת את כל הש״ס! 🎉" else "לא נמצאה מסכת")
                } else {
                    LazyColumn(Modifier.heightIn(max = 320.dp)) {
                        items(items) { i ->
                            val t = Shas.list[i]
                            val done = progress[i]
                            Column(
                                Modifier.fillMaxWidth().clickable { onPick(i) }.padding(vertical = 10.dp)
                            ) {
                                Text(t.name)
                                Text(
                                    if (done > 0) "כרגע: דף ${Shas.hebNum(2 + done)}" else "טרם התחלתי",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    )
}

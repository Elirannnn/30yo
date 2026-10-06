package com.shas.counter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import kotlinx.coroutines.launch
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(Modifier.fillMaxSize()) { Screen() }
                }
            }
        }
    }
}

@Composable
private fun Screen() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val progress by Store.flow(ctx).collectAsState(initial = Shas.initial)
    var today by remember { mutableStateOf(LocalDate.now()) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { today = LocalDate.now() }
    var showPicker by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Int?>(null) }

    val remaining = Shas.remaining(progress)
    val days = Store.daysLeft(today)
    val all = Shas.list.indices
    val done = all.filter { progress[it] >= Shas.list[it].dapim }
    val learning = all.filter { progress[it] in 1 until Shas.list[it].dapim }
    val notStarted = all.filter { progress[it] == 0 }

    fun act(block: suspend () -> Unit) = scope.launch { block(); DafWidget().updateAll(ctx) }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("ש״ס עד גיל 30", fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("דפים שנותרו:")
        Text(Store.fmt(remaining), fontSize = 56.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("ימים עד גיל 30:")
        Text(Store.fmt(days), fontSize = 44.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("קצב נדרש:")
        Text("${Store.pace(remaining, days)} דפים ביום", fontSize = 24.sp)
        Text("Target: 13.12.2033")
        Spacer(Modifier.height(8.dp))
        Button(onClick = { showPicker = true }, modifier = Modifier.fillMaxWidth()) { Text("+ סיימתי דף") }
        OutlinedButton(onClick = { act { Store.undo(ctx) } }, modifier = Modifier.fillMaxWidth()) { Text("בטל פעולה אחרונה") }

        Spacer(Modifier.height(16.dp))
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SectionTitle("✅ מסכתות שהושלמו (${done.size})")
            Text(if (done.isEmpty()) "—" else done.joinToString(" · ") { Shas.list[it].name })

            Spacer(Modifier.height(10.dp))
            SectionTitle("📖 בלימוד (${learning.size})")
            if (learning.isEmpty()) Text("—")
            learning.forEach { i ->
                val t = Shas.list[i]
                Row(
                    Modifier.fillMaxWidth().clickable { editing = i }.padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(t.name, fontWeight = FontWeight.Medium)
                    Text("דף ${Shas.hebNum(2 + progress[i])}")
                }
            }
            if (learning.isNotEmpty()) Text("לחיצה על מסכת מאפשרת תיקון", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(Modifier.height(10.dp))
            SectionTitle("⏳ טרם התחלתי (${notStarted.size})")
            Text(if (notStarted.isEmpty()) "—" else notStarted.joinToString(" · ") { Shas.list[it].name })
        }
    }

    if (showPicker) {
        TractatePicker(progress, onDismiss = { showPicker = false }) { i ->
            showPicker = false
            act { Store.advance(ctx, i) }
        }
    }
    editing?.let { i ->
        EditDialog(i, 2 + progress[i], onDismiss = { editing = null },
            onSave = { daf -> editing = null; act { Store.setCurrentDaf(ctx, i, daf) } },
            onFinish = { editing = null; act { Store.finishTractate(ctx, i) } })
    }
}

@Composable
private fun SectionTitle(text: String) =
    Text(text, fontSize = 18.sp, fontWeight = FontWeight.Bold)

@Composable
private fun EditDialog(index: Int, currentDaf: Int, onDismiss: () -> Unit, onSave: (Int) -> Unit, onFinish: () -> Unit) {
    val t = Shas.list[index]
    var text by remember { mutableStateOf(currentDaf.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(t.name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("דף נוכחי (2–${t.lastDaf}):")
                OutlinedTextField(
                    value = text, onValueChange = { text = it.filter(Char::isDigit).take(3) }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                TextButton(onClick = onFinish) { Text("✅ סיימתי את המסכת") }
            }
        },
        confirmButton = { TextButton(onClick = { text.toIntOrNull()?.let(onSave) }) { Text("שמור") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("ביטול") } }
    )
}

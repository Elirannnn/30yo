package com.shas.counter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
            MaterialTheme(colorScheme = if (androidx.compose.foundation.isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()) {
                Surface(Modifier.fillMaxSize()) { Screen() }
            }
        }
    }
}

@Composable
private fun Screen() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val remaining by Store.flow(ctx).collectAsState(initial = Store.INITIAL)
    var today by remember { mutableStateOf(LocalDate.now()) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { today = LocalDate.now() }
    val days = Store.daysLeft(today)
    var manual by remember { mutableStateOf("") }

    fun act(block: suspend () -> Unit) = scope.launch { block(); DafWidget().updateAll(ctx) }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
    ) {
        Text("ש״ס עד גיל 30", fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text("דפים שנותרו:")
        Text(Store.fmt(remaining), fontSize = 56.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("ימים עד גיל 30:")
        Text(Store.fmt(days), fontSize = 44.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("קצב נדרש:")
        Text("${Store.pace(remaining, days)} דפים ביום", fontSize = 24.sp)
        Text("Target: 13.12.2033")
        Spacer(Modifier.height(16.dp))
        Button(onClick = { act { Store.finishDaf(ctx) } }, modifier = Modifier.fillMaxWidth()) { Text("+ סיימתי דף") }
        OutlinedButton(onClick = { act { Store.undo(ctx) } }, modifier = Modifier.fillMaxWidth()) { Text("בטל פעולה אחרונה") }
        Spacer(Modifier.height(24.dp))
        Text("תיקון ידני של מספר הדפים", fontSize = 14.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = manual, onValueChange = { manual = it.filter(Char::isDigit).take(4) },
                singleLine = true, modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Button(onClick = { manual.toIntOrNull()?.let { v -> act { Store.setManual(ctx, v) }; manual = "" } }) { Text("עדכן") }
        }
    }
}

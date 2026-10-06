package com.shas.counter

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.appwidget.cornerRadius
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DafWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Date is read at render time, so every refresh uses the current local date.
        val remaining = Store.remaining(context)
        val days = Store.daysLeft(LocalDate.now())
        provideContent { GlanceTheme { Content(remaining, days) } }
    }

    @Composable
    private fun Content(remaining: Int, days: Long) {
        val c = GlanceTheme.colors
        Column(
            modifier = GlanceModifier.fillMaxSize().background(c.widgetBackground)
                .cornerRadius(24.dp).padding(12.dp)
                .clickable(actionStartActivity<MainActivity>()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("📚 ש״ס עד גיל 30", style = TextStyle(color = c.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium))
            Spacer(GlanceModifier.padding(2.dp))
            Text(Store.fmt(remaining), style = TextStyle(color = c.primary, fontSize = 40.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center))
            Text("דפים שנותרו", style = TextStyle(color = c.onSurface, fontSize = 12.sp))
            Spacer(GlanceModifier.padding(2.dp))
            Text(Store.fmt(days), style = TextStyle(color = c.primary, fontSize = 32.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center))
            Text("ימים שנותרו", style = TextStyle(color = c.onSurface, fontSize = 12.sp))
            Spacer(GlanceModifier.padding(2.dp))
            Text("קצב נדרש: ${Store.pace(remaining, days)} דפים ביום", style = TextStyle(color = c.onSurface, fontSize = 12.sp))
            Spacer(GlanceModifier.padding(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button("+ סיימתי דף", onClick = actionStartActivity<PickActivity>())
                Spacer(GlanceModifier.width(6.dp))
                Button("בטל", onClick = actionRunCallback<UndoAction>())
            }
        }
    }
}

class UndoAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        Store.undo(context)
        DafWidget().updateAll(context)
    }
}

class DafWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DafWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            Intent.ACTION_DATE_CHANGED, Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED -> {
                val pending = goAsync()
                CoroutineScope(Dispatchers.Default).launch {
                    try { DafWidget().updateAll(context) } finally { pending.finish() }
                }
            }
        }
    }
}

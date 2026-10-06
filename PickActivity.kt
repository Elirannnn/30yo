package com.shas.counter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.launch

/** Opened by the widget button: shows only the tractate picker over the home screen. */
class PickActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    val ctx = LocalContext.current
                    val scope = rememberCoroutineScope()
                    val progress by Store.flow(ctx).collectAsState(initial = null)
                    progress?.let { p ->
                        TractatePicker(p, onDismiss = { finish() }) { i ->
                            scope.launch {
                                Store.advance(ctx, i)
                                DafWidget().updateAll(ctx)
                                finish()
                            }
                        }
                    }
                }
            }
        }
    }
}

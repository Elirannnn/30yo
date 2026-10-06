package com.shas.counter

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale

private val Context.ds by preferencesDataStore("shas")

object Store {
    const val INITIAL = 2649
    const val TOTAL = 2711
    val TARGET: LocalDate = LocalDate.of(2033, 12, 13) // calendar date, no timezone
    private val REM = intPreferencesKey("remaining")
    private val PREV = intPreferencesKey("previous")

    private fun Preferences.rem() = this[REM] ?: INITIAL

    fun flow(c: Context): Flow<Int> = c.ds.data.map { it.rem() }
    suspend fun remaining(c: Context): Int = c.ds.data.first().rem()

    suspend fun finishDaf(c: Context) {
        c.ds.edit {
            val cur = it.rem()
            if (cur > 0) { it[PREV] = cur; it[REM] = cur - 1 }
        }
    }

    suspend fun undo(c: Context) {
        c.ds.edit {
            val p = it[PREV]
            if (p != null) { it[REM] = p; it.remove(PREV) }
        }
    }

    suspend fun setManual(c: Context, value: Int) {
        c.ds.edit {
            it[PREV] = it.rem()
            it[REM] = value.coerceIn(0, TOTAL)
        }
    }

    fun daysLeft(today: LocalDate = LocalDate.now()): Long =
        ChronoUnit.DAYS.between(today, TARGET).coerceAtLeast(0)

    fun pace(remaining: Int, days: Long): String =
        String.format(Locale.US, "%.2f", if (days <= 0) remaining.toDouble() else remaining.toDouble() / days)

    fun fmt(n: Number): String = String.format(Locale.US, "%,d", n.toLong())
}

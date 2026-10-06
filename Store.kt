package com.shas.counter

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale

private val Context.ds by preferencesDataStore("shas_v2")

/** Source of truth: completed dapim per tractate. The global counter is always derived from it. */
object Store {
    val TARGET: LocalDate = LocalDate.of(2033, 12, 13) // calendar date, no timezone
    private val PROGRESS = stringPreferencesKey("progress")
    private val PREV = stringPreferencesKey("previous")

    private fun parse(s: String?): List<Int> {
        val n = Shas.list.size
        val v = s?.split(",")?.mapNotNull { it.trim().toIntOrNull() }
        if (v == null || v.size != n) return Shas.initial
        return v.mapIndexed { i, x -> x.coerceIn(0, Shas.list[i].dapim) }
    }

    private fun Preferences.progress() = parse(this[PROGRESS])

    fun flow(c: Context): Flow<List<Int>> = c.ds.data.map { it.progress() }
    suspend fun progress(c: Context): List<Int> = c.ds.data.first().progress()
    suspend fun remaining(c: Context): Int = Shas.remaining(progress(c))

    private fun androidx.datastore.preferences.core.MutablePreferences.save(old: List<Int>, new: List<Int>) {
        if (old == new) return
        this[PREV] = old.joinToString(",")
        this[PROGRESS] = new.joinToString(",")
    }

    /** One more daf finished in tractate [index]. */
    suspend fun advance(c: Context, index: Int) {
        c.ds.edit {
            val old = it.progress()
            if (old[index] < Shas.list[index].dapim) {
                val new = old.toMutableList().also { l -> l[index] = l[index] + 1 }
                it.save(old, new)
            }
        }
    }

    suspend fun undo(c: Context) {
        c.ds.edit {
            val p = it[PREV]
            if (p != null) { it[PROGRESS] = parse(p).joinToString(","); it.remove(PREV) }
        }
    }

    /** Correction: set the daf currently being studied (daf 2 = nothing completed yet). */
    suspend fun setCurrentDaf(c: Context, index: Int, daf: Int) {
        c.ds.edit {
            val old = it.progress()
            val new = old.toMutableList().also { l -> l[index] = (daf - 2).coerceIn(0, Shas.list[index].dapim - 1) }
            it.save(old, new)
        }
    }

    suspend fun finishTractate(c: Context, index: Int) {
        c.ds.edit {
            val old = it.progress()
            val new = old.toMutableList().also { l -> l[index] = Shas.list[index].dapim }
            it.save(old, new)
        }
    }

    fun daysLeft(today: LocalDate = LocalDate.now()): Long =
        ChronoUnit.DAYS.between(today, TARGET).coerceAtLeast(0)

    fun pace(remaining: Int, days: Long): String =
        String.format(Locale.US, "%.2f", if (days <= 0) remaining.toDouble() else remaining.toDouble() / days)

    fun fmt(n: Number): String = String.format(Locale.US, "%,d", n.toLong())
}

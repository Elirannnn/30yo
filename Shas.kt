package com.shas.counter

/** lastDaf = the last daf number of the tractate; dapim = lastDaf - 1 (daf 2 .. lastDaf). */
data class Tractate(val name: String, val lastDaf: Int) {
    val dapim: Int get() = lastDaf - 1
}

object Shas {
    // Standard Shas Bavli order / Daf Yomi counts (Meilah includes Kinnim, Tamid, Middot).
    val list = listOf(
        Tractate("ברכות", 64), Tractate("שבת", 157), Tractate("עירובין", 105), Tractate("פסחים", 121),
        Tractate("שקלים", 22), Tractate("יומא", 88), Tractate("סוכה", 56), Tractate("ביצה", 40),
        Tractate("ראש השנה", 35), Tractate("תענית", 31), Tractate("מגילה", 32), Tractate("מועד קטן", 29),
        Tractate("חגיגה", 27), Tractate("יבמות", 122), Tractate("כתובות", 112), Tractate("נדרים", 91),
        Tractate("נזיר", 66), Tractate("סוטה", 49), Tractate("גיטין", 90), Tractate("קידושין", 82),
        Tractate("בבא קמא", 119), Tractate("בבא מציעא", 119), Tractate("בבא בתרא", 176),
        Tractate("סנהדרין", 113), Tractate("מכות", 24), Tractate("שבועות", 49), Tractate("עבודה זרה", 76),
        Tractate("הוריות", 14), Tractate("זבחים", 120), Tractate("מנחות", 110), Tractate("חולין", 142),
        Tractate("בכורות", 61), Tractate("ערכין", 34), Tractate("תמורה", 34), Tractate("כריתות", 28),
        Tractate("מעילה", 37), Tractate("נדה", 73)
    )

    val total: Int = list.sumOf { it.dapim } // 2711

    /** Completed dapim per tractate at the start. */
    val initial: List<Int> = list.map {
        when (it.name) { "ברכות" -> 20; "סוכה" -> 11; "מגילה" -> 31; else -> 0 }
    }

    fun remaining(progress: List<Int>): Int = total - progress.sum()

    fun hebNum(n: Int): String {
        var x = n
        val sb = StringBuilder()
        while (x >= 400) { sb.append('ת'); x -= 400 }
        if (x >= 100) { sb.append("קרשת"[x / 100 - 1]); x %= 100 }
        when (x) {
            15 -> sb.append("טו")
            16 -> sb.append("טז")
            else -> {
                if (x >= 10) sb.append("יכלמנסעפצ"[x / 10 - 1])
                if (x % 10 > 0) sb.append("אבגדהוזחט"[x % 10 - 1])
            }
        }
        val s = sb.toString()
        return if (s.length == 1) s + "׳" else s.dropLast(1) + "״" + s.last()
    }
}

package com.example.miram.shared.model

object RingDuration {
    /** 5초~30초 (5초 단위) + 40초~3600초 (10초 단위) */
    val allValues: List<Int> =
        (5..30 step 5).toList() + (40..3600 step 10).toList()

    fun label(seconds: Int): String {
        if (seconds < 60) return "${seconds}초"
        val m = seconds / 60
        val s = seconds % 60
        return if (s == 0) "${m}분" else "${m}분 ${s}초"
    }
}

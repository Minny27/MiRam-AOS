package com.seungmin.miram.shared.model

object RingDuration {
    /** 0초 = 계속 울림, 이후 5초~30초(5초 단위), 10초 단위로 1분까지 */
    val allValues: List<Int> = listOf(0, 5, 10, 15, 20, 25, 30, 40, 50, 60)

    fun normalize(seconds: Int): Int =
        allValues.minByOrNull { kotlin.math.abs(it - seconds) } ?: 0

    fun label(seconds: Int): String {
        if (seconds <= 0) return "계속 울림"
        if (seconds < 60) return "${seconds}초"
        val m = seconds / 60
        val s = seconds % 60
        return if (s == 0) "${m}분" else "${m}분 ${s}초"
    }
}

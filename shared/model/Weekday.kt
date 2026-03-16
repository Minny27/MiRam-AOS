package com.example.miram.shared.model

enum class Weekday(val value: Int, val label: String) {
    MON(1, "월"),
    TUE(2, "화"),
    WED(3, "수"),
    THU(4, "목"),
    FRI(5, "금"),
    SAT(6, "토"),
    SUN(7, "일");

    companion object {
        val storageOrder: List<Weekday> = listOf(SUN, MON, TUE, WED, THU, FRI, SAT)

        fun fromValue(value: Int): Weekday? = entries.find { it.value == value }
    }
}

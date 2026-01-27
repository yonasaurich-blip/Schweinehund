package com.example.nfcdailycheckin.util

import java.time.*

object DateTime {
    fun today(zoneId: ZoneId = ZoneId.systemDefault()): LocalDate = LocalDate.now(zoneId)
    fun nowInstant(): Instant = Instant.now()
    fun startOfDay(date: LocalDate, zoneId: ZoneId = ZoneId.systemDefault()): Instant =
        date.atStartOfDay(zoneId).toInstant()

    fun endOfDay(date: LocalDate, zoneId: ZoneId = ZoneId.systemDefault()): Instant =
        date.plusDays(1).atStartOfDay(zoneId).toInstant().minusMillis(1)
}

package com.prod.evergreen.helper

import java.time.Duration
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.ZoneId

object DateConverter {

    // Method to convert and format the date string
    fun convertToLocalUtcAndFormat(dateStr: String): String {
        val zonedDateTime = ZonedDateTime.parse(dateStr)
        val utcDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC"))
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        return utcDateTime.format(formatter)
    }

    // Method to get "time ago" string
    fun getTimeAgo(dateStr: String): String {
        val zonedDateTime = ZonedDateTime.parse(dateStr)
        val now = ZonedDateTime.now(ZoneId.of("UTC"))
        val duration = Duration.between(zonedDateTime, now)

        return when {
            duration.toMinutes() < 1 -> "Just now"
            duration.toMinutes() == 1L -> "1 min ago"
            duration.toMinutes() < 60 -> "${duration.toMinutes()} mins ago"
            duration.toHours() == 1L -> "1 hr ago"
            duration.toHours() < 24 -> "${duration.toHours()} hrs ago"
            duration.toDays() == 1L -> "1 day ago"
            duration.toDays() < 7 -> "${duration.toDays()} days ago"
            else -> convertToLocalUtcAndFormat(dateStr)
        }
    }
}

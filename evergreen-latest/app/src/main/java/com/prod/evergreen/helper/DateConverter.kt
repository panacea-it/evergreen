package com.prod.evergreen.helper

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object DateConverter {
    private val outputFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

    // Uses calendar date to avoid timezone day shifts in date-only fields.
    fun convertToLocalUtcAndFormat(dateStr: String?): String {
        val raw = dateStr?.trim().orEmpty()
        if (raw.isEmpty()) return "-"
        return try {
            val localDate = if (raw.contains("T")) {
                LocalDate.parse(raw.substring(0, 10))
            } else {
                LocalDate.parse(raw)
            }
            localDate.format(outputFormatter)
        } catch (_: Exception) {
            try {
                Instant.parse(raw).atZone(ZoneId.systemDefault()).toLocalDate()
                    .format(outputFormatter)
            } catch (_: Exception) {
                raw
            }
        }
    }

    // Method to get "time ago" string
    fun getTimeAgo(dateStr: String?): String {
        if (dateStr.isNullOrBlank()) return "-"
        return try {
            val zonedDateTime = ZonedDateTime.parse(dateStr)
            val now = ZonedDateTime.now(ZoneId.of("UTC"))
            val duration = Duration.between(zonedDateTime, now)

            when {
                duration.toMinutes() < 1 -> "Just now"
                duration.toMinutes() == 1L -> "1 min ago"
                duration.toMinutes() < 60 -> "${duration.toMinutes()} mins ago"
                duration.toHours() == 1L -> "1 hr ago"
                duration.toHours() < 24 -> "${duration.toHours()} hrs ago"
                duration.toDays() == 1L -> "1 day ago"
                duration.toDays() < 7 -> "${duration.toDays()} days ago"
                else -> convertToLocalUtcAndFormat(dateStr)
            }
        } catch (_: Exception) {
            convertToLocalUtcAndFormat(dateStr)
        }
    }
}

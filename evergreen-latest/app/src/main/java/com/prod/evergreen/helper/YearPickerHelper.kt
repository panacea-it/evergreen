package com.prod.evergreen.helper

import android.content.Context
import android.widget.FrameLayout
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import java.util.Calendar

object YearPickerHelper {

    fun show(
        context: Context,
        selectedYear: Int? = null,
        onYearSelected: (Int) -> Unit
    ) {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val picker = NumberPicker(context).apply {
            minValue = 1980
            maxValue = currentYear
            value = (selectedYear ?: currentYear).coerceIn(minValue, maxValue)
            wrapSelectorWheel = false
            descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
        }

        val padding = (24 * context.resources.displayMetrics.density).toInt()
        val container = FrameLayout(context).apply {
            setPadding(padding, padding / 2, padding, padding / 2)
            addView(picker)
        }

        AlertDialog.Builder(context)
            .setTitle("Select manufacture year")
            .setView(container)
            .setPositiveButton("OK") { _, _ -> onYearSelected(picker.value) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun yearFromStoredDate(value: String?): Int? {
        val year = value?.take(4)?.filter { it.isDigit() }
        return year?.toIntOrNull()
    }

    fun displayYear(value: String?): String {
        if (value.isNullOrBlank()) return ""
        val datePart = value.take(10)
        if (datePart.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
            val year = datePart.take(4).toIntOrNull() ?: return ""
            val month = datePart.substring(5, 7).toIntOrNull() ?: return year.toString()
            val day = datePart.substring(8, 10).toIntOrNull() ?: return year.toString()
            // Stored as Jan 1; UTC shift can show as 31 Dec of the previous year.
            return if (month == 12 && day >= 30) (year + 1).toString() else year.toString()
        }
        return yearFromStoredDate(value)?.toString().orEmpty()
    }

    fun apiDateFromYear(yearText: String): String {
        val year = yearText.take(4)
        return if (year.length == 4) "$year-01-01" else yearText
    }
}

package com.prod.evergreen.helper

import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

object FormValidator {
    data class Check(
        val view: View? = null,
        val message: String,
        val valid: Boolean
    )

    fun cardCompleteOrEmpty(vararg values: String): Boolean {
        val filled = values.count { it.isNotBlank() }
        return filled == 0 || filled == values.size
    }

    fun firstInvalid(vararg checks: Check): String? {
        val failed = checks.firstOrNull { !it.valid } ?: return null
        failed.view?.let { highlight(it, failed.message) }
        val context = failed.view?.context
        if (context != null) {
            Toast.makeText(context, failed.message, Toast.LENGTH_SHORT).show()
        }
        return failed.message
    }

    private fun highlight(view: View, message: String) {
        view.requestFocus()
        try {
            view.parent?.requestChildFocus(view, view)
        } catch (_: Exception) {
        }
        when (view) {
            is EditText -> view.error = message
            is TextView -> view.error = message
        }
    }
}

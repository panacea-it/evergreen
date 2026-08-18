package com.prod.evergreen.helper

object Validator {
    private val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$".toRegex()
    val MOBILE_REGEX = "^(\\+\\d{1,3}[- ]?)?\\d{10}$".toRegex()

    fun isEmailValid(email: String): Boolean {
        return EMAIL_REGEX.matches(email)
    }

    fun isMobileValid(mobile: String): Boolean {
        return MOBILE_REGEX.matches(mobile)
    }
}



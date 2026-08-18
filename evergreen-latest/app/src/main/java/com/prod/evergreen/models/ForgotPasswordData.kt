package com.prod.evergreen.models


data class ForgotPasswordData (
    val message: String? = null,
    val email: String? = null,
    val otp: Long? = null,
    val status: Int? = null
)

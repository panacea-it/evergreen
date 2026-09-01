package com.prod.evergreen.models

import com.google.gson.annotations.SerializedName


data class ChangePasswordData (
    @SerializedName("status")
    val status_code: Int? = null,
    val message: String? = null,
    val image_url: String? = null,
    val url: String? = null,
    val pdf_base64: String? = null,
    val errors: List<String>? = null,
)

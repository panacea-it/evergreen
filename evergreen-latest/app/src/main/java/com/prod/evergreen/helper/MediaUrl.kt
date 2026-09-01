package com.prod.evergreen.helper

import com.prod.evergreen.api.Constants

object MediaUrl {
    fun resolve(path: String?): String {
        val value = path?.trim().orEmpty()
        if (value.isEmpty()) return ""
        if (value.startsWith("http://") || value.startsWith("https://")) {
            return value
        }
        return Constants.BASE_URL.trimEnd('/') + "/" + value.trimStart('/')
    }
}

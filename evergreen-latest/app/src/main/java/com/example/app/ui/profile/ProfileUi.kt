package com.example.app.ui.profile

import com.prod.evergreen.helper.DateConverter
import com.prod.evergreen.models.Users
import com.prod.evergreen.models.attachedCompanyLabel

fun roleLabel(accessLevel: String?): String {
    return when (accessLevel?.lowercase()) {
        "eg_super_admin" -> "Admin"
        "eg_admin" -> "Manager"
        "client_admin" -> "Client Admin"
        "client" -> "Client"
        "technician" -> "Technician"
        else -> accessLevel.orEmpty()
            .replace('_', ' ')
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            .ifBlank { "-" }
    }
}

fun Users.toProfileData(): ProfileData {
    val displayRole = roleLabel(access_level)
    val company = attachedCompanyLabel().takeIf { it != "-" }.orEmpty()
    val address = permanent_ddress?.takeIf { it.isNotBlank() }
        ?: location?.takeIf { it.isNotBlank() }
        ?: company
    return ProfileData(
        name = name.orEmpty(),
        role = displayRole,
        location = location.orEmpty(),
        phone = phone.orEmpty(),
        userId = id?.toString().orEmpty(),
        fullName = name.orEmpty(),
        email = email.orEmpty(),
        phoneNumber = phone.orEmpty(),
        userRole = displayRole,
        joinedOn = DateConverter.convertToLocalUtcAndFormat(created_at).takeIf { it != "-" }.orEmpty(),
        address = address,
        company = company
    )
}

fun currentUserProfile(
    name: String?,
    email: String?,
    phone: String?,
    role: String?,
    userId: Int?,
    location: String?,
    company: String?
): ProfileData {
    val displayRole = roleLabel(role)
    val companyName = company.orEmpty()
    return ProfileData(
        name = name.orEmpty(),
        role = displayRole,
        location = location.orEmpty(),
        phone = phone.orEmpty(),
        userId = userId?.takeIf { it != 0 }?.toString().orEmpty(),
        fullName = name.orEmpty(),
        email = email.orEmpty(),
        phoneNumber = phone.orEmpty(),
        userRole = displayRole,
        address = location.orEmpty(),
        company = companyName
    )
}

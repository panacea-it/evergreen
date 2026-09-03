package com.prod.evergreen.helper

object RoleLabels {
    const val SUPER_ADMIN = "Evergreen Super Admin"
    const val MANAGER = "Evergreen Manager"
    const val CLIENT_ADMIN = "Client Admin"
    const val CLIENT = "Client"
    const val TECHNICIAN = "Technician"

    fun display(accessLevel: String?): String {
        return when (accessLevel?.lowercase()) {
            "eg_super_admin" -> SUPER_ADMIN
            "eg_admin" -> MANAGER
            "client_admin" -> CLIENT_ADMIN
            "client" -> CLIENT
            "technician" -> TECHNICIAN
            else -> accessLevel.orEmpty()
                .replace('_', ' ')
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                .ifBlank { "-" }
        }
    }

    fun pairs(): List<Pair<String, String>> = listOf(
        "eg_super_admin" to SUPER_ADMIN,
        "eg_admin" to MANAGER,
        "client_admin" to CLIENT_ADMIN,
        "client" to CLIENT,
        "technician" to TECHNICIAN
    )
}

package com.prod.evergreen.helper

object RoleAccess {
    fun canManageEquipment(role: String?): Boolean {
        return when (role?.lowercase()) {
            "technician" -> false
            else -> !role.isNullOrBlank()
        }
    }

    fun canManageTasks(role: String?): Boolean {
        return when (role?.lowercase()) {
            "eg_super_admin", "eg_admin", "client_admin", "client" -> true
            else -> false
        }
    }

    fun canManageUsers(role: String?): Boolean {
        return when (role?.lowercase()) {
            "eg_super_admin", "eg_admin", "client_admin" -> true
            else -> false
        }
    }

    fun lockToAttachedCompany(role: String?): Boolean {
        return when (role?.lowercase()) {
            "client_admin", "client" -> true
            else -> false
        }
    }

    fun canAssignTechnician(role: String?): Boolean {
        return when (role?.lowercase()) {
            "eg_super_admin", "eg_admin" -> true
            else -> false
        }
    }

    fun isUnassigned(technicianLink: Any?): Boolean {
        if (technicianLink == null) return true
        return when (technicianLink) {
            is Number -> technicianLink.toInt() == 0
            else -> technicianLink.toString().isBlank() ||
                technicianLink.toString() == "null" ||
                technicianLink.toString() == "0"
        }
    }

    fun canAcceptTask(role: String?): Boolean {
        return role.equals("technician", ignoreCase = true)
    }

    fun canGenerateServiceReport(role: String?): Boolean {
        return canManageServiceReports(role)
    }

    fun canManageServiceReports(role: String?): Boolean {
        return when (role?.lowercase()) {
            "eg_super_admin", "eg_admin" -> true
            else -> false
        }
    }
}

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

    fun canAcceptTask(role: String?): Boolean {
        return role.equals("technician", ignoreCase = true)
    }

    fun canGenerateServiceReport(role: String?): Boolean {
        return !role.isNullOrBlank() && !role.equals("technician", ignoreCase = true)
    }
}

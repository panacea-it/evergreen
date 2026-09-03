package com.prod.evergreen.helper

import com.example.app.ui.users.UserRole

object DashboardNav {
    var pendingTaskTab: Int? = null
    var pendingUserRole: UserRole? = null
    var pendingCompanyId: Int? = null
    var pendingEquipmentJson: String? = null
    var pendingCompanyJson: String? = null
    var pendingTaskJson: String? = null
    var pendingServiceReportId: Int? = null
    var pendingServiceReportViewOnly: Boolean = false
    var pendingServiceReportTaskJson: String? = null
}

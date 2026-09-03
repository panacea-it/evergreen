package com.prod.evergreen.helper

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.prod.evergreen.R
import com.prod.evergreen.activities.MainActivity
import com.prod.evergreen.models.TaskCreated

object ServiceReportNav {
    fun openList(fragment: Fragment) {
        go(fragment, R.id.serviceReportsFragment, "Service Reports")
    }

    fun openCreate(fragment: Fragment) {
        DashboardNav.pendingServiceReportId = null
        DashboardNav.pendingServiceReportViewOnly = false
        DashboardNav.pendingServiceReportTaskJson = null
        go(fragment, R.id.serviceReportFormFragment, "Create Service Report")
    }

    fun openEdit(fragment: Fragment, reportId: Int, viewOnly: Boolean) {
        DashboardNav.pendingServiceReportId = reportId
        DashboardNav.pendingServiceReportViewOnly = viewOnly
        DashboardNav.pendingServiceReportTaskJson = null
        go(fragment, R.id.serviceReportFormFragment, if (viewOnly) "View Service Report" else "Edit Service Report")
    }

    fun openFromTask(fragment: Fragment, task: TaskCreated) {
        DashboardNav.pendingServiceReportId = null
        DashboardNav.pendingServiceReportViewOnly = false
        DashboardNav.pendingServiceReportTaskJson = Gson().toJson(task)
        go(fragment, R.id.serviceReportFormFragment, "Generate Service Report")
    }

    fun openFromTask(activity: Activity, task: TaskCreated) {
        DashboardNav.pendingServiceReportId = null
        DashboardNav.pendingServiceReportViewOnly = false
        DashboardNav.pendingServiceReportTaskJson = Gson().toJson(task)
        if (activity is MainActivity) {
            val options = NavOptions.Builder().setLaunchSingleTop(true).build()
            activity.findNavController(R.id.nav_host_fragment).navigate(R.id.serviceReportFormFragment, null, options)
            activity.setTitleTextView("Generate Service Report")
        } else {
            activity.startActivity(
                Intent(activity, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    .putExtra("open_service_report_form", true)
            )
        }
    }

    private fun go(fragment: Fragment, destinationId: Int, title: String) {
        val options = NavOptions.Builder().setLaunchSingleTop(true).build()
        runCatching { fragment.findNavController().navigate(destinationId, null, options) }
        (fragment.activity as? MainActivity)?.setTitleTextView(title)
    }
}

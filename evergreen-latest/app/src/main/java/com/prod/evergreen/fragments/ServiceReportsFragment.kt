package com.prod.evergreen.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.app.ui.report.ServiceReportItem
import com.example.app.ui.report.ServiceReportListScreen
import com.google.gson.JsonObject
import com.prod.evergreen.XApplication
import com.prod.evergreen.activities.MainActivity
import com.prod.evergreen.activities.NotificationList
import com.prod.evergreen.activities.QrScanner
import com.prod.evergreen.api.MainRepository
import com.prod.evergreen.api.MainViewModel
import com.prod.evergreen.api.MyViewModelFactory
import com.prod.evergreen.api.RetrofitService
import com.prod.evergreen.helper.ConstantValues
import com.prod.evergreen.helper.DateConverter
import com.prod.evergreen.helper.ProgressDialogUtil
import com.prod.evergreen.helper.RoleAccess
import com.prod.evergreen.helper.ServiceReportHelper
import com.prod.evergreen.helper.ServiceReportNav
import com.prod.evergreen.helper.SharedPreferencesHelper
import com.prod.evergreen.models.ServiceReport
import java.text.NumberFormat
import java.util.Locale

class ServiceReportsFragment : Fragment() {
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var viewModel: MainViewModel
    private val reports = mutableStateOf<List<ServiceReport>>(emptyList())
    private val searchQuery = mutableStateOf("")
    private var pendingDeleteId: Int? = null
    private var pendingPdfId: Int? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        sharedPreferencesHelper = SharedPreferencesHelper(requireActivity())
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val source by reports
                val query by searchQuery
                val filtered = source.filter { report ->
                    if (query.isBlank()) true
                    else listOf(
                        report.serviceReportNumber,
                        report.companyName,
                        report.taskId,
                        report.technicianName,
                        report.equipmentName,
                        report.status
                    ).any { it.orEmpty().contains(query, ignoreCase = true) }
                }.map { it.toItem() }
                ServiceReportListScreen(
                    reports = filtered,
                    searchQuery = query,
                    onSearchQueryChange = { searchQuery.value = it },
                    onMenuClick = { openDrawer() },
                    onScanClick = { startActivity(Intent(requireActivity(), QrScanner::class.java)) },
                    onNotificationClick = { startActivity(Intent(requireActivity(), NotificationList::class.java)) },
                    onCreateClick = { ServiceReportNav.openCreate(this@ServiceReportsFragment) },
                    onReportClick = { ServiceReportNav.openEdit(this@ServiceReportsFragment, it.id, true) },
                    onMoreClick = { item ->
                        reports.value.firstOrNull { it.id == item.id }?.let { showActions(it) }
                    }
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViewmodel()
        viewModel.loading.observe(viewLifecycleOwner) { loading ->
            if (loading) ProgressDialogUtil.showProgressDialog(requireActivity(), "Loading")
            else ProgressDialogUtil.hideProgressDialog()
        }
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show()
        }
        viewModel.serviceReportsResponse.observe(viewLifecycleOwner) { data ->
            if (data.status == 403) {
                Toast.makeText(requireActivity(), data.message ?: "Not authorized", Toast.LENGTH_SHORT).show()
                return@observe
            }
            reports.value = data.data.orEmpty()
        }
        viewModel.changePasswordDataResponse.observe(viewLifecycleOwner) { data ->
            if (pendingDeleteId == null) return@observe
            pendingDeleteId = null
            Toast.makeText(requireActivity(), data.message ?: "Deleted", Toast.LENGTH_SHORT).show()
            if (data.status_code == 200) refresh()
        }
        viewModel.downloadpdf.observe(viewLifecycleOwner) { data ->
            if (pendingPdfId == null) return@observe
            pendingPdfId = null
            ServiceReportHelper.offer(requireActivity(), data)
        }
        refresh()
    }

    override fun onResume() {
        super.onResume()
        if (::viewModel.isInitialized) refresh()
    }

    private fun refresh() {
        val token = sharedPreferencesHelper.getValueString(ConstantValues.AuthToken)
        val role = sharedPreferencesHelper.getValueString(ConstantValues.TYPE_ROLE)
        if (!RoleAccess.canManageServiceReports(role)) {
            Toast.makeText(requireActivity(), "You are not authorized to view service reports", Toast.LENGTH_SHORT).show()
            return
        }
        if (token.isNullOrBlank()) {
            Toast.makeText(requireActivity(), "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
            return
        }
        viewModel.getAllServiceReports(token)
    }

    private fun showActions(report: ServiceReport) {
        val id = report.id ?: return
        val options = arrayOf("View", "Edit", "Generate / View PDF", "Delete")
        AlertDialog.Builder(requireActivity())
            .setTitle(report.serviceReportNumber ?: "Service Report")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> ServiceReportNav.openEdit(this, id, true)
                    1 -> ServiceReportNav.openEdit(this, id, false)
                    2 -> downloadPdf(id)
                    3 -> confirmDelete(id)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun downloadPdf(id: Int) {
        val token = sharedPreferencesHelper.getValueString(ConstantValues.AuthToken) ?: return
        pendingPdfId = id
        val body = JsonObject()
        body.addProperty("id", id)
        viewModel.downloadSavedServiceReport(body, token)
    }

    private fun confirmDelete(id: Int) {
        AlertDialog.Builder(requireActivity())
            .setTitle("Delete Service Report")
            .setMessage("Delete this service report? The related task, company, and equipment will not be deleted.")
            .setPositiveButton("Delete") { _, _ ->
                val token = sharedPreferencesHelper.getValueString(ConstantValues.AuthToken) ?: return@setPositiveButton
                pendingDeleteId = id
                val body = JsonObject()
                body.addProperty("id", id)
                viewModel.deleteServiceReport(body, token)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openDrawer() {
        (activity as? MainActivity)
            ?.findViewById<androidx.drawerlayout.widget.DrawerLayout>(R.id.drawer_layout)
            ?.openDrawer(GravityCompat.START)
    }

    private fun ServiceReport.toItem(): ServiceReportItem {
        val costText = cost?.let {
            "₹${NumberFormat.getNumberInstance(Locale("en", "IN")).format(it)}"
        }.orEmpty()
        return ServiceReportItem(
            id = id ?: 0,
            number = serviceReportNumber.orEmpty(),
            company = companyName.orEmpty(),
            taskId = taskId.orEmpty(),
            technician = technicianName.orEmpty(),
            equipment = equipmentName.orEmpty(),
            status = status.orEmpty().ifBlank { "Not Started" },
            cost = costText,
            createdAt = DateConverter.convertToLocalUtcAndFormat(createdAt)
        )
    }

    private fun setViewmodel() {
        viewModel = ViewModelProvider(
            this,
            MyViewModelFactory(
                MainRepository(
                    RetrofitService.getInstance(requireActivity()),
                    XApplication.database.newsDao(),
                    XApplication.database.companyDao()
                )
            )
        )[MainViewModel::class.java]
    }
}

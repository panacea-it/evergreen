package com.prod.evergreen.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.app.ui.report.ServiceReportFormScreen
import com.example.app.ui.report.ServiceReportFormState
import com.example.app.ui.report.TimelineEventState
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.prod.evergreen.R
import com.prod.evergreen.XApplication
import com.prod.evergreen.adapters.UserCompaniesAdapter
import com.prod.evergreen.api.MainRepository
import com.prod.evergreen.api.MainViewModel
import com.prod.evergreen.api.MyViewModelFactory
import com.prod.evergreen.api.RetrofitService
import com.prod.evergreen.helper.ConstantValues
import com.prod.evergreen.helper.DashboardNav
import com.prod.evergreen.helper.DateConverter
import com.prod.evergreen.helper.ProgressDialogUtil
import com.prod.evergreen.helper.RoleAccess
import com.prod.evergreen.helper.ServiceReportHelper
import com.prod.evergreen.helper.SharedPreferencesHelper
import com.prod.evergreen.models.AMCData
import com.prod.evergreen.models.ServiceReport
import com.prod.evergreen.models.TaskCreated
import com.prod.evergreen.models.activeCompanies

class ServiceReportFormFragment : Fragment() {
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var viewModel: MainViewModel
    private val formState = mutableStateOf(ServiceReportFormState())
    private var reportId: Int? = null
    private var isSubmitting = false
    private var waitingForPrefill = false
    private var waitingForSave = false
    private var waitingForPdf = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        sharedPreferencesHelper = SharedPreferencesHelper(requireActivity())
        reportId = DashboardNav.pendingServiceReportId
        val viewOnly = DashboardNav.pendingServiceReportViewOnly
        val taskJson = DashboardNav.pendingServiceReportTaskJson
        DashboardNav.pendingServiceReportId = null
        DashboardNav.pendingServiceReportViewOnly = false
        DashboardNav.pendingServiceReportTaskJson = null
        formState.value = formState.value.copy(
            viewOnly = viewOnly,
            title = when {
                viewOnly -> "View Service Report"
                reportId != null -> "Edit Service Report"
                taskJson != null -> "Generate Service Report"
                else -> "Create Service Report"
            },
            subtitle = if (taskJson != null) "Pre-filled from task" else "Complete the report details",
            saveLabel = if (reportId != null) "Update Report" else "Save Report"
        )
        if (!taskJson.isNullOrBlank()) {
            runCatching { Gson().fromJson(taskJson, TaskCreated::class.java) }
                .getOrNull()
                ?.let { applyTask(it) }
        }
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val state by formState
                ServiceReportFormScreen(
                    state = state,
                    onStateChange = { formState.value = it },
                    onBackClick = { findNavController().popBackStack() },
                    onSaveClick = { saveReport() },
                    onPdfClick = { downloadPdf() },
                    onCompanyClick = { showCompanyPicker() },
                    onStatusClick = { pickOption("Status", listOf("Not Started", "In Progress", "Hold", "Completed")) { formState.value = formState.value.copy(status = it) } },
                    onSatisfactionClick = { pickOption("Serviced to satisfaction", listOf("Yes", "No")) { formState.value = formState.value.copy(servicedToSatisfaction = it) } },
                    onSmoothClick = { pickOption("Running smoothly", listOf("Yes", "No")) { formState.value = formState.value.copy(runningSmoothly = it) } }
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
            isSubmitting = false
            waitingForPrefill = false
            waitingForSave = false
            waitingForPdf = false
            Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show()
        }
        viewModel.serviceReportDetailsResponse.observe(viewLifecycleOwner) { data ->
            if (data.status == 200 && data.data != null) applyReport(data.data)
            else if (data.message != null) Toast.makeText(requireActivity(), data.message, Toast.LENGTH_SHORT).show()
        }
        viewModel.serviceReportPrefillResponse.observe(viewLifecycleOwner) { data ->
            if (!waitingForPrefill) return@observe
            waitingForPrefill = false
            data.data?.let { applyReport(it, keepTaskFallback = true) }
        }
        viewModel.serviceReportSaveResponse.observe(viewLifecycleOwner) { data ->
            if (!waitingForSave) return@observe
            waitingForSave = false
            isSubmitting = false
            Toast.makeText(requireActivity(), data.message ?: "Saved", Toast.LENGTH_SHORT).show()
            if (data.status == 200 && data.data != null) {
                reportId = data.data.id
                applyReport(data.data)
                formState.value = formState.value.copy(
                    title = "Edit Service Report",
                    saveLabel = "Update Report"
                )
            }
        }
        viewModel.downloadpdf.observe(viewLifecycleOwner) { data ->
            if (!waitingForPdf) return@observe
            waitingForPdf = false
            ServiceReportHelper.offer(requireActivity(), data)
        }
        val token = sharedPreferencesHelper.getValueString(ConstantValues.AuthToken)
        if (!token.isNullOrBlank()) viewModel.getAllAmc(token)
        val role = sharedPreferencesHelper.getValueString(ConstantValues.TYPE_ROLE)
        if (!RoleAccess.canManageServiceReports(role)) {
            Toast.makeText(requireActivity(), "You are not authorized to manage service reports", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }
        if (reportId != null && token != null) {
            val body = JsonObject()
            body.addProperty("id", reportId)
            viewModel.getServiceReportDetails(body, token)
        } else if (formState.value.taskId.isNotBlank() && reportId == null && token != null) {
            waitingForPrefill = true
            val body = JsonObject()
            body.addProperty("task_id", formState.value.taskId)
            viewModel.getServiceReportPrefill(body, token)
        }
    }

    private fun applyTask(task: TaskCreated) {
        val ticket = task.task?.ticketNo.orEmpty().ifBlank {
            listOfNotNull(task.taskLink, task.task?.id).firstOrNull { it != 0 }?.toString().orEmpty()
        }
        val company = task.task?.equipment?.company
        val status = when (task.status?.lowercase()) {
            "open" -> "Not Started"
            "in_progress" -> "In Progress"
            "hold" -> "Hold"
            "closed" -> "Completed"
            else -> "Not Started"
        }
        formState.value = formState.value.copy(
            taskId = ticket,
            status = status,
            companyName = company?.name.orEmpty(),
            companyBranch = company?.branchName.orEmpty(),
            location = company?.location.orEmpty(),
            companyEmail = company?.email.orEmpty(),
            companyLink = company?.id,
            raisedByName = task.client?.name.orEmpty(),
            raisedByPhone = task.client?.phone.orEmpty(),
            raisedByNote = task.createdBy.orEmpty().takeIf { it.isNotBlank() }?.let { "Created by $it" }.orEmpty(),
            technicianName = task.technician?.name.orEmpty(),
            technicianPhone = task.technician?.phone.orEmpty(),
            technicianNote = if (task.technician?.name.isNullOrBlank()) "" else "Assigned technician",
            equipmentName = task.task?.equipment?.name.orEmpty(),
            equipmentMake = task.task?.equipment?.make.orEmpty(),
            equipmentModel = task.task?.equipment?.model.orEmpty(),
            serialNumber = task.task?.equipment?.serialNumber.orEmpty(),
            egSerial = task.task?.equipment?.egSerialNumber.orEmpty(),
            equipmentLocation = task.task?.equipment?.location.orEmpty(),
            callType = task.task?.callType.orEmpty().replace('_', ' ').uppercase(),
            issue = task.task?.name.orEmpty(),
            issueDescription = task.task?.description.orEmpty(),
            serviceDetails = task.task?.actionTaken.orEmpty(),
            servicedToSatisfaction = yesNo(task.task?.serviceSatisfactory),
            runningSmoothly = yesNo(task.task?.isRunningSmoothly),
            comments = task.task?.feedback ?: task.task?.notes?.toString().orEmpty(),
            rating = task.task?.rating?.toString().orEmpty(),
            timeline = listOf(
                TimelineEventState(
                    date = task.task?.createdAt?.take(10).orEmpty(),
                    time = "",
                    title = "Task created",
                    description = "${task.task?.name ?: "Task"} was raised${task.createdBy?.let { " by $it" } ?: ""}."
                )
            )
        )
    }

    private fun applyReport(report: ServiceReport, keepTaskFallback: Boolean = false) {
        val current = formState.value
        formState.value = current.copy(
            reportNumber = report.serviceReportNumber.orEmpty().ifBlank { current.reportNumber },
            taskId = report.taskId.orEmpty().ifBlank { if (keepTaskFallback) current.taskId else "" },
            status = report.status.orEmpty().ifBlank { current.status },
            generatedAt = DateConverter.convertToLocalUtcAndFormat(report.generatedAt).takeIf { it != "-" }.orEmpty(),
            companyName = report.companyName.orEmpty().ifBlank { current.companyName },
            companyBranch = report.companyBranch.orEmpty().ifBlank { current.companyBranch },
            location = report.location.orEmpty().ifBlank { current.location },
            companyEmail = report.companyEmail.orEmpty().ifBlank { current.companyEmail },
            companyLink = report.companyLink ?: current.companyLink,
            raisedByName = report.raisedByName.orEmpty().ifBlank { current.raisedByName },
            raisedByPhone = report.raisedByPhone.orEmpty().ifBlank { current.raisedByPhone },
            raisedByEmail = report.raisedByEmail.orEmpty().ifBlank { current.raisedByEmail },
            raisedByNote = report.raisedByNote.orEmpty().ifBlank { current.raisedByNote },
            technicianName = report.technicianName.orEmpty().ifBlank { current.technicianName },
            technicianPhone = report.technicianPhone.orEmpty().ifBlank { current.technicianPhone },
            technicianNote = report.technicianNote.orEmpty().ifBlank { current.technicianNote },
            equipmentName = report.equipmentName.orEmpty().ifBlank { current.equipmentName },
            equipmentMake = report.equipmentMake.orEmpty().ifBlank { current.equipmentMake },
            equipmentModel = report.equipmentModel.orEmpty().ifBlank { current.equipmentModel },
            serialNumber = report.equipmentSerialNumber.orEmpty().ifBlank { current.serialNumber },
            egSerial = report.equipmentEgSerialNumber.orEmpty().ifBlank { current.egSerial },
            equipmentLocation = report.equipmentLocation.orEmpty().ifBlank { current.equipmentLocation },
            callType = report.callType.orEmpty().ifBlank { current.callType },
            issue = report.issue.orEmpty().ifBlank { current.issue },
            issueDescription = report.issueDescription.orEmpty().ifBlank { current.issueDescription },
            serviceDetails = report.serviceDetails.orEmpty().ifBlank { current.serviceDetails },
            timeline = report.timelineEvents.orEmpty().map {
                TimelineEventState(
                    date = it.date.orEmpty(),
                    time = it.time.orEmpty(),
                    title = it.title.orEmpty(),
                    description = it.description.orEmpty()
                )
            }.ifEmpty { current.timeline },
            servicedToSatisfaction = report.servicedToSatisfaction.orEmpty().ifBlank { current.servicedToSatisfaction },
            runningSmoothly = report.runningSmoothly.orEmpty().ifBlank { current.runningSmoothly },
            comments = report.customerComments.orEmpty().ifBlank { current.comments },
            rating = report.rating.orEmpty().ifBlank { current.rating },
            cost = report.cost?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: current.cost
        )
        report.id?.let { reportId = it }
    }

    private fun saveReport() {
        if (isSubmitting || formState.value.viewOnly) return
        val company = formState.value.companyName.trim()
        if (company.isBlank()) {
            Toast.makeText(requireActivity(), "Company/Customer is required", Toast.LENGTH_SHORT).show()
            return
        }
        val token = sharedPreferencesHelper.getValueString(ConstantValues.AuthToken)
        if (token.isNullOrBlank()) {
            Toast.makeText(requireActivity(), "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
            return
        }
        isSubmitting = true
        waitingForSave = true
        viewModel.saveServiceReport(toBody(), token, reportId != null)
    }

    private fun downloadPdf() {
        val id = reportId
        val token = sharedPreferencesHelper.getValueString(ConstantValues.AuthToken)
        if (id == null) {
            Toast.makeText(requireActivity(), "Save the service report before generating the PDF", Toast.LENGTH_SHORT).show()
            return
        }
        if (token.isNullOrBlank()) return
        waitingForPdf = true
        val body = JsonObject()
        body.addProperty("id", id)
        viewModel.downloadSavedServiceReport(body, token)
    }

    private fun toBody(): JsonObject {
        val state = formState.value
        val body = JsonObject()
        reportId?.let { body.addProperty("id", it) }
        body.addProperty("task_id", state.taskId.trim())
        body.addProperty("status", state.status.ifBlank { "Not Started" })
        state.companyLink?.let { body.addProperty("company_link", it) }
        body.addProperty("company_name", state.companyName.trim())
        body.addProperty("company_branch", state.companyBranch.trim())
        body.addProperty("location", state.location.trim())
        body.addProperty("company_email", state.companyEmail.trim())
        body.addProperty("raised_by_name", state.raisedByName.trim())
        body.addProperty("raised_by_phone", state.raisedByPhone.trim())
        body.addProperty("raised_by_email", state.raisedByEmail.trim())
        body.addProperty("raised_by_note", state.raisedByNote.trim())
        body.addProperty("technician_name", state.technicianName.trim())
        body.addProperty("technician_phone", state.technicianPhone.trim())
        body.addProperty("technician_note", state.technicianNote.trim())
        body.addProperty("equipment_name", state.equipmentName.trim())
        body.addProperty("equipment_make", state.equipmentMake.trim())
        body.addProperty("equipment_model", state.equipmentModel.trim())
        body.addProperty("equipment_serial_number", state.serialNumber.trim())
        body.addProperty("equipment_eg_serial_number", state.egSerial.trim())
        body.addProperty("equipment_location", state.equipmentLocation.trim())
        body.addProperty("call_type", state.callType.trim())
        body.addProperty("issue", state.issue.trim())
        body.addProperty("issue_description", state.issueDescription.trim())
        body.addProperty("service_details", state.serviceDetails.trim())
        body.addProperty("customer_serviced_to_satisfaction", state.servicedToSatisfaction.trim())
        body.addProperty("equipment_running_smoothly", state.runningSmoothly.trim())
        body.addProperty("customer_comments", state.comments.trim())
        body.addProperty("rating", state.rating.trim())
        body.addProperty("cost", state.cost.trim())
        val events = JsonArray()
        state.timeline.forEach { event ->
            val item = JsonObject()
            item.addProperty("date", event.date)
            item.addProperty("time", event.time)
            item.addProperty("title", event.title)
            item.addProperty("description", event.description)
            events.add(item)
        }
        body.add("timeline_events", events)
        return body
    }

    private fun showCompanyPicker() {
        val dialog = BottomSheetDialog(requireActivity())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_amc_layout, null)
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        viewModel.allAmcDataResponse.observe(viewLifecycleOwner) { data ->
            recyclerView.layoutManager = LinearLayoutManager(requireActivity())
            recyclerView.adapter = UserCompaniesAdapter(data.data.activeCompanies()) { selected: AMCData ->
                formState.value = formState.value.copy(
                    companyName = selected.name.orEmpty(),
                    companyBranch = selected.branchName.orEmpty(),
                    location = selected.location.orEmpty(),
                    companyEmail = selected.email.orEmpty(),
                    companyLink = selected.id
                )
                dialog.dismiss()
            }
        }
        dialog.setContentView(view)
        dialog.show()
        sharedPreferencesHelper.getValueString(ConstantValues.AuthToken)?.let { viewModel.getAllAmc(it) }
    }

    private fun pickOption(title: String, options: List<String>, onPicked: (String) -> Unit) {
        AlertDialog.Builder(requireActivity())
            .setTitle(title)
            .setItems(options.toTypedArray()) { _, which -> onPicked(options[which]) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun yesNo(value: Any?): String {
        return when (value) {
            true, "true", "Yes", "yes" -> "Yes"
            false, "false", "No", "no" -> "No"
            else -> ""
        }
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

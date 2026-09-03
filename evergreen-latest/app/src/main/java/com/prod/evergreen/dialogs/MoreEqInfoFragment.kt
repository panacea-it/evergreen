package com.prod.evergreen.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.app.ui.task.TaskDetailsData
import com.example.app.ui.task.TaskDetailsScreen
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.prod.evergreen.R
import com.prod.evergreen.XApplication
import com.prod.evergreen.api.MainRepository
import com.prod.evergreen.api.MainViewModel
import com.prod.evergreen.api.MyViewModelFactory
import com.prod.evergreen.api.RetrofitService
import com.prod.evergreen.helper.ConstantValues
import com.prod.evergreen.helper.DateConverter
import com.prod.evergreen.helper.ProgressDialogUtil
import com.prod.evergreen.helper.RoleAccess
import com.prod.evergreen.helper.SharedPreferencesHelper
import com.prod.evergreen.models.CompanyData
import com.prod.evergreen.models.TaskCreated
import com.prod.evergreen.models.TasksItem

private const val ARG_TASK_ITEM = "arg_task_item"
private const val ARG_COMPANY_DATA = "arg_company_data"

class MoreEqInfoFragment : DialogFragment() {
    private var tasksItem: String? = null
    private var companyData: String? = null
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var viewModel: MainViewModel
    private var createdTask: TaskCreated? = null
    private var pendingMutation: String? = null
    private val detailsState = mutableStateOf(TaskDetailsData())
    private val canManage = mutableStateOf(false)
    private val canAssign = mutableStateOf(false)
    private val canReport = mutableStateOf(false)
    private var pendingAssign = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
        arguments?.let {
            tasksItem = it.getString(ARG_TASK_ITEM)
            companyData = it.getString(ARG_COMPANY_DATA)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        sharedPreferencesHelper = SharedPreferencesHelper(requireActivity())
        val role = sharedPreferencesHelper.getValueString(ConstantValues.TYPE_ROLE)
        canManage.value = RoleAccess.canManageTasks(role)
        canAssign.value = RoleAccess.canAssignTechnician(role)
        canReport.value = RoleAccess.canGenerateServiceReport(role)
        detailsState.value = parseDetails()
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val task by detailsState
                val showActions by canManage
                val assign by canAssign
                val report by canReport
                TaskDetailsScreen(
                    task = task,
                    showActions = showActions,
                    showAssign = assign && task.statusKey != "closed",
                    showReport = report,
                    assignLabel = if (task.technician.isBlank()) "Assign Technician" else "Reassign Technician",
                    onCloseClick = { dismiss() },
                    onEditTaskClick = { openEditTaskForm() },
                    onDeleteTaskClick = { confirmDeleteTask() },
                    onAssignClick = { showTechnicianPicker() },
                    onReportClick = {
                        createdTask?.let { task ->
                            dismiss()
                            com.prod.evergreen.helper.ServiceReportNav.openFromTask(requireActivity(), task)
                        } ?: requestServiceReport()
                    }
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViewmodel()
        viewModel.loading.observe(viewLifecycleOwner) { loading ->
            if (loading) {
                ProgressDialogUtil.showProgressDialog(requireActivity(), "Loading")
            } else {
                ProgressDialogUtil.hideProgressDialog()
            }
        }
        viewModel.changePasswordDataResponse.observe(viewLifecycleOwner) { data ->
            if (pendingMutation == null) return@observe
            Toast.makeText(requireActivity(), data.message ?: "Updated", Toast.LENGTH_SHORT).show()
            if (data.status_code == 200) {
                if (pendingMutation == "delete") {
                    pendingMutation = null
                    dismiss()
                    return@observe
                }
            }
            pendingMutation = null
        }
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            pendingMutation = null
            pendingAssign = false
            Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show()
        }
        viewModel.assignTechnicianDataResponse.observe(viewLifecycleOwner) { data ->
            if (!isAdded) return@observe
            Toast.makeText(requireActivity(), data.message ?: "Technician assigned", Toast.LENGTH_SHORT).show()
            if (data.status_code == 200) {
                dismiss()
            }
        }
        viewModel.allUsersDataResponse.observe(viewLifecycleOwner) { data ->
            if (!pendingAssign) return@observe
            pendingAssign = false
            val technicians = data.data.orEmpty().filter {
                it.access_level.equals("technician", ignoreCase = true)
            }
            if (technicians.isEmpty()) {
                Toast.makeText(requireActivity(), "No technicians available to assign", Toast.LENGTH_SHORT).show()
                return@observe
            }
            val labels = technicians.map { tech ->
                val phone = tech.phone.orEmpty()
                if (phone.isBlank()) tech.name.orEmpty() else "${tech.name} ($phone)"
            }.toTypedArray()
            AlertDialog.Builder(requireActivity())
                .setTitle("Assign Technician")
                .setItems(labels) { _, which ->
                    val chosen = technicians[which]
                    val taskId = resolveTaskId()
                    val token = sharedPreferencesHelper.getValueString(ConstantValues.AuthToken)
                    if (token.isNullOrBlank() || taskId == 0 || chosen.id == null) {
                        Toast.makeText(requireActivity(), "Unable to assign this task", Toast.LENGTH_SHORT).show()
                        return@setItems
                    }
                    val body = JsonObject()
                    body.addProperty("task_link", taskId)
                    body.addProperty("technician_link", chosen.id)
                    viewModel.assignTechnician(body, token)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
        viewModel.downloadpdf.observe(viewLifecycleOwner) { response ->
            if (!isAdded) return@observe
            com.prod.evergreen.helper.ServiceReportHelper.offer(requireActivity(), response)
        }
    }

    private fun parseDetails(): TaskDetailsData {
        val gson = Gson()
        val created = try {
            gson.fromJson(tasksItem, TaskCreated::class.java)
        } catch (_: Exception) {
            null
        }
        val legacy = if (created?.task == null) {
            try {
                gson.fromJson(tasksItem, TasksItem::class.java)
            } catch (_: Exception) {
                null
            }
        } else {
            null
        }
        createdTask = created?.takeIf { it.task != null || it.id != 0 }

        var companyName = ""
        var location = ""
        if (!companyData.isNullOrEmpty() && companyData != "null") {
            try {
                val company = gson.fromJson(companyData, CompanyData::class.java)
                companyName = company?.name.orEmpty()
                location = company?.location.orEmpty()
            } catch (_: Exception) {
            }
        }
        if (companyName.isBlank()) {
            val company = created?.task?.equipment?.company
            companyName = company?.name.orEmpty()
            location = company?.location.orEmpty()
        }

        val statusKey = created?.status ?: legacy?.status
        return TaskDetailsData(
            companyName = companyName,
            location = location,
            clientAdminName = created?.client?.name ?: legacy?.client?.name.orEmpty(),
            clientAdminPhone = created?.client?.phone ?: legacy?.client?.phone.orEmpty(),
            title = created?.task?.name ?: legacy?.task?.name.orEmpty(),
            description = created?.task?.description ?: legacy?.task?.description.orEmpty(),
            technician = created?.technician?.name ?: legacy?.technician?.name.orEmpty(),
            technicianPhone = created?.technician?.phone ?: legacy?.technician?.phone.orEmpty(),
            created = DateConverter.convertToLocalUtcAndFormat(
                created?.task?.createdAt ?: legacy?.task?.createdAt
            ),
            lastUpdate = DateConverter.convertToLocalUtcAndFormat(
                created?.task?.updatedAt ?: legacy?.task?.updatedAt
            ),
            status = statusLabel(statusKey),
            statusKey = statusKey.orEmpty(),
            imageUrl = created?.task?.image?.firstOrNull()
                ?: legacy?.task?.image?.firstOrNull()
        )
    }

    private fun statusLabel(status: String?): String {
        return when (status) {
            "open" -> "Not Started"
            "in_progress" -> "In Progress"
            "closed" -> "Closed"
            "hold" -> "Hold"
            else -> status.orEmpty()
        }
    }

    private fun resolveTaskId(): Int {
        val task = createdTask ?: return 0
        return listOfNotNull(task.task?.id, task.taskLink).firstOrNull { it != 0 } ?: task.id
    }

    private fun openEditTaskForm() {
        if (!canManage.value) {
            Toast.makeText(requireActivity(), "You cannot edit this task", Toast.LENGTH_SHORT).show()
            return
        }
        val payload = createdTask?.let { Gson().toJson(it) } ?: tasksItem
        if (payload.isNullOrBlank()) {
            Toast.makeText(requireActivity(), "Unable to edit this task", Toast.LENGTH_SHORT).show()
            return
        }
        com.prod.evergreen.helper.DashboardNav.pendingTaskJson = payload
        dismiss()
        startActivity(
            android.content.Intent(requireActivity(), com.prod.evergreen.activities.MainActivity::class.java)
                .addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .putExtra("open_create_task", true)
        )
    }

    private fun showEditTaskDialog() {
        if (!canManage.value) {
            Toast.makeText(requireActivity(), "You cannot edit this task", Toast.LENGTH_SHORT).show()
            return
        }
        val current = detailsState.value
        val container = android.widget.LinearLayout(requireActivity()).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(40, 20, 40, 10)
        }
        val subject = android.widget.EditText(requireActivity()).apply {
            hint = "Subject"
            setText(current.title)
        }
        val description = android.widget.EditText(requireActivity()).apply {
            hint = "Description"
            setText(current.description)
        }
        container.addView(subject)
        container.addView(description)
        AlertDialog.Builder(requireActivity())
            .setTitle("Edit Task")
            .setView(container)
            .setPositiveButton("Update") { _, _ ->
                val token = sharedPreferencesHelper.getValueString(ConstantValues.AuthToken)
                val taskId = resolveTaskId()
                if (token.isNullOrBlank() || taskId == 0) {
                    Toast.makeText(requireActivity(), "Unable to update this task", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (subject.text.toString().isBlank()) {
                    Toast.makeText(requireActivity(), "Please Enter Subject", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val name = subject.text.toString().trim()
                val desc = description.text.toString().trim()
                val body = JsonObject()
                body.addProperty("task_link", taskId)
                body.addProperty("name", name)
                body.addProperty("description", desc)
                pendingMutation = "edit"
                detailsState.value = current.copy(title = name, description = desc)
                viewModel.updateTask(body, token)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showTechnicianPicker() {
        if (!canAssign.value) {
            Toast.makeText(requireActivity(), "You cannot assign this task", Toast.LENGTH_SHORT).show()
            return
        }
        val token = sharedPreferencesHelper.getValueString(ConstantValues.AuthToken)
        if (token.isNullOrBlank()) {
            Toast.makeText(requireActivity(), "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
            return
        }
        pendingAssign = true
        val body = JsonObject()
        body.addProperty("access_level", "technician")
        viewModel.getAllUsers(token, body)
    }

    private fun requestServiceReport() {
        if (!canReport.value) {
            Toast.makeText(requireActivity(), "You cannot generate a service report", Toast.LENGTH_SHORT).show()
            return
        }
        val token = sharedPreferencesHelper.getValueString(ConstantValues.AuthToken)
        val taskId = resolveTaskId()
        if (token.isNullOrBlank() || taskId == 0) {
            Toast.makeText(requireActivity(), "Unable to generate service report", Toast.LENGTH_SHORT).show()
            return
        }
        Toast.makeText(requireActivity(), "Generating service report...", Toast.LENGTH_SHORT).show()
        val body = JsonObject()
        body.addProperty("task_link", taskId)
        viewModel.getServiceReport(body, token)
    }

    private fun confirmDeleteTask() {
        if (!canManage.value) {
            Toast.makeText(requireActivity(), "You cannot delete this task", Toast.LENGTH_SHORT).show()
            return
        }
        AlertDialog.Builder(requireActivity())
            .setTitle("Delete Task")
            .setMessage("Delete this task?")
            .setPositiveButton("Delete") { _, _ ->
                val token = sharedPreferencesHelper.getValueString(ConstantValues.AuthToken)
                val taskId = resolveTaskId()
                if (token.isNullOrBlank() || taskId == 0) {
                    Toast.makeText(requireActivity(), "Unable to delete this task", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val body = JsonObject()
                body.addProperty("task_link", taskId)
                pendingMutation = "delete"
                viewModel.deleteTask(body, token)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setViewmodel() {
        val repository = MainRepository(
            RetrofitService.getInstance(requireActivity()),
            XApplication.database.newsDao(),
            XApplication.database.companyDao()
        )
        viewModel = ViewModelProvider(this, MyViewModelFactory(repository))[MainViewModel::class.java]
    }

    companion object {
        @JvmStatic
        fun newInstance(tasksItem: String, companyData: String? = null) =
            MoreEqInfoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TASK_ITEM, tasksItem)
                    putString(ARG_COMPANY_DATA, companyData)
                }
            }
    }
}

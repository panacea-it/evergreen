package com.prod.evergreen.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.app.ui.task.TaskItem
import com.example.app.ui.task.TaskListScreen
import com.example.app.ui.task.defaultStatusCounts
import com.example.app.ui.task.taskStatusKeys
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.prod.evergreen.R
import com.prod.evergreen.XApplication
import com.prod.evergreen.activities.MainActivity
import com.prod.evergreen.activities.NotificationList
import com.prod.evergreen.activities.QrScanner
import com.prod.evergreen.adapters.ItemAdapter
import com.prod.evergreen.api.MainRepository
import com.prod.evergreen.api.MainViewModel
import com.prod.evergreen.api.MyViewModelFactory
import com.prod.evergreen.api.RetrofitService
import com.prod.evergreen.api.SharedViewModel
import com.prod.evergreen.dialogs.MoreEqInfoFragment
import com.prod.evergreen.helper.ConstantValues
import com.prod.evergreen.helper.DashboardNav
import com.prod.evergreen.helper.DateConverter
import com.prod.evergreen.helper.TabNav
import com.prod.evergreen.helper.ProgressDialogUtil
import com.prod.evergreen.helper.RoleAccess
import com.prod.evergreen.helper.SharedPreferencesHelper
import com.prod.evergreen.models.AMCData
import com.prod.evergreen.models.TaskCreated

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class TaskFragment : Fragment() {
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var viewModel: MainViewModel
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    val items = listOf("Not Started", "Hold", "In Progress", "Done")
    val task_status = listOf("open", "hold", "in_progress", "closed")
    private var token: String? = null
    private var param1: String? = null
    private var param2: String? = null
    private val selectedTab = mutableIntStateOf(0)
    private val searchQuery = mutableStateOf("")
    private val allTasks = mutableStateOf<List<TaskCreated>>(emptyList())
    private val openCount = mutableIntStateOf(0)
    private val holdCount = mutableIntStateOf(0)
    private val inProgressCount = mutableIntStateOf(0)
    private val closedCount = mutableIntStateOf(0)
    private val companyFilterId = mutableStateOf<Int?>(null)
    private val companyFilterName = mutableStateOf<String?>(null)
    private var pendingAssignTask: TaskCreated? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        sharedPreferencesHelper = SharedPreferencesHelper(requireActivity())
        token = sharedPreferencesHelper.getValueString(ConstantValues.AuthToken)
        val role = sharedPreferencesHelper.getValueString(ConstantValues.TYPE_ROLE)
        val canAssign = RoleAccess.canAssignTechnician(role)
        val canReport = RoleAccess.canGenerateServiceReport(role)
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val tab by selectedTab
                val query by searchQuery
                val source by allTasks
                val companyId by companyFilterId
                val companyName by companyFilterName
                TaskListScreen(
                    tasks = source.toUiTasks(query, companyId),
                    statusCounts = defaultStatusCounts(
                        open = openCount.intValue,
                        hold = holdCount.intValue,
                        inProgress = inProgressCount.intValue,
                        closed = closedCount.intValue
                    ),
                    selectedTab = tab,
                    searchQuery = query,
                    onSearchQueryChange = { searchQuery.value = it },
                    onTabSelected = { selectTab(it) },
                    onTaskClick = { item -> openTaskDetails(item.id) },
                    onStatusClick = { selectTab(it) },
                    onFilterClick = { showCompanyFilter() },
                    onAddClick = { TabNav.createAmc(this@TaskFragment) },
                    onCreateTaskClick = { onAddTask() },
                    onHomeClick = { TabNav.home(this@TaskFragment) },
                    onMessagesClick = { TabNav.equipment(this@TaskFragment) },
                    onTasksClick = {},
                    onProfileClick = { TabNav.profile(this@TaskFragment) },
                    onScanClick = {
                        startActivity(Intent(requireActivity(), QrScanner::class.java))
                    },
                    onNotificationClick = {
                        startActivity(Intent(requireActivity(), NotificationList::class.java))
                    },
                    onMenuClick = { openDrawer() },
                    filterLabel = companyName,
                    onClearFilter = {
                        companyFilterId.value = null
                        companyFilterName.value = null
                    },
                    showAssignButton = canAssign,
                    showReportButton = canReport,
                    onAssignClick = { item ->
                        allTasks.value.firstOrNull { it.id == item.id }?.let { showTechnicianPicker(it) }
                    },
                    onReportClick = { item ->
                        allTasks.value.firstOrNull { it.id == item.id }?.let { requestServiceReport(it) }
                    }
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViewmodel()

        viewModel.loading.observe(viewLifecycleOwner) { loading ->
            if (loading && allTasks.value.isEmpty()) {
                ProgressDialogUtil.showProgressDialog(requireActivity(), "Loading")
            } else {
                ProgressDialogUtil.hideProgressDialog()
            }
        }

        viewModel.allTasksDataResponse.observe(viewLifecycleOwner) { data ->
            if (!isAdded) return@observe
            if (data?.success == 200) {
                sharedViewModel.setSharedData(data)
                allTasks.value = data.data.orEmpty()
                val count = data.count
                if (count != null) {
                    openCount.intValue = count.open
                    holdCount.intValue = count.hold
                    inProgressCount.intValue = count.in_progress
                    closedCount.intValue = count.closed
                }
            }
        }

        sharedViewModel.sharedData.observe(viewLifecycleOwner) { data ->
            val count = data?.count ?: return@observe
            openCount.intValue = count.open
            holdCount.intValue = count.hold
            inProgressCount.intValue = count.in_progress
            closedCount.intValue = count.closed
        }

        DashboardNav.pendingTaskTab?.let { tab ->
            selectedTab.intValue = tab.coerceIn(0, 3)
            DashboardNav.pendingTaskTab = null
        }
        viewModel.assignTechnicianDataResponse.observe(viewLifecycleOwner) { response ->
            if (!isAdded) return@observe
            Toast.makeText(requireActivity(), response.message ?: "Technician assigned", Toast.LENGTH_SHORT).show()
            if (response.status_code == 200) {
                loadTasks(selectedTab.intValue)
            }
        }
        viewModel.allUsersDataResponse.observe(viewLifecycleOwner) { data ->
            val task = pendingAssignTask ?: return@observe
            pendingAssignTask = null
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
            androidx.appcompat.app.AlertDialog.Builder(requireActivity())
                .setTitle("Assign Technician")
                .setItems(labels) { _, which ->
                    val chosen = technicians[which]
                    val taskId = com.prod.evergreen.helper.ServiceReportHelper.taskId(task)
                    if (taskId == 0 || chosen.id == null) {
                        Toast.makeText(requireActivity(), "Unable to assign this task", Toast.LENGTH_SHORT).show()
                        return@setItems
                    }
                    val authToken = token
                    if (authToken.isNullOrBlank()) return@setItems
                    val body = JsonObject()
                    body.addProperty("task_link", taskId)
                    body.addProperty("technician_link", chosen.id)
                    viewModel.assignTechnician(body, authToken)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
        viewModel.downloadpdf.observe(viewLifecycleOwner) { response ->
            if (!isAdded) return@observe
            com.prod.evergreen.helper.ServiceReportHelper.offer(requireActivity(), response)
        }
        loadTasks(selectedTab.intValue)
    }

    private var tasksReady = false

    override fun onResume() {
        super.onResume()
        if (!tasksReady) {
            tasksReady = true
            return
        }
        if (::viewModel.isInitialized) {
            loadTasks(selectedTab.intValue)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TaskFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun selectTab(index: Int) {
        if (index == selectedTab.intValue) return
        selectedTab.intValue = index
        searchQuery.value = ""
        loadTasks(index)
    }

    private fun loadTasks(index: Int) {
        val authToken = token
        if (authToken.isNullOrBlank()) {
            Toast.makeText(requireActivity(), "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
            return
        }
        val body = JsonObject()
        body.addProperty("status", taskStatusKeys.getOrElse(index) { "open" })
        viewModel.getAllTasks(authToken, body)
    }

    private fun List<TaskCreated>.toUiTasks(query: String, companyId: Int?): List<TaskItem> {
        val needle = query.trim()
        return asSequence()
            .filter { created ->
                companyId == null ||
                    created.task?.equipment?.company?.id == companyId ||
                    created.task?.equipment?.companyLink == companyId
            }
            .map { it.toUiTask() }
            .filter { task ->
                needle.isEmpty() ||
                    task.title.contains(needle, ignoreCase = true) ||
                    task.equipmentName.contains(needle, ignoreCase = true) ||
                    task.serialNumber.contains(needle, ignoreCase = true) ||
                    task.company.contains(needle, ignoreCase = true)
            }
            .toList()
    }

    private fun TaskCreated.toUiTask(): TaskItem {
        val task = task
        return TaskItem(
            id = id,
            title = task?.name.orEmpty().ifBlank { "-" },
            type = task?.callType?.takeIf { it.isNotBlank() }
                ?: task?.description.orEmpty().ifBlank { "-" },
            status = when (status) {
                "open" -> "Not Started"
                "in_progress" -> "In Progress"
                "hold" -> "Hold"
                "closed" -> "Closed"
                else -> status.orEmpty()
            },
            statusKey = status.orEmpty(),
            equipmentName = task?.equipment?.name.orEmpty(),
            serialNumber = task?.equipment?.serialNumber.orEmpty(),
            company = task?.equipment?.company?.name.orEmpty(),
            createdAt = DateConverter.convertToLocalUtcAndFormat(task?.createdAt),
            imageUrl = task?.image?.firstOrNull(),
            technicianAssigned = !RoleAccess.isUnassigned(technicianLink)
        )
    }

    private fun showTechnicianPicker(task: TaskCreated) {
        val authToken = token
        if (authToken.isNullOrBlank()) {
            Toast.makeText(requireActivity(), "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
            return
        }
        pendingAssignTask = task
        val body = JsonObject()
        body.addProperty("access_level", "technician")
        viewModel.getAllUsers(authToken, body)
    }

    private fun requestServiceReport(task: TaskCreated) {
        val authToken = token
        val taskId = com.prod.evergreen.helper.ServiceReportHelper.taskId(task)
        if (authToken.isNullOrBlank() || taskId == 0) {
            Toast.makeText(requireActivity(), "Unable to generate service report", Toast.LENGTH_SHORT).show()
            return
        }
        Toast.makeText(requireActivity(), "Generating service report...", Toast.LENGTH_SHORT).show()
        val body = JsonObject()
        body.addProperty("task_link", taskId)
        viewModel.getServiceReport(body, authToken)
    }

    private fun openTaskDetails(taskId: Int) {
        val task = allTasks.value.firstOrNull { it.id == taskId } ?: return
        try {
            val gson = Gson()
            MoreEqInfoFragment.newInstance(
                gson.toJson(task),
                gson.toJson(task.task?.equipment?.company)
            ).show(childFragmentManager, "")
        } catch (_: Exception) {
            Toast.makeText(requireActivity(), "Unable to open task details", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onAddTask() {
        val role = sharedPreferencesHelper.getValueString(ConstantValues.TYPE_ROLE)
        if (RoleAccess.canManageTasks(role)) {
            goTo(R.id.createTaskFragment, "Create Task")
        } else {
            Toast.makeText(requireActivity(), "You cannot create a task", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showCompanyFilter() {
        val accessType = sharedPreferencesHelper.getValueString(ConstantValues.TYPE_ROLE)
        if (RoleAccess.lockToAttachedCompany(accessType)) {
            Toast.makeText(
                requireActivity(),
                sharedPreferencesHelper.getValueString(ConstantValues.COMPANYNAME) ?: "Your company",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        showBottomSheetDialog { company ->
            companyFilterId.value = company.id
            companyFilterName.value = company.name?.takeIf { it.isNotBlank() } ?: "Company"
        }
    }

    private fun goTo(destinationId: Int, title: String) {
        findNavController().navigate(destinationId)
        (activity as? MainActivity)?.setTitleTextView(title)
    }

    private fun openDrawer() {
        (activity as? MainActivity)
            ?.findViewById<androidx.drawerlayout.widget.DrawerLayout>(R.id.drawer_layout)
            ?.openDrawer(GravityCompat.START)
    }

    private fun setViewmodel() {
        val repository = MainRepository(
            RetrofitService.getInstance(requireActivity()),
            XApplication.database.newsDao(),
            XApplication.database.companyDao()
        )
        val viewModelFactory = MyViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]
    }

    private fun showBottomSheetDialog(onItemSelected: (AMCData) -> Unit) {
        val dialog = BottomSheetDialog(requireActivity(), R.style.NoBackgroundDialogTheme)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_amc_layout, null)
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        val searchView: SearchView = view.findViewById(R.id.searchView)
        val authToken = token
        if (!authToken.isNullOrBlank()) {
            viewModel.getAllAmc(authToken)
        }
        viewModel.allAmcDataResponse.observe(viewLifecycleOwner) { data ->
            val adapter = ItemAdapter(data.data.orEmpty()) { selectedItem ->
                onItemSelected(selectedItem)
                dialog.dismiss()
            }
            recyclerView.layoutManager = LinearLayoutManager(requireActivity())
            recyclerView.adapter = adapter
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean = false
                override fun onQueryTextChange(newText: String?): Boolean {
                    adapter.filter.filter(newText)
                    return false
                }
            })
        }
        dialog.setContentView(view)
        val layoutParams = view.layoutParams
        layoutParams.height = (resources.displayMetrics.heightPixels * 0.8).toInt()
        view.layoutParams = layoutParams
        dialog.show()
    }
}

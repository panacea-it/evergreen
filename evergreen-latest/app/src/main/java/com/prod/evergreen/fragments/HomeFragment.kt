package com.prod.evergreen.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.app.ui.home.DashboardStat
import com.example.app.ui.home.HomeActions
import com.example.app.ui.home.HomeScreen
import com.example.app.ui.home.HomeUiState
import com.example.app.ui.home.defaultAccessStats
import com.example.app.ui.home.defaultStatusStats
import com.prod.evergreen.R
import com.prod.evergreen.XApplication
import com.prod.evergreen.activities.MainActivity
import com.prod.evergreen.activities.NotificationList
import com.prod.evergreen.activities.QrScanner
import com.prod.evergreen.api.MainRepository
import com.prod.evergreen.api.MainViewModel
import com.prod.evergreen.api.MyViewModelFactory
import com.prod.evergreen.api.NetworkState
import com.prod.evergreen.api.RetrofitService
import com.prod.evergreen.helper.ConstantValues
import com.prod.evergreen.helper.DashboardNav
import com.prod.evergreen.helper.ProgressDialogUtil
import com.prod.evergreen.helper.TabNav
import com.prod.evergreen.helper.RoleAccess
import com.prod.evergreen.helper.SharedPreferencesHelper
import com.prod.evergreen.models.DataItem1
import com.prod.evergreen.models.DataItem2

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class HomeFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var viewModel: MainViewModel
    private val homeState = mutableStateOf(HomeUiState())

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
        val role = sharedPreferencesHelper.getValueString(ConstantValues.TYPE_ROLE)
        homeState.value = homeState.value.copy(
            greetingName = displayName(role),
            showAccessStats = role == "eg_super_admin" || role == "eg_admin"
        )

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val state by homeState
                HomeScreen(
                    state = state,
                    actions = HomeActions(
                        onMenuClick = { openDrawer() },
                        onScanClick = {
                            startActivity(Intent(requireActivity(), QrScanner::class.java))
                        },
                        onNotificationsClick = {
                            startActivity(Intent(requireActivity(), NotificationList::class.java))
                        },
                        onAccessStatClick = { index -> openUsers(index) },
                        onStatusStatClick = { index -> openTasks(index) },
                        onHomeClick = {},
                        onEquipmentClick = { TabNav.equipment(this@HomeFragment) },
                        onAddClick = { TabNav.createAmc(this@HomeFragment) },
                        onTasksClick = { TabNav.tasks(this@HomeFragment) },
                        onProfileClick = { TabNav.profile(this@HomeFragment) }
                    )
                )
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) = HomeFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_PARAM1, param1)
                putString(ARG_PARAM2, param2)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViewmodel()
        val token = sharedPreferencesHelper.getValueString(ConstantValues.AuthToken)
        val accesslevel = sharedPreferencesHelper.getValueString(ConstantValues.TYPE_ROLE)

        if (accesslevel == "eg_super_admin" || accesslevel == "eg_admin") {
            viewModel.fetchStats(token!!)
        } else {
            viewModel.getAllTaskCountAPI(token!!)
        }

        viewModel.loading.observe(viewLifecycleOwner) { data ->
            val empty = homeState.value.statusStats.all { it.value == "0" }
            if (data && empty) {
                ProgressDialogUtil.showProgressDialog(requireActivity(), "Loading")
            } else {
                ProgressDialogUtil.hideProgressDialog()
            }
        }

        viewModel.companiesStatsResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is NetworkState.Success -> {
                    val companiesStatsData = response.data
                    val dataItems: List<DataItem1> = companiesStatsData.data.orEmpty()
                    if (dataItems.isNotEmpty()) {
                        homeState.value = homeState.value.copy(
                            chartTitle = "Companies onboarded",
                            chartLabels = dataItems.map { it.month.orEmpty() },
                            chartValues = dataItems.map { it.value?.toFloat() ?: 0f }
                        )
                    }
                }
                is NetworkState.Error -> { }
            }
        }

        viewModel.userStatsResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is NetworkState.Success -> {
                    val dataItems: List<DataItem2> = response.data.data.orEmpty()
                    val counts = mutableMapOf(
                        "Client Admins" to 0,
                        "Clients" to 0,
                        "Evergreen Managers" to 0,
                        "Technicians" to 0
                    )
                    for (entry in dataItems) {
                        val count = entry.count ?: 0
                        when (entry.accessLevel) {
                            "technician" -> counts["Technicians"] = count
                            "client_admin" -> counts["Client Admins"] = count
                            "client" -> counts["Clients"] = count
                            "eg_admin", "eg_super_admin" ->
                                counts["Evergreen Managers"] = (counts["Evergreen Managers"] ?: 0) + count
                        }
                    }
                    homeState.value = homeState.value.copy(
                        accessStats = defaultAccessStats.map { stat ->
                            stat.copy(value = (counts[stat.title] ?: 0).toString())
                        }
                    )
                }
                is NetworkState.Error -> { }
            }
        }

        viewModel.getAllTaskCountResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is NetworkState.Success -> {
                    val data = response.data.data ?: return@observe
                    val open = data.open ?: 0
                    val hold = data.hold ?: 0
                    val inProgress = data.inProgress ?: 0
                    val closed = data.closed ?: 0
                    val active = open + hold + inProgress
                    homeState.value = homeState.value.copy(
                        statusStats = defaultStatusStats
                            .update("Open", open.toString())
                            .update("Hold", hold.toString())
                            .update("In Progress", inProgress.toString())
                            .update("Closed", closed.toString()),
                        bannerTitle = if (active == 0) {
                            "Everything looks good today!"
                        } else {
                            "You have $active active tickets"
                        },
                        bannerSubtitle = when {
                            open > 0 -> "$open open · $inProgress in progress · $hold on hold"
                            else -> "Closed $closed tickets so far this period."
                        }
                    )
                }
                is NetworkState.Error -> { }
            }
        }
    }

    private fun List<DashboardStat>.update(title: String, value: String): List<DashboardStat> {
        return map { if (it.title == title) it.copy(value = value) else it }
    }

    private fun displayName(role: String?): String {
        val stored = sharedPreferencesHelper.getValueString(ConstantValues.PREF_USERNAME)
            ?.trim()
            ?.substringBefore(" ")
            ?.takeIf { it.isNotBlank() }
        if (!stored.isNullOrBlank()) return stored
        return com.prod.evergreen.helper.RoleLabels.display(role).takeIf { it != "-" } ?: "there"
    }

    private fun openUsers(index: Int) {
        DashboardNav.pendingUserRole = when (index) {
            0 -> com.example.app.ui.users.UserRole.POC
            1 -> com.example.app.ui.users.UserRole.CLIENT
            2 -> com.example.app.ui.users.UserRole.MANAGER
            else -> com.example.app.ui.users.UserRole.TECHNICIAN
        }
        goTo(R.id.amc_mangers, "Users List")
    }

    private fun openTasks(index: Int) {
        DashboardNav.pendingTaskTab = index.coerceIn(0, 3)
        goTo(R.id.taskFragment, "Tasks List")
    }

    private var homeReady = false

    override fun onResume() {
        super.onResume()
        if (!homeReady) {
            homeReady = true
            return
        }
        if (::viewModel.isInitialized) {
            val token = sharedPreferencesHelper.getValueString(ConstantValues.AuthToken) ?: return
            val role = sharedPreferencesHelper.getValueString(ConstantValues.TYPE_ROLE)
            if (role == "eg_super_admin" || role == "eg_admin") {
                viewModel.fetchStats(token)
            } else {
                viewModel.getAllTaskCountAPI(token)
            }
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

    private fun onAddTapped() {
        val role = sharedPreferencesHelper.getValueString(ConstantValues.TYPE_ROLE)
        when {
            RoleAccess.canManageTasks(role) -> goTo(R.id.createTaskFragment, "Create Task")
            RoleAccess.canManageEquipment(role) -> goTo(R.id.addEquipmentFragment, "Add Equipment")
            else -> goTo(R.id.taskFragment, "Tasks List")
        }
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
}

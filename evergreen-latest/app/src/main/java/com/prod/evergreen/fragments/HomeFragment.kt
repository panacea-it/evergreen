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
import com.prod.evergreen.helper.ProgressDialogUtil
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
            greetingName = greetingForRole(role),
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
                        onAccessStatClick = { goTo(R.id.amc_mangers, "Users List") },
                        onStatusStatClick = { goTo(R.id.taskFragment, "Tasks List") },
                        onHomeClick = {},
                        onMessagesClick = {
                            startActivity(Intent(requireActivity(), NotificationList::class.java))
                        },
                        onAddClick = { onAddTapped() },
                        onTasksClick = { goTo(R.id.taskFragment, "Tasks List") },
                        onProfileClick = {
                            startActivity(Intent(requireActivity(), com.prod.evergreen.activities.UserDetails::class.java))
                        }
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
            if (data) {
                ProgressDialogUtil.showProgressDialog(requireActivity(), "Loading")
            } else {
                ProgressDialogUtil.hideProgressDialog()
            }
        }

        viewModel.companiesStatsResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is NetworkState.Success -> {
                    val companiesStatsData = response.data
                    if (companiesStatsData.status == 200) {
                        val dataItems: List<DataItem1> = companiesStatsData.data.orEmpty()
                        homeState.value = homeState.value.copy(
                            chartTitle = companiesStatsData.message
                                ?: homeState.value.chartTitle,
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
                    val companiesStatsData = response.data
                    if (companiesStatsData.status == 200) {
                        val dataItems: List<DataItem2> = companiesStatsData.data.orEmpty()
                        var stats = defaultAccessStats
                        for (entry in dataItems) {
                            val count = (entry.count ?: 0).toString()
                            stats = when (entry.accessLevel) {
                                "technician" -> stats.update("Technician's", count)
                                "client_admin" -> stats.update("POC's", count)
                                "client" -> stats.update("Client's", count)
                                "eg_admin" -> stats.update("Manager's", count)
                                else -> stats
                            }
                        }
                        homeState.value = homeState.value.copy(accessStats = stats)
                    }
                }
                is NetworkState.Error -> { }
            }
        }

        viewModel.getAllTaskCountResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is NetworkState.Success -> {
                    val companiesStatsData = response.data
                    if (companiesStatsData.success == 200 && companiesStatsData.data != null) {
                        val data = companiesStatsData.data
                        homeState.value = homeState.value.copy(
                            statusStats = defaultStatusStats
                                .update("Open", (data.open ?: 0).toString())
                                .update("Hold", (data.hold ?: 0).toString())
                                .update("In Progress", (data.inProgress ?: 0).toString())
                                .update("Closed", (data.closed ?: 0).toString())
                        )
                    }
                }
                is NetworkState.Error -> { }
            }
        }
    }

    private fun List<DashboardStat>.update(title: String, value: String): List<DashboardStat> {
        return map { if (it.title == title) it.copy(value = value) else it }
    }

    private fun greetingForRole(role: String?): String {
        return when (role) {
            "eg_super_admin" -> "Admin"
            "eg_admin" -> "Manager"
            "client_admin", "client" -> "Client"
            "technician" -> "Technician"
            else -> sharedPreferencesHelper.getValueString(ConstantValues.PREF_USERNAME) ?: "Admin"
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

package com.prod.evergreen.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.app.ui.users.UserListItem
import com.example.app.ui.users.UserRole
import com.example.app.ui.users.UsersListScreen
import com.example.app.ui.users.accessLevel
import com.example.app.ui.users.userRoleFromAccessLevel
import com.google.gson.JsonObject
import com.prod.evergreen.R
import com.prod.evergreen.XApplication
import com.prod.evergreen.activities.AddUser
import com.prod.evergreen.activities.MainActivity
import com.prod.evergreen.activities.NotificationList
import com.prod.evergreen.activities.QrScanner
import com.prod.evergreen.activities.UserDetails
import com.prod.evergreen.api.MainRepository
import com.prod.evergreen.api.MainViewModel
import com.prod.evergreen.api.MyViewModelFactory
import com.prod.evergreen.api.RetrofitService
import com.prod.evergreen.helper.ConstantValues
import com.prod.evergreen.helper.ProgressDialogUtil
import com.prod.evergreen.helper.RoleAccess
import com.prod.evergreen.helper.SharedPreferencesHelper
import com.prod.evergreen.models.Users

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ListAllAMCTechniciansFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var viewModel: MainViewModel
    private val allUsers = mutableStateOf<List<Users>>(emptyList())
    private val selectedRole = mutableStateOf(UserRole.CLIENT)
    private val searchQuery = mutableStateOf("")
    private val clientCount = mutableIntStateOf(0)
    private val pocCount = mutableIntStateOf(0)
    private val technicianCount = mutableIntStateOf(0)
    private val managerCount = mutableIntStateOf(0)

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
        val canAdd = RoleAccess.canManageUsers(
            sharedPreferencesHelper.getValueString(ConstantValues.TYPE_ROLE)
        )
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val source by allUsers
                val role by selectedRole
                val query by searchQuery
                UsersListScreen(
                    users = source.toUiUsers(),
                    clientCount = clientCount.intValue,
                    pocCount = pocCount.intValue,
                    technicianCount = technicianCount.intValue,
                    managerCount = managerCount.intValue,
                    selectedRole = role,
                    onRoleSelected = {
                        selectedRole.value = it
                        searchQuery.value = ""
                        loadUsers(it)
                    },
                    searchQuery = query,
                    onSearchQueryChange = { searchQuery.value = it },
                    showAddUser = canAdd,
                    onUserClick = { item ->
                        source.firstOrNull { it.id?.toString() == item.id }?.let { user ->
                            UserDetails.open(requireActivity(), user)
                        }
                    },
                    onAddUserClick = { onAddUser() },
                    onAddClick = { onAddUser() },
                    onHomeClick = { goTo(R.id.homeFragment, "Home") },
                    onMessagesClick = {
                        startActivity(Intent(requireActivity(), NotificationList::class.java))
                    },
                    onTasksClick = { goTo(R.id.taskFragment, "Tasks List") },
                    onProfileClick = {
                        startActivity(Intent(requireActivity(), UserDetails::class.java))
                    },
                    onScanClick = {
                        startActivity(Intent(requireActivity(), QrScanner::class.java))
                    },
                    onNotificationClick = {
                        startActivity(Intent(requireActivity(), NotificationList::class.java))
                    },
                    onMenuClick = { openDrawer() }
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
        viewModel.allUsersDataResponse.observe(viewLifecycleOwner) { data ->
            allUsers.value = data.data.orEmpty().toList()
            data.count?.let { counts ->
                clientCount.intValue = counts.client
                pocCount.intValue = counts.client_admin
                technicianCount.intValue = counts.technician
                managerCount.intValue = counts.eg_admin
            }
        }
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show()
        }
        loadUsers(selectedRole.value)
    }

    override fun onResume() {
        super.onResume()
        if (::viewModel.isInitialized) {
            loadUsers(selectedRole.value)
        }
    }

    private fun loadUsers(role: UserRole) {
        val token = sharedPreferencesHelper.getValueString(ConstantValues.AuthToken) ?: return
        val body = JsonObject()
        body.addProperty("access_level", role.accessLevel())
        viewModel.getAllUsers(token, body)
    }

    private fun List<Users>.toUiUsers(): List<UserListItem> {
        return mapIndexed { index, user ->
            UserListItem(
                id = user.id?.toString() ?: "user-$index-${user.name}-${user.phone}",
                name = user.name.orEmpty(),
                email = user.email.orEmpty(),
                mobile = user.phone.orEmpty(),
                role = userRoleFromAccessLevel(user.access_level) ?: selectedRole.value
            )
        }
    }

    private fun onAddUser() {
        val role = sharedPreferencesHelper.getValueString(ConstantValues.TYPE_ROLE)
        if (RoleAccess.canManageUsers(role)) {
            startActivity(Intent(requireActivity(), AddUser::class.java))
        } else {
            Toast.makeText(requireActivity(), "You cannot add users", Toast.LENGTH_SHORT).show()
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

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ListAllAMCTechniciansFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun setViewmodel() {
        val repository = MainRepository(
            RetrofitService.getInstance(requireActivity()),
            XApplication.database.newsDao(),
            XApplication.database.companyDao()
        )
        viewModel = ViewModelProvider(this, MyViewModelFactory(repository))[MainViewModel::class.java]
    }
}

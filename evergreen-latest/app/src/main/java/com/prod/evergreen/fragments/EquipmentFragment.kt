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
import androidx.navigation.fragment.findNavController
import com.example.app.ui.equipment.EquipmentListScreen
import com.example.app.ui.equipment.findByUiId
import com.example.app.ui.equipment.toUiEquipment
import com.prod.evergreen.R
import com.prod.evergreen.XApplication
import com.prod.evergreen.activities.MainActivity
import com.prod.evergreen.activities.NotificationList
import com.prod.evergreen.activities.QrScanner
import com.prod.evergreen.api.MainRepository
import com.prod.evergreen.api.MainViewModel
import com.prod.evergreen.api.MyViewModelFactory
import com.prod.evergreen.api.RetrofitService
import com.prod.evergreen.helper.ConstantValues
import com.prod.evergreen.helper.EquipmentEditor
import com.prod.evergreen.helper.ProgressDialogUtil
import com.prod.evergreen.helper.RoleAccess
import com.prod.evergreen.helper.SharedPreferencesHelper
import com.prod.evergreen.helper.TabNav
import com.prod.evergreen.models.Data

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class EquipmentFragment : Fragment() {
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var viewModel: MainViewModel
    private var param1: String? = null
    private var param2: String? = null
    private val allEquipment = mutableStateOf<List<Data>>(emptyList())
    private val searchQuery = mutableStateOf("")
    private val filterMode = mutableStateOf(FilterMode.ALL)

    private enum class FilterMode { ALL, ACTIVE, INACTIVE }

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
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val source by allEquipment
                val query by searchQuery
                val mode by filterMode
                val activeOnly = when (mode) {
                    FilterMode.ALL -> null
                    FilterMode.ACTIVE -> true
                    FilterMode.INACTIVE -> false
                }
                EquipmentListScreen(
                    equipments = source.toUiEquipment(query, activeOnly),
                    searchQuery = query,
                    onSearchQueryChange = { searchQuery.value = it },
                    onEquipmentClick = { item ->
                        source.findByUiId(item)?.let {
                            EquipmentEditor.openDetails(requireActivity(), it)
                        }
                    },
                    onEquipmentLongClick = { item ->
                        source.findByUiId(item)?.let {
                            showEquipmentActions(it)
                        }
                    },
                    onFilterClick = { showFilterDialog() },
                    onAddClick = { TabNav.createAmc(this@EquipmentFragment) },
                    onCreateEquipmentClick = { onAddEquipment() },
                    onHomeClick = { TabNav.home(this@EquipmentFragment) },
                    onTasksClick = { TabNav.tasks(this@EquipmentFragment) },
                    onMessagesClick = { TabNav.equipment(this@EquipmentFragment) },
                    onProfileClick = { TabNav.profile(this@EquipmentFragment) },
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
        viewModel.loading.observe(viewLifecycleOwner) { data ->
            if (data && allEquipment.value.isEmpty()) {
                ProgressDialogUtil.showProgressDialog(requireActivity(), "Loading")
            } else {
                ProgressDialogUtil.hideProgressDialog()
            }
        }
        viewModel.changePasswordDataResponse.observe(viewLifecycleOwner) { data ->
            Toast.makeText(requireActivity(), data.message ?: "Updated", Toast.LENGTH_SHORT).show()
            loadEquipments()
        }
        viewModel.allequipmentsDataResponse.observe(viewLifecycleOwner) { data ->
            if (data.status == 200 || data.data != null) {
                allEquipment.value = data.data.orEmpty().toList()
            }
        }
        loadEquipments()
    }

    private var equipmentReady = false

    override fun onResume() {
        super.onResume()
        if (!equipmentReady) {
            equipmentReady = true
            return
        }
        if (::viewModel.isInitialized) {
            loadEquipments()
        }
    }

    private fun loadEquipments() {
        val token = sharedPreferencesHelper.getValueString(ConstantValues.AuthToken) ?: return
        viewModel.getAllEquipments(token)
    }

    private fun showFilterDialog() {
        val options = arrayOf("All equipment", "Active", "Inactive")
        AlertDialog.Builder(requireActivity())
            .setTitle("Filter")
            .setSingleChoiceItems(options, filterMode.value.ordinal) { dialog, which ->
                filterMode.value = FilterMode.entries[which]
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun onAddEquipment() {
        val role = sharedPreferencesHelper.getValueString(ConstantValues.TYPE_ROLE)
        if (RoleAccess.canManageEquipment(role)) {
            goTo(R.id.addEquipmentFragment, "Add Equipment")
        } else {
            Toast.makeText(requireActivity(), "You cannot add equipment", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showEquipmentActions(equipment: Data) {
        val name = equipment.name?.takeIf { it.isNotBlank() } ?: "Equipment"
        val role = sharedPreferencesHelper.getValueString(ConstantValues.TYPE_ROLE)
        val options = mutableListOf("View Equipment")
        if (RoleAccess.canManageEquipment(role)) {
            options.add("Edit Equipment")
            options.add(if (equipment.isActive()) "Mark Inactive" else "Mark Active")
        }
        options.add("Cancel")
        AlertDialog.Builder(requireActivity())
            .setTitle(name)
            .setItems(options.toTypedArray()) { dialog, which ->
                when (options[which]) {
                    "View Equipment" -> EquipmentEditor.openDetails(requireActivity(), equipment)
                    "Edit Equipment" -> EquipmentEditor.openEdit(requireActivity(), equipment)
                    "Mark Inactive", "Mark Active" -> toggleEquipmentActive(equipment)
                    else -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun toggleEquipmentActive(equipment: Data) {
        val token = sharedPreferencesHelper.getValueString(ConstantValues.AuthToken) ?: return
        val body = com.google.gson.JsonObject()
        body.addProperty("equipment_link", equipment.id)
        body.addProperty("action", if (equipment.isActive()) "delete" else "activate")
        viewModel.deleteEquipment(body, token)
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
            EquipmentFragment().apply {
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
        val viewModelFactory = MyViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]
    }
}

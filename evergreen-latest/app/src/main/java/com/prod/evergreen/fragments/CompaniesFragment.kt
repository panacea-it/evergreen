package com.prod.evergreen.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.app.ui.company.Company
import com.example.app.ui.company.CompanyListScreen
import com.google.gson.JsonObject
import com.prod.evergreen.R
import com.prod.evergreen.XApplication
import com.prod.evergreen.activities.EquipmentsList
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
import com.prod.evergreen.helper.SharedPreferencesHelper
import com.prod.evergreen.helper.Validator
import com.prod.evergreen.models.AMCData
import com.prod.evergreen.models.isCompanyActive

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class CompaniesFragment : Fragment() {
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var viewModel: MainViewModel

    private var param1: String? = null
    private var param2: String? = null
    private var pendingDeleteCompanyId: Int? = null
    private val allCompanies = mutableStateOf<List<AMCData>>(emptyList())
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
        val greeting = greetingForRole(sharedPreferencesHelper.getValueString(ConstantValues.TYPE_ROLE))

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val source by allCompanies
                val query by searchQuery
                val filter by filterMode
                CompanyListScreen(
                    companies = source.toUiCompanies(query, filter),
                    greetingName = greeting,
                    searchQuery = query,
                    onSearchQueryChange = { searchQuery.value = it },
                    onCompanyClick = { company ->
                        val id = company.id ?: return@CompanyListScreen
                        startActivity(
                            Intent(requireActivity(), EquipmentsList::class.java)
                                .putExtra("c_id", id)
                                .putExtra("name", company.name)
                        )
                    },
                    onCompanyLongClick = { company ->
                        source.firstOrNull { it.id == company.id }?.let { showCompanyActions(it) }
                    },
                    onFilterClick = { showFilterDialog() },
                    onAddClick = { goTo(R.id.createAmcFragment, "Create AMC") },
                    onHomeClick = { goTo(R.id.homeFragment, "Home") },
                    onMessagesClick = {
                        startActivity(Intent(requireActivity(), NotificationList::class.java))
                    },
                    onTasksClick = { goTo(R.id.taskFragment, "Tasks List") },
                    onProfileClick = {
                        startActivity(Intent(requireActivity(), com.prod.evergreen.activities.UserDetails::class.java))
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

        viewModel.allAmcDataResponse.observe(viewLifecycleOwner) { data ->
            if (data.status == 200) {
                allCompanies.value = data.data.orEmpty()
            }
        }

        viewModel.changePasswordDataResponse.observe(viewLifecycleOwner) { data ->
            if (!isAdded) return@observe
            val message = data.message ?: "Operation completed"
            showToast(message)
            if (data.status_code == 200) {
                pendingDeleteCompanyId = null
                refreshCompanies()
            } else {
                pendingDeleteCompanyId = null
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (!isAdded || message.isNullOrBlank()) return@observe
            showToast(message)
            pendingDeleteCompanyId = null
        }

        refreshCompanies()
    }

    private fun List<AMCData>.toUiCompanies(query: String, filter: FilterMode): List<Company> {
        val needle = query.trim()
        return filter { company ->
            val matchesFilter = when (filter) {
                FilterMode.ALL -> true
                FilterMode.ACTIVE -> company.isCompanyActive()
                FilterMode.INACTIVE -> !company.isCompanyActive()
            }
            val matchesQuery = needle.isEmpty() ||
                company.name.orEmpty().contains(needle, ignoreCase = true) ||
                company.branchName.orEmpty().contains(needle, ignoreCase = true) ||
                company.location.orEmpty().contains(needle, ignoreCase = true)
            matchesFilter && matchesQuery
        }.map { it.toUiCompany() }
    }

    private fun AMCData.toUiCompany(): Company {
        val locationText = location?.takeIf { it.isNotBlank() } ?: branchName.orEmpty()
        val poc = pocDetails?.user
        return Company(
            id = id,
            name = name.orEmpty(),
            location = locationText,
            pocName = poc?.name.orEmpty(),
            email = poc?.email.orEmpty(),
            mobile = poc?.phone.orEmpty(),
            startDate = DateConverter.convertToLocalUtcAndFormat(startDate),
            endDate = DateConverter.convertToLocalUtcAndFormat(endDate),
            imageUrl = logo,
            isActive = isCompanyActive()
        )
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

    private fun showFilterDialog() {
        val options = arrayOf("All companies", "Active", "Inactive")
        val selected = filterMode.value.ordinal
        AlertDialog.Builder(requireActivity())
            .setTitle("Filter")
            .setSingleChoiceItems(options, selected) { dialog, which ->
                filterMode.value = FilterMode.entries[which]
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
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

    private fun refreshCompanies() {
        val token = sharedPreferencesHelper.getValueString(ConstantValues.AuthToken)
        if (token.isNullOrBlank()) {
            Toast.makeText(requireActivity(), "Session expired. Please login again.", Toast.LENGTH_SHORT)
                .show()
            return
        }
        viewModel.getAllAmc(token)
    }

    private fun showCompanyActions(company: AMCData) {
        val companyName = company.name?.takeIf { it.isNotBlank() } ?: "Company"
        val statusAction = if (company.isCompanyActive()) "Mark Inactive" else "Mark Active"
        AlertDialog.Builder(requireActivity())
            .setTitle(companyName)
            .setItems(arrayOf("Edit Company", statusAction, "Cancel")) { dialog, which ->
                when (which) {
                    0 -> showEditCompanyDialog(company)
                    1 -> showCompanyStatusDialog(company)
                    else -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun showEditCompanyDialog(company: AMCData) {
        val companyId = company.id ?: run {
            Toast.makeText(requireActivity(), "Invalid company id", Toast.LENGTH_SHORT).show()
            return
        }

        val container = LinearLayout(requireActivity()).apply {
            orientation = LinearLayout.VERTICAL
            val padding = resources.displayMetrics.density.times(16).toInt()
            setPadding(padding, padding, padding, 0)
        }

        val pocUser = company.pocDetails?.user
        val siteNameInput = createDialogEditText("Site Name *", company.name.orEmpty())
        val branchNameInput = createDialogEditText("Branch Name *", company.branchName.orEmpty())
        val locationInput = createDialogEditText("Site Location", company.location.orEmpty())
        val emailInput = createDialogEditText("Company Email ID", company.email.orEmpty())
        val startDateInput =
            createDialogEditText("Start Date (YYYY-MM-DD) *", extractDateValue(company.startDate))
        val endDateInput =
            createDialogEditText("End Date (YYYY-MM-DD) *", extractDateValue(company.endDate))
        val pocCardLabel = TextView(requireActivity()).apply {
            text = "Client Admin Details (optional — fill all or leave empty)"
            textSize = 15f
            setPadding(0, (resources.displayMetrics.density * 12).toInt(), 0, (resources.displayMetrics.density * 4).toInt())
        }
        val pocNameInput = createDialogEditText("Name", pocUser?.name.orEmpty())
        val pocMobileInput = createDialogEditText("Mobile", pocUser?.phone.orEmpty())
        val pocEmailInput = createDialogEditText("Email", pocUser?.email.orEmpty())
        val pocPasswordInput = createDialogEditText(
            if (pocUser == null) "Password" else "Password (leave blank to keep)",
            ""
        ).apply { inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD }

        listOf(
            siteNameInput,
            branchNameInput,
            locationInput,
            emailInput,
            startDateInput,
            endDateInput,
            pocCardLabel,
            pocNameInput,
            pocMobileInput,
            pocEmailInput,
            pocPasswordInput
        ).forEach { container.addView(it) }

        val scrollView = ScrollView(requireActivity()).apply { addView(container) }

        val dialog = AlertDialog.Builder(requireActivity())
            .setTitle("Edit Company")
            .setView(scrollView)
            .setPositiveButton("Update", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val siteName = siteNameInput.text.toString().trim()
                val branchName = branchNameInput.text.toString().trim()
                val startDate = startDateInput.text.toString().trim()
                val endDate = endDateInput.text.toString().trim()
                val pocName = pocNameInput.text.toString().trim()
                val pocMobile = pocMobileInput.text.toString().trim()
                val pocEmail = pocEmailInput.text.toString().trim()
                val pocPassword = pocPasswordInput.text.toString().trim()
                val companyEmail = emailInput.text.toString().trim()
                val clientAdminComplete = if (pocUser == null) {
                    com.prod.evergreen.helper.FormValidator.cardCompleteOrEmpty(
                        pocName, pocMobile, pocEmail, pocPassword
                    )
                } else {
                    com.prod.evergreen.helper.FormValidator.cardCompleteOrEmpty(
                        pocName, pocMobile, pocEmail
                    )
                }
                if (com.prod.evergreen.helper.FormValidator.firstInvalid(
                        com.prod.evergreen.helper.FormValidator.Check(
                            siteNameInput, "Please enter site name", siteName.isNotBlank()
                        ),
                        com.prod.evergreen.helper.FormValidator.Check(
                            branchNameInput, "Please enter branch name", branchName.isNotBlank()
                        ),
                        com.prod.evergreen.helper.FormValidator.Check(
                            emailInput,
                            "Please enter valid email address",
                            companyEmail.isBlank() || Validator.isEmailValid(companyEmail)
                        ),
                        com.prod.evergreen.helper.FormValidator.Check(
                            startDateInput, "Use YYYY-MM-DD", isValidDateFormat(startDate)
                        ),
                        com.prod.evergreen.helper.FormValidator.Check(
                            endDateInput, "Use YYYY-MM-DD", isValidDateFormat(endDate)
                        ),
                        com.prod.evergreen.helper.FormValidator.Check(
                            pocNameInput,
                            "Please fill all Client Admin details or leave them empty",
                            clientAdminComplete
                        ),
                        com.prod.evergreen.helper.FormValidator.Check(
                            pocMobileInput,
                            "Please enter valid mobile number",
                            pocMobile.isBlank() || Validator.isMobileValid(pocMobile)
                        ),
                        com.prod.evergreen.helper.FormValidator.Check(
                            pocEmailInput,
                            "Please enter valid email address",
                            pocEmail.isBlank() || Validator.isEmailValid(pocEmail)
                        )
                    ) != null
                ) {
                    return@setOnClickListener
                }

                val token = sharedPreferencesHelper.getValueString(ConstantValues.AuthToken)
                if (token.isNullOrBlank()) {
                    Toast.makeText(requireActivity(), "Session expired. Please login again.", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }

                val body = JsonObject().apply {
                    addProperty("company_name", siteName)
                    addProperty("branch_name", branchName)
                    addProperty("start_date", startDate)
                    addProperty("end_date", endDate)
                    addProperty("company_location", locationInput.text.toString().trim())
                    addProperty("company_email", emailInput.text.toString().trim())
                    addProperty("logo", company.logo.orEmpty())
                    addProperty("name", pocNameInput.text.toString().trim())
                    addProperty("phone", pocMobile)
                    addProperty("email", pocEmail)
                    addProperty("password", pocPasswordInput.text.toString().trim())
                }

                viewModel.updateAMC(companyId, body, token)
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun showCompanyStatusDialog(company: AMCData) {
        val companyId = company.id ?: run {
            Toast.makeText(requireActivity(), "Invalid company id", Toast.LENGTH_SHORT).show()
            return
        }

        val name = company.name?.takeIf { it.isNotBlank() } ?: "this company"
        val makeActive = !company.isCompanyActive()
        val title = if (makeActive) "Activate Company" else "Mark Company Inactive"
        val message = if (makeActive) {
            "Activate $name so it can be used again?"
        } else {
            "Mark $name as inactive? It will stay visible to admin but hidden from equipment and task assignment."
        }
        AlertDialog.Builder(requireActivity())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(if (makeActive) "Activate" else "Inactive") { _, _ ->
                val token = sharedPreferencesHelper.getValueString(ConstantValues.AuthToken)
                if (token.isNullOrBlank()) {
                    Toast.makeText(requireActivity(), "Session expired. Please login again.", Toast.LENGTH_SHORT)
                        .show()
                    return@setPositiveButton
                }
                pendingDeleteCompanyId = companyId
                viewModel.deleteAMC(companyId, token, activate = makeActive)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun createDialogEditText(hint: String, value: String): EditText {
        return EditText(requireActivity()).apply {
            this.hint = hint
            setText(value)
            inputType = InputType.TYPE_CLASS_TEXT
        }
    }

    private fun extractDateValue(rawDate: String?): String {
        if (rawDate.isNullOrBlank()) return ""
        val date = rawDate.trim()
        return if (date.contains("T") && date.length >= 10) {
            date.substring(0, 10)
        } else {
            date
        }
    }

    private fun isValidDateFormat(date: String): Boolean {
        return Regex("^\\d{4}-\\d{2}-\\d{2}$").matches(date)
    }

    private fun showToast(message: String) {
        context?.let {
            Toast.makeText(it, message, Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CompaniesFragment().apply {
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

package com.prod.evergreen.activities

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.prod.evergreen.R
import com.prod.evergreen.XApplication
import com.prod.evergreen.adapters.ItemAdapter
import com.prod.evergreen.api.MainRepository
import com.prod.evergreen.api.MainViewModel
import com.prod.evergreen.api.MyViewModelFactory
import com.prod.evergreen.api.RetrofitService
import com.prod.evergreen.databinding.ActivityAddUserBinding
import com.prod.evergreen.helper.ConstantValues
import com.prod.evergreen.helper.SharedPreferencesHelper
import com.prod.evergreen.helper.Validator
import com.prod.evergreen.helper.customdialog.PopupDialog
import com.prod.evergreen.helper.customdialog.Styles
import com.prod.evergreen.helper.customdialog.listener.OnDialogButtonClickListener
import com.prod.evergreen.models.AMCData

class AddUser : AppCompatActivity() {
    private var companyLinksArray: JsonArray = JsonArray()
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var viewModel: MainViewModel
    private var amc_id: String? = null
    private var selected_accessleve: String? = null
    lateinit var binding: ActivityAddUserBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setViewmodel()
        sharedPreferencesHelper = SharedPreferencesHelper(this)

        val token = sharedPreferencesHelper.getValueString(ConstantValues.AuthToken)
        val accessType = sharedPreferencesHelper.getValueString(ConstantValues.TYPE_ROLE)
        viewModel.getAllAmc(token!!)
        val spinnerItems = listOf(
            Pair("eg_super_admin", "Super Admin"),
            Pair("eg_admin", "Manager"),
            Pair("client_admin", "Client Admin"),
            Pair("client", "Client"),
            Pair("technician", "Technician")
        )

        // Initialize filteredItems as a mutable list
        var filteredItems =
            spinnerItems.filter { it.second.isNotEmpty() } // Filter out items with empty second strings
                .map { it.second } // Map to extract the second item (value) from each pair

        var filteredItemsFirst =
            spinnerItems.filter { it.first.isNotEmpty() } // Filter out items with empty second strings
                .map { it.first } // Map to extract the second item (value) from each pair
        if (accessType.equals("client_admin", ignoreCase = true)) {
            // Filter out "Super Admin" and "Manager" based on accessType condition
            filteredItems = filteredItems.filter {
                it.equals("Client", ignoreCase = true)
            }
            filteredItemsFirst = filteredItemsFirst.filter {
                it.equals("client", ignoreCase = true)
            }
        }

        if (accessType.equals("eg_admin", ignoreCase = true)) {
            // Filter out "Super Admin" and "Manager" based on accessType condition
            filteredItems = filteredItems.filter {
                it.equals("Client Admin", ignoreCase = true) || it.equals(
                    "Client",
                    ignoreCase = true
                ) || it.equals("Technician", ignoreCase = true)
            }
            filteredItemsFirst = filteredItemsFirst.filter {
                it.equals("client_admin", ignoreCase = true) || it.equals(
                    "client",
                    ignoreCase = true
                ) || it.equals("technician", ignoreCase = true)
            }
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filteredItems)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.chooseAccessType.adapter = adapter
        binding.chooseAccessType.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedItemPair =
                        filteredItemsFirst[position] // assuming allItems is your list of pairs
                    selected_accessleve = selectedItemPair
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // Do nothing
                }
            }

        viewModel.errorMessage.observe(this) { errorMessage ->
            Toast.makeText(this, errorMessage.toString(), Toast.LENGTH_SHORT).show()
        }

        binding.back.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        viewModel.changePasswordDataResponse.observe(this) { data ->
            if (data.status_code == 200) {
                showDialog(data.message!!, data.status_code)
            } else {
                showDialog(data.message!!, data.status_code!!)
            }
        }

        binding.chooseAmc.setOnClickListener {
            showBottomSheetDialog(token) { selectedItem ->
                binding.chooseAmc.text = selectedItem.name
                amc_id = selectedItem.id.toString()
            }
        }

        binding.verifyBtn.setOnClickListener {
            val name = binding.name.text.toString()
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()
            val mobile = binding.mobile.text.toString()
            if (name.isEmpty()) {
                Toast.makeText(this@AddUser, "Enter name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (mobile.isEmpty()) {
                Toast.makeText(this@AddUser, "Enter mobile number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (!Validator.isMobileValid(mobile)) {
                Toast.makeText(this@AddUser, "Enter valid  mobile number", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            } else if (email.isEmpty()) {
                Toast.makeText(this@AddUser, "Enter email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (!Validator.isEmailValid(email)) {
                Toast.makeText(this@AddUser, "Enter valid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (password.isEmpty()) {
                Toast.makeText(this@AddUser, "Enter password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (amc_id != null) {
                val companyLinks = listOf(amc_id!!.toInt())
                companyLinksArray = JsonArray().apply {
                    companyLinks.forEach { add(it) }
                }
            }
            val jsondata = JsonObject().apply {
                addProperty("email", email)
                addProperty("location", "")
                addProperty("notes", "")
                addProperty("pan_id", "")
                addProperty("aadhaar_id", "")
                addProperty("permanent_address", "")
                addProperty("password", password)
                addProperty("name", name)
                addProperty("phone", mobile)
                add("company_link", companyLinksArray)
                addProperty("access_level", selected_accessleve)
            }
            // val jsonObject = createJsonObject(email, password, name, mobile, amc, companyLinks)
            Log.d("output", jsondata.toString())
            viewModel.createTechnician(jsondata, token)
        }
    }

    private fun setViewmodel() {
        val repository = MainRepository(
            RetrofitService.getInstance(this),
            XApplication.database.newsDao(),
            XApplication.database.companyDao()
        )
        val viewModelFactory = MyViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]
    }

    private fun showBottomSheetDialog(token: String?, onItemSelected: (AMCData) -> Unit) {
        val dialog = BottomSheetDialog(this, R.style.NoBackgroundDialogTheme)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_amc_layout, null)
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        val searchView: SearchView = view.findViewById(R.id.searchView)
        viewModel.allAmcDataResponse.observe(this) { data ->

            val adapter = ItemAdapter(data.data!!) { selectedItem ->
                onItemSelected(selectedItem)
                dialog.dismiss()
            }
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = adapter

            // Setup search view listener
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

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

    fun showDialog(message: String, code: Int) {
        PopupDialog.getInstance(this)!!
            .setStyle(Styles.IOS)!!
            .setHeading("Message")!!
            .setDescription(message)!!
            .setCancelable(false)!!
            .setPositiveButtonText(getString(R.string.positive))!!
            .showDialog(object : OnDialogButtonClickListener() {
                override fun onPositiveClicked(dialog: Dialog?) {
                    super.onPositiveClicked(dialog)
                    if (code == 200)
                        onBackPressedDispatcher.onBackPressed()
                }
            }, true)
    }
}
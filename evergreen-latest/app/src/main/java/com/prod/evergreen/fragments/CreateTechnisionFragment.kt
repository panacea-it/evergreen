package com.prod.evergreen.fragments


import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.prod.evergreen.XApplication
import com.prod.evergreen.R
import com.prod.evergreen.adapters.ItemAdapter
import com.prod.evergreen.api.MainRepository
import com.prod.evergreen.api.MainViewModel
import com.prod.evergreen.api.MyViewModelFactory
import com.prod.evergreen.api.RetrofitService
import com.prod.evergreen.databinding.FragmentCreateTechnisionBinding
import com.prod.evergreen.helper.ConstantValues
import com.prod.evergreen.helper.SharedPreferencesHelper
import com.prod.evergreen.helper.Validator
import com.prod.evergreen.helper.customdialog.PopupDialog
import com.prod.evergreen.helper.customdialog.Styles
import com.prod.evergreen.helper.customdialog.listener.OnDialogButtonClickListener
import com.prod.evergreen.models.AMCData


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class CreateTechnisionFragment : Fragment() {



    lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var viewModel: MainViewModel
    lateinit var binding:FragmentCreateTechnisionBinding
    private var param1: String? = null
    private var param2: String? = null
    private var amc_id: String? = null
    private var isSubmitting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding= FragmentCreateTechnisionBinding.inflate(layoutInflater, container, false)
        setViewmodel()
        sharedPreferencesHelper=SharedPreferencesHelper(requireActivity())

       return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val token=sharedPreferencesHelper.getValueString(ConstantValues.AuthToken)
       viewModel.getAllAmc(token!!)
//        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
//            Toast.makeText(requireActivity(), errorMessage.toString(), Toast.LENGTH_SHORT).show()
//        }

        viewModel.changePasswordDataResponse.observe(viewLifecycleOwner) { data ->
            if(data.status_code==200){
                showCreateResultDialog(data.message ?: "Created successfully", true)
            } else {
                showCreateResultDialog(data.message ?: "Something went wrong", false)
            }
        }

        binding.chooseAmc.setOnClickListener {
            showBottomSheetDialog(token) { selectedItem ->
                binding.chooseAmc.text = selectedItem.name
                amc_id=selectedItem.id.toString()
            }
        }

        binding.verifyBtn.setOnClickListener {
            if (isSubmitting) return@setOnClickListener
            val name=binding.name.text.toString()
            val email=binding.email.text.toString()
            val password=binding.password.text.toString()
            val mobile=binding.mobile.text.toString()
            val amc=binding.chooseAmc.text.toString()

            if (name.isEmpty()) {
                Toast.makeText(requireActivity(), "Enter name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (mobile.isEmpty()) {
                Toast.makeText(requireActivity(), "Enter mobile number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!Validator.isMobileValid(mobile)) {
                Toast.makeText(requireActivity(), "Enter valid mobile number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                Toast.makeText(requireActivity(), "Enter email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!Validator.isEmailValid(email)) {
                Toast.makeText(requireActivity(), "Enter valid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                Toast.makeText(requireActivity(), "Enter password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (amc_id == null) {
                Toast.makeText(requireActivity(), "Select company", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val companyLinks = listOf(amc_id!!.toInt())
            val jsonObject = createJsonObject(email, password, name, mobile, amc, companyLinks)


            Log.d("output",jsonObject.toString())
            isSubmitting = true
            binding.verifyBtn.isEnabled = false
            viewModel.createTechnician(jsonObject,token)


        }

    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CreateTechnisionFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    private fun setViewmodel() {
        val repository = MainRepository(RetrofitService.getInstance(requireActivity()),XApplication.database.newsDao(),XApplication.database.companyDao())
        val viewModelFactory = MyViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)
    }

    private fun showBottomSheetDialog(token: String?, onItemSelected: (AMCData) -> Unit) {
        val dialog = BottomSheetDialog(requireActivity(),R.style.NoBackgroundDialogTheme)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_amc_layout, null)
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        val searchView: SearchView = view.findViewById(R.id.searchView)
        viewModel.allAmcDataResponse.observe(viewLifecycleOwner) { data ->

            val adapter = ItemAdapter(data.data!!) { selectedItem ->
                onItemSelected(selectedItem)
                dialog.dismiss()
            }

            recyclerView.layoutManager = LinearLayoutManager(requireActivity())
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

    private fun showCreateResultDialog(message: String, success: Boolean) {
        PopupDialog.getInstance(requireActivity())!!
            .setStyle(Styles.IOS)!!
            .setHeading("Message")!!
            .setDescription(message)!!
            .setCancelable(false)!!
            .setPositiveButtonText(getString(R.string.positive))!!
            .showDialog(object : OnDialogButtonClickListener() {
                override fun onPositiveClicked(dialog: Dialog?) {
                    super.onPositiveClicked(dialog)
                    if (success) {
                        if (!findNavController().popBackStack()) {
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        }
                    } else {
                        isSubmitting = false
                        binding.verifyBtn.isEnabled = true
                    }
                }
            }, true)
    }
}


fun createJsonObject(email: String, password: String, name: String, mobile: String, amc: String, companyLinks: List<Int>): JsonObject {
    // Create a JsonArray from the list
    val companyLinkArray = JsonArray().apply {
        companyLinks.forEach { add(it) }
    }

    // Create and return the JsonObject
    return JsonObject().apply {
        addProperty("email", email)
        addProperty("location", "")
        addProperty("notes", "")
        addProperty("pan_id", "")
        addProperty("aadhaar_id", "")
        addProperty("permanent_address", "")
        addProperty("password", password)
        addProperty("name", name)
        addProperty("phone", mobile)
        addProperty("amc", amc)
        add("company_link", companyLinkArray) // Correct way to add JsonArray
        addProperty("access_level", "technician")
    }
}



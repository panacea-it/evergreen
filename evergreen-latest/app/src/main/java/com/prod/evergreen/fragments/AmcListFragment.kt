package com.prod.evergreen.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.google.gson.JsonObject
import com.prod.evergreen.XApplication
import com.prod.evergreen.adapters.UsersListAdapter
import com.prod.evergreen.api.MainRepository
import com.prod.evergreen.api.MainViewModel
import com.prod.evergreen.api.MyViewModelFactory
import com.prod.evergreen.api.RetrofitService
import com.prod.evergreen.api.SharedViewModel
import com.prod.evergreen.databinding.FragmentAmcListBinding
import com.prod.evergreen.helper.ConstantValues
import com.prod.evergreen.helper.ProgressDialogUtil
import com.prod.evergreen.helper.SharedPreferencesHelper
import com.prod.evergreen.models.attachedCompanyLabel


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AmcListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AmcListFragment : Fragment() {
    private val sharedViewModel: SharedViewModel by activityViewModels()
    lateinit var amcListAdapter: UsersListAdapter
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var viewModel: MainViewModel

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var binding: FragmentAmcListBinding
    private var token: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAmcListBinding.inflate(layoutInflater, container, false)
        sharedPreferencesHelper = SharedPreferencesHelper(requireActivity())
        token = sharedPreferencesHelper.getValueString(ConstantValues.AuthToken)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val object1 = JsonObject()
        object1.addProperty("access_level", param2)
        viewModel.getAllUsers(token!!, object1)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViewmodel()
        amcListAdapter = UsersListAdapter { user ->
            startActivity(
                android.content.Intent(requireActivity(), com.prod.evergreen.activities.UserDetails::class.java)
                    .putExtra("user_data", com.google.gson.Gson().toJson(user))
            )
        }
        binding.recyclerView.adapter = amcListAdapter
        viewModel.allUsersDataResponse.observe(viewLifecycleOwner) { data ->
           // if (data.status == 200) {
            sharedViewModel.setSharedDataCounter(data.count!!)
            amcListAdapter.addData(data.data!!)
              Log.d("COUNT :: ",data.count.toString())

                if (data.data.isEmpty()) {
                    binding.noDataLayout.visibility = View.VISIBLE
                } else {
                    binding.noDataLayout.visibility = View.GONE
                }

           // }
        }
        viewModel.loading.observe(viewLifecycleOwner) { data ->
            if (data){
                ProgressDialogUtil.showProgressDialog(requireActivity(),"Loading")
            }
            else{
                ProgressDialogUtil.hideProgressDialog()
            }
        }


    }


    private fun showUserDetails(user: com.prod.evergreen.models.Users) {
        val role = when (user.access_level) {
            "client_admin" -> "Client Admin"
            else -> user.access_level.orEmpty()
                .replace('_', ' ')
                .replaceFirstChar { it.uppercase() }
        }.ifBlank { "-" }
        val details = """
            Name : ${user.name ?: "-"}
            Role : $role
            Mobile : ${user.phone ?: "-"}
            Email : ${user.email ?: "-"}
            Location : ${user.location ?: "-"}
            Company : ${user.attachedCompanyLabel()}
        """.trimIndent()
        androidx.appcompat.app.AlertDialog.Builder(requireActivity())
            .setTitle("User details")
            .setMessage(details)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun setViewmodel() {
        val repository = MainRepository(
            RetrofitService.getInstance(requireActivity()),
            XApplication.database.newsDao(),
            XApplication.database.companyDao()
        )
        val viewModelFactory = MyViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)
    }

    companion object {
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AmcListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

    }
}
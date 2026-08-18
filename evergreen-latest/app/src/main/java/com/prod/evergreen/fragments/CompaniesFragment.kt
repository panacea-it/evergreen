package com.prod.evergreen.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.prod.evergreen.XApplication
import com.prod.evergreen.adapters.CompanieslistAdapter
import com.prod.evergreen.activities.EquipmentsList
import com.prod.evergreen.api.MainRepository
import com.prod.evergreen.api.MainViewModel
import com.prod.evergreen.api.MyViewModelFactory
import com.prod.evergreen.api.RetrofitService
import com.prod.evergreen.databinding.FragmentCompaniesBinding
import com.prod.evergreen.helper.ConstantValues
import com.prod.evergreen.helper.ProgressDialogUtil
import com.prod.evergreen.helper.SharedPreferencesHelper

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class CompaniesFragment : Fragment() {


    lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var viewModel: MainViewModel

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var companieslistAdapter: CompanieslistAdapter

    lateinit var binding: FragmentCompaniesBinding
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
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentCompaniesBinding.inflate(layoutInflater, container, false)
        sharedPreferencesHelper = SharedPreferencesHelper(requireActivity())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViewmodel()
        companieslistAdapter = CompanieslistAdapter(sharedPreferencesHelper) { data,name ->
            startActivity(
                Intent(requireActivity(), EquipmentsList::class.java).putExtra(
                    "c_id",
                    data
                ).putExtra(
                    "name",
                    name
                )
            )
        }
        binding.recyclerCompanies.setHasFixedSize(true)
        binding.recyclerCompanies.adapter = companieslistAdapter
        binding.etSearc.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                companieslistAdapter.filter.filter(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        viewModel.loading.observe(viewLifecycleOwner) { data ->
            if (data){
                ProgressDialogUtil.showProgressDialog(requireActivity(),"Loading")
            }
            else{
                ProgressDialogUtil.hideProgressDialog()
            }
        }

        viewModel.allAmcDataResponse.observe(viewLifecycleOwner) { data ->
            if (data.status == 200) {
                if (data.data!!.isEmpty()) {
                    binding.noDataLayout!!.visibility=View.VISIBLE

                } else {
                    companieslistAdapter.addData(data.data)
                    binding.noDataLayout!!.visibility=View.GONE
                }
            }

        }

        val token = sharedPreferencesHelper.getValueString(ConstantValues.AuthToken)
        viewModel.getAllAmc(token!!)

//
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
        val repository = MainRepository(RetrofitService.getInstance(requireActivity()),XApplication.database.newsDao(),XApplication.database.companyDao())
        val viewModelFactory = MyViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)
    }
}
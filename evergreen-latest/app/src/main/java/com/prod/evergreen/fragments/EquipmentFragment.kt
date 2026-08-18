package com.prod.evergreen.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.prod.evergreen.XApplication
import com.prod.evergreen.activities.EquipmentDetails
import com.prod.evergreen.adapters.EquipmentListAdapter

import com.prod.evergreen.api.MainRepository
import com.prod.evergreen.api.MainViewModel
import com.prod.evergreen.api.MyViewModelFactory
import com.prod.evergreen.api.RetrofitService
import com.prod.evergreen.databinding.FragmentEquipmentBinding
import com.prod.evergreen.helper.ConstantValues
import com.prod.evergreen.helper.ProgressDialogUtil
import com.prod.evergreen.helper.SharedPreferencesHelper


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
class EquipmentFragment : Fragment() {
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var viewModel: MainViewModel
    lateinit var equipmentlistAdapter: EquipmentListAdapter
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
lateinit var binding:FragmentEquipmentBinding
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
        binding= FragmentEquipmentBinding.inflate(layoutInflater, container, false)
        sharedPreferencesHelper= SharedPreferencesHelper(requireActivity())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViewmodel()

        equipmentlistAdapter= EquipmentListAdapter(sharedPreferencesHelper) { data->
//            val gson = Gson()
//            val json = gson.toJson(data)
            startActivity(Intent(requireActivity(), EquipmentDetails::class.java).putExtra("eq_id",data.id))
        }

        binding.recyclerCompanies.adapter=equipmentlistAdapter
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                equipmentlistAdapter.filter.filter(s.toString())
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
        viewModel.allequipmentsDataResponse.observe(viewLifecycleOwner) { data ->
            if (data.status==200){
                equipmentlistAdapter.addData(data.data)

                if (data.data!!.isEmpty()){
                    binding.etSearch.visibility=View.GONE
                    binding.noDataLayout!!.visibility=View.VISIBLE
                }
                else{
                    binding.etSearch.visibility=View.VISIBLE
                    binding.noDataLayout!!.visibility=View.GONE
                }
            }

        }
        val token=sharedPreferencesHelper.getValueString(ConstantValues.AuthToken)
        viewModel.getAllEquipments(token!!)

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
        val repository = MainRepository(RetrofitService.getInstance(requireActivity()),XApplication.database.newsDao(),XApplication.database.companyDao())
        val viewModelFactory = MyViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)
    }
}
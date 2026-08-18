package com.prod.evergreen.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.prod.evergreen.R
import com.prod.evergreen.api.Constants
import com.prod.evergreen.databinding.FragmentMoreEqInfoBinding
import com.prod.evergreen.helper.ConstantValues
import com.prod.evergreen.helper.DateConverter
import com.prod.evergreen.helper.SharedPreferencesHelper
import com.prod.evergreen.models.CompanyData
import com.prod.evergreen.models.TasksItem

private const val ARG_TASK_ITEM = "arg_task_item"
private const val ARG_COMPANY_DATA = "arg_company_data"

class MoreEqInfoFragment : DialogFragment() {
    private var tasksItem: String? = null
    private var companyData: String? = null
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    private lateinit var binding: FragmentMoreEqInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)

        arguments?.let {
            tasksItem = it.getString(ARG_TASK_ITEM)!!
            companyData = it.getString(ARG_COMPANY_DATA)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMoreEqInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferencesHelper= SharedPreferencesHelper(requireActivity())
        binding.close.setOnClickListener {
            dismiss()
        }
        val gson = Gson()

        // Convert JSON to Kotlin object (Task)
        val task = gson.fromJson(tasksItem, TasksItem::class.java)
        if (!companyData.isNullOrEmpty())
        {
            val company = gson.fromJson(companyData, CompanyData::class.java)
            binding.tvCompanyName.text = company.name
            binding.tvCompanyLocation.text =company.location

        }

        // Now you can use tasksItem and companyData to populate your UI
       if(task!=null){
           binding.tvTaskName.text = task.task!!.name
           binding.tvTaskDescription.text = task.task.description
           if (task.technicianLink!=null) {
               binding.tvTaskTechnician.text = task.technician!!.name
               binding.tvTaskTechnicianPhone.text = task.technician.phone
           }
           binding.tvTaskCreated.text = DateConverter.convertToLocalUtcAndFormat(task.task.createdAt!!)
           binding.tvTaskLastUpdate.text = DateConverter.convertToLocalUtcAndFormat(task.task.updatedAt!!)

           binding.tvStatus.text = when (task.status) {
               "open" -> "Not Started"
               "in_progress" -> "In Progress"
               "closed" -> "Closed"
               else -> "" // Maintain the current text if no condition is met
           }
           binding.tvPocName.text = task.client!!.name
           binding.tvPocPhone.text = task.client.phone
           if(task.task.image?.get(0) !=null){
               binding.imageIssue.visibility=View.VISIBLE
               Glide.with(requireActivity()).load(Constants.BASE_URL+task.task.image[0]).into(binding.imageIssue)
           }
           else{
               binding.imageIssue.visibility=View.GONE
           }
       }



    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param tasksItem Parameter 1 (TasksItem).
         * @param companyData Parameter 2 (CompanyDataResponse).
         * @return A new instance of fragment MoreEqInfoFragment.
         */
        @JvmStatic
        fun newInstance(tasksItem: String, companyData: String? = null) =
            MoreEqInfoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TASK_ITEM, tasksItem)
                    putString(ARG_COMPANY_DATA, companyData)
                }
            }
    }
}

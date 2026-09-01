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
import com.prod.evergreen.models.TaskCreated
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
            tasksItem = it.getString(ARG_TASK_ITEM)
            companyData = it.getString(ARG_COMPANY_DATA)
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
        try {
        val gson = Gson()

        val created = try {
            gson.fromJson(tasksItem, TaskCreated::class.java)
        } catch (_: Exception) {
            null
        }
        val legacy = if (created?.task == null) {
            try {
                gson.fromJson(tasksItem, TasksItem::class.java)
            } catch (_: Exception) {
                null
            }
        } else {
            null
        }
        if (!companyData.isNullOrEmpty() && companyData != "null") {
            try {
                val company = gson.fromJson(companyData, CompanyData::class.java)
                binding.tvCompanyName.text = company?.name ?: "-"
                binding.tvCompanyLocation.text = company?.location ?: "-"
            } catch (_: Exception) {
                binding.tvCompanyName.text = "-"
                binding.tvCompanyLocation.text = "-"
            }
        }

        val taskName = created?.task?.name ?: legacy?.task?.name
        val taskDescription = created?.task?.description ?: legacy?.task?.description
        val createdAt = created?.task?.createdAt ?: legacy?.task?.createdAt
        val updatedAt = created?.task?.updatedAt ?: legacy?.task?.updatedAt
        val status = created?.status ?: legacy?.status
        val technicianName = created?.technician?.name ?: legacy?.technician?.name
        val technicianPhone = created?.technician?.phone ?: legacy?.technician?.phone
        val clientName = created?.client?.name ?: legacy?.client?.name
        val clientPhone = created?.client?.phone ?: legacy?.client?.phone
        val imageUrl = created?.task?.image?.firstOrNull() ?: legacy?.task?.image?.firstOrNull()

        binding.tvTaskName.text = taskName ?: "-"
        binding.tvTaskDescription.text = taskDescription ?: "-"
        binding.tvTaskTechnician.text = technicianName ?: "-"
        binding.tvTaskTechnicianPhone.text = technicianPhone ?: "-"
        binding.tvTaskCreated.text = DateConverter.convertToLocalUtcAndFormat(createdAt)
        binding.tvTaskLastUpdate.text = DateConverter.convertToLocalUtcAndFormat(updatedAt)
        binding.tvStatus.text = when (status) {
            "open" -> "Not Started"
            "in_progress" -> "In Progress"
            "closed" -> "Closed"
            "hold" -> "Hold"
            else -> status.orEmpty()
        }
        binding.tvPocName.text = clientName ?: "-"
        binding.tvPocPhone.text = clientPhone ?: "-"
        if (!imageUrl.isNullOrBlank()) {
            binding.imageIssue.visibility = View.VISIBLE
            Glide.with(requireActivity()).load(Constants.BASE_URL + imageUrl).into(binding.imageIssue)
        } else {
            binding.imageIssue.visibility = View.GONE
        }
        } catch (_: Exception) {
            binding.tvTaskName.text = "-"
            binding.imageIssue.visibility = View.GONE
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

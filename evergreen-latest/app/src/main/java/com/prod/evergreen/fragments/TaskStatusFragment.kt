package com.prod.evergreen.fragments

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.prod.evergreen.XApplication
import com.prod.evergreen.R
import com.prod.evergreen.activities.FeedbackFormActivity
import com.prod.evergreen.adapters.AssigningTasksStatusAdapter
import com.prod.evergreen.api.Constants
import com.prod.evergreen.api.MainRepository
import com.prod.evergreen.api.MainViewModel
import com.prod.evergreen.api.MyViewModelFactory
import com.prod.evergreen.api.RetrofitService
import com.prod.evergreen.api.SharedViewModel
import com.prod.evergreen.databinding.FragmentTaskStatusBinding
import com.prod.evergreen.dialogs.MoreEqInfoFragment
import com.prod.evergreen.helper.ConstantValues
import com.prod.evergreen.helper.ProgressDialogUtil
import com.prod.evergreen.helper.SharedPreferencesHelper
import com.prod.evergreen.helper.customdialog.PopupDialog
import com.prod.evergreen.helper.customdialog.Styles
import com.prod.evergreen.helper.customdialog.listener.OnDialogButtonClickListener


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class TaskStatusFragment : Fragment(), CustomDialogFragment.CustomDialogListener, SetHoldFagment.ReasonDialogListener {

    private lateinit var feedbackFormLauncher: ActivityResultLauncher<Intent>
    private val sharedViewModel: SharedViewModel by activityViewModels()
    lateinit var adapter:AssigningTasksStatusAdapter
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var viewModel: MainViewModel
lateinit var bindin:FragmentTaskStatusBinding
    private var param1: String? = null
    private var param2: String? = null
    private var token: String? = null
    private var userid: Int? = null

    private var accessType: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bindin= FragmentTaskStatusBinding.inflate(layoutInflater, container, false)
        sharedPreferencesHelper = SharedPreferencesHelper(requireActivity())
        token = sharedPreferencesHelper.getValueString(ConstantValues.AuthToken)
        userid = sharedPreferencesHelper.getValueInt(ConstantValues.USER_ID)
        accessType = sharedPreferencesHelper.getValueString(ConstantValues.TYPE_ROLE)
        return bindin.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViewmodel()

        bindin.etSearc.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter.filter(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        adapter=AssigningTasksStatusAdapter(sharedPreferencesHelper,accessType =accessType!!,taskData =  { taskData ->
            if (taskData.technicianLink == null) {
                val object1 = JsonObject()
                object1.addProperty("task_link", taskData.taskLink)
                object1.addProperty("technician_link", userid)
                viewModel.assignTechnician(object1, token!!)
            } else if (taskData.status == "open") {

                val object1 = JsonObject()
                object1.addProperty("task_user_link", taskData.id)
                object1.addProperty("status", "in_progress")
                viewModel.upDateTaskStatus(object1, token!!)
            } else if (taskData.status == "in_progress") {


 if (taskData.task.rating==null) {

     if (accessType.equals("technician"))
     {
         if(taskData.task.followUp == null) {

             val gson = Gson()
             val json = gson.toJson(taskData)
             val feedbackFormIntent = Intent(requireActivity(), FeedbackFormActivity::class.java)
             // Pass any necessary data using extras
             feedbackFormIntent.putExtra("jsonData", json)
             feedbackFormLauncher.launch(feedbackFormIntent)

         }

     }else{
         val gson = Gson()
         val json = gson.toJson(taskData)
         val feedbackFormIntent = Intent(requireActivity(), FeedbackFormActivity::class.java)
         // Pass any necessary data using extras
         feedbackFormIntent.putExtra("jsonData", json)
         feedbackFormLauncher.launch(feedbackFormIntent)
//         FeedBackFormFragmentDialog.newInstance(json, this)
//             .show(childFragmentManager, "")
     }


     }
 else{
     if (accessType.equals("technician")){
         showCustomDialog(taskData.task.otp, taskData.id)
     }
                }

            }
            else{
                if (taskData.status=="hold"){


                    val object1 = JsonObject().apply {
                        addProperty("task_user_link", taskData.id)
                        addProperty("status", "in_progress")
                    }

                    // Create the nested hold JSON object
                    if (taskData.task.holdReason.isNotEmpty()){
                        val holdObject = JsonObject().apply {
                            addProperty("release_from_hold", true)
                            addProperty("hold_reason_link", taskData.task.holdReason[0].id)

                        }
                        object1.add("hold", holdObject)
                        Log.d("sasaasasasasa",object1.toString())
                        viewModel.upDateTaskStatus(object1, token!!)
                    }





                }
                else {
                    val object1 = JsonObject()
                    object1.addProperty("task_user_link", taskData.id)
                    object1.addProperty("status", "in_progress")
                    viewModel.upDateTaskStatus(object1, token!!)
                }
            }




        }, taskDataMore = { responseData ->
            val gson = Gson()
            val responseTask = gson.toJson(responseData)
            val responseCompany = gson.toJson(responseData.task.equipment?.company)
            MoreEqInfoFragment.newInstance(responseTask,responseCompany).show(childFragmentManager,"")

        },
            settohold = {responseData ->
                val reasonDialog = SetHoldFagment.newInstance(responseData.id,"")
                reasonDialog.setListener(this)
                reasonDialog.show(childFragmentManager,"")
            },downloadfile={downloadfile ->
                val object1 = JsonObject()
                object1.addProperty("task_link", downloadfile.id)
                viewModel.getServiceReport(object1, token!!)
            }, editReson = { responseData ->

                if (responseData.status.contains("hold")) {
                    val reasonDialog = SetHoldFagment.newInstance(
                        responseData.id,
                        Gson().toJson(responseData.task.holdReason)
                    )
                    reasonDialog.setListener(this)
                    reasonDialog.show(childFragmentManager, "")
                }
                else{
                    val gson = Gson()
                    val json = gson.toJson(responseData)
                    val feedbackFormIntent = Intent(requireActivity(), FeedbackFormActivity::class.java)
                    // Pass any necessary data using extras
                    feedbackFormIntent.putExtra("jsonData", json)
                    feedbackFormLauncher.launch(feedbackFormIntent)
                }
            }
            )


        bindin.recyclerView.setHasFixedSize(true)
        bindin.recyclerView.adapter=adapter
        val object1 = JsonObject()
        object1.addProperty("status", param2)

        viewModel.assignTechnicianDataResponse.observe(viewLifecycleOwner) { response ->
            if (response.status_code==200) {
                showDialog(response.message!!)
                viewModel.getAllTasks(token!!, object1)
            }
            else{
                showDialog(response.message!!)
            }

        }


        viewModel.downloadpdf.observe(viewLifecycleOwner) { response ->
            if (response.status_code==200) {
                openPdfInBrowser(Constants.BASE_URL+response.url!!)

            }
            else{
                showDialog(response.message!!)
            }

        }



        viewModel.upDateTaskStatusDataResponse.observe(viewLifecycleOwner) { response ->
            showDialog(response.message!!)
            viewModel.getAllTasks(token!!,object1)

        }

        viewModel.loading.observe(viewLifecycleOwner) { data ->
            if (data){
                ProgressDialogUtil.showProgressDialog(requireActivity(),"Loading")
            }
            else{
                ProgressDialogUtil.hideProgressDialog()
            }
        }
        viewModel.allTasksDataResponse.observe(viewLifecycleOwner) { data ->
            if (data.success == 200) {
                sharedViewModel.setSharedData(data)
                if (data.data.isEmpty()){
                    bindin.noDataLayout.visibility=View.VISIBLE
                    bindin.recyclerView.visibility=View.GONE
                    Glide.with(requireActivity()).load(R.drawable.list).into(bindin.animationView)
                }
                else {
                    bindin.recyclerView.visibility=View.VISIBLE
                    bindin.noDataLayout.visibility=View.GONE
                    adapter.addData(data.data)
                }
            }
        }

        viewModel.getAllTasks(token!!,object1)

        feedbackFormLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val object12 = JsonObject()
                object12.addProperty("status", param2)
                viewModel.getAllTasks(token!!, object12)
            }
        }
    }
    private fun setViewmodel() {
        val repository = MainRepository(RetrofitService.getInstance(requireActivity()),XApplication.database.newsDao(),XApplication.database.companyDao())
        val viewModelFactory = MyViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)
    }
    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TaskStatusFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


    fun showDialog(message: String) {
        PopupDialog.getInstance(requireActivity())!!
            .setStyle(Styles.IOS)!!
            .setHeading("Message")!!
            .setDescription(message)!!
            .setCancelable(false)!!
            .setPositiveButtonText(getString(R.string.positive))!!
            .showDialog(object : OnDialogButtonClickListener() {
                override fun onPositiveClicked(dialog: Dialog?) {
                    super.onPositiveClicked(dialog)

                }
            }, true)
    }

//    override fun onFeedbackSubmittedSuccessfully(success: Boolean, id: Int) {
//
//        if (success) {
//
//            if (accessType!="technician"){
//                val object1 = JsonObject()
//                object1.addProperty("task_user_link", id)
//                object1.addProperty("status", "closed")
//                viewModel.upDateTaskStatus(object1, token!!)
//            }
//            else {
//                Log.d("accessType1",accessType.toString())
//                val object1 = JsonObject()
//                object1.addProperty("status", param2)
//                viewModel.getAllTasks(token!!, object1)
//            }
//
//            // Handle successful feedback submission
//        }
//
//    }
    private fun showCustomDialog(otp: String, taskLink: Int) {
        val dialog = CustomDialogFragment(otp,taskLink)
        dialog.setCustomDialogListener(this)
        dialog.show(childFragmentManager, "CustomDialogFragment")
    }
    override fun onDialogPositiveClick(id: Int) {
        val object1 = JsonObject()
        object1.addProperty("task_user_link", id)
        object1.addProperty("status", "closed")
        viewModel.upDateTaskStatus(object1, token!!)
    }

    override fun onDialogNegativeClick() {

    }

    override fun onreason(
        reason: String,
        id: Int,
        hold_reason: String,
        spare_part_number: String,
        image: String
    ) {
        val object1 = JsonObject().apply {
            addProperty("task_user_link", id)
            addProperty("status", "hold")
        }

        // Create the nested hold JSON object
        val holdObject = JsonObject().apply {
            addProperty("hold_reason", hold_reason)
            addProperty("spare_part_number", spare_part_number)
            addProperty("image", image)
            addProperty("reason", reason)
        }

        // Add the nested object to the main JSON object
        object1.add("hold", holdObject)
//        val object1 = JsonObject()
//        object1.addProperty("task_user_link", id)
//        object1.addProperty("status", "hold")
//        object1.addProperty("reason", reason)
        viewModel.upDateTaskStatus(object1, token!!)
    }

    override fun onreasonUpdate(
        feedback: String,
        id: Int,
        hold_reason: String,
        spare_part_number: String,
        image: String
    ) {

        val object1 = JsonObject().apply {
            addProperty("hold_reason_link", id)
            addProperty("hold_reason", hold_reason)
            addProperty("spare_part_number", spare_part_number)
            addProperty("image", image)
            addProperty("reason", feedback)
        }
        viewModel.updateHoldReasons(object1, token!!)
    }

    object FeedbackFormResultKeys {
        const val FEEDBACK_SUBMISSION_RESULT = "feedback_submission_result"
        const val TASK_ID = "task_id"
    }

    private fun openPdfInBrowser(pdfUrl: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(pdfUrl)
        }

        startActivity(intent)
    }
}
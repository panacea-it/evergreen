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
    private var pendingTaskMutation = false

    private fun resolveTaskId(task: com.prod.evergreen.models.TaskCreated): Int {
        return listOfNotNull(task.task?.id, task.taskLink).firstOrNull { it != 0 } ?: task.id
    }
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
        val authToken = token
        if (authToken.isNullOrBlank()) {
            Toast.makeText(requireActivity(), "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
            return
        }
        val currentAccessType = accessType ?: "others"

        adapter=AssigningTasksStatusAdapter(sharedPreferencesHelper,accessType =currentAccessType,taskData =  { taskData ->
            if (taskData.technicianLink == null) {
                val object1 = JsonObject()
                object1.addProperty("task_link", taskData.taskLink)
                object1.addProperty("technician_link", userid)
                viewModel.assignTechnician(object1, authToken)
            } else if (taskData.status == "open") {

                val object1 = JsonObject()
                object1.addProperty("task_user_link", taskData.id)
                object1.addProperty("status", "in_progress")
                viewModel.upDateTaskStatus(object1, authToken)
            } else if (taskData.status == "in_progress") {


 if (taskData.task?.rating==null) {

     if (currentAccessType.equals("technician"))
     {
         if(taskData.task?.followUp == null) {

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
     if (currentAccessType.equals("technician")){
         showCustomDialog(taskData.task?.otp.orEmpty(), taskData.id)
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
                    val holdReason = taskData.task?.holdReason.orEmpty()
                    if (holdReason.isNotEmpty()){
                        val holdObject = JsonObject().apply {
                            addProperty("release_from_hold", true)
                            addProperty("hold_reason_link", holdReason[0].id)

                        }
                        object1.add("hold", holdObject)
                        Log.d("sasaasasasasa",object1.toString())
                        viewModel.upDateTaskStatus(object1, authToken)
                    }





                }
                else {
                    val object1 = JsonObject()
                    object1.addProperty("task_user_link", taskData.id)
                    object1.addProperty("status", "in_progress")
                    viewModel.upDateTaskStatus(object1, authToken)
                }
            }




        }, taskDataMore = { responseData ->
            try {
                val gson = Gson()
                val responseTask = gson.toJson(responseData)
                val responseCompany = gson.toJson(responseData.task?.equipment?.company)
                MoreEqInfoFragment.newInstance(responseTask,responseCompany).show(childFragmentManager,"")
            } catch (error: Exception) {
                Toast.makeText(requireActivity(), "Unable to open task details", Toast.LENGTH_SHORT).show()
            }

        },
            settohold = {responseData ->
                val reasonDialog = SetHoldFagment.newInstance(responseData.id,"")
                reasonDialog.setListener(this)
                reasonDialog.show(childFragmentManager,"")
            },downloadfile={ item ->
                val taskId = item.taskLink ?: item.task?.id
                if (taskId == null || taskId == 0) {
                    Toast.makeText(requireActivity(), "Unable to generate service report", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireActivity(), "Generating service report...", Toast.LENGTH_SHORT).show()
                    val object1 = JsonObject()
                    object1.addProperty("task_link", taskId)
                    viewModel.getServiceReport(object1, authToken)
                }
            }, editReson = { responseData ->

                if (responseData.status.orEmpty().contains("hold")) {
                    val reasonDialog = SetHoldFagment.newInstance(
                        responseData.id,
                        Gson().toJson(responseData.task?.holdReason.orEmpty())
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
            },
            onActionClick = { task -> showTaskActions(task) }
            )

        bindin.etSearc.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (::adapter.isInitialized) {
                    adapter.filter.filter(s.toString())
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        bindin.recyclerView.setHasFixedSize(true)
        bindin.recyclerView.adapter=adapter
        val object1 = JsonObject()
        object1.addProperty("status", param2)

        viewModel.assignTechnicianDataResponse.observe(viewLifecycleOwner) { response ->
            if (response.status_code == 200) {
                showDialog(response.message ?: "Technician assigned successfully")
                viewModel.getAllTasks(authToken, object1)
            }
            else{
                showDialog(response.message ?: "Unable to assign technician")
            }

        }


        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (!isAdded || message.isNullOrBlank()) return@observe
            showDialog(message)
        }

        viewModel.downloadpdf.observe(viewLifecycleOwner) { response ->
            if (response.status_code == 200 && !response.pdf_base64.isNullOrBlank()) {
                offerLocalServiceReport(response.pdf_base64)
            } else if (response.status_code == 200 && !response.url.isNullOrBlank()) {
                offerServiceReport(Constants.BASE_URL.trimEnd('/') + "/" + response.url.trimStart('/'))
            } else {
                showDialog(response.message ?: "Unable to download report")
            }
        }



        viewModel.upDateTaskStatusDataResponse.observe(viewLifecycleOwner) { response ->
            showDialog(response.message ?: "Task updated")
            viewModel.getAllTasks(authToken, object1)

        }
        viewModel.changePasswordDataResponse.observe(viewLifecycleOwner) { response ->
            if (!pendingTaskMutation) return@observe
            pendingTaskMutation = false
            if (!isAdded) return@observe
            val message = response.message ?: "Updated"
            showDialog(message)
            if ((response.status_code ?: 0) == 200) {
                viewModel.getAllTasks(authToken, object1)
            }
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
            if (!isAdded) return@observe
            try {
                if (data?.success == 200) {
                    sharedViewModel.setSharedData(data)
                    val tasks = data.data.orEmpty()
                    if (tasks.isEmpty()){
                        bindin.noDataLayout.visibility=View.VISIBLE
                        bindin.recyclerView.visibility=View.GONE
                        Glide.with(requireActivity()).load(R.drawable.list).into(bindin.animationView)
                    }
                    else {
                        bindin.recyclerView.visibility=View.VISIBLE
                        bindin.noDataLayout.visibility=View.GONE
                        adapter.addData(tasks)
                    }
                }
            } catch (error: Exception) {
                Toast.makeText(requireActivity(), "Unable to load tasks", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.getAllTasks(authToken, object1)

        feedbackFormLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val object12 = JsonObject()
                object12.addProperty("status", param2)
                viewModel.getAllTasks(authToken, object12)
            }
        }
    }
    private fun showTaskActions(task: com.prod.evergreen.models.TaskCreated) {
        if (!isAdded) return
        val role = accessType
        if (!com.prod.evergreen.helper.RoleAccess.canManageTasks(role)) {
            Toast.makeText(requireActivity(), "You cannot edit or delete this task", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            androidx.appcompat.app.AlertDialog.Builder(requireActivity())
                .setTitle(task.task?.name ?: "Task")
                .setItems(arrayOf("Edit Task", "Delete Task", "Cancel")) { dialog, which ->
                    when (which) {
                        0 -> showEditTaskDialog(task)
                        1 -> confirmDeleteTask(task)
                        else -> dialog.dismiss()
                    }
                }
                .show()
        } catch (error: Exception) {
            Toast.makeText(requireActivity(), "Unable to open task actions", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showEditTaskDialog(task: com.prod.evergreen.models.TaskCreated) {
        if (!isAdded) return
        val container = android.widget.LinearLayout(requireActivity()).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(40, 20, 40, 10)
        }
        val subject = android.widget.EditText(requireActivity()).apply {
            hint = "Subject"
            setText(task.task?.name.orEmpty())
        }
        val description = android.widget.EditText(requireActivity()).apply {
            hint = "Description"
            setText(task.task?.description.orEmpty())
        }
        container.addView(subject)
        container.addView(description)
        androidx.appcompat.app.AlertDialog.Builder(requireActivity())
            .setTitle("Edit Task")
            .setView(container)
            .setPositiveButton("Update") { _, _ ->
                val tokenValue = token ?: return@setPositiveButton
                val taskId = resolveTaskId(task)
                if (taskId == 0) {
                    Toast.makeText(requireActivity(), "Unable to update this task", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (subject.text.toString().isBlank()) {
                    Toast.makeText(requireActivity(), "Please Enter Subject", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val body = JsonObject()
                body.addProperty("task_link", taskId)
                body.addProperty("name", subject.text.toString().trim())
                body.addProperty("description", description.text.toString().trim())
                pendingTaskMutation = true
                viewModel.updateTask(body, tokenValue)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmDeleteTask(task: com.prod.evergreen.models.TaskCreated) {
        if (!isAdded) return
        androidx.appcompat.app.AlertDialog.Builder(requireActivity())
            .setTitle("Delete Task")
            .setMessage("Delete this task?")
            .setPositiveButton("Delete") { _, _ ->
                val tokenValue = token ?: return@setPositiveButton
                val taskId = resolveTaskId(task)
                if (taskId == 0) {
                    Toast.makeText(requireActivity(), "Unable to delete this task", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val body = JsonObject()
                body.addProperty("task_link", taskId)
                pendingTaskMutation = true
                viewModel.deleteTask(body, tokenValue)
            }
            .setNegativeButton("Cancel", null)
            .show()
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
        if (!isAdded) return
        val popup = PopupDialog.getInstance(requireActivity()) ?: return
        popup
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
        val authToken = token ?: return
        val object1 = JsonObject()
        object1.addProperty("task_user_link", id)
        object1.addProperty("status", "closed")
        viewModel.upDateTaskStatus(object1, authToken)
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
        val authToken = token
        if (!authToken.isNullOrBlank()) {
            viewModel.upDateTaskStatus(object1, authToken)
        }
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
        val authToken = token
        if (!authToken.isNullOrBlank()) {
            viewModel.updateHoldReasons(object1, authToken)
        }
    }

    object FeedbackFormResultKeys {
        const val FEEDBACK_SUBMISSION_RESULT = "feedback_submission_result"
        const val TASK_ID = "task_id"
    }

    private fun offerLocalServiceReport(base64: String) {
        if (!isAdded) return
        try {
            val bytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
            val file = java.io.File(requireActivity().cacheDir, "evergreen_service_report.pdf")
            file.writeBytes(bytes)
            val uri = androidx.core.content.FileProvider.getUriForFile(
                requireActivity(),
                "${requireActivity().packageName}.fileprovider",
                file
            )
            androidx.appcompat.app.AlertDialog.Builder(requireActivity())
                .setTitle("Service Report")
                .setMessage("The report is ready. You can open or share the PDF.")
                .setPositiveButton("Open") { _, _ ->
                    val open = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/pdf")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    startActivity(Intent.createChooser(open, "Open service report"))
                }
                .setNeutralButton("Share") { _, _ ->
                    val share = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_SUBJECT, "Evergreen Service Report")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    startActivity(Intent.createChooser(share, "Share service report"))
                }
                .setNegativeButton("Close", null)
                .show()
        } catch (error: Exception) {
            showDialog(error.message ?: "Unable to open service report")
        }
    }

    private fun offerServiceReport(pdfUrl: String) {
        if (!isAdded) return
        androidx.appcompat.app.AlertDialog.Builder(requireActivity())
            .setTitle("Service Report")
            .setMessage("The report is ready. You can open or share the PDF.")
            .setPositiveButton("Open") { _, _ -> openPdfInBrowser(pdfUrl) }
            .setNeutralButton("Share") { _, _ ->
                val share = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "Evergreen Service Report")
                    putExtra(Intent.EXTRA_TEXT, pdfUrl)
                }
                startActivity(Intent.createChooser(share, "Share service report"))
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun openPdfInBrowser(pdfUrl: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(pdfUrl) }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireActivity(), "No app found to open PDF", Toast.LENGTH_SHORT).show()
        }
    }
}
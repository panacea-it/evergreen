package com.prod.evergreen.activities

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.gson.JsonObject
import com.prod.evergreen.R
import com.prod.evergreen.XApplication
import com.prod.evergreen.adapters.NotificationsAdapter
import com.prod.evergreen.api.MainRepository
import com.prod.evergreen.api.MainViewModel
import com.prod.evergreen.api.MyViewModelFactory
import com.prod.evergreen.api.RetrofitService
import com.prod.evergreen.databinding.ActivityNotificationListBinding
import com.prod.evergreen.helper.ConstantValues
import com.prod.evergreen.helper.ProgressDialogUtil
import com.prod.evergreen.helper.SharedPreferencesHelper
import com.prod.evergreen.helper.customdialog.PopupDialog
import com.prod.evergreen.helper.customdialog.Styles
import com.prod.evergreen.helper.customdialog.listener.OnDialogButtonClickListener

class NotificationList : AppCompatActivity() {

    lateinit var notificationsAdapter: NotificationsAdapter

    private var token: String? = ""
    private var userid: Int? =null

    lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    private lateinit var viewModel: MainViewModel
    lateinit var binding: ActivityNotificationListBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        sharedPreferencesHelper= SharedPreferencesHelper(this)
        token=sharedPreferencesHelper.getValueString(ConstantValues.AuthToken)
        userid=sharedPreferencesHelper.getValueInt(ConstantValues.USER_ID)
        binding= ActivityNotificationListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setViewmodel()

//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

        viewModel.loading.observe(this) { data ->
            if (data){
                ProgressDialogUtil.showProgressDialog(this,"Loading")
            }
            else{
                ProgressDialogUtil.hideProgressDialog()
            }
        }

        viewModel.errorMessage.observe(this) { response ->
            showDialog(response)

        }
        viewModel.assignTechnicianDataResponse.observe(this) { response ->
            if (response.status_code == 200) {
                showDialog(response.message!!)
                viewModel.getNotifications(token!!)
            }
        }
        viewModel.notificationsListResponse.observe(this) { responseData ->
            if (responseData.status==200){
                if (responseData.data!!.isEmpty()){
                    binding.noDataLayout.visibility=View.VISIBLE
                    Glide.with(this)
                        .load(R.drawable.no_notification)
                        .into(binding.animationView)
                }
                else{
                    binding.noDataLayout.visibility=View.GONE
                    notificationsAdapter=NotificationsAdapter(sharedPreferencesHelper,responseData.data, { item ->
                        showNotificationDialog(item.title,item.description,item.taskLink,item.title,item.title,userid)
                    }) { item ->
                        if (item.taskLink != null && userid != null) {
                            val body = JsonObject()
                            body.addProperty("task_link", item.taskLink)
                            body.addProperty("technician_link", userid)
                            viewModel.assignTechnician(body, token!!)
                        }
                    }
                    binding.rvNotification.adapter= notificationsAdapter
                }

            }

        }

        viewModel.getNotifications(token!!)

binding.back.setOnClickListener {
    onBackPressedDispatcher.onBackPressed()
}
    }
    private fun setViewmodel() {
        val repository = MainRepository(RetrofitService.getInstance(this), XApplication.database.newsDao(), XApplication.database.companyDao())
        val viewModelFactory = MyViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]
    }

    private fun showNotificationDialog(
        title: String?,
        body: String?,
        taskLink: Int?,
        sno: String?,
        location: String?,
        userid: Int?
    ) {
        val dialog = Dialog(this,R.style.FullScreenDialogStyle)
        dialog.requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.custom_notification_dialog)

        val textViewTitle = dialog.findViewById<TextView>(R.id.textViewTitle)
        val textViewBody = dialog.findViewById<TextView>(R.id.textViewBody)
        // val textViewDescription = dialog.findViewById<TextView>(R.id.textViewDescription)
        val tvsn = dialog.findViewById<TextView>(R.id.tv_sn)
        val location1  = dialog.findViewById<TextView>(R.id.location)


        textViewTitle.text = title ?: ""
        textViewBody.text = body ?: ""

//        textViewDescription.text = description ?: ""
        tvsn.text = "S.NO : "+sno ?: ""
        location1.text = "Location : "+location ?: ""



        val buttonAccept = dialog.findViewById<Button>(R.id.buttonAccept)
        val canAccept = com.prod.evergreen.helper.RoleAccess.canAcceptTask(
            sharedPreferencesHelper.getValueString(ConstantValues.TYPE_ROLE)
        ) && taskLink != null
        buttonAccept.visibility = if (canAccept) View.VISIBLE else View.GONE
        buttonAccept.text = "Accept"
        buttonAccept.setOnClickListener {
            val object1 = JsonObject()
            object1.addProperty("task_link", taskLink!!.toInt())
            object1.addProperty("technician_link", userid)
            viewModel.assignTechnician(object1, token!!)
            dialog.dismiss()
        }

        val buttonDismiss = dialog.findViewById<Button>(R.id.buttonDismiss)
        buttonDismiss.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        val window = dialog.window
        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
    }
    fun showDialog(message: String) {
        PopupDialog.getInstance(this)!!
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
}
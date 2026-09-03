package com.prod.evergreen.activities

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.JsonObject
import com.prod.evergreen.Enums
import com.prod.evergreen.R
import com.prod.evergreen.XApplication
import com.prod.evergreen.adapters.CustomMenuAdapter
import com.prod.evergreen.api.MainRepository
import com.prod.evergreen.api.MainViewModel
import com.prod.evergreen.api.MyViewModelFactory
import com.prod.evergreen.api.RetrofitService
import com.prod.evergreen.databinding.ActivityMainBinding
import com.prod.evergreen.dialogs.BlankFragment
import com.prod.evergreen.helper.ConstantValues
import com.prod.evergreen.helper.ProgressDialogUtil
import com.prod.evergreen.helper.RoleAccess
import com.prod.evergreen.helper.SharedPreferencesHelper
import android.provider.Settings
import com.prod.evergreen.helper.customdialog.PopupDialog
import com.prod.evergreen.helper.customdialog.Styles
import com.prod.evergreen.helper.customdialog.listener.OnDialogButtonClickListener
import com.prod.evergreen.models.ListItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel
    private lateinit var navController: NavController
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var binding: ActivityMainBinding
    private var userid: Int? = null
    private var token: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setViewmodel()
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            handleNotificationIntent(intent)
        }


        sharedPreferencesHelper = SharedPreferencesHelper(this)
        userid = sharedPreferencesHelper.getValueInt(ConstantValues.USER_ID)
        token = sharedPreferencesHelper.getValueString(ConstantValues.AuthToken)

        if (intent.getBooleanExtra("company_inactive", false)) {
            Toast.makeText(this, "Your company is inactive", Toast.LENGTH_LONG).show()
        }

        navController = findNavController(R.id.nav_host_fragment)
        navController.addOnDestinationChangedListener { _, _, _ ->
            setHomeChrome(true)
        }
        val accessLevel = getAccessLevelFromString(sharedPreferencesHelper.getValueString(
            ConstantValues.TYPE_ROLE
        ))
        val items = getItemsForAccessLevel(accessLevel)

        binding.navHeader.accessLevel.text = com.prod.evergreen.helper.RoleLabels.display(
            sharedPreferencesHelper.getValueString(ConstantValues.TYPE_ROLE)
        )

        binding.navigationRv.adapter = CustomMenuAdapter(items) { selectedItem ->
            navigateToDestination(selectedItem.destinationId, selectedItem.name)
            binding.drawerLayout.closeDrawers()
        }



        if (intent.getBooleanExtra("open_users", false)) {
            navigateToDestination(R.id.amc_mangers, "Users List")
        }
        if (intent.getBooleanExtra("open_create_task", false)) {
            intent.getStringExtra("equipment_data")?.let {
                com.prod.evergreen.helper.DashboardNav.pendingEquipmentJson = it
            }
            navigateToDestination(R.id.createTaskFragment, "Create Task")
        }
        if (intent.getBooleanExtra("open_create_amc", false)) {
            navigateToDestination(R.id.createAmcFragment, "Create AMC")
        }
        if (intent.getBooleanExtra("open_service_report_form", false)) {
            navigateToDestination(R.id.serviceReportFormFragment, "Generate Service Report")
        }

        binding.navHeader.userName.text=sharedPreferencesHelper.getValueString(ConstantValues.PREF_USERNAME)
        binding.navHeader.mobileNumber.text=sharedPreferencesHelper.getValueString(ConstantValues.PREF_MOBILE)
        binding.logout.setOnClickListener { confirmLogout() }
        viewModel.assignTechnicianDataResponse.observe(this) { response ->
            if (response.status_code==200)
            showDialog(response.message!!)


        }
        registerFcmToken()

        viewModel.loading.observe(this) { data ->
            if (data){
                ProgressDialogUtil.showProgressDialog(this,"Loading")
            }
            else{
                ProgressDialogUtil.hideProgressDialog()
            }
        }
        binding.scanner.setOnClickListener {
            startActivity(Intent(this@MainActivity, QrScanner::class.java))
        }

        binding.notifications.setOnClickListener {
            startActivity(Intent(this@MainActivity, NotificationList::class.java))
        }
        binding.menu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    return
                }
                if (!this@MainActivity::navController.isInitialized ||
                    navController.currentDestination?.id == R.id.homeFragment
                ) {
                    moveTaskToBack(true)
                    return
                }
                navigateToDestination(R.id.homeFragment, "Home")
            }
        })
    }

    private fun navigateToDestination(destinationId: Int, title: String) {
        if (!this::navController.isInitialized) return
        if (navController.currentDestination?.id != destinationId) {
            val options = NavOptions.Builder()
                .setPopUpTo(R.id.homeFragment, destinationId == R.id.homeFragment)
                .setLaunchSingleTop(true)
                .build()
            navController.navigate(destinationId, null, options)
        }
        binding.title.text = title
    }

    private fun getItemsForAccessLevel(accessLevel: Enums.Companion.ClientRole): List<ListItem> {
        val allItems = listOf(
            ListItem("Home", R.drawable.home_nav, R.id.homeFragment, "Home"),
            ListItem("Companies", R.drawable.ic_companys_list_icon, R.id.companiesFragment, "Company’s List"),
            ListItem("Equipment", R.drawable.ic_equipments_list_icon, R.id.equipmentFragment, "Equipments List"),
            ListItem("Tasks", R.drawable.ic_tasks_list_icon, R.id.taskFragment, "Tasks List"),
            ListItem("Create Task", R.drawable.ic_create_task_icon, R.id.createTaskFragment, "Create Task"),
            ListItem("Users", R.drawable.ic_users_list_icon, R.id.amc_mangers, "Users List"),
            ListItem("Assign Tasks", R.drawable.ic_assign_tasks_to_technician_icon, R.id.taskFragment, "Assign Tasks to Technician"),
            ListItem("Service Reports", R.drawable.task_square, R.id.serviceReportsFragment, "Service Reports"),
            ListItem("Create AMC", R.drawable.ic_create_amc_icon, R.id.createAmcFragment, "Create AMC"),
            ListItem("Upload Equipment", R.drawable.ic_upload_equipments_excel_data_icon, R.id.uploadEqpmntExcelData, "Upload Equipments Excel Data"),
            ListItem("Upload AMCs", R.drawable.ic_upload_equipments_excel_data_icon, R.id.uploadAmcExcelData, "Upload AMCs Excel Data"),
            ListItem("Upload Technicians", R.drawable.ic_upload_equipments_excel_data_icon, R.id.uploadTechnicianExcelData, "Upload Technicians Excel Data"),
            ListItem("Download QR", R.drawable.ic_pending_task_list_icon, R.id.downloadQrFragment, "Download QR"),
            ListItem("Add Equipment", R.drawable.ic_add_equipment_icon, R.id.addEquipmentFragment, "Add Equipment")
        )

        return when (accessLevel) {
            Enums.Companion.ClientRole.eg_super_admin -> allItems.filter {
                it.key != "Equipments List"
            }
            Enums.Companion.ClientRole.eg_admin -> allItems.filter {
                it.key != "Equipments List"
            }

            Enums.Companion.ClientRole.technician -> allItems.filter {
                it.key in listOf("Home", "Equipments List", "Tasks List")
            }

            Enums.Companion.ClientRole.client -> allItems.filter {
                it.key in listOf("Home", "Equipments List", "Tasks List", "Create Task", "Add Equipment", "Download QR")
            }

            Enums.Companion.ClientRole.client_admin -> allItems.filter {
                it.key in listOf(
                    "Home",
                    "Equipments List",
                    "Tasks List",
                    "Create Task",
                    "Upload Equipments Excel Data",
                    "Add Equipment",
                    "Download QR",
                    "Users List"
                )
            }

            Enums.Companion.ClientRole.others -> allItems.filter {
                it.key in listOf("Home", "Notifications")
            }
        }.filter { item ->
            item.key != "Assign Tasks to Technician" ||
                RoleAccess.canAssignTechnician(accessLevel.name)
        }.filter { item ->
            item.key != "Service Reports" ||
                RoleAccess.canManageServiceReports(accessLevel.name)
        }
    }

    private fun getAccessLevelFromString(roleString: String?): Enums.Companion.ClientRole {
        return when (roleString?.lowercase(Locale.ROOT)) {
            "eg_super_admin" -> Enums.Companion.ClientRole.eg_super_admin
            "technician" -> Enums.Companion.ClientRole.technician
            "eg_admin" -> Enums.Companion.ClientRole.eg_admin
            "client" -> Enums.Companion.ClientRole.client
            "client_admin" -> Enums.Companion.ClientRole.client_admin
            else -> Enums.Companion.ClientRole.others
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra("open_users", false)) {
            navigateToDestination(R.id.amc_mangers, "Users List")
        }
        if (intent.getBooleanExtra("open_create_task", false)) {
            intent.getStringExtra("equipment_data")?.let {
                com.prod.evergreen.helper.DashboardNav.pendingEquipmentJson = it
            }
            navigateToDestination(R.id.createTaskFragment, "Create Task")
        }
        if (intent.getBooleanExtra("open_create_amc", false)) {
            navigateToDestination(R.id.createAmcFragment, "Create AMC")
        }
        if (intent.getBooleanExtra("open_service_report_form", false)) {
            navigateToDestination(R.id.serviceReportFormFragment, "Generate Service Report")
        }
        handleNotificationIntent(intent)
    }
//
private fun handleNotificationIntent(intent: Intent) {
        val title = intent.getStringExtra("title")
        val body = intent.getStringExtra("body")
        val taskLink = intent.getStringExtra("task_link")
        val description = intent.getStringExtra("description")
        val channel_id = intent.getStringExtra("channel_id")
        val location = intent.getStringExtra("location")
        val sno = intent.getStringExtra("sno")

        // Log.d("NotificationDataReceived", "Title: $title, Body: $body, Task Link: $taskLink, Description: $description,location:location,sno:sno)

        if (channel_id != null) {
            // Use this data to show a dialog or navigate to a specific screen
            showNotificationDialog(title, body, taskLink, description, sno, location)

    }
}

    private fun showNotificationDialog(title: String?, body: String?, taskLink: String?, description: String?, sno:String?,location:String?) {
        val dialog = Dialog(this,R.style.FullScreenDialogStyle)
        dialog.requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.custom_notification_dialog)
        val textViewTitle = dialog.findViewById<TextView>(R.id.textViewTitle)
        val textViewBody = dialog.findViewById<TextView>(R.id.textViewBody)
       // val textViewDescription = dialog.findViewById<TextView>(R.id.textViewDescription)
        val tvsn = dialog.findViewById<TextView>(R.id.tv_sn)
        val location1  = dialog.findViewById<TextView>(R.id.location)


        textViewTitle.text = title ?: ""
        textViewBody.text = body ?: ""
        tvsn.text = "S.NO : "+sno ?: ""
        location1.text = "Location : "+location ?: ""



        val buttonAccept = dialog.findViewById<Button>(R.id.buttonAccept)
        val canAccept = RoleAccess.canAcceptTask(
            sharedPreferencesHelper.getValueString(ConstantValues.TYPE_ROLE)
        ) && !taskLink.isNullOrBlank()
        buttonAccept.visibility = if (canAccept) android.view.View.VISIBLE else android.view.View.GONE
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
    private fun registerFcmToken() {
        val authToken = token
        if (authToken.isNullOrBlank()) return
        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful || task.result.isNullOrBlank() || deviceId.isNullOrBlank()) {
                return@addOnCompleteListener
            }
            sharedPreferencesHelper.save(ConstantValues.PREFCM_Tooken, task.result)
            val body = JsonObject()
            body.addProperty("fcm_id", task.result)
            body.addProperty("device_id", deviceId)
            viewModel.upsertToken(body, authToken)
        }
    }

    private fun setViewmodel() {
        val repository = MainRepository(
            RetrofitService.getInstance(this@MainActivity),
            XApplication.database.newsDao(),
            XApplication.database.companyDao())
        val viewModelFactory = MyViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]
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



    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    private fun removeFirebaseMessage(){
        CoroutineScope(Dispatchers.Default).launch {
            FirebaseMessaging.getInstance().isAutoInitEnabled = false
            FirebaseInstallations.getInstance().delete()
            FirebaseMessaging.getInstance().deleteToken()
        }
    }

    fun setTitleTextView(title: String) {
        binding.title.text = title
    }

    private fun setHomeChrome(isHome: Boolean) {
        binding.toolbar.visibility = if (isHome) View.GONE else View.VISIBLE
        val params = binding.navContainer.layoutParams as LinearLayout.LayoutParams
        params.topMargin = if (isHome) 0 else resources.getDimensionPixelSize(R.dimen._5sdp)
        binding.navContainer.layoutParams = params
        if (isHome) {
            binding.navContainer.background = ColorDrawable(Color.parseColor("#F9FAFC"))
            binding.mainActivityContentId.setBackgroundColor(Color.parseColor("#F9FAFC"))
        } else {
            binding.navContainer.background =
                ContextCompat.getDrawable(this, R.drawable.top_rounded_corners)
            binding.mainActivityContentId.setBackgroundResource(R.drawable.kitechn_back)
        }
    }

    fun confirmLogout() {
        PopupDialog.getInstance(this)!!
            .setStyle(Styles.IOS)!!
            .setHeading("Logout")!!
            .setDescription("Are you sure want to logout")!!
            .setCancelable(false)!!
            .setPositiveButtonText(getString(R.string.positive))!!
            .showDialog(object : OnDialogButtonClickListener() {
                override fun onPositiveClicked(dialog: Dialog?) {
                    super.onPositiveClicked(dialog)
                    sharedPreferencesHelper.clearSharedPreferences()
                    startActivity(Intent(this@MainActivity, Login::class.java))
                    finishAffinity()
                }
            }, false)
    }

}
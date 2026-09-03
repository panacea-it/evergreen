package com.prod.evergreen.activities

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModelProvider
import com.example.app.ui.notifications.NotificationListScreen
import com.example.app.ui.notifications.NotificationUiItem
import com.google.gson.JsonObject
import com.prod.evergreen.R
import com.prod.evergreen.XApplication
import com.prod.evergreen.api.MainRepository
import com.prod.evergreen.api.MainViewModel
import com.prod.evergreen.api.MyViewModelFactory
import com.prod.evergreen.api.RetrofitService
import com.prod.evergreen.helper.ConstantValues
import com.prod.evergreen.helper.DateConverter
import com.prod.evergreen.helper.ProgressDialogUtil
import com.prod.evergreen.helper.RoleAccess
import com.prod.evergreen.helper.SharedPreferencesHelper
import com.prod.evergreen.helper.customdialog.PopupDialog
import com.prod.evergreen.helper.customdialog.Styles
import com.prod.evergreen.helper.customdialog.listener.OnDialogButtonClickListener
import com.prod.evergreen.models.DataItem

class NotificationList : AppCompatActivity() {
    private var token: String? = ""
    private var userid: Int? = null
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var viewModel: MainViewModel
    private val itemsState = mutableStateOf<List<NotificationUiItem>>(emptyList())
    private val loadingMore = mutableStateOf(false)
    private val emptyState = mutableStateOf(false)
    private var page = 1
    private var hasMore = true
    private var requestInFlight = false
    private val rawItems = mutableListOf<DataItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        sharedPreferencesHelper = SharedPreferencesHelper(this)
        token = sharedPreferencesHelper.getValueString(ConstantValues.AuthToken)
        userid = sharedPreferencesHelper.getValueInt(ConstantValues.USER_ID)
        setViewmodel()
        setContent {
            val items by itemsState
            val more by loadingMore
            val empty by emptyState
            NotificationListScreen(
                items = items,
                loading = false,
                loadingMore = more,
                empty = empty,
                onBackClick = { onBackPressedDispatcher.onBackPressed() },
                onItemClick = { item ->
                    val raw = rawItems.firstOrNull { it.id == item.id }
                    showNotificationDialog(raw?.title, raw?.description, raw?.taskLink, raw?.title, raw?.title, userid)
                },
                onAcceptClick = { item -> accept(item.id) },
                onLoadMore = { loadPage(false) }
            )
        }
        viewModel.loading.observe(this) { data ->
            if (data && page == 1) {
                ProgressDialogUtil.showProgressDialog(this, "Loading")
            } else {
                ProgressDialogUtil.hideProgressDialog()
            }
        }
        viewModel.errorMessage.observe(this) { response ->
            requestInFlight = false
            loadingMore.value = false
            showDialog(response)
        }
        viewModel.assignTechnicianDataResponse.observe(this) { response ->
            if (response.status_code == 200) {
                showDialog(response.message!!)
                page = 1
                hasMore = true
                rawItems.clear()
                loadPage(true)
            }
        }
        viewModel.notificationsListResponse.observe(this) { responseData ->
            requestInFlight = false
            loadingMore.value = false
            if (responseData.status != 200) return@observe
            val incoming = responseData.data.orEmpty()
            if (page == 1) {
                rawItems.clear()
            }
            rawItems.addAll(incoming)
            itemsState.value = rawItems.map { it.toUi() }
            emptyState.value = rawItems.isEmpty()
            hasMore = responseData.hasMore == true
            if (incoming.isNotEmpty()) {
                page += 1
            }
        }
        loadPage(true)
    }

    private fun loadPage(reset: Boolean) {
        val auth = token ?: return
        if (requestInFlight) return
        if (!reset && !hasMore) return
        if (reset) {
            page = 1
            hasMore = true
        }
        requestInFlight = true
        loadingMore.value = page > 1
        viewModel.getNotificationsPage(auth, page)
    }

    private fun accept(id: Int) {
        val raw = rawItems.firstOrNull { it.id == id } ?: return
        if (raw.taskLink == null || userid == null) return
        val body = JsonObject()
        body.addProperty("task_link", raw.taskLink)
        body.addProperty("technician_link", userid)
        viewModel.assignTechnician(body, token!!)
    }

    private fun DataItem.toUi(): NotificationUiItem {
        val canAccept = RoleAccess.canAcceptTask(
            sharedPreferencesHelper.getValueString(ConstantValues.TYPE_ROLE)
        ) && taskLink != null
        return NotificationUiItem(
            id = id ?: 0,
            title = title.orEmpty(),
            description = description.orEmpty(),
            time = DateConverter.convertToLocalUtcAndFormat(createdAt),
            unread = isRead != true,
            canAccept = canAccept
        )
    }

    private fun setViewmodel() {
        val repository = MainRepository(
            RetrofitService.getInstance(this),
            XApplication.database.newsDao(),
            XApplication.database.companyDao()
        )
        viewModel = ViewModelProvider(this, MyViewModelFactory(repository))[MainViewModel::class.java]
    }

    private fun showNotificationDialog(
        title: String?,
        body: String?,
        taskLink: Int?,
        sno: String?,
        location: String?,
        userid: Int?
    ) {
        val dialog = Dialog(this, R.style.FullScreenDialogStyle)
        dialog.requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.custom_notification_dialog)
        dialog.findViewById<TextView>(R.id.textViewTitle).text = title ?: ""
        dialog.findViewById<TextView>(R.id.textViewBody).text = body ?: ""
        dialog.findViewById<TextView>(R.id.tv_sn).text = "S.NO : ${sno ?: ""}"
        dialog.findViewById<TextView>(R.id.location).text = "Location : ${location ?: ""}"
        val buttonAccept = dialog.findViewById<Button>(R.id.buttonAccept)
        val canAccept = RoleAccess.canAcceptTask(
            sharedPreferencesHelper.getValueString(ConstantValues.TYPE_ROLE)
        ) && taskLink != null
        buttonAccept.visibility = if (canAccept) View.VISIBLE else View.GONE
        buttonAccept.text = "Accept"
        buttonAccept.setOnClickListener {
            val object1 = JsonObject()
            object1.addProperty("task_link", taskLink!!)
            object1.addProperty("technician_link", userid)
            viewModel.assignTechnician(object1, token!!)
            dialog.dismiss()
        }
        dialog.findViewById<Button>(R.id.buttonDismiss).setOnClickListener { dialog.dismiss() }
        dialog.show()
        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
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

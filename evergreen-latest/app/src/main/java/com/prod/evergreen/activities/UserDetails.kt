package com.prod.evergreen.activities

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModelProvider
import com.example.app.ui.profile.ProfileScreen
import com.example.app.ui.profile.currentUserProfile
import com.example.app.ui.profile.toProfileData
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.prod.evergreen.XApplication
import com.prod.evergreen.api.MainRepository
import com.prod.evergreen.api.MainViewModel
import com.prod.evergreen.api.MyViewModelFactory
import com.prod.evergreen.api.RetrofitService
import com.prod.evergreen.helper.ConstantValues
import com.prod.evergreen.helper.RoleAccess
import com.prod.evergreen.helper.SharedPreferencesHelper
import com.prod.evergreen.models.Users

class UserDetails : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private val userState = mutableStateOf<Users?>(null)
    private var canManage = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        sharedPreferencesHelper = SharedPreferencesHelper(this)
        val repository = MainRepository(
            RetrofitService.getInstance(this),
            XApplication.database.newsDao(),
            XApplication.database.companyDao()
        )
        viewModel = ViewModelProvider(this, MyViewModelFactory(repository))[MainViewModel::class.java]
        canManage = RoleAccess.canManageUsers(sharedPreferencesHelper.getValueString(ConstantValues.TYPE_ROLE))
        userState.value = parseUser()

        setContent {
            val user by userState
            val profile = user?.toProfileData() ?: currentUserFromPrefs()
            ProfileScreen(
                profile = profile,
                showBack = true,
                showEdit = canManage && user != null,
                onBackClick = { onBackPressedDispatcher.onBackPressed() },
                onMoreClick = { showMoreActions() },
                onEditClick = { openEdit() },
                onHomeClick = { returnToMain() },
                onEquipmentClick = { returnToMain() },
                onAddClick = { returnToMain() },
                onTasksClick = { returnToMain() },
                onProfileClick = { returnToMain() },
                onScanClick = { startActivity(Intent(this, QrScanner::class.java)) }
            )
        }

        viewModel.changePasswordDataResponse.observe(this) { data ->
            Toast.makeText(this, data.message ?: "Updated", Toast.LENGTH_SHORT).show()
            if (data.status_code == 200) {
                finish()
            }
        }
        viewModel.errorMessage.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        userState.value = parseUser()
    }

    private fun parseUser(): Users? {
        return intent.getStringExtra("user_data")?.let { Gson().fromJson(it, Users::class.java) }
    }

    private fun currentUserFromPrefs() = currentUserProfile(
        name = sharedPreferencesHelper.getValueString(ConstantValues.PREF_USERNAME),
        email = sharedPreferencesHelper.getValueString(ConstantValues.PREF_EMAIL),
        phone = sharedPreferencesHelper.getValueString(ConstantValues.PREF_MOBILE),
        role = sharedPreferencesHelper.getValueString(ConstantValues.TYPE_ROLE),
        userId = sharedPreferencesHelper.getValueInt(ConstantValues.USER_ID),
        location = sharedPreferencesHelper.getValueString(ConstantValues.LOCATION),
        company = sharedPreferencesHelper.getValueString(ConstantValues.COMPANYNAME)
    )

    private fun openEdit() {
        val user = userState.value
        if (user == null) {
            Toast.makeText(this, "Unable to edit this profile", Toast.LENGTH_SHORT).show()
            return
        }
        startActivity(Intent(this, AddUser::class.java).putExtra("user_data", Gson().toJson(user)))
    }

    private fun showMoreActions() {
        val user = userState.value
        val options = if (canManage && user != null) {
            arrayOf("Edit details", "Delete user", "Cancel")
        } else {
            arrayOf("Edit details", "Cancel")
        }
        AlertDialog.Builder(this)
            .setTitle(user?.name?.takeIf { it.isNotBlank() } ?: "User")
            .setItems(options) { dialog, which ->
                when (options[which]) {
                    "Edit details" -> openEdit()
                    "Delete user" -> user?.let { confirmDelete(it) }
                    else -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun confirmDelete(user: Users) {
        AlertDialog.Builder(this)
            .setTitle("Delete user")
            .setMessage("Delete ${user.name ?: "this user"}?")
            .setPositiveButton("Delete") { _, _ ->
                val token = sharedPreferencesHelper.getValueString(ConstantValues.AuthToken) ?: return@setPositiveButton
                val body = JsonObject()
                body.addProperty("user_link", user.id)
                viewModel.deleteUser(body, token)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun returnToMain() {
        startActivity(
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        )
        finish()
    }

    companion object {
        fun open(context: Context, user: Users? = null) {
            val intent = Intent(context, UserDetails::class.java)
            if (user != null) {
                intent.putExtra("user_data", Gson().toJson(user))
            }
            context.startActivity(intent)
        }
    }
}

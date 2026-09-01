package com.prod.evergreen.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.prod.evergreen.XApplication
import com.prod.evergreen.api.MainRepository
import com.prod.evergreen.api.MainViewModel
import com.prod.evergreen.api.MyViewModelFactory
import com.prod.evergreen.api.RetrofitService
import com.prod.evergreen.databinding.ActivityUserDetailsBinding
import com.prod.evergreen.helper.ConstantValues
import com.prod.evergreen.helper.RoleAccess
import com.prod.evergreen.helper.SharedPreferencesHelper
import com.prod.evergreen.models.Users
import com.prod.evergreen.models.attachedCompanyLabel

class UserDetails : AppCompatActivity() {
    private lateinit var binding: ActivityUserDetailsBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private var user: Users? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPreferencesHelper = SharedPreferencesHelper(this)
        val repository = MainRepository(
            RetrofitService.getInstance(this),
            XApplication.database.newsDao(),
            XApplication.database.companyDao()
        )
        viewModel = ViewModelProvider(this, MyViewModelFactory(repository))[MainViewModel::class.java]
        user = Gson().fromJson(intent.getStringExtra("user_data"), Users::class.java)
        bindUser()
        binding.back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        val role = sharedPreferencesHelper.getValueString(ConstantValues.TYPE_ROLE)
        val canManage = RoleAccess.canManageUsers(role)
        binding.actions.visibility = if (canManage) View.VISIBLE else View.GONE
        binding.editUser.setOnClickListener {
            startActivity(
                Intent(this, AddUser::class.java)
                    .putExtra("user_data", Gson().toJson(user))
            )
        }
        binding.deleteUser.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete user")
                .setMessage("Delete ${user?.name ?: "this user"}?")
                .setPositiveButton("Delete") { _, _ ->
                    val token = sharedPreferencesHelper.getValueString(ConstantValues.AuthToken) ?: return@setPositiveButton
                    val body = JsonObject()
                    body.addProperty("user_link", user?.id)
                    viewModel.deleteUser(body, token)
                }
                .setNegativeButton("Cancel", null)
                .show()
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
        intent.getStringExtra("user_data")?.let {
            user = Gson().fromJson(it, Users::class.java)
            bindUser()
        }
    }

    private fun bindUser() {
        val current = user ?: return
        val role = when (current.access_level) {
            "client_admin" -> "Client Admin"
            else -> current.access_level.orEmpty().replace('_', ' ')
                .replaceFirstChar { it.uppercase() }
        }.ifBlank { "-" }
        binding.details.text = """
            Name : ${current.name ?: "-"}
            Role : $role
            Mobile : ${current.phone ?: "-"}
            Email : ${current.email ?: "-"}
            Location : ${current.location ?: "-"}
            Company : ${current.attachedCompanyLabel()}
        """.trimIndent()
    }
}

package com.prod.evergreen.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.example.app.ui.profile.ProfileScreen
import com.example.app.ui.profile.currentUserProfile
import com.google.gson.Gson
import com.prod.evergreen.R
import com.prod.evergreen.activities.AddUser
import com.prod.evergreen.activities.MainActivity
import com.prod.evergreen.activities.QrScanner
import com.prod.evergreen.helper.ConstantValues
import com.prod.evergreen.helper.RoleAccess
import com.prod.evergreen.helper.SharedPreferencesHelper
import com.prod.evergreen.helper.TabNav
import com.prod.evergreen.models.Users

class ProfileFragment : Fragment() {
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        sharedPreferencesHelper = SharedPreferencesHelper(requireActivity())
        val profile = currentUserProfile(
            name = sharedPreferencesHelper.getValueString(ConstantValues.PREF_USERNAME),
            email = sharedPreferencesHelper.getValueString(ConstantValues.PREF_EMAIL),
            phone = sharedPreferencesHelper.getValueString(ConstantValues.PREF_MOBILE),
            role = sharedPreferencesHelper.getValueString(ConstantValues.TYPE_ROLE),
            userId = sharedPreferencesHelper.getValueInt(ConstantValues.USER_ID),
            location = sharedPreferencesHelper.getValueString(ConstantValues.LOCATION),
            company = sharedPreferencesHelper.getValueString(ConstantValues.COMPANYNAME)
        )
        val canEdit = RoleAccess.canManageUsers(
            sharedPreferencesHelper.getValueString(ConstantValues.TYPE_ROLE)
        ) || !profile.userId.isBlank()

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ProfileScreen(
                    profile = profile,
                    showBack = false,
                    showEdit = canEdit,
                    onMenuClick = { openDrawer() },
                    onMoreClick = { showEditOptions() },
                    onEditClick = { openEdit() },
                    onHomeClick = { TabNav.home(this@ProfileFragment) },
                    onEquipmentClick = { TabNav.equipment(this@ProfileFragment) },
                    onAddClick = { TabNav.createAmc(this@ProfileFragment) },
                    onTasksClick = { TabNav.tasks(this@ProfileFragment) },
                    onProfileClick = {},
                    onScanClick = {
                        startActivity(Intent(requireActivity(), QrScanner::class.java))
                    },
                    showLogout = true,
                    onLogoutClick = { confirmLogout() }
                )
            }
        }
    }

    private fun showEditOptions() {
        AlertDialog.Builder(requireActivity())
            .setTitle("Profile")
            .setItems(arrayOf("Edit details", "Cancel")) { dialog, which ->
                if (which == 0) openEdit() else dialog.dismiss()
            }
            .show()
    }

    private fun openEdit() {
        val userId = sharedPreferencesHelper.getValueInt(ConstantValues.USER_ID)
        if (userId == null || userId == 0) {
            Toast.makeText(requireActivity(), "Unable to edit this profile", Toast.LENGTH_SHORT).show()
            return
        }
        val user = Users(
            id = userId,
            name = sharedPreferencesHelper.getValueString(ConstantValues.PREF_USERNAME),
            phone = sharedPreferencesHelper.getValueString(ConstantValues.PREF_MOBILE),
            email = sharedPreferencesHelper.getValueString(ConstantValues.PREF_EMAIL),
            access_level = sharedPreferencesHelper.getValueString(ConstantValues.TYPE_ROLE),
            location = sharedPreferencesHelper.getValueString(ConstantValues.LOCATION),
            company_name = sharedPreferencesHelper.getValueString(ConstantValues.COMPANYNAME)
        )
        startActivity(Intent(requireActivity(), AddUser::class.java).putExtra("user_data", Gson().toJson(user)))
    }

    private fun confirmLogout() {
        (activity as? MainActivity)?.confirmLogout()
    }

    private fun openDrawer() {
        (activity as? MainActivity)
            ?.findViewById<androidx.drawerlayout.widget.DrawerLayout>(R.id.drawer_layout)
            ?.openDrawer(GravityCompat.START)
    }
}

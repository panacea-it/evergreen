package com.prod.evergreen.helper

import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.prod.evergreen.R
import com.prod.evergreen.activities.MainActivity

object TabNav {
    fun home(fragment: Fragment) = go(fragment, R.id.homeFragment, "Home")
    fun equipment(fragment: Fragment) = go(fragment, R.id.equipmentFragment, "Equipments List")
    fun createAmc(fragment: Fragment) = go(fragment, R.id.createAmcFragment, "Create AMC")
    fun tasks(fragment: Fragment) = go(fragment, R.id.taskFragment, "Tasks List")
    fun profile(fragment: Fragment) = go(fragment, R.id.profileFragment, "Profile")
    fun serviceReports(fragment: Fragment) = go(fragment, R.id.serviceReportsFragment, "Service Reports")

    private fun go(fragment: Fragment, destinationId: Int, title: String) {
        val options = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setPopUpTo(destinationId, inclusive = false)
            .build()
        runCatching {
            fragment.findNavController().navigate(destinationId, null, options)
        }
        (fragment.activity as? MainActivity)?.setTitleTextView(title)
    }
}

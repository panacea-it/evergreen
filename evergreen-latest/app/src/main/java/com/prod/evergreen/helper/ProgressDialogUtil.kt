package com.prod.evergreen.helper

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.WindowManager
import com.prod.evergreen.R

object ProgressDialogUtil {
    private var alertDialog: AlertDialog? = null

    fun showProgressDialog(context: Context, message: String="") {
        if (alertDialog == null) {
            val builder = AlertDialog.Builder(context)
            val inflater = LayoutInflater.from(context)
            val view = inflater.inflate(R.layout.custom_progress_dialog, null)

            builder.setView(view)
            builder.setCancelable(false)
            alertDialog = builder.create()

            // Make the background transparent
            alertDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
            alertDialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }
        alertDialog?.show()
    }

    fun hideProgressDialog() {
        alertDialog?.takeIf { it.isShowing }?.dismiss()
        alertDialog = null
    }
}

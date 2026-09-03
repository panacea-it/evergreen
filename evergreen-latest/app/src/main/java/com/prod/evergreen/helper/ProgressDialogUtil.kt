package com.prod.evergreen.helper

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.WindowManager
import com.prod.evergreen.R

object ProgressDialogUtil {
    private var alertDialog: AlertDialog? = null

    fun showProgressDialog(context: Context, message: String = "") {
        if (alertDialog?.isShowing == true) return
        hideProgressDialog()
        val builder = AlertDialog.Builder(context)
        val view = LayoutInflater.from(context).inflate(R.layout.custom_progress_dialog, null)
        builder.setView(view)
        builder.setCancelable(true)
        alertDialog = builder.create()
        alertDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        alertDialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        runCatching { alertDialog?.show() }
    }

    fun hideProgressDialog() {
        runCatching { alertDialog?.dismiss() }
        alertDialog = null
    }
}

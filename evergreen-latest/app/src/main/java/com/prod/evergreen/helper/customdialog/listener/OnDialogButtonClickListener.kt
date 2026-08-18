package com.prod.evergreen.helper.customdialog.listener

import android.app.Dialog


abstract class OnDialogButtonClickListener {
    open  fun onPositiveClicked(dialog: Dialog?) {
        dismiss(dialog)
    }

    open  fun onNegativeClicked(dialog: Dialog?) {
        dismiss(dialog)
    }

      open fun onDismissClicked(dialog: Dialog) {
        dialog.dismiss()
    }

    private fun dismiss(dialog: Dialog?) {
        if (dialog != null && dialog.isShowing) {
            dialog.dismiss()
        }
    }
}
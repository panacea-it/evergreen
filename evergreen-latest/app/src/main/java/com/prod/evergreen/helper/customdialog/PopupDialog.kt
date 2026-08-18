
package com.prod.evergreen.helper.customdialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context

class PopupDialog private constructor(private val context: Context) {
    /**
     * Popup Dialog class.
     * Created by Saad Ahmed on 17-May-2022.
     * Github: [...](https://github.com/saadahmedscse/Android-Popup-Dialog)
     * A custom android popup dialog library which provides you a lot of popup dialog and progress dialog with and without animation
     */
    private val dialog: Dialog?

    /**
     * Private constructor of popup dialog
     * @param context is required to create instance of dialog
     */
    init {
        dialog = Dialog(context)
    }

    /**
     * setStyle function will set the style which you want
     * @param style is required to create instance of create dialog class
     * @return instance of create dialog class
     */
    fun setStyle(style: Styles): CreateDialog? {
        instance = null
        return CreateDialog.getInstance(context, style!!, dialog!!)
    }

    /**
     * Dismiss the dialog if it is showing
     */
    fun dismissDialog() {
        if (dialog != null && dialog.isShowing) {
            dialog.dismiss()
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: PopupDialog? = null

        /**
         * Static function to get instance of popup dialog class
         * @param context is required to create instance of popup dialog class
         * @return instance of popup dialog class
         */
        fun getInstance(context: Context): PopupDialog? {
            if (instance == null) {
                instance = PopupDialog(context)
            }
            return instance
        }
    }
}
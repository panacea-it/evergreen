package com.prod.evergreen.dialogs

import android.app.AlertDialog
import android.content.Context
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.card.MaterialCardView
import com.prod.evergreen.R

class ReasonDialog(private val context: Context,val id:Int) {

    interface ReasonDialogListener {
        fun onreason(feedback: String,id:Int)
    }

    private var listener: ReasonDialogListener? = null

    fun setListener(listener: ReasonDialogListener) {
        this.listener = listener
    }

    fun show() {
        val dialogBuilder = AlertDialog.Builder(context)
        val dialogView = View.inflate(context, R.layout.set_hold_reason_dialog, null)
        dialogBuilder.setView(dialogView)

        val fileName: TextView = dialogView.findViewById(R.id.choose_tv)
        val attachFile: TextView = dialogView.findViewById(R.id.hide_icon1)
        val spinner: Spinner = dialogView.findViewById(R.id.sp_reason)
        val switch = dialogView.findViewById<SwitchCompat>(R.id.sw_req)
        val feedbackInput = dialogView.findViewById<EditText>(R.id.feedback_input)
        val submitButton = dialogView.findViewById<MaterialCardView>(R.id.submit_button)



        if (switch.isChecked) {
            feedbackInput.visibility = View.GONE
        } else {
            feedbackInput.visibility = View.VISIBLE
        }
        val alertDialog = dialogBuilder.create()
        attachFile.setOnClickListener {


        }
        submitButton.setOnClickListener {
            var selectedItem:String?=""
            if (spinner.selectedItemPosition == 0) {
                Toast.makeText(context, "Please choose one", Toast.LENGTH_SHORT).show()
            } else if (spinner.selectedItemPosition == 1) {
                selectedItem="spare_required"
            } else {
                selectedItem="expert_service_required"
            }
            // Handle valid selection




            val feedback = feedbackInput.text.toString().trim()
            if (feedback.isNotEmpty()) {
                listener?.onreason(feedback,id)
                alertDialog.dismiss()
            } else {
                feedbackInput.error = "Reason cannot be empty"
            }
        }

        alertDialog.show()
        val window = alertDialog.window
        window?.setLayout((context.resources.displayMetrics.widthPixels * 0.9).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
    }


}

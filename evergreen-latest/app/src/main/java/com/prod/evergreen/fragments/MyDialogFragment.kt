package com.prod.evergreen.fragments

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.prod.evergreen.R

class MyDialogFragment : DialogFragment() {

    // Define a listener interface to handle button click and pass data from EditText
    interface MyDialogListener {
        fun onDialogPositiveClick(text: String)
    }

    private lateinit var listener: MyDialogListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.fragment_my_dialog, null)

            builder.setView(view)
                .setTitle("Enter Text")
                .setPositiveButton("OK") { dialog, id ->
                    // Send the positive button event back to the host activity
                    val editText = view.findViewById<EditText>(R.id.editText)
                    val enteredText = editText.text.toString()
                    listener.onDialogPositiveClick(enteredText)
                }
                .setNegativeButton("Cancel") { dialog, id ->
                    // Cancel button clicked, dismiss the dialog
                    dialog.dismiss()
                }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    // Attach the listener to the fragment
    fun setListener(listener: MyDialogListener) {
        this.listener = listener
    }
}

package com.prod.evergreen.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment


import com.prod.evergreen.databinding.DialogOtpverificationLayoutBinding


class CustomDialogFragment(val otp: String, val taskLink: Int) : DialogFragment() {

    interface CustomDialogListener {
        fun onDialogPositiveClick(option: Int)
        fun onDialogNegativeClick()
    }

    private var listener: CustomDialogListener? = null
    private lateinit var binding: DialogOtpverificationLayoutBinding  // Replace with your actual binding class

    fun setCustomDialogListener(listener: CustomDialogListener) {
        this.listener = listener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DialogOtpverificationLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonCancel.setOnClickListener {
            listener?.onDialogNegativeClick()
            dismiss()
        }

        binding.buttonOk.setOnClickListener {
            if (binding.editOption.text.toString().isNotEmpty()){
            if (binding.editOption.text.toString() == otp) {

                listener?.onDialogPositiveClick(taskLink)
                dismiss()
            }
            else{
                Toast.makeText(requireActivity(),"Enter Correct OTP",Toast.LENGTH_SHORT).show()
            }
            }
            else{
                Toast.makeText(requireActivity(),"Enter OTP",Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        return dialog
    }
    override fun onStart() {
        super.onStart()

        // Adjust width of the dialog window (80% of screen width for example)
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            dialog.window?.setLayout(width, height)

            val displayMetrics = resources.displayMetrics
            val dialogWidth = (displayMetrics.widthPixels * 0.8).toInt()  // 80% of screen width

            dialog.window?.setLayout(dialogWidth, height)
        }
    }

}

package com.prod.evergreen.helper.customdialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.view.View
import android.widget.ImageView

import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.RawRes
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieAnimationView

import com.prod.evergreen.R
import com.prod.evergreen.helper.customdialog.listener.OnDialogButtonClickListener


class CreateDialog
/**
 * Private constructor of create dialog class
 * @param context is required for some use cases
 * @param style is required to create the dialog
 * @param dialog is required to modify it
 */ private constructor(
    private val context: Context,
    private val style: Styles,
    private val dialog: Dialog
) {
    private var heading: String? = null
    private var description: String? = null
    private var positiveButtonText: String? = null
    private var negativeButtonText: String? = null
    private var dismissButtonText: String? = null
    private var lottieFile: String? = null
    private var cancelable = true

    @ColorInt
    private var tint: Int? = null
    private var lottieRepeatCount: Int? = null
    private var lottieAnimationSpeed: Float? = null
    private var progressDialogTimeout: Long? = null

    @RawRes
    private var lottieRaw: Int? = null

    @ColorRes
    private var positiveButtonTextColor: Int? = null

    @ColorRes
    private var negativeButtonTextColor: Int? = null

    @ColorRes
    private var dismissButtonTextColor: Int? = null

    @ColorRes
    private var headingTextColor: Int? = null

    @ColorRes
    private var descriptionTextColor: Int? = null

    @ColorRes
    private var iconTint: Int? = null

    @DrawableRes
    private var icon: Int? = null

    @DrawableRes
    private var dialogBackground: Int? = null

    @DrawableRes
    private var positiveButtonBackground: Int? = null

    @DrawableRes
    private var negativeButtonBackground: Int? = null

    @DrawableRes
    private var dismissButtonBackground: Int? = null

    /**
     * Heading will be shown as dialog heading
     * @param heading is not required. The dialog heading will be blank if the heading become null
     * @return instance of create dialog class
     */
    fun setHeading(heading: String?): CreateDialog? {
        this.heading = heading
        return instance
    }

    /**
     * Description will be shown as dialog description
     * @param description is not required. The dialog description will be blank if the description become null
     * @return instance of create dialog class
     */
    fun setDescription(description: String?): CreateDialog? {
        this.description = description
        return instance
    }

    /**
     * This String will be shown as positive button text
     * @param positiveButtonText is not required. If it become null then the default value "Submit" will be shown as positive button text
     * @return instance of create dialog class
     */
    fun setPositiveButtonText(positiveButtonText: String?): CreateDialog? {
        this.positiveButtonText = positiveButtonText
        return instance
    }

    /**
     * This String will be shown as negative button text
     * @param negativeButtonText is not required. If it become null then the default value "Cancel" will be shown as negative button text
     * @return instance of create dialog class
     */
    fun setNegativeButtonText(negativeButtonText: String?): CreateDialog? {
        this.negativeButtonText = negativeButtonText
        return instance
    }

    /**
     * This String will be shown as dismiss button text
     * @param dismissButtonText is not required. If it become null then the default value "Dismiss" will be shown as dismiss button text
     * @return instance of create dialog class
     */
    fun setDismissButtonText(dismissButtonText: String?): CreateDialog? {
        this.dismissButtonText = dismissButtonText
        return instance
    }

    /**
     * This color will be shown as positive button text color
     * @param color is not required. If it become null then the default color "#FFFFFF" will be shown as positive button text color
     * @return instance of create dialog class
     */
    fun setPositiveButtonTextColor(@ColorRes color: Int): CreateDialog? {
        positiveButtonTextColor = color
        return instance
    }

    /**
     * This color will be shown as negative button text color
     * @param color is not required. If it become null then the default color "#FFFFFF" will be shown as negative button text color
     * @return instance of create dialog class
     */
    fun setNegativeButtonTextColor(@ColorRes color: Int): CreateDialog? {
        negativeButtonTextColor = color
        return instance
    }

    /**
     * This color will be shown as dismiss button text color
     * @param color is not required. If it become null then the default color "#202020" will be shown as dismiss button text color
     * @return instance of create dialog class
     */
    fun setDismissButtonTextColor(@ColorRes color: Int): CreateDialog? {
        dismissButtonTextColor = color
        return instance
    }

    /**
     * This background will be shown as positive button background
     * @param background is not required. If it become null then the default background "bg_blue_10" will be shown as positive button background
     * @return instance of create dialog class
     */
    fun setPositiveButtonBackground(@DrawableRes background: Int): CreateDialog? {
        positiveButtonBackground = background
        return instance
    }

    /**
     * This background will be shown as negative button background
     * @param background is not required. If it become null then the default background "bg_light_grey_10" will be shown as negative button background
     * @return instance of create dialog class
     */
    fun setNegativeButtonBackground(@DrawableRes background: Int): CreateDialog? {
        negativeButtonBackground = background
        return instance
    }

    /**
     * This background will be shown as dismiss button background
     * @param background is not required. If it become null then the default background "bg_dark_grey_10" will be shown as dismiss button background
     * @return instance of create dialog class
     */
    fun setDismissButtonBackground(@DrawableRes background: Int): CreateDialog? {
        dismissButtonBackground = background
        return instance
    }

    /**
     * This icon will be shown as standard dialog icon
     * @param icon is required. If it become null then the default icon "ic_home" will be shown as standard dialog icon
     * @return instance of create dialog class
     */
    fun setPopupDialogIcon(@DrawableRes icon: Int): CreateDialog? {
        this.icon = icon
        return instance
    }

    /**
     * This icon tint will be shown as standard dialog icon color
     * @param iconTint is required. If it become null then the default color "#000000" will be shown as standard dialog icon color
     * @return instance of create dialog class
     */
    fun setPopupDialogIconTint(@ColorRes iconTint: Int): CreateDialog? {
        this.iconTint = iconTint
        return instance
    }

    /**
     * This cancelable is a boolean defines the is is cancelable or not while touching outside
     * @param cancelable is not required. If it become null then the default value "true" will be defined as cancelable
     * @return instance of create dialog class
     */
    fun setCancelable(cancelable: Boolean): CreateDialog? {
        this.cancelable = cancelable
        dialog.setCancelable(cancelable)
        return instance
    }



    /**
     * This color will be shown as heading text color
     * @param color is not required. If it become null then the default color "#202020" will be shown as heading text color
     * @return instance of create dialog class
     */
    fun setHeadingTextColor(@ColorRes color: Int): CreateDialog? {
        headingTextColor = color
        return instance
    }

    /**
     * This color will be shown as description text color
     * @param color is not required. If it become null then the default color "#202020" will be shown as description text color
     * @return instance of create dialog class
     */
    fun setDescriptionTextColor(@ColorRes color: Int): CreateDialog? {
        descriptionTextColor = color
        return instance
    }

    /**
     * This background will be shown as the dialog parent layout background
     * @param background is not required. If it become null then the default background "bg_white_10" will be shown as dialog parent layout background
     * @return instance of create dialog class
     */
    fun setDialogBackground(@DrawableRes background: Int): CreateDialog? {
        dialogBackground = background
        return instance
    }

    /**
     * This color will be shown as the progress dialog tint
     * @param tint is not required. If it become null then the default color "#215C5C" will be shown as progress dialog tint
     * @return instance of create dialog class
     */
    fun setProgressDialogTint(@ColorInt tint: Int): CreateDialog? {
        this.tint = tint
        return instance
    }

    /**
     * Dialog will be closed after the timeout
     * @param seconds is not required. If it become null then the dialog will not close automatically
     * @return instance of create dialog class
     */
    fun setTimeout(seconds: Long): CreateDialog? {
        Handler().postDelayed({ dialog.dismiss() }, seconds * 1000)
        return instance
    }

    /**
     * This asset name will be the lottie animation name in asset folder
     * @param assetName or rawRes is required. If it become null then the lottie animation progress bar will not show
     * @return instance of create dialog class
     */
    fun setLottieAssetName(assetName: String?): CreateDialog? {
        lottieFile = assetName
        return instance
    }

    /**
     * This rawRes will be the lottie animation resource in raw folder
     * @param rawRes or assetName is required. If it become null then the lottie animation progress bar will not show
     * @return instance of create dialog class
     */
    fun setLottieRawRes(@RawRes rawRes: Int): CreateDialog? {
        lottieRaw = rawRes
        return instance
    }

    /**
     * This repeatCount will define how many times will the animation become repeated
     * @param repeatCount is not required. If it become null then the lottie animation will be played only once
     * @return instance of create dialog class
     */
    fun setLottieRepeatCount(repeatCount: Int): CreateDialog? {
        lottieRepeatCount = repeatCount
        return instance
    }

    /**
     * This speed will define how much speed of the animation animate will be
     * @param speed is not required. If it become null then the lottie animation will animate with it's default speed
     * @return instance of create dialog class
     */
    fun setLottieAnimationSpeed(speed: Float): CreateDialog? {
        lottieAnimationSpeed = speed
        return instance
    }

    /**
     * This timeout defines how much time will the dialog be visible
     * @param seconds is not required. If it become null then the progress of lottie progress dialog will not close by itself
     * @return instance of create dialog class
     */
    fun setLottieDialogTimeout(seconds: Long): CreateDialog? {
        progressDialogTimeout = seconds
        return instance
    }


    fun showDialog(listener: OnDialogButtonClickListener, boolean: Boolean=false) {
        when (style) {
            Styles.IOS -> {
                dialogStyleOne(R.layout.dialog_ios, listener,boolean)
                show()
            }

            Styles.SUCCESS -> {
                dialogStyleThree(Styles.SUCCESS, listener)
                show()
            }

            Styles.FAILED -> {
                dialogStyleThree(Styles.FAILED, listener)
                show()
            }

            Styles.ALERT -> {
                dialogStyleThree(Styles.ALERT, listener)
                show()
            }

            else -> {}
        }
    }

    /**
     * This is a private function will show the dialog it it's not showing
     */
    private fun show() {
        if (!dialog.isShowing) {
            instance = null
            dialog.show()
        }
    }





    private fun showAlertDialog(listener: OnDialogButtonClickListener) {
        val alertDialog = AlertDialog.Builder(
            context
        )
            .setTitle(heading)
            .setMessage(description)
            .setPositiveButton(
                if (positiveButtonText == null) "Submit" else positiveButtonText,
                DialogInterface.OnClickListener { dialogInterface: DialogInterface?, i: Int ->
                    instance = null
                    listener.onPositiveClicked(dialog)
                })
            .setNegativeButton(
                if (negativeButtonText == null) "Cancel" else negativeButtonText,
                DialogInterface.OnClickListener { dialogInterface: DialogInterface?, i: Int ->
                    instance = null
                    listener.onNegativeClicked(dialog)
                })
        alertDialog.setCancelable(cancelable)
        alertDialog.show()
    }

    /**
     * This a type function of popup dialogs without any kind of icon
     * @param layout is required for the dialog content view
     * @param listener is required to get the callback on positive or negative or dismiss button clicked
     */
    private fun dialogStyleOne(
        @LayoutRes layout: Int,
        listener: OnDialogButtonClickListener,
        boolean: Boolean
    ) {
        setContentView(layout)
        val heading: TextView
        val description: TextView
        val btnNegative: TextView
        val btnPositive: TextView
        val root: ConstraintLayout
        root = dialog.findViewById(R.id.root_layout)
        heading = dialog.findViewById(R.id.tv_heading)
        description = dialog.findViewById(R.id.tv_description)
        btnNegative = dialog.findViewById(R.id.btn_negative)
        btnPositive = dialog.findViewById(R.id.btn_positive)
        val view = dialog.findViewById<View>(R.id.view)
        if(boolean){
            btnNegative.visibility=View.GONE
            view.visibility=View.GONE
        }
        if (this.heading != null) {
            heading.setText(this.heading)
        }
        if (this.description != null) {
            description.setText(this.description)
        }
        if (dialogBackground != null) {
            root.setBackgroundResource(dialogBackground!!)
        }
        if (positiveButtonText != null) {
            btnPositive.setText(positiveButtonText)
        }
        if (negativeButtonText != null) {
            btnNegative.setText(negativeButtonText)
        }
        if (positiveButtonTextColor != null) {
            btnPositive.setTextColor(ContextCompat.getColor(context, positiveButtonTextColor!!))
        }
        if (negativeButtonTextColor != null) {
            btnNegative.setTextColor(ContextCompat.getColor(context, negativeButtonTextColor!!))
        }
        if (positiveButtonBackground != null) {
            btnPositive.setBackgroundResource(positiveButtonBackground!!)
        }
        if (negativeButtonBackground != null) {
            btnNegative.setBackgroundResource(negativeButtonBackground!!)
        }
        if (headingTextColor != null) {
            heading.setTextColor(ContextCompat.getColor(context, headingTextColor!!))
        }
        if (descriptionTextColor != null) {
            description.setTextColor(ContextCompat.getColor(context, descriptionTextColor!!))
        }
        btnPositive.setOnClickListener(View.OnClickListener { view: View? ->
            listener.onPositiveClicked(
                dialog
            )
        })
        btnNegative.setOnClickListener(View.OnClickListener { view: View? ->
            listener.onNegativeClicked(
                dialog
            )
        })
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    /**
     * This a type function of popup dialogs with any kind of icon
     * @param layout is required for the dialog content view
     * @param listener is required to get the callback on positive or negative or dismiss button clicked
     */
    private fun dialogStyleTwo(@LayoutRes layout: Int, listener: OnDialogButtonClickListener) {
        dialogStyleOne(layout, listener,false)
        val icon = dialog.findViewById<ImageView>(R.id.iv_icon)
        if (this.icon != null) {
            icon.setImageResource(this.icon!!)
        }
        if (iconTint != null) {
            icon.setColorFilter(ContextCompat.getColor(context, iconTint!!), PorterDuff.Mode.SRC_IN)
        }
    }

    /**
     * This a type function of popup dialogs with lottie animation icon
     * @param style is to create the dialog
     * @param listener is required to get the callback on positive or negative or dismiss button clicked
     */
    private fun dialogStyleThree(style: Styles, listener: OnDialogButtonClickListener) {
        setContentView(R.layout.dialog_success_failed_alert)
        val icon: LottieAnimationView = dialog.findViewById<LottieAnimationView>(R.id.lottie_icon)
        val root: ConstraintLayout = dialog.findViewById<ConstraintLayout>(R.id.root_layout)
        val heading: TextView
        val description: TextView
        val btnDismiss: TextView
        heading = dialog.findViewById<TextView>(R.id.tv_heading)
        description = dialog.findViewById<TextView>(R.id.tv_description)
        btnDismiss = dialog.findViewById<TextView>(R.id.btn_dismiss)
        btnDismiss.setOnClickListener(View.OnClickListener { view: View? ->
            listener.onDismissClicked(
                dialog
            )
        })
        if (this.heading != null) {
            heading.setText(this.heading)
        }
        if (this.description != null) {
            description.setText(this.description)
        }
        if (dialogBackground != null) {
            root.setBackgroundResource(dialogBackground!!)
        }
        if (dismissButtonText != null) {
            btnDismiss.setText(dismissButtonText)
        }
        if (dismissButtonTextColor != null) {
            btnDismiss.setTextColor(ContextCompat.getColor(context, dismissButtonTextColor!!))
        }
        if (headingTextColor != null) {
            heading.setTextColor(ContextCompat.getColor(context, headingTextColor!!))
        }
        if (descriptionTextColor != null) {
            description.setTextColor(ContextCompat.getColor(context, descriptionTextColor!!))
        }
        /**
         * Added dialogStyleThree Options
         */
        if (lottieRepeatCount != null) {
            icon.setRepeatCount(lottieRepeatCount!!)
        }
        when (style) {
            Styles.SUCCESS -> {
                icon.setAnimation(R.raw.success)
               // btnDismiss.setBackgroundResource(R.drawable.ripple_bg_dark_grey_10)
            }

            Styles.FAILED -> {
                icon.setAnimation(R.raw.failed)
                btnDismiss.setBackgroundResource(R.drawable.ripple_bg_red_10)
            }

            Styles.ALERT -> {
                icon.setAnimation(R.raw.warning)
                if (dismissButtonTextColor == null) btnDismiss.setTextColor(
                    ContextCompat.getColor(
                        context, R.color.gray1
                    )
                )
                btnDismiss.setBackgroundResource(R.drawable.ripple_bg_yellow_10)
            }

            else -> {}
        }
        if (dismissButtonBackground != null) {
            btnDismiss.setBackgroundResource(dismissButtonBackground!!)
        }
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    /**
     * This function will set the layout content to the dialog
     * @param layout is required to set the content view in dialog
     */
    private fun setContentView(@LayoutRes layout: Int) {
        dialog.setContentView(layout)
    }

    companion object {
        /**
         * Create Dialog class.
         * Created by Saad Ahmed on 17-May-2022.
         * A class which creates many kind of dialogs which you can modify easily.
         */
        @SuppressLint("StaticFieldLeak")
        private var instance: CreateDialog? = null

        /**
         * Static function to get instance of create dialog class
         * @param context is required to create instance of create dialog class
         * @param style is required to create the dialog
         * @param dialog is required to modify it later
         * @return instance of create dialog class
         */
        fun getInstance(context: Context, style: Styles, dialog: Dialog): CreateDialog? {
            if (instance == null) {
                instance = CreateDialog(context, style, dialog)
            }
            return instance
        }
    }
}
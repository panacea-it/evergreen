package com.prod.evergreen.dialogs

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.github.gcacace.signaturepad.views.SignaturePad
import com.prod.evergreen.R
import com.prod.evergreen.XApplication
import com.prod.evergreen.api.MainRepository
import com.prod.evergreen.api.MainViewModel
import com.prod.evergreen.api.MyViewModelFactory
import com.prod.evergreen.api.RetrofitService
import com.prod.evergreen.databinding.FragmentBlankBinding
import com.prod.evergreen.fragments.CustomDialogFragment.CustomDialogListener
import com.prod.evergreen.helper.ConstantValues
import com.prod.evergreen.helper.ProgressDialogUtil
import com.prod.evergreen.helper.SharedPreferencesHelper
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [BlankFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BlankFragment : DialogFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var listener: SignatureDialogListener? = null
    interface SignatureDialogListener {
        fun onSignatureUploaded(signatureUrl: String)
    }
    fun setCustomDialogListener(listener: SignatureDialogListener) {
        this.listener = listener
    }
    private var param2: String? = null
    private lateinit var currentPhotoPath: String
    private lateinit var viewModel: MainViewModel
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    lateinit var mViewBinding:FragmentBlankBinding

    private var token: String? = null
    private var accesstype: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mViewBinding=FragmentBlankBinding.inflate(layoutInflater,container, false)
        // Inflate the layout for this fragment
        sharedPreferencesHelper= SharedPreferencesHelper(requireActivity())

        return mViewBinding.root
    }

    companion object {

        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            BlankFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setClickListener()
        setViewmodel()
        mViewBinding.close.setOnClickListener {
            dismiss()
        }
        token=sharedPreferencesHelper.getValueString(ConstantValues.AuthToken)
        accesstype=sharedPreferencesHelper.getValueString(ConstantValues.TYPE_ROLE)
        viewModel.loading.observe(this) { data ->
            if (data){
                ProgressDialogUtil.showProgressDialog(requireActivity(),"Loading")
            }
            else{
                ProgressDialogUtil.hideProgressDialog()
            }
        }
        viewModel.imageUploadDataResponse.observe(viewLifecycleOwner) { data ->
            if (data.status_code==200) {
                listener?.onSignatureUploaded(data.image_url!!)
                dismiss()
            }
            else{
                Toast.makeText(requireActivity(), data.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initView() {

        mViewBinding.signaturePad.setOnSignedListener(object : SignaturePad.OnSignedListener {
            override fun onStartSigning() {

            }

            override fun onSigned() {
                //Event triggered when the pad is signed

            }

            override fun onClear() {

            }
        })
    }

    private fun setClickListener() {
        mViewBinding.btnComplete.setOnClickListener {


       //    val file=bitmapToFile(mViewBinding.signaturePad.transparentSignatureBitmap,"signature")

            val file = bitmapToFileExternal(requireActivity(), mViewBinding.signaturePad.signatureBitmap, "signature.png")
            //Set the captured signature in Imageview
            val part= prepareFilePart(file!!)
          //  mViewBinding.ivSignature.setImageBitmap(mViewBinding.signaturePad.transparentSignatureBitmap)
          viewModel.upLoadImage(part,token!!,"Client-Signature")


        }
        mViewBinding.btnClear.setOnClickListener {
            //Clear captured signature
            mViewBinding.signaturePad.clear()
        }
    }

//    fun getFileUri(context: Context, file: File): Uri {
//        return FileProvider.getUriForFile(
//            context,
//            "${context.packageName}.fileprovider",
//            file
//        )
//    }
fun prepareFilePart(file: File): MultipartBody.Part {
    // Determine the MIME type based on the file extension
    val mimeType = if (file.extension == "jpg") "image/jpeg" else "image/png"
    val requestBody = RequestBody.create(mimeType.toMediaType(), file)
    return MultipartBody.Part.createFormData("file", file.name, requestBody)
}
    private fun setViewmodel() {
        val repository = MainRepository(
            RetrofitService.getInstance(requireActivity()),
            XApplication.database.newsDao(),
            XApplication.database.companyDao())
        val viewModelFactory = MyViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]
    }

    fun bitmapToFileExternal(context: Context, bitmap: Bitmap, fileName: String): File? {
        val file = File(context.filesDir, fileName)
        return try {
            FileOutputStream(file).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                fos.flush()
            }
            file
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}
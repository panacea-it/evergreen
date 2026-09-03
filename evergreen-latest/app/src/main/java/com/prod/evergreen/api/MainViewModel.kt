package com.prod.evergreen.api

import android.util.Log
import androidx.lifecycle.*
import com.google.gson.JsonObject
import com.prod.evergreen.activities.AddEquipment
import com.prod.evergreen.db.Movie
import com.prod.evergreen.models.AllAmcData
import com.prod.evergreen.models.AllEquipmentsData
import com.prod.evergreen.models.AllTaskCountResponse
import com.prod.evergreen.models.AllTasks
import com.prod.evergreen.models.AllUsers
import com.prod.evergreen.models.ChangePasswordData
import com.prod.evergreen.models.CompaniesStatsResponse
import com.prod.evergreen.models.Company
import com.prod.evergreen.models.EquipmentInfo
import com.prod.evergreen.models.ForgotPasswordData
import com.prod.evergreen.models.LoginData
import com.prod.evergreen.models.NotificationsListResponse
import com.prod.evergreen.models.ServiceReportResponse
import com.prod.evergreen.models.ServiceReportsResponse
import com.prod.evergreen.models.UserStatsResponse
import com.prod.evergreen.models.VerifyData
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody


class MainViewModel(private val mainRepository: MainRepository) : ViewModel() {

    val allTasksDataResponse :MutableLiveData<AllTasks> = MutableLiveData()
    val allUsersDataResponse :MutableLiveData<AllUsers> = MutableLiveData()
    val allequipmentsDataResponse :MutableLiveData<AllEquipmentsData> = MutableLiveData()
    val companyEquipmentsDataResponse :MutableLiveData<AllEquipmentsData> = MutableLiveData()
    val allAmcDataResponse :MutableLiveData<AllAmcData> = MutableLiveData()
    val userloginresponse :MutableLiveData<LoginData> = MutableLiveData()
    val forgotPasswordResponse :MutableLiveData<ForgotPasswordData> = MutableLiveData()
    val verifyOtpResponse :MutableLiveData<VerifyData> = MutableLiveData()
    val changePasswordDataResponse :MutableLiveData<ChangePasswordData> = MutableLiveData()
    val upDateTaskStatusDataResponse :MutableLiveData<ChangePasswordData> = MutableLiveData()
    val equipmentDataResponse :MutableLiveData<EquipmentInfo> = MutableLiveData()
    val assignTechnicianDataResponse :MutableLiveData<ChangePasswordData> = MutableLiveData()
    val taskUpdateFeedbackDataResponse :MutableLiveData<ChangePasswordData> = MutableLiveData()
    val genaratepdffile :MutableLiveData<ChangePasswordData> = MutableLiveData()
    val downloadpdf :MutableLiveData<ChangePasswordData> = MutableLiveData()
    val serviceReportsResponse :MutableLiveData<ServiceReportsResponse> = MutableLiveData()
    val serviceReportDetailsResponse :MutableLiveData<ServiceReportResponse> = MutableLiveData()
    val serviceReportSaveResponse :MutableLiveData<ServiceReportResponse> = MutableLiveData()
    val serviceReportPrefillResponse :MutableLiveData<ServiceReportResponse> = MutableLiveData()
    val downloadQrDataResponse :MutableLiveData<ResponseBody> = MutableLiveData()
    val notificationsListResponse :MutableLiveData<NotificationsListResponse> = MutableLiveData()
    val createTaskDataResponse :MutableLiveData<ChangePasswordData> = MutableLiveData()
    val imageUploadDataResponse :MutableLiveData<ChangePasswordData> = MutableLiveData()


    private val _companiesStatsResponse = MutableLiveData<NetworkState<CompaniesStatsResponse>>()
    val companiesStatsResponse: LiveData<NetworkState<CompaniesStatsResponse>> = _companiesStatsResponse

    private val _userStatsResponse = MutableLiveData<NetworkState<UserStatsResponse>>()
    val userStatsResponse: LiveData<NetworkState<UserStatsResponse>> = _userStatsResponse

    private val _getAllTaskCountResponse = MutableLiveData<NetworkState<AllTaskCountResponse>>()
    val getAllTaskCountResponse: LiveData<NetworkState<AllTaskCountResponse>> = _getAllTaskCountResponse




    val allNews: LiveData<List<Movie>> = mainRepository.allNews
    val allCompanies: LiveData<List<Company>> = mainRepository.allUserCompanies

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String>
        get() = _errorMessage

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("MainViewModel", "coroutine failed", throwable)
        val detail = throwable.localizedMessage
            ?: throwable.message
            ?: throwable.javaClass.simpleName
        onError(
            if (detail.isBlank() || detail == "null") {
                "Unable to complete request. Please try again."
            } else {
                detail
            }
        )
    }
     val loading = MutableLiveData<Boolean>()

    private fun handleApiError(response: NetworkState.Error<*>) {
        loading.value = false
        val code = response.statusCode
        val message = when {
            code != null && code >= 500 -> "Internal Server Error: Please try again later."
            code == 404 -> "Some thing went wrong please try again"
            !response.message.isNullOrBlank() && response.message != "null" -> response.message
            else -> "Unable to connect. Please check your internet and try again."
        }
        onError(message)
    }

    fun getAllMovies() {
        loading.value = true
        viewModelScope.launch(exceptionHandler) {
            Log.d("Thread Inside", Thread.currentThread().name)
            when (val response = mainRepository.getAllMovies()) {
                is NetworkState.Success -> {
                    response.data.products.let { products ->
                        // Insert movies into database
                        mainRepository.saveProducts(products)
                    }
                    loading.value = false
                }
                is NetworkState.Error -> {
                    Log.d("errors",response.message.toString())
                    loading.value = false
                    if ((response.statusCode ?: 0) >= 500) {
                        onError("Internal Server Error: Please try again later.")
                    }
                    else if (response.statusCode == 404) {
                        onError("Some thing went wrong please try again")
                    }

                    else {
                        onError(response.message ?: "Unknown error")
                    }

                }
            }
        }
    }
      fun getAllAmc(authorization: String) {
        loading.value = true
        viewModelScope.launch(exceptionHandler) {
            Log.d("Thread Inside", Thread.currentThread().name)
            when (val response = mainRepository.getAllAmc(authorization)) {
                is NetworkState.Success -> {
if (response.data.status==200) {
    allAmcDataResponse.postValue(response.data)
}
                    loading.value = false
                }
                is NetworkState.Error -> {
                    Log.d("errors",response.message.toString())
                    loading.value = false
                    if ((response.statusCode ?: 0) >= 500) {
                        onError("Internal Server Error: Please try again later.")
                    }
                    else if (response.statusCode == 404) {
                        onError("Some thing went wrong please try again")
                    }

                    else {
                        onError(response.message ?: "Unknown error")
                    }

                }
            }
        }
    }

    fun getAllEquipments(authorization: String) {
        loading.value = true
        viewModelScope.launch(exceptionHandler) {
            Log.d("Thread Inside", Thread.currentThread().name)
            when (val response = mainRepository.getAllEquipments(authorization)) {
                is NetworkState.Success -> {
                    allequipmentsDataResponse.postValue(response.data)
                    loading.value = false
                }
                is NetworkState.Error -> {
                    Log.d("errors",response.message.toString())
                    loading.value = false
                    if ((response.statusCode ?: 0) >= 500) {
                        onError("Internal Server Error: Please try again later.")
                    }
                    else if (response.statusCode == 404) {
                        onError("Some thing went wrong please try again")
                    }

                    else {
                        onError(response.message ?: "Unknown error")
                    }

                }
            }
        }
    }



    fun getAllEquipmentsByID(authorization: String,c_id:JsonObject) {
        loading.value = true
        viewModelScope.launch(exceptionHandler) {
            when (val response = mainRepository.getAllEquipmentsByID(authorization,c_id)) {
                is NetworkState.Success -> {
                    companyEquipmentsDataResponse.postValue(response.data)
                    loading.value = false
                }
                is NetworkState.Error -> {
                    Log.d("errors",response.message.toString())
                    loading.value = false
                    if ((response.statusCode ?: 0) >= 500) {
                        onError("Internal Server Error: Please try again later.")
                    }
                    else if (response.statusCode == 404) {
                        onError("Some thing went wrong please try again")
                    }

                    else {
                        onError(response.message ?: "Unknown error")
                    }

                }
            }
        }
    }




    fun getAllUsers(authorization: String,access_lavel:JsonObject) {
        loading.value = true
        viewModelScope.launch(exceptionHandler) {
            when (val response = mainRepository.getAllUsers(authorization,access_lavel)) {
                is NetworkState.Success -> {
                    allUsersDataResponse.postValue(response.data)
                    loading.value = false
                }
               is NetworkState.Error -> {
                    Log.d("errors",response.message.toString())
                    loading.value = false
                   if ((response.statusCode ?: 0) >= 500) {
                       onError("Internal Server Error: Please try again later.")
                   }
                    else if (response.statusCode == 404) {
                        onError("Some thing went wrong please try again")
                    }

                    else {
                        onError(response.message ?: "Unknown error")
                    }

                }
            }
        }
    }



    fun getAllTasks(authorization: String,access_lavel:JsonObject) {
        loading.value = true
        viewModelScope.launch(exceptionHandler) {
            when (val response = mainRepository.getAllTasks(authorization,access_lavel)) {
                is NetworkState.Success -> {
                    allTasksDataResponse.postValue(response.data)
                    loading.value = false
                }
               is NetworkState.Error -> {
                    Log.d("errors",response.message.toString())
                    loading.value = false
                   if ((response.statusCode ?: 0) >= 500) {
                       onError("Internal Server Error: Please try again later.")
                   }
                    else if (response.statusCode == 404) {
                        onError("Some thing went wrong please try again")
                    }

                    else {
                        onError(response.message ?: "Unknown error")
                    }

                }
            }
        }
    }




    fun userLogin(body:JsonObject) {
        loading.value = true
        viewModelScope.launch(exceptionHandler) {

            when (val response = mainRepository.userLogin(body)) {
                is NetworkState.Success -> {
                    userloginresponse.postValue(response.data)
                    loading.value = false
                }
                is NetworkState.Error -> {
                    Log.e("userLogin", "error code=${response.statusCode} msg=${response.message}")
                    loading.value = false
                    val code = response.statusCode
                    val apiMessage = response.message?.trim().orEmpty()
                    onError(
                        when {
                            code == 400 -> apiMessage.ifBlank { "invalid mobile number" }
                            code == 403 -> apiMessage.ifBlank { "you do not have the access" }
                            code == 402 -> apiMessage.ifBlank { "wrong password" }
                            else -> apiMessage.ifBlank { "Login failed. Please try again." }
                        }
                    )
                }
            }
        }
    }


    fun forgotPassword(body:JsonObject) {
        loading.value = true
        viewModelScope.launch(exceptionHandler) {

            when (val response = mainRepository.forgotPassword(body)) {
                is NetworkState.Success -> {
                    forgotPasswordResponse.postValue(response.data)
                    loading.value = false
                }
               is NetworkState.Error -> {
                    Log.d("errors",response.message.toString())
                    loading.value = false
                   if ((response.statusCode ?: 0) >= 500) {
                       onError("Internal Server Error: Please try again later.")
                   }
                    else if (response.statusCode == 404) {
                        onError("Some thing went wrong please try again")
                    }

                    else {
                        onError(response.message ?: "Unknown error")
                    }

                }
            }
        }
    }


    fun verifyOtp(body:JsonObject) {
        loading.value = true
        viewModelScope.launch(exceptionHandler) {

            when (val response = mainRepository.verifyOtp(body)) {
                is NetworkState.Success -> {
                    verifyOtpResponse.postValue(response.data)
                    loading.value = false
                }
               is NetworkState.Error -> {
                    Log.d("errors",response.message.toString())
                    loading.value = false
                   if ((response.statusCode ?: 0) >= 500) {
                       onError("Internal Server Error: Please try again later.")
                   }
                    else if (response.statusCode == 404) {
                        onError("Some thing went wrong please try again")
                    }

                    else {
                        onError(response.message ?: "Unknown error")
                    }

                }
            }
        }
    }

    fun updatePassword(body:JsonObject) {
        loading.value = true
        viewModelScope.launch(exceptionHandler) {

            when (val response = mainRepository.updatePassword(body)) {
                is NetworkState.Success -> {
                    changePasswordDataResponse.postValue(response.data)
                    loading.value = false
                }
               is NetworkState.Error -> {
                    Log.d("errors",response.message.toString())
                    loading.value = false
                   if ((response.statusCode ?: 0) >= 500) {
                       onError("Internal Server Error: Please try again later.")
                   }
                    else if (response.statusCode == 404) {
                        onError("Some thing went wrong please try again")
                    }

                    else {
                        onError(response.message ?: "Unknown error")
                    }

                }
            }
        }
    }


    fun createTechnician(body:JsonObject,authenticator: String) {
        loading.value = true
        viewModelScope.launch(exceptionHandler) {

            when (val response = mainRepository.createTechnician(body,authenticator)) {
                is NetworkState.Success -> {
                    changePasswordDataResponse.postValue(response.data)
                    loading.value = false
                }
               is NetworkState.Error -> {
                    Log.d("errors",response.message.toString())
                    loading.value = false
                   if ((response.statusCode ?: 0) >= 500) {
                       onError("Internal Server Error: Please try again later.")
                   }
                    else if (response.statusCode == 404) {
                        onError("Some thing went wrong please try again")
                    }

                    else {
                        onError(response.message ?: "Unknown error")
                    }

                }
            }
        }
    }


    fun createTask(body:JsonObject,authenticator: String) {
        loading.value = true
        viewModelScope.launch(exceptionHandler) {

            when (val response = mainRepository.createTask(body,authenticator)) {
                is NetworkState.Success -> {
                    createTaskDataResponse.postValue(response.data)
                    loading.value = false
                }
               is NetworkState.Error -> {
                    Log.d("errors",response.message.toString())
                    loading.value = false
                   if ((response.statusCode ?: 0) >= 500) {
                       onError("Internal Server Error: Please try again later.")
                   }
                    else if (response.statusCode == 404) {
                        onError("Some thing went wrong please try again")
                    }

                    else {
                        onError(response.message ?: "Unknown error")
                    }

                }
            }
        }
    }


    fun createAMC(body:JsonObject,authenticator: String) {
        loading.value = true
        viewModelScope.launch(exceptionHandler) {

            when (val response = mainRepository.createAMC(body,authenticator)) {
                is NetworkState.Success -> {
                    changePasswordDataResponse.postValue(response.data)
                    loading.value = false
                }
               is NetworkState.Error -> {
                    Log.d("errors",response.message.toString())
                    loading.value = false
                   if ((response.statusCode ?: 0) >= 500) {
                       onError("Internal Server Error: Please try again later.")
                   }
                    else if (response.statusCode == 404) {
                        onError("Some thing went wrong please try again")
                    }

                    else {
                        onError(response.message ?: "Unknown error")
                    }

                }
            }
        }
    }

    fun updateAMC(companyId: Int, body: JsonObject, authenticator: String) {
        loading.value = true
        viewModelScope.launch(exceptionHandler) {
            when (val response = mainRepository.updateAMC(companyId, body, authenticator)) {
                is NetworkState.Success -> {
                    changePasswordDataResponse.postValue(response.data)
                    loading.value = false
                }
                is NetworkState.Error -> {
                    Log.d("errors", response.message.toString())
                    loading.value = false
                    if ((response.statusCode ?: 0) >= 500) {
                        onError("Internal Server Error: Please try again later.")
                    } else if (response.statusCode == 404) {
                        onError("Some thing went wrong please try again")
                    } else {
                        onError(response.message ?: "Unknown error")
                    }
                }
            }
        }
    }

    fun deleteAMC(companyId: Int, authenticator: String, activate: Boolean = false) {
        loading.value = true
        viewModelScope.launch(exceptionHandler) {
            val body = JsonObject().apply {
                addProperty("company_link", companyId)
                addProperty("action", if (activate) "activate" else "delete")
            }
            when (val response = mainRepository.deleteAMC(body, authenticator)) {
                is NetworkState.Success -> {
                    val payload = response.data
                    if (payload.status_code == 200) {
                        changePasswordDataResponse.postValue(payload)
                    } else {
                        onError(mapDeleteCompanyError(payload.message))
                    }
                    loading.value = false
                }
                is NetworkState.Error -> {
                    Log.d("errors", response.message.toString())
                    loading.value = false
                    if ((response.statusCode ?: 0) >= 500) {
                        onError("Internal Server Error: Please try again later.")
                    } else if (response.statusCode == 404) {
                        onError("Delete company endpoint not found. Please update backend.")
                    } else {
                        onError(mapDeleteCompanyError(response.message))
                    }
                }
            }
        }
    }

    private fun mapDeleteCompanyError(message: String?): String {
        val raw = message?.trim().orEmpty()
        if (raw.contains("user already exists", ignoreCase = true)) {
            return "Delete failed because backend is outdated. Please redeploy latest backend and try again."
        }
        if (raw.contains("PLEASE ENTER MOBILE NUMBER", ignoreCase = true)) {
            return "Delete failed because backend treated this as company create. Please redeploy latest backend."
        }
        return raw.ifBlank { "Unable to delete company" }
    }



    fun deleteEquipment(body: JsonObject, authenticator: String) {
        loading.value = true
        viewModelScope.launch(exceptionHandler) {
            when (val response = mainRepository.deleteEquipment(body, authenticator)) {
                is NetworkState.Success -> {
                    changePasswordDataResponse.postValue(response.data)
                    loading.value = false
                }
                is NetworkState.Error -> {
                    loading.value = false
                    onError(response.message ?: "Unable to update equipment status")
                }
            }
        }
    }

    fun updateTask(body: JsonObject, authenticator: String) {
        loading.value = true
        viewModelScope.launch(exceptionHandler) {
            when (val response = mainRepository.updateTask(body, authenticator)) {
                is NetworkState.Success -> {
                    changePasswordDataResponse.postValue(response.data)
                    loading.value = false
                }
                is NetworkState.Error -> {
                    loading.value = false
                    onError(response.message ?: "Unable to update task")
                }
            }
        }
    }

    fun deleteTask(body: JsonObject, authenticator: String) {
        loading.value = true
        viewModelScope.launch(exceptionHandler) {
            when (val response = mainRepository.deleteTask(body, authenticator)) {
                is NetworkState.Success -> {
                    changePasswordDataResponse.postValue(response.data)
                    loading.value = false
                }
                is NetworkState.Error -> {
                    loading.value = false
                    onError(response.message ?: "Unable to delete task")
                }
            }
        }
    }

    fun updateUser(userId: Int, body: JsonObject, authenticator: String) {
        loading.value = true
        viewModelScope.launch(exceptionHandler) {
            when (val response = mainRepository.updateUser(userId, body, authenticator)) {
                is NetworkState.Success -> {
                    changePasswordDataResponse.postValue(response.data)
                    loading.value = false
                }
                is NetworkState.Error -> {
                    loading.value = false
                    onError(response.message ?: "Unable to update user")
                }
            }
        }
    }

    fun deleteUser(body: JsonObject, authenticator: String) {
        loading.value = true
        viewModelScope.launch(exceptionHandler) {
            when (val response = mainRepository.deleteUser(body, authenticator)) {
                is NetworkState.Success -> {
                    changePasswordDataResponse.postValue(response.data)
                    loading.value = false
                }
                is NetworkState.Error -> {
                    loading.value = false
                    onError(response.message ?: "Unable to delete user")
                }
            }
        }
    }

    fun updateEquipment(body: AddEquipment.EquipmentUpdate, authenticator: String) {
        loading.value = true
        viewModelScope.launch(exceptionHandler) {

            when (val response = mainRepository.updateEquipment(body,authenticator)) {
                is NetworkState.Success -> {
                    changePasswordDataResponse.postValue(response.data)
                    loading.value = false
                }
               is NetworkState.Error -> {
                    Log.d("errors",response.message.toString())
                    loading.value = false
                   if ((response.statusCode ?: 0) >= 500) {
                       onError("Internal Server Error: Please try again later.")
                   }
                    else if (response.statusCode == 404) {
                        onError("Some thing went wrong please try again")
                    }

                    else {
                        onError(response.message ?: "Unknown error")
                    }

                }
            }
        }
    }



    fun updateHoldReasons(body: JsonObject, authenticator: String) {
        loading.value = true
        viewModelScope.launch(exceptionHandler) {

            when (val response = mainRepository.updateHoldReasons(body,authenticator)) {
                is NetworkState.Success -> {
                    upDateTaskStatusDataResponse.postValue(response.data)
                    loading.value = false
                }
               is NetworkState.Error -> {
                    Log.d("errors",response.message.toString())
                    loading.value = false
                   if ((response.statusCode ?: 0) >= 500) {
                       onError("Internal Server Error: Please try again later.")
                   }
                    else if (response.statusCode == 404) {
                        onError("Some thing went wrong please try again")
                    }

                    else {
                        onError(response.message ?: "Unknown error")
                    }

                }
            }
        }
    }


    fun addEquipments(body: AddEquipment.Equipment, authenticator: String) {
        loading.value = true
        viewModelScope.launch(exceptionHandler) {

            when (val response = mainRepository.addEquipment(body,authenticator)) {
                is NetworkState.Success -> {
                    changePasswordDataResponse.postValue(response.data)
                    loading.value = false
                }
                is NetworkState.Error -> {
                    Log.d("errors",response.message.toString())
                    loading.value = false
                    if ((response.statusCode ?: 0) >= 500) {
                        onError("Internal Server Error: Please try again later.")
                    }
                    else if (response.statusCode == 404) {
                        onError("Some thing went wrong please try again")
                    }

                    else {
                        onError(response.message ?: "Unknown error")
                    }

                }
            }
        }
    }


    fun upDateTaskStatus(body: JsonObject, authenticator: String) {
        loading.value = true
        viewModelScope.launch(exceptionHandler) {

            when (val response = mainRepository.upDateTaskStatus(body,authenticator)) {
                is NetworkState.Success -> {
                    upDateTaskStatusDataResponse.postValue(response.data)
                    loading.value = false
                }
               is NetworkState.Error -> {
                    Log.d("errors",response.message.toString())
                    loading.value = false
                   if ((response.statusCode ?: 0) >= 500) {
                       onError("Internal Server Error: Please try again later.")
                   }
                    else if (response.statusCode == 404) {
                        onError("Some thing went wrong please try again")
                    }

                    else {
                        onError(response.message ?: "Unknown error")
                    }

                }
            }
        }
    }


    fun GetEquipmentInfo(body: JsonObject, authenticator: String) {
        loading.value = true
        viewModelScope.launch(exceptionHandler) {

            when (val response = mainRepository.GetEquipmentInfo(body,authenticator)) {
                is NetworkState.Success -> {
                    equipmentDataResponse.postValue(response.data)
                    loading.value = false
                    if (response.data.data == null) {
                        onError(response.data.message ?: "Equipment details were not found.")
                    }
                }
               is NetworkState.Error -> {
                    Log.d("errors",response.message.toString())
                    loading.value = false
                   if ((response.statusCode ?: 0) >= 500) {
                       onError("Internal Server Error: Please try again later.")
                   }
                    else if (response.statusCode == 404) {
                        onError("Some thing went wrong please try again")
                    }

                    else {
                        onError(response.message ?: "Unknown error")
                    }

                }
            }
        }
    }



    fun assignTechnician(body: JsonObject, authenticator: String) {
        loading.value = true
        viewModelScope.launch(exceptionHandler) {

            when (val response = mainRepository.assignTechnician(body,authenticator)) {
                is NetworkState.Success -> {
                    assignTechnicianDataResponse.postValue(response.data)
                    loading.value = false
                }
               is NetworkState.Error -> {
                    Log.d("errors",response.message.toString())
                    loading.value = false
                   if ((response.statusCode ?: 0) >= 500) {
                       onError("Internal Server Error: Please try again later.")
                   }
                    else if (response.statusCode == 404) {
                        onError("Some thing went wrong please try again")
                    }

                    else {
                        onError(response.message ?: "Unknown error")
                    }

                }
            }
        }
    }


    fun taskUpDateFeedback(body: JsonObject, authenticator: String) {
        loading.value = true
        viewModelScope.launch(exceptionHandler) {

            when (val response = mainRepository.taskUpDateFeedback(body,authenticator)) {
                is NetworkState.Success -> {
                    taskUpdateFeedbackDataResponse.postValue(response.data)
                    loading.value = false
                }
               is NetworkState.Error -> {
                    Log.d("errors",response.message.toString())
                    loading.value = false
                   if ((response.statusCode ?: 0) >= 500) {
                       onError("Internal Server Error: Please try again later.")
                   }
                    else if (response.statusCode == 404) {
                        onError("Some thing went wrong please try again")
                    }

                    else {
                        onError(response.message ?: "Unknown error")
                    }

                }
            }
        }
    }


    fun generateServiceReport(body: JsonObject, authenticator: String) {
        loading.value = true
        viewModelScope.launch(exceptionHandler) {

            when (val response = mainRepository.generateServiceReport(body,authenticator)) {
                is NetworkState.Success -> {
                    genaratepdffile.postValue(response.data)
                    loading.value = false
                }
               is NetworkState.Error -> {
                    Log.d("errors",response.message.toString())
                    loading.value = false
                    // Report generation is best-effort after approval/close; surface a result so UI can continue.
                    genaratepdffile.postValue(
                        ChangePasswordData(
                            status_code = response.statusCode,
                            message = response.message
                        )
                    )
                }
            }
        }
    }
    fun getServiceReport(body: JsonObject, authenticator: String) {
        loading.value = true
        viewModelScope.launch(exceptionHandler) {

            when (val response = mainRepository.getServiceReport(body,authenticator)) {
                is NetworkState.Success -> {
                    downloadpdf.postValue(response.data)
                    loading.value = false
                }
               is NetworkState.Error -> {
                    Log.d("errors",response.message.toString())
                    loading.value = false
                    onError(response.message ?: "Unable to generate service report")
                }
            }
        }
    }

    fun getAllServiceReports(authenticator: String) {
        loading.value = true
        viewModelScope.launch(exceptionHandler) {
            when (val response = mainRepository.getAllServiceReports(JsonObject(), authenticator)) {
                is NetworkState.Success -> {
                    serviceReportsResponse.postValue(response.data)
                    loading.value = false
                }
                is NetworkState.Error -> {
                    loading.value = false
                    onError(response.message ?: "Unable to load service reports")
                }
            }
        }
    }

    fun getServiceReportDetails(body: JsonObject, authenticator: String) {
        loading.value = true
        viewModelScope.launch(exceptionHandler) {
            when (val response = mainRepository.getServiceReportDetails(body, authenticator)) {
                is NetworkState.Success -> {
                    serviceReportDetailsResponse.postValue(response.data)
                    loading.value = false
                }
                is NetworkState.Error -> {
                    loading.value = false
                    onError(response.message ?: "Unable to load service report")
                }
            }
        }
    }

    fun saveServiceReport(body: JsonObject, authenticator: String, isUpdate: Boolean) {
        loading.value = true
        viewModelScope.launch(exceptionHandler) {
            val result = if (isUpdate) {
                mainRepository.updateServiceReport(body, authenticator)
            } else {
                mainRepository.createServiceReport(body, authenticator)
            }
            when (result) {
                is NetworkState.Success -> {
                    serviceReportSaveResponse.postValue(result.data)
                    loading.value = false
                }
                is NetworkState.Error -> {
                    loading.value = false
                    onError(result.message ?: "Unable to save service report")
                }
            }
        }
    }

    fun deleteServiceReport(body: JsonObject, authenticator: String) {
        loading.value = true
        viewModelScope.launch(exceptionHandler) {
            when (val response = mainRepository.deleteServiceReport(body, authenticator)) {
                is NetworkState.Success -> {
                    changePasswordDataResponse.postValue(response.data)
                    loading.value = false
                }
                is NetworkState.Error -> {
                    loading.value = false
                    onError(response.message ?: "Unable to delete service report")
                }
            }
        }
    }

    fun downloadSavedServiceReport(body: JsonObject, authenticator: String) {
        loading.value = true
        viewModelScope.launch(exceptionHandler) {
            when (val response = mainRepository.downloadServiceReportPdf(body, authenticator)) {
                is NetworkState.Success -> {
                    downloadpdf.postValue(response.data)
                    loading.value = false
                }
                is NetworkState.Error -> {
                    loading.value = false
                    onError(response.message ?: "Unable to generate service report PDF")
                }
            }
        }
    }

    fun getServiceReportPrefill(body: JsonObject, authenticator: String) {
        loading.value = true
        viewModelScope.launch(exceptionHandler) {
            when (val response = mainRepository.getServiceReportPrefill(body, authenticator)) {
                is NetworkState.Success -> {
                    serviceReportPrefillResponse.postValue(response.data)
                    loading.value = false
                }
                is NetworkState.Error -> {
                    loading.value = false
                    onError(response.message ?: "Unable to load task details")
                }
            }
        }
    }


    fun downloadAllEquipment(body: JsonObject, authenticator: String) {
        loading.value = true
        viewModelScope.launch(exceptionHandler) {

            when (val response = mainRepository.downloadAllEquipment(body,authenticator)) {
                is NetworkState.Success -> {
                    downloadQrDataResponse.postValue(response.data)
                    loading.value = false
                }
               is NetworkState.Error -> {
                    Log.d("errors",response.message.toString())
                    loading.value = false
                   if ((response.statusCode ?: 0) >= 500) {
                       onError("Internal Server Error: Please try again later.")
                   }
                    else if (response.statusCode == 404) {
                        onError("Some thing went wrong please try again")
                    }

                    else {
                        onError(response.message ?: "Unknown error")
                    }

                }
            }
        }
    }






    fun getNotificationsPage(authenticator: String, page: Int, limit: Int = 20) {
        loading.value = page == 1
        viewModelScope.launch(exceptionHandler) {
            val body = JsonObject()
            body.addProperty("page", page)
            body.addProperty("limit", limit)
            when (val response = mainRepository.getNotificationsPage(authenticator, body)) {
                is NetworkState.Success -> {
                    notificationsListResponse.postValue(response.data)
                    loading.value = false
                }
                is NetworkState.Error -> {
                    loading.value = false
                    onError(response.message ?: "Unable to load notifications")
                }
            }
        }
    }

    fun getNotifications(authenticator: String) {
        loading.value = true
        viewModelScope.launch(exceptionHandler) {

            when (val response = mainRepository.getNotifications(authenticator)) {
                is NetworkState.Success -> {
                    notificationsListResponse.postValue(response.data)
                    loading.value = false
                }
               is NetworkState.Error -> {
                    Log.d("errors",response.message.toString())
                    loading.value = false
                   if ((response.statusCode ?: 0) >= 500) {
                       onError("Internal Server Error: Please try again later.")
                   }
                    else if (response.statusCode == 404) {
                        onError("Some thing went wrong please try again")
                    }

                    else {
                        onError(response.message ?: "Unknown error")
                    }

                }
            }
        }
    }






    fun getAllTaskCountAPI(authorization: String) {
        viewModelScope.launch {
            loading.value = true
            try {
                coroutineScope {

                    val getAllTskCount = async { mainRepository.getAllTskCount(authorization) }
                    _getAllTaskCountResponse.value = getAllTskCount.await()
                }
            } catch (e: Exception) {
                // Handle exception
            } finally {
                loading.value = false
            }
        }
    }

    fun fetchStats(authorization: String) {
        viewModelScope.launch {
            loading.value = true
            try {
                coroutineScope {
                    val companiesStatsDeferred = async { mainRepository.getCompaniesStats(authorization) }
                    val userStatsDeferred = async { mainRepository.getUserStats(authorization) }
                    val getAllTskCount = async { mainRepository.getAllTskCount(authorization) }

                    _companiesStatsResponse.value = companiesStatsDeferred.await()
                    _userStatsResponse.value = userStatsDeferred.await()
                    _getAllTaskCountResponse.value = getAllTskCount.await()
                }
            } catch (e: Exception) {
                // Handle exception
            } finally {
                loading.value = false
            }
        }
    }




    fun upLoadImage(body:MultipartBody.Part,authenticator: String,destination:String) {
        loading.value = true
        viewModelScope.launch(exceptionHandler) {

            when (val response = mainRepository.upLoadImage(body,authenticator,destination)) {
                is NetworkState.Success -> {
                    imageUploadDataResponse.postValue(response.data)
                    loading.value = false
                }
               is NetworkState.Error -> {
                    Log.d("errors",response.message.toString())
                    loading.value = false
                   if ((response.statusCode ?: 0) >= 500) {
                       onError("Internal Server Error: Please try again later.")
                   }
                    else if (response.statusCode == 404) {
                        onError("Some thing went wrong please try again")
                    }

                    else {
                        onError(response.message ?: "Unknown error")
                    }

                }
            }
        }
    }


    fun upLoadFile(body:MultipartBody.Part,authenticator: String,destination: RequestBody) {
        uploadExcel(body, authenticator) { mainRepository.uploadExcelFile(it, authenticator, destination) }
    }

    fun uploadAmcExcel(body: MultipartBody.Part, authenticator: String) {
        uploadExcel(body, authenticator) { mainRepository.uploadAmcExcelFile(it, authenticator) }
    }

    fun uploadTechnicianExcel(body: MultipartBody.Part, authenticator: String) {
        uploadExcel(body, authenticator) { mainRepository.uploadTechnicianExcelFile(it, authenticator) }
    }

    private fun uploadExcel(
        body: MultipartBody.Part,
        authenticator: String,
        request: suspend (MultipartBody.Part) -> NetworkState<ChangePasswordData>
    ) {
        loading.value = true
        viewModelScope.launch(exceptionHandler) {

            when (val response = request(body)) {
                is NetworkState.Success -> {
                    imageUploadDataResponse.postValue(response.data)
                    loading.value = false
                }
               is NetworkState.Error -> {
                    Log.d("errors",response.message.toString())
                    loading.value = false
                   if ((response.statusCode ?: 0) >= 500) {
                       onError("Internal Server Error: Please try again later.")
                   }
                    else if (response.statusCode == 404) {
                        onError("Some thing went wrong please try again")
                    }

                    else {
                        onError(response.message ?: "Unknown error")
                    }

                }
            }
        }
    }



    fun upsertToken(body: JsonObject, authenticator: String) {
        viewModelScope.launch(exceptionHandler) {
            mainRepository.upsertToken(body, authenticator)
        }
    }

    private fun onError(message: String) {
        _errorMessage.postValue(message)
        loading.postValue(false)
    }

    override fun onCleared() {
        super.onCleared()
    }
}
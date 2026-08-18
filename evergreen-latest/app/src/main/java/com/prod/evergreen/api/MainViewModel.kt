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
        onError("Exception handled: ${throwable.localizedMessage}")
    }
     val loading = MutableLiveData<Boolean>()

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
                    if (response.statusCode!! >= 500) {
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
                    if (response.statusCode!! >= 500) {
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
                    if (response.statusCode!! >= 500) {
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
                    allequipmentsDataResponse.postValue(response.data)
                    loading.value = false
                }
                is NetworkState.Error -> {
                    Log.d("errors",response.message.toString())
                    loading.value = false
                    if (response.statusCode!! >= 500) {
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
                   if (response.statusCode!! >= 500) {
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
                   if (response.statusCode!! >= 500) {
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
                    Log.d("errors",response.message.toString())
                    loading.value = false
                    if (response.statusCode!! >= 500) {
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
                   if (response.statusCode!! >= 500) {
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
                   if (response.statusCode!! >= 500) {
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
                   if (response.statusCode!! >= 500) {
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
                   if (response.statusCode!! >= 500) {
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
                   if (response.statusCode!! >= 500) {
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
                   if (response.statusCode!! >= 500) {
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
                   if (response.statusCode!! >= 500) {
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
                   if (response.statusCode!! >= 500) {
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
                    if (response.statusCode!! >= 500) {
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
                   if (response.statusCode!! >= 500) {
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
                }
               is NetworkState.Error -> {
                    Log.d("errors",response.message.toString())
                    loading.value = false
                   if (response.statusCode!! >= 500) {
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
                   if (response.statusCode!! >= 500) {
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
                   if (response.statusCode!! >= 500) {
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
                   if (response.statusCode!! >= 500) {
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
                   if (response.statusCode!! >= 500) {
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
                   if (response.statusCode!! >= 500) {
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
                   if (response.statusCode!! >= 500) {
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
                   if (response.statusCode!! >= 500) {
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
        loading.value = true
        viewModelScope.launch(exceptionHandler) {

            when (val response = mainRepository.uploadExcelFile(body,authenticator,destination)) {
                is NetworkState.Success -> {
                    imageUploadDataResponse.postValue(response.data)
                    loading.value = false
                }
               is NetworkState.Error -> {
                    Log.d("errors",response.message.toString())
                    loading.value = false
                   if (response.statusCode!! >= 500) {
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



    private fun onError(message: String) {
        _errorMessage.value = message
        loading.value = false
    }

    override fun onCleared() {
        super.onCleared()
    }
}
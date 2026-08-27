package com.prod.evergreen.api

import androidx.lifecycle.LiveData
import com.google.gson.JsonObject
import com.prod.evergreen.activities.AddEquipment
import com.prod.evergreen.db.CompaniesDao
import com.prod.evergreen.db.Movie
import com.prod.evergreen.db.NewsDao
import com.prod.evergreen.db.ProductsResponse
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
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody


class MainRepository(private val retrofitService: RetrofitService, private val newsDao: NewsDao ,private  val companiesDao: CompaniesDao) {

    val allNews: LiveData<List<Movie>> = newsDao.getAllNews()
    val allUserCompanies: LiveData<List<Company>> = companiesDao.getAllCompanies()


    suspend fun getAllMovies(): NetworkState<ProductsResponse> {
        return try {
            val response = retrofitService.getAllMovies()
            response.parseResponse()

        } catch (e: Exception) {
            NetworkState.Error(e.message)
        }
    }




    suspend fun getAllAmc(authenticator: String): NetworkState<AllAmcData> {
        return try {
            val response = retrofitService.getAllAmc("Bearer "+authenticator)
            response.parseResponse()

        } catch (e: Exception) {
            NetworkState.Error(e.message)
        }
    }



    suspend fun getAllEquipments(authorization: String): NetworkState<AllEquipmentsData> {
        return try {
            val response = retrofitService.getAllEquipments("Bearer "+authorization)
            response.parseResponse()

        } catch (e: Exception) {
            NetworkState.Error(e.message)
        }
    }



    suspend fun getAllEquipmentsByID(authorization: String,company_id: JsonObject): NetworkState<AllEquipmentsData> {
        return try {
            val response = retrofitService.getAllEquipmentsByID("Bearer "+authorization,company_id)
            response.parseResponse()

        } catch (e: Exception) {
            NetworkState.Error(e.message)
        }
    }




    suspend fun getAllUsers(authorization: String,company_id: JsonObject): NetworkState<AllUsers> {
        return try {
            val response = retrofitService.getAllUsers("Bearer "+authorization,company_id)
            response.parseResponse()

        } catch (e: Exception) {
            NetworkState.Error(e.message)
        }
    }



    suspend fun getAllTasks(authorization: String,status: JsonObject): NetworkState<AllTasks> {
        return try {
            val response = retrofitService.getAllTasks("Bearer "+authorization,status)
            response.parseResponse()

        } catch (e: Exception) {
            NetworkState.Error(e.message)
        }
    }





    suspend fun userLogin(body: JsonObject): NetworkState<LoginData> {
        return try {
            val response = retrofitService.userLogin("123456",body)
            response.parseResponse()

        } catch (e: Exception) {
            android.util.Log.e("userLogin", "request failed", e)
            val message = when (e) {
                is java.net.UnknownHostException,
                is java.net.ConnectException ->
                    "Unable to reach server. Check your internet connection."
                is java.net.SocketTimeoutException ->
                    "Connection timed out. Please try again."
                else -> e.message?.takeIf { it.isNotBlank() && it != "null" }
                    ?: e.javaClass.simpleName
            }
            NetworkState.Error(message)
        }
    }
    suspend fun forgotPassword(body: JsonObject): NetworkState<ForgotPasswordData> {
        return try {
            val response = retrofitService.forgotPassword("123456",body)
            response.parseResponse()

        } catch (e: Exception) {
            NetworkState.Error(e.message)
        }
    }

    suspend fun verifyOtp(body: JsonObject): NetworkState<VerifyData> {
        return try {
            val response = retrofitService.verifyOtp(body)
            response.parseResponse()

        } catch (e: Exception) {
            NetworkState.Error(e.message)
        }
    }

  suspend fun updatePassword(body: JsonObject): NetworkState<ChangePasswordData> {
        return try {
            val response = retrofitService.updatePassword(body)
            response.parseResponse()

        } catch (e: Exception) {
            NetworkState.Error(e.message)
        }
    }


  suspend fun createTechnician(body: JsonObject,authenticator: String): NetworkState<ChangePasswordData> {
        return try {
            val response = retrofitService.createTechnician(body,"Bearer "+authenticator)
            response.parseResponse()

        } catch (e: Exception) {
            NetworkState.Error(e.message)
        }
    }



  suspend fun createTask(body: JsonObject,authenticator: String): NetworkState<ChangePasswordData> {
        return try {
            val response = retrofitService.createTask(body,"Bearer "+authenticator)
            response.parseResponse()

        } catch (e: Exception) {
            NetworkState.Error(e.message)
        }
    }



  suspend fun createAMC(body: JsonObject,authenticator: String): NetworkState<ChangePasswordData> {
        return try {
            val response = retrofitService.createAMC(body,"Bearer "+authenticator)
            response.parseResponse()

        } catch (e: Exception) {
            NetworkState.Error(e.message)
        }
    }


  suspend fun addEquipment(body: AddEquipment.Equipment, authenticator: String): NetworkState<ChangePasswordData> {
        return try {
            val response = retrofitService.addEquipment(body,"Bearer "+authenticator)
            response.parseResponse()

        } catch (e: Exception) {
            NetworkState.Error(e.message)
        }
    }


  suspend fun updateEquipment(body: AddEquipment.EquipmentUpdate, authenticator: String): NetworkState<ChangePasswordData> {
        return try {
            val response = retrofitService.updateEquipment(body,"Bearer "+authenticator)
            response.parseResponse()

        } catch (e: Exception) {
            NetworkState.Error(e.message)
        }
    }


  suspend fun updateHoldReasons(body: JsonObject, authenticator: String): NetworkState<ChangePasswordData> {
        return try {
            val response = retrofitService.updateHoldReasons(body,"Bearer "+authenticator)
            response.parseResponse()

        } catch (e: Exception) {
            NetworkState.Error(e.message)
        }
    }



  suspend fun upDateTaskStatus(body: JsonObject, authenticator: String): NetworkState<ChangePasswordData> {
        return try {
            val response = retrofitService.upDateTaskStatus(body,"Bearer "+authenticator)
            response.parseResponse()

        } catch (e: Exception) {
            NetworkState.Error(e.message)
        }
    }


  suspend fun GetEquipmentInfo(body: JsonObject, authenticator: String): NetworkState<EquipmentInfo> {
        return try {
            val response = retrofitService.GetEquipmentInfo(body,"Bearer "+authenticator)
            response.parseResponse()

        } catch (e: Exception) {
            NetworkState.Error(e.message)
        }
    }



  suspend fun assignTechnician(body: JsonObject, authenticator: String): NetworkState<ChangePasswordData> {
        return try {
            val response = retrofitService.assignTechnician(body,"Bearer "+authenticator)
            response.parseResponse()

        } catch (e: Exception) {
            NetworkState.Error(e.message)
        }
    }

  suspend fun taskUpDateFeedback(body: JsonObject, authenticator: String): NetworkState<ChangePasswordData> {
        return try {
            val response = retrofitService.taskUpDateFeedback(body,"Bearer "+authenticator)
            response.parseResponse()

        } catch (e: Exception) {
            NetworkState.Error(e.message)
        }
    }


  suspend fun generateServiceReport(body: JsonObject, authenticator: String): NetworkState<ChangePasswordData> {
        return try {
            val response = retrofitService.generateServiceReport(body,"Bearer "+authenticator)
            response.parseResponse()

        } catch (e: Exception) {
            NetworkState.Error(e.message)
        }
    }


  suspend fun getServiceReport(body: JsonObject, authenticator: String): NetworkState<ChangePasswordData> {
        return try {
            val response = retrofitService.getServiceReport(body,"Bearer "+authenticator)
            response.parseResponse()

        } catch (e: Exception) {
            NetworkState.Error(e.message)
        }
    }



  suspend fun downloadAllEquipment(body: JsonObject, authenticator: String): NetworkState<ResponseBody> {
        return try {
            val response = retrofitService.downloadAllEquipment(body,"Bearer "+authenticator)
            response.parseResponse()

        } catch (e: Exception) {
            NetworkState.Error(e.message)
        }
    }


  suspend fun getNotifications(authenticator: String): NetworkState<NotificationsListResponse> {
        return try {
            val response = retrofitService.getNotifications("Bearer "+authenticator)
            response.parseResponse()

        } catch (e: Exception) {
            NetworkState.Error(e.message)
        }
    }

  suspend fun getUserStats(authenticator: String): NetworkState<UserStatsResponse> {
        return try {
            val response = retrofitService.getUserStats("Bearer "+authenticator)
            response.parseResponse()

        } catch (e: Exception) {
            NetworkState.Error(e.message)
        }
    }



  suspend fun getAllTskCount(authenticator: String): NetworkState<AllTaskCountResponse> {
        return try {
            val response = retrofitService.getAllTskCount("Bearer "+authenticator)
            response.parseResponse()

        } catch (e: Exception) {
            NetworkState.Error(e.message)
        }
    }





  suspend fun getCompaniesStats(authenticator: String): NetworkState<CompaniesStatsResponse> {
        return try {
            val response = retrofitService.getCompanyStats("Bearer "+authenticator)
            response.parseResponse()

        } catch (e: Exception) {
            NetworkState.Error(e.message)
        }
    }






  suspend fun upLoadImage(body: MultipartBody.Part, authenticator: String,destination:String): NetworkState<ChangePasswordData> {
        return try {
            val response = retrofitService.upLoadImage(body,"Bearer "+authenticator,destination)
            response.parseResponse()

        } catch (e: Exception) {
            NetworkState.Error(e.message)
        }
    }


  suspend fun uploadExcelFile(body: MultipartBody.Part, authenticator: String,type: RequestBody): NetworkState<ChangePasswordData> {
        return try {
            val response = retrofitService.uploadExcelFile(body,"Bearer "+authenticator,type)
            response.parseResponse()

        } catch (e: Exception) {
            NetworkState.Error(e.message)
        }
    }


    suspend fun saveProducts(products: List<Movie>) {
        newsDao.insertNews(products)
    }


    suspend fun saveCompanies(products: Company) {
        companiesDao.insertCompanies(products)
    }



}
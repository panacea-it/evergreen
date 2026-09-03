package com.prod.evergreen.api


import android.content.Context
import com.google.gson.JsonObject
import com.prod.evergreen.activities.AddEquipment
import com.prod.evergreen.db.ProductsResponse
import com.prod.evergreen.helper.ConstantValues
import com.prod.evergreen.helper.SharedPreferencesHelper
import com.prod.evergreen.models.AllAmcData
import com.prod.evergreen.models.AllEquipmentsData
import com.prod.evergreen.models.AllTaskCountResponse
import com.prod.evergreen.models.AllTasks
import com.prod.evergreen.models.AllUsers
import com.prod.evergreen.models.ChangePasswordData
import com.prod.evergreen.models.CompaniesStatsResponse
import com.prod.evergreen.models.EquipmentInfo
import com.prod.evergreen.models.ForgotPasswordData
import com.prod.evergreen.models.LoginData
import com.prod.evergreen.models.NotificationsListResponse
import com.prod.evergreen.models.ServiceReportResponse
import com.prod.evergreen.models.ServiceReportsResponse
import com.prod.evergreen.models.UserStatsResponse
import com.prod.evergreen.models.VerifyData
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.PUT
import retrofit2.http.Query

interface RetrofitService {

    @GET(Constants.GETLIST)
    suspend fun getAllMovies() : Response<ProductsResponse>


    @GET(Constants.GET_ALL_AMC)
    suspend fun getAllAmc(@Header("Authorization") authorization: String?) : Response<AllAmcData>



    @POST(Constants.GET_ALL_EQUIPMENTS)
    suspend fun getAllEquipments(@Header("Authorization") authorization: String?) : Response<AllEquipmentsData>



    @POST(Constants.GET_ALL_USERS)
    suspend fun getAllUsers(@Header("Authorization") authorization: String?,@Body access_level: JsonObject) : Response<AllUsers>




    @POST(Constants.GET_ALL_TASKS)
    suspend fun getAllTasks(@Header("Authorization") authorization: String?,@Body status: JsonObject) : Response<AllTasks>



    @POST(Constants.GET_ALL_EQUIPMENTS_BY_ID)
    suspend fun getAllEquipmentsByID(@Header("Authorization") authorization: String?, @Body companyId: JsonObject) : Response<AllEquipmentsData>


    @POST(Constants.LOGIN)
    suspend fun userLogin(@Header("api-key") authorization: String?,@Body body: JsonObject): Response<LoginData>


    @POST(Constants.FORGOT_PASSWORD)
    suspend fun forgotPassword(@Header("api-key") authorization: String?,@Body body: JsonObject): Response<ForgotPasswordData>


    @POST(Constants.VERIFYOTP)
    suspend fun verifyOtp(@Body body: JsonObject): Response<VerifyData>


    @POST(Constants.UPDATE_PASSWORD)
    suspend fun updatePassword(@Body body: JsonObject): Response<ChangePasswordData>


    @POST(Constants.CREATE_TECHNICIAN)
    suspend fun createTechnician(@Body body: JsonObject, @Header("Authorization") authorization: String?,): Response<ChangePasswordData>


    @POST(Constants.CREATE_TASK)
    suspend fun createTask(@Body body: JsonObject, @Header("Authorization") authorization: String?,): Response<ChangePasswordData>


    @POST(Constants.CREATE_AMC)
    suspend fun createAMC(@Body body: JsonObject, @Header("Authorization") authorization: String?,): Response<ChangePasswordData>

    @PUT(Constants.UPDATE_AMC + "/{id}")
    suspend fun updateAMC(@Path("id") companyId: Int, @Body body: JsonObject, @Header("Authorization") authorization: String?,): Response<ChangePasswordData>

    @POST(Constants.DELETE_AMC)
    suspend fun deleteAMC(@Body body: JsonObject, @Header("Authorization") authorization: String?,): Response<ChangePasswordData>


    @POST(Constants.ADD_EQUIPMENT)
    suspend fun addEquipment(@Body equipment: AddEquipment.Equipment, @Header("Authorization") authorization: String?,): Response<ChangePasswordData>


    @POST(Constants.updateEquipment)
    suspend fun updateEquipment(@Body equipment: AddEquipment.EquipmentUpdate, @Header("Authorization") authorization: String?,): Response<ChangePasswordData>

    @POST(Constants.DELETE_EQUIPMENT)
    suspend fun deleteEquipment(@Body body: JsonObject, @Header("Authorization") authorization: String?,): Response<ChangePasswordData>

    @POST(Constants.UPDATE_TASK)
    suspend fun updateTask(@Body body: JsonObject, @Header("Authorization") authorization: String?,): Response<ChangePasswordData>

    @POST(Constants.DELETE_TASK)
    suspend fun deleteTask(@Body body: JsonObject, @Header("Authorization") authorization: String?,): Response<ChangePasswordData>

    @PUT(Constants.UPDATE_USER + "/{id}")
    suspend fun updateUser(@Path("id") userId: Int, @Body body: JsonObject, @Header("Authorization") authorization: String?,): Response<ChangePasswordData>

    @POST(Constants.DELETE_USER)
    suspend fun deleteUser(@Body body: JsonObject, @Header("Authorization") authorization: String?,): Response<ChangePasswordData>

    @POST(Constants.updateHoldReasons)
    suspend fun updateHoldReasons(@Body equipment: JsonObject, @Header("Authorization") authorization: String?,): Response<ChangePasswordData>



    @POST(Constants.UPDATE_TASK_STATUS)
    suspend fun upDateTaskStatus(@Body equipment:JsonObject, @Header("Authorization") authorization: String?,): Response<ChangePasswordData>



    @POST(Constants.GET_EQ_INFO)
    suspend fun GetEquipmentInfo(@Body equipment:JsonObject, @Header("Authorization") authorization: String?,): Response<EquipmentInfo>



    @POST(Constants.ASSIGN_TECHNICIAN)
    suspend fun assignTechnician(@Body equipment:JsonObject, @Header("Authorization") authorization: String?,): Response<ChangePasswordData>


    @POST(Constants.TASK_UPDATE_FEEDBACK)
    suspend fun taskUpDateFeedback(@Body equipment:JsonObject, @Header("Authorization") authorization: String?,): Response<ChangePasswordData>


    @POST(Constants.GenerateServiceReport)
    suspend fun generateServiceReport(@Body equipment:JsonObject, @Header("Authorization") authorization: String?,): Response<ChangePasswordData>


    @POST(Constants.getServiceReport)
    suspend fun getServiceReport(@Body equipment:JsonObject, @Header("Authorization") authorization: String?,): Response<ChangePasswordData>

    @POST(Constants.GET_ALL_SERVICE_REPORTS)
    suspend fun getAllServiceReports(@Body body: JsonObject, @Header("Authorization") authorization: String?): Response<ServiceReportsResponse>

    @POST(Constants.GET_SERVICE_REPORT_DETAILS)
    suspend fun getServiceReportDetails(@Body body: JsonObject, @Header("Authorization") authorization: String?): Response<ServiceReportResponse>

    @POST(Constants.CREATE_SERVICE_REPORT)
    suspend fun createServiceReport(@Body body: JsonObject, @Header("Authorization") authorization: String?): Response<ServiceReportResponse>

    @POST(Constants.UPDATE_SERVICE_REPORT)
    suspend fun updateServiceReport(@Body body: JsonObject, @Header("Authorization") authorization: String?): Response<ServiceReportResponse>

    @POST(Constants.DELETE_SERVICE_REPORT)
    suspend fun deleteServiceReport(@Body body: JsonObject, @Header("Authorization") authorization: String?): Response<ChangePasswordData>

    @POST(Constants.DOWNLOAD_SERVICE_REPORT_PDF)
    suspend fun downloadServiceReportPdf(@Body body: JsonObject, @Header("Authorization") authorization: String?): Response<ChangePasswordData>

    @POST(Constants.GET_SERVICE_REPORT_PREFILL)
    suspend fun getServiceReportPrefill(@Body body: JsonObject, @Header("Authorization") authorization: String?): Response<ServiceReportResponse>



    @POST(Constants.EQUIPMENT_QR_DOWNLOAD)
    suspend fun downloadAllEquipment(@Body equipment:JsonObject, @Header("Authorization") authorization: String?,): Response<ResponseBody>



    @POST(Constants.NOTIFICATIONS_LIST)
    suspend fun getNotifications(@Header("Authorization") authorization: String?,): Response<NotificationsListResponse>

    @POST(Constants.NOTIFICATIONS_PAGE)
    suspend fun getNotificationsPage(
        @Body body: JsonObject,
        @Header("Authorization") authorization: String?
    ): Response<NotificationsListResponse>

    @POST(Constants.UPSERT_TOKEN)
    suspend fun upsertToken(@Body body: JsonObject, @Header("Authorization") authorization: String?): Response<ChangePasswordData>



    @POST(Constants.COMPANIES_STATS)
    suspend fun getCompanyStats(@Header("Authorization") authorization: String?,): Response<CompaniesStatsResponse>


    @POST(Constants.USER_STATS)
    suspend fun getUserStats(@Header("Authorization") authorization: String?,): Response<UserStatsResponse>


    @POST(Constants.GET_ALL_TASKS_COUNT)
    suspend fun getAllTskCount(@Header("Authorization") authorization: String?,): Response<AllTaskCountResponse>



//destination
    @Multipart
    @POST(Constants.UPLOAD)
    suspend fun upLoadImage(@Part file: MultipartBody.Part,@Header("Authorization") authorization: String?,@Query("folderName") destination:String): Response<ChangePasswordData>


//destination
    @Multipart
    @POST(Constants.UPLOAD_EXCEL_DATA)
    suspend fun uploadExcelFile(@Part file: MultipartBody.Part,@Header("Authorization") authorization: String?,@Part("company_link")type: RequestBody): Response<ChangePasswordData>

    @Multipart
    @POST(Constants.UPLOAD_AMC_EXCEL_DATA)
    suspend fun uploadAmcExcelFile(@Part file: MultipartBody.Part,@Header("Authorization") authorization: String?): Response<ChangePasswordData>

    @Multipart
    @POST(Constants.UPLOAD_TECHNICIAN_EXCEL_DATA)
    suspend fun uploadTechnicianExcelFile(@Part file: MultipartBody.Part,@Header("Authorization") authorization: String?): Response<ChangePasswordData>



    companion object {
        private val httpClient = OkHttpClient.Builder()
            .connectTimeout(20, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(45, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(45, java.util.concurrent.TimeUnit.SECONDS)
            .callTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
            .build()

        private var retrofitService: RetrofitService? = null

        fun getInstance(context: Context) : RetrofitService {
        //  val  baseurl= SharedPreferencesHelper(context).getValueString(ConstantValues.BASE_URL)?:""
              if (retrofitService == null) {
                val retrofit = Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .client(httpClient) // Set custom OkHttpClient
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                retrofitService = retrofit.create(RetrofitService::class.java)
            }
            return retrofitService!!
        }
    }


}

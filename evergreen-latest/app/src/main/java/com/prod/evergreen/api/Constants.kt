package com.prod.evergreen.api

class Constants {

    companion object{
        const val BASE_URL = "https://api.testverse.site/"
        const val LOGIN = "auth/signIn"
        const val GETLIST = "products"
        const val FORGOT_PASSWORD = "user/forgetPassword"
        const val VERIFYOTP = "verify"
        const val UPLOAD_EXCEL_DATA = "user/uploadEquipmentData"
        const val UPDATE_PASSWORD = "update-password"
        const val CREATE_TECHNICIAN = "user/createUser"
        const val CREATE_TASK = "user/createTask"
        const val CREATE_AMC = "user/createCompany"
        const val UPLOAD = "user/upload-file"
        const val GET_ALL_AMC = "user/getAllCompanies"
        const val GET_ALL_USERS = "user/getAllUser"
        const val GET_ALL_TASKS = "user/getAllTasks"
        const val GET_ALL_EQUIPMENTS = "user/getAllEquipments"
        const val ADD_EQUIPMENT = "user/createEquipment"
        const val updateEquipment = "user/updateEquipment"
        const val updateHoldReasons = "user/updateHoldReasons"
        const val GET_ALL_EQUIPMENTS_BY_ID = "user/getAllEquipments"
        const val UPDATE_TASK_STATUS = "user/updateTaskStatus"
        const val GET_EQ_INFO = "user/getEquipmentInfo"
        const val ASSIGN_TECHNICIAN = "user/assignTechnician"
        const val TASK_UPDATE_FEEDBACK = "user/updateTaskFeedback"
        const val GenerateServiceReport = "user/clientSignatureForReport"
        const val getServiceReport = "user/getServiceReport"
        const val EQUIPMENT_QR_DOWNLOAD = "user/qrCodeGenerator"
        const val NOTIFICATIONS_LIST = "user/getNotificationsByUserId"
        const val COMPANIES_STATS = "user/companiesStats"
        const val USER_STATS = "user/getUserStats"
        const val GET_ALL_TASKS_COUNT = "user/getAllTasksCount"

    }
}
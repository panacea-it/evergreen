package com.prod.evergreen.models

import com.google.gson.annotations.SerializedName
data class AllTasks(
    val data: List<TaskCreated>? = emptyList(),
    val count: Count? = null,
    val success: Int? = null,
    val message: String? = null,
)

data class TaskCreated(
    val id: Int = 0,
    @SerializedName("technician_link")
    val technicianLink: Any? = null,
    @SerializedName("client_link")
    val clientLink: Int? = null,
    @SerializedName("task_link")
    val taskLink: Int? = null,
    val status: String? = null,
    val reason: String?=null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("created_by")
    val createdBy: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    @SerializedName("updated_by")
    val updatedBy: String? = null,
    val task: Task? = null,
    val client: Client? = null,
    @SerializedName("technician") val technician: Technician?=null

)



data class Task(
    val id: Int = 0,
    val name: String? = null,
    val description: String? = null,
    @SerializedName("call_type")
    val callType: String? = null,
    val image: List<String>? = emptyList(),
    @SerializedName("equipment_link")
    val equipmentLink: Long? = null,
    @SerializedName("ticket_no")
    val ticketNo: String? = null,
    @SerializedName("hold_reason")
    var holdReason:List<HoldReason>? = emptyList(),
    val otp: String? = null,
    val feedback: String?=null,
    @SerializedName("action_taken")
    val actionTaken: String?=null,
    @SerializedName("req_details")
    val reqDetails: String?,
    @SerializedName("follow_up")
    val followUp: Boolean?=null,
    @SerializedName("action_req_details")
    val actionReqDetails: String?,
    @SerializedName("service_satisfactory")
    val serviceSatisfactory: Any?,
    @SerializedName("is_running_smoothly")
    val isRunningSmoothly: Any?,
    val notes: Any?,
    val rating: Int?=null,
    val status: String?=null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("created_by")
    val createdBy: String?=null,
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    @SerializedName("updated_by")
    val updatedBy: String?=null,
    val equipment: Equipment?=null,
)



data class HoldReason (
    val id: Int? = null,
    val hold: String? = null,
    val is_active: Boolean? = null,
    val spare_part_number: String? = null,
    val image: String? = null,
    val reason: String? = null,
    val created_at: String? = null,
    val created_by: String? = null,
    val updated_at: String? = null,
    val updated_by: String? = null,
    val task_link: Int? = null
)
data class Equipment(
    val id: Int,
    val name: String,
    val make: String,
    val model: String,
    @SerializedName("serial_number")
    val serialNumber: String,
    val specifications: String,
    @SerializedName("manufacturer_date")
    val manufacturerDate: String,
    val location: String,
    val description: String,
    @SerializedName("tm_frequency")
    val tmFrequency: String,
    @SerializedName("company_link")
    val companyLink: Int,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("created_by")
    val createdBy: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("updated_by")
    val updatedBy: String,
    val company: CompanyData? = null,
)

data class CompanyData(
    val id: Int,
    val name: String,
    @SerializedName("branch_name")
    val branchName: String,
    val email: String,
    @SerializedName("start_date")
    val startDate: String,
    @SerializedName("end_date")
    val endDate: String,
    val location: String,
    val logo: String,
    @SerializedName("parent_company_link")
    val parentCompanyLink: Any?,
    @SerializedName("address_link")
    val addressLink: Any?,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("created_by")
    val createdBy: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("updated_by")
    val updatedBy: String,
)

data class Client(
    val name: String,
    val id: Long,
    @SerializedName("access_level")
    val accessLevel: String,
    val phone: String,
)

data class Count(
    val open: Int,
    val closed: Int,
    val in_progress: Int,
    val hold: Int,
)
data class Technician (

    @SerializedName("name"         ) var name        : String? = null,
    @SerializedName("id"           ) var id          : Int?    = null,
    @SerializedName("access_level" ) var accessLevel : String? = null,
    @SerializedName("phone"        ) var phone       : String? = null

)








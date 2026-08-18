package com.prod.evergreen.models

import com.google.gson.annotations.SerializedName

data class EquipmentInfo(

	@field:SerializedName("data")
	val data: ResponseData? = null,

	@field:SerializedName("success")
	val success: Int? = null
)

data class TaskData(

	@field:SerializedName("image")
	val image: List<String?>? = null,

	@field:SerializedName("notes")
	val notes: Any? = null,

	@field:SerializedName("action_taken")
	val actionTaken: Any? = null,

	@field:SerializedName("rating")
	val rating: Any? = null,

	@field:SerializedName("description")
	val description: String? = null,

	@field:SerializedName("created_at")
	val createdAt: String? = null,

	@field:SerializedName("otp")
	val otp: String? = null,

	@field:SerializedName("created_by")
	val createdBy: String? = null,

	@field:SerializedName("req_details")
	val reqDetails: Any? = null,

	@field:SerializedName("follow_up")
	val followUp: Any? = null,

	@field:SerializedName("feedback")
	val feedback: Any? = null,

	@field:SerializedName("updated_at")
	val updatedAt: String? = null,

	@field:SerializedName("ticket_no")
	val ticketNo: String? = null,

	@field:SerializedName("equipment_link")
	val equipmentLink: Int? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("is_running_smoothly")
	val isRunningSmoothly: Any? = null,

	@field:SerializedName("updated_by")
	val updatedBy: String? = null,

	@field:SerializedName("action_req_details")
	val actionReqDetails: Any? = null,

	@field:SerializedName("service_satisfactory")
	val serviceSatisfactory: Any? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("call_type")
	val callType: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class ClientData(

	@field:SerializedName("access_level")
	val accessLevel: String? = null,

	@field:SerializedName("phone")
	val phone: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: Int? = null
)

data class TasksItem(

	@field:SerializedName("task_link")
	val taskLink: Int? = null,

	@field:SerializedName("task")
	val task: TaskData? = null,

	@field:SerializedName("updated_at")
	val updatedAt: String? = null,

	@field:SerializedName("updated_by")
	val updatedBy: String? = null,

	@field:SerializedName("created_at")
	val createdAt: String? = null,

	@field:SerializedName("client")
	val client: ClientData? = null,

	@field:SerializedName("technician")
	val technician: TechnicianData? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("client_link")
	val clientLink: Int? = null,

	@field:SerializedName("technician_link")
	val technicianLink: Int? = null,

	@field:SerializedName("created_by")
	val createdBy: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class TechnicianData(

	@field:SerializedName("access_level")
	val accessLevel: String? = null,

	@field:SerializedName("phone")
	val phone: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: Int? = null
)

data class ResponseData(

	@field:SerializedName("tm_frequency")
	val tmFrequency: String? = null,

	@field:SerializedName("company_link")
	val companyLink: Int? = null,

	@field:SerializedName("description")
	val description: String? = null,

	@field:SerializedName("eg_serial_number")
	val egserialnumber: String? = null,

	@field:SerializedName("created_at")
	val createdAt: String? = null,

	@field:SerializedName("serial_number")
	val serialNumber: String? = null,

	@field:SerializedName("specifications")
	val specifications: String? = null,

	@field:SerializedName("image_url")
	val imageUrl: String? = null,

	@field:SerializedName("created_by")
	val createdBy: String? = null,

	@field:SerializedName("manufacturer_date")
	val manufacturerDate: String? = null,

	@field:SerializedName("updated_at")
	val updatedAt: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("updated_by")
	val updatedBy: String? = null,

	@field:SerializedName("model")
	val model: String? = null,

	@field:SerializedName("location")
	val location: String? = null,

	@field:SerializedName("company")
	val company: CompanyDataResponse? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("make")
	val make: String? = null,

	@field:SerializedName("tasks")
	val tasks: List<TasksItem>? = null
)

data class CompanyDataResponse(

	@field:SerializedName("end_date")
	val endDate: String? = null,

	@field:SerializedName("created_at")
	val createdAt: String? = null,

	@field:SerializedName("created_by")
	val createdBy: String? = null,

	@field:SerializedName("parent_company_link")
	val parentCompanyLink: Any? = null,

	@field:SerializedName("updated_at")
	val updatedAt: String? = null,

	@field:SerializedName("branch_name")
	val branchName: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("address_link")
	val addressLink: Any? = null,

	@field:SerializedName("updated_by")
	val updatedBy: String? = null,

	@field:SerializedName("logo")
	val logo: String? = null,

	@field:SerializedName("location")
	val location: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("email")
	val email: String? = null,

	@field:SerializedName("start_date")
	val startDate: String? = null
)

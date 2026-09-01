package com.prod.evergreen.models

import com.google.gson.annotations.SerializedName

data class AllAmcData(

	@field:SerializedName("data")
	val data: List<AMCData>? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Int? = null
)

data class User1(

	@field:SerializedName("access_level")
	val accessLevel: String? = null,

	@field:SerializedName("phone")
	val phone: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("email")
	val email: String? = null
)

data class AMCData(

	@field:SerializedName("end_date")
	val endDate: String? = null,

	@field:SerializedName("poc_details")
	val pocDetails: PocDetails? = null,

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
	val startDate: String? = null,

	@field:SerializedName("is_active")
	val isActive: Boolean? = null
)

fun AMCData.isCompanyActive(): Boolean = isActive != false

fun List<AMCData>?.activeCompanies(): List<AMCData> =
	orEmpty().filter { it.isCompanyActive() }

data class PocDetails(

	@field:SerializedName("user")
	val user: User1? = null
)

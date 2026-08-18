package com.prod.evergreen.models

import com.google.gson.annotations.SerializedName

data class CompaniesStatsResponse(

	@field:SerializedName("data")
	val data: List<DataItem1>? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Int? = null
)

data class DataItem1(

	@field:SerializedName("month")
	val month: String? = null,

	@field:SerializedName("value")
	val value: Int? = null
)

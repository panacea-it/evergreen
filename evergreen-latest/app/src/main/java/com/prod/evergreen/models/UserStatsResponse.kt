package com.prod.evergreen.models

import com.google.gson.annotations.SerializedName

data class UserStatsResponse(

	@field:SerializedName("data")
	val data: List<DataItem2>? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Int? = null
)

data class DataItem2(

	@field:SerializedName("access_level")
	val accessLevel: String? = null,

	@field:SerializedName("count")
	val count: Int? = null
)

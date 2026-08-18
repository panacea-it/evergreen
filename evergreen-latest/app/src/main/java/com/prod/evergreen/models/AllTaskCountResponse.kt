package com.prod.evergreen.models

import com.google.gson.annotations.SerializedName

data class AllTaskCountResponse(

	@field:SerializedName("data")
	val data: DataCount? = null,

	@field:SerializedName("status")
	val success: Int? = null,

	@field:SerializedName("message")
	val message: String? = null
)

data class DataCount(

	@field:SerializedName("in_progress")
	val inProgress: Int? = null,

	@field:SerializedName("closed")
	val closed: Int? = null,

	@field:SerializedName("open")
	val open: Int? = null,

	@field:SerializedName("hold")
	val hold: Int? = null
)

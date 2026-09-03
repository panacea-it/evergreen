package com.prod.evergreen.models

import com.google.gson.annotations.SerializedName

data class NotificationsListResponse(

	@field:SerializedName("data")
	val data: List<DataItem>? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Int? = null,

    @field:SerializedName("page")
    val page: Int? = null,

    @field:SerializedName("limit")
    val limit: Int? = null,

    @field:SerializedName("total")
    val total: Int? = null,

    @field:SerializedName("has_more")
    val hasMore: Boolean? = null
)

data class DataItem(

	@field:SerializedName("task_link")
	val taskLink: Int? = null,

	@field:SerializedName("is_read")
	val isRead: Boolean? = null,

	@field:SerializedName("updated_at")
	val updatedAt: String? = null,

	@field:SerializedName("image_url")
	val imageUrl: String? = null,

	@field:SerializedName("user_link")
	val userLink: Int? = null,

	@field:SerializedName("updated_by")
	val updatedBy: String? = null,

	@field:SerializedName("accepted_by")
	val acceptedBy: AcceptedBy? = null,


	@field:SerializedName("description")
	val description: String? = null,

	@field:SerializedName("created_at")
	val createdAt: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("title")
	val title: String? = null,

	@field:SerializedName("created_by")
	val createdBy: String? = null
)


data class  AcceptedBy(
	@field:SerializedName("name")
	val name: String? = null,
	@field:SerializedName("phone")
	val phone: String? = null,
)

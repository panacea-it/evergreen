package com.prod.evergreen.models

import com.google.gson.annotations.SerializedName

data class UserData(
    @SerializedName("id"                ) var id               : Int?    = null,
    @SerializedName("name"              ) var name             : String? = null,
    @SerializedName("location"          ) var location         : String? = null,
    @SerializedName("phone"             ) var phone            : String? = null,
    @SerializedName("email"             ) var email            : String? = null,
    @SerializedName("password"          ) var password         : String? = null,
    @SerializedName("notes"             ) var notes            : String? = null,
    @SerializedName("access_level"      ) var accessLevel      : String? = null,
    @SerializedName("pan_id"            ) var panId            : String? = null,
    @SerializedName("aadhaar_id"        ) var aadhaarId        : String? = null,
    @SerializedName("permanent_address" ) var permanentAddress : String? = null,
    @SerializedName("created_at"        ) var createdAt        : String? = null,
    @SerializedName("created_by"        ) var createdBy        : String? = null,
    @SerializedName("updated_at"        ) var updatedAt        : String? = null,
    @SerializedName("updated_by"        ) var updatedBy        : String? = null

)
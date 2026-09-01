package com.prod.evergreen.models

import androidx.room.Entity
import androidx.room.PrimaryKey

data class LoginData (
    val access_token: String? = null,
    val data: User? = null,
    val status: Int? = null,
    val message:String?=null,
    val company_inactive: Boolean? = null
)

data class User (
    val id: Int? = null,
    val name: String? = null,
    val location: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val password: String? = null,
    val notes: String? = null,
    val access_level: String? = null,
    val pan_id: String? = null,
    val aadhaar_id: String? = null,
    val permanent_address: String? = null,
    val created_at: String? = null,
    val created_by: String? = null,
    val updated_at: String? = null,
    val updated_by: String? = null,
    val company_user: List<CompanyUserdata>? = null
)

data class CompanyUserdata (
    val user_link: Long? = null,
    val company_link: Long? = null,
    val assignedAt: String? = null,
    val role: Any? = null,
    val company: Company? = null
)

@Entity(tableName = "users_company")
data class Company (
    @PrimaryKey(autoGenerate = true)
    val localid: Int? = null,
    val id: Int? = null,
    val name: String? = null,
    val branch_name: String? = null,
    val email: String? = null,
    val location: String? = null,

)

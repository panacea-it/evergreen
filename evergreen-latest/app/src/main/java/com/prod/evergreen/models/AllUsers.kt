package com.prod.evergreen.models



data class AllUsers (
    val status: Int? = null,
    val message: String? = null,
    val data: List<Users>? = null,
    val count: Countdata?=null,
)

data class Users (
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
    val permanent_ddress: String? = null,
    val created_at: String? = null,
    val created_by: String? = null,
    val updated_at: String? = null,
    val updated_by: String? = null
)

data class Countdata(
    val eg_super_admin: Int,
    val eg_admin: Int,
    val client_admin: Int,
    val client: Int,
    val technician: Int
)

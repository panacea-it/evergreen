package com.prod.evergreen.models

data class AllEquipmentsData (
    val status: Int? = null,
    val message: String? = null,
    val data: List<Data>? = null
)

data class Data (
    val id: Int? = null,
    val name: String? = null,
    val make: String? = null,
    val model: String? = null,
    val serial_number: String? = null,
    val eg_serial_number: String? = null,
    val image_url: String? = null,
    val specifications: String? = null,
    val manufacturer_date: String? = null,
    val location: String? = null,
    val tm_frequency: String? = null,
    val description: String? = null,
    val company_link: String? = null,
    val created_at: String? = null,
    val created_by: String? = null,
    val updated_at: String? = null,
    val updated_by: String? = null,
    val v: Long? = null
)

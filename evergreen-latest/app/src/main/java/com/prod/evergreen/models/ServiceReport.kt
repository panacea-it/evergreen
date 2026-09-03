package com.prod.evergreen.models

import com.google.gson.annotations.SerializedName

data class ServiceReportsResponse(
    val status: Int? = null,
    val message: String? = null,
    val data: List<ServiceReport>? = null,
    val url: String? = null,
    @SerializedName("pdf_base64") val pdfBase64: String? = null
)

data class ServiceReportResponse(
    val status: Int? = null,
    val message: String? = null,
    val data: ServiceReport? = null,
    val url: String? = null,
    @SerializedName("pdf_base64") val pdfBase64: String? = null
)

data class ServiceReport(
    val id: Int? = null,
    @SerializedName("service_report_number") val serviceReportNumber: String? = null,
    @SerializedName("task_id") val taskId: String? = null,
    @SerializedName("company_link") val companyLink: Int? = null,
    @SerializedName("company_name") val companyName: String? = null,
    @SerializedName("company_branch") val companyBranch: String? = null,
    val location: String? = null,
    @SerializedName("company_email") val companyEmail: String? = null,
    @SerializedName("raised_by_name") val raisedByName: String? = null,
    @SerializedName("raised_by_phone") val raisedByPhone: String? = null,
    @SerializedName("raised_by_email") val raisedByEmail: String? = null,
    @SerializedName("raised_by_note") val raisedByNote: String? = null,
    @SerializedName("technician_name") val technicianName: String? = null,
    @SerializedName("technician_phone") val technicianPhone: String? = null,
    @SerializedName("technician_note") val technicianNote: String? = null,
    @SerializedName("equipment_name") val equipmentName: String? = null,
    @SerializedName("equipment_make") val equipmentMake: String? = null,
    @SerializedName("equipment_model") val equipmentModel: String? = null,
    @SerializedName("equipment_serial_number") val equipmentSerialNumber: String? = null,
    @SerializedName("equipment_eg_serial_number") val equipmentEgSerialNumber: String? = null,
    @SerializedName("equipment_location") val equipmentLocation: String? = null,
    @SerializedName("call_type") val callType: String? = null,
    val issue: String? = null,
    @SerializedName("issue_description") val issueDescription: String? = null,
    @SerializedName("service_details") val serviceDetails: String? = null,
    @SerializedName("timeline_events") val timelineEvents: List<ServiceReportEvent>? = emptyList(),
    @SerializedName("customer_serviced_to_satisfaction") val servicedToSatisfaction: String? = null,
    @SerializedName("equipment_running_smoothly") val runningSmoothly: String? = null,
    @SerializedName("customer_comments") val customerComments: String? = null,
    val rating: String? = null,
    @SerializedName("cost") val costRaw: Any? = null,
    val status: String? = null,
    @SerializedName("generated_at") val generatedAt: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("created_by") val createdBy: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
) {
    val cost: Double?
        get() = when (val value = costRaw) {
            is Number -> value.toDouble()
            is String -> value.replace(",", "").replace("₹", "").trim().toDoubleOrNull()
            else -> null
        }
}

data class ServiceReportEvent(
    val date: String? = null,
    val time: String? = null,
    val title: String? = null,
    val description: String? = null
)

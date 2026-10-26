package com.prod.evergreen.helper

/** Mirrors the backend `company_service_type` and `amc_type` enums. Only AMC companies have an AMC type and contract dates. */
object CompanyServiceType {
    const val AMC = "amc"

    val serviceTypes = listOf(AMC to "AMC", "non_amc" to "Non-AMC", "regular" to "Regular")
    val amcTypes = listOf("c_amc" to "C AMC", "nc_amc" to "NC AMC")

    fun serviceLabel(value: String?): String? = serviceTypes.firstOrNull { it.first == value }?.second

    fun amcLabel(value: String?): String? = amcTypes.firstOrNull { it.first == value }?.second

    /** "AMC · C AMC", "Non-AMC", or null when the company has no service type yet. */
    fun displayLabel(serviceType: String?, amcType: String?): String? {
        val service = serviceLabel(serviceType) ?: return null
        val amc = amcLabel(amcType)
        return if (serviceType == AMC && amc != null) "$service · $amc" else service
    }
}

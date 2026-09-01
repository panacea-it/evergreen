package com.prod.evergreen.helper

import android.content.Context
import android.content.Intent
import com.google.gson.Gson
import com.prod.evergreen.activities.AddEquipment
import com.prod.evergreen.activities.EquipmentDetails
import com.prod.evergreen.models.CompanyDataResponse
import com.prod.evergreen.models.Data
import com.prod.evergreen.models.ResponseData

object EquipmentEditor {
    fun openDetails(context: Context, equipment: Data) {
        context.startActivity(
            Intent(context, EquipmentDetails::class.java)
                .putExtra("eq_id", equipment.id)
                .putExtra("eq_sn", equipment.eg_serial_number)
        )
    }

    fun openEdit(
        context: Context,
        equipment: Data,
        fallbackCompanyName: String? = null,
        fallbackCompanyId: Int? = null
    ) {
        val companyId = equipment.company?.id
            ?: equipment.company_link?.toIntOrNull()
            ?: fallbackCompanyId
        val companyName = equipment.company?.name ?: fallbackCompanyName
        val payload = ResponseData(
            id = equipment.id,
            name = equipment.name,
            make = equipment.make,
            model = equipment.model,
            serialNumber = equipment.serial_number,
            specifications = equipment.specifications ?: equipment.serial_number,
            imageUrl = equipment.image_url,
            manufacturerDate = equipment.manufacturer_date,
            location = equipment.location,
            tmFrequency = equipment.tm_frequency,
            description = equipment.description,
            companyLink = companyId,
            egserialnumber = equipment.eg_serial_number,
            company = CompanyDataResponse(
                id = companyId,
                name = companyName
            )
        )
        context.startActivity(
            Intent(context, AddEquipment::class.java)
                .putExtra("equipment_data", Gson().toJson(payload))
        )
    }
}

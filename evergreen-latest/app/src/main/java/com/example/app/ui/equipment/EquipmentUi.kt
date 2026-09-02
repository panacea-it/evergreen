package com.example.app.ui.equipment

import com.prod.evergreen.models.Data

val PM_FREQUENCY_OPTIONS = linkedMapOf(
    "Daily" to "daily",
    "Weekly" to "weekly",
    "Bi-Weekly" to "biweekly",
    "Monthly" to "monthly",
    "Bi-Monthly" to "bi_monthly",
    "Tri-Monthly" to "tri_monthly",
    "Quarterly" to "quarterly",
    "Semi-Annual" to "semi_annual",
    "Annual" to "annual"
)

fun pmFrequencyLabel(value: String?): String {
    if (value.isNullOrBlank()) return ""
    return PM_FREQUENCY_OPTIONS.entries
        .firstOrNull { it.value.equals(value, ignoreCase = true) }
        ?.key
        ?: value
}

fun Data.toEquipmentItem(): EquipmentItem {
    val stableId = id?.toString()
        ?: listOfNotNull(name, serial_number, model, location)
            .joinToString("-")
            .ifBlank { "eq-${hashCode()}" }
    return EquipmentItem(
        id = stableId,
        name = name.orEmpty(),
        description = description.orEmpty().ifBlank { make.orEmpty() },
        modelNumber = model.orEmpty(),
        location = location.orEmpty(),
        serialNumber = serial_number.orEmpty(),
        maintenanceFrequency = pmFrequencyLabel(tm_frequency),
        imageUrl = image_url,
        isActive = isActive()
    )
}

fun List<Data>.toUiEquipment(query: String, activeOnly: Boolean? = null): List<EquipmentItem> {
    val needle = query.trim()
    return filter { equipment ->
        val matchesFilter = when (activeOnly) {
            null -> true
            true -> equipment.isActive()
            false -> !equipment.isActive()
        }
        val matchesQuery = needle.isEmpty() ||
            equipment.name.orEmpty().contains(needle, ignoreCase = true) ||
            equipment.model.orEmpty().contains(needle, ignoreCase = true) ||
            equipment.location.orEmpty().contains(needle, ignoreCase = true) ||
            equipment.serial_number.orEmpty().contains(needle, ignoreCase = true) ||
            equipment.make.orEmpty().contains(needle, ignoreCase = true)
        matchesFilter && matchesQuery
    }.map { it.toEquipmentItem() }
}

fun List<Data>.findByUiId(item: EquipmentItem): Data? {
    return firstOrNull { it.id?.toString() == item.id }
        ?: firstOrNull { it.toEquipmentItem().id == item.id }
}

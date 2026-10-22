package com.prod.evergreen.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.prod.evergreen.databinding.ItemEquipmentUploadReviewBinding
import com.prod.evergreen.helper.EquipmentWorkbookRecord

class EquipmentUploadReviewAdapter(
    private val onEdit: (Int) -> Unit
) : RecyclerView.Adapter<EquipmentUploadReviewAdapter.ViewHolder>() {
    private val records = mutableListOf<EquipmentWorkbookRecord>()

    fun submit(records: List<EquipmentWorkbookRecord>) {
        this.records.clear()
        this.records.addAll(records)
        notifyDataSetChanged()
    }

    fun recordAt(position: Int): EquipmentWorkbookRecord = records[position]

    fun update(position: Int, record: EquipmentWorkbookRecord) {
        records[position] = record
        notifyItemChanged(position)
    }

    fun allRecords(): List<EquipmentWorkbookRecord> = records.toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEquipmentUploadReviewBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(records[position]) { onEdit(holder.adapterPosition) }
    }

    override fun getItemCount(): Int = records.size

    class ViewHolder(private val binding: ItemEquipmentUploadReviewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(record: EquipmentWorkbookRecord, onEdit: () -> Unit) {
            binding.tvEquipmentName.text = record.name.ifBlank { "Unnamed equipment" }
            binding.tvEquipmentSummary.text = listOf(
                "Make: ${record.make.ifBlank { "-" }}",
                "Model: ${record.model.ifBlank { "-" }}",
                "Location: ${record.location.ifBlank { "-" }}"
            ).joinToString("  ·  ")
            binding.tvEquipmentDetails.text = listOfNotNull(
                record.specifications.takeIf(String::isNotBlank)?.let { "Specifications: $it" },
                record.description.takeIf(String::isNotBlank)?.let { "Description: $it" },
                record.tmFrequency.takeIf(String::isNotBlank)?.let { "Maintenance: $it" },
                record.manufacturerDate.takeIf(String::isNotBlank)?.let { "Manufactured: $it" },
                record.egSerialNumber.takeIf(String::isNotBlank)?.let { "EG serial: $it" }
            ).joinToString("  ·  ").ifBlank { "No optional details" }
            binding.btnEditEquipment.setOnClickListener { onEdit() }
        }
    }
}

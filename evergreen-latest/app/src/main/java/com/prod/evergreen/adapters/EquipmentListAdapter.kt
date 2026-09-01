package com.prod.evergreen.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.prod.evergreen.api.Constants
import com.prod.evergreen.databinding.EquipmentItemsBinding
import com.prod.evergreen.helper.SharedPreferencesHelper
import com.prod.evergreen.models.Data

class EquipmentListAdapter(
    val sharedPreferencesHelper: SharedPreferencesHelper,
    val onViewClick: (Data) -> Unit,
    val onActionClick: (Data) -> Unit
) : RecyclerView.Adapter<EquipmentListAdapter.ViewHolder>(), Filterable {
    private var filteredTaskList: List<Data> = listOf()
    private var eqpmntList: List<Data> = listOf()

    class ViewHolder(val binding: EquipmentItemsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Data, onViewClick: (Data) -> Unit, onActionClick: (Data) -> Unit) {
            binding.viewMore.setOnClickListener { onViewClick(data) }
            binding.equipmentMenu.setOnClickListener { onActionClick(data) }
            binding.sNumber.text = data.serial_number
            binding.make.text = "Make: ${data.make?.takeIf { it.isNotBlank() } ?: "-"}"
            binding.name.text = if (data.isActive()) data.name else "${data.name} (Inactive)"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = EquipmentItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = filteredTaskList[position]
        holder.bind(item, onViewClick, onActionClick)
        if (!item.image_url.isNullOrBlank()) {
            Glide.with(holder.binding.image.context)
                .load(Constants.BASE_URL + item.image_url)
                .into(holder.binding.image)
        }
    }

    override fun getItemCount(): Int {
        return filteredTaskList.size
    }

    fun addData(data: List<Data>?) {
        eqpmntList = data?.toMutableList() ?: mutableListOf()
        filteredTaskList = eqpmntList
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.lowercase()?.trim() ?: ""
                filteredTaskList = if (query.isEmpty()) {
                    eqpmntList
                } else {
                    eqpmntList.filter {
                        it.name.orEmpty().contains(query, ignoreCase = true) ||
                            it.serial_number.orEmpty().contains(query, ignoreCase = true) ||
                            it.make.orEmpty().contains(query, ignoreCase = true)
                    }
                }
                return FilterResults().apply { values = filteredTaskList }
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredTaskList = (results?.values as? List<Data>).orEmpty()
                notifyDataSetChanged()
            }
        }
    }
}

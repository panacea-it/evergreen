package com.prod.evergreen.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.prod.evergreen.api.Constants
import com.prod.evergreen.databinding.EquipmentItemsBinding
import com.prod.evergreen.helper.ConstantValues
import com.prod.evergreen.helper.SharedPreferencesHelper
import com.prod.evergreen.models.Data

class EquipmentListAdapter(val sharedPreferencesHelper: SharedPreferencesHelper,val CallBack: (Data) -> Unit) : RecyclerView.Adapter<EquipmentListAdapter.ViewHolder>(), Filterable {
    private var filteredTaskList: List<Data> = listOf()
    private var eqpmntList: List<Data> = listOf()

    class ViewHolder(val binding: EquipmentItemsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Data, callback: (Data) -> Unit) {
            binding.viewMore.setOnClickListener {
                callback.invoke(data)
            }
            binding.sNumber.text = data.serial_number
            binding.name.text = data.name
            // Uncomment and set the correct image field if necessary

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = EquipmentItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(filteredTaskList[position], CallBack)
        if (filteredTaskList[position].image_url!=null) {
            Glide.with(holder.binding.image.context).load(Constants.BASE_URL+filteredTaskList[position].image_url).into(holder.binding.image)
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
                        it.name!!.contains(query, ignoreCase = true) ||
                                it.serial_number!!.contains(query, ignoreCase = true)
                    }
                }
                return FilterResults().apply { values = filteredTaskList }
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredTaskList = results?.values as List<Data>
                notifyDataSetChanged()
            }
        }
    }
}

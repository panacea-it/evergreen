package com.prod.evergreen.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.prod.evergreen.R
import com.prod.evergreen.models.AMCData
import java.util.*
import kotlin.collections.ArrayList

class ItemAdapter(private val items: List<AMCData>, private val onItemClick: (AMCData) -> Unit) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>(), Filterable {

    private var filteredItems: MutableList<AMCData> = ArrayList(items)

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.amc_item_view, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = filteredItems[position]
        holder.textView.text = item.name+"(${item.branchName})"
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount() = filteredItems.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList = if (constraint.isNullOrEmpty()) {
                    items
                } else {
                    val filterPattern = constraint.toString().toLowerCase(Locale.ROOT).trim()
                    items.filter {
                        it.name!!.lowercase(Locale.ROOT).contains(filterPattern)
                    }
                }

                val results = FilterResults()
                results.values = filteredList
                return results
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredItems = results?.values as MutableList<AMCData>
                notifyDataSetChanged()
            }
        }
    }
}

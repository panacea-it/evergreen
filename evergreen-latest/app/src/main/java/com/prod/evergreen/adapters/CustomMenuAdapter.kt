package com.prod.evergreen.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.prod.evergreen.models.ListItem
import com.prod.evergreen.databinding.ListMenuItemBinding

class CustomMenuAdapter(private val items: List<ListItem>, val callback: (ListItem) -> Unit) :
    RecyclerView.Adapter<CustomMenuAdapter.ViewHolder>() {

    class ViewHolder(val binding: ListMenuItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListMenuItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.textView.text = item.name
        holder.binding.imageView.setImageResource(item.imageResId)
        holder.binding.imageView.setColorFilter(Color.parseColor("#2057A6"))
        holder.itemView.setOnClickListener {
            callback(items[position])
        }
    }

    override fun getItemCount(): Int = items.size
}

package com.prod.evergreen.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.prod.evergreen.R

class AtachmentAdapter(private val imageList: List<String>, private val onRemove: (Int) -> Unit) : RecyclerView.Adapter<AtachmentAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageName: TextView = itemView.findViewById(R.id.imageName)
        val removeButton: ImageView = itemView.findViewById(R.id.removeButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.image_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageName = imageList[position]
        holder.imageName.text = imageName
        holder.removeButton.setOnClickListener {
            onRemove(position)
        }
    }

    override fun getItemCount(): Int = imageList.size
}

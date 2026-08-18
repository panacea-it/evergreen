package com.prod.evergreen.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.prod.evergreen.databinding.AmcItemsBinding
import com.prod.evergreen.models.AMCData
import com.prod.evergreen.models.Users

class UsersListAdapter(): RecyclerView.Adapter<UsersListAdapter.viewholder>() {

    var userdata = mutableListOf<Users>()
    class  viewholder(val binding: AmcItemsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun binding(users: Users) {

            binding.name.text=users.name
            binding.branch.text=users.phone
            binding.place.text=users.location

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersListAdapter.viewholder {
        val itemView = AmcItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return viewholder(itemView)
    }

    override fun onBindViewHolder(holder: UsersListAdapter.viewholder, position: Int) {
holder.binding(userdata[position])

    }

    override fun getItemCount(): Int {
        return userdata.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addData(data: List<Users>) {
        this.userdata=data.toMutableList()?: mutableListOf()
        notifyDataSetChanged()
    }
}
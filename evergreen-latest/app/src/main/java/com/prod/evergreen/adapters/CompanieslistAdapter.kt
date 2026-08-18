package com.prod.evergreen.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.prod.evergreen.R
import com.prod.evergreen.api.Constants
import com.prod.evergreen.databinding.CompaniesListItemsBinding
import com.prod.evergreen.helper.DateConverter
import com.prod.evergreen.helper.SharedPreferencesHelper
import com.prod.evergreen.models.AMCData



class CompanieslistAdapter(val sharedPreferencesHelper: SharedPreferencesHelper,val callback: (Int,String)->Unit): RecyclerView.Adapter<CompanieslistAdapter.viewHolder>(),Filterable {

    private var amcList: List<AMCData> = listOf()
    private var filteredTaskList: List<AMCData> = listOf()
    class viewHolder(val binding: CompaniesListItemsBinding) : RecyclerView.ViewHolder(binding.root) {
         fun bind(datum: AMCData) {

             binding.endDate.text=datum.endDate
             binding.statDate.text=datum.startDate
//             binding.cEmail.text="Email ID : "+datum.email
             if (datum.pocDetails!=null) {
                 binding.pocName.text = "Name :" + datum.pocDetails!!.user!!.name
              binding.pocMail.text="Email ID :"+datum.pocDetails!!.user!!.email
              binding.pocMobile.text="Mobile :"+datum.pocDetails!!.user!!.phone
             }
             binding.location.text="Location : "+datum.branchName
             binding.companyName.text="Name : "+datum.name
             binding.statDate.text="Start Date : "+DateConverter.convertToLocalUtcAndFormat(datum.startDate!!)
             binding.endDate.text="End Date : "+DateConverter.convertToLocalUtcAndFormat(datum.endDate!!)


        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        val binding = CompaniesListItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return viewHolder(binding)
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int) {
           holder.bind(filteredTaskList[position])
        Glide.with(holder.binding.image.context). load(Constants.BASE_URL+filteredTaskList[position].logo).error(R.drawable.place_holder1).placeholder(R.drawable.place_holder1).into(holder.binding.image)
        holder.itemView.setOnClickListener {
            callback(filteredTaskList[position].id!!,filteredTaskList[position].name!!)
        }

    }

    override fun getItemCount(): Int {
        return filteredTaskList.size
    }

    fun addData(data: List<AMCData>?) {
        this.amcList=data?.toMutableList()?: mutableListOf()
        this.filteredTaskList=amcList
        notifyDataSetChanged()
    }
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.lowercase()?.trim() ?: ""
                filteredTaskList = if (query.isEmpty()) {
                    amcList
                } else {
                    amcList.filter {
                        it.name!!.contains(query, ignoreCase = true)
                                it.branchName!!.contains(query, ignoreCase = true) ||
                                it.location!!.contains(query, ignoreCase = true)||it.name.contains(query, ignoreCase = true)
                    }
                }
                return FilterResults().apply { values = filteredTaskList }
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredTaskList = results?.values as List<AMCData>
                notifyDataSetChanged()
            }
        }
    }


}
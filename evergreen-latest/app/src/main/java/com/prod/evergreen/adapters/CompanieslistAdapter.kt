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
import com.prod.evergreen.models.AMCData
import com.prod.evergreen.models.isCompanyActive

class CompanieslistAdapter(
    private val onCompanyClick: (Int, String) -> Unit,
    private val onCompanyActionClick: (AMCData) -> Unit
) : RecyclerView.Adapter<CompanieslistAdapter.ViewHolder>(), Filterable {

    private var amcList: List<AMCData> = emptyList()
    private var filteredCompanyList: List<AMCData> = emptyList()

    class ViewHolder(val binding: CompaniesListItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(datum: AMCData) {
            binding.location.text = "Location : ${datum.branchName.orEmpty()}"
            val inactiveLabel = if (datum.isCompanyActive()) "" else "  (Inactive)"
            binding.companyName.text = "Name : ${datum.name.orEmpty()}$inactiveLabel"
            binding.statDate.text = "Start Date : ${formatDate(datum.startDate)}"
            binding.endDate.text = "End Date : ${formatDate(datum.endDate)}"

            val pocUser = datum.pocDetails?.user
            binding.pocName.text = "Name : ${pocUser?.name.orEmpty()}"
            binding.pocMail.text = "Email ID : ${pocUser?.email.orEmpty()}"
            binding.pocMobile.text = "Mobile : ${pocUser?.phone.orEmpty()}"
        }

        private fun formatDate(date: String?): String {
            if (date.isNullOrBlank()) return "-"
            return DateConverter.convertToLocalUtcAndFormat(date)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            CompaniesListItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = filteredCompanyList[position]
        holder.bind(item)
        Glide.with(holder.itemView.context)
            .load(Constants.BASE_URL + item.logo)
            .error(R.drawable.place_holder1)
            .placeholder(R.drawable.place_holder1)
            .into(holder.binding.image)

        holder.itemView.setOnClickListener {
            val id = item.id ?: return@setOnClickListener
            onCompanyClick(id, item.name.orEmpty())
        }
        holder.binding.companyMenu.setOnClickListener {
            onCompanyActionClick(item)
        }
    }

    override fun getItemCount(): Int = filteredCompanyList.size

    fun addData(data: List<AMCData>?) {
        amcList = data.orEmpty()
        filteredCompanyList = amcList
        notifyDataSetChanged()
    }

    fun removeCompanyById(companyId: Int) {
        amcList = amcList.filter { it.id != companyId }
        filteredCompanyList = filteredCompanyList.filter { it.id != companyId }
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.lowercase()?.trim().orEmpty()
                filteredCompanyList = if (query.isEmpty()) {
                    amcList
                } else {
                    amcList.filter { company ->
                        company.name.orEmpty().contains(query, ignoreCase = true) ||
                            company.branchName.orEmpty().contains(query, ignoreCase = true) ||
                            company.location.orEmpty().contains(query, ignoreCase = true)
                    }
                }
                return FilterResults().apply { values = filteredCompanyList }
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredCompanyList = (results?.values as? List<AMCData>).orEmpty()
                notifyDataSetChanged()
            }
        }
    }
}

package com.prod.evergreen.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.prod.evergreen.api.Constants
import com.prod.evergreen.databinding.TaskItemBinding
import com.prod.evergreen.helper.ConstantValues
import com.prod.evergreen.helper.DateConverter
import com.prod.evergreen.helper.SharedPreferencesHelper
import com.prod.evergreen.models.TaskCreated

class AssigningTasksStatusAdapter( val sharedPreferencesHelper: SharedPreferencesHelper,
    val accessType: String,
    val taskData: (TaskCreated) -> Unit,
    val taskDataMore: (TaskCreated) -> Unit,
    val settohold: (TaskCreated) -> Unit,
    val downloadfile: (TaskCreated) -> Unit, val editReson: (TaskCreated) -> Unit,
    val onActionClick: (TaskCreated) -> Unit = {},
    val assignTechnician: (TaskCreated) -> Unit = {}
) : RecyclerView.Adapter<AssigningTasksStatusAdapter.ViewHolder>(), Filterable {

    private var filteredTaskList: List<TaskCreated> = listOf()
    var taskslist: List<TaskCreated> = listOf()

    class ViewHolder(val binding: TaskItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(datum: TaskCreated, accessType: String) {
            binding.title.text = datum.task?.name ?: "-"
            binding.description.text = datum.task?.description ?: "-"
            binding.eqName.text = datum.task?.equipment?.name ?: "-"
            binding.eqSno.text = datum.task?.equipment?.serialNumber ?: "-"

            binding.ticketNo.text = datum.task?.ticketNo ?: "-"
            binding.company.text = datum.task?.equipment?.company?.name ?: "-"

            binding.status.text = when (datum.status) {
                "open" -> "Not Started"
                "in_progress" -> "In Progress"
                "hold" -> "Hold"
                "closed" -> "Closed"
                else -> datum.status.orEmpty()
            }

            if (com.prod.evergreen.helper.RoleAccess.isUnassigned(datum.technicianLink) || datum.technician == null) {
                binding.llTechnician.visibility = View.GONE
            } else {
                binding.llTechnician.visibility = View.VISIBLE
                binding.technician.text = datum.technician.name ?: "-"
            }

            binding.createdDate.text = DateConverter.convertToLocalUtcAndFormat(datum.task?.createdAt)
            binding.days.text = DateConverter.getTimeAgo(datum.task?.createdAt)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = TaskItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
      try {
        val datum = filteredTaskList.getOrNull(position) ?: return
        val imageUrl = datum.task?.image?.firstOrNull()
        if (!imageUrl.isNullOrBlank()) {
            Glide.with(holder.binding.image.context)
                .load(com.prod.evergreen.helper.MediaUrl.resolve(imageUrl))
                .into(holder.binding.image)
        }



        holder.bind(datum, accessType)
        if (accessType == "technician") {
            if (datum.status != "closed") {
                holder.binding.taskStatusUpdater.visibility = View.VISIBLE
                holder.binding.tvHold.visibility = View.INVISIBLE
               if (datum.technicianLink!=null){
                   holder.binding.tvHold.visibility = View.VISIBLE
               }

                if (datum.status == "hold"){
                    holder.binding.taskStatusUpdater.visibility = View.VISIBLE
                    holder.binding.editReason.visibility = View.VISIBLE
                    holder.binding.tvHold.visibility = View.INVISIBLE
                    if(datum.reason!=null){
                        holder.binding.llReson.visibility=View.VISIBLE
                        holder.binding.tvReason.text=datum.reason
                    }
                    else{
                        holder.binding.llReson.visibility=View.GONE
                    }
                }
                holder.binding.taskStatusUpdater.text = when {
                    com.prod.evergreen.helper.RoleAccess.isUnassigned(datum.technicianLink) -> "Accept"
                    datum.status == "open" -> "Move to InProgress"
                    datum.status == "in_progress" && datum.task?.followUp == null -> "Move to Done"
                    datum.status == "in_progress" && datum.task?.followUp != null -> "Move to Done"
                    else -> "Move to In Progress"
                }
                if (datum.status == "in_progress" && datum.task?.followUp != null){

                    holder.binding.taskStatusUpdater.visibility = View.INVISIBLE
                    holder.binding.tvHold.visibility = View.INVISIBLE
                    holder.binding.editReason.visibility = View.VISIBLE
                    holder.binding.content.visibility = View.VISIBLE
                    holder.binding.content.text="Awaiting for client response"
                }


            } else {
                holder.binding.actionBtn.visibility = View.GONE
                holder.binding.taskStatusUpdater.visibility = View.GONE
                holder.binding.tvHold.visibility = View.GONE
                holder.binding.content.visibility = View.GONE
                holder.binding.completed.visibility = View.VISIBLE
                holder.binding.download.visibility = View.GONE
            }
        } else {
            if (datum.status != "closed") {
                if(datum.status == "in_progress" && datum.task?.followUp != null){
                    holder.binding.tvHold.visibility = View.INVISIBLE
                }
                val canAssign = com.prod.evergreen.helper.RoleAccess.canAssignTechnician(accessType)
                val unassigned = com.prod.evergreen.helper.RoleAccess.isUnassigned(datum.technicianLink)
                val awaitingApproval = datum.status == "in_progress" && datum.task?.followUp != null
                holder.binding.taskStatusUpdater.visibility = when {
                    awaitingApproval -> View.VISIBLE
                    canAssign -> View.VISIBLE
                    else -> View.GONE
                }
                holder.binding.taskStatusUpdater.text = when {
                    awaitingApproval -> "Approval"
                    canAssign && unassigned -> "Assign Technician"
                    canAssign -> "Reassign Technician"
                    else -> ""
                }

                if (datum.status == "hold"){

                    if(datum.reason!=null){
                        holder.binding.llReson.visibility=View.VISIBLE

                        holder.binding.tvReason.text=datum.reason
                    }
                    else{
                        holder.binding.llReson.visibility=View.GONE
                    }
                }
            } else {
                holder.binding.actionBtn.visibility = View.GONE
                holder.binding.taskStatusUpdater.visibility = View.GONE
                holder.binding.tvHold.visibility = View.GONE
                holder.binding.content.visibility = View.GONE
                holder.binding.completed.visibility = View.VISIBLE
                holder.binding.download.visibility = View.GONE
            }
        }

        holder.binding.taskStatusUpdater.setOnClickListener {
            val canAssign = com.prod.evergreen.helper.RoleAccess.canAssignTechnician(accessType)
            val awaitingApproval = datum.status == "in_progress" && datum.task?.followUp != null
            if (canAssign && datum.status != "closed" && !awaitingApproval) {
                assignTechnician(datum)
            } else {
                taskData(datum)
            }
        }

        holder.binding.download.setOnClickListener {
            downloadfile(datum)
        }


        holder.binding.tvHold.setOnClickListener {
            settohold(datum)
        }
        holder.binding.editReason.setOnClickListener {
            editReson(datum)
        }




        holder.binding.btnServiceReport.visibility =
            if (com.prod.evergreen.helper.RoleAccess.canGenerateServiceReport(accessType)) View.VISIBLE else View.GONE
        holder.binding.btnServiceReport.setOnClickListener { downloadfile(datum) }
        holder.binding.taskMenu.visibility =
            if (com.prod.evergreen.helper.RoleAccess.canManageTasks(accessType)) View.VISIBLE else View.GONE
        holder.binding.taskMenu.setOnClickListener { view ->
            view.isEnabled = false
            try {
                onActionClick(datum)
            } finally {
                view.postDelayed({ view.isEnabled = true }, 400)
            }
        }
        holder.itemView.setOnClickListener {
          taskDataMore(datum)
      }
      } catch (_: Exception) {
          holder.binding.title.text = "-"
      }
    }

    override fun getItemCount(): Int = filteredTaskList.size

    fun addData(data: List<TaskCreated>?) {
        taskslist = (data ?: emptyList()).sortedByDescending { it.task?.createdAt.orEmpty() }
        filteredTaskList = taskslist
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.lowercase()?.trim() ?: ""
                filteredTaskList = if (query.isEmpty()) {
                    taskslist
                } else {
                    taskslist.filter {
                        it.task?.name.orEmpty().contains(query, ignoreCase = true) ||
                            it.task?.equipment?.name.orEmpty().contains(query, ignoreCase = true) ||
                            it.task?.equipment?.company?.name.orEmpty().contains(query, ignoreCase = true)
                    }
                }
                return FilterResults().apply { values = filteredTaskList }
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredTaskList = (results?.values as? List<TaskCreated>).orEmpty()
                notifyDataSetChanged()
            }
        }
    }

}


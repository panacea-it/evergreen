package com.prod.evergreen.adapters


import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.prod.evergreen.databinding.HistoryItemsBinding
import com.prod.evergreen.helper.DateConverter
import com.prod.evergreen.models.TasksItem


class EqupmentHistroyAdapter(val data:(TasksItem)->Unit,val selfAssign:(TasksItem)->Unit,val downloadFile:(TasksItem)->Unit) : RecyclerView.Adapter<EqupmentHistroyAdapter.ViewHolder>() {
    private val VIEW_TYPE_NORMAL = 0
    private val VIEW_TYPE_LAST_ITEM = 1
    private  var accesstype:String?=null
    private  var userid:Int?=null
    var tasksList = mutableListOf<TasksItem>()
    class ViewHolder(val binding: HistoryItemsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(tasksItem: TasksItem) {

            binding.date.text= DateConverter.convertToLocalUtcAndFormat(tasksItem.createdAt!!)
            binding.status.text=tasksItem.status
            binding.title.text=tasksItem.task!!.name

            if(tasksItem.technicianLink!=null){
                binding.technician.text=tasksItem.technician!!.name
            }
            else{
                binding.technician.text="-"

            }


        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = HistoryItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(tasksList[position])
        if (accesstype!=null){
        if (accesstype=="technician") {
            if (tasksList[position].technicianLink == null) {
                holder.binding.selfAssign.visibility = View.VISIBLE

            } else {
                if (tasksList[position].technicianLink!=userid){
                    Log.d("daddadad","${position}-----$userid")
                    if(tasksList[position].status == "open" || tasksList[position].status == "hold"){
                        holder.binding.selfAssign.visibility = View.VISIBLE
                    }
                }
                else{
                    Log.d("daddadadqqqq","${position}-----$userid")
                    holder.binding.selfAssign.visibility = View.GONE
                }

            }
        }

        }
        holder.binding.selfAssign.setOnClickListener {
            selfAssign(tasksList[position])
        }
        holder.itemView.setOnClickListener {
            data(tasksList[position])
        }

        if (com.prod.evergreen.helper.RoleAccess.canGenerateServiceReport(accesstype)) {
            holder.binding.downloadFile.visibility = View.VISIBLE
        } else {
            holder.binding.downloadFile.visibility = View.GONE
        }
        holder.binding.downloadFile.setOnClickListener {
            downloadFile(tasksList[position])
        }


    }

    override fun getItemCount(): Int = tasksList.size

    fun addData(tasks: List<TasksItem>?, accesstype: String?=null,userid: Int?=null) {
     this.tasksList=tasks?.toMutableList()?: mutableListOf()
        this.accesstype=accesstype
        this.userid=userid
        notifyDataSetChanged()
    }
}

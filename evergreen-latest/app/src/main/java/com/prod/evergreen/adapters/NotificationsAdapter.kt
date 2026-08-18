package com.prod.evergreen.adapters


import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.prod.evergreen.api.Constants
import com.prod.evergreen.databinding.NotificationlistItemsBinding
import com.prod.evergreen.helper.ConstantValues
import com.prod.evergreen.helper.DateConverter
import com.prod.evergreen.helper.SharedPreferencesHelper
import com.prod.evergreen.models.DataItem


class NotificationsAdapter(val sharedPreferencesHelper: SharedPreferencesHelper,private  var data: List<DataItem>,private  var tmpData:(DataItem)->Unit ) : RecyclerView.Adapter<NotificationsAdapter.ViewHolder>() {

    class ViewHolder(val binding: NotificationlistItemsBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = NotificationlistItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.setOnClickListener {
            if (data[position].acceptedBy==null){
                tmpData(data[position])
            }


        }

holder.binding.apply {
    if (data[position].acceptedBy!=null){
        technician.text="Technician : ${data[position].acceptedBy!!.name}"
    }
    else{
        technician.text="Status : Open"
       // technician.textColors="Status : Open"
    }
//    sNo.text="S.NO : ${data[position].}"
title.text=data[position].title
desc.text=data[position].description
date.text=DateConverter.getTimeAgo(data[position].createdAt!!)
    createdby.text=data[position].createdBy
    if (data[position].imageUrl!=null||data[position].imageUrl!="") {
        Glide.with(image.context)
            .load(Constants.BASE_URL+data[position].imageUrl)
            .listener(object : RequestListener<Drawable> {
                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                   image.visibility= View.VISIBLE
                    return  false
                }

                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    image.visibility = ImageView.GONE
                    return false
                }

            })
            .into(image)
       // Glide.with(image.context).load(Constants.BASE_URL+data[position].imageUrl).into(image)
    }


}
    }

    override fun getItemCount(): Int = data.size
}

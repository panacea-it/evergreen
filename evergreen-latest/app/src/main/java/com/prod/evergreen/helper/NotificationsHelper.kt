package com.prod.evergreen.helper

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.prod.evergreen.R
import com.prod.evergreen.activities.MainActivity

fun showNotification(
    context: Context,
    title: String?,
    body: String?,
    taskLink: String?,
    description: String?,
    imageUrl: String?,
    sno: String?,
    location: String?,
    channel_id: String?,
    action: String? = null
) {

    val sharedPreferences = SharedPreferencesHelper(context)
    sharedPreferences.getValueString(ConstantValues.AuthToken) ?: return
    Log.d("NotificationExtractedData", "Title: $title, Body: $body, Task Link: $taskLink, Description: $description, Image URL: $imageUrl")
    val canAssign = action.equals("assign", ignoreCase = true) ||
        channel_id.equals("evergreen", ignoreCase = true)
    val notificationId = (taskLink ?: System.currentTimeMillis().toString()).hashCode()
    val intent = Intent(context, MainActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        if (channel_id != null || canAssign) {
            putExtra("title", title)
            putExtra("body", description)
            putExtra("task_link", taskLink)
            putExtra("description", description)
            putExtra("sno", sno)
            putExtra("channel_id", channel_id)
            putExtra("location", location)
            putExtra("action", action)
        }
    }

    val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText(body)
            .setBigContentTitle(title)

    val dismissIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            this.action = "ACTION_DISMISS"
            putExtra("notification_id", notificationId)
       }
    val dismissPendingIntent = PendingIntent.getBroadcast(
        context,
        notificationId + 1,
        dismissIntent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
    val pendingIntent = PendingIntent.getActivity(
        context,
        notificationId,
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
    val notificationBuilder = NotificationCompat.Builder(context, channel_id ?: "evergreen_normal")
        .setSmallIcon(R.drawable.ic_add_equipment_icon)
        .setContentTitle(title)
        .setContentText(description)
        .setStyle(bigTextStyle)
        .setColorized(true)
        .setColor(context.getColor(R.color.colorRed))
        .setAllowSystemGeneratedContextualActions(true)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .addAction(R.drawable.ic_tasks_list_icon, "Do later", dismissPendingIntent)
        .setContentIntent(pendingIntent)
        .setDefaults(NotificationCompat.DEFAULT_ALL)

    if (canAssign && !taskLink.isNullOrBlank()) {
        val assignIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            this.action = "ACTION_ASSIGN"
            putExtra("task_link", taskLink)
            putExtra("notification_id", notificationId)
        }
        val assignPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 2,
            assignIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        notificationBuilder.addAction(
            R.drawable.ic_tasks_list_icon,
            "Assign to me",
            assignPendingIntent
        )
    }

    val notificationManager = NotificationManagerCompat.from(context)
    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
        return
    }
    notificationManager.notify(notificationId, notificationBuilder.build())
}

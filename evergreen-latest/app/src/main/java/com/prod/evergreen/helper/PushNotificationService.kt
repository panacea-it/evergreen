package com.prod.evergreen.helper

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class PushNotificationService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("notificationdata", remoteMessage.data.toString())

        if (remoteMessage.data.isNotEmpty()) {
            val title = remoteMessage.data["title"]
            val body = remoteMessage.data["description"]
            val taskLink = remoteMessage.data["task_link"]
            val description = remoteMessage.data["description"]
            val imageUrl: String? = remoteMessage.data["image_url"]
            val sno: String? = remoteMessage.data["Serial_number"]
            val location: String? = remoteMessage.data["location"]
            val channel_id: String? = remoteMessage.data["channel_id"]

          //  Log.d("NotificationExtractedData", "Title: $title, Body: $body, Task Link: $taskLink, Description: $description, Image URL: $imageUrl")
            showNotification(applicationContext,title, body, taskLink, description, imageUrl,sno,location,channel_id)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Log.d("tokenfcm",token)
    }

//    private fun sendNotification(
//        title: String?,
//        body: String?,
//        taskLink: String?,
//        description: String?,
//        imageUrl: String?,
//        sno: String?,
//        location: String?,
//        channel_id: String?
//    ) {
//
//
////        val intent = Intent(this, MainActivity::class.java).apply {
////            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
////            putExtra("title", title)
////            putExtra("body", body)
////            putExtra("task_link", taskLink)
////            putExtra("description", description)
////            putExtra("image_url", imageUrl)
////            putExtra("sno", sno)
////            putExtra("location", location)
////        }
////
////        val pendingIntent = PendingIntent.getActivity(
////            this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
////
////
////        val dismissIntent = Intent(this, NotificationActionReceiver::class.java).apply {
////            action = "ACTION_DISMISS"
////        }
////        val dismissPendingIntent = PendingIntent.getBroadcast(
////            this, 0, dismissIntent, PendingIntent.FLAG_IMMUTABLE)
//////        val soundUri:Uri
//////        if (channel_id=="evergreen") {
//////            soundUri  = Uri.parse("android.resource://" + applicationContext.packageName + "/" + "raw/soft.mp3")
//////        }
//////        else{
//////             soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
//////        }
////        val bigTextStyle = NotificationCompat.BigTextStyle()
////            .bigText(body)
////            .setBigContentTitle(title)
////        val channel: String
////        if (channel_id.equals(CHANNEL_ID)){
////         channel= CHANNEL_ID
////     }
////        else{
////         channel="evergreen"
////        }
////        val notification: Notification = NotificationCompat.Builder(this, channel)
////            .setContentTitle(title)
////            .setContentText(body)
////            .setSmallIcon(R.mipmap.ic_launcher)
////            .setContentIntent(pendingIntent)
////            .setStyle(bigTextStyle)
////            .setAutoCancel(true)
////            .addAction(R.drawable.ic_add_equipment_icon, "Do later", dismissPendingIntent)
////             // Keeps the notification active until action is taken
////            .build()
////
////        val notificationManager = NotificationManagerCompat.from(this)
////        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
////            notificationManager.notify(NOTIFICATION_ID, notification)
////        }
//    }


    companion object {
        private const val CHANNEL_ID = "evergreen_normal"
        private const val NOTIFICATION_ID = 12
    }
}

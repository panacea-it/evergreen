package com.prod.evergreen.helper



import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.prod.evergreen.activities.MainActivity

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            "ACTION_ACCEPT" -> {
                Log.d("NotificationAction", "Accept action received")
                // Handle accept action
                val mainIntent = Intent(context, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                context?.startActivity(mainIntent)
            }
            "ACTION_DISMISS" -> {
                Log.d("NotificationAction", "Dismiss action received")
                // Handle dismiss action
                context?.let {
                    val notificationManager = NotificationManagerCompat.from(it)
                    notificationManager.cancel(NOTIFICATION_ID)
                }
            }
        }
    }
    companion object {
        private const val NOTIFICATION_ID = 1 // Notification ID
    }
}

package com.prod.evergreen.helper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import com.google.gson.JsonObject
import com.prod.evergreen.activities.MainActivity
import com.prod.evergreen.api.RetrofitService
import kotlinx.coroutines.runBlocking

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            "ACTION_ACCEPT" -> {
                Log.d("NotificationAction", "Accept action received")
                val pendingResult = goAsync()
                Thread {
                    try {
                        assignTaskFromNotification(context, intent)
                    } catch (error: Exception) {
                        Log.e("NotificationAction", "Accept from notification failed", error)
                    } finally {
                        pendingResult.finish()
                    }
                }.start()
            }
            "ACTION_ASSIGN" -> {
                val pendingResult = goAsync()
                Thread {
                    try {
                        assignTaskFromNotification(context, intent)
                    } catch (error: Exception) {
                        Log.e("NotificationAction", "Assign from notification failed", error)
                    } finally {
                        pendingResult.finish()
                    }
                }.start()
            }
            "ACTION_DISMISS" -> {
                Log.d("NotificationAction", "Dismiss action received")
                context?.let {
                    val notificationManager = NotificationManagerCompat.from(it)
                    val notificationId = intent.getIntExtra("notification_id", NOTIFICATION_ID)
                    notificationManager.cancel(notificationId)
                }
            }
        }
    }

    private fun assignTaskFromNotification(context: Context?, intent: Intent?) {
        val ctx = context ?: return
        val taskLink = intent?.getStringExtra("task_link")
        val notificationId = intent?.getIntExtra("notification_id", NOTIFICATION_ID) ?: NOTIFICATION_ID
        val prefs = SharedPreferencesHelper(ctx)
        val token = prefs.getValueString(ConstantValues.AuthToken)
        val userId = prefs.getValueInt(ConstantValues.USER_ID)
        if (taskLink.isNullOrBlank() || token.isNullOrBlank() || userId == null || userId == 0) {
            showToast(ctx, "Unable to assign this task")
            return
        }
        val body = JsonObject()
        body.addProperty("task_link", taskLink.toInt())
        body.addProperty("technician_link", userId)
        val response = runBlocking {
            RetrofitService.getInstance(ctx).assignTechnician(body, "Bearer $token")
        }
        if (response.isSuccessful && response.body()?.status_code == 200) {
            NotificationManagerCompat.from(ctx).cancel(notificationId)
            showToast(ctx, response.body()?.message ?: "Task assigned to you")
        } else {
            showToast(ctx, response.body()?.message ?: "Could not assign this task")
        }
    }

    private fun showToast(context: Context, message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 1
    }
}

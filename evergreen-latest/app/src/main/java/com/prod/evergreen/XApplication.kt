package com.prod.evergreen



import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig
import androidx.room.Room
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.prod.evergreen.db.NewsDatabase
import com.prod.evergreen.helper.ConstantValues
import com.prod.evergreen.helper.SharedPreferencesHelper

class XApplication : Application(), CameraXConfig.Provider {
    override fun getCameraXConfig(): CameraXConfig {
        return Camera2Config.defaultConfig()
    }
    private lateinit var database1: DatabaseReference
    companion object {
        lateinit var database: NewsDatabase
    }
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    override fun onCreate() {
        super.onCreate()
        sharedPreferencesHelper=SharedPreferencesHelper(applicationContext)
        createNotificationChannel()
        createNotificationChannel1()
        FirebaseApp.initializeApp(this)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600) // Fetch every hour
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)
       // fetchBaseUrl()
        database = Room.databaseBuilder(applicationContext, NewsDatabase::class.java, "EverGreen")

            //.addMigrations(NewsDatabase.MIGRATION_1_2)
            .build()

//        database1 = FirebaseDatabase.getInstance().reference


//        database1.child("BASEURL").child("baseURL").addValueEventListener(object :
//            ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                val value = dataSnapshot.getValue(String::class.java)
//                Log.d("FirebaseDebug", "onDataChange called with value: $value")
//                if (value != null) {
//                    // Successfully retrieved data
//                    sharedPreferencesHelper.save(ConstantValues.BASE_URL, value)
//                } else {
//                    Log.d("FirebaseDebug", "Value is null")
//                }
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                // Failed to read value
//                Log.e("FirebaseDebug", "onCancelled called with error: ${databaseError.message}")
//                Toast.makeText(applicationContext, "Failed to read data!", Toast.LENGTH_SHORT).show()
//            }
//        })



    }

    private fun createNotificationChannel1() {
        val name = "Ever green tasks"
        val descriptionText = getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel("evergreen_normal", name, importance).apply {
            description = descriptionText
            setImportance(NotificationManager.IMPORTANCE_HIGH)



        }
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotificationChannel() {
        val soundUri= Uri.parse("android.resource://" + packageName + "/" + R.raw.soft)
        //val soundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()
        val name = getString(R.string.channel_name)
        val descriptionText = getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel("evergreen", name, importance).apply {
            description = descriptionText
            setImportance(NotificationManager.IMPORTANCE_HIGH)
            setSound(soundUri,audioAttributes)



        }
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
    private fun fetchBaseUrl() {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val baseUrl = remoteConfig.getString("base_url")
                    // Store the base URL for your API calls
                    sharedPreferencesHelper.save(ConstantValues.BASE_URL, baseUrl)
                   // Toast.makeText(this,baseUrl,Toast.LENGTH_SHORT).show()
                }
            }
    }
}
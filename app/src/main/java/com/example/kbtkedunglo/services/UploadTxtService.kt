package com.example.kbtkedunglo.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.kbtkedunglo.MainActivity
import com.example.kbtkedunglo.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class UploadTxtService : Service() {
    private lateinit var sharedPreferences:SharedPreferences

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
        val context = this
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userId = intent?.getStringExtra("userId")
                val eventId = intent?.getStringExtra("eventId")
                val judulMedal = intent?.getStringExtra("judulMedal")
                val descriptionMedal = intent?.getStringExtra("descriptionMedal")
                Log.d("KBTAPP", "usreId: ${userId}, eventId:$eventId, judulMedal:$judulMedal, description:$descriptionMedal")
                val eventFile = "location_data-${eventId}.txt"
                val directory = File(applicationContext.filesDir, "resource_data")
                val file = File(directory, eventFile)
                if(file.exists()){
                    val fileBody = file.asRequestBody("text/plain".toMediaTypeOrNull())
                    val request = Request.Builder()
                        .url("https://kbt.us.to/events/upload/")
                        .post(
                            MultipartBody.Builder()
                                .setType(MultipartBody.FORM)
                                .addFormDataPart("file", eventFile, fileBody)
                                .addFormDataPart("event_id", eventId as String)
                                .addFormDataPart("user_id", userId as String)
                                .addFormDataPart("title", judulMedal as String)
                                .addFormDataPart("description", descriptionMedal as String)
                                .build())
                        .build()

                    val client = OkHttpClient()
                    val response = client.newCall(request).execute()

                    if (response.isSuccessful) {
                        MedalOfflineDatabase(context).deleteByEvent(eventId.toString().toInt())
                        onUploadDoneEvent("success")
                        showNotification(true,"Berhasil Mengunggah Aktivitas", "berhasil mengunggah aktivitas ke server")
                    } else {
                        onUploadDoneEvent("error")
                        Log.e("KBTAPP", "$response")
                        showNotification(false,"Gagal Mengunggah Aktivitas", "gagal mengunggah aktivitas ke server")
                    }
                }else{
                    onUploadDoneEvent("filenotfound")
                    showNotification(false,"Gagal Mengunggah Aktivitas", "Anda belum medal")
                }
            } catch (e: Exception) {
                onUploadDoneEvent("error")
                showNotification(false,"Gagal Mengunggah Aktivitas", "gagal mengunggah aktivitas ke server")
                Log.e("KBTAPP", "Error during upload", e)
            }
        }

        return START_NOT_STICKY
    }

    fun onUploadDoneEvent(status:String){
        Log.d("KBTAPP", "event bus trigger")
        val eventId = sharedPreferences.getString("event_id", "")
        val eventFile = "location_data-${eventId}.txt"
        val directory = File(this.filesDir, "resource_data")
        val file = File(directory, eventFile)
        if(status != "error"){
            if(status == "success"){
                file.delete()
            }
            MedalOfflineDatabase(this).deleteByEvent(eventId.toString().toInt())
            val editor = sharedPreferences.edit()
            editor.putString("start_event", false.toString())
            editor.putString("file_txt_created", false.toString())
            editor.putString("main_event", false.toString())
            editor.putString("event_id", "")
            editor.putString("event_nama", "")
            editor.putString("event_gpx", "")
            editor.putString("follow_event", false.toString())
            editor.apply()
        }
    }

    private fun createNotification(): Notification {
        val notificationChannelId = "UPLOAD_CHANNEL"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationChannel = NotificationChannel(
                notificationChannelId,
                "Upload Service",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }

        return NotificationCompat.Builder(this, notificationChannelId)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Upload in progress")
            .setContentText("Uploading file...")
            .build()
    }

    private fun showNotification(stt:Boolean, title: String, text: String) {
        val notificationChannelId = "UPLOAD_CHANNEL"
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        val notification = NotificationCompat.Builder(this, notificationChannelId)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    companion object {
        const val NOTIFICATION_ID = 1717
    }
}
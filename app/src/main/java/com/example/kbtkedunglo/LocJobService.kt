package com.example.kbtkedunglo

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import org.json.JSONObject

class LocJobService : JobService() {
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null
    private var refreshRate: Long = 30000L
    private var maxDistance: Float = 20f
    private var urlPost:String = "https://kbt.us.to/tracker/post/"

    override fun onCreate() {
        super.onCreate()
        startForeground(123456789, buildNotification())
        Log.i("KBTAPP", "LocJobService onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            startForeground(123456789, buildNotification())
            val userId = intent?.extras?.getString("userId")
            startLocationUpdates(userId)
        }catch (e:Exception){
            Log.e("KBTAPP", e.message.toString())
        }
        return START_STICKY
    }

    override fun onStartJob(params: JobParameters): Boolean {
        Log.i("KBTAPP", "START JOB")
        return true
    }

    private fun buildNotification(): Notification {
        val channelId = "kbtapp-tracking"
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "KBT APP TRACKING POSITION",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(this, EventFragment::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, "kbtapp-tracking")
            .setContentTitle("KBT APP")
            .setContentText("Mohon jangan tutup aplikasi! Event KBT Sedang Berlangsung")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        return builder.build()
    }


    private fun startLocationUpdates(userId:String?) {
        if(ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) Log.e("KBTAPP", "TIDAK ADA IJIN APP")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(refreshRate)
            .setFastestInterval(refreshRate)
            .setSmallestDisplacement(maxDistance)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult?.lastLocation?.let { location ->
                    Log.i("KBTAPP", "BG TASK: ${location}")
                    uploadCoordinate(location, userId)
                }
            }
        }

        fusedLocationClient?.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun restartLocationUpdates(userId: String?) {
        locationCallback?.let {
            fusedLocationClient?.removeLocationUpdates(it)
        }
        startLocationUpdates(userId)
    }

    private fun uploadCoordinate(location: Location, userId:String?) {
        val apiclient = ApiClient()
        apiclient.postData(
            urlPost,
            """{"event": 4, "kbtuser": ${userId}, "lat": "${location.latitude}", "lon": "${location.longitude}", "alt": "${location.altitude}"}""",
            object :ApiResponseCallback{
                override fun onSuccess(jsonObject: JSONObject) {
                    val refreshratestring = jsonObject.optInt("refresh_rate", 33)
                    val maxdistancestring = jsonObject.optInt("max_distance", 5)
                    val newurlPost = jsonObject.getString("url_post")
                    val newRefreshRate = (refreshratestring * 1000L).coerceAtLeast(3000L)
                    val newMaxDistance = maxdistancestring.toFloat().coerceAtLeast(2f)
                    Log.i("KBTAPP", "Response: $jsonObject")
                    Log.i("KBTAPP", "refreshrate: ${refreshRate.toString()}")
                    Log.i("KBTAPP", "maxdistance: ${maxDistance.toString()}")
                    Log.i("KBTAPP", "maxdistance: ${urlPost}")
                    if (newRefreshRate != refreshRate || newMaxDistance != maxDistance || newurlPost != urlPost) {
                        Log.i("KBTAPP", "restart lokasi interval")
                        refreshRate = newRefreshRate
                        maxDistance = newMaxDistance
                        urlPost = newurlPost
                        restartLocationUpdates(userId)
                    }
                }
                override fun onFailure(error: String) {
                    Log.e("KBTPAPP","Error: $error")
                }
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(STOP_FOREGROUND_DETACH)
        locationCallback?.let {
            fusedLocationClient?.removeLocationUpdates(it)
        }
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(123456789)
    }

    override fun onStopJob(params: JobParameters): Boolean {
        stopForeground(STOP_FOREGROUND_DETACH)
        locationCallback?.let {
            fusedLocationClient?.removeLocationUpdates(it)
        }
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(123456789)
        return true
    }
}

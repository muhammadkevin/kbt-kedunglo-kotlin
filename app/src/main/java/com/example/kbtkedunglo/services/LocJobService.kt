package com.example.kbtkedunglo.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.kbtkedunglo.R
import com.example.kbtkedunglo.pages.record.MedalFragment
import com.example.kbtkedunglo.utilsclass.ApiClient
import com.example.kbtkedunglo.utilsclass.ApiResponseCallback
import com.example.kbtkedunglo.utilsclass.FileCreatedEvent
import com.example.kbtkedunglo.utilsclass.LocationChangeEvent
import com.example.kbtkedunglo.utilsclass.LocationWithSpeedAverageChange
import com.example.kbtkedunglo.utilsclass.PermissionUtils
import com.example.kbtkedunglo.utilsclass.ScreenStatusEvent
import com.example.kbtkedunglo.utilsclass.StatusTimeChangeEvent
import com.example.kbtkedunglo.utilsclass.TimeChangeEvent
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.round

@SuppressLint("SpecifyJobSchedulerIdRange")
class LocJobService : JobService(){
    private lateinit var sharedPreferences: SharedPreferences
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null
    private var refreshRate: Long = 30000L
    private var maxDistance: Float = 20f
    private var maxDistanceScreen:Float = 5F
    private var maxDistanceLive:Int = 5             //interval waktu dalam detik
    private var intervalGps:Long = 5000L
    private var koefisien_interval:Double = 0.7
    private var urlPost: String = "https://kbt.us.to/tracker/post_live/"
    private lateinit var permissionUtils: PermissionUtils
    private var mainEvent: String? = null
    private var scopeuploadCoordinate: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var eventId: String? = null
    private var userId: String? = null
    private var isUploadInProgress = false
    private val coroutineLock = Mutex()
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private lateinit var connectivityManager:ConnectivityManager
    private lateinit var editor:SharedPreferences.Editor
    private var previousLocation: Location? = null
    private var handlerTimer: Handler? = null
    private var runnableTimer: Runnable? = null
    private var startTimeMillis: Long = 0
    private var statusTimer:Boolean = false
    private var isTimerPaused:String = "False"
    private var pausedTimeMillis: Long = 0
    private var allSpeed:ArrayList<Double> = arrayListOf()
    private var layarNyala:Boolean = true

    override fun onCreate() {
        super.onCreate()
        EventBus.getDefault().register(this)
        Log.d("KBTAPP", "on create")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            startForeground(123456789, buildNotification())
            userId = intent?.extras?.getString("userId")
            eventId = intent?.extras?.getString("eventId")
            sharedPreferences =
                applicationContext.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
            mainEvent = sharedPreferences.getString("main_event", "")
            connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {}
                override fun onLost(network: Network) {
                    restartLocationUpdatesBg(userId)
                }
            }
            connectivityManager?.registerDefaultNetworkCallback(networkCallback)
            if(!statusTimer){
                statusTimer = true
                startTimer()
            }
            startLocationUpdatesBg(userId)
        } catch (e: Exception) {
            Log.e("KBTAPP", "KESALAHAN SAAT PERTAMA KALI MEMBUAT NOTIF ${e.message.toString()}")
        }
        return START_STICKY
    }

    override fun onStartJob(params: JobParameters): Boolean {
        Log.d("KBTAPP", "OnStart JOB")
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

        val notificationIntent = Intent(this, MedalFragment::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, "kbtapp-tracking")
            .setContentTitle("KBT APP")
            .setContentText("Mohon jangan tutup aplikasi! Event KBT Sedang Berlangsung")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        return builder.build()
    }

    private fun restartLocationUpdatesBg(userId: String?) {
        Log.d("KBTAPP", "restart nih")
        locationCallback?.let {fusedLocationClient?.removeLocationUpdates(it)}
        startLocationUpdatesBg(userId)
    }
    @SuppressLint("MissingPermission")
    private fun startLocationUpdatesBg(userId: String?) {
        permissionUtils = PermissionUtils(this)
        if (!permissionUtils.hasLocationPermission()) Log.e("KBTAPP", "TIDAK ADA IJIN APP")
        else {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            var locationRequest:LocationRequest
            locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(intervalGps)
                .setFastestInterval(intervalGps)

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    locationResult?.lastLocation?.let { location ->
                        if(previousLocation != null){
                            var pv = previousLocation as Location
                            var distances = location.distanceTo(pv)
                            if(statusTimer){
                                avgSpeed(location)
                                if(distances >= maxDistance){
                                    writeLocationToFile(location)
                                    if (permissionUtils.isInternetConnected()) {
                                        scopeuploadCoordinate.launch {
                                            handleLocationUpload(location)
                                        }
                                    }
                                    previousLocation = location
                                }
                            }else{
                                if(layarNyala) EventBus.getDefault().post(LocationChangeEvent(location))
                            }
                        }else{
                            if(statusTimer){
                                avgSpeed(location)
                                processingHandleLocation(location)
                            }else{
                                if(layarNyala) EventBus.getDefault().post(LocationChangeEvent(location))
                            }
                            previousLocation = location
                        }
                    }
                }
            }

            fusedLocationClient?.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    private fun avgSpeed(location:Location){
        allSpeed.add(location.speed * 3.6)
        val avgSpeed = allSpeed.sum() / allSpeed.size
        if (layarNyala) EventBus.getDefault().post(LocationWithSpeedAverageChange((round(avgSpeed * 10.0) / 10.0).toString(), location))
    }

    //TIMER
    private fun startTimer (){
        Log.e("KBTAPP", "START TIMERNYA")
        if (isTimerPaused == "True") {
            startTimeMillis += System.currentTimeMillis() - pausedTimeMillis
            isTimerPaused = "False"
        } else {
            startTimeMillis = System.currentTimeMillis()
        }
        handlerTimer = Handler(Looper.getMainLooper())
        runnableTimer = object : Runnable {
            override fun run() {
                val currentTimeMillis = System.currentTimeMillis()
                val elapsedTimeSeconds = (currentTimeMillis - startTimeMillis) / 1000
                if(layarNyala) EventBus.getDefault().post(TimeChangeEvent(elapsedTimeSeconds))
                handlerTimer?.postDelayed(this, 1000)
            }
        }
        handlerTimer?.post(runnableTimer as Runnable)
    }
    private fun stopTimer(pause:Boolean) {
        if(pause){
            isTimerPaused = "True"
            pausedTimeMillis = System.currentTimeMillis()
        }else {
            isTimerPaused = "False"
            pausedTimeMillis = 0L
            allSpeed.clear()
        }
        PauseTimer()
        previousLocation = null
        handlerTimer?.let { ht ->
            runnableTimer?.let{ rt ->
                ht.removeCallbacks(rt)
            }
        }
    }
    @SuppressLint("MissingPermission")
    private fun PauseTimer(){
        locationCallback?.let { lc ->
            fusedLocationClient?.removeLocationUpdates(lc)
        }
        permissionUtils = PermissionUtils(this)
        if (!permissionUtils.hasLocationPermission()) Log.e("KBTAPP", "TIDAK ADA IJIN APP")
        else {
            val locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setNumUpdates(1)
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    locationResult?.lastLocation?.let { location -> processingHandleLocation(location) }
                }
            }
            fusedLocationClient?.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    private fun processingHandleLocation(location: Location){
        if (!permissionUtils.isInternetConnected()) {
            writeLocationToFile(location)
        } else {
            writeLocationToFile(location)
            scopeuploadCoordinate.launch {
                handleLocationUpload(location)
            }
        }
    }

    private suspend fun handleLocationUpload(location: Location) {
        coroutineLock.withLock {
            try {
                withTimeout(refreshRate) { uploadCoordinate(location) }
            } catch (e: TimeoutCancellationException) {
                Log.e("KBTAPP", "Error upload coroutinelock: ${e.message}")
            }
        }
    }

    @Subscribe
    fun onStatusScreenOnChange(event: ScreenStatusEvent) {
        layarNyala = event.status
    }
    
    @Subscribe
    fun onStatusTimeChangeEvent(event: StatusTimeChangeEvent) {
        if(event.status == "pause") stopTimer(true)
        else if(event.status == "resume") {
            restartLocationUpdatesBg(userId)
            startTimer()
        }
    }

    private fun writeLocationToFile(location: Location) {
        try {
            val directory = File(filesDir, "resource_data")
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val filename = "location_data-${eventId.toString()}.txt"
            val file = File(directory, filename)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val currentDateAndTime: String = dateFormat.format(Date())
            val locationString =
                "${location.latitude};${location.longitude};${location.altitude};${location.speed * 3.6};${isTimerPaused};${currentDateAndTime}\n"
            if (!file.exists()) {
                file.createNewFile()
                EventBus.getDefault().post(FileCreatedEvent(true))
            }
            val fileWriter = FileWriter(file, true)
            fileWriter.append(locationString)
            fileWriter.close()
        } catch (e: IOException) {
            Log.e("KBTAPP", "Error writing to file: ${e.message}")
        }
    }

    private fun uploadCoordinate(location: Location) {
        val apiclient = ApiClient()
        try {
            val spd = location.speed * 3.6
            val requestBody = """{
                "event": "$eventId",
                "kbtuser": "$userId",
                "speeds": ${spd},
                "is_paused": "$isTimerPaused",
                "lat": "${location.latitude}",
                "lon": "${location.longitude}",
                "alt": "${location.altitude}"
            }"""
            apiclient.postData(
                urlPost,
                requestBody,
                object : ApiResponseCallback {
                    override fun onSuccess(jsonObject: JSONObject, code:Int) {
                        Log.i("KBTAPP", "Response dari API: $jsonObject")
                        val refreshratestring = jsonObject.getString("refresh_rate")
                        val maxdistancestring = jsonObject.getString("max_distance")
                        val maxdistanceScreenString = jsonObject.getString("max_distance_screen")
                        val newurlPost = jsonObject.getString("url_post")
                        val newkoefisien = jsonObject.getString("koefisien_interval").toDouble()
                        val newMaxDistanceLive = jsonObject.getString("max_distance_live").toInt()
                        val newRefreshRate = (refreshratestring.toInt() * 1000L).coerceAtLeast(3000L)
                        val newMaxDistance = maxdistancestring.toFloat().coerceAtLeast(1f)
                        val newMaxDistanceScreen = maxdistanceScreenString.toFloat().coerceAtLeast(1f)
                        if (newRefreshRate != refreshRate || koefisien_interval != newkoefisien || newMaxDistance != maxDistance || newurlPost != urlPost || maxDistanceScreen != newMaxDistanceScreen || maxDistanceLive != newMaxDistanceLive) {
                            Log.i("KBTAPP", "restart lokasi interval")
                            refreshRate = newRefreshRate
                            maxDistance = newMaxDistance      //INI JADIIN SATU SEKARANG, KELIPATANNYA NANTI
                            maxDistanceScreen = newMaxDistanceScreen
                            urlPost = newurlPost
                            koefisien_interval = newkoefisien
                            maxDistanceLive = newMaxDistanceLive
                            restartLocationUpdatesBg(userId)
                        }
                    }

                    override fun onFailure(error: String) {
                        Log.e("KBTPAPP", "Error saat upload ke API: $error")
                    }
                }
            )
        } catch (e: Exception) {
            Log.i("KBTAPP", "Terjadi kesalahan mengambil apiclient class")
        }
    }

    //DESTROY SERVICE
    private fun destroy(){
        scopeuploadCoordinate.cancel()
        stopTimer(false)
        EventBus.getDefault().unregister(this)
        Log.d("KBTAPP", "Destroy Service")
        connectivityManager?.unregisterNetworkCallback(networkCallback)
        locationCallback?.let { lc ->
            fusedLocationClient?.removeLocationUpdates(lc)
        }
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(123456789)
        stopForeground(STOP_FOREGROUND_DETACH)
    }
    override fun onDestroy() {
        super.onDestroy()
        destroy()
    }
    override fun onStopJob(params: JobParameters): Boolean {
        destroy()
        return true
    }
}
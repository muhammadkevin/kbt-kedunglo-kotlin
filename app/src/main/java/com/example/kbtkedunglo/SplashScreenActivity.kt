package com.example.kbtkedunglo

import android.animation.ValueAnimator
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.AnimatedVectorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.example.kbtkedunglo.utilsclass.ApiClient
import com.example.kbtkedunglo.utilsclass.ApiResponseGet
import com.example.kbtkedunglo.utilsclass.PermissionUtils
import com.example.kbtkedunglo.utilsclass.showAlert
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class SplashScreenActivity : AppCompatActivity() {
    private lateinit var apiClient:ApiClient
    private lateinit var permissionUtils:PermissionUtils
    private lateinit var downloadLine:View
    private var myVersionCode:Long = 0
    private var myVersionName:String = "0"
    private var screenWidth:Int = 0
    private lateinit var animateSplash:ImageView
    private val sharedPreferences: SharedPreferences by lazy {
        getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        animateSplash = findViewById(R.id.animateSplash)
        (animateSplash.drawable as? AnimatedVectorDrawable)?.start()

        permissionUtils = PermissionUtils(this)
        apiClient = ApiClient()
        downloadLine = findViewById(R.id.downloadLine)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = windowManager.currentWindowMetrics
            val insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            screenWidth = windowMetrics.bounds.width() - insets.left - insets.right
        } else {
            val displayMetrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            screenWidth = displayMetrics.widthPixels
        }

        checkVersionApk()
        if(permissionUtils.isInternetConnected()){
            getApkBaru()
        }else{
            showAlert(this, "Internet tidak aktif")
        }
    }

    private fun setDownloadLinePercentage(targetPercentage: Float) {
        val currentWidth = downloadLine.layoutParams.width
        val targetWidth = (screenWidth * targetPercentage / 100).toInt()

        val animator = ValueAnimator.ofInt(currentWidth, targetWidth)
        animator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Int
            val layoutParams = downloadLine.layoutParams
            layoutParams.width = animatedValue
            downloadLine.layoutParams = layoutParams
            val params = animateSplash.layoutParams as ViewGroup.MarginLayoutParams
            params.leftMargin = animatedValue
            animateSplash.layoutParams = params
        }

        animator.interpolator = LinearInterpolator()
        animator.start()
    }

    private fun checkVersionApk(){
        val packageInfo = this.packageManager.getPackageInfo(packageName, 0)
        myVersionName = packageInfo.versionName
        myVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            packageInfo.versionCode.toLong()
        }
        Log.d("KBTAPP", "versi nama:${myVersionName} - versi code:${myVersionCode}")
        setDownloadLinePercentage(0F)
    }

    private fun getApkBaru(){
        apiClient.getData("https://kbt.us.to/settings/apk/get/", object:ApiResponseGet{
            override fun onSuccess(response: String, code: Int) {
                val jsonObject = JSONObject(response)
                val versionName = jsonObject.getString("version")
                val fileName = jsonObject.getString("file_name")
                runOnUiThread{ setDownloadLinePercentage(27F) }
                if(versionName != myVersionName){
                    checkUnknownSourcesPermission(fileName)
                }else{
                    runOnUiThread{ setDownloadLinePercentage(100F) }
                    startAppropriateActivity()
                }
            }
            override fun onFailure(error: String) {
                Log.d("KBTAPP", "terjadi kesalahan cekapk ${error}")
            }
        })
    }

    private val REQUEST_UNKNOWN_SOURCES = 123
    private fun checkUnknownSourcesPermission(apkUrl:String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val packageManager = packageManager
            if (!packageManager.canRequestPackageInstalls()) {
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                intent.data = Uri.parse("package:$packageName")
                @Suppress("DEPRECATION")
                startActivityForResult(intent, REQUEST_UNKNOWN_SOURCES, null)
            } else {
                downloadApkWithOkHttp(apkUrl)
            }
        } else {
            downloadApkWithOkHttp(apkUrl)
        }
    }

    private fun downloadApkWithOkHttp(apkUrl:String) {
        val destination = File(filesDir, "app_update.apk")
        val client = OkHttpClient()

        val request = Request.Builder()
            .url(apkUrl)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("KBTAPP", "Kegagalan get apk baru")
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let { responseBody ->
                    try {
                        val inputStream = responseBody.byteStream()
                        val outputStream = FileOutputStream(destination)
                        var bytesDownloaded: Long = 0
                        val totalBytes = responseBody.contentLength()

                        val buffer = ByteArray(4096)
                        var bytesRead: Int
                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                            bytesDownloaded += bytesRead
                            val downloadPercentage = 27 + (bytesDownloaded.toFloat() / totalBytes) * 73
                            runOnUiThread {
                                setDownloadLinePercentage(downloadPercentage.toFloat())
                            }
                        }

                        outputStream.flush()
                        outputStream.close()
                        inputStream.close()
                        installApkInBackground(destination)
                    } catch (e: IOException) {
                        Log.d("KBTAPP", "kesalahan download apk:${e.message}")
                    }
                }
            }
        })
    }

    private fun installApkInBackground(file: File) {
        val apkUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", file)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION

        runOnUiThread{
            AlertDialog.Builder(this)
                .setTitle("Pembaruan Tersedia")
                .setMessage("Aplikasi baru telah diunduh. Apakah kamu ingin menginstalnya sekarang?")
                .setPositiveButton("OK") { _, _ ->
                    startActivity(intent)
                }
                .setNegativeButton("", null)
                .show()
        }
    }

    private fun showUpdateFinishedNotification() {
        createNotificationChannel()

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(this, "kbtapp_update_package")
            .setContentTitle("Pembaruan Selesai")
            .setContentText("Ketuk untuk membuka aplikasi yang sudah diperbarui.")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(17, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Channel Name"
            val descriptionText = "Channel Description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("kbtapp_update_package", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startAppropriateActivity() {
        val savedUsername:String? = sharedPreferences.getString("username", "")
        val savedPassword:String? = sharedPreferences.getString("password", "")
        val accessToken:String? = sharedPreferences.getString("access_token", "")
        if (savedUsername.isNullOrEmpty() && savedPassword.isNullOrEmpty() && accessToken.isNullOrEmpty()) {
            startActivity(Intent(this, LoginActivity::class.java))
        } else{
            startActivity(Intent(this, MainActivity::class.java))
        }
        finish()
    }
}
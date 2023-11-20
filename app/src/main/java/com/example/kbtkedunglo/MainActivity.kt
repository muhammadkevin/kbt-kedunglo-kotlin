package com.example.kbtkedunglo


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private val sharedPreferences: SharedPreferences by lazy {
        getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val savedUsername:String? = sharedPreferences.getString("username", "")
        val savedPassword:String? = sharedPreferences.getString("password", "")
        val idUser:String? = sharedPreferences.getString("id", "")


        if (savedUsername.isNullOrEmpty() && savedPassword.isNullOrEmpty()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            //inisialiasai page
            val btmNav: BottomNavigationView = findViewById(R.id.bottomNavigationView)
            if(savedInstanceState == null){
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, HomeFragment())
                    .commit()

            }
            btmNav.setOnItemSelectedListener { item ->
                when(item.itemId){
                    R.id.menu_event -> {
                        val eventFragment = EventFragment.newInstance(idUser.orEmpty())
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, eventFragment)
                            .commit()
                        true
                    }
                    R.id.menu_home -> {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, HomeFragment())
                            .commit()
                        true
                    }
                    else -> false
                }
            }

            if (hasLocationPermission()) {
                checkGpsStatus()
            } else {
                requestLocationPermission()
            }
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        requestPermissions(
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun checkGpsStatus() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showGpsDisabledAlert()
        } else {
           Log.i("LOG LOCATION", "GPS NYALA")
        }
    }

    private fun showGpsDisabledAlert() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setMessage("GPS is disabled. Do you want to enable it?")
        alertDialogBuilder.setPositiveButton("Yes") { dialog, id ->
            // Buka pengaturan untuk mengaktifkan GPS
            val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(settingsIntent)
        }
        alertDialogBuilder.setNegativeButton("No") { dialog, id ->
            // Tangani penolakan oleh pengguna, sesuai kebutuhan Anda
        }
        val alert = alertDialogBuilder.create()
        alert.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Izin diberikan, dapatkan lokasi
                checkGpsStatus()
            } else {
                Log.i("error", "ditolak")
                requestLocationPermission()
            }
        }
    }

}
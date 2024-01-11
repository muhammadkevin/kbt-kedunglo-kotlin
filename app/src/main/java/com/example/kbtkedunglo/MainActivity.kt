package com.example.kbtkedunglo


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.kbtkedunglo.fragments.DetailAfterMedalFragment
import com.example.kbtkedunglo.fragments.EventFragment
import com.example.kbtkedunglo.fragments.HomeFragment
import com.example.kbtkedunglo.fragments.ProfilFragment
import com.example.kbtkedunglo.fragments.SettingFragment
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private lateinit var activeFragment:Fragment
    private lateinit var btmNav: BottomNavigationView
    private val sharedPreferences: SharedPreferences by lazy {
        getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btmNav = findViewById(R.id.bottomNavigationView)

        if (hasLocationPermission()) {
            checkGpsStatus()
        } else {
            requestLocationPermission()
        }

        //inisialiasai page
        val btmNav: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        if(savedInstanceState == null){
            activeFragment = HomeFragment()
            supportFragmentManager.beginTransaction()
               .setCustomAnimations(R.anim.slide_out, R.anim.slide_in)
               .replace(R.id.fragment_container, HomeFragment())
               .commit()
            changeSelectedItemId(R.id.menu_home)
        }
        btmNav.setOnItemSelectedListener { item ->
            when(item.itemId){
                R.id.menu_event -> {
                    val followEvent:String? = sharedPreferences.getString("follow_event", "")
                    if(followEvent != null && followEvent.toBoolean()){
                        supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
                        .replace(R.id.fragment_container, DetailAfterMedalFragment())
                        .commit()
                        activeFragment = DetailAfterMedalFragment()
                    }else{
                        supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
                        .replace(R.id.fragment_container, EventFragment())
                        .commit()
                        activeFragment = EventFragment()
                    }
                    true
                }
                R.id.menu_home -> {
                    if (activeFragment is EventFragment || activeFragment is DetailAfterMedalFragment) {
                        supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                        .replace(R.id.fragment_container, HomeFragment()).commit()
                    } else {
                        supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
                        .replace(R.id.fragment_container, HomeFragment()).commit()
                    }
                    activeFragment = HomeFragment()
                    true

                }
                R.id.menu_profil -> {
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                        .replace(R.id.fragment_container, ProfilFragment())
                        .commit()
                    activeFragment = ProfilFragment()
                    true
                }
                R.id.menu_setting -> {
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                        .replace(R.id.fragment_container, SettingFragment())
                        .commit()
                    activeFragment = SettingFragment()
                    true
                }
                else -> false
            }
        }

    }

    fun changeSelectedItemId(menu: Int) {
        btmNav.selectedItemId = menu
    }

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    fun toggleBottomNavigationVisibility(visibility: Int) {
        val btmNav: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        btmNav.visibility = visibility
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

    private val gpsActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        {res->
            Log.i("KBTAPP", res.resultCode.toString())
            if (res.resultCode == 0 || res.resultCode == Activity.RESULT_OK) {
                if (!isGpsEnabled()) showGpsDisabledAlert()
            }
        }
    private fun showGpsDisabledAlert() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setMessage("GPS anda mati. Hidupkan GPS sekarang?")
        alertDialogBuilder.setPositiveButton("Ya") { dialog, id ->
            val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            gpsActivityResultLauncher.launch(settingsIntent)
        }
        alertDialogBuilder.setNegativeButton("Tidak") { dialog, id ->
            showGpsDisabledAlert()
        }
        val alert = alertDialogBuilder.create()
        alert.show()
    }

    private fun isGpsEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkGpsStatus()
            } else {
                Log.i("error", "ditolak")
                requestLocationPermission()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}
package com.example.kbtkedunglo

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker


private const val ARG_PARAM2 = "param2"

class EventFragment : Fragment() {
    private var userId: String? = null
    private var param2: String? = null
    private var locationCallback:LocationCallback? = null
    private lateinit var mapView: MapView
    private lateinit var fusedLocationClientFragment: FusedLocationProviderClient
    private var eventActive = false

    private val sharedPreferences: SharedPreferences by lazy {
        requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getString("id")
            param2 = it.getString(ARG_PARAM2)
        }
        Log.i("KBTAPP", "userid: ${userId}")
        fusedLocationClientFragment = LocationServices.getFusedLocationProviderClient(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val ctx = requireContext().applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        return inflater.inflate(R.layout.fragment_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = view.findViewById<MapView>(R.id.map)
        if(mapView != null){
            mapView.setTileSource(TileSourceFactory.MAPNIK)
            mapView.controller.setZoom(18.0)
            mapView.controller.setCenter(GeoPoint(-7.8195106358, 112.007007986))
            mapView.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            mapView.setMultiTouchControls(true)

            if (hasLocationPermission()) {
                startLocationUpdates()
            }

            val startEv: Button = view.findViewById(R.id.buttonEvent)
            val startevent:String? = sharedPreferences.getString("start_event", "")
            if(startevent?.toBoolean() ?:false)startEv.text = "Stop Event"
            else startEv.text = "Start Event"
            eventActive = startevent?.toBoolean() ?: false

            startEv.setOnClickListener {
                val editor = sharedPreferences.edit()
                if (eventActive) {
                    stopLocationService()
                    eventActive = false
                    editor.putString("start_event", false.toString())
                    editor.apply()
                    startEv.text = "Start Event"
                } else {
                    startLocationService()
                    eventActive = true
                    editor.putString("start_event", true.toString())
                    editor.apply()
                    startEv.text = "Stop Event"
                }
            }
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun updateMarkerLocation(location: Location) {
        val latitude = location.latitude
        val longitude = location.longitude

        if (mapView.overlays.isEmpty()) {
            val marker = Marker(mapView)
            marker.position = GeoPoint(latitude, longitude)
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            mapView.overlays.add(marker)
        }else{
            val marker = mapView.overlays[0] as Marker
            marker.position = GeoPoint(latitude, longitude)
        }
        mapView.controller.setCenter(GeoPoint(latitude, longitude))
        mapView.invalidate()
    }

    private fun startLocationService() {
        val serviceIntent = Intent(requireContext(), LocJobService::class.java)
        serviceIntent.putExtra("userId", userId)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireContext().startForegroundService(serviceIntent)
        } else {
            requireContext().startService(serviceIntent)
        }
    }

    private fun stopLocationService() {
        val serviceIntent = Intent(requireContext(), LocJobService::class.java)
        requireContext().stopService(serviceIntent)
    }

    private fun startLocationUpdates() {
        try {
            val locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(20000L)
                .setFastestInterval(20000L)
                .setSmallestDisplacement(10f)

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    locationResult?.lastLocation?.let { location ->
                        updateMarkerLocation(location)
                    }
                }
            }

            fusedLocationClientFragment.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Log.e("KBTAPP", e.message.toString())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.overlays.clear()
        mapView.invalidate()
        locationCallback?.let {
            fusedLocationClientFragment.removeLocationUpdates(it)
        }
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    companion object {
        @JvmStatic
        fun newInstance(id:String) =
            EventFragment().apply {
                arguments = Bundle().apply {
                    putString("id", id)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
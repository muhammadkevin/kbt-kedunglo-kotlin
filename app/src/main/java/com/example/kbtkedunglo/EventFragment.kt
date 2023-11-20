package com.example.kbtkedunglo

import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
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
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class EventFragment : Fragment() {
    private var userId: String? = null
    private var param2: String? = null
    private var locationCallback:LocationCallback? = null
    private lateinit var mapView: MapView
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var eventActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getString("id")
            param2 = it.getString(ARG_PARAM2)
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = view.findViewById<MapView>(R.id.map)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.controller.setZoom(16.0)
        mapView.controller.setCenter(GeoPoint(-7.8195106358, 112.007007986))
        mapView.invalidate()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val ctx = requireContext().applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))

        val view = inflater.inflate(R.layout.fragment_event, container, false)

//        // Inisialisasi mapView sebelum pemanggilan findViewById
//        mapView = view.findViewById<MapView>(R.id.map)
//        mapView.setTileSource(TileSourceFactory.MAPNIK)
//        mapView.controller.setZoom(15.0)
//        mapView.controller.setCenter(GeoPoint(-7.8195106358, 112.007007986))
//
//        // Refresh peta
//        mapView.invalidate()

        if (hasLocationPermission()) {
            startLocationUpdates(false)
        }

        val startEv: Button = view.findViewById(R.id.coolButton)
        startEv.setOnClickListener {
            if (eventActive) {
                startLocationUpdates(false)
                startEv.text = "Start Event"
            } else {
                startLocationUpdates(true)
                startEv.text = "Stop Event"
            }
//
        //            if (hasLocationPermission()) {
//                startLocationUpdates(true)
//            }
        }

        return view
    }

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun updateMarkerLocation(location: Location) {
        Log.i("KBTAPP", location.toString())
        val latitude = location.latitude
        val longitude = location.longitude

//        if(mapView != null){
            mapView.overlays.clear()
            val currentLocation = GeoPoint(latitude, longitude)
            val marker = Marker(mapView)
            marker.position = currentLocation
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            mapView.overlays.add(marker)
            mapView.controller.setCenter(currentLocation)
            // Refresh peta
            mapView.invalidate()
//        }

    }

    private fun uploadCoordinate(location: Location){
        val apiclient = ApiClient()
        apiclient.postData(
            "https://kbt.us.to/tracker/post/",
            """{"event": 4, "kbtuser": $userId, "lat": "${location.latitude}", "lon": "${location.longitude}", "alt": "${location.altitude}"}""",
            object :ApiResponseCallback{
                override fun onSuccess(jsonObject: JSONObject) {
                    Log.i("KBTAPP", "Response: $jsonObject")
                }
                override fun onFailure(error: String) {
                    Log.e("KBTPAPP","Error: $error")
                }
            }
        )
    }

    private fun startLocationUpdates(posted:Boolean) {
        try {
            if(posted) eventActive = true
            else eventActive = false
            val locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000L)
                .setSmallestDisplacement(10f)

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    locationResult?.lastLocation?.let { location ->
                        updateMarkerLocation(location)
                        if(posted) uploadCoordinate(location)
                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
    }

    private fun stopLocationUpdates() {
        mapView.overlays.clear()
        mapView.invalidate()
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause() // Untuk menghentikan pembaruan peta ketika fragmen tidak terlihat
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume() // Untuk melanjutkan pembaruan peta ketika fragmen terlihat kembali
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
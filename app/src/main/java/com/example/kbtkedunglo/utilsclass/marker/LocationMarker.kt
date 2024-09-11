package com.example.kbtkedunglo.utilsclass.marker

import android.location.Location
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.animation.LinearInterpolator
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class LocationMarker(val mapView:MapView?, val marker:Marker, userControlMapPar:Boolean) {
    var userControlMap = userControlMapPar
    var notInitializedMarker = true

    fun update(location:Location){
        val latitude = location.latitude
        val longitude = location.longitude
        mapView?.let { mapView ->
            try {
                if(!userControlMap){
                    mapView.controller?.let { controller ->
                        controller.animateTo(GeoPoint(latitude, longitude), 17.0, 700)
                    }
                }
                if(notInitializedMarker) {
                    marker.position = GeoPoint(latitude, longitude)
                    notInitializedMarker = false
                }else animateMarker(GeoPoint(latitude, longitude))
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                mapView.invalidate()
            } catch (e: Exception) {
                Log.e("KBTAPP", "ERROR PEMBUATAN MARKER: ${e.message}")
            }
        }
    }

    fun animateMarker(toPosition:GeoPoint){
        val handler = Handler(Looper.getMainLooper())
        mapView?.let{map ->
            val start = SystemClock.uptimeMillis()
            val proj = map.projection
            val startPoint = proj.toPixels(marker.position, null)
            val startGeoPoint = proj.fromPixels(startPoint.x, startPoint.y)
            val duration: Long = 500
            val interpolator = LinearInterpolator()
            handler.post(object : Runnable {
                override fun run() {
                    val elapsed = SystemClock.uptimeMillis() - start
                    val t = interpolator.getInterpolation(elapsed.toFloat() / duration)
                    val lng = t * toPosition.longitude + (1 - t) * startGeoPoint.longitude
                    val lat = t * toPosition.latitude + (1 - t) * startGeoPoint.latitude
                    marker.position = GeoPoint(lat, lng)
                    if (t < 1.0) {
                        handler.postDelayed(this, 15)
                    }
                    map.postInvalidate()
                }
            })
        }
    }
}
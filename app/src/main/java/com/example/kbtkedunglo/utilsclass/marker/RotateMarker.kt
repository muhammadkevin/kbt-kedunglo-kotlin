package com.example.kbtkedunglo.utilsclass.marker

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.animation.LinearInterpolator
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class RotateMarker(val mapView:MapView?, val marker:Marker) {
    var isAnimatingMark:Boolean = false
    var duration:Long = 500L

    fun animateRotateMarker(toRotation:Float){
        Log.d("KBTAPP", toRotation.toString())
        isAnimatingMark = true
        mapView?.let{ mv->
            val handler = Handler(Looper.getMainLooper())
            val start = SystemClock.uptimeMillis()
            val startRotation = marker.rotation
            val interpolator = LinearInterpolator()
            handler.post(object : Runnable {
                override fun run() {
                    val elapsed = SystemClock.uptimeMillis() - start
                    val dTheta = (toRotation - startRotation) % 360
                    val shortestAngle = if (Math.abs(dTheta) > 180) dTheta - (360 * Math.signum(dTheta)) else dTheta
                    val t: Float = interpolator.getInterpolation(elapsed.toFloat() / duration)
                    val rot = (startRotation + t * shortestAngle) % 360
                    marker.rotation = rot
                    if (t < 1.0) {
                        handler.postDelayed(this, 15)
                        mv.invalidate()
                    }else{
                        isAnimatingMark = false
                    }
                }
            })
        }
    }
}
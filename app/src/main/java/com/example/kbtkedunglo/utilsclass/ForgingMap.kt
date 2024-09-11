package com.example.kbtkedunglo.utilsclass

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Paint
import android.location.Location
import android.media.MediaPlayer
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.kbtkedunglo.R
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.Polyline


class ForgingMap (private val context:Context){
    private val sharedPreferences: SharedPreferences by lazy {
         context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
    }

    fun calculateDistance(location: Location, road: Road, textDistanceToEvent:TextView):Location? {
        val editor = sharedPreferences.edit()
        val markerPosition = location
        var shortestDistance:Float = Float.MAX_VALUE
        var nearLocRoad:Location? = null

        road?.let{
            for (i in 0 until it.mRouteHigh.size - 1) {
                val startPoint = it.mRouteHigh[i]
                val roadLoc = Location("kbtAppProvider")
                roadLoc.longitude = startPoint.longitude
                roadLoc.latitude = startPoint.latitude
                val distance = markerPosition.distanceTo(roadLoc)
                if (distance < shortestDistance) {
                    nearLocRoad = roadLoc
                    shortestDistance = distance
                }
            }
        }
        if(sharedPreferences.getString("alert_to_event_route", "") == "false"){
            if(shortestDistance > 100) {
                val mediaPlayer: MediaPlayer? = MediaPlayer.create(context, R.raw.alarm_route)
                mediaPlayer?.isLooping = true
                mediaPlayer?.start()

                val builder = AlertDialog.Builder(context)
                builder.setTitle("Peringatan")
                builder.setMessage("Anda Keluar Dari Rute Acara!")
                builder.setPositiveButton("OK") { dialog, which ->
                    dialog.dismiss()
                    mediaPlayer?.release()
                }
                val alertDialog: AlertDialog = builder.create()
                alertDialog.show()
            }
            editor.putString("alert_to_event_route", "true")
            editor.apply()
        }
        textDistanceToEvent.text = meterToKilometer(shortestDistance)
        return nearLocRoad
    }

    fun drawRoad(road:Road, mapView:MapView, color:Int): Polyline?{
        val roads = RoadManager.buildRoadOverlay(road)
        mapView?.let { mv ->
            roads.outlinePaint?.color = color
            roads.outlinePaint?.strokeWidth = 10f
            roads.outlinePaint?.strokeJoin = Paint.Join.ROUND
            roads.outlinePaint?.strokeCap = Paint.Cap.ROUND
            roads.outlinePaint?.style = Paint.Style.FILL_AND_STROKE
            if (mv.overlays == null) {
                val newOverlays = mutableListOf<Overlay>()
                val markerOverlay = Marker(mv)
                newOverlays.add(0, markerOverlay)
                newOverlays.add(1, roads)
                mv.overlays.addAll(newOverlays)
            }else{
                if (mv.overlays.isEmpty()) {
                    val markerOverlay = Marker(mv)
                    mv.overlays.add(0, markerOverlay)
                }
                mv.overlays.add(1, roads)
            }
            mv.zoomToBoundingBox(road.mBoundingBox, true)
            mv.invalidate()
        }
        return roads
    }

}
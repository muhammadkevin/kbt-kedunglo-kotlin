package com.example.kbtkedunglo.utilsclass

import android.content.Context
import android.graphics.Canvas
import android.graphics.Point
import android.graphics.drawable.Drawable
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.OverlayItem

class CustomMarkerOverlay(context: Context, private var item: OverlayItem, markerDrawable: Drawable?) :
    ItemizedIconOverlay<OverlayItem>(mutableListOf(item), object : ItemizedIconOverlay.OnItemGestureListener<OverlayItem> {
        override fun onItemSingleTapUp(index: Int, item: OverlayItem?): Boolean {
            return true
        }

        override fun onItemLongPress(index: Int, item: OverlayItem?): Boolean {
            return false
        }
    }, context) {

    private val marker: Drawable? = markerDrawable

    override fun draw(canvas: Canvas?, mapView: MapView?, shadow: Boolean) {
        super.draw(canvas, mapView, shadow)

        if (!shadow && marker != null && canvas != null && mapView != null) {
            val point = Point()
            mapView.projection.toPixels(item.point, point)
            marker.setBounds(
                point.x - marker.intrinsicWidth / 2,
                point.y - marker.intrinsicHeight,
                point.x + marker.intrinsicWidth / 2,
                point.y
            )
            marker.draw(canvas)
        }
    }

    private fun markerToBitmap(marker: Drawable): Drawable {
        return marker
    }
}



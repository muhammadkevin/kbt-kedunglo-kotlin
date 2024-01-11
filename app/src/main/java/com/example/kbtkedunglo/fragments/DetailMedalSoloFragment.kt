package com.example.kbtkedunglo.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.example.kbtkedunglo.R
import com.example.kbtkedunglo.utilsclass.ForgingMap
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException


class DetailMedalSoloFragment : Fragment() {
    private var eventFile: String? = null
    private var mapView:MapView? = null
    private lateinit var forgingMap: ForgingMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        forgingMap = ForgingMap(requireContext())
        arguments?.let {
            eventFile = it.getString("eventFile")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val ctx = requireContext().applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        val view:View = inflater.inflate(R.layout.fragment_detail_medal_solo, container, false)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = view?.findViewById(R.id.mapView)
        mapView?.let { mv ->
            mv.setTileSource(TileSourceFactory.MAPNIK)
            mv.controller.setZoom(18.0)
            mv.controller.setCenter(GeoPoint(-7.8195106358, 112.007007986))
            mv.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            mv.setMultiTouchControls(true)
            readCoordinatesFromFile(eventFile)?.let { coordinates ->
                if (coordinates.size >= 2) {
                    val polyline = Polyline()
                    coordinates.forEach { coordinate ->
                        polyline.addPoint(GeoPoint(coordinate.latitude, coordinate.longitude))
                    }
                    mv.controller.setCenter(GeoPoint(polyline.actualPoints[0].latitude, polyline.actualPoints[0].longitude))
                    mv.overlayManager.add(polyline)
                    mv.invalidate()
                }
            }
        }
    }

    private fun readCoordinatesFromFile(fileName: String?): List<Coordinate>? {
        val coordinates = mutableListOf<Coordinate>()
        fileName?.let {
            try {
                val directory = File(requireContext().filesDir, "resource_data")
                val file = File(directory, fileName)
                val reader = BufferedReader(FileReader(file))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val parts = line!!.split(";").toTypedArray()
                    if (parts.size >= 4) {
                        val latitude = parts[0].toDouble()
                        val longitude = parts[1].toDouble()
                        coordinates.add(Coordinate(latitude, longitude))
                    }
                }
            } catch (e: IOException) {
                Log.e("DetailMedalSoloFragment", "Error reading file", e)
            }
        }
        return coordinates
    }
    data class Coordinate(val latitude: Double, val longitude: Double)

    override fun onDestroy() {
        super.onDestroy()
        mapView?.let{
            it.overlays.clear()
            it.invalidate()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(eventFile: String) =
            DetailMedalSoloFragment().apply {
                arguments = Bundle().apply {
                    putString("eventFile", eventFile)
                }
            }
    }
}
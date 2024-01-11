package com.example.kbtkedunglo.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.example.kbtkedunglo.R
import com.example.kbtkedunglo.utilsclass.ApiClient
import com.example.kbtkedunglo.utilsclass.ApiResponseGet
import com.example.kbtkedunglo.utilsclass.CustomScrollView
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import kotlin.math.round

class DetailTimeLineFragment : Fragment() {
    private var userid: String? = null
    private var eventid: String? = null
    private var namamedaler:String? = null
    private var titleMedal:String? = null
    private var detailTime:String? = null
    private var distance:String? = null
    private var avgSpeed:String? = null
    private var elevation:String? = null
    private var duration:String? = null
    private var fotoUrl:String? = null
    private lateinit var namaMedalerTxt:TextView
    private lateinit var detailWaktuTxt:TextView
    private lateinit var titleMedalTxt:TextView
    private lateinit var jarakTempuhTxt:TextView
    private lateinit var elevationTxt:TextView
    private lateinit var durationTxt:TextView
    private lateinit var avgSpeedTxt:TextView
    private lateinit var bestElevationTxt:TextView
    private lateinit var bestSpeedTxt:TextView
    private lateinit var profilImage:ImageView
    private var mapView:MapView? = null
    private lateinit var scrollView:CustomScrollView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userid = it.getString("user_id")
            eventid = it.getString("event_id")
            namamedaler = it.getString("nama_medaler")
            titleMedal = it.getString("title_medal")
            detailTime = it.getString("detail_time")
            distance = it.getString("distance")
            avgSpeed = it.getString("avg_speed")
            elevation = it.getString("elevation")
            duration = it.getString("duration")
            fotoUrl = it.getString("foto_url")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val ctx = requireContext().applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        val view:View = inflater.inflate(R.layout.fragment_detail_time_line, container, false)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        namaMedalerTxt = view.findViewById(R.id.namamedaler)
        detailWaktuTxt = view.findViewById(R.id.detailwaktu)
        titleMedalTxt = view.findViewById(R.id.titlemedal)
        jarakTempuhTxt = view.findViewById(R.id.distance)
        elevationTxt = view.findViewById(R.id.elevation)
        durationTxt = view.findViewById(R.id.duration)
        avgSpeedTxt = view.findViewById(R.id.avgspeed)
        bestElevationTxt = view.findViewById(R.id.bestElevation)
        bestSpeedTxt = view.findViewById(R.id.bestSpeed)
        profilImage = view.findViewById(R.id.circleViewbgProfil)
        scrollView = view.findViewById(R.id.scrollView)

        namaMedalerTxt.text = namamedaler
        titleMedalTxt.text = titleMedal
        detailWaktuTxt.text = detailTime
        jarakTempuhTxt.text = "${distance} km"
        avgSpeedTxt.text = "${avgSpeed} km/jm"
        elevationTxt.text = "${elevation} m"
        durationTxt.text = duration

        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
        Log.i("KBTAPP", "${userid} : ${eventid}")
        return view
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = view?.findViewById(R.id.mapView)
        mapView?.let { mv ->
            mv.setTileSource(TileSourceFactory.MAPNIK)
            mv.controller.setZoom(17.0)
            mv.controller.setCenter(GeoPoint(-7.8195106358, 112.007007986))
            mv.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            mv.setMultiTouchControls(true)

            mv.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        scrollView.isScrollable = false
                    }
                    MotionEvent.ACTION_UP -> {
                        scrollView.isScrollable = true
                    }
                }
                v.onTouchEvent(event)
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    Picasso.get().load("${fotoUrl}").get()
                }
                withContext(Dispatchers.Main) {
                    profilImage.setImageBitmap(bitmap)
                }
            } catch (e: Exception) {
                Log.e("KBTAPP", "Error draw image: ${e.message}")
                e.printStackTrace()
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            val apiclient = ApiClient()
            apiclient.getData("https://kbt.us.to/tracker/report/${userid}/${eventid}/",
                object: ApiResponseGet {
                    override fun onSuccess(response: String, code: Int) {
                        val respArr = JSONObject(response)
                        val coord = JSONArray(respArr.getString("tracker_data"))
                        val maxSpeedStr = respArr.getString("max_speed").toFloat()
                        val maxSpeed = round(maxSpeedStr * 100.0) / 100.0
                        bestSpeedTxt.text = "${maxSpeed} km/jm"
                        val polyline = Polyline()
                        for (i in 0 until coord.length()) {
                            val jsonPoint = coord.getJSONObject(i)
                            val latitude = jsonPoint.getDouble("lat")
                            val longitude = jsonPoint.getDouble("lon")
                            val geoPoint = GeoPoint(latitude, longitude)
                            polyline.addPoint(geoPoint)
                        }
                        requireActivity().runOnUiThread {
                            mapView?.let{mv->
                                mv.controller.setCenter(GeoPoint(polyline.actualPoints[0].latitude, polyline.actualPoints[0].longitude))
                                mv.overlays.add(polyline)
                                mv.invalidate()
                            }
                        }
                        Log.d("KTBAPP", "${respArr}")
                    }
                    override fun onFailure(error: String) {
                        Log.e("KBTAPP", error)
                    }
                })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.let{
            it.overlays.clear()
            it.invalidate()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(userid: String, eventid: String, namaMedaler:String, titleMedal:String,
                        detailTime:String, distance:String, avgSpeed:String, elevation:String,
                        duration:String, foto:String) =
            DetailTimeLineFragment().apply {
                arguments = Bundle().apply {
                    putString("event_id", eventid)
                    putString("user_id", userid)
                    putString("nama_medaler", namaMedaler)
                    putString("title_medal", titleMedal)
                    putString("detail_time", detailTime)
                    putString("distance", distance)
                    putString("avg_speed", avgSpeed)
                    putString("elevation", elevation)
                    putString("duration", duration)
                    putString("foto_url", foto)
                }
            }
    }
}
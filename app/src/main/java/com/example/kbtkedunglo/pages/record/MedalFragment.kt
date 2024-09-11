package com.example.kbtkedunglo.pages.record

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.example.kbtkedunglo.MainActivity
import com.example.kbtkedunglo.R
import com.example.kbtkedunglo.pages.DetailAfterMedalFragment
import com.example.kbtkedunglo.services.LocJobService
import com.example.kbtkedunglo.services.MedalOfflineDatabase
import com.example.kbtkedunglo.utilsclass.ApiClient
import com.example.kbtkedunglo.utilsclass.ApiResponseCallback
import com.example.kbtkedunglo.utilsclass.FileCreatedEvent
import com.example.kbtkedunglo.utilsclass.ForgingMap
import com.example.kbtkedunglo.utilsclass.LocationChangeEvent
import com.example.kbtkedunglo.utilsclass.LocationWithSpeedAverageChange
import com.example.kbtkedunglo.utilsclass.PermissionUtils
import com.example.kbtkedunglo.utilsclass.ScreenStatusEvent
import com.example.kbtkedunglo.utilsclass.StatusTimeChangeEvent
import com.example.kbtkedunglo.utilsclass.TimeChangeEvent
import com.example.kbtkedunglo.utilsclass.marker.LocationMarker
import com.example.kbtkedunglo.utilsclass.marker.RotateMarker
import com.example.kbtkedunglo.utilsclass.showAlert
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONObject
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.abs
import kotlin.math.atan2


class MedalFragment : Fragment() {
    private var userId: String? = null                      //DATA INITALIZE
    private var eventId: String? = null
    private var fotoUrl:String? = null
    private var eventNama:String? = null
    private var eventGpx: String? = null
    private var mainEvent:String? = null
    private var startEvent:Boolean = false
    private var followEvent:Boolean = false
    private var nearLocRoad:Location? = null
    private var currentLoc:Location? = null
    private var lastKnownLocation: Location? = null
    private var maxDistanceScreen:Float = 3F
    private var userControlMap:Boolean = false
    private var rotationDifferentMap:Float = 0F
    private var initialDeltaDegree:Float = 0F
    private var initialTotalDegree:Double = 0.0
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var fusedLocationClientFragment: FusedLocationProviderClient
    private var mapView: MapView? = null                    //VIEW INITIALIZE
    private var roadEvent:Road? = null
    private lateinit var progressBar: ProgressBar
    private lateinit var statusRoute:TextView
    private lateinit var textstt:TextView
    private lateinit var textSpeedAvg:TextView
    private lateinit var textDistanceToEvent:TextView
    private lateinit var wrapDistanceToEvent:LinearLayout
    private lateinit var detailwrapevent:LinearLayout
    private lateinit var forgingMap:ForgingMap
    private lateinit var timerView:TextView
    private lateinit var imageWatchEvent:ImageView
    private lateinit var startEv:Button                     //BUTTON INITIALIZE
    private lateinit var buttonrefreshroute:Button
    private lateinit var btUserControl:Button
    private lateinit var imagebtUserControl:ImageView
    private lateinit var pauseView: RelativeLayout
    private lateinit var resumeView: RelativeLayout
    private lateinit var buttonUnsubEvent: Button
    private lateinit var buttonFullScreen:ImageView
    private lateinit var detailBt:ImageView
    private lateinit var btWatchEvent:Button
    private var fileTxtCreated:Boolean = false
    private val sensorManager by lazy {                     // SENSOR INITIALIZE
        requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    private val accelerometerSensor by lazy {
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }
    private val magnetometerSensor by lazy {
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }
    private val accelerometerValues = FloatArray(3)
    private val magnetometerValues = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private var lastMarkerRotation:Float = 0F               //SENSOR ROTATE MAP
    private var initialRotationMap = 0f
    private var initialDeltaX1:Float? = null
    private var initialDeltaY1:Float? = null
    private var initialDeltaX2:Float? = null
    private var initialDeltaY2:Float? = null
    private val handlerSensor:Handler = Handler(Looper.getMainLooper())
    private lateinit var context: Context                   //UTIL INITIALIZE
    private lateinit var activity:FragmentActivity
    private lateinit var rotateMarker: RotateMarker
    private lateinit var locationMarker: LocationMarker
    private lateinit var permissionUtils:PermissionUtils
    private var locationCallback: LocationCallback? = null
    private var roadBackToEventOverlay: Polyline? = null
    private val scopeDrawGpx = CoroutineScope(Dispatchers.IO)
    private val scopeDrawRouteToEvent = CoroutineScope(Dispatchers.IO)
    private val sharedPreferences: SharedPreferences by lazy {
        requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
    }
    private val marker: Marker by lazy {
        val marker = Marker(mapView)
        val markerDrawable = ContextCompat.getDrawable(context, R.drawable.ic_marker)
        mapView?.overlays?.add(0, marker)
        marker.icon =  markerDrawable
        marker
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = requireContext()                                  //UTIL INIT
        activity = requireActivity()
        forgingMap = ForgingMap(context)
        permissionUtils = PermissionUtils(context)
        editor = sharedPreferences.edit()                           //DATA INIT
        userId = sharedPreferences.getString("id", "")
        fotoUrl = sharedPreferences.getString("foto", "")
        eventId = sharedPreferences.getString("event_id", "")
        eventNama = sharedPreferences.getString("event_nama", "")
        eventGpx = sharedPreferences.getString("event_gpx", "")
        mainEvent = sharedPreferences.getString("main_event", "")
        startEvent = sharedPreferences.getString("start_event", "").toBoolean()
        followEvent = sharedPreferences.getString("follow_event", "").toBoolean()
        fileTxtCreated = sharedPreferences.getString("file_txt_created", "").toBoolean()
        fusedLocationClientFragment = LocationServices.getFusedLocationProviderClient(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val ctx = context.applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        val view = inflater.inflate(R.layout.fragment_medal, container, false)
        mapView = view?.findViewById(R.id.mapViewEvent)                                 //MAP
        rotateMarker = RotateMarker(mapView, marker)                                    //UTIL
        locationMarker = LocationMarker(mapView, marker, userControlMap)
        startEv = view.findViewById(R.id.buttonEvent)                                   //BUTTON
        buttonUnsubEvent = view.findViewById(R.id.unsubscribeevent)
        buttonrefreshroute = view.findViewById(R.id.reloadroute)
        buttonFullScreen = view.findViewById(R.id.fullscreenbt)
        detailBt = view.findViewById(R.id.detailbt)
        btUserControl = view.findViewById(R.id.focusMe)
        imagebtUserControl = view.findViewById(R.id.imagefocusme)
        pauseView = view.findViewById(R.id.pauseView)
        resumeView = view.findViewById(R.id.resumeView)
        btWatchEvent = view.findViewById(R.id.watchEvent)
        detailwrapevent = view.findViewById(R.id.wrapdetailevent)                       //VIEW LAIN
        progressBar = view.findViewById(R.id.progressBar)
        timerView = view.findViewById(R.id.timertextview)
        textstt = view.findViewById(R.id.subscribeeventstt)
        statusRoute = view.findViewById(R.id.routestatustext)
        textDistanceToEvent = view.findViewById(R.id.distancetoevent)
        wrapDistanceToEvent = view.findViewById(R.id.distancetoeventwrap)
        textSpeedAvg = view.findViewById(R.id.speedAvgView)
        imageWatchEvent = view.findViewById(R.id.imageWatchEvent)
        registerSensor()

        if(eventNama == "" || eventNama == null || mainEvent == "false"){
            btWatchEvent.visibility = View.GONE
            imageWatchEvent.visibility = View.GONE
            buttonUnsubEvent.visibility = View.GONE
            buttonrefreshroute.visibility = View.GONE
        }else{
            textstt.text = eventNama
            btWatchEvent.visibility = View.VISIBLE
            imageWatchEvent.visibility = View.VISIBLE
            buttonUnsubEvent.visibility = View.VISIBLE
            buttonrefreshroute.visibility = View.VISIBLE
        }
        return view
    }

    @SuppressLint("ResourceAsColor", "ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        mapView?.let{mapView ->
            mapView.setMapOrientation(0F, true)
            mapView.setTileSource(TileSourceFactory.MAPNIK)
            mapView.controller.setZoom(18.0)
            mapView.controller.setCenter(GeoPoint(-7.8195106358, 112.007007986))
            mapView.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            mapView.setMultiTouchControls(true)
            val btPause:Button = view.findViewById(R.id.btPause)
            val btResume:Button = view.findViewById(R.id.btResume)
            val buttonRouteToEvent:Button = view.findViewById(R.id.routetoevent)
            if (permissionUtils.hasLocationPermission()) startLocationUpdates(mapView)
            if((eventGpx != "" && eventGpx != null) && mainEvent == "true"){
                loadGpxFile(eventGpx.toString(), false)
            }else {
                wrapDistanceToEvent.visibility = View.GONE
                statusRoute.text = "tidak ada event"
            }
            if(startEvent) {
                startEv.text = "Selesai"
                pauseView.visibility = View.VISIBLE
            }else {
                pauseView.visibility = View.GONE
                resumeView.visibility = View.GONE
                startEv.text = "Mulai"
            }

            //BUTTON CLICK LISTENER
            btWatchEvent.setOnClickListener {
                if(mainEvent == "true"){
                    if(permissionUtils.isInternetConnected()){
                        val url = "https://kbt.us.to/tracker/livetracker/event/$eventId"
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(url)
                        startActivity(intent)
                    }else{
                        showAlert(context, "Untuk menonton event yang sedang berlangsung, mohon nyalakan internet")
                    }
                }
            }
            buttonFullScreen.setOnClickListener{
                val relativeAll: RelativeLayout = view.findViewById(R.id.relativeall)
                val marginInDp = 129
                val density = resources.displayMetrics.density
                val marginInPx = (marginInDp * density).toInt()

                if (relativeAll.layoutParams is RelativeLayout.LayoutParams) {
                    val layoutParams = relativeAll.layoutParams as RelativeLayout.LayoutParams
                    if (layoutParams.bottomMargin == marginInPx) {
                        layoutParams.bottomMargin = 0
                        relativeAll.layoutParams = layoutParams
                        startEv.visibility = View.GONE
                        btUserControl.visibility = View.GONE
                        imagebtUserControl.visibility = View.GONE
                        btWatchEvent.visibility = View.GONE
                        imageWatchEvent.visibility = View.GONE
                        (activity as? MainActivity)?.toggleBottomNavigationVisibility(View.GONE)
                        buttonFullScreen.setImageResource(R.drawable.ic_closefullscreen)
                    } else {
                        layoutParams.bottomMargin = marginInPx
                        relativeAll.layoutParams = layoutParams
                        startEv.visibility = View.VISIBLE
                        btUserControl.visibility = View.VISIBLE
                        imagebtUserControl.visibility = View.VISIBLE
                        if(mainEvent == "true"){
                            btWatchEvent.visibility = View.VISIBLE
                            imageWatchEvent.visibility = View.VISIBLE
                        }
                        (activity as? MainActivity)?.toggleBottomNavigationVisibility(View.VISIBLE)
                        buttonFullScreen.setImageResource(R.drawable.ic_fullscreen)
                    }
                }
            }
            btPause.setOnClickListener {
                pauseView.visibility = View.GONE
                resumeView.visibility = View.VISIBLE
                EventBus.getDefault().post(StatusTimeChangeEvent(status = "pause"))
            }
            btResume.setOnClickListener {
                resumeView.visibility = View.GONE
                pauseView.visibility = View.VISIBLE
                EventBus.getDefault().post(StatusTimeChangeEvent(status = "resume"))
            }
            startEv.setOnClickListener {
                if (startEvent) {
                    if(fileTxtCreated){
                        stopLocationService()
                        startLocationUpdates(mapView)
                        removeDataEvent(false)
                        editor.putString("follow_event", true.toString())
                        editor.apply()
                        EventBus.getDefault().unregister(this)
                        activity.supportFragmentManager.beginTransaction()
                            .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
                            .replace(R.id.fragment_container, DetailAfterMedalFragment())
                            .commit()
                    }else{
                        showAlert(context, "Belum ada aktivitas medal yang tersimpan, silahkan medal")
                    }
                } else {
                    stopLocationUpdate()
                    if(permissionUtils.isInternetConnected()){
                        pauseView.visibility = View.VISIBLE
                        if(eventId == null || eventId == ""){
                            createEvent()
                        }else{
                            val evIdx:Int = eventId!!.toInt()
                            val savedb = MedalOfflineDatabase(context)
                            savedb.insert(evIdx, "event_live", "location_data-${eventId}.txt")
                            startLocationService(eventId.toString())
                        }
                        startEvent = true
                        editor.putString("start_event", true.toString())
                        editor.apply()
                        startEv.text = "Selesai"
                    }else{
                        showAlert(context, "untuk memulai medal, internet harus aktif.")
                    }
                }
            }
            buttonrefreshroute.setOnClickListener {
                editor.putString("alert_to_event_route", "false")
                editor.apply()
                removeRoute()
                loadGpxFile(eventGpx.toString(), true)
            }
            btUserControl.setOnClickListener{
                Log.i("KBTAPP", "fokuskan")
                userControlMap = false
                locationMarker.userControlMap = false
                rotationDifferentMap = 0F
                mapView.mapOrientation = 0F
                editor.putString("alert_to_event_route", "false")
                editor.apply()
                if(lastKnownLocation != null) {
                    if((eventGpx != "" && eventGpx != null) && mainEvent == "true"){
                        roadEvent?.let{
                            nearLocRoad = forgingMap.calculateDistance(lastKnownLocation as Location, it, textDistanceToEvent)
                        }
                    }
                    locationMarker.update(lastKnownLocation as Location)
                }
            }
            buttonRouteToEvent.setOnClickListener {
                lifecycleScope.launch {
                    currentLoc?.let{cl-> nearLocRoad?.let {
                        drawRouteToEvent(cl, it)
                    }}
                }
            }
            detailBt.setOnClickListener {
                if(detailwrapevent.visibility == View.VISIBLE){
                    Log.i("KBTAPP", "klik di detail")
                    detailwrapevent.animate()
                        .translationY(detailwrapevent.height.toFloat())
                        .setDuration(300)
                        .withEndAction { detailwrapevent.visibility = View.GONE }
                }else{
                    Log.i("KBTAPP", "klik no di detail")
                    detailwrapevent.visibility = View.VISIBLE
                    detailwrapevent.translationY = detailwrapevent.height.toFloat()
                    detailwrapevent.animate()
                        .setDuration(300)
                        .translationY(0f)
                }
            }
            buttonUnsubEvent.setOnClickListener { unsubEvent(buttonrefreshroute) }

            //MAP TOUCH LISTENER
            mapView.setOnTouchListener(object : View.OnTouchListener {
                @SuppressLint("ClickableViewAccessibility")
                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                    when (event?.action) {
                        MotionEvent.ACTION_POINTER_DOWN, 261 -> {
                            if (event.pointerCount == 2) {
                                handlerSensor.removeCallbacksAndMessages(null)
                                unRegisterSensor()
                                initialDeltaX1 = event.getX(0)
                                initialDeltaY1 = event.getY(0)
                                initialDeltaX2 = event.getX(1)
                                initialDeltaY2 = event.getY(1)
                                initialRotationMap = myRotation(
                                    event.getX(0),
                                    event.getY(0),
                                    event.getX(1),
                                    event.getY(1)
                                )
                            }
                        }
                        MotionEvent.ACTION_MOVE -> {
                            userControlMap = true
                            locationMarker.userControlMap = true
                            if (event.pointerCount == 2) {
                                val dX1 = event.getX(0)
                                val dY1 = event.getY(0)
                                val dX2 = event.getX(1)
                                val dY2 = event.getY(1)
                                if (initialDeltaX1 != dX1 && initialDeltaX2 != dX2 && initialDeltaY1 != dY1 && initialDeltaY1 != dY1) {
                                    val rotation = myRotation(dX1, dY1, dX2, dY2)
                                    val rotationDifference = (rotation - initialRotationMap + 180) % 360 - 180
                                    mapView.mapOrientation += rotationDifference
                                    val rotateMark = lastMarkerRotation + (-rotationDifference)
                                    rotationDifferentMap += -rotationDifference
                                    marker.rotation = rotateMark
                                    lastMarkerRotation = rotateMark
                                    initialRotationMap = rotation
                                    initialDeltaX1 = dX1
                                    initialDeltaY1 = dY1
                                    initialDeltaX2 = dX2
                                    initialDeltaY2 = dY2
                                }
                            }
                        }
                        MotionEvent.ACTION_UP -> {
                            handlerSensor.postDelayed({registerSensor()}, 3000)
                        }
                    }
                    return false
                }

                private fun myRotation(X1:Float, Y1:Float, X2:Float, Y2:Float): Float {
                    val deltaX = X1.toDouble() - X2.toDouble()
                    val deltaY = Y1.toDouble() - Y2.toDouble()
                    val radians = atan2(deltaY, deltaX)
                    return Math.toDegrees(radians).toFloat()
                }
            })
        }
    }

    //SENSOR MANAGEMENT
    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> System.arraycopy(event.values, 0, accelerometerValues, 0, accelerometerValues.size)
                Sensor.TYPE_MAGNETIC_FIELD -> System.arraycopy(event.values,0,magnetometerValues,0,magnetometerValues.size)
            }
            if(!rotateMarker.isAnimatingMark){
                SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerValues, magnetometerValues)
                val orientationValues = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientationValues)
                val azimuth = -orientationValues[0]
                val azimuthDegree = Math.toDegrees(azimuth.toDouble()).toFloat()
                val deltaDegree = azimuthDegree - lastMarkerRotation
                initialDeltaDegree += deltaDegree
                if(abs(initialDeltaDegree) >= 5.0) {
                    if (abs(initialDeltaDegree) < 10.0) rotateMarker.duration = 300L
                    else if (abs(initialDeltaDegree) < 20.0) rotateMarker.duration = 400L
                    else if(abs(initialDeltaDegree) < 30.0) rotateMarker.duration = 500L
                    else if(abs(initialTotalDegree) < 40.0) rotateMarker.duration = 600L
                    else rotateMarker.duration = 700L
                    rotateMarker.animateRotateMarker(azimuthDegree + rotationDifferentMap)
                    initialDeltaDegree = 0F
                }
                lastMarkerRotation = azimuthDegree + rotationDifferentMap
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }
    private fun registerSensor(){
        if (accelerometerSensor != null && magnetometerSensor != null) {
            sensorManager.registerListener(
                sensorEventListener,
                accelerometerSensor,
                SensorManager.SENSOR_DELAY_UI
            )
            sensorManager.registerListener(
                sensorEventListener,
                magnetometerSensor,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }
    private fun unRegisterSensor(){
        sensorManager.unregisterListener(sensorEventListener)
    }

    private fun createEvent(){
        val apiclient = ApiClient()
        apiclient.postData(
            "https://kbt.us.to/events/add/api/",
            """{}""",
            object : ApiResponseCallback {
                override fun onSuccess(jsonObject: JSONObject, resCode:Int) {
                    Log.i("KBTAPP", "BUAT EVENT BARU: ${jsonObject}")
                    val resid = jsonObject.getString("id")
                    val resnama = jsonObject.getString("nama")
                    startLocationService(resid)
                    editor.putString("event_id", resid)
                    editor.putString("event_nama", resnama)
                    if(jsonObject.getString("is_event") == "true") editor.putString("main_event", true.toString())
                    else editor.putString("main_event", false.toString())
                    editor.apply()
                    activity.runOnUiThread { textstt.text = resnama }
                    val savedb = MedalOfflineDatabase(context)
                    savedb.insert(resid.toInt(), resnama, "location_data-${resid}.txt")
                }
                override fun onFailure(error: String) {
                    Log.e("KBTPAPP","Error: $error")
                }
            }
        )
    }

    @Subscribe
    fun onFileCreatedEvent(event:FileCreatedEvent){
        Log.d("KBTAPP", "PANGGIL EVENT BUS BAHWA FILE TXT CREATED")
        fileTxtCreated = event.status
        editor.putString("file_txt_created", event.status.toString())
        editor.apply()
    }

    @Subscribe
    fun onTimeChangeEvent(event: TimeChangeEvent) {
        val currentTime = event.currentTime
        val minutes = currentTime / 60
        val remainingSeconds = currentTime % 60
        val timeString = String.format("%02d:%02d", minutes, remainingSeconds)
        timerView.text = timeString
    }

    //GPX
    private fun loadGpxFile(gpxUrl: String, refresh:Boolean) {
        statusRoute.text = "rute sedang diunduh"
        progressBar.progress = 0
        progressBar.visibility = View.VISIBLE
        scopeDrawGpx.launch {
            try {
                val gpxFile = downloadGpxFile(gpxUrl, refresh)
                withContext(Dispatchers.Main){ progressBar.progress = 30 }
                val road = loadRoadFromGpxFile(gpxFile)
                roadEvent = road
                Log.i("KBTAPP", "GPX ROAD: ${road.toString()}")

                withContext(Dispatchers.Main) {
                    progressBar.progress = 100
                    progressBar.visibility = View.GONE
                    statusRoute.text = "sukses"
                    road?.let{ r -> currentLoc?.let{
                        nearLocRoad = forgingMap.calculateDistance(it, r, textDistanceToEvent)
                    } }
                    mapView?.let{ mv->
                        road?.let { forgingMap.drawRoad(it, mv, Color.rgb(27, 191, 123)) }
                    }
                }
            } catch (e: Exception) {
                Log.e("KBTAPP", "kesalahan GPX: ${e.message}")
                withContext(Dispatchers.Main) {
                    progressBar.progress = 100
                    progressBar.visibility = View.GONE
                    statusRoute.text = "terjadi kesalahan"
                    e.printStackTrace()
                }
            }
        }
    }
    private fun downloadGpxFile(gpxUrl: String, refresh: Boolean): File {
        var redirectedUrl = gpxUrl
        var urlConnection: HttpURLConnection? = null
        try {
            val directory = File(context.filesDir, "route")
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val gpxFile = File(directory, "route-${eventId}.gpx")
            if(!gpxFile.exists() || refresh){
                do {
                    val url = URL(redirectedUrl)
                    urlConnection = url.openConnection() as HttpURLConnection
                    urlConnection.instanceFollowRedirects = false
                    val responseCode = urlConnection.responseCode
                    if (responseCode == HttpURLConnection.HTTP_MOVED_PERM || responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                        redirectedUrl = urlConnection.getHeaderField("Location")
                    }
                } while (responseCode == HttpURLConnection.HTTP_MOVED_PERM || responseCode == HttpURLConnection.HTTP_MOVED_TEMP)
                if (urlConnection?.responseCode == HttpURLConnection.HTTP_OK) {
                    gpxFile.outputStream().use { output ->
                        urlConnection?.inputStream.use { input ->
                            input?.copyTo(output)
                        }
                    }
                    return gpxFile
                } else {
                    Log.e("KBTAPP", "HTTP Response Code: ${urlConnection?.responseCode}")
                }
            }else{
                return gpxFile
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("KBTAPP", "Error downloading GPX: ${e.message}")
        } finally {
            urlConnection?.disconnect()
        }

        return File("")
    }
    private suspend fun loadRoadFromGpxFile(gpxFile: File): Road? {
        try {
            withContext(Dispatchers.Main){
                statusRoute.text = "menggambar rute"
            }
            val waypoints = arrayListOf<GeoPoint>()
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            val inputStream = FileInputStream(gpxFile)
            parser.setInput(inputStream, null)
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "trkpt" -> {
                                val lat = parser.getAttributeValue(null, "lat")
                                val lon = parser.getAttributeValue(null, "lon")
                                if (lat != null && lon != null) {
                                    val geoPoint = GeoPoint(lat.toDouble(), lon.toDouble())
                                    waypoints.add(geoPoint)
                                }
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
            withContext(Dispatchers.Main){
                progressBar.progress = 70
            }
            if (waypoints.isNotEmpty()) {
                return OSRMRoadManager(context, "kbtkedunglo").getRoad(waypoints)
            } else {
                Log.e("KBTAPP", "Tidak ada waypoint yang ditemukan dalam GPX file.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("KBTAPP", "Error parsing GPX: ${e.message}")
        }
        return null
    }
    private fun drawRouteToEvent(location: Location, roadLc: Location){
        scopeDrawRouteToEvent.launch {
            try{
                val roadManager: RoadManager = OSRMRoadManager(context, "kbtkedunglo")
                val waypoints: ArrayList<GeoPoint> = arrayListOf()
                val startPoint = GeoPoint(location.latitude, location.longitude, location.altitude)
                val endPoint = GeoPoint(roadLc.latitude, roadLc.longitude, location.altitude)
                waypoints.add(startPoint)
                waypoints.add(endPoint)
                val road:Road = roadManager.getRoad(waypoints)
                withContext(Dispatchers.Main) {
                    try {
                        mapView?.let { mv ->
                            val newbacktoEvent = forgingMap.drawRoad(road, mv, Color.rgb(247, 54, 93))
                            roadBackToEventOverlay = newbacktoEvent
                        }
                    }catch (e:Exception){
                        Log.e("KBTAPP", "Kesalahan dalam membuat rute ke event: ${e.message}")
                    }
                }
            }catch(e:IOException){
                Log.e("KBTAPP", "KEsalahan di awal pembuatan:${e.message}")
                e.printStackTrace()
            }
        }
    }
    private fun removeRoute(){
        mapView?.let { mapView ->
            mapView.overlays.forEach { overlay ->
                if (overlay !is Marker) {
                    mapView.overlays.remove(overlay)
                }
            }
            mapView.invalidate()
        }
    }

    private fun unsubEvent(btrefresh:Button){
        stopLocationService()
        mapView?.let{startLocationUpdates(it) }
        removeDataEvent(true)
        scopeDrawGpx?.cancel()
        scopeDrawRouteToEvent?.cancel()
        progressBar.progress = 0
        progressBar.visibility = View.GONE
        removeRoute()
        editor.putString("alert_to_event_route", "false")
        editor.apply()
        buttonUnsubEvent.visibility = View.GONE
        wrapDistanceToEvent.visibility = View.GONE
        btrefresh.visibility = View.GONE
        statusRoute.text = "tidak ada event"
    }
    private fun stopLocationService() {
        val serviceIntent = Intent(context, LocJobService::class.java)
        context.stopService(serviceIntent)
    }

    // Location Marker Changed
    @Subscribe
    fun onLocationChangeEvent(event: LocationChangeEvent){
        lastKnownLocation = event.location
        locationMarker.update(event.location)
    }
    @Subscribe
    fun onTimeAverageSpeedChange(event: LocationWithSpeedAverageChange) {
        locationMarker.update(event.location)
        val speeds = event.speed
        textSpeedAvg.text = "${speeds} km/jam"
    }
    private fun stopLocationUpdate(){
        locationCallback?.let{ fusedLocationClientFragment.removeLocationUpdates(it) }
    }
    private fun startLocationUpdates(map:MapView) {
        try {
            val locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(2000L)
                .setFastestInterval(2000L)
                .setSmallestDisplacement(maxDistanceScreen)

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    locationResult?.lastLocation?.let { location ->
                        currentLoc = location
                        lastKnownLocation = location
                        locationMarker.update(location)
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
    //LOCATION SERVICE BACKGROUND
    private fun startLocationService(eId:String) {
        val serviceIntent = Intent(context, LocJobService::class.java)
        serviceIntent.putExtra("userId", userId)
        serviceIntent.putExtra("eventId", eId)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }

    private fun removeDataEvent(removeAll:Boolean){
        textstt.text = ""
        startEv.text = "Mulai"
        startEvent = false
        eventNama = ""
        eventGpx = ""
        eventId = null
        pauseView.visibility = View.GONE
        resumeView.visibility = View.GONE
        if(removeAll){
            editor.putString("start_event", false.toString())
            editor.putString("main_event", false.toString())
            editor.putString("event_id", "")
            editor.putString("event_nama", "")
            editor.putString("event_gpx", "")
            editor.apply()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scopeDrawGpx.cancel()
        scopeDrawRouteToEvent.cancel()
        EventBus.getDefault().unregister(this)
        unRegisterSensor()
        mapView?.let{
            it.overlays.clear()
            it.invalidate()
            locationCallback?.let {
                fusedLocationClientFragment.removeLocationUpdates(it)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onPause() {
        super.onPause()
        unRegisterSensor()
        mapView?.onPause()
        EventBus.getDefault().post(ScreenStatusEvent(false))
    }

    override fun onStop() {
        super.onStop()
        unRegisterSensor()
        mapView?.let{
            it.overlays.clear()
            it.invalidate()
        }
        EventBus.getDefault().post(ScreenStatusEvent(false))
    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().post(ScreenStatusEvent(true))
        eventId = sharedPreferences.getString("event_id", "")
        userId =  sharedPreferences.getString("id", "")
        registerSensor()
        Log.i("KBTAPP", "LOG ID EVENT DI ONRESUME:${eventId}")
        mapView?.onResume()
    }
}
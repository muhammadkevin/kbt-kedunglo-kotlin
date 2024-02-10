package com.example.kbtkedunglo.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.example.kbtkedunglo.R
import com.example.kbtkedunglo.pages.DetailTimeLineFragment
import com.example.kbtkedunglo.pages.reusable.CommentPostFragment
import com.example.kbtkedunglo.pages.reusable.LikePostFragment
import com.example.kbtkedunglo.utilsclass.formatDurationMedal
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.round

class TimeLineAdapter( private val eventArray: JSONArray, private val fragmentManager:FragmentManager) :
    RecyclerView.Adapter<TimeLineAdapter.TimeLineViewHolder>() {

    class TimeLineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val namaUser:TextView = itemView.findViewById(R.id.username)
        val namaevent: TextView = itemView.findViewById(R.id.namaevent)
        val mapThumbnailView: ImageView = itemView.findViewById(R.id.imagethumb)
        val jarakTempuh:TextView = itemView.findViewById(R.id.jaraktempuh)
        val avgSpeed:TextView = itemView.findViewById(R.id.avgspeed)
        val elevgain:TextView = itemView.findViewById(R.id.elevgain)
        val duration:TextView = itemView.findViewById(R.id.duration)
        val detailTime:TextView = itemView.findViewById(R.id.detailtime)
        val profilImage:ImageView = itemView.findViewById(R.id.profileImage)
        val wrapContent:LinearLayout = itemView.findViewById(R.id.wrapContent)
        val wrapMap:RelativeLayout = itemView.findViewById(R.id.wrapMap)
        val descriptionEvent:TextView = itemView.findViewById(R.id.descriptionEvent)
        val btLike:RelativeLayout = itemView.findViewById(R.id.btLike)
        val btComment:RelativeLayout = itemView.findViewById(R.id.btComment)
        val btShare:RelativeLayout = itemView.findViewById(R.id.btShare)
        val imgLike:ImageView = itemView.findViewById(R.id.imgLike)
        val imgComment:ImageView = itemView.findViewById(R.id.imgComment)
        val imgShare:ImageView = itemView.findViewById(R.id.imgShare)
        val whoLike:LinearLayout = itemView.findViewById(R.id.whoLike)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeLineViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.card_timeline, parent, false)
        return TimeLineViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeLineViewHolder, position: Int) {
        try {
            val eventData: JSONObject = eventArray.getJSONObject(position)
            Log.i("KBTAPP", eventData.toString())
            val kbtuserid = eventData.getString("kbtuser")
            val eventid = eventData.getString("event")
            val nama = eventData.getString("kbtuser_nama")
            val eventName = eventData.getString("event_nama")
            val mapThmb = eventData.getString("map_thumbnail")
            val fotoUrl = eventData.getString("foto_url")
            val distanceStr = eventData.getString("distance").toFloat()
            val avgSpeed = eventData.getString("average_speed").toFloat()
            val elevgain = eventData.getString("elevation_gain").toFloat()
            val duration = eventData.getString("moving_time").toFloat().toInt()
            val waktu = eventData.getString("created_at")
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSX", Locale.getDefault())
            val date = inputFormat.parse(waktu)
            val outputFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id"))
            outputFormat.timeZone = TimeZone.getTimeZone("Asia/Jakarta")
            val detailtimeR = outputFormat.format(date as Date)
            val jaraktempuhR = (round(distanceStr * 100.0) / 100.0).toString()
            val avgspeedR = (round(avgSpeed * 100.0) / 100.0).toString()
            val elevationR = (round(elevgain * 100.0) / 100.0).toString()
            val durationR = formatDurationMedal(duration)
            var description = eventData.getString("description")
            if( description== "null" ){
                description = ""
            }
            holder.descriptionEvent.text = description
            holder.namaUser.text = nama
            holder.detailTime.text = detailtimeR
            holder.namaevent.text = eventName
            holder.jarakTempuh.text = jaraktempuhR
            holder.avgSpeed.text = avgspeedR
            holder.elevgain.text = elevationR
            holder.duration.text = durationR
            if(mapThmb != "null" && mapThmb != null){
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val bitmap = withContext(Dispatchers.IO) {
                            Picasso.get().load("https:kbt.us.to/${mapThmb}").get()
                        }
                        withContext(Dispatchers.Main) {
                            holder.mapThumbnailView.setImageBitmap(bitmap)
                        }
                    } catch (e: Exception) {
                        Log.e("KBTAPP", "Error draw image: ${e.message}")
                        e.printStackTrace()
                    }
                }
            }
            if(fotoUrl != "null" && fotoUrl != null){
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val bitmapFtProfil = withContext(Dispatchers.IO) {
                            Picasso.get().load("https://kbt.us.to/media/${fotoUrl}").get()
                        }
                        withContext(Dispatchers.Main) {
                            holder.profilImage.setImageBitmap(bitmapFtProfil)
                        }
                    } catch (e: Exception) {
                        Log.e("KBTAPP", "Error draw image: ${e.message}")
                        e.printStackTrace()
                    }
                }
            }else{
                holder.profilImage.setImageResource(R.drawable.profilkosongl)
            }

            // CLICK LISTENER
            holder.wrapContent.setOnClickListener {
                val fragment = DetailTimeLineFragment.newInstance(
                    kbtuserid, eventid, nama, eventName, detailtimeR, jaraktempuhR, avgspeedR, elevationR,
                    durationR, "https://kbt.us.to/media/${fotoUrl}"
                )
                fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }
            holder.wrapMap.setOnClickListener {
                Log.d("KBTAPP", "KLIK DI MAP")
            }
            holder.whoLike.setOnClickListener { whoLikePost() }
            holder.btLike.setOnClickListener { likePost() }
            holder.btComment.setOnClickListener { commentPost() }
            holder.btShare.setOnClickListener { sharePost() }
        } catch (e: Exception) {
            Log.e("KBTAPP", e.message.toString())
            e.printStackTrace()
        }
    }

    private fun whoLikePost(){
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, LikePostFragment())
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    private fun likePost(){
        Log.v("KBTAPP", "Like Post")
    }

    private fun commentPost(){
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, CommentPostFragment())
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    private fun sharePost(){
        Log.v("KBTAPP", "Share Post")
    }

    override fun getItemCount(): Int {
        return eventArray.length()
    }
}
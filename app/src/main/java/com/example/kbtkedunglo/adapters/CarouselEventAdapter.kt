package com.example.kbtkedunglo.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.kbtkedunglo.R
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

interface CarousellEventClickListener {
    fun onCarousellEventClick(position: Int, idEvent: String, namaEvent: String,
                              gpxFile: String, description:String, waktuMulai:String, waktuSelesai:String)
}
class CarouselEventAdapter(private val context: Context,
                           val eventArray: JSONArray?,
                           private val carousellEventClickListener: CarousellEventClickListener) :
    RecyclerView.Adapter<CarouselEventAdapter.CarouselViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.carousel_event, parent, false)
        return CarouselViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
        if(eventArray != null){
            val eventData: JSONObject = eventArray.getJSONObject(position)
            val eventName = eventData.getString("nama")
            val logoUrl = eventData.getString("logo")
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val bitmap = withContext(Dispatchers.IO) {
                        Picasso.get().load(logoUrl).get()
                    }
                    withContext(Dispatchers.Main) {
                        holder.imageCarousel.setImageBitmap(bitmap)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            holder.imageCarousel.setOnClickListener {
                val idEvent = eventData.getString("id")
                val namaEvent = eventData.getString("nama")
                val gpxFile = eventData.getString("gpx_file")
                val description = eventData.getString("detail")
                val waktuMulai = eventData.getString("event_start")
                val waktuSelesai = eventData.getString("event_end")
                carousellEventClickListener.onCarousellEventClick(position, idEvent, namaEvent, gpxFile, description, waktuMulai, waktuSelesai)
            }
        }
    }

    override fun getItemCount(): Int {
        if(eventArray != null) return eventArray.length()
        else return 0
    }

    inner class CarouselViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageCarousel:ImageView = itemView.findViewById(R.id.logocarousel)
    }
}
package com.example.kbtkedunglo.adapters
// EventAdapter.kt
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kbtkedunglo.R
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale


interface EventClickListener {
    fun onEventClick(position: Int, idEvent: String, namaEvent: String, gpxFile: String)
}
class EventAdapter(
    private val eventArray: JSONArray,
    private val eventClickListener: EventClickListener
    ) :
    RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val eventImageView: ImageView = itemView.findViewById(R.id.eventImageView)
        val eventNameTextView: TextView = itemView.findViewById(R.id.eventNameTextView)
        val eventNameChannel: TextView = itemView.findViewById(R.id.eventNameChannel)
        val eventStartTime: TextView = itemView.findViewById(R.id.eventStartTime)
        val eventEndTime: TextView = itemView.findViewById(R.id.eventEndTime)
        val subcribeEventButton: Button = itemView.findViewById(R.id.btsubscribeevent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.card_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        try {
            val eventData: JSONObject = eventArray.getJSONObject(position)
            val eventName = eventData.getString("nama")
            val logoUrl = eventData.getString("logo")
            val eventChannel = eventData.getString("nama_channel")
            val dateStart = OffsetDateTime.parse(eventData.getString("event_start"))
            val zonedDateStart = ZonedDateTime.ofInstant(dateStart.toInstant(), dateStart.offset)
            val dateEnd = OffsetDateTime.parse(eventData.getString("event_end"))
            val zonedDateEnd = ZonedDateTime.ofInstant(dateEnd.toInstant(), dateEnd.offset)
            val formatter = DateTimeFormatter.ofPattern("HH:mm, dd MMMM yyyy", Locale("id"))

            holder.eventNameTextView.text = eventName
            holder.eventNameChannel.text = eventChannel
            holder.eventImageView.setImageResource(R.drawable.placeholder_image)
            holder.eventStartTime.text = formatter.format(zonedDateStart)
            holder.eventEndTime.text = formatter.format(zonedDateEnd)
//            holder.itemView.setOnClickListener {
//                val idEvent = eventData.getString("id")
//                eventClickListener.onEventClick(position, idEvent)
//            }
            holder.subcribeEventButton.setOnClickListener {
                val idEvent = eventData.getString("id")
                val namaEvent = eventData.getString("nama")
                val gpxFile = eventData.getString("gpx_file")
                eventClickListener.onEventClick(position, idEvent, namaEvent, gpxFile)
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val bitmap = withContext(Dispatchers.IO) {
                        Picasso.get().load(logoUrl).get()
                    }

                    withContext(Dispatchers.Main) {
                        holder.eventImageView.setImageBitmap(bitmap)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            Log.e("KBTAPP", e.message.toString())
        }
    }

    override fun getItemCount(): Int {
        return eventArray.length()
    }
}



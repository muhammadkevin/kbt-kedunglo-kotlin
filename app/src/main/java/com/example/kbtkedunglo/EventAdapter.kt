package com.example.kbtkedunglo
// EventAdapter.kt
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject


class EventAdapter(private val eventArray: JSONArray) :
    RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val eventImageView: ImageView = itemView.findViewById(R.id.eventImageView)
        val eventNameTextView: TextView = itemView.findViewById(R.id.eventNameTextView)
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

            holder.eventNameTextView.text = eventName
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val bitmap = withContext(Dispatchers.IO) {
                        // Muat gambar di latar belakang
                        Picasso.get().load(logoUrl).get()
                    }

                    // Setelah gambar dimuat, tampilkan di ImageView
                    withContext(Dispatchers.Main) {
                        holder.eventImageView.setImageBitmap(bitmap)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
//            Picasso.get()
//                .load(logoUrl)
//                .placeholder(R.drawable.placeholder_image)
//                .error(R.drawable.error_image)
//                .resize(20, 20)         //optional
//                .centerCrop()                        //optional
//                .into(holder.eventImageView);
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return eventArray.length()
    }
}



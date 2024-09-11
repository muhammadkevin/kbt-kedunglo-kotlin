package com.example.kbtkedunglo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kbtkedunglo.R
import com.example.kbtkedunglo.services.MedalOfflineDatabase

interface EventClickListenerMedalOffline {
    fun onEventClick(position: Int,
                     idMf:Int,
                     eventId: String,
                     eventFile: String,
                     progressBar: ProgressBar,
                     uploadBt: Button)
}

interface DetailClickMedalOffline {
    fun onDetailClick(idMf:Int, eventFile:String)
}
class MedalOfflineAdapter(
    private var medalOfflineList: List<MedalOfflineDatabase.MedalOffline>,
    private val eventClickListener: EventClickListenerMedalOffline,
    private val detailClickListener: DetailClickMedalOffline
) : RecyclerView.Adapter<MedalOfflineAdapter.MedalOfflineViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedalOfflineViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_medal_offline, parent, false)
        return MedalOfflineViewHolder(view)
    }

    fun removeItem(position: Int) {
        if (position in 0 until medalOfflineList.size) {
            medalOfflineList = medalOfflineList.toMutableList().apply {
                removeAt(position)
            }
            notifyItemRemoved(position)
        }
    }

    fun addItem(newItem: MedalOfflineDatabase.MedalOffline) {
        medalOfflineList = medalOfflineList.toMutableList().apply {
            add(newItem)
        }
        notifyItemInserted(medalOfflineList.size - 1)
    }

    override fun onBindViewHolder(holder: MedalOfflineViewHolder, position: Int) {
        val medaloffline = medalOfflineList[position]
        holder.bind(medaloffline)
        holder.uploadServerBt.setOnClickListener {
            eventClickListener.onEventClick(position,
                medaloffline.idx,
                medaloffline.eventId,
                medaloffline.eventFile,
                holder.progressBar,
                holder.uploadServerBt)
        }
        holder.detailBt.setOnClickListener {
            detailClickListener.onDetailClick(medaloffline.idx, medaloffline.eventFile)
        }
    }

    override fun getItemCount(): Int = medalOfflineList.size

    class MedalOfflineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(employee: MedalOfflineDatabase.MedalOffline) {
            itemView.findViewById<TextView>(R.id.namaevent).text = employee.eventNama
        }
        val uploadServerBt:Button = itemView.findViewById(R.id.btuploadevent)
        val detailBt:Button = itemView.findViewById(R.id.btndetail)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
    }
}

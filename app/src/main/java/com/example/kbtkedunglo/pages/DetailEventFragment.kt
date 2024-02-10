package com.example.kbtkedunglo.pages

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.kbtkedunglo.MainActivity
import com.example.kbtkedunglo.R


class DetailEventFragment : Fragment() {
    private lateinit var namaEventTxt:TextView                          //INIT VIEW
    private lateinit var descriptionEventTxt:TextView
    private lateinit var mulaiTxt:TextView
    private lateinit var selesaiTxt:TextView
    private lateinit var jumlahPesertaTxt:TextView
    private lateinit var statusMeTxt:TextView
    private lateinit var buttonRoute:Button
    private lateinit var statusMe:String                                //INIT DATA
    private lateinit var descriptionEvent:String
    private lateinit var waktuMulai:String
    private lateinit var waktuSelesai: String
    private val sharedPreferences: SharedPreferences by lazy {
        requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
    }
    private lateinit var editor:SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        editor = sharedPreferences.edit()
        arguments?.let {
            descriptionEvent = it.getString("description_event", "")
            waktuMulai = it.getString("waktu_mulai", "")
            waktuSelesai = it.getString("waktu_selesai", "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view:View = inflater.inflate(R.layout.fragment_detail_event, container, false)
        namaEventTxt = view.findViewById(R.id.namaEvent)
        descriptionEventTxt = view.findViewById(R.id.deskripsiEvent)
        mulaiTxt = view.findViewById(R.id.tanggalMulai)
        selesaiTxt = view.findViewById(R.id.tanggalSelesai)
        jumlahPesertaTxt = view.findViewById(R.id.jumlahPeserta)
        statusMeTxt = view.findViewById(R.id.statusMe)
        buttonRoute = view.findViewById(R.id.buttonRoute)
        
        val buttonScan:Button = view.findViewById(R.id.buttonScan)
        buttonScan.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ScanqrFragment())
                .addToBackStack(null)
                .commit()
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        namaEventTxt.text = sharedPreferences.getString("event_nama", "")
        descriptionEventTxt.text = descriptionEvent
        mulaiTxt.text = waktuMulai
        selesaiTxt.text = waktuSelesai
        jumlahPesertaTxt.text = "0"
        statusMeTxt.text = "Status Saya"

        buttonRoute.setOnClickListener {
            (activity as? MainActivity)?.changeSelectedItemId(R.id.menu_medal)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(descriptionEvent: String, waktuMulai:String, waktuSelesai:String) =
            DetailEventFragment().apply {
                arguments = Bundle().apply {
                    putString("description_event", descriptionEvent)
                    putString("waktu_mulai", waktuMulai)
                    putString("waktu_selesai", waktuSelesai)
                }
            }
    }
}
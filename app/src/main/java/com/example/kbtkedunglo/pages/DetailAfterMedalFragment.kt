package com.example.kbtkedunglo.pages

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.kbtkedunglo.R
import com.example.kbtkedunglo.services.UploadTxtService
import com.example.kbtkedunglo.utilsclass.PermissionUtils
import com.example.kbtkedunglo.utilsclass.showAlert


class DetailAfterMedalFragment : Fragment() {
    private var eventId:String? = null                          //DATA INIT
    private var eventNama:String? = null
    private var userId:String? = null
    private lateinit var judulMedal:EditText                    //VIEW COMPONENT INIT
    private lateinit var descriptionMedal:EditText
    private lateinit var buttonSimpan:Button
    private lateinit var context: Context                       //UTIL INIT
    private lateinit var permissionUtils:PermissionUtils
    private lateinit var editor:SharedPreferences.Editor
    private val sharedPreferences: SharedPreferences by lazy {
        requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {}
        context = requireContext()
        editor = sharedPreferences.edit()
        userId = sharedPreferences.getString("id", "")
        eventId = sharedPreferences.getString("event_id", "")
        eventNama = sharedPreferences.getString("event_nama", "")
        permissionUtils = PermissionUtils(context)
        Log.d("KBTAPP", "follow frag:${sharedPreferences.getString("follow_event", "")}")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_detail_after_medal, container, false)
        judulMedal = view.findViewById(R.id.judulMedal)
        descriptionMedal = view.findViewById(R.id.descriptionMedal)
        buttonSimpan = view.findViewById(R.id.btnSimpanMedal)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        judulMedal.setText(eventNama)
        Log.d("KBTAPP", "nama eventnya:${eventNama}")

        buttonSimpan.setOnClickListener {
            if(permissionUtils.isInternetConnected()){
                val intent = Intent(context, UploadTxtService::class.java)
                intent.putExtra("userId", userId)
                intent.putExtra("eventId", eventId)
                intent.putExtra("judulMedal", judulMedal.text.toString())
                intent.putExtra("descriptionMedal", descriptionMedal.text.toString())
                context.startService(intent)
                uploadTxtDone()
            }else{
                showAlert(context, "untuk menyimpan aktivitas medal, internet harus aktif.")
            }
        }
    }

    private fun uploadTxtDone(){
        val activity = requireActivity()
        val fragment = StatusTimeLineFragment.newInstance(userId)
        activity.supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onResume() {
        super.onResume()
        Log.d("KBTAPP", "userId ketika layar dinyalakan lagi: $userId")
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            DetailAfterMedalFragment().apply {
                arguments = Bundle().apply {}
            }
    }
}
package com.example.kbtkedunglo.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kbtkedunglo.R
import com.example.kbtkedunglo.adapters.DetailClickMedalOffline
import com.example.kbtkedunglo.adapters.EventClickListenerMedalOffline
import com.example.kbtkedunglo.adapters.MedalOfflineAdapter
import com.example.kbtkedunglo.services.MedalOfflineDatabase
import com.example.kbtkedunglo.utilsclass.PermissionUtils
import com.example.kbtkedunglo.utilsclass.showAlert
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import java.io.File
import java.io.IOException

class TimeLinePandingFragment : Fragment(), EventClickListenerMedalOffline, DetailClickMedalOffline {
    private lateinit var recyclerView: RecyclerView
    private var userId:String? = null
    private val sharedPreferences: SharedPreferences by lazy {
        requireActivity().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
    }
    private var medalOfflineAdapter:MedalOfflineAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {}
        userId = sharedPreferences.getString("id", "")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view:View = inflater.inflate(R.layout.fragment_time_line_panding, container, false)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
        val layoutManager = LinearLayoutManager(requireContext())
        val resdb = MedalOfflineDatabase(requireContext()).getAll()
        medalOfflineAdapter = MedalOfflineAdapter(resdb, this@TimeLinePandingFragment, this@TimeLinePandingFragment)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = medalOfflineAdapter
        return view
    }

    override fun onDetailClick(idMf: Int, eventFile: String) {
        val fragmentManager = requireActivity().supportFragmentManager
        val fragment = DetailMedalSoloFragment.newInstance(eventFile)
        fragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onEventClick(position: Int, idMf:Int, eventId: String, eventFile: String, progressBar: ProgressBar, uploadBt:Button) {
        val permissionUtils = PermissionUtils(requireContext())
        if(!permissionUtils.isInternetConnected()) {
            showAlert(requireContext(), "untuk mengupload aktivitas ke server, internet harus aktif.")
        }else{
            uploadBt.visibility = View.GONE
            progressBar.progress = 0
            progressBar.visibility = View.VISIBLE
            Log.i("KBTAPP", "Berhasil upload ke server dengan id:${eventId}")
            val directory = File(requireContext().filesDir, "resource_data")
            val file = File(directory, eventFile)
            val fileBody = file.asRequestBody("text/plain".toMediaTypeOrNull())

            val request = Request.Builder()
                .url("https://kbt.us.to/events/upload/")
                .post(
                    MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", eventFile, fileBody)
                        .addFormDataPart("event_id", eventId)
                        .addFormDataPart("user_id", userId.toString())
                        .build())
                .build()

            progressBar.progress = 10
            OkHttpClient().newCall(request).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        MedalOfflineDatabase(requireContext()).delete(idMf)
                        file.delete()
                        requireActivity().runOnUiThread {
                            progressBar.progress = 100
                            medalOfflineAdapter?.removeItem(position)
                        }
                        Log.i("KBTAPP", "File berhasil di-upload KARENA PANDING ${response}")
                    } else {
                        Log.e("KBTAPP", "Gagal upload file: ${response}")
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    Log.e("KBTAPP", "Gagal melakukan permintaan HTTP: ${e.message}")
                }
            })
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TimeLinePandingFragment().apply {
                arguments = Bundle().apply {}
            }
    }
}
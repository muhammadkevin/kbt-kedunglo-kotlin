package com.example.kbtkedunglo.pages.profil

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.kbtkedunglo.R
import com.example.kbtkedunglo.adapters.TimeLineAdapter
import com.example.kbtkedunglo.utilsclass.ApiClient
import com.example.kbtkedunglo.utilsclass.ApiResponseGet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject


class ActivitiesFragment : Fragment() {
    private var accessToken:String? = null
    private var userId:String? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var context: Context
    private lateinit var activity: FragmentActivity
    private lateinit var swipeRefreshLayout:SwipeRefreshLayout
    private lateinit var timeLineAdapter: TimeLineAdapter
    private val sharedPreferences: SharedPreferences by lazy {
        requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {}
        activity = requireActivity()
        context = requireContext()
        accessToken = sharedPreferences.getString("access_token", "")
        userId = sharedPreferences.getString("id", "")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.profil_fragment_activities, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager
        timeLineAdapter = TimeLineAdapter(activity.supportFragmentManager)
        recyclerView.adapter = timeLineAdapter
        getMyActivity()
        swipeRefreshLayout.setOnRefreshListener {
            getMyActivity()
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun getMyActivity(){
        val apiclient = ApiClient()
        apiclient.getDataWithToken("https://kbt.us.to/activity/report/geni/activities?id_user=${userId}", accessToken.toString(),
            object: ApiResponseGet {
                override fun onSuccess(response: String, code: Int) {
                    CoroutineScope(Dispatchers.Main).launch {
                        Log.i("KBTAPP", "$code")
                        if(code == 200) {
                            try {
                                val resp = JSONObject(response)
                                val respArr = JSONArray(resp.getString("results"))
                                timeLineAdapter.addData(respArr)
                            }catch (e:Exception){
                                Log.e("KBTAPP", e.message ?: "Error parsing response")
                            }
                        }else{
                            Log.e("KBTAPP", "terjadi kesalahan saat mengambil data")
                        }
                    }
                }
                override fun onFailure(error: String) {
                    Log.e("KBTAPP", error)
                }
            })
    }
}
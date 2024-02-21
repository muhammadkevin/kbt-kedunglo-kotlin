package com.example.kbtkedunglo.pages.beranda

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

class BerandaFragment : Fragment() {
    private var accessToken:String? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var context:Context
    private lateinit var activity: FragmentActivity
    private var currentPage = 1
    private var totalItems = 0
    private var itemsPerPage = 10
    private var statusGetNewActivity:Boolean = false
    private val sharedPreferences: SharedPreferences by lazy {
        requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {}
        context = requireContext()
        activity = requireActivity()
        accessToken = sharedPreferences.getString("access_token", "")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.beranda_fragment_beranda, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager
        getActivity(1)
        val layoutManagerRecycler = recyclerView.layoutManager as LinearLayoutManager
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount = layoutManagerRecycler.childCount
                val totalItemCount = layoutManagerRecycler.itemCount
                val firstVisibleItemPosition = layoutManagerRecycler.findFirstVisibleItemPosition()
                Log.v("KBTAPP", "firstVisivle ${firstVisibleItemPosition} + ${visibleItemCount} = ${totalItemCount}");
                if(visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= (totalItemCount - 3) && statusGetNewActivity == false){
                    currentPage = currentPage + 1
                    Log.d("KBTAPP", "get item baru $currentPage")
                    getActivity(currentPage)
                }
            }
        })
        swipeRefreshLayout.setOnRefreshListener {
            getActivity(1)
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun getActivity(page:Int){
        statusGetNewActivity = true
        val apiclient = ApiClient()
        apiclient.getDataWithToken("https://kbt.us.to/activity/report/geni/activities/?page=${page}", accessToken.toString(),
            object: ApiResponseGet {
                override fun onSuccess(response: String, code: Int) {
                    CoroutineScope(Dispatchers.Main).launch {
                        if(code == 200) {
                            try {
                                val resp = JSONObject(response)
                                val respArr = JSONArray(resp.getString("results"))
                                totalItems = respArr.length()
                                recyclerView.adapter = TimeLineAdapter(respArr, activity.supportFragmentManager)
                                statusGetNewActivity = false
                            }catch (e:Exception){
                                statusGetNewActivity = false
                                Log.e("KBTAPP", e.message ?: "Error parsing response")
                            }
                        }else{
                            Log.e("KBTAPP", "terjadi kesalahan saat mengambil data")
                            statusGetNewActivity = false
                        }
                    }
                }
                override fun onFailure(error: String) {
                    Log.e("KBTAPP", error)
                }
            })
    }
}
package com.example.kbtkedunglo.pages

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.kbtkedunglo.MainActivity
import com.example.kbtkedunglo.R
import com.example.kbtkedunglo.adapters.TimeLineAdapter
import com.example.kbtkedunglo.utilsclass.ApiClient
import com.example.kbtkedunglo.utilsclass.ApiResponseGet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray


class StatusTimeLineFragment : Fragment() {
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView:RecyclerView
    private var userId:String? = null
    private lateinit var context:Context
    private lateinit var activity: FragmentActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getString("userId")
        }
        activity = requireActivity()
        context = requireContext()
        Log.d("KBTAPP", "userID: $userId")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view:View = inflater.inflate(R.layout.fragment_time_line_me, container, false)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            (activity as? MainActivity)?.changeSelectedItemId(R.id.menu_profil)
        }
        recyclerView = view.findViewById(R.id.recyclerView)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            getUserHistory()
            swipeRefreshLayout.isRefreshing = false
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getUserHistory()
    }

    private fun getUserHistory(){
        val apiclient = ApiClient()
        CoroutineScope(Dispatchers.IO).launch {
            apiclient.getData("https://kbt.us.to/tracker/report/${userId}/",
                object: ApiResponseGet {
                    override fun onSuccess(response: String, code: Int) {
                        CoroutineScope(Dispatchers.Main).launch {
                            val respArr = JSONArray(response)
                            val layoutManager = LinearLayoutManager(context)
                            recyclerView.layoutManager = layoutManager
                            recyclerView.adapter = TimeLineAdapter(respArr, activity.supportFragmentManager)
                        }
                    }
                    override fun onFailure(error: String) {
                        Log.e("KBTAPP", error)
                    }
                })
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(userId: String?) =
            StatusTimeLineFragment().apply {
                arguments = Bundle().apply {
                    putString("userId", userId)
                }
            }
    }
}
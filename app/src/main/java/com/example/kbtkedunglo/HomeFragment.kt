package com.example.kbtkedunglo

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)

        val apiclient = ApiClient()
        apiclient.getData("https://kbt.us.to/events/listevent/",
            object: ApiResponseGet{
                override fun onSuccess(response: String) {
                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            val respArr = JSONArray(response)
                            val layoutManager = LinearLayoutManager(requireContext())
                            val eventAdapter = EventAdapter(respArr)
                            recyclerView.layoutManager = layoutManager
                            recyclerView.adapter = eventAdapter
                            Log.i("KBTAPP", "response get event: ${respArr.getJSONObject(0).get("logo")}")
                        } catch (e: Exception) {
                            Log.e("KBTAPP", e.message ?: "Error parsing response")
                        }
                    }
                }
                override fun onFailure(error: String) {
                    Log.e("KBTAPP", error)
                }
            })
        return view
    }

    companion object {
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
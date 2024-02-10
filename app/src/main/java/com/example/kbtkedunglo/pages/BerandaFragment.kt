package com.example.kbtkedunglo.pages

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager2.widget.ViewPager2
import com.example.kbtkedunglo.R
import com.example.kbtkedunglo.adapters.CarouselEventAdapter
import com.example.kbtkedunglo.adapters.CarousellEventClickListener
import com.example.kbtkedunglo.services.MedalOfflineDatabase
import com.example.kbtkedunglo.utilsclass.ApiClient
import com.example.kbtkedunglo.utilsclass.ApiResponseGet
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import kotlin.math.abs

class BerandaFragment : Fragment(), CarousellEventClickListener {
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var adapterCarousel: CarouselEventAdapter
    private lateinit var wrapViewPager:RelativeLayout
    private val sharedPreferences: SharedPreferences by lazy {
        requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
    }
    private var edit: SharedPreferences.Editor? = null
    private lateinit var context:Context
    private lateinit var activity: FragmentActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = requireContext()
        activity = requireActivity()
        arguments?.let {}
        edit = sharedPreferences.edit()
        val dbmedal = MedalOfflineDatabase(requireContext()).getAll()
        Log.d("KBTAPP", "DATABASE MEDAL: ${dbmedal.toString()}")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_beranda, container, false)
        wrapViewPager = view.findViewById(R.id.wrapViewPager)
        viewPager = view.findViewById(R.id.viewPager2)
        viewPager.increaseDragSensitivity(4)

        getEventData()
        tabLayout = view.findViewById(R.id.tabLayout)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            getEventData()
            swipeRefreshLayout.isRefreshing = false
        }
        adapterCarousel = CarouselEventAdapter(context, null, this@BerandaFragment)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateTabLayout(position)
            }
        })
        viewPager.setPageTransformer { page, position ->
            val offset = 16 * context.resources.displayMetrics.density
            page.translationX = -offset * position
            page.alpha = 1 - abs(position)
            page.scaleY = 1f
        }
        (viewPager.getChildAt(0) as? RecyclerView)?.isNestedScrollingEnabled = false
        return view
    }

    private fun ViewPager2.increaseDragSensitivity(angka:Int) {
        val recyclerViewField = ViewPager2::class.java.getDeclaredField("mRecyclerView")
        recyclerViewField.isAccessible = true
        val recyclerView = recyclerViewField.get(this) as RecyclerView

        val touchSlopField = RecyclerView::class.java.getDeclaredField("mTouchSlop")
        touchSlopField.isAccessible = true
        val touchSlop = touchSlopField.get(recyclerView) as Int
        touchSlopField.set(recyclerView, touchSlop/angka)
    }

    private fun updateTabLayout(selectedPosition: Int) {
        val tabCount = tabLayout.tabCount
        for (i in 0 until tabCount) {
            val tab = tabLayout.getTabAt(i)
            val customView = tab?.customView as? LinearLayout
            val imageView = customView?.findViewById<ImageView>(R.id.tabIcon)
            if (i == selectedPosition) {
                imageView?.setImageResource(R.drawable.ic_dotted)
            } else {
                imageView?.setImageResource(R.drawable.ic_dot)
            }
        }
    }

    private fun getEventData(){
        val apiclient = ApiClient()
        apiclient.getData("https://kbt.us.to/events/listevent/",
            object: ApiResponseGet {
                override fun onSuccess(response: String, code: Int) {
                    CoroutineScope(Dispatchers.Main).launch {
                        if(code == 200) {
                            wrapViewPager.visibility = View.VISIBLE
                            try {
                                val respArr = JSONArray(response)
                                if(respArr.length() > 0){
                                    adapterCarousel =
                                        CarouselEventAdapter(context, respArr, this@BerandaFragment)
                                    viewPager.offscreenPageLimit = 2
                                    viewPager.adapter = adapterCarousel
                                    TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                                        val tabIcon = if (position == viewPager.currentItem) {
                                            R.drawable.ic_dotted
                                        } else R.drawable.ic_dot
                                        tab.customView = createTabView(tabIcon)
                                    }.attach()
                                }else wrapViewPager.visibility = View.GONE
                            } catch (e: Exception) {
                                viewPager.visibility = View.GONE
                                Log.e("KBTAPP", e.message ?: "Error parsing response")
                            }
                        }else wrapViewPager.visibility = View.GONE
                    }
                }
                override fun onFailure(error: String) {
                    activity.runOnUiThread { wrapViewPager.visibility = View.GONE }
                    Log.e("KBTAPP", error)
                }
            })
    }

    private fun createTabView(tabIcon:Int): View {
        val tabView = LayoutInflater.from(context).inflate(R.layout.custom_tab_carousel, null) as LinearLayout
        val icon = tabView.findViewById<ImageView>(R.id.tabIcon)
        icon.setImageResource(tabIcon)
        tabView.isSelected = tabIcon == R.drawable.ic_dotted
        return tabView
    }

    override fun onCarousellEventClick(
        position: Int,
        idEvent: String,
        namaEvent: String,
        gpxFile: String,
        description:String,
        waktuMulai:String,
        waktuSelesai:String
    ) {
        edit?.putString("event_id", idEvent)
        edit?.putString("event_nama", namaEvent)
        edit?.putString("event_gpx", gpxFile)
        edit?.putString("main_event", true.toString())
        edit?.putString("alert_to_event_route", "false")
        edit?.apply()
        val fragment = DetailEventFragment.newInstance(description, waktuMulai, waktuSelesai)
        activity.supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
        Log.i("KBTAPP", "Mengikuti Event:${namaEvent}")
    }

    companion object {
        fun newInstance(param1: String, param2: String) =
            BerandaFragment().apply {
                arguments = Bundle().apply {}
            }
    }
}
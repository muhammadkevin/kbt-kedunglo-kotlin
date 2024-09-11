package com.example.kbtkedunglo.pages.profil

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.kbtkedunglo.R
import com.example.kbtkedunglo.utilsclass.ViewPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel

class ProfilFragment : Fragment() {
    private var fotoUrl: String? = null
    private var username: String? = null
    private var nama: String? = null
    private var julukan: String? = null
    private var group_kbt: String? = null
    private var tgl_lahir: String? = null
    private var jns_kelamin: String? = null
    private var no_wa: String? = null
    private var emailSaved: String? = null
    private var userId:String? = null
    private lateinit var profileImageView: CircleImageView
    private lateinit var tabLayout:TabLayout
    private lateinit var viewPager:ViewPager2
    private lateinit var namaView:TextView
    private val coroutineImageProfile = CoroutineScope(Dispatchers.IO)
    private val sharedPreferences: SharedPreferences by lazy {
        requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fotoUrl = sharedPreferences.getString("foto", "") ?: null
        username = sharedPreferences.getString("username", "")
        nama = sharedPreferences.getString("nama", "")
        julukan = sharedPreferences.getString("julukan", "")
        group_kbt = sharedPreferences.getString("group_kbt", "")
        tgl_lahir = sharedPreferences.getString("tgl_lahir", "")
        jns_kelamin = sharedPreferences.getString("jns_kelamin", "")
        no_wa = sharedPreferences.getString("no_wa", "")
        emailSaved = sharedPreferences.getString("email", "")
        userId = sharedPreferences.getString("id", "")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.profil_fragment_profil, container, false)
//        profileImageView = view.findViewById(R.id.profileImage)
        namaView = view.findViewById(R.id.namaText)
        tabLayout = view.findViewById(R.id.tab_layout)
        viewPager = view.findViewById(R.id.view_pager2)

        namaView.text = nama
//        profileImageView.setImageResource(R.drawable.profilkosongl)
//        Log.i("KBTAPP", "foto url:${fotoUrl}")
//        if(fotoUrl != null && fotoUrl != "null"){
//            val profilFotoUrl = "https://kbt.us.to${fotoUrl}"
//            coroutineImageProfile.launch {
//                try {
//                    val bitmap = withContext(Dispatchers.IO) {
//                        Picasso.get().load(profilFotoUrl).resize(313,313).get()
//                    }
//                    withContext(Dispatchers.Main) {
//                        profileImageView.setImageBitmap(bitmap)
//                    }
//                }catch (e:IOException){
//                    this.cancel()
//                    profileImageView.setImageResource(R.drawable.profilkosongl)
//                    Log.e("KBTAPP", "terjadi error get: ${e.message}")
//                }
//            }
//        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ViewPagerAdapter(requireActivity())
        adapter.addFragment(PersonalFragment(), "PROGRESS")
        adapter.addFragment(ActivitiesFragment(), "ACTIVITIES")
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = adapter.getPageTitle(position)
        }.attach()
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineImageProfile.cancel()
    }
}
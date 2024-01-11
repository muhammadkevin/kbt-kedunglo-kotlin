package com.example.kbtkedunglo.fragments

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.example.kbtkedunglo.R
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

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
        val view = inflater.inflate(R.layout.fragment_profil, container, false)
        val pendingtimelineBt: CardView = view.findViewById(R.id.pendingtimeline)
        val mytimelineBt: CardView = view.findViewById(R.id.mytimeline)
        profileImageView = view.findViewById(R.id.profileImage)
        profileImageView.setImageResource(R.drawable.profilkosongl)

        view.findViewById<TextView>(R.id.usernameText).text = username
        view.findViewById<TextView>(R.id.namaText).text = nama
        view.findViewById<TextView>(R.id.julukanText).text = julukan
        view.findViewById<TextView>(R.id.groupKbtText).text = group_kbt
        view.findViewById<TextView>(R.id.tglLahirText).text = tgl_lahir
        view.findViewById<TextView>(R.id.jenisKelaminText).text = if(jns_kelamin == "P") "Perempuan" else "Laki-Laki"
        if(no_wa == "null" || no_wa == null || no_wa == ""){
            view.findViewById<TextView>(R.id.waText).text = "Belum Ditambahkan"
            view.findViewById<TextView>(R.id.waText).setTextColor(Color.RED)
        }else view.findViewById<TextView>(R.id.waText).text = no_wa
        if(emailSaved == "null" || emailSaved == null || emailSaved == ""){
            view.findViewById<TextView>(R.id.emailText).text = "Belum Ditambahkan"
            view.findViewById<TextView>(R.id.emailText).setTextColor(Color.RED)
        }else view.findViewById<TextView>(R.id.emailText).text = emailSaved
        Log.i("KBTAPP", "foto url:${fotoUrl}")
        if(fotoUrl != null && fotoUrl != "null"){
            try {
                val profilFotoUrl = "https://kbt.us.to${fotoUrl}"
                coroutineImageProfile.launch {
                    val bitmap = withContext(Dispatchers.IO) {
                        Picasso.get().load(profilFotoUrl).resize(313,313).get()
                    }
                    withContext(Dispatchers.Main) {
                        profileImageView.setImageBitmap(bitmap)
                    }
                }
            }catch (e:IOException){
                profileImageView.setImageResource(R.drawable.profilkosongl)
                Log.e("KBTAPP", "terjadi error get: ${e.message}")
            }
        }

        mytimelineBt.setOnClickListener{
            val fragmentManager = requireActivity().supportFragmentManager
            val fragment = TimeLineMeFragment.newInstance(userId)
            fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        pendingtimelineBt.setOnClickListener{
            val fragmentManager = requireActivity().supportFragmentManager
            fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, TimeLinePandingFragment())
                .addToBackStack(null)
                .commit()
        }
        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineImageProfile.cancel()
    }

    companion object {
        @JvmStatic
        fun newInstance() = ProfilFragment().apply {}
    }
}
package com.example.kbtkedunglo.pages.kbt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.kbtkedunglo.R

class KBTFragment : Fragment() {
    private lateinit var view:View
    private lateinit var leafderBoardBt:Button
    private lateinit var memberBt:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {}
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = inflater.inflate(R.layout.kbt_fragment_kbt, container, false)
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            KBTFragment().apply {
                arguments = Bundle().apply {}
            }
    }
}
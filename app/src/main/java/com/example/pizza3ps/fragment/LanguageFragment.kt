package com.example.pizza3ps.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import com.example.pizza3ps.R
import com.example.pizza3ps.tool.LanguageHelper

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

class LanguageFragment : Fragment() {
    private lateinit var enBtn : Button
    private lateinit var vieBtn : Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view =  inflater.inflate(R.layout.fragment_language, container, false)

        enBtn = view.findViewById(R.id.enBtn)
        vieBtn = view.findViewById(R.id.vieBtn)

        val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)

        enBtn.setOnClickListener {
            prefs.edit().putString("lang", "en").apply()
            LanguageHelper.setLocale(requireContext(), "en")

            val intent = requireActivity().intent
            requireActivity().finish()
            startActivity(intent)
        }

        vieBtn.setOnClickListener {
            prefs.edit().putString("lang", "vi").apply()
            LanguageHelper.setLocale(requireContext(), "vi")

            val intent = requireActivity().intent
            requireActivity().finish()
            startActivity(intent)
        }

        return view
    }



}
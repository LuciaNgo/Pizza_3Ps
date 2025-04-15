package com.example.pizza3ps.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.pizza3ps.R
import com.example.pizza3ps.tool.LanguageHelper

class LanguageFragment : Fragment() {
    private lateinit var enLayout : ConstraintLayout
    private lateinit var viLayout : ConstraintLayout
    private lateinit var enCheck : ImageView
    private lateinit var viCheck : ImageView
    private lateinit var backButton : ImageView
    private lateinit var updateButton : Button
    private lateinit var selectedLang : String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.fragment_language, container, false)

        enLayout = view.findViewById(R.id.englishContainer)
        viLayout = view.findViewById(R.id.vietnameseContainer)
        enCheck = view.findViewById(R.id.englishCheckIcon)
        viCheck = view.findViewById(R.id.vietnameseCheckIcon)
        backButton = view.findViewById(R.id.backButton)
        updateButton = view.findViewById(R.id.updateButton)

        val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        val lang = prefs.getString("lang", "en") ?: "en"

        if (lang == "en") {
            enCheck.visibility = View.VISIBLE
            viCheck.visibility = View.INVISIBLE
            selectedLang = "en"
        } else {
            enCheck.visibility = View.INVISIBLE
            viCheck.visibility = View.VISIBLE
            selectedLang = "vi"
        }

        enLayout.setOnClickListener {
            if (selectedLang == "vi") {
                enCheck.visibility = View.VISIBLE
                viCheck.visibility = View.INVISIBLE

                selectedLang= "en"
            }
        }

        viLayout.setOnClickListener {
            if (selectedLang == "en") {
                enCheck.visibility = View.INVISIBLE
                viCheck.visibility = View.VISIBLE

                selectedLang= "vi"
            }
        }

        updateButton.setOnClickListener {
            prefs.edit().putString("lang", selectedLang).apply()
            LanguageHelper.setLocale(requireContext(), selectedLang)

            val intent = requireActivity().intent
            requireActivity().finish()
            startActivity(intent)
        }

        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        return view
    }
}
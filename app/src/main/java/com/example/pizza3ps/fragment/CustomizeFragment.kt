package com.example.pizza3ps.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.example.pizza3ps.R
import android.widget.Button
import com.example.pizza3ps.activity.CustomizePizzaActivity

class CustomizeFragment : Fragment() {
    private lateinit var customizeButton: Button
    private lateinit var backButton: ImageView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_customize, container, false)

        customizeButton = view.findViewById(R.id.customize_button)
        backButton = view.findViewById(R.id.back_button)

        customizeButton.setOnClickListener {
            val intent = Intent(requireContext(), CustomizePizzaActivity::class.java)
            startActivity(intent)
        }

        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        return view
    }


}
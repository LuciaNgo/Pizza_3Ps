package com.example.pizza3ps.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.pizza3ps.R
import com.example.pizza3ps.model.IngredientData
import android.widget.Button
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andremion.counterfab.CounterFab
import com.example.pizza3ps.activity.CustomizePizzaActivity
import com.example.pizza3ps.adapter.IngredientAdapter
import com.example.pizza3ps.database.DatabaseHelper
import com.example.pizza3ps.model.CartData
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DecimalFormat

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
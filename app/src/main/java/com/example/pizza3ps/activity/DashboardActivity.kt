package com.example.pizza3ps.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pizza3ps.R
import com.example.pizza3ps.adapter.FoodAdapter
import com.example.pizza3ps.model.FoodData
import com.google.firebase.firestore.FirebaseFirestore

class DashboardActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var foodAdapter: FoodAdapter
    private val foodList = mutableListOf<FoodData>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        recyclerView = findViewById(R.id.pizza_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        fetchData()
    }

    private fun fetchData() {
        db.collection("Food")
            .get()
            .addOnSuccessListener { documents ->
                foodList.clear()
                for (document in documents) {
                    val name = document.getString("name") ?: ""
                    val priceString = document.getString("price") ?: ""

                    val price = priceString.toIntOrNull() ?: 0
                    val formattedPrice = java.text.DecimalFormat("#,###").format(price)

                    foodList.add(FoodData(name, formattedPrice))
                }
                foodAdapter = FoodAdapter(foodList)
                recyclerView.adapter = foodAdapter
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Lỗi khi lấy dữ liệu", exception)
            }
    }
}

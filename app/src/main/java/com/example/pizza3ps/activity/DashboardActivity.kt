package com.example.pizza3ps.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pizza3ps.R
import com.example.pizza3ps.adapter.EventAdapter
import com.example.pizza3ps.adapter.FoodAdapter
import com.example.pizza3ps.model.EventData
import com.example.pizza3ps.model.FoodData
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DecimalFormat

class DashboardActivity : AppCompatActivity() {
    private lateinit var btnAccount: LinearLayout

    private lateinit var eventRecyclerView: RecyclerView
    private lateinit var pizzaRecyclerView: RecyclerView
    private lateinit var chickenRecyclerView: RecyclerView
    private lateinit var pastaRecyclerView: RecyclerView
    private lateinit var appetizerRecyclerView: RecyclerView
    private lateinit var drinksRecyclerView: RecyclerView

    private val eventList = mutableListOf<EventData>()
    private val pizzaList = mutableListOf<FoodData>()
    private val chickenList = mutableListOf<FoodData>()
    private val pastaList = mutableListOf<FoodData>()
    private val appetizerList = mutableListOf<FoodData>()
    private val drinksList = mutableListOf<FoodData>()

    private lateinit var eventAdapter: EventAdapter
    private lateinit var pizzaFoodAdapter: FoodAdapter
    private lateinit var chickenFoodAdapter: FoodAdapter
    private lateinit var pastaFoodAdapter: FoodAdapter
    private lateinit var appetizerFoodAdapter: FoodAdapter
    private lateinit var drinksFoodAdapter: FoodAdapter

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        btnAccount = findViewById(R.id.btnAccount)
        btnAccount.setOnClickListener {
            val intent = Intent(this, AccountActivity::class.java)
            startActivity(intent)
        }


        eventRecyclerView = findViewById(R.id.event_recycler_view)
        pizzaRecyclerView = findViewById(R.id.pizza_recycler_view)
        chickenRecyclerView = findViewById(R.id.chicken_recycler_view)
        pastaRecyclerView = findViewById(R.id.pasta_recycler_view)
        appetizerRecyclerView = findViewById(R.id.appetizer_recycler_view)
        drinksRecyclerView = findViewById(R.id.drinks_recycler_view)

        eventRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        pizzaRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        chickenRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        pastaRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        appetizerRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        drinksRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        eventAdapter = EventAdapter(eventList)
        pizzaFoodAdapter = FoodAdapter(pizzaList)
        chickenFoodAdapter = FoodAdapter(chickenList)
        pastaFoodAdapter = FoodAdapter(pastaList)
        appetizerFoodAdapter = FoodAdapter(appetizerList)
        drinksFoodAdapter = FoodAdapter(drinksList)

        eventRecyclerView.adapter = eventAdapter
        pizzaRecyclerView.adapter = pizzaFoodAdapter
        chickenRecyclerView.adapter = chickenFoodAdapter
        pastaRecyclerView.adapter = pastaFoodAdapter
        appetizerRecyclerView.adapter = appetizerFoodAdapter
        drinksRecyclerView.adapter = drinksFoodAdapter

        fetchEventData()
        fetchFoodData()
    }

    private fun fetchEventData() {
        db.collection("Event")
            .get()
            .addOnSuccessListener { documents ->
                eventList.clear()

                for (document in documents) {
                    val name = document.getString("name") ?: ""
                    val imgPath = document.getString("imgPath") ?: ""

                    // Tạo đối tượng EventData
                    val eventItem = EventData(name, imgPath)
                    eventList.add(eventItem)

                    Log.d("Firestore", "Tên: $name, Hình ảnh: $imgPath")
                }

                // Cập nhật giao diện
                eventAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Lỗi khi lấy dữ liệu", exception)
            }
    }

    private fun fetchFoodData() {
        db.collection("Food")
            .get()
            .addOnSuccessListener { documents ->
                pizzaList.clear()
                chickenList.clear()
                pastaList.clear()
                appetizerList.clear()
                drinksList.clear()

                for (document in documents) {
                    val name = document.getString("name") ?: ""
                    val priceString = document.getString("price") ?: "0"
                    val imgPath = document.getString("imgPath") ?: ""
                    val ingredientString = document.getString("ingredient") ?: ""
                    val category = document.getString("category") ?: ""

                    val price = priceString.toIntOrNull() ?: 0
                    val formattedPrice = DecimalFormat("#,###").format(price)

                    // Tách danh sách nguyên liệu thành mảng
                    val ingredientList = ingredientString.split(", ").map { it.trim() }

                    // Tạo đối tượng FoodData
                    val foodItem = FoodData(name, formattedPrice, imgPath, ingredientList)

                    // Thêm vào danh sách theo category
                    when (category.lowercase()) {
                        "pizza" -> pizzaList.add(foodItem)
                        "pasta" -> pastaList.add(foodItem)
                        "chicken" -> chickenList.add(foodItem)
                        "appetizer" -> appetizerList.add(foodItem)
                        "drinks" -> drinksList.add(foodItem)
                        else -> Log.w("Firestore", "Danh mục không xác định: $category")
                    }

                    Log.d("Firestore", "Tên: $name, Giá: $formattedPrice, Hình ảnh: $imgPath, Nguyên liệu: $ingredientList, Danh mục: $category")
                }

                // Cập nhật giao diện
                pizzaFoodAdapter.notifyDataSetChanged()
                chickenFoodAdapter.notifyDataSetChanged()
                pastaFoodAdapter.notifyDataSetChanged()
                appetizerFoodAdapter.notifyDataSetChanged()
                drinksFoodAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Lỗi khi lấy dữ liệu", exception)
            }
    }

}

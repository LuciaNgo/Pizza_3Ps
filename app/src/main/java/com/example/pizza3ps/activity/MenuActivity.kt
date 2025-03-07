package com.example.pizza3ps.activity

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SearchView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pizza3ps.R
import com.example.pizza3ps.adapter.EventAdapter
import com.example.pizza3ps.adapter.FoodAdapter
import com.example.pizza3ps.model.EventData
import com.example.pizza3ps.model.FoodData
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DecimalFormat

class MenuActivity : AppCompatActivity() {
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

    private lateinit var searchBar : SearchView

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_menu)

        val customizeLayout: LinearLayout = findViewById(R.id.customize_layout)
        val homeLayout : LinearLayout = findViewById(R.id.home_layout)
        val menuLayout : LinearLayout = findViewById(R.id.menu_layout)

        val scrollView = findViewById<ScrollView>(R.id.menu_scroll_view)

        // Lấy các danh mục trên thanh category
        val pizzaCategory = findViewById<LinearLayout>(R.id.pizza_category)
        val chickenCategory = findViewById<LinearLayout>(R.id.chicken_category)
        val pastaCategory = findViewById<LinearLayout>(R.id.pasta_category)
        val appetizerCategory = findViewById<LinearLayout>(R.id.appetizer_category)
        val drinksCategory = findViewById<LinearLayout>(R.id.drinks_category)

        // Lấy các tiêu đề section tương ứng
        val pizzaSection = findViewById<View>(R.id.pizza_section_title)
        val chickenSection = findViewById<View>(R.id.chicken_section_title)
        val pastaSection = findViewById<View>(R.id.pasta_section_title)
        val appetizerSection = findViewById<View>(R.id.appetizer_section_title)
        val drinksSection = findViewById<View>(R.id.drinks_section_title)

        // Thiết lập sự kiện click để cuộn đến phần tương ứng
        pizzaCategory.setOnClickListener { scrollToSection(scrollView, pizzaSection) }
        chickenCategory.setOnClickListener { scrollToSection(scrollView, chickenSection) }
        pastaCategory.setOnClickListener { scrollToSection(scrollView, pastaSection) }
        appetizerCategory.setOnClickListener { scrollToSection(scrollView, appetizerSection) }
        drinksCategory.setOnClickListener { scrollToSection(scrollView, drinksSection) }

        pizzaRecyclerView = findViewById(R.id.pizza_view)
        chickenRecyclerView = findViewById(R.id.chicken_view)
        pastaRecyclerView = findViewById(R.id.pasta_view)
        appetizerRecyclerView = findViewById(R.id.appetizer_view)
        drinksRecyclerView = findViewById(R.id.drinks_view)

        searchBar = findViewById(R.id.menu_search_bar)

        pizzaRecyclerView.layoutManager = GridLayoutManager(this, 2)
        chickenRecyclerView.layoutManager = GridLayoutManager(this, 2)
        pastaRecyclerView.layoutManager = GridLayoutManager(this, 2)
        appetizerRecyclerView.layoutManager = GridLayoutManager(this, 2)
        drinksRecyclerView.layoutManager = GridLayoutManager(this, 2)

        eventAdapter = EventAdapter(eventList)
        pizzaFoodAdapter = FoodAdapter(pizzaList)
        chickenFoodAdapter = FoodAdapter(chickenList)
        pastaFoodAdapter = FoodAdapter(pastaList)
        appetizerFoodAdapter = FoodAdapter(appetizerList)
        drinksFoodAdapter = FoodAdapter(drinksList)

        pizzaRecyclerView.adapter = pizzaFoodAdapter
        chickenRecyclerView.adapter = chickenFoodAdapter
        pastaRecyclerView.adapter = pastaFoodAdapter
        appetizerRecyclerView.adapter = appetizerFoodAdapter
        drinksRecyclerView.adapter = drinksFoodAdapter

        fetchFoodData()

        customizeLayout.setOnClickListener {
            val intent = Intent(this, CustomizePizzaActivity::class.java)
            startActivity(intent)
        }

        menuLayout.setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
        }

        homeLayout.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
        }

    //        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
    //            override fun onQueryTextSubmit(query: String?): Boolean {
    //                query?.let { performFilteredSearch(it) }
    //                return true
    //            }
    //
    //            override fun onQueryTextChange(newText: String?): Boolean {
    //                performFilteredSearch(newText ?: "")
    //                return true
    //            }
    //        })

    }


    private fun getAllFoodItems(): List<FoodData> {
        return pizzaList + chickenList + pastaList + appetizerList + drinksList
    }

//    private fun performSearch(query: String) {
//        if (query.isEmpty()) {
//            // ✅ If search bar is cleared, restore original lists
//            pizzaFoodAdapter.updateData(pizzaList.toMutableList())
//            chickenFoodAdapter.updateData(chickenList.toMutableList())
//            pastaFoodAdapter.updateData(pastaList.toMutableList())
//            appetizerFoodAdapter.updateData(appetizerList.toMutableList())
//            drinksFoodAdapter.updateData(drinksList.toMutableList())
//            return
//        }
//
//        val filteredList = getAllFoodItems().filter {
//            it.name.contains(query, ignoreCase = true)
//        }
//
//        if (filteredList.isEmpty()) {
//            Log.d("SearchView", "No items match the query: $query")
//        } else {
//            Log.d("SearchView", "Filtered items: ${filteredList.map { it.name }}")
//        }
//
//        pizzaFoodAdapter.updateData(filteredList)
//        chickenFoodAdapter.updateData(filteredList)
//        pastaFoodAdapter.updateData(filteredList)
//        appetizerFoodAdapter.updateData(filteredList)
//        drinksFoodAdapter.updateData(filteredList)
//
//    }

//    private fun performFilteredSearch(query: String) {
//        if (query.isEmpty()) {
//            // ✅ Reset lists when search is cleared
//            pizzaFoodAdapter.updateData(pizzaList)
//            chickenFoodAdapter.updateData(chickenList)
//            pastaFoodAdapter.updateData(pastaList)
//            appetizerFoodAdapter.updateData(appetizerList)
//            drinksFoodAdapter.updateData(drinksList)
//            return
//        }
//    }


    private fun scrollToSection(scrollView: ScrollView, targetView: View) {
        scrollView.post {
            val location = IntArray(2)
            targetView.getLocationOnScreen(location)

            // Lấy vị trí Y của targetView so với ScrollView
            val scrollY = location[1] - scrollView.top - 50

            scrollView.smoothScrollTo(0, scrollY)
        }
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

                    val foodItem = FoodData(name, formattedPrice, imgPath, ingredientList)

                    when (category.lowercase()) {
                        "pizza" -> pizzaList.add(foodItem)
                        "pasta" -> pastaList.add(foodItem)
                        "chicken" -> chickenList.add(foodItem)
                        "appetizer" -> appetizerList.add(foodItem)
                        "drinks" -> drinksList.add(foodItem)
                        else -> Log.w("Firestore", "Danh mục không xác định: $category")
                    }

                    Log.d(
                        "Firestore",
                        "Tên: $name, Giá: $formattedPrice, Hình ảnh: $imgPath, Nguyên liệu: $ingredientList, Danh mục: $category"
                    )
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




//    fun fetchFirestoreSuggestions() {
//        val db = FirebaseFirestore.getInstance()
//        db.collection("FoodItems") // Assume you have a "FoodItems" collection
//            .get()
//            .addOnSuccessListener { documents ->
//                val suggestions = documents.map { it.getString("name") ?: "" }
//                val adapter = ArrayAdapter(this, R.layout.suggestion_item, suggestions)
//                searchAutoComplete.setAdapter(adapter)
//            }
//            .addOnFailureListener {
//                Log.e("Firestore", "Error fetching suggestions", it)
//            }
//    }

}



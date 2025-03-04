package com.example.pizza3ps.activity

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pizza3ps.R
import com.example.pizza3ps.adapter.IngredientAdapter
import com.example.pizza3ps.model.IngredientData
import com.google.firebase.firestore.FirebaseFirestore

class FoodInfoActivity : AppCompatActivity() {
    private lateinit var recyclerViewMeat: RecyclerView
    private lateinit var recyclerViewSeafood: RecyclerView
    private lateinit var recyclerViewVegetable: RecyclerView
    private lateinit var recyclerViewAddition: RecyclerView
    private lateinit var recyclerViewSauce: RecyclerView

    private lateinit var meatAdapter: IngredientAdapter
    private lateinit var seafoodAdapter: IngredientAdapter
    private lateinit var vegetableAdapter: IngredientAdapter
    private lateinit var additionAdapter: IngredientAdapter
    private lateinit var sauceAdapter: IngredientAdapter

    private val meatList = mutableListOf<IngredientData>()
    private val seafoodList = mutableListOf<IngredientData>()
    private val vegetableList = mutableListOf<IngredientData>()
    private val additionList = mutableListOf<IngredientData>()
    private val sauceList = mutableListOf<IngredientData>()

    private val db = FirebaseFirestore.getInstance()
    private var totalPrice = 0
    private lateinit var addToCartButton: Button
    private lateinit var ingredientList: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_food_info)

        val name = intent.getStringExtra("food_name") ?: ""
        val price = intent.getStringExtra("food_price") ?: ""
        val imgPath = intent.getStringExtra("food_image") ?: ""
        ingredientList = intent.getStringArrayListExtra("ingredientList") ?: arrayListOf()
        Log.d("FoodInfoActivity", "Danh sách nguyên liệu nhận được: $ingredientList")

        val pizzaNameTextView: TextView = findViewById(R.id.pizza_name)
        addToCartButton = findViewById(R.id.add_to_cart_button)
        val pizzaImageView: ImageView = findViewById(R.id.pizza_image)

        pizzaNameTextView.text = name
        totalPrice = price.toIntOrNull() ?: 0
        updatePrice()
        Glide.with(this).load(imgPath).into(pizzaImageView)

        recyclerViewMeat = findViewById(R.id.meat_recycler_view)
        recyclerViewSeafood = findViewById(R.id.seafood_recycler_view)
        recyclerViewVegetable = findViewById(R.id.vegetable_recycler_view)
        recyclerViewAddition = findViewById(R.id.addition_recycler_view)
        recyclerViewSauce = findViewById(R.id.sauce_recycler_view)

        recyclerViewMeat.layoutManager = GridLayoutManager(this, 5)
        recyclerViewSeafood.layoutManager = GridLayoutManager(this, 5)
        recyclerViewVegetable.layoutManager = GridLayoutManager(this, 5)
        recyclerViewAddition.layoutManager = GridLayoutManager(this, 5)
        recyclerViewSauce.layoutManager = GridLayoutManager(this, 5)

        fetchIngredientData()
    }

    private fun fetchIngredientData() {
        db.collection("Ingredient")
            .get()
            .addOnSuccessListener { documents ->
                // Xóa danh sách cũ trước khi thêm dữ liệu mới
                meatList.clear()
                seafoodList.clear()
                vegetableList.clear()
                additionList.clear()
                sauceList.clear()

                for (document in documents) {
                    val name = document.getString("name") ?: ""
                    val price = document.getString("price") ?: "0"
                    val iconImgPath = document.getString("iconImgPath") ?: ""
                    val layerImgPath = document.getString("layerImgPath") ?: ""
                    val category = document.getString("category") ?: ""

                    val ingredient = IngredientData(name, price, iconImgPath, layerImgPath)

                    when (category.lowercase()) {
                        "meat" -> meatList.add(ingredient)
                        "seafood" -> seafoodList.add(ingredient)
                        "vegetable" -> vegetableList.add(ingredient)
                        "addition" -> additionList.add(ingredient)
                        "sauce" -> sauceList.add(ingredient)
                        else -> Log.w("Firestore", "Danh mục không xác định: $category")
                    }
                }

                // Cập nhật Adapter
                meatAdapter = IngredientAdapter(meatList)
                seafoodAdapter = IngredientAdapter(seafoodList)
                vegetableAdapter = IngredientAdapter(vegetableList)
                additionAdapter = IngredientAdapter(additionList)
                sauceAdapter = IngredientAdapter(sauceList)

                recyclerViewMeat.adapter = meatAdapter
                recyclerViewSeafood.adapter = seafoodAdapter
                recyclerViewVegetable.adapter = vegetableAdapter
                recyclerViewAddition.adapter = additionAdapter
                recyclerViewSauce.adapter = sauceAdapter

                // Chọn sẵn nguyên liệu có trong ingredientList
                selectPreChosenIngredients()
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Lỗi khi lấy dữ liệu", exception)
            }
    }

    private fun selectPreChosenIngredients() {
        val selected = mutableListOf<String>()
        selected.addAll(ingredientList)

        meatAdapter.setSelectedIngredients(selected)
        seafoodAdapter.setSelectedIngredients(selected)
        vegetableAdapter.setSelectedIngredients(selected)
        additionAdapter.setSelectedIngredients(selected)
        sauceAdapter.setSelectedIngredients(selected)
    }

    private fun updatePrice() {
        addToCartButton.text = "Add to cart - $totalPrice VND"
    }
}

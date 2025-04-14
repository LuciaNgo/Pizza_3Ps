package com.example.pizza3ps.activity

import android.content.Intent
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.andremion.counterfab.CounterFab
import com.example.pizza3ps.R
import com.example.pizza3ps.tool.LanguageHelper
import com.example.pizza3ps.database.DatabaseHelper
import com.example.pizza3ps.model.FoodData
import com.example.pizza3ps.model.IngredientData
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {
    lateinit var cartFab: CounterFab
    private lateinit var bottomNavigationView: BottomNavigationView

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        fetchFoodData()
        fetchIngredientData()

        val dbHelper = DatabaseHelper(this)
        cartFab = findViewById(R.id.cart_fab)
        cartFab.count = dbHelper.getCartItemCount()

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        bottomNavigationView = findViewById(R.id.nav_view)
        bottomNavigationView.setupWithNavController(navController)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_dashboard -> {
                    if (navController.currentDestination?.id != R.id.navigation_dashboard) {
                        navController.popBackStack(R.id.navigation_dashboard, false)
                    }
                    true
                }

                else -> {
                    NavigationUI.onNavDestinationSelected(item, navController)
                }
            }
        }

        cartFab.setOnClickListener {
            val intent = Intent(this, ShowCartActivity::class.java)
            startActivity(intent)
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        val prefs = newBase?.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val langCode = prefs?.getString("lang", "en") ?: "en"

        val context = LanguageHelper.setLocale(newBase!!, langCode)
        super.attachBaseContext(context)
    }

    private fun fetchFoodData() {
        db.collection("Food")
            .get()
            .addOnSuccessListener { documents ->

                val dbHelper = DatabaseHelper(this)
                dbHelper.deleteAllFood()

                for (document in documents) {
                    val nameMap = document.get("name") as? Map<*, *>
                    val name_en = nameMap?.get("en") as? String ?: ""
                    val name_vi = nameMap?.get("vi") as? String ?: ""
                    val price = document.getString("price")?.toIntOrNull() ?: 0
                    val formattedPrice = DecimalFormat("#,###").format(price)
                    val imgPath = document.getString("imgPath") ?: ""
                    val ingredientString = document.getString("ingredient") ?: ""
                    val category = document.getString("category") ?: ""
                    val ingredientList = ingredientString.split(", ").map { it.trim() }
                    val foodItem = FoodData(name_en, name_vi, formattedPrice, category, imgPath, ingredientList)

                    dbHelper.addFood(foodItem)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Lỗi khi lấy dữ liệu", exception)
            }
    }

    private fun fetchIngredientData() {
        db.collection("Ingredient")
            .get()
            .addOnSuccessListener { documents ->

                val dbHelper = DatabaseHelper(this)
                dbHelper.deleteAllIngredients()

                for (document in documents) {
                    val name = document.getString("name") ?: ""
                    val price = document.getString("price") ?: "0"
                    val iconImgPath = document.getString("iconImgPath") ?: ""
                    val layerImgPath = document.getString("layerImgPath") ?: ""
                    val category = document.getString("category") ?: ""

                    val ingredientItem = IngredientData(name, price, category, iconImgPath, layerImgPath)

                    dbHelper.addIngredient(ingredientItem)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Lỗi khi lấy dữ liệu", exception)
            }
    }
}
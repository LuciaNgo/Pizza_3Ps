package com.example.pizza3ps.activity

import android.content.Intent
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import com.example.pizza3ps.model.AddressData
import com.example.pizza3ps.model.CartData
import com.example.pizza3ps.model.FoodData
import com.example.pizza3ps.model.IngredientData
import com.example.pizza3ps.model.UserData
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {
    lateinit var cartFab: CounterFab
    private lateinit var bottomNavigationView: BottomNavigationView

    private val db = FirebaseFirestore.getInstance()
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        dbHelper = DatabaseHelper(this)

        fetchUserData()
        fetchFoodData()
        fetchCartData()
        fetchAddressData()
        fetchIngredientData()
        fetchRestaurantInfo()

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

    override fun onResume() {
        super.onResume()
        dbHelper = DatabaseHelper(this)
        cartFab.count = dbHelper.getCartItemCount()
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

                dbHelper.deleteAllFood()

                for (document in documents) {
                    val foodId = document.id
                    val nameMap = document.get("name") as? Map<*, *>
                    val name_en = nameMap?.get("en") as? String ?: ""
                    val name_vi = nameMap?.get("vi") as? String ?: ""
                    val price = document.getString("price")?.toIntOrNull() ?: 0
                    val formattedPrice = DecimalFormat("#,###").format(price)
                    val imgPath = document.getString("imgPath") ?: ""
                    val ingredientString = document.getString("ingredient") ?: ""
                    val category = document.getString("category") ?: ""
                    val ingredientList = ingredientString.split(", ").map { it.trim() }
                    val foodItem = FoodData(foodId, name_en, name_vi, formattedPrice, category, imgPath, ingredientList)

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

    private fun fetchCartData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("Cart")
            .document(userId)
            .collection("items")
            .get()
            .addOnSuccessListener { documents ->

                dbHelper.deleteAllCart()

                for (document in documents.documents) {
                    val cartId = document.id
                    val foodId = document.getLong("food_id")?.toInt() ?: 0
                    val price = document.getLong("price")?.toInt() ?: 0
                    val ingredients = document.get("ingredients") as? List<String> ?: emptyList()
                    val size = document.getString("size") ?: ""
                    val crust = document.getString("crust") ?: ""
                    val crustBase = document.getString("crustBase") ?: ""
                    val quantity = document.getLong("quantity")?.toInt() ?: 1

                    val cartItem = CartData(
                        food_id = foodId,
                        price = price,
                        ingredients = ingredients,
                        size = size,
                        crust = crust,
                        crustBase = crustBase,
                        quantity = quantity
                    )

                    dbHelper.addFoodToCartWithId(cartId, cartItem)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Failed to load cart data", exception)
            }
    }

    private fun fetchAddressData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("Address")
            .document(userId)
            .collection("items")
            .get()
            .addOnSuccessListener { documents ->

                dbHelper.deleteAllAddress()

                for (document in documents) {
                    val addressId = document.id
                    val name = document.getString("name") ?: ""
                    val phone = document.getString("phone") ?: ""
                    val address = document.getString("address") ?: ""
                    val isDefault = document.getBoolean("default") ?: false
                    val addressItem = AddressData(name, phone, address, isDefault)
                    Log.d("AddressData", "Address ID: $addressId, Name: $name, Phone: $phone, Address: $address, IsDefault: $isDefault")

                    dbHelper.addAddressWithId(addressId, addressItem)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Failed to load address data", exception)
            }
    }

    private fun fetchUserData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            db.collection("Users").document(userId)
                .get()
                .addOnSuccessListener { document ->

                    dbHelper.deleteAllUser()

                    if (document != null && document.exists()) {
                        val name = document.getString("name") ?: "Guest"
                        val email = document.getString("email") ?: ""
                        val phone = document.getString("phone") ?: ""
                        val address = document.getString("address") ?: ""
                        val points = document.getLong("points")?.toInt() ?: 0
                        val user = UserData(userId, email, name, phone, address, points)

                        val sharedPref = this.getSharedPreferences("user_pref", Context.MODE_PRIVATE)
                        sharedPref.edit().putString("username", name).apply()

                        dbHelper.addUser(user)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("Firestore", "Failed to load user data", exception)
                }
        }
    }

    private fun fetchRestaurantInfo() {
        db.collection("RestaurantInfo").document("Info")
        .get()
            .addOnSuccessListener { document ->

                dbHelper.deleteAllRestaurantInfo()

                if (document != null && document.exists()) {
                    val nameInfo = document.getString("name").toString()
                    val locationInfo = document.getString("location").toString()
                    val mailInfo = document.getString("mail").toString()
                    val phoneInfo = document.getString("phone").toString()

                    dbHelper.addRestaurantInfo(nameInfo, locationInfo, mailInfo, phoneInfo)
                }
            }
            .addOnFailureListener { exception ->
                Log.d("Firestore", "Lỗi khi đọc document: ", exception)
            }
    }
}
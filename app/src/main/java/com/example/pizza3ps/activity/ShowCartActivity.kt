package com.example.pizza3ps.activity

import android.content.Context
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pizza3ps.R
import com.example.pizza3ps.adapter.CartAdapter
import com.example.pizza3ps.database.DatabaseHelper
import com.example.pizza3ps.model.CartData

class ShowCartActivity : AppCompatActivity() {
    private lateinit var backButton: ImageView
    private lateinit var cartRecyclerView: RecyclerView
    private lateinit var cartAdapter: CartAdapter
    private var cartList = listOf<CartData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_show_cart)

        // Lấy danh sách giỏ hàng từ cart
        val dbHelper = DatabaseHelper(this)
//        dbHelper.deleteAllCart()
        cartList = dbHelper.getAllCartItems()

        backButton = findViewById(R.id.back_button)
        cartRecyclerView = findViewById(R.id.cart_recyclerView)
        cartRecyclerView.layoutManager = LinearLayoutManager(this)
        //cartRecyclerView.adapter = CartAdapter(cartList)
        cartAdapter = CartAdapter(this, cartList)
        cartRecyclerView.adapter = cartAdapter
        backButton.setOnClickListener {
            onBackPressed()
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val langCode = prefs.getString("lang", "en") ?: "en"
        val context = com.example.pizza3ps.tool.LanguageHelper.setLocale(newBase, langCode)
        super.attachBaseContext(context)
    }

}
package com.example.pizza3ps.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pizza3ps.R
import com.example.pizza3ps.adapter.CartAdapter
import com.example.pizza3ps.database.DatabaseHelper
import com.example.pizza3ps.model.CartData
import java.text.DecimalFormat

class ShowCartActivity : AppCompatActivity() {
    private lateinit var backButton: ImageView
    private lateinit var totalPrice : TextView
    private lateinit var checkoutButton : Button
    private lateinit var cartRecyclerView: RecyclerView
    private var cartList = listOf<CartData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_show_cart)

        // Lấy danh sách giỏ hàng từ cart
        val dbHelper = DatabaseHelper(this)
        cartList = dbHelper.getAllCartItems()

        backButton = findViewById(R.id.back_button)
        totalPrice = findViewById(R.id.total_price)
        checkoutButton = findViewById(R.id.checkout_button)
        cartRecyclerView = findViewById(R.id.cart_recyclerView)
        cartRecyclerView.layoutManager = LinearLayoutManager(this)
        cartRecyclerView.adapter = CartAdapter(this, cartList)

        val totalPrice = dbHelper.calculateTotalPrice()
        val formattedTotalPrice = DecimalFormat("#,###").format(totalPrice) + " VND"
        this.totalPrice.text = formattedTotalPrice

        backButton.setOnClickListener {
            onBackPressed()
        }

        checkoutButton.setOnClickListener {
            val intent = Intent(this, PaymentActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onResume() {
        super.onResume()

        val dbHelper = DatabaseHelper(this)

        // Clear cartList va doc lai du lieu
        cartList = listOf<CartData>()
        cartList = dbHelper.getAllCartItems()

        // Cap nhat lai adapter
        cartRecyclerView.adapter = CartAdapter(this, cartList)
        cartRecyclerView.adapter?.notifyDataSetChanged()

        // Cap nhat lai tong tien
        val totalPrice = dbHelper.calculateTotalPrice()
        val formattedTotalPrice = DecimalFormat("#,###").format(totalPrice) + " VND"
        this.totalPrice.text = formattedTotalPrice
    }

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val langCode = prefs.getString("lang", "en") ?: "en"
        val context = com.example.pizza3ps.tool.LanguageHelper.setLocale(newBase, langCode)
        super.attachBaseContext(context)
    }

}
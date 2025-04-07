package com.example.pizza3ps.activity

import android.content.Intent
import android.content.Context
import android.os.Bundle
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
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    lateinit var cartFab: CounterFab
    private lateinit var bottomNavigationView: BottomNavigationView

//    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

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
            // Chuyển đến activity giỏ hàng
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


}

package com.example.pizza3ps.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.pizza3ps.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class AdminMainActivity : AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_main)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        bottomNavigationView = findViewById(R.id.nav_view)
        bottomNavigationView.setupWithNavController(navController)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_admin_statistics -> {
                    if (navController.currentDestination?.id != R.id.navigation_admin_statistics) {
                        navController.popBackStack(R.id.navigation_admin_statistics, false)
                    }
                    true
                }

                else -> {
                    NavigationUI.onNavDestinationSelected(item, navController)
                }
            }
        }
    }
}
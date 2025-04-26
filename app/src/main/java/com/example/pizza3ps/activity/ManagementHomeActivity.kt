package com.example.pizza3ps.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.pizza3ps.R
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar

class ManagementHomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_management_home)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val textViewCurrentDate = findViewById<TextView>(R.id.textViewCurrentDate)
        val btnLogOut = findViewById<MaterialButton>(R.id.btnManagementLogOut)
        val layoutManageOrders = findViewById<ConstraintLayout>(R.id.constraintLayoutManageOrders)
        val layoutViewStats = findViewById<ConstraintLayout>(R.id.constraintLayoutViewStats)

        val time = Calendar.getInstance().time
        val formatter = SimpleDateFormat("dd MMM yyyy")

        textViewCurrentDate.setText(formatter.format(time))

        btnLogOut.setOnClickListener {
            signOut(this)
        }

        layoutManageOrders.setOnClickListener {
            val intent = Intent(this, OrderManagementActivity::class.java)
            startActivity(intent)
        }

        layoutViewStats.setOnClickListener {
            val intent = Intent(this, ViewStatisticsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun signOut(context: Context) {
        FirebaseAuth.getInstance().signOut()
        val sharedPref = context.getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        sharedPref.edit().remove("username").apply()
        val intent = Intent(context, LogInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
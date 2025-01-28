package com.example.pizza3ps

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        val backButton: ImageView = findViewById(R.id.back_button)
        val logInButton : TextView = findViewById(R.id.text_view_2)

        backButton.setOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)

            startActivity(intent)
        }

        logInButton.setOnClickListener {
            val intent = Intent(this, LogInActivity::class.java)

            startActivity(intent)
        }
    }
}
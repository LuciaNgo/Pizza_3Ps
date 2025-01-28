package com.example.pizza3ps

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_welcome)

        val signUpButton : Button = findViewById(R.id.sign_up_button)
        val logInButton : Button = findViewById(R.id.log_in_button)

        signUpButton.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)

            startActivity(intent)
        }

        logInButton.setOnClickListener {
            val intent = Intent(this, LogInActivity::class.java)

            startActivity(intent)
        }
    }
}
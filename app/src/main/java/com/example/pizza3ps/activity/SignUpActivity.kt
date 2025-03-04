package com.example.pizza3ps.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.pizza3ps.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class SignUpActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var nameEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var signUpButton: Button
    private lateinit var logInButton: TextView
    private lateinit var backButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        auth = FirebaseAuth.getInstance() // Khởi tạo Firebase Authentication

        nameEditText = findViewById(R.id.name_input)
        phoneEditText = findViewById(R.id.phone_input)
        emailEditText = findViewById(R.id.email_input)
        passwordEditText = findViewById(R.id.password_input)
        confirmPasswordEditText = findViewById(R.id.confirm_password_input)
        signUpButton = findViewById(R.id.sign_up_button)
        backButton = findViewById(R.id.back_button)
        logInButton = findViewById(R.id.text_view_2)

        backButton.setOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)

            startActivity(intent)
        }

        logInButton.setOnClickListener {
            val intent = Intent(this, LogInActivity::class.java)

            startActivity(intent)
        }

        signUpButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val phone = phoneEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please enter full information!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Confirmation password does not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            registerUser(name, phone, email, password)
        }
    }

    private fun registerUser(name: String, phone: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val db = FirebaseFirestore.getInstance()
                    val metadataRef = db.collection("metadata").document("user_counter")

                    metadataRef.get().addOnSuccessListener { document ->
                        val currentId = document.getLong("current_id") ?: 0
                        val newId = currentId + 1

                        // Cập nhật ID mới vào metadata
                        metadataRef.set(mapOf("current_id" to newId), SetOptions.merge())

                        // Tạo user map
                        val userMap = hashMapOf(
                            "name" to name,
                            "phone" to phone,
                            "email" to email
                        )

                        // Lưu thông tin user vào Firestore
                        db.collection("Users").document(newId.toString()).set(userMap)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Sign up successful!", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, LogInActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Firestore Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }.addOnFailureListener { e ->
                        Toast.makeText(this, "Firestore Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Auth Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
package com.example.pizza3ps.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.pizza3ps.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class AddProfileActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var user: FirebaseUser? = null
    private lateinit var nameEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var createButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        user = auth.currentUser

        nameEditText = findViewById(R.id.profile_name_input)
        phoneEditText = findViewById(R.id.profile_phone_input)
        emailEditText = findViewById(R.id.profile_email_input)
        passwordEditText = findViewById(R.id.profile_password_input)
        confirmPasswordEditText = findViewById(R.id.profile_confirm_password_input)
        createButton = findViewById(R.id.profile_create_button)

        val userName = user?.displayName.toString()
        val userEmail = user?.email.toString()

        nameEditText.setText(userName)
        emailEditText.setText(userEmail)

        createButton.setOnClickListener {
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

            addUserProfile(name, phone, email, password)
        }
    }

    private fun addUserProfile(name: String, phone: String, email: String, password: String) {
        val db = FirebaseFirestore.getInstance()
        val metadataRef = db.collection("metadata").document("user_counter")

        metadataRef.get().addOnSuccessListener { document ->
            val userId = auth.currentUser?.uid.toString()
            // Cập nhật ID mới vào metadata
            metadataRef.set(mapOf("current_id" to userId), SetOptions.merge())

            // Tạo user map
            val userMap = hashMapOf(
                "name" to name,
                "phone" to phone,
                "email" to email,
                "address" to "",
                "points" to 0
            )

            // Lưu thông tin user vào Firestore
            db.collection("Users").document(userId.toString()).set(userMap)
                .addOnSuccessListener {
                    Toast.makeText(this, "Create profile successful!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LogInActivity::class.java))
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Firestore Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Firestore Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
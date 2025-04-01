package com.example.pizza3ps.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.pizza3ps.R
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore

class CustomerServiceActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_customer_service)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val mail = findViewById<TextView>(R.id.customerServiceMailValue)
        val phone = findViewById<TextView>(R.id.customerServicePhoneValue)

        val docRef = db.collection("RestaurantInfo").document("Info")
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val locationInfo = document.getString("location")
                    val mailInfo = document.getString("mail")
                    val phoneInfo = document.getString("phone")

                    Log.d("Firestore", "address: $locationInfo")
                    Log.d("Firestore", "mail: $mailInfo")
                    Log.d("Firestore", "phone: $phoneInfo")
                    Log.d("access", "ok")

                    mail.text = mailInfo
                    phone.text = phoneInfo
                } else {
                    Log.d("Firestore", "Document không tồn tại")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("Firestore", "Lỗi khi đọc document: ", exception)
            }

        findViewById<MaterialButton>(R.id.btnMakeCall).setOnClickListener {
            val phoneNumber = phone.text.toString()
            Log.d("phoneNumber", "$phoneNumber")
            val callIntent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CALL_PHONE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startActivity(callIntent)
            } else {
                // Yêu cầu cấp quyền CALL_PHONE nếu chưa có
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), 1)
            }
        }
    }
}
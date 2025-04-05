package com.example.pizza3ps.fragment

import android.content.Context
import com.example.pizza3ps.model.UserData
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pizza3ps.R
import com.example.pizza3ps.database.UserDatabaseHelper
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AccountFragment : Fragment() {
    private lateinit var btnCustomerService: ConstraintLayout
    private val db = FirebaseFirestore.getInstance()
    private lateinit var userNameView: TextView
    private lateinit var showInfoBtn : MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account, container, false)

        // Lấy tham chiếu đến nút Customer Service
        btnCustomerService = view.findViewById(R.id.btnCustomerService)

        // Thiết lập sự kiện nhấn nút
        btnCustomerService.setOnClickListener {
            // Dùng NavController để điều hướng tới CustomerServiceFragment
            findNavController().navigate(R.id.action_accountFragment_to_customerServiceFragment)
        }

        // Cập nhật padding cho view khi có thay đổi từ system bars (mặc định dùng EdgeToEdge)
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        userNameView = view.findViewById(R.id.userName)
        showInfoBtn = view.findViewById(R.id.informationBtn)

        val sharedPref = requireContext().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val cachedName = sharedPref.getString("username", "Guest")
        userNameView.text = cachedName


        Log.d("FETCH", "fetchUserData() called")


        if (cachedName == "Guest") {
            fetchUserData()
        } else {
            userNameView.text = cachedName
        }

        showInfoBtn.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_account_to_personalInfoFragment)
        }


        return view
    }

    private fun fetchUserData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(context, "Please log in first", Toast.LENGTH_SHORT).show()
            return
        }
//        val userId = "10"
        val dbHelper = UserDatabaseHelper(requireContext())

        db.collection("Users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                Log.e("Firestore_FetchData", "Begin to fetch")
                if (document != null && document.exists()) {
                    val name = document.getString("name") ?: "Guest"
                    val email = document.getString("email") ?: ""
                    val phone = document.getString("phone") ?: ""
                    val address = document.getString("address") ?: ""
                    val points = document.getLong("points")?.toInt() ?: 0
                    Log.d("FETCH", "Document loaded successfully: name = $name")

                    // Display name in UI
                    userNameView.text = name

                    // After getting the name from Firebase or SQLite
                    val sharedPref = requireContext().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
                    sharedPref.edit().putString("username", name).apply()

                    // Save to SQLite
                    val user = UserData(name, email, phone, address, points)
                    dbHelper.addUser(user)
                    Log.e("Firestore_FetchDataDone", "Done")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Failed to load user data", exception)
                //Toast.makeText(context, "Failed to load user data", Toast.LENGTH_SHORT).show()
            }
    }

}

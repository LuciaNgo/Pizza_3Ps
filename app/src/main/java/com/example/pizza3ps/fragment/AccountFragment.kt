package com.example.pizza3ps.fragment

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
import com.example.pizza3ps.database.DatabaseHelper
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AccountFragment : Fragment() {
    private lateinit var btnCustomerService: ConstraintLayout
    private val db = FirebaseFirestore.getInstance()
    private lateinit var userNameView: TextView
    private lateinit var showInfoBtn : MaterialButton
    private lateinit var fab: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account, container, false)

        fab = requireActivity().findViewById(R.id.cart_fab)
        fab.visibility = View.GONE

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


        Log.d("FETCH", "fetchUserData() called")


        fetchUserData()

        showInfoBtn.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_account_to_personalInfoFragment)
        }


        return view
    }

    private fun fetchUserData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        Log.d("userID", userId.toString())
        if (userId == null) {
            Toast.makeText(context, "Please log in first", Toast.LENGTH_SHORT).show()
            return
        }
//        val userId = "10"
        val dbHelper = DatabaseHelper(requireContext())

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

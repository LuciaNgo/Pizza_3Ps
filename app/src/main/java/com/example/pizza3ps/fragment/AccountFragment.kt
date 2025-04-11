package com.example.pizza3ps.fragment

import android.content.Intent
import android.content.Context
import com.example.pizza3ps.model.UserData
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.andremion.counterfab.CounterFab
import com.example.pizza3ps.R
import com.example.pizza3ps.activity.LogInActivity
import com.example.pizza3ps.database.DatabaseHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AccountFragment : Fragment() {
    private lateinit var termsPoliciesLayout: ConstraintLayout
    private lateinit var customerServiceLayout: ConstraintLayout
    private lateinit var userInfoLayout: ConstraintLayout
    private lateinit var changeLanguageLayout: ConstraintLayout
    private lateinit var logOutLayout: LinearLayout
    private lateinit var userNameView: TextView
    private lateinit var fab: CounterFab
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account, container, false)

        termsPoliciesLayout = view.findViewById(R.id.termsAndPoliciesContainer)
        customerServiceLayout = view.findViewById(R.id.supportCenterContainer)
        userInfoLayout = view.findViewById(R.id.userInfoContainer)
        changeLanguageLayout = view.findViewById(R.id.languageContainer)
        logOutLayout = view.findViewById(R.id.logoutContainer)
        userNameView = view.findViewById(R.id.userName)
        fab = requireActivity().findViewById(R.id.cart_fab)
        fab.visibility = View.GONE

        val sharedPref = requireContext().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val cachedName = sharedPref.getString("username", "Guest")
        userNameView.text = cachedName

        Log.d("FETCH", "fetchUserData() called")

        if (cachedName == "Guest") {
            fetchUserData()
        } else {
            userNameView.text = cachedName
        }

        customerServiceLayout.setOnClickListener {
            findNavController().navigate(R.id.action_accountFragment_to_customerServiceFragment)
        }

        userInfoLayout.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_account_to_personalInfoFragment)
        }

        changeLanguageLayout.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_account_to_languageFragment)
        }

        logOutLayout.setOnClickListener {
            signOut()
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

                    // After getting the name from Firebase or SQLite
                    val sharedPref = requireContext().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
                    sharedPref.edit().putString("username", name).apply()

                    // Save to SQLite
                    val user = UserData(email, name, phone, address, points)
                    dbHelper.addUser(user)
                    Log.e("Firestore_FetchDataDone", "Done")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Failed to load user data", exception)
                //Toast.makeText(context, "Failed to load user data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        val sharedPref = requireContext().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        sharedPref.edit().remove("username").apply()
        val intent = Intent(context, LogInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}

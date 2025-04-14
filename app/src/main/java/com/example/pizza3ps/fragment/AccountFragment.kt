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

class AccountFragment : Fragment() {
    private lateinit var savedAddressLayout: ConstraintLayout
    private lateinit var termsPoliciesLayout: ConstraintLayout
    private lateinit var customerServiceLayout: ConstraintLayout
    private lateinit var userInfoLayout: ConstraintLayout
    private lateinit var changeLanguageLayout: ConstraintLayout
    private lateinit var logOutLayout: LinearLayout
    private lateinit var userNameView: TextView
    private lateinit var fab: CounterFab

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account, container, false)

        savedAddressLayout = view.findViewById(R.id.addressContainer)
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

        savedAddressLayout.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_account_to_savedAddressFragment)
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
        val dbHelper = DatabaseHelper(requireContext())
        val user = dbHelper.getUser()
        if (user != null) {
            userNameView.text = user.name
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

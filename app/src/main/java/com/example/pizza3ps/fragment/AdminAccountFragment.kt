package com.example.pizza3ps.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.example.pizza3ps.R
import com.example.pizza3ps.activity.LogInActivity
import com.google.firebase.auth.FirebaseAuth


class AdminAccountFragment : Fragment() {
    private lateinit var logOutLayout: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_account, container, false)

        logOutLayout = view.findViewById(R.id.logoutContainer)

        logOutLayout.setOnClickListener {
            signOut()
        }

        return view
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
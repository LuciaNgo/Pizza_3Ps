package com.example.pizza3ps.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.pizza3ps.R
import com.example.pizza3ps.database.DatabaseHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PersonalInfoFragment : Fragment() {
    private lateinit var nameEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var emailEditText: EditText

    private lateinit var preName: String
    private lateinit var prePhone: String

    private lateinit var backBtn : ImageView
    private lateinit var editBtn : Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_personal_info, container, false)
        val user = DatabaseHelper(requireContext()).getUser()
        preName = user?.name.toString()
        prePhone = user?.phone.toString()

        nameEditText = view.findViewById(R.id.nameHolder)
        phoneEditText = view.findViewById(R.id.phoneHolder)
        emailEditText = view.findViewById(R.id.emailHolder)

        nameEditText.setText(user?.name)
        phoneEditText.setText(user?.phone)
        emailEditText.setText(user?.email)
        emailEditText.isEnabled = false

        backBtn = view.findViewById(R.id.backButton)
        editBtn = view.findViewById(R.id.editBtn)

        backBtn.setOnClickListener{
            findNavController().navigate(R.id.action_personalInfoFragment_to_navigation_account)
        }

        editBtn.setOnClickListener {
            if (nameEditText.text.isEmpty() || phoneEditText.text.isEmpty()) {
                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (nameEditText.text.toString() == preName && phoneEditText.text.toString() == prePhone) {
                Toast.makeText(context, "No changes made", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newName = nameEditText.text.toString()
            val newPhone = phoneEditText.text.toString()

            updateName(newName)
            updatePhone(newPhone)
        }
        return view
    }

    private fun updateName(newName: String) {
        // Db
        val dbHelper = DatabaseHelper(requireContext())
        dbHelper.updateUserName(newName)

        // Firebase
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection(("Users")).document(userId)

        docRef.get().addOnSuccessListener { document ->
            docRef.update("name", newName)
        }
        .addOnSuccessListener {
            Toast.makeText(context, "Name updated successfully", Toast.LENGTH_SHORT).show()
        }
            .addOnFailureListener {
            Toast.makeText(context, "Failed to update name", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updatePhone(newPhone: String) {
        // Db
        val dbHelper = DatabaseHelper(requireContext())
        dbHelper.updateUserPhone(newPhone)

        // Firebase
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection(("Users")).document(userId)

        docRef.get().addOnSuccessListener { document ->
            docRef.update("phone", newPhone)
        }
        .addOnSuccessListener {
            Toast.makeText(context, "Phone updated successfully", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener {
            Toast.makeText(context, "Failed to update phone", Toast.LENGTH_SHORT).show()
        }
    }
}
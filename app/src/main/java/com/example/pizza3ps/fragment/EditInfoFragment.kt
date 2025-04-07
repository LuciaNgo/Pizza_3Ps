package com.example.pizza3ps.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.pizza3ps.R
import com.example.pizza3ps.database.DatabaseHelper
import com.example.pizza3ps.model.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditInfoFragment : Fragment() {

    private lateinit var saveBtn : Button
    private lateinit var cancelBtn : Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_edit_info, container, false)

        Log.d("Received", "begin receiving")
        val args = EditInfoFragmentArgs.fromBundle(requireArguments())

        val name = args.name
        val email = args.email
        val phone = args.phone
        val address = args.address
        val points = args.points

        Log.d("Received_done", "Done receiving")

        view.findViewById<EditText>(R.id.nameHolder).setText(name)
        view.findViewById<EditText>(R.id.emailHolder).setText(email)
        view.findViewById<EditText>(R.id.phoneHolder).setText(phone)
        view.findViewById<EditText>(R.id.addressHolder).setText(address)
        view.findViewById<TextView>(R.id.pointHolder).text = "$points"

        saveBtn = view.findViewById(R.id.saveBtn)
        cancelBtn = view.findViewById(R.id.cancelBtn)

        saveBtn.setOnClickListener {
            saveUpdatedInfo()
        }

        cancelBtn.setOnClickListener {
            findNavController().navigate(R.id.action_editInfoFragment_to_personalInfoFragment2)
                    }


        return view
    }

    private fun saveUpdatedInfo() {
        // 1. Get updated values from EditTexts
        val updatedName = view?.findViewById<EditText>(R.id.nameHolder)?.text.toString()
        val updatedEmail = view?.findViewById<EditText>(R.id.emailHolder)?.text.toString()
        val updatedPhone = view?.findViewById<EditText>(R.id.phoneHolder)?.text.toString()
        val updatedAddress = view?.findViewById<EditText>(R.id.addressHolder)?.text.toString()
        val updatedPointsText = view?.findViewById<EditText>(R.id.pointHolder)?.text.toString()
        val updatedPoints = updatedPointsText.toLongOrNull() ?: 0L

        // 2. Firestore user ID – use current UID or hardcoded test ID
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(context, "Please log in first", Toast.LENGTH_SHORT).show()
            return
        }

        val db = FirebaseFirestore.getInstance()
        val updatedData = hashMapOf(
            "name" to updatedName,
            "email" to updatedEmail,
            "phone" to updatedPhone,
            "address" to updatedAddress,
            "points" to updatedPoints
        )

        // 3. Update Firestore
        db.collection("Users").document(userId)
            .update(updatedData as Map<String, Any>)
            .addOnSuccessListener {
                // 4. Also update local SQLite
                val dbHelper = DatabaseHelper(requireContext())
                val user = UserData(updatedName, updatedEmail, updatedPhone, updatedAddress, updatedPoints.toInt())
                dbHelper.addUser(user)

                val sharedPref = requireContext().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
                sharedPref.edit().putString("username", updatedName).apply()


                Toast.makeText(context, "Information updated!", Toast.LENGTH_SHORT).show()

                // 5. Navigate back to AccountFragment
                findNavController().navigate(R.id.action_editInfoFragment_to_navigation_account)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
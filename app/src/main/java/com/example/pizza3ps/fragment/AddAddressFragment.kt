package com.example.pizza3ps.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import androidx.navigation.fragment.findNavController
import com.example.pizza3ps.R
import com.example.pizza3ps.database.DatabaseHelper
import com.example.pizza3ps.model.AddressData
import com.example.pizza3ps.model.CartData
import com.google.firebase.firestore.FirebaseFirestore

class AddAddressFragment : Fragment() {
    private lateinit var nameEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var addressEditText: EditText
    private lateinit var mapIcon: ImageView
    private lateinit var defaultAddress: CheckBox
    private lateinit var saveButton: Button
    private lateinit var backButton: ImageView

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_add_address, container, false)

        dbHelper = DatabaseHelper(requireContext())

        nameEditText = view.findViewById(R.id.receiverName)
        phoneEditText = view.findViewById(R.id.receiverPhone)
        addressEditText = view.findViewById(R.id.receiverAddress)
        mapIcon = view.findViewById(R.id.receiverAddressMap)
        defaultAddress = view.findViewById(R.id.receiverDefaultAddress)
        saveButton = view.findViewById(R.id.addAddressButton)
        backButton = view.findViewById(R.id.backButton)

        saveButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val phone = phoneEditText.text.toString()
            val address = addressEditText.text.toString()
            val isDefault = defaultAddress.isChecked

            if (name.isNotEmpty() && phone.isNotEmpty() && address.isNotEmpty()) {
                // Validate the phone number format
                if (!phone.matches(Regex("^[0-9]{10}$"))) {
                    phoneEditText.error = "Invalid phone number"
                    return@setOnClickListener
                }
                // Validate the address format
                if (address.length < 10) {
                    addressEditText.error = "Address must be at least 10 characters"
                    return@setOnClickListener
                }

                val addressItem = AddressData(
                    name = name,
                    phone = phone,
                    address = address,
                    isDefault = isDefault
                )

                // Save the address to the database
                val dbHelper = DatabaseHelper(requireContext())
                dbHelper.addAddress(addressItem)

                // Sync to firestore
                val userId = dbHelper.getUser()?.id
                if (userId != null) {
                    syncAddressItem(userId, addressItem)
                }

                // Navigate back to the saved addresses screen
                findNavController().navigateUp()
            } else {
                // Show an error message
            }
        }


        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        return view
    }

    fun syncAddressItem(userId: String, addressItem: AddressData) {
        val databaseRef = FirebaseFirestore.getInstance()
            .collection("Address")
            .document(userId)
            .collection("items")

        val id = dbHelper.getAddressId(addressItem)

        databaseRef.document(id.toString())
            .set(addressItem)
            .addOnSuccessListener {
                Log.d("FirebaseSync", "Synced item: $id")
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseSync", "Failed to sync item: $id", e)
            }
    }

}
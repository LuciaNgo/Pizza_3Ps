package com.example.pizza3ps.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import com.example.pizza3ps.R
import com.example.pizza3ps.database.DatabaseHelper
import com.example.pizza3ps.model.AddressData

class AddAddressFragment : Fragment() {
    private lateinit var nameEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var addressEditText: EditText
    private lateinit var mapIcon: ImageView
    private lateinit var defaultAddress: CheckBox
    private lateinit var saveButton: Button
    private lateinit var backButton: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_add_address, container, false)

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
                // Save the address to the database
                val dbHelper = DatabaseHelper(requireContext())
                dbHelper.addAddress(AddressData(name, phone, address, isDefault))

                // Navigate back to the saved addresses screen
                requireActivity().onBackPressed()
            } else {
                // Show an error message
            }
        }


        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        return view
    }

}
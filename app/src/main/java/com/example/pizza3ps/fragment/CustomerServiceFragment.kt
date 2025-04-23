package com.example.pizza3ps.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.andremion.counterfab.CounterFab
import com.example.pizza3ps.R
import com.example.pizza3ps.database.DatabaseHelper
import com.example.pizza3ps.model.RestaurantData

class CustomerServiceFragment : Fragment() {
    private lateinit var restaurantInfo : RestaurantData
    private lateinit var customerServiceMail : ConstraintLayout
    private lateinit var customerServicePhone : ConstraintLayout
    private lateinit var mailText : TextView
    private lateinit var phoneText : TextView
    private lateinit var backButton : ImageView
    private lateinit var fab: CounterFab

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_customer_service, container, false)

        dbHelper = DatabaseHelper(requireContext())

        fab = requireActivity().findViewById(R.id.cart_fab)
        fab.visibility = View.GONE

        customerServiceMail = view.findViewById(R.id.customerServiceMailContainer)
        customerServicePhone = view.findViewById(R.id.customerServicePhoneContainer)
        mailText = view.findViewById(R.id.customerServiceMailText)
        phoneText = view.findViewById(R.id.customerServicePhoneText)
        backButton = view.findViewById(R.id.backButton)

        restaurantInfo = dbHelper.getRestaurantInfo()!!
        mailText.text = "Email: ${restaurantInfo.mail}"
        phoneText.text = "Hotline: ${restaurantInfo.phone}"

        /*
        val docRef = db.collection("RestaurantInfo").document("Info")
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    mailInfo = document.getString("mail").toString()
                    phoneInfo = document.getString("phone").toString()

                    Log.d("Firestore", "mail: $mailInfo")
                    Log.d("Firestore", "phone: $phoneInfo")

                    mailText.text = "Email: $mailInfo"
                    phoneText.text = "Hotline: $phoneInfo"
                } else {
                    Log.d("Firestore", "Document không tồn tại")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("Firestore", "Lỗi khi đọc document: ", exception)
            }
         */

        customerServicePhone.setOnClickListener {
            val phoneNumber = restaurantInfo.phone
            Log.d("phoneNumber", phoneNumber)
            val callIntent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }

            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CALL_PHONE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startActivity(callIntent)
            } else {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CALL_PHONE), 1)
            }
        }

        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        return view
    }
}

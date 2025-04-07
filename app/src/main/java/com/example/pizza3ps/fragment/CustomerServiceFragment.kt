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
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.andremion.counterfab.CounterFab
import com.example.pizza3ps.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore

class CustomerServiceFragment : Fragment() {
    private val db = FirebaseFirestore.getInstance()
    private lateinit var fab: CounterFab

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_customer_service, container, false)

        fab = requireActivity().findViewById(R.id.cart_fab)
        fab.visibility = View.GONE

        val mail = view.findViewById<TextView>(R.id.customerServiceMailValue)
        val phone = view.findViewById<TextView>(R.id.customerServicePhoneValue)
        val btnMakeCall = view.findViewById<MaterialButton>(R.id.btnMakeCall)

        val docRef = db.collection("RestaurantInfo").document("Info")
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val mailInfo = document.getString("mail")
                    val phoneInfo = document.getString("phone")

                    Log.d("Firestore", "mail: $mailInfo")
                    Log.d("Firestore", "phone: $phoneInfo")

                    mail.text = mailInfo
                    phone.text = phoneInfo
                } else {
                    Log.d("Firestore", "Document không tồn tại")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("Firestore", "Lỗi khi đọc document: ", exception)
            }

        btnMakeCall.setOnClickListener {
            val phoneNumber = phone.text.toString()
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

        return view
    }
}

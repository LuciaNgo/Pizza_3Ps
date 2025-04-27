package com.example.pizza3ps.fragment

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
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
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.pizza3ps.R
import com.example.pizza3ps.database.DatabaseHelper
import com.example.pizza3ps.model.AddressData
import com.example.pizza3ps.model.CartData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class AddAddressFragment : Fragment() {
    private lateinit var nameEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var addressEditText: EditText
    private lateinit var mapIcon: ImageView
    private lateinit var defaultAddress: CheckBox
    private lateinit var saveButton: Button
    private lateinit var backButton: ImageView
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var dbHelper: DatabaseHelper
    private var addressId: Int? = null

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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        arguments?.let {
            addressId = it.getInt("selectedAddressId", -1)
        }

        if (addressId != null && addressId != -1) {
            saveButton.text = "Save"
            view.findViewById<TextView>(R.id.toolbar_title).text = "Edit Address Info"
            loadAddressDetails(addressId!!)
        }

        mapIcon.setOnClickListener {
            getCurrentLocation()
        }

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

                if (addressId != null && addressId != -1) {
                    // Update the address in the database
                    dbHelper.updateAddress(addressId!!, addressItem)
                } else {
                    // Add a new address to the database
                    dbHelper.addAddress(addressItem)
                }

                // Sync to firestore
                val userId = dbHelper.getUser()?.id
                if (userId != null) {
                    syncAddressItem(userId, addressItem)
                }

                // Navigate back to the saved addresses screen
                findNavController().popBackStack()
            } else {
                // Show an error message
            }
        }


        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        return view
    }

    private fun loadAddressDetails(addressId: Int) {
        val addressItem = dbHelper.getAddressById(addressId)
        addressItem?.let {
            nameEditText.setText(it.name)
            phoneEditText.setText(it.phone)
            addressEditText.setText(it.address)
            defaultAddress.isChecked = it.isDefault
        }
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

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            requestNewLocationData()
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        val latitude = location.latitude
                        val longitude = location.longitude
                        val address = getAddressFromLocation(requireContext(), latitude, longitude)
                        addressEditText.setText(address)
                    }
                }
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    fun getAddressFromLocation(context: Context, latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(context, Locale.getDefault())
        return try {
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address: Address = addresses[0]
                val addressLines = (0..address.maxAddressLineIndex).map { address.getAddressLine(it) }
                addressLines.joinToString(separator = "\n")
            } else {
                "Không tìm thấy địa chỉ"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "Không thể lấy địa chỉ"
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Log.d("permission", "Đã được cấp quyền truy cập vị trí.")
            } else {
                Log.d("permission", "Quyền truy cập vị trí bị từ chối.")
            }
        }
    }

    private fun requestNewLocationData() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val locationRequest = com.google.android.gms.location.LocationRequest.create().apply {
            priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 0
            fastestInterval = 0
            numUpdates = 1
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            object : com.google.android.gms.location.LocationCallback() {
                override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                    val location = locationResult.lastLocation
                    location?.let {
                        val latitude = it.latitude
                        val longitude = it.longitude
                        if (isAdded) {
                            val address = getAddressFromLocation(requireContext(), latitude, longitude)
                            addressEditText.setText(address)
                            addressEditText.setText(address)
                        }
                    }
                }
            },
            null
        )
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
}
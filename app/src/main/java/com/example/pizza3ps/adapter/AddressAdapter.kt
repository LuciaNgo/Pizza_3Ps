package com.example.pizza3ps.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.pizza3ps.R
import com.example.pizza3ps.database.DatabaseHelper
import com.example.pizza3ps.fragment.SavedAddressFragment
import com.example.pizza3ps.model.AddressData
import com.google.firebase.firestore.FirebaseFirestore

class AddressAdapter(
    private val fragment: SavedAddressFragment,
    private val addressList: List<AddressData>,
    private var selectedAddressId: Int?) :
    RecyclerView.Adapter<AddressAdapter.AddressViewHolder>() {

    private var defaultHolder: AddressViewHolder? = null
    private var selectedHolder: AddressViewHolder? = null

    var onItemSelectedListener: ((Int?) -> Unit)? = null

    class AddressViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val receiverName : TextView = view.findViewById(R.id.receiver_name)
        val receiverPhone : TextView = view.findViewById(R.id.receiver_phone_number)
        val receiverAddress : TextView = view.findViewById(R.id.receiver_address)
        val selectedIcon : ImageView = view.findViewById(R.id.selected_icon)
        val defaultAddress : TextView = view.findViewById(R.id.is_default)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.address_item, parent, false)
        return AddressViewHolder(view)
    }

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        val address = addressList[position]
        holder.receiverName.text = address.name
        holder.receiverPhone.text = address.phone
        holder.receiverAddress.text = address.address
        holder.selectedIcon.visibility = View.GONE
        holder.defaultAddress.visibility = View.GONE

        if (address.isDefault) {
            holder.defaultAddress.visibility = View.VISIBLE
        }

        val dbHelper = DatabaseHelper(fragment.requireContext())
        val addressId = dbHelper.getAddressId(addressList[position])

        if (selectedAddressId != null && addressId == selectedAddressId) {
            holder.selectedIcon.visibility = View.VISIBLE
            selectedHolder = holder
        }

        holder.itemView.setOnClickListener {
            selectedHolder?.selectedIcon?.visibility = View.GONE
            holder.selectedIcon.visibility = View.VISIBLE
            selectedHolder = holder
            selectedAddressId = addressId

            onItemSelectedListener?.invoke(selectedAddressId)
        }
    }

    fun deleteItem(position: Int) {
        val dbHelper = DatabaseHelper(fragment.requireContext())
        val addressId = dbHelper.getAddressId(addressList[position])
        val name = addressList[position].name
        val phone = addressList[position].phone
        val address = addressList[position].address
        val isDefault = addressList[position].isDefault
        Log.d("AddressAdapter", "Deleting address: $name, $phone, $address, $isDefault")
        Log.d("AddressAdapter", "Deleting address with ID: $addressId")
        dbHelper.deleteAddress(addressId)

        if (selectedAddressId == addressId) {
            selectedAddressId = null
            onItemSelectedListener?.invoke(selectedAddressId)
        }

        (addressList as MutableList).removeAt(position)
        notifyItemRemoved(position)
    }

    fun removeSyncToFirebase(addressId: Int) {
        val dbHelper = DatabaseHelper(fragment.requireContext())
        val userId = dbHelper.getUser()!!.id

        FirebaseFirestore.getInstance()
            .collection("Address")
            .document(userId)
            .collection("items")
            .document(addressId.toString())
            .delete()
            .addOnSuccessListener {
                Log.d("FirebaseSync", "Synced item: $addressId")
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseSync", "Failed to sync item: $addressId", e)
            }
    }

    fun setDefaultAddress() {

    }

    fun getSelectedAddressId() : Int? {
        return selectedAddressId
    }

    override fun getItemCount() = addressList.size

}
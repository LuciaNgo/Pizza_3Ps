package com.example.pizza3ps.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.pizza3ps.R
import com.example.pizza3ps.model.AddressData

class AddressAdapter(private val addressList: List<AddressData>) :
    RecyclerView.Adapter<AddressAdapter.AddressViewHolder>() {

    class AddressViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val receiverName : TextView = view.findViewById(R.id.receiver_name)
        val receiverPhone : TextView = view.findViewById(R.id.receiver_phone_number)
        val receiverAddress : TextView = view.findViewById(R.id.receiver_address)
        val selectedIcon : ImageView = view.findViewById(R.id.selected_icon)
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

        if (address.isDefault) {
        }

        holder.itemView.setOnClickListener {
            if (holder.selectedIcon.visibility == View.GONE) {
                holder.selectedIcon.visibility = View.VISIBLE
            } else {
                holder.selectedIcon.visibility = View.GONE
            }
        }
    }

    override fun getItemCount() = addressList.size

}
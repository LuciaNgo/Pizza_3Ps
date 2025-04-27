package com.example.pizza3ps.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pizza3ps.R
import com.example.pizza3ps.database.DatabaseHelper
import com.example.pizza3ps.model.OrderData
import java.text.DecimalFormat

class RedeemAdapter(
    private val context: Context,
    private val items: List<OrderData>
) : RecyclerView.Adapter<RedeemAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val orderID: TextView = view.findViewById(R.id.order_id)
        val orderDate: TextView = view.findViewById(R.id.order_date)
        val totalPrice: TextView = view.findViewById(R.id.total_price)
        val pointPlus: TextView = view.findViewById(R.id.points_plus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.redeem_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.orderID.text = item.orderId
        holder.orderDate.text = item.createdDate
        holder.totalPrice.text = DecimalFormat("#,###").format(item.totalAfterDiscount) + " VND"
        holder.pointPlus.text = "+" + (item.totalAfterDiscount/1000 * 10).toString() + " pts"
    }

    override fun getItemCount() = items.size
}
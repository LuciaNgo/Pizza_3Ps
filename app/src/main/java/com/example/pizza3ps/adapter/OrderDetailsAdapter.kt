package com.example.pizza3ps.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pizza3ps.R
import com.example.pizza3ps.database.DatabaseHelper
import com.example.pizza3ps.model.CartData
import java.text.DecimalFormat

class OrderDetailsAdapter(private val orderDetailsList: List<CartData>) :
    RecyclerView.Adapter<OrderDetailsAdapter.OrderDetailsViewHolder>() {

    inner class OrderDetailsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.food_name)
        val size: TextView = view.findViewById(R.id.food_size)
        val crust: TextView = view.findViewById(R.id.food_crust)
        val crustBase: TextView = view.findViewById(R.id.food_crust_base)
        val ingredients: TextView = view.findViewById(R.id.food_ingredients)
        val quantity: TextView = view.findViewById(R.id.food_quantity)
        val price: TextView = view.findViewById(R.id.food_price)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderDetailsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.order_details_item, parent, false)
        return OrderDetailsViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderDetailsViewHolder, position: Int) {
        val item = orderDetailsList[position]

        if (item.food_id != 0) {
            val dlHelper = DatabaseHelper(holder.itemView.context)
            val foodInfo = dlHelper.getFoodById(item.food_id)

            holder.name.text = foodInfo.getName("en")

            if (foodInfo.category == "pizza") {
                holder.ingredients.visibility = View.VISIBLE
                holder.ingredients.text = item.user_email
            } else {
                holder.ingredients.visibility = View.GONE
            }
        }
        else if (item.food_id == 0) { // customize pizza
            holder.name.text = "Customize pizza"
            holder.ingredients.visibility = View.VISIBLE
            holder.ingredients.text = item.user_email
        }

        if (item.size == "") {
            holder.size.visibility = View.GONE
        } else {
            holder.size.visibility = View.VISIBLE
            holder.size.text = "Size: ${item.size}"
        }

        if (item.crust == "") {
            holder.crust.visibility = View.GONE
        } else {
            holder.crust.visibility = View.VISIBLE
            holder.crust.text = "Crust: ${item.crust}"
        }

        if (item.crustBase == "") {
            holder.crustBase.visibility = View.GONE
        } else {
            holder.crustBase.visibility = View.VISIBLE
            holder.crustBase.text = "Crust base: ${item.crustBase}"
        }

        val price = item.price * item.quantity
        val formattedPrice = DecimalFormat("#,###").format(price)
        holder.price.text = "$formattedPrice VND"
        holder.quantity.text = "Quantity: ${item.quantity}"
    }

    override fun getItemCount(): Int = orderDetailsList.size


}
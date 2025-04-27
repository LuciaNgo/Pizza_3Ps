package com.example.pizza3ps.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pizza3ps.R
import com.example.pizza3ps.database.DatabaseHelper
import com.example.pizza3ps.model.CartData
import com.example.pizza3ps.model.OrderItemData
import java.text.DecimalFormat

class OrderDetailsAdapter(private val orderDetailsList: List<OrderItemData>) :
    RecyclerView.Adapter<OrderDetailsAdapter.OrderDetailsViewHolder>() {

    inner class OrderDetailsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val foodImage: ImageView = view.findViewById(R.id.food_image)
        val foodName: TextView = view.findViewById(R.id.food_name)
        val foodDescription: TextView = view.findViewById(R.id.food_description)
        val foodIngredients: TextView = view.findViewById(R.id.food_ingredients)
        val foodQuantity: TextView = view.findViewById(R.id.food_quantity)
        val foodPrice: TextView = view.findViewById(R.id.food_price)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderDetailsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.delivery_item, parent, false)
        return OrderDetailsViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderDetailsViewHolder, position: Int) {
        val item = orderDetailsList[position]

        Log.d("Ingredients", "Item: ${item.ingredients}")
        if (item.foodId != 0) {
            val dbHelper = DatabaseHelper(holder.itemView.context)
            val foodData = dbHelper.getFoodById(item.foodId)

            holder.foodName.text = foodData.name_en
            holder.foodQuantity.text = "x${item.quantity}"
            holder.foodPrice.text = DecimalFormat("#,###").format(item.price) + " VND"

            if (item.size == "" && item.crust == "" && item.crustBase == "") {
                holder.foodDescription.text = "Size: Standard"
                holder.foodIngredients.text = "Not specified"
            } else if (item.crustBase == "") {
                holder.foodDescription.text = "Size: ${item.size} | Crust: ${item.crust}"
                holder.foodIngredients.text = "${item.ingredients}"
            } else {
                holder.foodDescription.text = "Size: ${item.size} | Crust: ${item.crust} | Base: ${item.crustBase}"
                holder.foodIngredients.text = "${item.ingredients}"
            }

            Glide.with(holder.itemView.context)
                .load(foodData.imgPath)
                .into(holder.foodImage)
        }
        else {
            holder.foodName.text = "Customize pizza"
            holder.foodQuantity.text = "x${item.quantity}"
            holder.foodPrice.text = DecimalFormat("#,###").format(item.price) + " VND"

            if (item.crustBase == "") {
                holder.foodDescription.text = "Size: ${item.size} | Crust: ${item.crust}"
                holder.foodIngredients.text = "${item.ingredients}"
            } else {
                holder.foodDescription.text = "Size: ${item.size} | Crust: ${item.crust} | Base: ${item.crustBase}"
                holder.foodIngredients.text = "${item.ingredients}"
            }

            Glide.with(holder.itemView.context)
                .load(R.drawable.default_customize_pizza)
                .into(holder.foodImage)
        }
//        val item = orderDetailsList[position]
//
//        if (item.food_id != 0) {
//            val dlHelper = DatabaseHelper(holder.itemView.context)
//            val foodInfo = dlHelper.getFoodById(item.food_id)
//
//            holder.name.text = foodInfo.getName("en")
//
//            if (foodInfo.category == "pizza") {
//                holder.ingredients.visibility = View.VISIBLE
//                holder.ingredients.text = item.ingredients.toString()
//            } else {
//                holder.ingredients.visibility = View.GONE
//            }
//        }
//        else if (item.food_id == 0) { // customize pizza
//            holder.name.text = "Customize pizza"
//            holder.ingredients.visibility = View.VISIBLE
//            holder.ingredients.text = item.ingredients.toString()
//        }
//
//        if (item.size == "") {
//            holder.size.visibility = View.GONE
//        } else {
//            holder.size.visibility = View.VISIBLE
//            holder.size.text = "Size: ${item.size}"
//        }
//
//        if (item.crust == "") {
//            holder.crust.visibility = View.GONE
//        } else {
//            holder.crust.visibility = View.VISIBLE
//            holder.crust.text = "Crust: ${item.crust}"
//        }
//
//        if (item.crustBase == "") {
//            holder.crustBase.visibility = View.GONE
//        } else {
//            holder.crustBase.visibility = View.VISIBLE
//            holder.crustBase.text = "Crust base: ${item.crustBase}"
//        }
//
//        val price = item.price * item.quantity
//        val formattedPrice = DecimalFormat("#,###").format(price)
//        holder.price.text = "$formattedPrice VND"
//        holder.quantity.text = "Quantity: ${item.quantity}"
    }

    override fun getItemCount(): Int = orderDetailsList.size


}
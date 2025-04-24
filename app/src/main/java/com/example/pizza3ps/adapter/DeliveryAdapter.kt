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
import com.example.pizza3ps.model.DeliveryData
import java.text.DecimalFormat

class DeliveryAdapter(
    private val context: Context,
    private val items: List<DeliveryData>
) : RecyclerView.Adapter<DeliveryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val foodImage: ImageView = view.findViewById(R.id.food_image)
        val foodName: TextView = view.findViewById(R.id.food_name)
        val foodDescription: TextView = view.findViewById(R.id.food_description)
        val foodIngredients: TextView = view.findViewById(R.id.food_ingredients)
        val foodQuantity: TextView = view.findViewById(R.id.food_quantity)
        val foodPrice: TextView = view.findViewById(R.id.food_price)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.delivery_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        if (item.foodId != 0) {
            val dbHelper = DatabaseHelper(context)
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

            Glide.with(context)
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

            Glide.with(context)
                .load(R.drawable.default_customize_pizza)
                .into(holder.foodImage)
        }
    }

    override fun getItemCount() = items.size
}
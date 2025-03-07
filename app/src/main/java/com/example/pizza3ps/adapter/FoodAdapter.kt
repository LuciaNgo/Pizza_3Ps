
package com.example.pizza3ps.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pizza3ps.R
import com.example.pizza3ps.activity.FoodInfoActivity
import com.example.pizza3ps.model.FoodData

class FoodAdapter(private val foodList: List<FoodData>) :
    RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    class FoodViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.food_name)
        val priceTextView: TextView = view.findViewById(R.id.food_price)
        val foodImageView: ImageView = view.findViewById(R.id.food_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.food_item, parent, false)
        return FoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val food = foodList[position]
        holder.nameTextView.text = food.name
        holder.priceTextView.text = "${food.price} VND"

        // Load ảnh bằng Glide
        Glide.with(holder.itemView.context)
            .load(food.imgPath)
            .into(holder.foodImageView)

        // Thiết lập sự kiện click để mở FoodInfoActivity
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, FoodInfoActivity::class.java).apply {
                putExtra("food_name", food.name)
                putExtra("food_price", food.price)
                putExtra("food_image", food.imgPath)
                putStringArrayListExtra("ingredientList", ArrayList(food.ingredients))
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = foodList.size
}

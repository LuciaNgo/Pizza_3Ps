package com.example.pizza3ps.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.pizza3ps.R
import com.example.pizza3ps.database.DatabaseHelper

class BestSellerAdapter(private var foodList: List<Pair<Int, Int>>)
    : RecyclerView.Adapter<BestSellerAdapter.BestSellerViewHolder>() {

    class BestSellerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.food_name)
        val quantityTextView: TextView = view.findViewById(R.id.food_quantity)
        val foodImageView: ImageView = view.findViewById(R.id.food_image)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BestSellerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.best_seller_item, parent, false)
        return BestSellerViewHolder(view)
    }

    override fun onBindViewHolder(holder: BestSellerViewHolder, position: Int) {
        val food = foodList[position]
        
        val foodId = food.first
        val foodQuantity = food.second

        val dbHelper = DatabaseHelper(holder.itemView.context)
        val foodData = dbHelper.getFoodById(foodId)

        holder.nameTextView.text = foodData.name_en
        holder.quantityTextView.text = "Quantity: ${foodQuantity}"

        // Load ảnh bằng Glide
        Glide.with(holder.itemView.context)
            .load(foodData.imgPath)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(holder.foodImageView)
    }

    override fun getItemCount(): Int {
        return foodList.size
    }
}
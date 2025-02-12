package com.example.pizza3ps.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pizza3ps.R
import com.example.pizza3ps.model.FoodData

class FoodAdapter(private val foodList: List<FoodData>) :
    RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    class FoodViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.food_name)
        val priceTextView: TextView = view.findViewById(R.id.food_price)
        val foodImageView: ImageView = view.findViewById(R.id.food_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.food_item, parent, false)
        return FoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val food = foodList[position]
        holder.nameTextView.text = food.name
        holder.priceTextView.text = "${food.price} VND"

        Glide.with(holder.itemView.context)
            .load(food.imgPath) // Sử dụng đường dẫn ảnh từ Firebase hoặc Imgur/PostImages
            .placeholder(R.drawable.placeholder) // Ảnh mặc định nếu load chậm
            .error(R.drawable.error_image) // Ảnh mặc định nếu lỗi
            .into(holder.foodImageView)
    }

    override fun getItemCount() = foodList.size
}

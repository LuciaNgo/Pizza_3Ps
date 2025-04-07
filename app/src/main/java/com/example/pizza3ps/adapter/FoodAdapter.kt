
package com.example.pizza3ps.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.pizza3ps.R
import com.example.pizza3ps.fragment.FoodInfoFragment
import com.example.pizza3ps.model.FoodData

class FoodAdapter(
    private var foodList: List<FoodData>,
    private val layoutType: LayoutType
) : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    enum class LayoutType {
        DASHBOARD,
        MENU
    }

    class FoodViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.food_name)
        val priceTextView: TextView = view.findViewById(R.id.food_price)
        val foodImageView: ImageView = view.findViewById(R.id.food_image)
        val addToCartButton: Button = view.findViewById(R.id.add_to_cart_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val layoutId = when (layoutType) {
            LayoutType.DASHBOARD -> R.layout.food_item
            LayoutType.MENU -> R.layout.menu_food_item
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return FoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val food = foodList[position]
        holder.nameTextView.text = food.name
        holder.priceTextView.text = "${food.price} VND"

        // Load ảnh bằng Glide
        Glide.with(holder.itemView.context)
            .load(food.imgPath)
            .placeholder(R.drawable.placeholder_image)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(holder.foodImageView)

        // Thiết lập sự kiện click để mở FoodInfoBottomSheet
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context

            // Tạo một đối tượng FoodInfoBottomSheet và truyền dữ liệu vào Bundle
            val foodInfoFragment = FoodInfoFragment().apply {
                arguments = Bundle().apply {
                    putString("food_name", food.name)
                    putString("food_price", food.price)
                    putString("food_category", food.category)
                    putString("food_image", food.imgPath)
                    putStringArrayList("ingredientList", ArrayList(food.ingredients))
                }
            }

            // Hiển thị BottomSheet
            foodInfoFragment.show((context as AppCompatActivity).supportFragmentManager, "FoodInfoBottomSheet")
        }

        holder.addToCartButton.setOnClickListener {
            val context = holder.itemView.context

            // Tạo một đối tượng FoodInfoBottomSheet và truyền dữ liệu vào Bundle
            val foodInfoFragment = FoodInfoFragment().apply {
                arguments = Bundle().apply {
                    putString("food_name", food.name)
                    putString("food_price", food.price)
                    putString("food_category", food.category)
                    putString("food_image", food.imgPath)
                    putStringArrayList("ingredientList", ArrayList(food.ingredients))
                }
            }

            // Hiển thị BottomSheet
            foodInfoFragment.show((context as AppCompatActivity).supportFragmentManager, "FoodInfoBottomSheet")
        }

    }

    fun updateData(newList: List<FoodData>) {
        foodList = newList
        notifyDataSetChanged()
    }


    override fun getItemCount() = foodList.size

}

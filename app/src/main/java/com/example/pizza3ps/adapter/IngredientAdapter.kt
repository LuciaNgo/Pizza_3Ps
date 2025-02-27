package com.example.pizza3ps.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pizza3ps.R
import com.example.pizza3ps.model.IngredientData

class IngredientAdapter(
    private val ingredientList: List<IngredientData>,
    private val onIngredientSelected: (Int) -> Unit
    ): RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder>() {

    private val selectedItems = mutableSetOf<Int>()

    class IngredientViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ingredientIcon: ImageView = view.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.ingredient_item, parent, false)
        return IngredientViewHolder(view)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        val ingredient = ingredientList[position]
        Glide.with(holder.itemView.context)
            .load(ingredient.iconImgPath)
            .into(holder.ingredientIcon)

        val frameLayout = holder.itemView as FrameLayout

        val backgroundDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 30f // Bo góc

            // Đổi màu nền khi chọn
            if (selectedItems.contains(position)) {
                setColor(Color.LTGRAY)
            } else {
                setColor(Color.TRANSPARENT)
            }
        }

        // Áp dụng background
        frameLayout.background = backgroundDrawable

        // Xử lý sự kiện khi người dùng nhấn vào item
        holder.itemView.setOnClickListener {
            if (selectedItems.contains(position)) {
                selectedItems.remove(position) // Bỏ chọn
                onIngredientSelected(-ingredient.price.toInt()) // Giảm giá tiền
            } else {
                selectedItems.add(position) // Chọn
                onIngredientSelected(ingredient.price.toInt()) // Cộng giá tiền
            }
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int = ingredientList.size
}
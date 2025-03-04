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
    private val ingredientList: List<IngredientData>
) : RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder>() {

    private val selectedIngredients = mutableSetOf<String>()

    class IngredientViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ingredientIcon: ImageView = view.findViewById(R.id.imageView)
        val frameLayout: FrameLayout = view as FrameLayout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.ingredient_item, parent, false)
        return IngredientViewHolder(view)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        val ingredient = ingredientList[position]

        // Load ảnh vào ImageView
        Glide.with(holder.itemView.context)
            .load(ingredient.iconImgPath)
            .into(holder.ingredientIcon)

        // Cập nhật màu nền dựa vào trạng thái đã chọn
        val backgroundDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 30f
            setColor(if (selectedIngredients.contains(ingredient.name)) Color.LTGRAY else Color.TRANSPARENT)
        }
        holder.frameLayout.background = backgroundDrawable
    }

    override fun getItemCount(): Int = ingredientList.size

    // Đánh dấu ingredient đã chọn và cập nhật UI
    fun setSelectedIngredients(selectedList: List<String>) {
        selectedIngredients.clear()
        selectedIngredients.addAll(selectedList)
        notifyDataSetChanged()
    }
}

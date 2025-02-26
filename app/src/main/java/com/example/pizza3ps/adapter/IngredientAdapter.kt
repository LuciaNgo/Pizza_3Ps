package com.example.pizza3ps.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pizza3ps.R
import com.example.pizza3ps.model.IngredientData

class IngredientAdapter(private val ingredientList: List<IngredientData>):
    RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder>() {

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
            .placeholder(R.drawable.firee) // Ảnh tạm khi load
            .error(R.drawable.menu) // Ảnh lỗi khi load thất bại
            .into(holder.ingredientIcon)
    }

    override fun getItemCount(): Int = ingredientList.size
}
package com.example.pizza3ps.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.pizza3ps.R
import com.example.pizza3ps.fragment.FoodInfoFragment
import com.example.pizza3ps.model.CartData
import java.text.DecimalFormat

class CartAdapter(private val cartItems: List<CartData>) :
    RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.food_name)
        val size: TextView = view.findViewById(R.id.food_size)
        val crust: TextView = view.findViewById(R.id.food_crust)
        val crustBase: TextView = view.findViewById(R.id.food_crust_base)
        val ingredients: TextView = view.findViewById(R.id.food_ingredients)
        val price: TextView = view.findViewById(R.id.food_price)
        val plus : ImageButton = view.findViewById(R.id.plus)
        val minus : ImageButton = view.findViewById(R.id.minus)
        val quantity: TextView = view.findViewById(R.id.quantity_text)
        val image: ImageView = view.findViewById(R.id.food_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cart_item, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = cartItems[position]
        holder.name.text = item.name

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
            holder.crustBase.text = "Crust Base: ${item.crustBase}"
        }

        if (item.category == "pizza") {
            holder.ingredients.visibility = View.VISIBLE
            holder.ingredients.text = "Ingredients: ${item.ingredients?.joinToString(", ")}"
        }
        else {
            holder.ingredients.visibility = View.GONE
        }

        val price = item.price * item.quantity
        val formattedPrice = DecimalFormat("#,###").format(price)
        holder.price.text = "$formattedPrice VND"
        holder.quantity.text = item.quantity.toString()

        // Load ảnh bằng Glide
        Glide.with(holder.itemView.context)
            .load(item.imgPath)
            .placeholder(R.drawable.placeholder_image)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(holder.image)

        // Thiết lập sự kiện click để mở FoodInfoActivity
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context

            // Tạo một đối tượng FoodInfoBottomSheet và truyền dữ liệu vào Bundle
            val foodInfoFragment = FoodInfoFragment().apply {
                arguments = Bundle().apply {
                    putString("food_name", item.name)
                    putInt("food_price", item.price)
                    putString("food_category", item.category)
                    putString("food_image", item.imgPath)
                    putStringArrayList("ingredientList", ArrayList(item.ingredients))
                }
            }

            // Hiển thị BottomSheet
            foodInfoFragment.show((context as AppCompatActivity).supportFragmentManager, "FoodInfoBottomSheet")
        }
    }

    override fun getItemCount(): Int = cartItems.size
}
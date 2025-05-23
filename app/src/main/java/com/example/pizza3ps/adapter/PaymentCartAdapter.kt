package com.example.pizza3ps.adapter

import android.annotation.SuppressLint
import android.util.Log
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
import com.example.pizza3ps.activity.PaymentActivity
import com.example.pizza3ps.database.DatabaseHelper
import com.example.pizza3ps.fragment.PaymentFragment
import com.example.pizza3ps.model.CartData
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DecimalFormat

class PaymentCartAdapter(
    private val fragment: PaymentFragment,
    private val cartItems: List<CartData>) :
    RecyclerView.Adapter<PaymentCartAdapter.PaymentCartViewHolder>() {

    class PaymentCartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
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
        val delete: ImageView = view.findViewById(R.id.delete_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentCartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.payment_cart_item, parent, false)
        return PaymentCartViewHolder(view)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: PaymentCartViewHolder, position: Int) {
        val item = cartItems[position]
        var quantity = item.quantity

        if (item.food_id != 0) {
            val dlHelper = DatabaseHelper(holder.itemView.context)
            val foodInfo = dlHelper.getFoodById(item.food_id)

            holder.name.text = foodInfo.getName("en")

            Glide.with(holder.itemView.context)
                .load(foodInfo.imgPath)
                .into(holder.image)

            if (foodInfo.category == "pizza") {
                holder.ingredients.visibility = View.VISIBLE
                holder.ingredients.text = "${item.ingredients?.joinToString(", ")}".replaceFirstChar { it.uppercase() }
            } else {
                holder.ingredients.visibility = View.GONE
            }
        }
        else if (item.food_id == 0) { // customize pizza
            holder.name.text = "Customize pizza"

            Glide.with(holder.itemView.context)
                .load(R.drawable.default_customize_pizza)
                .into(holder.image)

            holder.ingredients.visibility = View.VISIBLE
            holder.ingredients.text = "${item.ingredients?.joinToString(", ")}".replaceFirstChar { it.uppercase() }
        }

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
            holder.crustBase.text = "Crust base: ${item.crustBase}"
        }

        val price = item.price * item.quantity
        val formattedPrice = DecimalFormat("#,###").format(price)
        holder.price.text = "$formattedPrice VND"
        holder.quantity.text = item.quantity.toString()

        /*
        // Thiết lập sự kiện click để mở FoodInfoActivity
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context

            // Tạo một đối tượng FoodInfoBottomSheet và truyền dữ liệu vào Bundle
            val foodInfoFragment = FoodInfoFragment().apply {
                arguments = Bundle().apply {
                    putInt("food_id", item.food_id)
                    putInt("food_price", item.price)
                    putString("size", item.size)
                    putString("crust", item.crust)
                    putString("crustBase", item.crustBase)
                    putInt("quantity", item.quantity)
                    putStringArrayList("ingredientList", ArrayList(item.ingredients))
                }
            }

            // Hiển thị BottomSheet
//            foodInfoFragment.show((context as AppCompatActivity).supportFragmentManager, "FoodInfoBottomSheet")
            foodInfoFragment.show(activity.supportFragmentManager, "FoodInfoBottomSheet")

        }

         */

        holder.minus.setOnClickListener {
            if (quantity > 1) {
                quantity--
                val dbHelper = DatabaseHelper(holder.itemView.context)
                val id = dbHelper.getIdOfCartItem(item)
                dbHelper.updateCartItemQuantity(id, quantity)
                syncToFirebase(dbHelper, id)

                holder.quantity.text = quantity.toString()
                val newPrice = item.price * quantity
                val formattedPrice = DecimalFormat("#,###").format(newPrice)
                holder.price.text = "$formattedPrice VND"

                updatePrice()
            }
        }

        holder.plus.setOnClickListener {
            quantity++
            val dbHelper = DatabaseHelper(holder.itemView.context)
            val id = dbHelper.getIdOfCartItem(item)
            dbHelper.updateCartItemQuantity(id, quantity)
            syncToFirebase(dbHelper, id)

            holder.quantity.text = quantity.toString()
            val newPrice = item.price * quantity
            val formattedPrice = DecimalFormat("#,###").format(newPrice)
            holder.price.text = "$formattedPrice VND"

            updatePrice()
        }

        holder.delete.setOnClickListener {
            val dbHelper = DatabaseHelper(holder.itemView.context)
            val id = dbHelper.getIdOfCartItem(item)

            if (id != -1) {
                dbHelper.deleteCartItem(id)
                removeSyncToFirebase(dbHelper, id)

                // Tạo danh sách mới loại bỏ item vừa xoá
                val updatedList = cartItems.filterIndexed { index, cartItem ->
                    index != holder.adapterPosition
                }

                // Cập nhật lại adapter
                updateCart(updatedList)
            }
        }
    }

    fun updateCart(newList: List<CartData>) {
        (cartItems as? MutableList<CartData>)?.clear()
        (cartItems as? MutableList<CartData>)?.addAll(newList)
        notifyDataSetChanged()
        updatePrice()
    }

    fun updatePrice() {
        fragment.updateTotalPrice()
    }

    fun syncToFirebase(dbHelper: DatabaseHelper, cartId: Int) {
        val userId = dbHelper.getUser()!!.id
        val cartItem = dbHelper.getCartById(cartId)

        val databaseRef = FirebaseFirestore.getInstance()
            .collection("Cart")
            .document(userId)
            .collection("items")

        if (cartItem != null) {
            databaseRef.document(cartId.toString())
                .set(cartItem)
                .addOnSuccessListener {
                    Log.d("FirebaseSync", "Synced item: $cartId")
                }
                .addOnFailureListener { e ->
                    Log.e("FirebaseSync", "Failed to sync item: $cartId", e)
                }
        }
    }

    fun removeSyncToFirebase(dbHelper: DatabaseHelper, cartId: Int) {
        val userId = dbHelper.getUser()!!.id

        FirebaseFirestore.getInstance()
            .collection("Cart")
            .document(userId)
            .collection("items")
            .document(cartId.toString())
            .delete()
            .addOnSuccessListener {
                Log.d("FirebaseSync", "Synced item: $cartId")
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseSync", "Failed to sync item: $cartId", e)
            }
    }

    override fun getItemCount(): Int = cartItems.size
}
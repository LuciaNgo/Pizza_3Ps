package com.example.pizza3ps.activity

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andremion.counterfab.CounterFab
import com.bumptech.glide.Glide
import com.example.pizza3ps.R
import com.example.pizza3ps.adapter.IngredientAdapter
import com.example.pizza3ps.database.DatabaseHelper
import com.example.pizza3ps.model.CartData
import com.example.pizza3ps.model.IngredientData
import com.example.pizza3ps.tool.LanguageHelper
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DecimalFormat

class CustomizePizzaActivity : AppCompatActivity() {
    private lateinit var recyclerViewMeat: RecyclerView
    private lateinit var recyclerViewSeafood: RecyclerView
    private lateinit var recyclerViewVegetable: RecyclerView
    private lateinit var recyclerViewAddition: RecyclerView
    private lateinit var recyclerViewSauce: RecyclerView

    private lateinit var meatAdapter: IngredientAdapter
    private lateinit var seafoodAdapter: IngredientAdapter
    private lateinit var vegetableAdapter: IngredientAdapter
    private lateinit var additionAdapter: IngredientAdapter
    private lateinit var sauceAdapter: IngredientAdapter

    private val meatList = mutableListOf<IngredientData>()
    private val seafoodList = mutableListOf<IngredientData>()
    private val vegetableList = mutableListOf<IngredientData>()
    private val additionList = mutableListOf<IngredientData>()
    private val sauceList = mutableListOf<IngredientData>()

    private lateinit var addToCartButton: Button
    private lateinit var pizzaImageView: ImageView
    private lateinit var quantityTextView: TextView
    private lateinit var minusCardView: CardView
    private lateinit var plusCardView: CardView
    private lateinit var sizeSRadioButton: RadioButton
    private lateinit var sizeMRadioButton: RadioButton
    private lateinit var sizeLRadioButton: RadioButton
    private lateinit var crustThinButton: RadioButton
    private lateinit var crustThickButton: RadioButton
    private lateinit var crustCheeseCheckBox: CheckBox
    private lateinit var crustChickenCheckBox: CheckBox
    private lateinit var crustSausageCheckBox: CheckBox

    private var basePrice = 50000
    private var totalPrice = basePrice
    private var selectedSize : String = "S"
    private var selectedCrust : String = "Thin"
    private var selectedCrustBase : String = ""
    private var selectedIngredients: List<String> = emptyList()
    private var quantity = 1

    private lateinit var layerContainer: FrameLayout
    private lateinit var backButton: ImageView
    private val ingredientImageViews = mutableMapOf<String, ImageView>()

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_customize_pizza)

        dbHelper = DatabaseHelper(this)

        recyclerViewMeat = findViewById(R.id.meat_recycler_view)!!
        recyclerViewSeafood = findViewById(R.id.seafood_recycler_view)!!
        recyclerViewVegetable = findViewById(R.id.vegetable_recycler_view)!!
        recyclerViewAddition = findViewById(R.id.addition_recycler_view)!!
        recyclerViewSauce = findViewById(R.id.sauce_recycler_view)!!

        recyclerViewMeat.layoutManager = GridLayoutManager(this, 5)
        recyclerViewSeafood.layoutManager = GridLayoutManager(this, 5)
        recyclerViewVegetable.layoutManager = GridLayoutManager(this, 5)
        recyclerViewAddition.layoutManager = GridLayoutManager(this, 5)
        recyclerViewSauce.layoutManager = GridLayoutManager(this, 5)

        meatAdapter = IngredientAdapter(meatList, ::handleIngredientClick)
        seafoodAdapter = IngredientAdapter(seafoodList, ::handleIngredientClick)
        vegetableAdapter = IngredientAdapter(vegetableList, ::handleIngredientClick)
        additionAdapter = IngredientAdapter(additionList, ::handleIngredientClick)
        sauceAdapter = IngredientAdapter(sauceList, ::handleIngredientClick)

        recyclerViewMeat.adapter = meatAdapter
        recyclerViewSeafood.adapter = seafoodAdapter
        recyclerViewVegetable.adapter = vegetableAdapter
        recyclerViewAddition.adapter = additionAdapter
        recyclerViewSauce.adapter = sauceAdapter

        addToCartButton = findViewById(R.id.add_to_cart_button)
        pizzaImageView = findViewById(R.id.pizza_image)
        quantityTextView = findViewById(R.id.quantity_text)
        minusCardView = findViewById(R.id.minus)
        plusCardView = findViewById(R.id.plus)
        sizeSRadioButton = findViewById(R.id.size_s)
        sizeMRadioButton = findViewById(R.id.size_m)
        sizeLRadioButton = findViewById(R.id.size_l)
        crustThinButton = findViewById(R.id.crust_thin)
        crustThickButton = findViewById(R.id.crust_thick)
        crustCheeseCheckBox = findViewById(R.id.crust_cheese)
        crustChickenCheckBox = findViewById(R.id.crust_chicken)
        crustSausageCheckBox = findViewById(R.id.crust_sausage)
        layerContainer = findViewById(R.id.layerContainer)
        backButton = findViewById(R.id.backButton)

        sizeSRadioButton.isChecked = true
        crustThinButton.isChecked = true
        quantityTextView.text = quantity.toString()

        backButton.setOnClickListener {
            onBackPressed()
        }

        // Plus
        plusCardView.setOnClickListener {
            quantity++
            quantityTextView.text = quantity.toString()
            updatePrice()
        }

        // Minus
        minusCardView.setOnClickListener {
            if (quantity > 1) {
                quantity--
                quantityTextView.text = quantity.toString()
                updatePrice()
            }
        }

        // Size
        sizeSRadioButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                basePrice += 40000
                selectedSize = "S"
            }
            else {
                basePrice -= 40000
            }
            updatePrice()
        }

        sizeMRadioButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                basePrice += 70000
                selectedSize = "M"
            } else {
                basePrice -= 70000
            }
            updatePrice()
        }

        sizeLRadioButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                basePrice += 90000
                selectedSize = "L"
            } else {
                basePrice -= 90000
            }
            updatePrice()
        }

        // Crust thickness
        crustThinButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                basePrice += 10000
                selectedCrust = "Thin"
            } else {
                basePrice -= 10000
            }
            updatePrice()
        }

        crustThickButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                basePrice += 20000
                selectedCrust = "Thick"
            } else {
                basePrice -= 20000
            }
            updatePrice()
        }

        // Crust base ingredients
        crustCheeseCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                crustChickenCheckBox.isChecked = false
                crustSausageCheckBox.isChecked = false
                selectedCrustBase = "Cheese"
                basePrice += 40000
            } else basePrice -= 40000
            updatePrice()
        }

        crustChickenCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                crustCheeseCheckBox.isChecked = false
                crustSausageCheckBox.isChecked = false
                selectedCrustBase = "Chicken"
                basePrice += 40000
            } else basePrice -= 40000
            updatePrice()
        }

        crustSausageCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                crustCheeseCheckBox.isChecked = false
                crustChickenCheckBox.isChecked = false
                selectedCrustBase = "Sausage"
                basePrice += 30000
            } else basePrice -= 30000
            updatePrice()
        }

        updatePrice()
        fetchIngredientData()

        addToCartButton.setOnClickListener {
            selectedIngredients = (meatAdapter.getSelectedIngredients() +
                    seafoodAdapter.getSelectedIngredients() +
                    vegetableAdapter.getSelectedIngredients() +
                    additionAdapter.getSelectedIngredients() +
                    sauceAdapter.getSelectedIngredients()).distinct()

            val cartData = CartData(
                food_id = 0,
                price = basePrice,
                ingredients = selectedIngredients,
                size = selectedSize,
                crust = selectedCrust,
                crustBase = selectedCrustBase,
                quantity = quantity
            )

            dbHelper.addFoodToCart(cartData)

            val userId = dbHelper.getUser()?.id
            if (userId != null) {
                syncCartItem(userId, cartData)
            }

            Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show()

            // Reset cac lua chon
            selectedSize = "S"
            selectedCrust = "Thin"
            selectedCrustBase = ""
            selectedIngredients = emptyList()
            quantity = 1
            quantityTextView.text = quantity.toString()
            sizeSRadioButton.isChecked = true
            crustThinButton.isChecked = true
            crustCheeseCheckBox.isChecked = false
            crustChickenCheckBox.isChecked = false
            crustSausageCheckBox.isChecked = false
            meatAdapter.clearSelectedIngredients()
            seafoodAdapter.clearSelectedIngredients()
            vegetableAdapter.clearSelectedIngredients()
            additionAdapter.clearSelectedIngredients()
            sauceAdapter.clearSelectedIngredients()
//            layerContainer.removeAllViews()

            var i = 0
            while (i < layerContainer.childCount) {
                val view = layerContainer.getChildAt(i)
                if (view != pizzaImageView) {
                    layerContainer.removeViewAt(i)
                } else {
                    i++
                }
            }

            ingredientImageViews.clear()
            basePrice = 50000
            updatePrice()
        }
    }

    private fun fetchIngredientData() {
        val ingredientList = dbHelper.getAllIngredients()

        meatList.clear()
        seafoodList.clear()
        vegetableList.clear()
        additionList.clear()
        sauceList.clear()

        for (ingredient in ingredientList) {
            if (ingredient.category == "meat") {
                meatList.add(ingredient)
            } else if (ingredient.category == "seafood") {
                seafoodList.add(ingredient)
            } else if (ingredient.category == "vegetable") {
                vegetableList.add(ingredient)
            } else if (ingredient.category == "addition") {
                additionList.add(ingredient)
            } else if (ingredient.category == "sauce") {
                sauceList.add(ingredient)
            }
        }

        meatAdapter.notifyDataSetChanged()
        seafoodAdapter.notifyDataSetChanged()
        vegetableAdapter.notifyDataSetChanged()
        additionAdapter.notifyDataSetChanged()
        sauceAdapter.notifyDataSetChanged()
    }

    private fun handleIngredientClick(ingredient: IngredientData, isSelected: Boolean) {
        basePrice += if (isSelected) ingredient.price.toInt() else -ingredient.price.toInt()
        updatePrice()

        if (isSelected) {
            basePrice += ingredient.price.toInt()
            updatePrice()

            val imageView = ImageView(this)
            Glide.with(this)
                .load(ingredient.layerImgPath)
                .into(imageView)

            layerContainer.addView(imageView)
            ingredientImageViews[ingredient.name] = imageView
        } else {
            basePrice -= ingredient.price.toInt()
            updatePrice()

            ingredientImageViews[ingredient.name]?.let { layerContainer.removeView(it) }
            ingredientImageViews.remove(ingredient.name)
        }
    }

    private fun updatePrice() {
        totalPrice = basePrice * quantity
        val formatter = DecimalFormat("#,###")
        val formattedPrice = formatter.format(totalPrice)
        addToCartButton.text = "Add to Cart - $formattedPrice"
    }

    fun syncCartItem(userId: String, cartItem: CartData) {
        val databaseRef = FirebaseFirestore.getInstance()
            .collection("Cart")
            .document(userId)
            .collection("items")

        val id = dbHelper.getIdOfCartItem(cartItem)

        databaseRef.document(id.toString())
            .set(cartItem)
            .addOnSuccessListener {
                Log.d("FirebaseSync", "Synced item: $id")
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseSync", "Failed to sync item: $id", e)
            }
    }

}
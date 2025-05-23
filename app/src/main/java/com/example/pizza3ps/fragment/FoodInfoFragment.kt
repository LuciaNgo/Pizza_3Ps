package com.example.pizza3ps.fragment

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
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
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DecimalFormat

class FoodInfoFragment : BottomSheetDialogFragment() {
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

    private lateinit var foodOriginalPriceTextView: TextView
    private lateinit var sizeTextView: TextView
    private lateinit var crustThicknessTextView: TextView
    private lateinit var crustTypeTextView: TextView
    private lateinit var sauceTextView: TextView
    private lateinit var meatTextView: TextView
    private lateinit var seafoodTextView: TextView
    private lateinit var vegetableTextView: TextView
    private lateinit var additionTextView: TextView
    private lateinit var pizzaNameTextView: TextView
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

    private lateinit var addToCartButton: Button

    private lateinit var dbHelper: DatabaseHelper

    private lateinit var ingredientList: List<String>
    private var basePrice = 50000
    private var totalPrice = basePrice
    private var selectedSize : String = "S"
    private var selectedCrust : String = "Thin"
    private var selectedCrustBase : String = ""
    private var selectedIngredients: List<String> = emptyList()
    private var quantity = 1

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            val behavior = BottomSheetBehavior.from(bottomSheet!!)

            // Set the state to expanded
            behavior.state = BottomSheetBehavior.STATE_EXPANDED

            // Disable dragging
            behavior.isDraggable = false

            // Adjust height to match parent
            bottomSheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_food_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val name = arguments?.getString("food_name") ?: ""
        val price = arguments?.getString("food_price") ?.replace(",", "")?.toIntOrNull() ?: 0
        val category = arguments?.getString("food_category") ?: ""
        val imgPath = arguments?.getString("food_image") ?: ""
        ingredientList = arguments?.getStringArrayList("ingredientList") ?: arrayListOf()

        dbHelper = DatabaseHelper(requireContext())

        foodOriginalPriceTextView = view.findViewById(R.id.original_price)
        sizeTextView = view.findViewById(R.id.size_textview)
        crustThicknessTextView = view.findViewById(R.id.crust_thickness_textview)
        crustTypeTextView = view.findViewById(R.id.crust_base_ingredient_textview)
        sauceTextView = view.findViewById(R.id.sauce_textview)
        meatTextView = view.findViewById(R.id.meat_textview)
        seafoodTextView = view.findViewById(R.id.seafood_textview)
        vegetableTextView = view.findViewById(R.id.vegetable_textview)
        additionTextView = view.findViewById(R.id.addition_textview)
        pizzaNameTextView = view.findViewById(R.id.pizza_name)
        pizzaImageView = view.findViewById(R.id.pizza_image)
        quantityTextView = view.findViewById(R.id.quantity_text)
        minusCardView = view.findViewById(R.id.minus)
        plusCardView = view.findViewById(R.id.plus)
        sizeSRadioButton = view.findViewById(R.id.size_s)
        sizeMRadioButton = view.findViewById(R.id.size_m)
        sizeLRadioButton = view.findViewById(R.id.size_l)
        crustThinButton = view.findViewById(R.id.crust_thin)
        crustThickButton = view.findViewById(R.id.crust_thick)
        crustCheeseCheckBox = view.findViewById(R.id.crust_cheese)
        crustChickenCheckBox = view.findViewById(R.id.crust_chicken)
        crustSausageCheckBox = view.findViewById(R.id.crust_sausage)
        addToCartButton = view.findViewById(R.id.add_to_cart_button)

        pizzaNameTextView.text = name
        val formattedPrice = DecimalFormat("#,###").format(price)
        foodOriginalPriceTextView.text = "$formattedPrice VND"
        sizeSRadioButton.isChecked = true
        crustThinButton.isChecked = true
        quantityTextView.text = quantity.toString()

        // Kiểm tra nếu category không phải pizza thì ẩn các thành phần liên quan
        if (category.lowercase() != "pizza") {
            sizeTextView.visibility = TextView.GONE
            crustThicknessTextView.visibility = TextView.GONE
            crustTypeTextView.visibility = TextView.GONE
            sauceTextView.visibility = TextView.GONE
            meatTextView.visibility = TextView.GONE
            seafoodTextView.visibility = TextView.GONE
            vegetableTextView.visibility = TextView.GONE
            additionTextView.visibility = TextView.GONE
            sizeSRadioButton.visibility = Button.GONE
            sizeMRadioButton.visibility = Button.GONE
            sizeLRadioButton.visibility = Button.GONE
            crustThinButton.visibility = Button.GONE
            crustThickButton.visibility = Button.GONE
            crustCheeseCheckBox.visibility = Button.GONE
            crustChickenCheckBox.visibility = Button.GONE
            crustSausageCheckBox.visibility = Button.GONE
        }

        Glide.with(this)
            .load(imgPath)
            .into(pizzaImageView)

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

        if (category == "pizza") {
            // Size
            sizeSRadioButton.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    basePrice += 40000
                    selectedSize = "S"
                } else {
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

            crustThinButton.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    basePrice += 10000
                    selectedCrust = "Thin"
                } else basePrice -= 10000
                updatePrice()
            }

            crustThickButton.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    basePrice += 20000
                    selectedCrust = "Thick"
                } else basePrice -= 20000
                updatePrice()
            }

            crustCheeseCheckBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    crustChickenCheckBox.isChecked = false
                    crustSausageCheckBox.isChecked = false
                    basePrice += 40000
                    selectedCrustBase = "Cheese"
                } else basePrice -= 40000
                updatePrice()
            }

            crustChickenCheckBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    crustCheeseCheckBox.isChecked = false
                    crustSausageCheckBox.isChecked = false
                    basePrice += 40000
                    selectedCrustBase = "Chicken"
                } else basePrice -= 40000
                updatePrice()
            }

            crustSausageCheckBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    crustCheeseCheckBox.isChecked = false
                    crustChickenCheckBox.isChecked = false
                    basePrice += 30000
                    selectedCrustBase = "Sausage"
                } else basePrice -= 30000
                updatePrice()
            }

            fetchIngredientData()
            setupRecyclerViews(view)
            setupAdapters()
            selectPreChosenIngredients()
        }

        if (category != "pizza") {
            basePrice = price
        }

        updatePrice()

        // Bấm vào addToCartButton thì thêm thông tin vào giỏ hàng
        addToCartButton.setOnClickListener {
            val foodId = dbHelper.getFoodId(name)

            if (category == "pizza") {
                selectedIngredients = (meatAdapter.getSelectedIngredients() +
                        seafoodAdapter.getSelectedIngredients() +
                        vegetableAdapter.getSelectedIngredients() +
                        additionAdapter.getSelectedIngredients() +
                        sauceAdapter.getSelectedIngredients()).distinct()

                val cartData = CartData(
                    food_id = foodId,
                    price = basePrice,
                    ingredients = selectedIngredients,
                    size = selectedSize,
                    crust = selectedCrust,
                    crustBase = selectedCrustBase,
                    quantity = quantity
                )
                Log.d("Cart data", "Adding to cart: $cartData")
                dbHelper.addFoodToCart(cartData)

                val userId = dbHelper.getUser()?.id
                if (userId != null) {
                    syncCartItem(userId, cartData)
                }

            } else {
                val cartData = CartData(
                    food_id = foodId,
                    price = basePrice,
                    ingredients = ingredientList,
                    size = "",
                    crust = "",
                    crustBase = "",
                    quantity = quantity
                )
                Log.d("Cart data", "Adding to cart: $cartData")
                dbHelper.addFoodToCart(cartData)

                val userId = dbHelper.getUser()?.id
                if (userId != null) {
                    syncCartItem(userId, cartData)
                }
            }

            // Cập nhật lại số lượng món ăn trong giỏ hàng
            val cartFab : CounterFab = requireActivity().findViewById(R.id.cart_fab)
            val cartItemCount = dbHelper.getCartItemCount()
            cartFab.count = cartItemCount

            dismiss()
        }
    }

    private fun setupRecyclerViews(view: View) {
        recyclerViewMeat = view.findViewById(R.id.meat_recycler_view)
        recyclerViewSeafood = view.findViewById(R.id.seafood_recycler_view)
        recyclerViewVegetable = view.findViewById(R.id.vegetable_recycler_view)
        recyclerViewAddition = view.findViewById(R.id.addition_recycler_view)
        recyclerViewSauce = view.findViewById(R.id.sauce_recycler_view)

        recyclerViewMeat.layoutManager = GridLayoutManager(requireContext(), 5)
        recyclerViewSeafood.layoutManager = GridLayoutManager(requireContext(), 5)
        recyclerViewVegetable.layoutManager = GridLayoutManager(requireContext(), 5)
        recyclerViewAddition.layoutManager = GridLayoutManager(requireContext(), 5)
        recyclerViewSauce.layoutManager = GridLayoutManager(requireContext(), 5)
    }

    private fun setupAdapters() {
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
    }

    private fun selectPreChosenIngredients() {
        meatAdapter.setSelectedIngredients(ingredientList)
        seafoodAdapter.setSelectedIngredients(ingredientList)
        vegetableAdapter.setSelectedIngredients(ingredientList)
        additionAdapter.setSelectedIngredients(ingredientList)
        sauceAdapter.setSelectedIngredients(ingredientList)

        // Cập nhật tổng giá tiền khi load dữ liệu
        for (ingredient in meatList + seafoodList + vegetableList + additionList + sauceList) {
            if (ingredientList.contains(ingredient.name)) {
                basePrice += ingredient.price.toInt()
            }
        }
        updatePrice()
    }

    private fun fetchIngredientData() {
        val ingredientData = dbHelper.getAllIngredients()

        meatList.clear()
        seafoodList.clear()
        vegetableList.clear()
        additionList.clear()
        sauceList.clear()

        for (ingredient in ingredientData) {
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
    }

    private fun handleIngredientClick(ingredient: IngredientData, isSelected: Boolean) {
        basePrice += if (isSelected) ingredient.price.toInt() else -ingredient.price.toInt()
        updatePrice()
    }

    private fun updatePrice() {
        totalPrice = basePrice * quantity
        val formattedPrice = DecimalFormat("#,###").format(totalPrice)
        addToCartButton.text = "Add to cart - $formattedPrice VND"
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

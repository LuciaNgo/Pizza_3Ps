package com.example.pizza3ps.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.pizza3ps.R
import com.example.pizza3ps.model.IngredientData
import android.widget.Button
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andremion.counterfab.CounterFab
import com.example.pizza3ps.adapter.IngredientAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DecimalFormat

class CustomizeFragment : Fragment() {
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

    private val db = FirebaseFirestore.getInstance()
    private var quantity = 1
    private var basePrice = 50000
    private var totalPrice = basePrice

    private lateinit var layerContainer: FrameLayout
    private lateinit var fab: CounterFab
    private val ingredientImageViews = mutableMapOf<String, ImageView>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_customize, container, false)

        fab = requireActivity().findViewById(R.id.cart_fab)
        fab.visibility = View.GONE

        addToCartButton = view.findViewById(R.id.add_to_cart_button)
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
        layerContainer = view.findViewById(R.id.layerContainer)

        sizeSRadioButton.isChecked = true
        crustThinButton.isChecked = true
        quantityTextView.text = quantity.toString()

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
        sizeSRadioButton.setOnCheckedChangeListener { _, isChecked -> if (isChecked) basePrice += 40000 else basePrice -= 40000; updatePrice() }
        sizeMRadioButton.setOnCheckedChangeListener { _, isChecked -> if (isChecked) basePrice += 70000 else basePrice -= 70000; updatePrice() }
        sizeLRadioButton.setOnCheckedChangeListener { _, isChecked -> if (isChecked) basePrice += 90000 else basePrice -= 90000; updatePrice() }

        // Crust thickness
        crustThinButton.setOnCheckedChangeListener { _, isChecked -> if (isChecked) basePrice += 10000 else basePrice -= 10000; updatePrice() }
        crustThickButton.setOnCheckedChangeListener { _, isChecked -> if (isChecked) basePrice += 20000 else basePrice -= 20000; updatePrice() }

        // Crust base ingredients
        crustCheeseCheckBox.setOnCheckedChangeListener { _, isChecked -> if (isChecked) { crustChickenCheckBox.isChecked = false; crustSausageCheckBox.isChecked = false; basePrice += 40000 } else basePrice -= 40000; updatePrice() }
        crustChickenCheckBox.setOnCheckedChangeListener { _, isChecked -> if (isChecked) { crustCheeseCheckBox.isChecked = false; crustSausageCheckBox.isChecked = false; basePrice += 40000 } else basePrice -= 40000; updatePrice() }
        crustSausageCheckBox.setOnCheckedChangeListener { _, isChecked -> if (isChecked) { crustCheeseCheckBox.isChecked = false; crustChickenCheckBox.isChecked = false; basePrice += 30000 } else basePrice -= 30000; updatePrice() }

        updatePrice()
        fetchIngredientData()

        return view
    }


    private fun setupRecyclerViews() {
        recyclerViewMeat = view?.findViewById(R.id.meat_recycler_view)!!
        recyclerViewSeafood = view?.findViewById(R.id.seafood_recycler_view)!!
        recyclerViewVegetable = view?.findViewById(R.id.vegetable_recycler_view)!!
        recyclerViewAddition = view?.findViewById(R.id.addition_recycler_view)!!
        recyclerViewSauce = view?.findViewById(R.id.sauce_recycler_view)!!


        recyclerViewMeat.layoutManager = GridLayoutManager(requireContext(), 5)
        recyclerViewSeafood.layoutManager = GridLayoutManager(requireContext(), 5)
        recyclerViewVegetable.layoutManager = GridLayoutManager(requireContext(), 5)
        recyclerViewAddition.layoutManager = GridLayoutManager(requireContext(), 5)
        recyclerViewSauce.layoutManager = GridLayoutManager(requireContext(), 5)
    }

    private fun fetchIngredientData() {
        db.collection("Ingredient")
            .get()
            .addOnSuccessListener { documents ->
                meatList.clear()
                seafoodList.clear()
                vegetableList.clear()
                additionList.clear()
                sauceList.clear()

                for (document in documents) {
                    val name = document.getString("name") ?: ""
                    val price = document.getString("price") ?: "0"
                    val iconImgPath = document.getString("iconImgPath") ?: ""
                    val layerImgPath = document.getString("layerImgPath") ?: ""
                    val category = document.getString("category") ?: ""

                    val ingredient = IngredientData(name, price, iconImgPath, layerImgPath)

                    when (category.lowercase()) {
                        "meat" -> meatList.add(ingredient)
                        "seafood" -> seafoodList.add(ingredient)
                        "vegetable" -> vegetableList.add(ingredient)
                        "addition" -> additionList.add(ingredient)
                        "sauce" -> sauceList.add(ingredient)
                    }
                }

                setupAdapters()
            }
    }

    private fun setupAdapters() {
        setupRecyclerViews()

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

    private fun handleIngredientClick(ingredient: IngredientData, isSelected: Boolean) {
        basePrice += if (isSelected) ingredient.price.toInt() else -ingredient.price.toInt()
        updatePrice()

        if (isSelected) {
            basePrice += ingredient.price.toInt()
            updatePrice()
            val imageView = ImageView(requireContext())
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




}
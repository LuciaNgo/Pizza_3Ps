package com.example.pizza3ps.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.andremion.counterfab.CounterFab
import com.example.pizza3ps.R
import com.example.pizza3ps.adapter.FoodAdapter
import com.example.pizza3ps.adapter.IngredientAdapter
import com.example.pizza3ps.database.DatabaseHelper
import com.example.pizza3ps.model.FoodData
import com.example.pizza3ps.model.IngredientData
import com.google.android.material.slider.RangeSlider

class MenuFragment : Fragment() {
    private lateinit var pizzaCategory: LinearLayout
    private lateinit var chickenCategory: LinearLayout
    private lateinit var pastaCategory: LinearLayout
    private lateinit var appetizerCategory: LinearLayout
    private lateinit var drinksCategory: LinearLayout

    private lateinit var foodRecyclerView: RecyclerView
    private lateinit var foodAdapter: FoodAdapter

    private lateinit var ingredientFilterRecyclerView : RecyclerView
    private lateinit var ingredientAdapter : IngredientAdapter

    private lateinit var fab: CounterFab
    private lateinit var searchBar: SearchView
    private lateinit var filterButton: ImageView
    private lateinit var filterLayout: ConstraintLayout
    private lateinit var priceSlider: RangeSlider
    private lateinit var resetButton: ImageView

    private val foodList = mutableListOf<FoodData>()
    private var currentCategory: String = "pizza"
    private var currentMinPrice: Int = 0
    private var currentMaxPrice: Int = 400000
    private var selectedIngredients = mutableSetOf<String>()

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_menu, container, false)

        fab = requireActivity().findViewById(R.id.cart_fab)
        fab.visibility = View.VISIBLE

        pizzaCategory = view.findViewById(R.id.pizza_category)
        chickenCategory = view.findViewById(R.id.chicken_category)
        pastaCategory = view.findViewById(R.id.pasta_category)
        appetizerCategory = view.findViewById(R.id.appetizer_category)
        drinksCategory = view.findViewById(R.id.drinks_category)

        filterButton = view.findViewById(R.id.filter_button)
        filterLayout = view.findViewById(R.id.filter_container)
        priceSlider = view.findViewById(R.id.priceRangeSlider)
        resetButton = view.findViewById(R.id.reset_icon)

        searchBar = view.findViewById(R.id.menu_search_bar)
        searchBar.clearFocus()

        foodRecyclerView = view.findViewById(R.id.food_recyclerView)
        foodRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        ingredientFilterRecyclerView = view.findViewById(R.id.ingredient_filter_recyclerView)
        ingredientFilterRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        filterLayout.visibility = View.GONE

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        val lang = prefs.getString("lang", "en") ?: "en"

        dbHelper = DatabaseHelper(requireContext())

        val ingredientList = dbHelper.getAllIngredients()
        ingredientAdapter = IngredientAdapter(ingredientList, ::filterFoodByIngredient)
        ingredientFilterRecyclerView.adapter = ingredientAdapter

        foodList.addAll(dbHelper.getFoodByCategory("pizza"))
        foodAdapter = FoodAdapter(lang, foodList, FoodAdapter.LayoutType.MENU)
        foodRecyclerView.adapter = foodAdapter

        Log.d("MenuFragment", "Loaded ${foodList.size} pizzas")
        foodAdapter.notifyDataSetChanged()

        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredList = foodList.filter { food ->
                    if (lang == "en") {
                        food.name_en.contains(newText ?: "", ignoreCase = true)
                    } else {
                        food.name_vi.contains(newText ?: "", ignoreCase = true)
                    }
                }
                foodAdapter.updateData(filteredList)
                return true
            }
        })

        pizzaCategory.setOnClickListener {
            changeCategory("pizza")
        }

        chickenCategory.setOnClickListener {
            changeCategory("chicken")
        }

        pastaCategory.setOnClickListener {
            changeCategory("pasta")
        }

        appetizerCategory.setOnClickListener {
            changeCategory("appetizer")
        }

        drinksCategory.setOnClickListener {
            changeCategory("drinks")
        }

        filterButton.setOnClickListener {
            val transition = AutoTransition().apply {
                duration = 300
            }

            TransitionManager.beginDelayedTransition(view as ViewGroup, transition)

            if (filterLayout.visibility == View.GONE) {
                filterLayout.visibility = View.VISIBLE
            } else {
                filterLayout.visibility = View.GONE
            }
        }

        priceSlider.addOnChangeListener { slider, _, _ ->
            val values = slider.values
            currentMinPrice = values[0].toInt()
            currentMaxPrice = values[1].toInt()

            applyFilters()
        }

        resetButton.setOnClickListener {
            priceSlider.values = listOf(0f, 400000f)
            currentMinPrice = 0
            currentMaxPrice = 400000
            selectedIngredients.clear()
            ingredientAdapter.clearSelectedIngredients()
            applyFilters()
        }
    }

    private fun filterFoodByIngredient(ingredient: IngredientData, isSelected: Boolean) {
        if (isSelected) {
            selectedIngredients.add(ingredient.name)
        } else {
            selectedIngredients.remove(ingredient.name)
        }
        applyFilters()
    }

    private fun applyFilters() {
        val dbHelper = DatabaseHelper(requireContext())
        var filteredList = dbHelper.getFoodByCategory(currentCategory)

        // Filter by ingredient
        if (currentCategory == "pizza") {
            if (selectedIngredients.isNotEmpty()) {
                filteredList = filteredList.filter { food ->
                    food.ingredients.any { it in selectedIngredients }
                }
            }
        }

        // Filter by price
        filteredList = filteredList.filter { food ->
            val price = food.price.toString().replace(",", "").toInt()
            price in currentMinPrice..currentMaxPrice
        }

        foodAdapter.updateData(filteredList)
        foodRecyclerView.scrollToPosition(0)
    }

    private fun changeCategory(category: String) {
        if (currentCategory != category) {
            currentCategory = category
            applyFilters()
        }
    }

}
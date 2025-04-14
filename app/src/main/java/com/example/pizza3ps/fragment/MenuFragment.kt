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
import com.example.pizza3ps.tool.LanguageHelper
import com.google.android.material.slider.RangeSlider

class MenuFragment : Fragment() {
    private lateinit var pizzaCategory: LinearLayout
    private lateinit var chickenCategory: LinearLayout
    private lateinit var pastaCategory: LinearLayout
    private lateinit var appetizerCategory: LinearLayout
    private lateinit var drinksCategory: LinearLayout

    private lateinit var foodRecyclerView: RecyclerView
    private val foodList = mutableListOf<FoodData>()
    private lateinit var foodAdapter: FoodAdapter

    private lateinit var fab: CounterFab
    private lateinit var searchBar: SearchView
    private lateinit var filterButton: ImageView
    private lateinit var filterLayout: ConstraintLayout
    private lateinit var priceSlider: RangeSlider
    private lateinit var ingredientFilterRecyclerView : RecyclerView
    private lateinit var ingredientAdapter : IngredientAdapter

    private var currentMinPrice: Int = 0
    private var currentMaxPrice: Int = 400000

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_menu, container, false)
        val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        val lang = prefs.getString("lang", "en") ?: "en"

        fab = requireActivity().findViewById(R.id.cart_fab)
        fab.visibility = View.VISIBLE

        pizzaCategory = view.findViewById(R.id.pizza_category)
        chickenCategory = view.findViewById(R.id.chicken_category)
        pastaCategory = view.findViewById(R.id.pasta_category)
        appetizerCategory = view.findViewById(R.id.appetizer_category)
        drinksCategory = view.findViewById(R.id.drinks_category)
        foodRecyclerView = view.findViewById(R.id.food_recyclerView)
        filterButton = view.findViewById(R.id.filter_button)
        filterLayout = view.findViewById(R.id.filter_container)
        priceSlider = view.findViewById(R.id.priceRangeSlider)
        ingredientFilterRecyclerView = view.findViewById(R.id.ingredient_filter_recyclerView)
        searchBar = view.findViewById(R.id.menu_search_bar)
        searchBar.clearFocus()

        ingredientFilterRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        foodRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        foodAdapter = FoodAdapter(lang, foodList, FoodAdapter.LayoutType.MENU)
        foodRecyclerView.adapter = foodAdapter

        filterLayout.visibility = View.GONE

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        val lang = prefs.getString("lang", "en") ?: "en"

        val dbHelper = DatabaseHelper(requireContext())

        // Load food data category = pizza trong database
        foodList.clear()
        foodList.addAll(dbHelper.getFoodByCategory("pizza"))
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
            val newList = dbHelper.getFoodByCategory("pizza")
            foodList.clear()
            foodList.addAll(newList)
            Log.d("MenuFragment", "Loaded ${newList.size} pizzas")
            foodAdapter.updateData(newList)
            filterProductsByPrice(currentMinPrice, currentMaxPrice)
        }

        chickenCategory.setOnClickListener {
            val newList = dbHelper.getFoodByCategory("chicken")
            foodList.clear()
            foodList.addAll(newList)
            Log.d("MenuFragment", "Loaded ${newList.size} chickens")
            foodAdapter.updateData(newList)
            filterProductsByPrice(currentMinPrice, currentMaxPrice)
        }

        pastaCategory.setOnClickListener {
            val newList = dbHelper.getFoodByCategory("pasta")
            foodList.clear()
            foodList.addAll(newList)
            Log.d("MenuFragment", "Loaded ${newList.size} pastas")
            foodAdapter.updateData(newList)
            filterProductsByPrice(currentMinPrice, currentMaxPrice)
        }

        appetizerCategory.setOnClickListener {
            val newList = dbHelper.getFoodByCategory("appetizer")
            foodList.clear()
            foodList.addAll(newList)
            Log.d("MenuFragment", "Loaded ${newList.size} appetizers")
            foodAdapter.updateData(newList)
            filterProductsByPrice(currentMinPrice, currentMaxPrice)
        }

        drinksCategory.setOnClickListener {
            val newList = dbHelper.getFoodByCategory("drinks")
            foodList.clear()
            foodList.addAll(newList)
            Log.d("MenuFragment", "Loaded ${newList.size} drinks")
            foodAdapter.updateData(newList)
            filterProductsByPrice(currentMinPrice, currentMaxPrice)
        }

        filterButton.setOnClickListener {
            val transition = AutoTransition().apply {
                duration = 300 // thời gian animation (ms)
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

            filterProductsByPrice(currentMinPrice, currentMaxPrice)
        }
    }

    private fun filterProductsByPrice(minPrice: Int, maxPrice: Int) {
        val filteredList = foodList.filter { food ->
            val price = food.price.toString().replace(",", "").toInt()
            price in minPrice..maxPrice
        }

        // Cập nhật danh sách trong RecyclerView
        foodAdapter.updateData(filteredList)
        foodAdapter.notifyDataSetChanged()
        foodRecyclerView.scrollToPosition(0)
    }
}
package com.example.pizza3ps.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pizza3ps.R
import com.example.pizza3ps.adapter.FoodAdapter
import com.example.pizza3ps.database.FoodDatabaseHelper
import com.example.pizza3ps.model.FoodData

class MenuFragment : Fragment() {
    private lateinit var pizzaCategory: LinearLayout
    private lateinit var chickenCategory: LinearLayout
    private lateinit var pastaCategory: LinearLayout
    private lateinit var appetizerCategory: LinearLayout
    private lateinit var drinksCategory: LinearLayout

    private lateinit var foodRecyclerView: RecyclerView
    private val foodList = mutableListOf<FoodData>()
    private lateinit var foodAdapter: FoodAdapter

    private lateinit var searchBar: SearchView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_menu, container, false)

        pizzaCategory = view.findViewById(R.id.pizza_category)
        chickenCategory = view.findViewById(R.id.chicken_category)
        pastaCategory = view.findViewById(R.id.pasta_category)
        appetizerCategory = view.findViewById(R.id.appetizer_category)
        drinksCategory = view.findViewById(R.id.drinks_category)
        foodRecyclerView = view.findViewById(R.id.food_recyclerView)
        searchBar = view.findViewById(R.id.menu_search_bar)
        searchBar.clearFocus()

        foodRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        foodAdapter = FoodAdapter(foodList, FoodAdapter.LayoutType.MENU)
        foodRecyclerView.adapter = foodAdapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load food data category = pizza trong database
        val foodDatabaseHelper = FoodDatabaseHelper(requireContext())
        foodList.clear()
        foodList.addAll(foodDatabaseHelper.getFoodByCategory("pizza"))
        foodAdapter.notifyDataSetChanged()

        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val filteredList = foodList.filter { food ->
                    food.name.contains(query ?: "", ignoreCase = true)
                }
                foodAdapter = FoodAdapter(filteredList, FoodAdapter.LayoutType.MENU)
                foodRecyclerView.adapter = foodAdapter
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredList = foodList.filter { food ->
                    food.name.contains(newText ?: "", ignoreCase = true)
                }
                foodAdapter = FoodAdapter(filteredList, FoodAdapter.LayoutType.MENU)
                foodRecyclerView.adapter = foodAdapter
                return true
            }
        })

        pizzaCategory.setOnClickListener {
            foodList.clear()
            foodList.addAll(FoodDatabaseHelper(requireContext()).getFoodByCategory("pizza"))
            foodAdapter.notifyDataSetChanged()
        }

        chickenCategory.setOnClickListener {
            foodList.clear()
            foodList.addAll(FoodDatabaseHelper(requireContext()).getFoodByCategory("chicken"))
            foodAdapter.notifyDataSetChanged()
        }

        pastaCategory.setOnClickListener {
            foodList.clear()
            foodList.addAll(FoodDatabaseHelper(requireContext()).getFoodByCategory("pasta"))
            foodAdapter.notifyDataSetChanged()
        }

        appetizerCategory.setOnClickListener {
            foodList.clear()
            foodList.addAll(FoodDatabaseHelper(requireContext()).getFoodByCategory("appetizer"))
            foodAdapter.notifyDataSetChanged()
        }

        drinksCategory.setOnClickListener {
            foodList.clear()
            foodList.addAll(FoodDatabaseHelper(requireContext()).getFoodByCategory("drinks"))
            foodAdapter.notifyDataSetChanged()
        }
    }
}
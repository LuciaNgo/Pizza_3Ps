package com.example.pizza3ps.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SearchView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pizza3ps.R
import com.example.pizza3ps.activity.CustomizePizzaActivity
import com.example.pizza3ps.activity.MainActivity
import com.example.pizza3ps.adapter.EventAdapter
import com.example.pizza3ps.adapter.FoodAdapter
import com.example.pizza3ps.model.EventData
import com.example.pizza3ps.model.FoodData
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DecimalFormat

class MenuFragment : Fragment() {

    private lateinit var pizzaRecyclerView: RecyclerView
    private lateinit var chickenRecyclerView: RecyclerView
    private lateinit var pastaRecyclerView: RecyclerView
    private lateinit var appetizerRecyclerView: RecyclerView
    private lateinit var drinksRecyclerView: RecyclerView

    private val eventList = mutableListOf<EventData>()
    private val pizzaList = mutableListOf<FoodData>()
    private val chickenList = mutableListOf<FoodData>()
    private val pastaList = mutableListOf<FoodData>()
    private val appetizerList = mutableListOf<FoodData>()
    private val drinksList = mutableListOf<FoodData>()

    private lateinit var eventAdapter: EventAdapter
    private lateinit var pizzaFoodAdapter: FoodAdapter
    private lateinit var chickenFoodAdapter: FoodAdapter
    private lateinit var pastaFoodAdapter: FoodAdapter
    private lateinit var appetizerFoodAdapter: FoodAdapter
    private lateinit var drinksFoodAdapter: FoodAdapter

    private lateinit var searchBar: SearchView
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_menu, container, false)

        val scrollView = view.findViewById<ScrollView>(R.id.menu_scroll_view)

        val chickenCategory = view.findViewById<LinearLayout>(R.id.chicken_category)
        val pastaCategory = view.findViewById<LinearLayout>(R.id.pasta_category)
        val appetizerCategory = view.findViewById<LinearLayout>(R.id.appetizer_category)
        val drinksCategory = view.findViewById<LinearLayout>(R.id.drinks_category)

        val chickenSection = view.findViewById<View>(R.id.chicken_section_title)
        val pastaSection = view.findViewById<View>(R.id.pasta_section_title)
        val appetizerSection = view.findViewById<View>(R.id.appetizer_section_title)
        val drinksSection = view.findViewById<View>(R.id.drinks_section_title)

        chickenCategory.setOnClickListener { scrollToSection(scrollView, chickenSection) }
        pastaCategory.setOnClickListener { scrollToSection(scrollView, pastaSection) }
        appetizerCategory.setOnClickListener { scrollToSection(scrollView, appetizerSection) }
        drinksCategory.setOnClickListener { scrollToSection(scrollView, drinksSection) }

        pizzaRecyclerView = view.findViewById(R.id.pizza_view)
        chickenRecyclerView = view.findViewById(R.id.chicken_view)
        pastaRecyclerView = view.findViewById(R.id.pasta_view)
        appetizerRecyclerView = view.findViewById(R.id.appetizer_view)
        drinksRecyclerView = view.findViewById(R.id.drinks_view)

        searchBar = view.findViewById(R.id.menu_search_bar)
        searchBar.clearFocus()

        pizzaRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        chickenRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        pastaRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        appetizerRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        drinksRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        eventAdapter = EventAdapter(eventList)
        pizzaFoodAdapter = FoodAdapter(pizzaList)
        chickenFoodAdapter = FoodAdapter(chickenList)
        pastaFoodAdapter = FoodAdapter(pastaList)
        appetizerFoodAdapter = FoodAdapter(appetizerList)
        drinksFoodAdapter = FoodAdapter(drinksList)

        pizzaRecyclerView.adapter = pizzaFoodAdapter
        chickenRecyclerView.adapter = chickenFoodAdapter
        pastaRecyclerView.adapter = pastaFoodAdapter
        appetizerRecyclerView.adapter = appetizerFoodAdapter
        drinksRecyclerView.adapter = drinksFoodAdapter

        fetchFoodData()

        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }
        })

        return view
    }

    private fun filterList(text: String?) {
        if (text.isNullOrEmpty()) return

        val filteredPizza = pizzaList.filter { it.name.lowercase().contains(text.lowercase()) }
        val filteredChicken = chickenList.filter { it.name.lowercase().contains(text.lowercase()) }
        val filteredPasta = pastaList.filter { it.name.lowercase().contains(text.lowercase()) }
        val filteredAppetizer =
            appetizerList.filter { it.name.lowercase().contains(text.lowercase()) }
        val filteredDrinks = drinksList.filter { it.name.lowercase().contains(text.lowercase()) }

        pizzaRecyclerView.adapter = if (filteredPizza.isNotEmpty()) FoodAdapter(filteredPizza) else null
        chickenRecyclerView.adapter = if (filteredChicken.isNotEmpty()) FoodAdapter(filteredChicken) else null
        pastaRecyclerView.adapter = if (filteredPasta.isNotEmpty()) FoodAdapter(filteredPasta) else null
        appetizerRecyclerView.adapter = if (filteredAppetizer.isNotEmpty()) FoodAdapter(filteredAppetizer) else null
        drinksRecyclerView.adapter = if (filteredDrinks.isNotEmpty()) FoodAdapter(filteredDrinks) else null
    }

    private fun scrollToSection(scrollView: ScrollView, targetView: View) {
        scrollView.post {
            val location = IntArray(2)
            targetView.getLocationOnScreen(location)
            val scrollY = location[1] - scrollView.top - 50
            scrollView.smoothScrollTo(0, scrollY)
        }
    }

    private fun fetchFoodData() {
        db.collection("Food")
            .get()
            .addOnSuccessListener { documents ->
                pizzaList.clear()
                chickenList.clear()
                pastaList.clear()
                appetizerList.clear()
                drinksList.clear()

                for (document in documents) {
                    val name = document.getString("name") ?: ""
                    val price = document.getString("price") ?: "0"
                    val imgPath = document.getString("imgPath") ?: ""
                    val ingredient = document.getString("ingredient") ?: ""
                    val category = document.getString("category") ?: ""

                    val foodItem = FoodData(name, price, category, imgPath, ingredient.split(", "))

                    when (category.lowercase()) {
                        "pizza" -> pizzaList.add(foodItem)
                        "pasta" -> pastaList.add(foodItem)
                        "chicken" -> chickenList.add(foodItem)
                        "appetizer" -> appetizerList.add(foodItem)
                        "drinks" -> drinksList.add(foodItem)
                    }
                }

                pizzaFoodAdapter.notifyDataSetChanged()
                chickenFoodAdapter.notifyDataSetChanged()
                pastaFoodAdapter.notifyDataSetChanged()
                appetizerFoodAdapter.notifyDataSetChanged()
                drinksFoodAdapter.notifyDataSetChanged()
            }
    }
}
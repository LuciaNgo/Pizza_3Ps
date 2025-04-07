package com.example.pizza3ps.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andremion.counterfab.CounterFab
import com.example.pizza3ps.R
import com.example.pizza3ps.adapter.EventAdapter
import com.example.pizza3ps.adapter.FoodAdapter
import com.example.pizza3ps.database.DatabaseHelper
import com.example.pizza3ps.model.EventData
import com.example.pizza3ps.model.FoodData
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DecimalFormat

class DashboardFragment : Fragment() {
    private lateinit var eventRecyclerView: RecyclerView
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

    private val db = FirebaseFirestore.getInstance()
    private lateinit var fab: CounterFab

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        fab = requireActivity().findViewById(R.id.cart_fab)
        fab.visibility = View.VISIBLE

        val scrollView = view.findViewById<ScrollView>(R.id.scroll_view)

        val pizzaCategory = view.findViewById<LinearLayout>(R.id.pizza_category)
        val chickenCategory = view.findViewById<LinearLayout>(R.id.chicken_category)
        val pastaCategory = view.findViewById<LinearLayout>(R.id.pasta_category)
        val appetizerCategory = view.findViewById<LinearLayout>(R.id.appetizer_category)
        val drinksCategory = view.findViewById<LinearLayout>(R.id.drinks_category)

        val pizzaSection = view.findViewById<View>(R.id.pizza_section_title)
        val chickenSection = view.findViewById<View>(R.id.chicken_section_title)
        val pastaSection = view.findViewById<View>(R.id.pasta_section_title)
        val appetizerSection = view.findViewById<View>(R.id.appetizer_section_title)
        val drinksSection = view.findViewById<View>(R.id.drinks_section_title)

        pizzaCategory.setOnClickListener { scrollToSection(scrollView, pizzaSection) }
        chickenCategory.setOnClickListener { scrollToSection(scrollView, chickenSection) }
        pastaCategory.setOnClickListener { scrollToSection(scrollView, pastaSection) }
        appetizerCategory.setOnClickListener { scrollToSection(scrollView, appetizerSection) }
        drinksCategory.setOnClickListener { scrollToSection(scrollView, drinksSection) }

        eventRecyclerView = view.findViewById(R.id.event_recycler_view)
        pizzaRecyclerView = view.findViewById(R.id.pizza_recycler_view)
        chickenRecyclerView = view.findViewById(R.id.chicken_recycler_view)
        pastaRecyclerView = view.findViewById(R.id.pasta_recycler_view)
        appetizerRecyclerView = view.findViewById(R.id.appetizer_recycler_view)
        drinksRecyclerView = view.findViewById(R.id.drinks_recycler_view)

        eventRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        pizzaRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        chickenRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        pastaRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        appetizerRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        drinksRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        eventAdapter = EventAdapter(eventList)
        pizzaFoodAdapter = FoodAdapter(pizzaList, FoodAdapter.LayoutType.DASHBOARD)
        chickenFoodAdapter = FoodAdapter(chickenList, FoodAdapter.LayoutType.DASHBOARD)
        pastaFoodAdapter = FoodAdapter(pastaList, FoodAdapter.LayoutType.DASHBOARD)
        appetizerFoodAdapter = FoodAdapter(appetizerList, FoodAdapter.LayoutType.DASHBOARD)
        drinksFoodAdapter = FoodAdapter(drinksList, FoodAdapter.LayoutType.DASHBOARD)

        eventRecyclerView.adapter = eventAdapter
        pizzaRecyclerView.adapter = pizzaFoodAdapter
        chickenRecyclerView.adapter = chickenFoodAdapter
        pastaRecyclerView.adapter = pastaFoodAdapter
        appetizerRecyclerView.adapter = appetizerFoodAdapter
        drinksRecyclerView.adapter = drinksFoodAdapter

        fetchEventData()
        fetchFoodData()

        return view
    }

    private fun scrollToSection(scrollView: ScrollView, targetView: View) {
        scrollView.post {
            val location = IntArray(2)
            targetView.getLocationOnScreen(location)
            val scrollY = location[1] - scrollView.top - 50
            scrollView.smoothScrollTo(0, scrollY)
        }
    }

    private fun fetchEventData() {
        db.collection("Event")
            .get()
            .addOnSuccessListener { documents ->
                eventList.clear()
                for (document in documents) {
                    val name = document.getString("name") ?: ""
                    val imgPath = document.getString("imgPath") ?: ""
                    val description = document.getString("description") ?: ""
                    eventList.add(EventData(name, imgPath, description))
                }
                eventAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Lỗi khi lấy dữ liệu", exception)
            }
    }

    private fun fetchFoodData() {
        val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        val lang = prefs.getString("lang", "en") ?: "en"

        db.collection("Food")
            .get()
            .addOnSuccessListener { documents ->
                pizzaList.clear()
                chickenList.clear()
                pastaList.clear()
                appetizerList.clear()
                drinksList.clear()

                val dbHelper = DatabaseHelper(requireContext())
//                dbHelper.clearAllFood()

                // Xóa dữ liệu cũ trong cơ sở dữ liệu cục bộ
                dbHelper.deleteAllFood()

                for (document in documents) {
                    val nameMap = document.get("name") as? Map<*, *>
                    val name = nameMap?.get(lang) as? String ?: ""
                    val price = document.getString("price")?.toIntOrNull() ?: 0
                    val formattedPrice = DecimalFormat("#,###").format(price)
                    val imgPath = document.getString("imgPath") ?: ""
                    val ingredientString = document.getString("ingredient") ?: ""
                    val category = document.getString("category") ?: ""
                    val ingredientList = ingredientString.split(", ").map { it.trim() }
                    val foodItem = FoodData(name, formattedPrice, category, imgPath, ingredientList)

                    when (category.lowercase()) {
                        "pizza" -> pizzaList.add(foodItem)
                        "pasta" -> pastaList.add(foodItem)
                        "chicken" -> chickenList.add(foodItem)
                        "appetizer" -> appetizerList.add(foodItem)
                        "drinks" -> drinksList.add(foodItem)
                    }

                    // Lưu vào cơ sở dữ liệu cục bộ
                    dbHelper.addFood(foodItem)
                }

                pizzaFoodAdapter.notifyDataSetChanged()
                chickenFoodAdapter.notifyDataSetChanged()
                pastaFoodAdapter.notifyDataSetChanged()
                appetizerFoodAdapter.notifyDataSetChanged()
                drinksFoodAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Lỗi khi lấy dữ liệu", exception)
            }
    }
}

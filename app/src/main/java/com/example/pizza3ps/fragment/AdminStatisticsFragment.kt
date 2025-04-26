package com.example.pizza3ps.fragment

import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CalendarView
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.pizza3ps.R
import com.example.pizza3ps.model.OrderData
import com.example.pizza3ps.model.OrderItemData
import com.example.pizza3ps.viewModel.OrdersViewModel
import com.google.android.material.tabs.TabLayout
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AdminStatisticsFragment : Fragment() {
    private lateinit var overviewButton: Button
    private lateinit var dailySelectDate: ImageView
    private lateinit var monthlySelectDate: ImageView
    private lateinit var yearlySelectDate: ImageView

    private lateinit var dailyRevenueValue: TextView
    private lateinit var monthlyRevenueValue: TextView
    private lateinit var yearlyRevenueValue: TextView

    private lateinit var revenueTabLayout: TabLayout
    private lateinit var revenueViewPager: ViewPager2

    private lateinit var bestSellerRecyclerView: RecyclerView
    private lateinit var viewModel: OrdersViewModel
    private val orderList = mutableListOf<OrderData>()
    private val orderItemList = mutableListOf<OrderItemData>()

    private var selectedDayForDailyRevenue: Int? = null
    private var selectedMonthForDailyRevenue: Int? = null
    private var selectedYearForDailyRevenue: Int? = null

    private var selectedMonthForMonthlyRevenue: Int? = null
    private var selectedYearForMonthlyRevenue: Int? = null

    private var selectedYearForYearlyRevenue: Int? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_statistics, container, false)

        viewModel = ViewModelProvider(this).get(OrdersViewModel::class.java)

        overviewButton = view.findViewById(R.id.overviewButton)
        dailySelectDate = view.findViewById(R.id.dailySelectDate)
        monthlySelectDate = view.findViewById(R.id.monthlySelectMonth)
        yearlySelectDate = view.findViewById(R.id.yearlySelectYear)
        dailyRevenueValue = view.findViewById(R.id.dailyRevenueValue)
        monthlyRevenueValue = view.findViewById(R.id.monthlyRevenueValue)
        yearlyRevenueValue = view.findViewById(R.id.yearlyRevenueValue)
        revenueTabLayout = view.findViewById(R.id.tabLayoutRevenueLineChart)
        revenueViewPager = view.findViewById(R.id.viewPagerRevenueLineChart)
        bestSellerRecyclerView = view.findViewById(R.id.recyclerView)
        bestSellerRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
//        bestSellerAdapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        observeCompletedOrders()

        overviewButton.setOnClickListener {
            // Handle overview button click
        }

        dailySelectDate.setOnClickListener {
            val dialog = Dialog(requireContext())
            dialog.setContentView(R.layout.date_picker)
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            val calendarView = dialog.findViewById<CalendarView>(R.id.calendar_view) // nhớ gán ID cho CalendarView trong XML nhé

            calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
                selectedDayForDailyRevenue = dayOfMonth
                selectedMonthForDailyRevenue = month + 1
                selectedYearForDailyRevenue = year

                dialog.dismiss()
            }

            dialog.show()

            // Neu co thay doi ngay thi cap nhat doanh thu
            dialog.setOnDismissListener {
                UpdateDailyRevenue()
            }
        }

        monthlySelectDate.setOnClickListener {
            val dialog = MonthYearPickerDialogFragment{ month, year ->
                selectedMonthForMonthlyRevenue = month
                selectedYearForMonthlyRevenue = year
            }

            dialog.show(parentFragmentManager, "MonthYearPickerDialog")

            // Neu dialog dong thi cap nhat doanh thu
//            dialog.setOnDismissListener {
//                UpdateMonthlyRevenue()
//            }
            // setOnDismissListener ko co tac dung gi o day vi ko co dialog
            // nen phai dung setOnDateChangeListener o CalendarView
            // de cap nhat doanh thu

        }

        yearlySelectDate.setOnClickListener {
            val dialog = YearPickerDialogFragment { year ->
                selectedYearForYearlyRevenue = year
            }
            dialog.show(parentFragmentManager, "YearPickerDialog")
        }
    }

    private fun observeCompletedOrders() {
        viewModel.startListeningOrders(orderStatus = "Completed", orderByDate = true)

        viewModel.orderList.observe(viewLifecycleOwner) { completedOrders ->
            orderList.clear()
            orderList.addAll(completedOrders)

            UpdateDailyRevenue()
            UpdateMonthlyRevenue()
            UpdateYearlyRevenue()
        }

        viewModel.orderItemList.observe(viewLifecycleOwner) { orderItems ->
            orderItemList.clear()
            orderItemList.addAll(orderItems)

            getBestSellerFoodId()

            // Update best seller RecyclerView
//            val bestSellerAdapter = BestSellerAdapter(bestSeller)
//            bestSellerRecyclerView.adapter = bestSellerAdapter
        }
    }

    private fun UpdateDailyRevenue() {
        if (selectedDayForDailyRevenue == null ||
            selectedMonthForDailyRevenue == null ||
            selectedYearForDailyRevenue == null
        ) {
            // Set ngay mac dinh
            val calendar = Calendar.getInstance()
            selectedDayForDailyRevenue = calendar.get(Calendar.DAY_OF_MONTH)
            selectedMonthForDailyRevenue = calendar.get(Calendar.MONTH) + 1
            selectedYearForDailyRevenue = calendar.get(Calendar.YEAR)
        }

        var dailyRevenue = 0

        for (order in orderList) {
            val dateTriple = parseDateToDayMonthYear(order.createdDate)

            if (dateTriple != null) {
                val (day, month, year) = dateTriple

                if (day == selectedDayForDailyRevenue &&
                    month == selectedMonthForDailyRevenue &&
                    year == selectedYearForDailyRevenue) {
                    dailyRevenue += order.totalAfterDiscount
                }
            }
        }

        val currencyFormat = java.text.NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        dailyRevenueValue.text = currencyFormat.format(dailyRevenue)

    }

    fun UpdateMonthlyRevenue() {
        if (selectedMonthForMonthlyRevenue == null ||
            selectedYearForMonthlyRevenue == null
        ) {
            // Set ngay mac dinh
            val calendar = Calendar.getInstance()
            selectedMonthForMonthlyRevenue = calendar.get(Calendar.MONTH) + 1
            selectedYearForMonthlyRevenue = calendar.get(Calendar.YEAR)
        }

        var monthlyRevenue = 0

        for (order in orderList) {
            val dateTriple = parseDateToDayMonthYear(order.createdDate)

            if (dateTriple != null) {
                val (day, month, year) = dateTriple

                if (month == selectedMonthForMonthlyRevenue &&
                    year == selectedYearForMonthlyRevenue) {
                    monthlyRevenue += order.totalAfterDiscount
                }
            }
        }

        val currencyFormat = java.text.NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        monthlyRevenueValue.text = currencyFormat.format(monthlyRevenue)

    }

    fun UpdateYearlyRevenue() {
        if (selectedYearForYearlyRevenue == null
        ) {
            // Set ngay mac dinh
            val calendar = Calendar.getInstance()
            selectedYearForYearlyRevenue = calendar.get(Calendar.YEAR)
        }

        var yearlyRevenue = 0

        for (order in orderList) {
            val dateTriple = parseDateToDayMonthYear(order.createdDate)

            if (dateTriple != null) {
                val (day, month, year) = dateTriple

                if (year == selectedYearForYearlyRevenue) {
                    yearlyRevenue += order.totalAfterDiscount
                }
            }
        }

        val currencyFormat = java.text.NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        yearlyRevenueValue.text = currencyFormat.format(yearlyRevenue)

    }

    private fun parseDateToDayMonthYear(dateString: String): Triple<Int, Int, Int>? {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) // Format đúng nè
        return try {
            val date = dateFormat.parse(dateString)
            if (date != null) {
                val calendar = Calendar.getInstance()
                calendar.time = date
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                val month = calendar.get(Calendar.MONTH) + 1
                val year = calendar.get(Calendar.YEAR)
                Triple(day, month, year)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun getBestSellerFoodId(): List<Pair<Int, Int>> {
        val foodIdCountMap = mutableMapOf<Int, Int>()

        for (orderItem in orderItemList) {
            val foodId = orderItem.foodId

            foodIdCountMap[foodId] = (foodIdCountMap[foodId] ?: 0) + orderItem.quantity
        }

        val sortedFoodIdCountMap = foodIdCountMap.toList().sortedByDescending { (_, count) -> count }

        return sortedFoodIdCountMap.take(3)
    }
}
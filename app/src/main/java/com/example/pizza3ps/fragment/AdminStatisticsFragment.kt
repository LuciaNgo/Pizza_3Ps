package com.example.pizza3ps.fragment

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.media.Image
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CalendarView
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pizza3ps.R
import com.example.pizza3ps.activity.LogInActivity
import com.example.pizza3ps.adapter.BestSellerAdapter
import com.example.pizza3ps.model.OrderData
import com.example.pizza3ps.model.OrderItemData
import com.example.pizza3ps.viewModel.AdminOrdersViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AdminStatisticsFragment : Fragment() {
    private lateinit var scrollView: ScrollView
    private lateinit var overviewButton: Button
    private lateinit var logoutButton: ImageView

    private lateinit var dailyCardView: CardView
    private lateinit var weeklyCardView: CardView
    private lateinit var monthlyCardView: CardView
    private lateinit var yearlyCardView: CardView

    private lateinit var dailyRevenueValue: TextView
    private lateinit var weeklyRevenueValue: TextView
    private lateinit var monthlyRevenueValue: TextView
    private lateinit var yearlyRevenueValue: TextView
    private lateinit var revenueOverviewTitle: TextView

    private lateinit var revenueTabLayout: TabLayout
    private lateinit var revenueBarChart: BarChart

    private lateinit var bestSellerRecyclerView: RecyclerView
    private lateinit var viewModel: AdminOrdersViewModel
    private val orderList = mutableListOf<OrderData>()
    private val orderItemList = mutableListOf<OrderItemData>()

    private var selectedDayForDailyRevenue: Int? = null
    private var selectedMonthForDailyRevenue: Int? = null
    private var selectedYearForDailyRevenue: Int? = null

    private var selectedDayForWeeklyRevenue: Int? = null
    private var selectedMonthForWeeklyRevenue: Int? = null
    private var selectedYearForWeeklyRevenue: Int? = null

    private var selectedMonthForMonthlyRevenue: Int? = null
    private var selectedYearForMonthlyRevenue: Int? = null

    private var selectedYearForYearlyRevenue: Int? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_statistics, container, false)

        viewModel = ViewModelProvider(this).get(AdminOrdersViewModel::class.java)

        scrollView = view.findViewById(R.id.scrollView)
        overviewButton = view.findViewById(R.id.overviewButton)
        logoutButton = view.findViewById(R.id.logoutButton)
        dailyCardView = view.findViewById(R.id.dailyCardView)
        weeklyCardView = view.findViewById(R.id.weeklyCardView)
        monthlyCardView = view.findViewById(R.id.monthlyCardView)
        yearlyCardView = view.findViewById(R.id.yearlyCardView)
        dailyRevenueValue = view.findViewById(R.id.dailyRevenueValue)
        weeklyRevenueValue = view.findViewById(R.id.weeklyRevenueValue)
        monthlyRevenueValue = view.findViewById(R.id.monthlyRevenueValue)
        yearlyRevenueValue = view.findViewById(R.id.yearlyRevenueValue)
        revenueOverviewTitle = view.findViewById(R.id.revenueOverviewTitle)
        revenueTabLayout = view.findViewById(R.id.tabLayoutRevenueLineChart)
        revenueBarChart = view.findViewById(R.id.revenueBarChart)
        bestSellerRecyclerView = view.findViewById(R.id.recyclerView)
        bestSellerRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        revenueBarChart.description.isEnabled = false
//        bestSellerAdapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        observeCompletedOrders()

        overviewButton.setOnClickListener {
            // Handle overview button click
            scrollView.post {
                val location = IntArray(2)
                revenueOverviewTitle.getLocationOnScreen(location)
                val scrollY = location[1] - scrollView.top - 68
                scrollView.smoothScrollTo(0, scrollY)
            }
        }

        revenueTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        revenueBarChart.visibility = View.VISIBLE
                        revenueBarChart.clear()
                        drawRevenueBarChart(orderList, revenueBarChart, "daily")
                    }
                    1 -> {
                        revenueBarChart.visibility = View.VISIBLE
                        revenueBarChart.clear()
                        drawRevenueBarChart(orderList, revenueBarChart, "monthly")
                    }
                    2 -> {
                        revenueBarChart.visibility = View.VISIBLE
                        revenueBarChart.clear()
                        drawRevenueBarChart(orderList, revenueBarChart, "yearly")
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })


        dailyCardView.setOnClickListener {
            val dialog = Dialog(requireContext())
            dialog.setContentView(R.layout.date_picker)
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            val calendarView = dialog.findViewById<CalendarView>(R.id.calendar_view)

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

        weeklyCardView.setOnClickListener {
            val dialog = Dialog(requireContext())
            dialog.setContentView(R.layout.date_picker)
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            val calendarView = dialog.findViewById<CalendarView>(R.id.calendar_view)

            calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
                selectedDayForWeeklyRevenue = dayOfMonth
                selectedMonthForWeeklyRevenue = month + 1
                selectedYearForWeeklyRevenue = year

                dialog.dismiss()
            }

            dialog.show()

            // Neu co thay doi ngay thi cap nhat doanh thu
            dialog.setOnDismissListener {
                UpdateWeeklyRevenue()
            }
        }

        monthlyCardView.setOnClickListener {
            val dialog = MonthYearPickerDialogFragment{ month, year ->
                selectedMonthForMonthlyRevenue = month
                selectedYearForMonthlyRevenue = year
            }

            dialog.show(parentFragmentManager, "MonthYearPickerDialog")

            // Neu dialog dong thi cap nhat doanh thu
//            dialog.setOnDismissListener {
//                UpdateMonthlyRevenue()
//            }
        }

        yearlyCardView.setOnClickListener {
            val dialog = YearPickerDialogFragment { year ->
                selectedYearForYearlyRevenue = year
            }
            dialog.show(parentFragmentManager, "YearPickerDialog")
        }

        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val sharedPref = requireContext().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
            sharedPref.edit().remove("username").apply()
            val intent = Intent(context, LogInActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun observeCompletedOrders() {
        viewModel.startListeningOrders(orderStatus = "Completed", orderByDate = true)

        viewModel.orderList.observe(viewLifecycleOwner) { completedOrders ->
            orderList.clear()
            orderList.addAll(completedOrders)

            UpdateDailyRevenue()
            UpdateWeeklyRevenue()
            UpdateMonthlyRevenue()
            UpdateYearlyRevenue()
            drawRevenueBarChart(orderList, revenueBarChart, "daily")
        }

        viewModel.orderItemList.observe(viewLifecycleOwner) { orderItems ->
            orderItemList.clear()
            orderItemList.addAll(orderItems)

            val bestSeller = getBestSellerFoodId()

            // Update best seller RecyclerView
            val bestSellerAdapter = BestSellerAdapter(bestSeller)
            bestSellerRecyclerView.adapter = bestSellerAdapter
        }
    }

    fun drawRevenueBarChart(orderList: List<OrderData>, barChart: BarChart, mode: String) {
        val revenueMap = mutableMapOf<String, Float>()

        // Lặp qua orderList và cộng doanh thu theo ngày
        for (order in orderList) {
            val dateTriple = parseDateToDayMonthYear(order.createdDate)

            if (dateTriple != null) {
                val (day, month, year) = dateTriple

                var orderDate = ""
                if (mode == "daily") orderDate = "$day/$month/$year"
                else if (mode == "monthly") orderDate = "$month/$year"
                else if (mode == "yearly") orderDate = "$year"

                // Cộng doanh thu vào doanh thu của ngày đó
                revenueMap[orderDate] = revenueMap.getOrDefault(orderDate, 0f) + order.totalAfterDiscount.toFloat()
            }
        }

        // Chuyển map doanh thu thành list các BarEntry (x là ngày, y là doanh thu)
        val entries = mutableListOf<BarEntry>()
        var xValue = 0f

        for ((date, revenue) in revenueMap) {
            entries.add(BarEntry(xValue++, revenue))
        }

        // Tạo BarDataSet từ list entries
        var barDataSet = BarDataSet(entries, "Doanh thu theo ngày")
        if (mode == "daily") {
            barDataSet = BarDataSet(entries, "Doanh thu theo ngày")
        } else if (mode == "monthly") {
            barDataSet = BarDataSet(entries, "Doanh thu theo tháng")
        } else if (mode == "yearly") {
            barDataSet = BarDataSet(entries, "Doanh thu theo năm")
        }

        barDataSet.color = resources.getColor(R.color.orange)
        barDataSet.valueTextColor = resources.getColor(R.color.black)
        barDataSet.valueTextSize = 10f

        // Tạo BarData từ BarDataSet
        val barData = BarData(barDataSet)

        // Gán BarData vào BarChart
        barChart.data = barData

        // Cập nhật biểu đồ
        barChart.invalidate()
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

    private fun UpdateWeeklyRevenue() {
        if (selectedDayForWeeklyRevenue == null ||
            selectedMonthForWeeklyRevenue == null ||
            selectedYearForWeeklyRevenue == null
        ) {
            // Set ngay mac dinh
            val calendar = Calendar.getInstance()
            selectedDayForWeeklyRevenue = calendar.get(Calendar.DAY_OF_MONTH)
            selectedMonthForWeeklyRevenue = calendar.get(Calendar.MONTH) + 1
            selectedYearForWeeklyRevenue = calendar.get(Calendar.YEAR)
        }

        val selectedDate = "$selectedDayForWeeklyRevenue/$selectedMonthForWeeklyRevenue/$selectedYearForWeeklyRevenue"

        val (startOfWeek, endOfWeek) = getStartAndEndOfWeek(selectedDate) ?: return

        var weeklyRevenue = 0

        for (order in orderList) {
            val dateTriple = parseDateToDayMonthYear(order.createdDate)

            if (dateTriple != null) {
                val (day, month, year) = dateTriple
                val orderDate = "$day/$month/$year"

                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val startDate = dateFormat.parse(startOfWeek)
                val endDate = dateFormat.parse(endOfWeek)
                val compareDate = dateFormat.parse(orderDate)

                if (compareDate != null && startDate != null && endDate != null) {
                    if (compareDate >= startDate && compareDate <= endDate) {
                        weeklyRevenue += order.totalAfterDiscount
                    }
                }
            }
        }

        val currencyFormat = java.text.NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        weeklyRevenueValue.text = currencyFormat.format(weeklyRevenue)
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

    fun getStartAndEndOfWeek(dateString: String): Pair<String, String>? {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        return try {
            val date = dateFormat.parse(dateString)

            // Sử dụng Calendar để tính toán
            val calendar = Calendar.getInstance()
            calendar.time = date

            calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
            val startOfWeek = calendar.time

            calendar.add(Calendar.DATE, 6)
            val endOfWeek = calendar.time

            // Chuyển đổi ngày đầu và cuối tuần thành chuỗi định dạng dd/MM/yyyy
            val startOfWeekString = dateFormat.format(startOfWeek)
            val endOfWeekString = dateFormat.format(endOfWeek)

            Pair(startOfWeekString, endOfWeekString)
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

        return sortedFoodIdCountMap.filter { it.first != 0 }.take(3)
    }
}
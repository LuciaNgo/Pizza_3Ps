package com.example.pizza3ps.fragment

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CalendarView
import android.widget.ScrollView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pizza3ps.R
import com.example.pizza3ps.adapter.BestSellerAdapter
import com.example.pizza3ps.model.OrderData
import com.example.pizza3ps.model.OrderItemData
import com.example.pizza3ps.viewModel.AdminOrdersViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.tabs.TabLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AdminStatisticsFragment : Fragment() {
    private lateinit var scrollView: ScrollView
    private lateinit var overviewButton: Button

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
    private lateinit var revenueLineChart: LineChart

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
        revenueLineChart = view.findViewById(R.id.revenueLineChart)
        bestSellerRecyclerView = view.findViewById(R.id.recyclerView)
        bestSellerRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        revenueLineChart.description.isEnabled = false
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
                    0 -> drawRevenueLineChart(orderList, revenueLineChart, "daily")
                    1 -> drawRevenueLineChart(orderList, revenueLineChart, "monthly")
                    2 -> drawRevenueLineChart(orderList, revenueLineChart, "yearly")
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
            // setOnDismissListener ko co tac dung gi o day vi ko co dialog
            // nen phai dung setOnDateChangeListener o CalendarView
            // de cap nhat doanh thu

        }

        yearlyCardView.setOnClickListener {
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
            UpdateWeeklyRevenue()
            UpdateMonthlyRevenue()
            UpdateYearlyRevenue()
            drawRevenueLineChart(orderList, revenueLineChart, "daily")
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

    fun drawRevenueLineChart(orderList: List<OrderData>, lineChart: LineChart, mode: String) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
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

        // Chuyển map doanh thu thành list các Entry (x là ngày, y là doanh thu)
        val entries = mutableListOf<Entry>()
        var xValue = 0f

        for ((date, revenue) in revenueMap) {
            entries.add(Entry(xValue++, revenue))
        }

        // Tạo LineDataSet từ list entries
        val lineDataSet = LineDataSet(entries, "Doanh thu theo ngày")
        lineDataSet.color = resources.getColor(R.color.orange)  // Màu đường line
        lineDataSet.valueTextColor = resources.getColor(R.color.black)  // Màu giá trị
        lineDataSet.valueTextSize = 10f

        // Tạo LineData từ LineDataSet
        val lineData = LineData(lineDataSet)

        // Gán LineData vào LineChart
        lineChart.data = lineData

        // Cập nhật biểu đồ
        lineChart.invalidate()  // Redraw chart
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
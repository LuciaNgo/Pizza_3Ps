package com.example.pizza3ps.fragment

import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CalendarView
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.pizza3ps.R
import com.google.android.material.tabs.TabLayout

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_admin_statistics, container, false)

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

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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
                val selectedDate = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)

                // dailyRevenueValue.setText(selectedDate)
                var selectedDateVariable = selectedDate

                dialog.dismiss()
            }

            dialog.show()


        }

        monthlySelectDate.setOnClickListener {
            val dialog = MonthYearPickerDialogFragment { month, year ->
                val selected = String.format("%02d/%d", month, year)
                monthlyRevenueValue.text = selected
            }

            dialog.show(parentFragmentManager, "MonthYearPickerDialog")
        }

        yearlySelectDate.setOnClickListener {
            val dialog = YearPickerDialogFragment { year ->
                val selected = String.format("%d", year)
                yearlyRevenueValue.text = selected
            }

            dialog.show(parentFragmentManager, "YearPickerDialog")
        }
    }


}
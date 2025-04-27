package com.example.pizza3ps.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.pizza3ps.fragment.OrderHistoryListFragment
import com.example.pizza3ps.fragment.OrderListFragment

class OrdersViewPagerHistoryAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {
    private val statuses = listOf(
        "Pending",
        "Confirmed",
        "Preparing",
        "Shipping",
        "Completed",
        "Cancelled"
    )

    override fun getItemCount(): Int {
        return statuses.size
    }

    override fun createFragment(position: Int): Fragment {
        return OrderHistoryListFragment.newInstance(statuses[position])
    }

}
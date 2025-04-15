package com.example.pizza3ps.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.pizza3ps.fragment.OrderListFragment

class OrdersViewPagerAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {
    private val statuses = listOf(
        "Awaiting confirmation",
        "Confirmed",
        "Preparing",
        "Delivering",
        "Completed",
        "Cancelled"
    )

    override fun getItemCount(): Int {
        return statuses.size
    }

    override fun createFragment(position: Int): Fragment {
        return OrderListFragment.newInstance(statuses[position])
    }

}
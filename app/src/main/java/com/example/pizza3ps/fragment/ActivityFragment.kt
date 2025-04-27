package com.example.pizza3ps.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.andremion.counterfab.CounterFab
import com.example.pizza3ps.R
import com.example.pizza3ps.activity.DeliveryActivity
import com.example.pizza3ps.adapter.OrderAdapter
import com.example.pizza3ps.adapter.OrdersViewPagerAdapter
import com.example.pizza3ps.adapter.OrdersViewPagerHistoryAdapter
import com.example.pizza3ps.model.OrderData
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.Locale

class ActivityFragment : Fragment() {
    private lateinit var fab: CounterFab
    private lateinit var recyclerView: RecyclerView
    private lateinit var orderAdapter: OrderAdapter
    private var db = FirebaseFirestore.getInstance()
    private var orderListenerRegistration: ListenerRegistration? = null
    private var orderStatus: String? = null


//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//    }

//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        fab = requireActivity().findViewById(R.id.cart_fab)
//        fab.visibility = View.GONE
//        return inflater.inflate(R.layout.fragment_activity, container, false)
//    }


    companion object {
        private const val ARG_STATUS = "order_status"

        fun newInstance(status: String): OrderListFragment {
            val fragment = OrderListFragment()
            val args = Bundle()
            args.putString(ARG_STATUS, status)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        orderStatus = arguments?.getString(ARG_STATUS)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_order_history, container, false)

        val viewPager = view.findViewById<ViewPager2>(R.id.viewPagerManageOrders)
        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayoutManageOrders)

        // Adapter quản lý các Fragment con tương ứng với từng trạng thái đơn hàng
        val adapter = OrdersViewPagerHistoryAdapter(requireActivity())
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Pending"
                1 -> "Confirmed"
                2 -> "Preparing"
                3 -> "Shipping"
                4 -> "Completed"
                else -> "Cancelled"
            }
        }.attach()
        return view
    }

}
package com.example.pizza3ps.fragment

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pizza3ps.R
import com.example.pizza3ps.adapter.OrderAdapter
import com.example.pizza3ps.model.OrderData
import com.example.pizza3ps.viewModel.OrdersViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class OrderListFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var orderAdapter: OrderAdapter
    private var db = FirebaseFirestore.getInstance()
    private var orderListenerRegistration: ListenerRegistration? = null
    private lateinit var viewModel: OrdersViewModel
    private var orderStatus: String? = null
    private var isLoading = false

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
        val view = inflater.inflate(R.layout.fragment_order_list, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewOrders)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        orderAdapter = OrderAdapter(
            onCancelClicked = { order -> cancelOrder(order) },
            onNextStatusClicked = { order, nextStatus -> updateOrderStatus(order, nextStatus) },
            onItemClicked = { order -> showOrderDetails(order) }
        )
        recyclerView.adapter = orderAdapter

        setupViewModel()

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        orderListenerRegistration?.remove()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this).get(OrdersViewModel::class.java)

        viewModel.orderList.observe(viewLifecycleOwner) { newOrderList ->
            orderAdapter.submitList(newOrderList)
        }

        loadOrders()
    }

    private fun loadOrders() {
        if (orderStatus == "Completed" || orderStatus == "Cancelled") {
            viewModel.startListeningOrders(orderStatus = orderStatus, orderByDate = true)
        } else {
            viewModel.startListeningOrders(orderStatus = orderStatus, orderByDate = false)
        }
    }

    private fun setupScrollListener() {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    val layoutManager = rv.layoutManager as LinearLayoutManager
                    val totalItemCount = layoutManager.itemCount
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                    if (!isLoading && lastVisibleItemPosition >= totalItemCount - 5) {
//                        loadNextPage()
                    }
                }
            }
        })
    }

    private fun cancelOrder(order: OrderData) {
        db.collection("Orders")
            .document(order.orderId)
            .update("status", "Cancelled")
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Order cancelled", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateOrderStatus(order: OrderData, nextStatus: String) {
        db.collection("Orders")
            .document(order.orderId)
            .update("status", nextStatus)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Updated to $nextStatus", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showOrderDetails(order: OrderData) {
        val action = OrderManagementFragmentDirections.actionOrderManagementToOrderDetails(order)
        findNavController().navigate(action)
    }
}

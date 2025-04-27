package com.example.pizza3ps.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pizza3ps.R
import com.example.pizza3ps.activity.DeliveryActivity
import com.example.pizza3ps.adapter.OrderAdapter
import com.example.pizza3ps.model.OrderData
import com.example.pizza3ps.viewModel.AdminOrdersViewModel
import com.example.pizza3ps.viewModel.CustomerOrderViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class OrderHistoryListFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var orderAdapter: OrderAdapter
    private var db = FirebaseFirestore.getInstance()
    private var orderListenerRegistration: ListenerRegistration? = null
    private lateinit var viewModel: CustomerOrderViewModel
    private var orderStatus: String? = null

    companion object {
        private const val ARG_STATUS = "order_status"

        fun newInstance(status: String): OrderHistoryListFragment {
            val fragment = OrderHistoryListFragment()
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
            onItemClicked = { order -> toDeliveryPage(order) },
            showNextStatusButton = false,
            hideCancelAfterConfirmed = true,
            source = "customer"
        )
        recyclerView.adapter = orderAdapter

        viewModel = ViewModelProvider(this).get(CustomerOrderViewModel::class.java)
        if (orderStatus != null) {
            viewModel.listenToOrdersRealtime(orderStatus!!)
            viewModel.getOrdersByStatus(orderStatus!!).observe(viewLifecycleOwner) { orders ->
                orderAdapter.submitList(orders)
            }
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        orderListenerRegistration?.remove()
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

    private fun toDeliveryPage(order: OrderData) {
        val intent = Intent(requireContext(), DeliveryActivity::class.java)
        intent.putExtra("order_id", order.orderId)
        startActivity(intent)
    }
}

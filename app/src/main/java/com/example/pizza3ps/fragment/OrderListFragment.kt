package com.example.pizza3ps.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pizza3ps.R
import com.example.pizza3ps.adapter.OrderAdapter
import com.example.pizza3ps.model.OrderData
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale

class OrderListFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var orderAdapter: OrderAdapter
    private var db = FirebaseFirestore.getInstance()
    private var orderListenerRegistration: ListenerRegistration? = null
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

//        listenToOrdersRealtime()
        loadOrders()

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        orderListenerRegistration?.remove()
    }

    private fun loadOrders() {
        if (orderStatus == "Completed" || orderStatus == "Cancelled") {
            listenToCompletedAndCancelledOrdersRealtime()
        }
        else {
            listenToOrdersRealtime()
        }
    }

    private fun listenToCompletedAndCancelledOrdersRealtime() {
        if (orderStatus == null) return

        orderListenerRegistration = db.collection("Orders")
            .whereEqualTo("status", orderStatus)
            .orderBy("createdDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val newOrderList = snapshots?.mapNotNull { toOrderData(it) } ?: emptyList()

                orderAdapter.submitList(newOrderList)
            }
    }

    private fun listenToOrdersRealtime() {
        if (orderStatus == null) return

        orderListenerRegistration = db.collection("Orders")
            .whereEqualTo("status", orderStatus)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val newOrderList = snapshots?.mapNotNull { toOrderData(it) } ?: emptyList()

                orderAdapter.submitList(newOrderList)
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

    private fun toOrderData(doc: DocumentSnapshot): OrderData? {
        return try {
            OrderData(
                orderId = doc.id,
                createdDate = doc.getTimestamp("createdDate")?.let { formatTimestamp(it) } ?: "",
                status = doc.getString("status") ?: "",
                receiverName = doc.getString("receiverName") ?: "",
                phoneNumber = doc.getString("phoneNumber") ?: "",
                address = doc.getString("address") ?: "",
                totalQuantity = doc.getLong("totalQuantity")?.toInt() ?: 0,
                totalAfterDiscount = doc.getLong("totalAfterDiscount")?.toInt() ?: 0,
                payment = doc.getString("payment") ?: ""
            )
        } catch (e: Exception) {
            null
        }
    }

    private val dateFormatter by lazy {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    }

    fun formatTimestamp(timestamp: com.google.firebase.Timestamp): String {
        return dateFormatter.format(timestamp.toDate())
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
        val orderDetailsFragment = OrderDetailsFragment.newInstance(order)
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerOrderManagment, orderDetailsFragment)
            .addToBackStack(null)
            .commit()
    }
}

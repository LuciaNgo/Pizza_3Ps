package com.example.pizza3ps.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pizza3ps.R
import com.example.pizza3ps.activity.DeliveryActivity
import com.example.pizza3ps.adapter.OrderAdapter
import com.example.pizza3ps.model.OrderData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.Locale

class OrderHistoryListFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var orderAdapter: OrderAdapter
    private var db = FirebaseFirestore.getInstance()
    private var orderListenerRegistration: ListenerRegistration? = null
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
            hideCancelAfterConfirmed = true
        )
        recyclerView.adapter = orderAdapter

        listenToOrdersRealtime()

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        orderListenerRegistration?.remove()
    }

    private fun listenToOrdersRealtime() {
        val user = FirebaseAuth.getInstance().currentUser?.uid
        if (orderStatus == null || user == null) return

        orderListenerRegistration = db.collection("Orders")
            .whereEqualTo("userID", user)
            .whereEqualTo("status", orderStatus)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val newOrderList = snapshots?.mapNotNull { doc ->
                    try {
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
                } ?: emptyList()

                orderAdapter.submitList(newOrderList)
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

    private fun toDeliveryPage(order: OrderData) {
        val intent = Intent(requireContext(), DeliveryActivity::class.java)
        intent.putExtra("order_id", order.orderId)
        startActivity(intent)
    }
}

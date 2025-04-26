package com.example.pizza3ps.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pizza3ps.R
import com.example.pizza3ps.adapter.OrderDetailsAdapter
import com.example.pizza3ps.model.CartData
import com.example.pizza3ps.model.OrderData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class OrderDetailsFragment : Fragment() {
    private var orderData : OrderData? = null
    private var orderDetailsItemList = mutableListOf<CartData>()
    private lateinit var adapter: OrderDetailsAdapter
    private lateinit var recyclerViewOrderDetails: RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args: OrderDetailsFragmentArgs by navArgs()
        orderData = args.order
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_order_details, container, false)

        orderData?.let { order ->
            view.findViewById<TextView>(R.id.tvOrderId).text = "Order #${order.orderId}"
            view.findViewById<TextView>(R.id.tvOrderDate).text = "Date: ${order.createdDate}"
            view.findViewById<TextView>(R.id.tvOrderStatus).text = "Status: ${order.status}"
            view.findViewById<TextView>(R.id.tvReceiverName).text = "Name: ${order.receiverName}"
            view.findViewById<TextView>(R.id.tvPhone).text = "Phone: ${order.phoneNumber}"
            view.findViewById<TextView>(R.id.tvAddress).text = "Address: ${order.address}"
            view.findViewById<TextView>(R.id.tvTotalQuantity).text = "Total Quantity: ${order.totalQuantity}"
            view.findViewById<TextView>(R.id.tvTotalPrice).text = "Total: ${order.totalAfterDiscount}đ"
            view.findViewById<TextView>(R.id.tvPayment).text = "Payment: ${order.payment}"
        }

        recyclerViewOrderDetails = view.findViewById(R.id.recyclerView_OrderDetailsItemsList)
        adapter = OrderDetailsAdapter(orderDetailsItemList)
        recyclerViewOrderDetails.adapter = adapter
        recyclerViewOrderDetails.layoutManager = LinearLayoutManager(requireContext())

        getOrderDetailsItemList()


        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        val btnNextStatus = view.findViewById<Button>(R.id.btnNextStatus)

        val currentStatus = orderData?.status
        val orderId = orderData?.orderId

        if (currentStatus == "Cancelled" || currentStatus == "Completed") {
            btnCancel.visibility = View.GONE
            btnNextStatus.visibility = View.GONE
        } else {
            btnCancel.visibility = View.VISIBLE
            btnNextStatus.visibility = View.VISIBLE

            val nextStatus = getNextStatus(currentStatus ?: "")

            btnNextStatus.text = nextStatus ?: "Done"

            btnNextStatus.setOnClickListener {
                if (nextStatus != null && orderId != null) {
                    FirebaseFirestore.getInstance()
                        .collection("Orders")
                        .document(orderId)
                        .update("status", nextStatus)
                        .addOnSuccessListener {
                            orderData?.status = nextStatus
                            view.findViewById<TextView>(R.id.tvOrderStatus).text = "Status: $nextStatus"
                            if (nextStatus == "Completed") {
                                btnNextStatus.visibility = View.GONE
                                btnCancel.visibility = View.GONE
                            } else {
                                btnNextStatus.text = getNextStatus(nextStatus) ?: "Done"
                            }
                        }
                }
            }

            btnCancel.setOnClickListener {
                if (orderId != null) {
                    FirebaseFirestore.getInstance()
                        .collection("Orders")
                        .document(orderId)
                        .update("status", "Cancelled")
                        .addOnSuccessListener {
                            orderData?.status = "Cancelled"
                            view.findViewById<TextView>(R.id.tvOrderStatus).text = "Status: Cancelled"
                            btnCancel.visibility = View.GONE
                            btnNextStatus.visibility = View.GONE
                        }
                }
            }
        }

        return view
    }

    private fun getOrderDetailsItemList() {
        val db = FirebaseFirestore.getInstance()

        db.collection("Orders")
            .document(orderData?.orderId ?: return)  // Nếu orderData hoặc orderId null thì thoát
            .collection("OrderDetails")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val items = mutableListOf<CartData>()
                    val querySnapshot: QuerySnapshot = task.result ?: return@addOnCompleteListener

                    for (document in querySnapshot) {
                        val food_id = document.getLong("foodId") ?.toInt() ?: 0
                        val size = document.getString("size") ?: ""
                        val crust = document.getString("crust") ?: ""
                        val crustBase = document.getString("crustBase") ?: ""
                        val ingredients = document.getString("ingredients") ?.replaceFirstChar { it.uppercase() } ?: ""
                        val quantity = document.getLong("quantity")?.toInt() ?: 1
                        val price = document.getLong("price")?.toInt() ?: 0

                        Log.d("price", price.toString())

                        val cartItem = CartData(food_id, price, emptyList(), size, crust, crustBase, quantity)
                        items.add(cartItem)
                    }

                    orderDetailsItemList.clear()
                    orderDetailsItemList.addAll(items)
                    adapter.notifyDataSetChanged()

                } else {
                    task.exception?.printStackTrace()
                }
            }
    }

    private fun getNextStatus(currentStatus: String): String? {
        val statusFlow = listOf("Pending", "Confirmed", "Preparing", "Shipping", "Completed")
        val index = statusFlow.indexOf(currentStatus)
        return if (index != -1 && index < statusFlow.size - 1) {
            statusFlow[index + 1]
        } else {
            null
        }
    }
}
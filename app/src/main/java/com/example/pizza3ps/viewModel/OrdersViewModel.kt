package com.example.pizza3ps.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import com.example.pizza3ps.model.OrderData
import com.example.pizza3ps.model.OrderItemData
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class OrdersViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private var orderListenerRegistration: ListenerRegistration? = null

    private val _orderList = MutableLiveData<List<OrderData>>()
    val orderList: LiveData<List<OrderData>> = _orderList

    private val _orderItemList = MutableLiveData<List<OrderItemData>>()
    val orderItemList: LiveData<List<OrderItemData>> = _orderItemList

    fun startListeningOrders(orderStatus: String?, orderByDate: Boolean = false) {
        if (orderStatus == null) return

        orderListenerRegistration?.remove()

        var query = db.collection("Orders")
            .whereEqualTo("status", orderStatus)

        if (orderByDate) {
            query = query.orderBy("createdDate", Query.Direction.DESCENDING)
        }

        orderListenerRegistration = query
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

//                val newOrderList = snapshots?.mapNotNull { toOrderData(it) } ?: emptyList()
//                _orderList.value = newOrderList

                val newOrderList = mutableListOf<OrderData>()
                val newOrderItemList = mutableListOf<OrderItemData>()

                // Lấy thông tin từng order
                snapshots?.forEach { orderDoc ->
                    val orderData = toOrderData(orderDoc)
                    if (orderData != null) {
                        // Thêm order vào danh sách orderList
                        newOrderList.add(orderData)

                        // Lấy các món hàng trong order
                        getOrderItems(orderDoc.id) { orderItems ->
                            // Thêm món hàng vào danh sách orderItemList
                            Log.d("OrderItems", "Order ID: ${orderDoc.id}, Items: $orderItems")
                            newOrderItemList.addAll(orderItems)

                            // Khi đã hoàn tất, cập nhật LiveData
                            if (newOrderList.size == snapshots.size()) {
                                _orderList.value = newOrderList
                            }
                            _orderItemList.value = newOrderItemList
                        }
                    }
                }

            }
    }

    fun stopListeningOrders() {
        orderListenerRegistration?.remove()
        orderListenerRegistration = null
    }

    override fun onCleared() {
        super.onCleared()
        stopListeningOrders()
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

    private fun formatTimestamp(timestamp: Timestamp): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
        return sdf.format(timestamp.toDate())
    }

    private fun getOrderItems(orderId: String, onComplete: (List<OrderItemData>) -> Unit) {
        db.collection("Orders")
            .document(orderId)
            .collection("OrderDetails")
            .get()
            .addOnSuccessListener { itemsSnapshot ->
//                val orderItems = itemsSnapshot.mapNotNull { it.toObject(OrderItemData::class.java) }
                val orderItems = itemsSnapshot.mapNotNull { itemDoc ->
                    try {
                        OrderItemData(
                            foodId = itemDoc.getLong("foodId")?.toInt() ?: 0,
                            price = itemDoc.getLong("price")?.toInt() ?: 0,
                            ingredients = itemDoc.getString("ingredients") ?: "",
                            size = itemDoc.getString("size") ?: "",
                            crust = itemDoc.getString("crust") ?: "",
                            crustBase = itemDoc.getString("crustBase") ?: "",
                            quantity = itemDoc.getLong("quantity")?.toInt() ?: 1,
                            orderId = orderId
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                onComplete(orderItems)
            }
            .addOnFailureListener {
                onComplete(emptyList())
            }
    }
}
package com.example.pizza3ps.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pizza3ps.model.OrderData
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.Locale

class CustomerOrderViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private var orderListenerRegistration: ListenerRegistration? = null

    private val orderListsByStatus = mutableMapOf<String, MutableLiveData<List<OrderData>>>()

    fun getOrdersByStatus(status: String): LiveData<List<OrderData>> {
        return orderListsByStatus.getOrPut(status) { MutableLiveData() }
    }

    fun listenToOrdersRealtime(orderStatus: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null || orderStatus.isNullOrEmpty()) return

        orderListenerRegistration?.remove()

        orderListenerRegistration = db.collection("Orders")
            .whereEqualTo("userID", userId)
            .whereEqualTo("status", orderStatus)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                // Chuyển đổi dữ liệu từ Firebase thành OrderData và post vào LiveData
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

                // Cập nhật dữ liệu vào LiveData tương ứng với status
                orderListsByStatus.getOrPut(orderStatus) { MutableLiveData() }.postValue(newOrderList)
            }
    }

//    fun listenToOrdersRealtime(orderStatus: String?) {
//        val userId = FirebaseAuth.getInstance().currentUser?.uid
//        if (userId == null || orderStatus == null) return
//
//        orderListenerRegistration?.remove()
//
//        orderListenerRegistration = db.collection("Orders")
//            .whereEqualTo("userID", userId)
//            .whereEqualTo("status", orderStatus)
//            .addSnapshotListener { snapshots, error ->
//                if (error != null) {
//                    return@addSnapshotListener
//                }
//
//                val newOrderList = snapshots?.mapNotNull { doc ->
//                    try {
//                        OrderData(
//                            orderId = doc.id,
//                            createdDate = doc.getTimestamp("createdDate")?.let { formatTimestamp(it) } ?: "",
//                            status = doc.getString("status") ?: "",
//                            receiverName = doc.getString("receiverName") ?: "",
//                            phoneNumber = doc.getString("phoneNumber") ?: "",
//                            address = doc.getString("address") ?: "",
//                            totalQuantity = doc.getLong("totalQuantity")?.toInt() ?: 0,
//                            totalAfterDiscount = doc.getLong("totalAfterDiscount")?.toInt() ?: 0,
//                            payment = doc.getString("payment") ?: ""
//                        )
//                    } catch (e: Exception) {
//                        null
//                    }
//                } ?: emptyList()
//
//                _orderList.postValue(newOrderList)
//            }
//    }

    private val dateFormatter by lazy {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    }

    private fun formatTimestamp(timestamp: Timestamp): String {
        return dateFormatter.format(timestamp.toDate())
    }

    override fun onCleared() {
        super.onCleared()
        orderListenerRegistration?.remove()
    }
}
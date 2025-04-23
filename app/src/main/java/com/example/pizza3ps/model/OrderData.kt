package com.example.pizza3ps.model

data class OrderData(
    val orderId: String = "",
    val createdDate: String = "",
    val status: String = "",
    val receiverName: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val totalQuantity: Int = 0,
    val totalAfterDiscount: Int = 0,
    val discount: Int = 0,
    val payment: String = ""
)

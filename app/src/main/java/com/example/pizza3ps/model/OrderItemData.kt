package com.example.pizza3ps.model


data class OrderItemData(
    val foodId: Int,
    val price: Int = 0,
    val ingredients: String = "",
    val size: String = "",
    val crust: String = "",
    val crustBase: String = "",
    val quantity: Int = 1,
    val orderId: String = ""
){
    constructor() : this(0, 0, "", "", "", "", 1, "")
}

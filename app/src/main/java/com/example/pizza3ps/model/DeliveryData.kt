package com.example.pizza3ps.model

data class DeliveryData(
    val foodId: Int = 0,
    val quantity: Int = 0,
    val price: Int = 0,
    val size: String = "",
    val crust: String = "",
    val crustBase: String = "",
    val ingredients: String = ""
)


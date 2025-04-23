package com.example.pizza3ps.model

data class CartData(
    val food_id: Int,
    val price: Int = 0,
    val ingredients: List<String> = emptyList(),
    val size: String = "",
    val crust: String = "",
    val crustBase: String = "",
    val quantity: Int = 1,
    val user_email: String = ""
)
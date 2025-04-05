package com.example.pizza3ps.model

data class CartData(
    val name: String = "",
    val price: Int = 0,
    val category: String = "",
    val imgPath: String = "",
    val ingredients: List<String>,
    val size: String = "",
    val crust: String = "",
    val crustBase: String = "",
    val quantity: Int = 1
)

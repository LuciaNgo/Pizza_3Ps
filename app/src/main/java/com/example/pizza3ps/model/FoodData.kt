package com.example.pizza3ps.model

data class FoodData(
    val name: String = "",
    val price: String = "",
    val category: String = "",
    val imgPath: String = "",
    val ingredients: List<String>
)

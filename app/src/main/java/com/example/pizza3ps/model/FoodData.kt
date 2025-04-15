package com.example.pizza3ps.model

data class FoodData(
    val name_en: String = "",
    val name_vi: String = "",
    val price: String = "",
    val category: String = "",
    val imgPath: String = "",
    val ingredients: List<String>
) {
    fun getName(lang: String): String {
        return if (lang == "en") name_en else name_vi
    }
}

package com.example.app2025.Domain

import java.io.Serializable

data class FoodModel(
    var BestFood: Boolean = false,
    var CategoryId: String = "",
    var Description: String = "",
    var Id: Long = 0L,
    var ImagePath: String = "",
    var LocationId: Long = 0L,
    var Price: Double = 0.0,
    var PriceId: Long = 0L,
    var Star: Double = 0.0,
    var TimeId: Long = 0L,
    var TimeValue: Long = 0L,
    var Title: String = "",
    var Calorie: Int = 0,
    var numberInCart: Int = 0
) : Serializable

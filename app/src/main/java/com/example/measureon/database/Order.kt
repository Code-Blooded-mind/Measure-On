package com.example.measureon.database

data class Order(
    val id: Int = 0,
    val orderNumber: Int,
    val customerNumber: String,
    val deliveryDate: String,
    val shirts: Int,
    val pants: Int,
    val shorts: Int,
    val totalPrice: Double,
    val status: String = "pending",
    val isStarred: Boolean = false,
    val isReady: Boolean = false,
    val isDelivered: Boolean = false,
    val measurements: String = "",
    val imageUri: String?
)

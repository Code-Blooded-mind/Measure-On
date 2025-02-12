package com.example.measureon.database

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class OrderRepository(context: Context) {

    private val orderDao: OrderDao = OrderDao(context)

    suspend fun insertOrder(order: Order) {
        withContext(Dispatchers.IO) {
            try {
                orderDao.insertOrder(order)
                Log.d("OrderRepository", "Order inserted successfully: $order")
            } catch (e: Exception) {
                Log.e("OrderRepository", "Error inserting order", e)
            }
        }
    }

    suspend fun getOrderById(orderNumber: Int): Order? {
        return withContext(Dispatchers.IO) {
            try {
                orderDao.getOrderById(orderNumber)
            } catch (e: Exception) {
                Log.e("OrderRepository", "Error fetching order by ID", e)
                null
            }
        }
    }

    suspend fun getAllOrders(): List<Order> {
        return withContext(Dispatchers.IO) {
            try {
                orderDao.getAllOrdersFlow().firstOrNull() ?: emptyList()
            } catch (e: Exception) {
                Log.e("OrderRepository", "Error fetching all orders", e)
                emptyList()
            }
        }
    }

    suspend fun updateOrderStatus(orderNumber: Int, status: String) {
        withContext(Dispatchers.IO) {
            try {
                val order = getOrderById(orderNumber)
                if (order != null) {
                    val updatedOrder = order.copy(status = status)
                    orderDao.updateOrder(updatedOrder)
                    Log.d("OrderRepository", "Order #$orderNumber updated to $status")
                } else {
                    Log.e("OrderRepository", "Order #$orderNumber not found for status update")
                }
            } catch (e: Exception) {
                Log.e("OrderRepository", "Error updating order #$orderNumber", e)
            }
        }
    }

    suspend fun getOrderStatus(orderNumber: Int): String {
        return withContext(Dispatchers.IO) {
            try {
                val order = getOrderById(orderNumber)
                order?.status ?: "unknown"
            } catch (e: Exception) {
                Log.e("OrderRepository", "Error fetching order status for #$orderNumber", e)
                "unknown"
            }
        }
    }

    suspend fun updateOrder(
        orderNumber: Int,
        shirts: Int,
        pants: Int,
        shorts: Int,
        deliveryDate: String,
        isReady: Boolean,
        isDelivered: Boolean,
        isStarred: Boolean,
        imageUri: String?,
        measurements: String
    ) {
        withContext(Dispatchers.IO) {
            try {
                val order = getOrderById(orderNumber)
                if (order != null) {
                    val updatedOrder = order.copy(
                        shirts = shirts,
                        pants = pants,
                        shorts = shorts,
                        deliveryDate = deliveryDate,
                        isReady = isReady,
                        isDelivered = isDelivered,
                        isStarred = isStarred,
                        imageUri = imageUri,
                        measurements = measurements
                    )
                    orderDao.updateOrder(updatedOrder)
                    Log.d("OrderRepository", "Order #$orderNumber updated successfully")
                } else {
                    Log.e("OrderRepository", "Order #$orderNumber not found for update")
                }
            } catch (e: Exception) {
                Log.e("OrderRepository", "Error updating order #$orderNumber", e)
            }
        }
    }

    suspend fun deleteOrder(orderNumber: Int) {
        withContext(Dispatchers.IO) {
            try {
                orderDao.deleteOrder(orderNumber)
                Log.d("OrderRepository", "Order #$orderNumber deleted successfully")
            } catch (e: Exception) {
                Log.e("OrderRepository", "Error deleting order #$orderNumber", e)
            }
        }
    }

    suspend fun getNextOrderNumber(): Int {
        return withContext(Dispatchers.IO) {
            try {
                (orderDao.getMaxOrderNumber() ?: 0) + 1
            } catch (e: Exception) {
                Log.e("OrderRepository", "Error getting next order number", e)
                1
            }
        }
    }
}
package com.example.measureon.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class OrderDao(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE orders (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                orderNumber INTEGER NOT NULL UNIQUE,
                customerNumber TEXT NOT NULL,
                deliveryDate TEXT NOT NULL,
                shirts INTEGER DEFAULT 0,
                pants INTEGER DEFAULT 0,
                shorts INTEGER DEFAULT 0,
                totalPrice REAL NOT NULL,
                status TEXT DEFAULT 'pending',
                isStarred INTEGER DEFAULT 0,
                isReady INTEGER DEFAULT 0,
                isDelivered INTEGER DEFAULT 0,
                measurements TEXT DEFAULT '',
                imageUri TEXT DEFAULT NULL
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS orders")
        onCreate(db)
    }

    fun getOrderById(orderNumber: Int): Order? {
        val db = readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM orders WHERE orderNumber = ?", arrayOf(orderNumber.toString()))
        return cursor.use { if (it.moveToFirst()) cursorToOrder(it) else null }
    }

    fun insertOrder(order: Order): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("orderNumber", order.orderNumber)
            put("customerNumber", order.customerNumber)
            put("deliveryDate", order.deliveryDate)
            put("shirts", order.shirts)
            put("pants", order.pants)
            put("shorts", order.shorts)
            put("totalPrice", order.totalPrice)
            put("status", order.status)
            put("isStarred", if (order.isStarred) 1 else 0)
            put("isReady", if (order.isReady) 1 else 0)
            put("isDelivered", if (order.isDelivered) 1 else 0)
            put("measurements", order.measurements)
            put("imageUri", order.imageUri ?: "") // Handle null values
        }
        return db.insert("orders", null, values)
    }

    fun updateOrder(order: Order) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("customerNumber", order.customerNumber)
            put("deliveryDate", order.deliveryDate)
            put("shirts", order.shirts)
            put("pants", order.pants)
            put("shorts", order.shorts)
            put("totalPrice", order.totalPrice)
            put("status", order.status)
            put("isStarred", if (order.isStarred) 1 else 0)
            put("isReady", if (order.isReady) 1 else 0)
            put("isDelivered", if (order.isDelivered) 1 else 0)
            put("measurements", order.measurements)
            put("imageUri", order.imageUri ?: "") // Handle null values properly
        }
        db.update("orders", values, "orderNumber = ?", arrayOf(order.orderNumber.toString()))
    }

    fun deleteOrder(orderNumber: Int) {
        val db = writableDatabase
        db.delete("orders", "orderNumber = ?", arrayOf(orderNumber.toString()))
    }

    fun getAllOrdersFlow(): Flow<List<Order>> = flow {
        val db = readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM orders", null)
        val orders = mutableListOf<Order>()
        while (cursor.moveToNext()) {
            cursorToOrder(cursor)?.let { orders.add(it) }
        }
        cursor.close()
        emit(orders)
    }

    fun getMaxOrderNumber(): Int? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT MAX(orderNumber) FROM orders", null)
        return if (cursor.moveToFirst()) cursor.getInt(0) else null
    }

    private fun cursorToOrder(cursor: Cursor): Order {
        return Order(
            id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
            orderNumber = cursor.getInt(cursor.getColumnIndexOrThrow("orderNumber")),
            customerNumber = cursor.getString(cursor.getColumnIndexOrThrow("customerNumber")),
            deliveryDate = cursor.getString(cursor.getColumnIndexOrThrow("deliveryDate")),
            shirts = cursor.getInt(cursor.getColumnIndexOrThrow("shirts")),
            pants = cursor.getInt(cursor.getColumnIndexOrThrow("pants")),
            shorts = cursor.getInt(cursor.getColumnIndexOrThrow("shorts")),
            totalPrice = cursor.getDouble(cursor.getColumnIndexOrThrow("totalPrice")),
            status = cursor.getString(cursor.getColumnIndexOrThrow("status")),
            isStarred = cursor.getInt(cursor.getColumnIndexOrThrow("isStarred")) == 1,
            isReady = cursor.getInt(cursor.getColumnIndexOrThrow("isReady")) == 1,
            isDelivered = cursor.getInt(cursor.getColumnIndexOrThrow("isDelivered")) == 1,
            measurements = cursor.getString(cursor.getColumnIndexOrThrow("measurements")),
            imageUri = cursor.getString(cursor.getColumnIndexOrThrow("imageUri")) ?: "" // Handle null values
        )
    }

    companion object {
        private const val DATABASE_NAME = "measureon.db"
        private const val DATABASE_VERSION = 2 // Update schema version
    }
}

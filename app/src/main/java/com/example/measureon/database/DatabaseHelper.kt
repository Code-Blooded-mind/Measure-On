package com.example.measureon.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val createOrdersTable = """
            CREATE TABLE $TABLE_ORDERS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_ORDER_NUMBER INTEGER UNIQUE,
                $COLUMN_CUSTOMER_NUMBER TEXT NOT NULL,
                $COLUMN_DELIVERY_DATE TEXT NOT NULL,
                $COLUMN_SHIRTS INTEGER DEFAULT 0,
                $COLUMN_PANTS INTEGER DEFAULT 0,
                $COLUMN_SHORTS INTEGER DEFAULT 0,
                $COLUMN_TOTAL_PRICE REAL DEFAULT 0,
                $COLUMN_STATUS TEXT DEFAULT 'pending',
                $COLUMN_STARRED INTEGER DEFAULT 0,
                $COLUMN_READY INTEGER DEFAULT 0,
                $COLUMN_DELIVERED INTEGER DEFAULT 0,
                $COLUMN_MEASUREMENTS TEXT DEFAULT '',
                $COLUMN_IMAGE_URI TEXT
            )
        """.trimIndent()
        db.execSQL(createOrdersTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ORDERS")
        onCreate(db)
    }

    fun insertOrder(order: Order): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ORDER_NUMBER, order.orderNumber)
            put(COLUMN_CUSTOMER_NUMBER, order.customerNumber)
            put(COLUMN_DELIVERY_DATE, order.deliveryDate)
            put(COLUMN_SHIRTS, order.shirts)
            put(COLUMN_PANTS, order.pants)
            put(COLUMN_SHORTS, order.shorts)
            put(COLUMN_TOTAL_PRICE, order.totalPrice)
            put(COLUMN_STATUS, order.status)
            put(COLUMN_STARRED, if (order.isStarred) 1 else 0)
            put(COLUMN_READY, if (order.isReady) 1 else 0)
            put(COLUMN_DELIVERED, if (order.isDelivered) 1 else 0)
            put(COLUMN_MEASUREMENTS, order.measurements)
            put(COLUMN_IMAGE_URI, order.imageUri)
        }
        return db.insert(TABLE_ORDERS, null, values)
    }

    fun updateOrder(order: Order) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CUSTOMER_NUMBER, order.customerNumber)
            put(COLUMN_DELIVERY_DATE, order.deliveryDate)
            put(COLUMN_SHIRTS, order.shirts)
            put(COLUMN_PANTS, order.pants)
            put(COLUMN_SHORTS, order.shorts)
            put(COLUMN_TOTAL_PRICE, order.totalPrice)
            put(COLUMN_STATUS, order.status)
            put(COLUMN_STARRED, if (order.isStarred) 1 else 0)
            put(COLUMN_READY, if (order.isReady) 1 else 0)
            put(COLUMN_DELIVERED, if (order.isDelivered) 1 else 0)
            put(COLUMN_MEASUREMENTS, order.measurements)
            put(COLUMN_IMAGE_URI, order.imageUri)
        }
        db.update(TABLE_ORDERS, values, "$COLUMN_ORDER_NUMBER = ?", arrayOf(order.orderNumber.toString()))
    }

    companion object {
        private const val DATABASE_NAME = "measurego.db"
        private const val DATABASE_VERSION = 1
        const val TABLE_ORDERS = "orders"
        const val COLUMN_ID = "id"
        const val COLUMN_ORDER_NUMBER = "order_number"
        const val COLUMN_CUSTOMER_NUMBER = "customer_number"
        const val COLUMN_DELIVERY_DATE = "delivery_date"
        const val COLUMN_SHIRTS = "shirts"
        const val COLUMN_PANTS = "pants"
        const val COLUMN_SHORTS = "shorts"
        const val COLUMN_TOTAL_PRICE = "total_price"
        const val COLUMN_STATUS = "status"
        const val COLUMN_STARRED = "starred"
        const val COLUMN_READY = "is_ready"
        const val COLUMN_DELIVERED = "is_delivered"
        const val COLUMN_MEASUREMENTS = "measurements"
        const val COLUMN_IMAGE_URI = "image_uri"
    }
}

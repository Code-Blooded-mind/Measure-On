package com.example.measureon.ui.screens

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.measureon.database.Order
import com.example.measureon.database.OrderRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

@Composable
fun DashboardScreen(orderRepository: OrderRepository, context: Context) {
    val coroutineScope = rememberCoroutineScope()
    var orders by remember { mutableStateOf(emptyList<Order>()) }
    var selectedMonth by remember { mutableStateOf(LocalDate.now().monthValue) }
    var selectedYear by remember { mutableStateOf(LocalDate.now().year) }

    LaunchedEffect(selectedMonth, selectedYear) {
        coroutineScope.launch {
            orders = orderRepository.getAllOrders().filter { order ->
                val orderDate = runCatching {
                    LocalDate.parse(order.deliveryDate, DateTimeFormatter.ofPattern("yyyy-M-d"))
                }.getOrNull()
                orderDate?.monthValue == selectedMonth && orderDate?.year == selectedYear
            }
        }
    }

    val totalOrders = orders.size
    val deliveredOrders = orders.filter { it.status == "delivered" }
    val totalDelivered = deliveredOrders.size
    val totalReadyToDeliver = orders.count { it.status == "ready" }
    val totalPending = orders.count { it.status == "pending" }

    val totalShirtsDelivered = deliveredOrders.sumOf { it.shirts ?: 0 }
    val totalPantsDelivered = deliveredOrders.sumOf { it.pants ?: 0 }
    val totalShortsDelivered = deliveredOrders.sumOf { it.shorts ?: 0 }

    val shirtPrice = 450
    val pantPrice = 500
    val shortPrice = 400
    val totalRevenue = (totalShirtsDelivered * shirtPrice) + (totalPantsDelivered * pantPrice) + (totalShortsDelivered * shortPrice)

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Dashboard", style = MaterialTheme.typography.headlineMedium)

        Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Total Orders: $totalOrders")
                Text("Total Delivered: $totalDelivered")
                Text("Total Ready to Deliver: $totalReadyToDeliver")
                Text("Total Pending (Not Received): $totalPending")
                Text("Total Shirts Delivered: $totalShirtsDelivered")
                Text("Total Pants Delivered: $totalPantsDelivered")
                Text("Total Shorts Delivered: $totalShortsDelivered")
                Text("Total Revenue Collected: ₹$totalRevenue")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            showDatePicker(context) { month, year ->
                selectedMonth = month
                selectedYear = year
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Filter by Month & Year")
        }
    }
}

fun showDatePicker(context: Context, onDateSelected: (Int, Int) -> Unit) {
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)

    DatePickerDialog(context, { _, selectedYear, selectedMonth, _ ->
        onDateSelected(selectedMonth + 1, selectedYear)
    }, year, month, 1).show()
}
package com.example.measureon.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.measureon.database.Order
import com.example.measureon.database.OrderRepository
import com.example.measureon.utils.SmsHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, orderRepository: OrderRepository) {
    var searchQuery by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf("All") }
    val coroutineScope = rememberCoroutineScope()
    val ordersState = remember { mutableStateOf(emptyList<Order>()) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            ordersState.value = orderRepository.getAllOrders()
        }
    }

    val filteredOrders = ordersState.value.filter { order ->
        val matchesSearch = searchQuery.isEmpty() ||
                order.orderNumber.toString().contains(searchQuery, ignoreCase = true) ||
                order.customerNumber.contains(searchQuery, ignoreCase = true)

        when (filterType) {
            "UDO" -> matchesSearch && order.status != "delivered"
            "SO" -> matchesSearch && order.isStarred
            else -> matchesSearch
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        HomeTopBar(searchQuery, onSearchQueryChanged = { query -> searchQuery = query })
        Spacer(modifier = Modifier.height(8.dp))
        FilterButtons(filterType, onFilterChanged = { filterType = it })
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            if (filteredOrders.isEmpty()) {
                item { Text("No orders found", modifier = Modifier.padding(16.dp)) }
            } else {
                items(filteredOrders) { order ->
                    OrderCard(order, orderRepository, navController)
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
        FloatingActionButton(
            onClick = { navController.navigate("take_order") },
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Order", tint = Color.Black)
        }
    }
}

@Composable
fun HomeTopBar(searchQuery: String, onSearchQueryChanged: (String) -> Unit) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChanged,
        label = { Text("Search by Order Number or Phone Number") },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun FilterButtons(selectedFilter: String, onFilterChanged: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        listOf("All", "UDO", "SO").forEach { filter ->
            Button(
                onClick = { onFilterChanged(filter) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedFilter == filter) Color.Blue else Color.Gray
                )
            ) {
                Text(filter, color = Color.White)
            }
        }
    }
}

@Composable
fun OrderCard(order: Order, orderRepository: OrderRepository, navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var orderStatus by remember { mutableStateOf(order.status) }

    LaunchedEffect(order.orderNumber) {
        coroutineScope.launch {
            orderStatus = orderRepository.getOrderStatus(order.orderNumber)
        }
    }

    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Order #${order.orderNumber}", fontWeight = FontWeight.Bold)
            Text("Delivery Date: ${order.deliveryDate}")
            Text("Amount: ₹${order.totalPrice}")

            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            orderRepository.updateOrderStatus(order.orderNumber, "ready")
                            SmsHelper.sendSms(context, order.customerNumber, "Your order #${order.orderNumber} is ready for pickup!")
                            orderStatus = "ready"
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = orderStatus !in listOf("ready", "delivered")
                ) {
                    Text("Order Ready")
                }
                Button(
                    onClick = {
                        coroutineScope.launch {
                            orderRepository.updateOrderStatus(order.orderNumber, "delivered")
                            SmsHelper.sendSms(context, order.customerNumber, "Your order #${order.orderNumber} has been delivered. Thank you!")
                            orderStatus = "delivered"
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = orderStatus != "delivered"
                ) {
                    Text("Order Delivered")
                }
                Button(
                    onClick = { navController.navigate("view_order/${order.orderNumber}") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("View Order")
                }
            }
        }
    }
}

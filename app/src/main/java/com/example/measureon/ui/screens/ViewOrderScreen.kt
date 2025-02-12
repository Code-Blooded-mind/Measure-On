package com.example.measureon.ui.screens

import android.app.DatePickerDialog
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.measureon.R
import com.example.measureon.database.Order
import com.example.measureon.database.OrderRepository
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun ViewOrderScreen(navController: NavController, orderRepository: OrderRepository, orderNumber: Int) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var order by remember { mutableStateOf<Order?>(null) }

    LaunchedEffect(orderNumber) {
        order = orderRepository.getOrderById(orderNumber)
    }

    order?.let { currentOrder ->
        var shirts by remember { mutableStateOf(currentOrder.shirts) }
        var pants by remember { mutableStateOf(currentOrder.pants) }
        var shorts by remember { mutableStateOf(currentOrder.shorts) }
        var deliveryDate by remember { mutableStateOf(currentOrder.deliveryDate) }
        var isReady by remember { mutableStateOf(currentOrder.isReady) }
        var isDelivered by remember { mutableStateOf(currentOrder.isDelivered) }
        var isStarred by remember { mutableStateOf(currentOrder.isStarred) }
        var imageUri by remember { mutableStateOf<Uri?>(currentOrder.imageUri?.let { Uri.parse(it) }) }

        val measurements = currentOrder.measurements?.split("|") ?: listOf("", "", "")
        var shirtMeasurements by remember { mutableStateOf(measurements.getOrElse(0) { "" }) }
        var pantMeasurements by remember { mutableStateOf(measurements.getOrElse(1) { "" }) }
        var shortMeasurements by remember { mutableStateOf(measurements.getOrElse(2) { "" }) }

        val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            imageUri = uri
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(text = "View Order #$orderNumber", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            IconButton(onClick = { isStarred = !isStarred }) {
                Icon(
                    painter = painterResource(if (isStarred) R.drawable.ic_star_filled else R.drawable.ic_star_outline),
                    contentDescription = "Star Order",
                    tint = if (isStarred) Color.Yellow else Color.Gray
                )
            }

            OrderInputField("Shirts", shirts) { shirts = it }
            MeasurementInputField("Shirt Measurements", shirtMeasurements) { shirtMeasurements = it }
            OrderInputField("Pants", pants) { pants = it }
            MeasurementInputField("Pant Measurements", pantMeasurements) { pantMeasurements = it }
            OrderInputField("Shorts", shorts) { shorts = it }
            MeasurementInputField("Short Measurements", shortMeasurements) { shortMeasurements = it }

            Button(onClick = {
                val calendar = Calendar.getInstance()
                DatePickerDialog(context, { _, year, month, dayOfMonth ->
                    deliveryDate = "$year-${month + 1}-$dayOfMonth"
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
            }) {
                Text(if (deliveryDate.isEmpty()) "Select Delivery Date" else "Delivery Date: $deliveryDate")
            }

            Spacer(modifier = Modifier.height(16.dp))

            imageUri?.let {
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = "Order Image",
                    modifier = Modifier.fillMaxWidth().height(200.dp).clickable { imagePicker.launch("image/*") }
                )
            } ?: Button(onClick = { imagePicker.launch("image/*") }) {
                Text("Add/Replace Image")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(onClick = {
                    coroutineScope.launch {
                        orderRepository.updateOrder(
                            orderNumber, shirts, pants, shorts, deliveryDate, isReady, isDelivered, isStarred, imageUri?.toString(), "$shirtMeasurements|$pantMeasurements|$shortMeasurements"
                        )
                        Toast.makeText(context, "Order updated", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                }) {
                    Text("Save Changes")
                }

                Button(onClick = {
                    coroutineScope.launch {
                        orderRepository.deleteOrder(orderNumber)
                        navController.popBackStack()
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                    Text("Delete Order")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

        }
    } ?: Text(text = "Order not found", color = Color.Red)
}
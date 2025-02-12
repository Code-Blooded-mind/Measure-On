package com.example.measureon.ui.screens

import android.app.DatePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.measureon.R
import com.example.measureon.database.Order
import com.example.measureon.database.OrderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun TakeOrderScreen(navController: NavController, orderRepository: OrderRepository) {
    var shirtCount by remember { mutableStateOf(0) }
    var pantCount by remember { mutableStateOf(0) }
    var shortCount by remember { mutableStateOf(0) }
    var shirtMeasurement by remember { mutableStateOf("") }
    var pantMeasurement by remember { mutableStateOf("") }
    var shortMeasurement by remember { mutableStateOf("") }
    var deliveryDate by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var isStarred by remember { mutableStateOf(false) }
    var clothImageUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("MeasureOnPrefs", Context.MODE_PRIVATE)
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        clothImageUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Take Order", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OrderInputField("Shirts", shirtCount) { shirtCount = it }
        MeasurementInputField("Shirt Measurements", shirtMeasurement) { shirtMeasurement = it }

        OrderInputField("Pants", pantCount) { pantCount = it }
        MeasurementInputField("Pant Measurements", pantMeasurement) { pantMeasurement = it }

        OrderInputField("Shorts", shortCount) { shortCount = it }
        MeasurementInputField("Short Measurements", shortMeasurement) { shortMeasurement = it }

        Button(onClick = {
            val calendar = Calendar.getInstance()
            DatePickerDialog(context, { _, year, month, dayOfMonth ->
                deliveryDate = "$year-${month + 1}-$dayOfMonth"
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }) {
            Text(if (deliveryDate.isEmpty()) "Select Delivery Date" else "Delivery Date: $deliveryDate")
        }

        OutlinedTextField(
            value = customerPhone,
            onValueChange = { customerPhone = it },
            label = { Text("Customer Phone Number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { imagePickerLauncher.launch("image/*") }) {
            Text("Upload Cloth Image")
        }
        clothImageUri?.let { uri ->
            Image(
                painter = rememberAsyncImagePainter(uri),
                contentDescription = "Selected Cloth Image",
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        IconButton(onClick = { isStarred = !isStarred }) {
            Icon(
                painter = painterResource(if (isStarred) R.drawable.ic_star_filled else R.drawable.ic_star_outline),
                contentDescription = "Star Order"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (customerPhone.isBlank() || deliveryDate.isBlank()) {
                Toast.makeText(context, "Please enter all details", Toast.LENGTH_SHORT).show()
                return@Button
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val orderNumber = orderRepository.getNextOrderNumber()
                    val totalPrice = calculateTotalAmount(shirtCount, pantCount, shortCount, sharedPreferences)
                    val status = "pending"

                    val order = Order(
                        orderNumber = orderNumber,
                        customerNumber = customerPhone,
                        deliveryDate = deliveryDate,
                        shirts = shirtCount,
                        pants = pantCount,
                        shorts = shortCount,
                        totalPrice = totalPrice,
                        status = status,
                        isStarred = isStarred,
                        isReady = false,
                        isDelivered = false,
                        measurements = "$shirtMeasurement|$pantMeasurement|$shortMeasurement",
                        imageUri = clothImageUri?.toString()
                    )

                    orderRepository.insertOrder(order)
                    launch(Dispatchers.Main) {
                        Toast.makeText(context, "Order Added", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                } catch (e: Exception) {
                    Log.e("TakeOrderScreen", "Error saving order", e)
                }
            }
        }) {
            Text("Save Order")
        }
    }
}

@Composable
fun OrderInputField(label: String, value: Int, onValueChange: (Int) -> Unit) {
    OutlinedTextField(
        value = value.toString(),
        onValueChange = { newValue -> onValueChange(newValue.toIntOrNull() ?: 0) },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun MeasurementInputField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth()
    )
}

fun calculateTotalAmount(shirts: Int, pants: Int, shorts: Int, sharedPreferences: SharedPreferences): Double {
    val shirtPrice = sharedPreferences.getFloat("shirtPrice", 450.0f).toDouble()
    val pantPrice = sharedPreferences.getFloat("pantPrice", 500.0f).toDouble()
    val shortPrice = sharedPreferences.getFloat("shortPrice", 400.0f).toDouble()
    return (shirts * shirtPrice) + (pants * pantPrice) + (shorts * shortPrice)
}

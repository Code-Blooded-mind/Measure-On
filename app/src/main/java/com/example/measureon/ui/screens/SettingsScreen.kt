package com.example.measureon.ui.screens

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.measureon.database.OrderRepository

@Composable
fun SettingsScreen(orderRepository: OrderRepository) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("MeasureOnPrefs", Context.MODE_PRIVATE)

    var shirtPrice by remember { mutableStateOf(sharedPreferences.getFloat("shirtPrice", 450.0f)) }
    var pantPrice by remember { mutableStateOf(sharedPreferences.getFloat("pantPrice", 500.0f)) }
    var shortPrice by remember { mutableStateOf(sharedPreferences.getFloat("shortPrice", 400.0f)) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = shirtPrice.toString(),
            onValueChange = { shirtPrice = it.toFloatOrNull() ?: shirtPrice },
            label = { Text("Shirt Price") }
        )
        OutlinedTextField(
            value = pantPrice.toString(),
            onValueChange = { pantPrice = it.toFloatOrNull() ?: pantPrice },
            label = { Text("Pant Price") }
        )
        OutlinedTextField(
            value = shortPrice.toString(),
            onValueChange = { shortPrice = it.toFloatOrNull() ?: shortPrice },
            label = { Text("Short Price") }
        )

        Button(onClick = {
            savePrices(sharedPreferences, shirtPrice, pantPrice, shortPrice, context)
        }) {
            Text("Save Prices")
        }
    }
}

fun savePrices(prefs: SharedPreferences, shirt: Float, pant: Float, short: Float, context: Context) {
    prefs.edit().apply {
        putFloat("shirtPrice", shirt)
        putFloat("pantPrice", pant)
        putFloat("shortPrice", short)
        commit()
    }
    Toast.makeText(context, "Prices Updated", Toast.LENGTH_SHORT).show()
}

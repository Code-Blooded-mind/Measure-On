package com.example.measureon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.measureon.database.OrderRepository
import com.example.measureon.ui.screens.*
import com.example.measureon.ui.theme.MeasureOnTheme

class MainActivity : ComponentActivity() {
    private lateinit var orderRepository: OrderRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        orderRepository = OrderRepository(this)

        setContent {
            MeasureOnTheme {
                val navController = rememberNavController()
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = { BottomNavigationBar(navController) }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("home") { HomeScreen(navController, orderRepository) }
                        composable("take_order") { TakeOrderScreen(navController, orderRepository) }
                        composable("view_order/{orderNumber}") { backStackEntry ->
                            val orderNumber = backStackEntry.arguments?.getString("orderNumber")?.toIntOrNull()
                            if (orderNumber != null) {
                                ViewOrderScreen(navController, orderRepository, orderNumber)
                            }
                        }
                        composable("dashboard") { DashboardScreen(orderRepository, this@MainActivity) }
                        composable("settings") { SettingsScreen(orderRepository) }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = false,
            onClick = { navController.navigate("home") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Add, contentDescription = "Take Order") },
            label = { Text("Order") },
            selected = false,
            onClick = { navController.navigate("take_order") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.List, contentDescription = "Dashboard") },
            label = { Text("Dashboard") },
            selected = false,
            onClick = { navController.navigate("dashboard") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") },
            selected = false,
            onClick = { navController.navigate("settings") }
        )
    }
}

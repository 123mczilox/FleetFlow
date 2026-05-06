package com.example.fleetflow.ui.Driver

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DriverDashboardScreen(
    onNavigateToRecordTrip: () -> Unit,
    onNavigateToMyVehicle: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Driver Dashboard", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onNavigateToRecordTrip, modifier = Modifier.fillMaxWidth()) {
            Text("Record Trip")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNavigateToMyVehicle, modifier = Modifier.fillMaxWidth()) {
            Text("My Vehicle")
        }
    }
}

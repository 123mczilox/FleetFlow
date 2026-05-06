package com.example.fleetflow.ui.Owner

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OwnerDashboardScreen(
    onNavigateToVehicles: () -> Unit,
    onNavigateToTrips: () -> Unit,
    onNavigateToMaintenance: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Owner Dashboard", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onNavigateToVehicles, modifier = Modifier.fillMaxWidth()) {
            Text("Manage Vehicles")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNavigateToTrips, modifier = Modifier.fillMaxWidth()) {
            Text("Trip Overview")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNavigateToMaintenance, modifier = Modifier.fillMaxWidth()) {
            Text("Maintenance")
        }
    }
}

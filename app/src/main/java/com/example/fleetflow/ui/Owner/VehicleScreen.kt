package com.example.fleetflow.ui.Owner

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun VehicleScreen(onNavigateToAddVehicle: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Manage Vehicles")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onNavigateToAddVehicle) {
                Text("Add New Vehicle")
            }
        }
    }
}

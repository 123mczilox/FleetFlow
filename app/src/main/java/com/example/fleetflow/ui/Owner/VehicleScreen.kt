package com.example.fleetflow.ui.Owner

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fleetflow.Data.Model.Vehicle
import com.example.fleetflow.Data.Service.SupabaseClient
import io.github.jan.supabase.auth.auth

@Composable
fun VehicleScreen(
    onNavigateToAddVehicle: () -> Unit,
    viewModel: OwnerViewModel = viewModel()
) {
    val vehicles by viewModel.vehicles.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val user = SupabaseClient.client.auth.currentUserOrNull()

    LaunchedEffect(user?.id) {
        user?.id?.let { viewModel.fetchVehicles(it) }
    }

    Scaffold(
        floatingActionButton = {
            Button(onClick = onNavigateToAddVehicle) {
                Text("Add Vehicle")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (vehicles.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No vehicles found", style = MaterialTheme.typography.bodyLarge)
                    Text("Click 'Add Vehicle' to get started", style = MaterialTheme.typography.bodySmall)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(vehicles) { vehicle ->
                        VehicleItem(vehicle)
                    }
                }
            }
        }
    }
}

@Composable
fun VehicleItem(vehicle: Vehicle) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Plate: ${vehicle.plate_number}", style = MaterialTheme.typography.titleMedium)
            Text(text = "Fleet: ${vehicle.fleet_number}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Route: ${vehicle.route ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

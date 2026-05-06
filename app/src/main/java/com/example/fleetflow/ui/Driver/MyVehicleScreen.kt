package com.example.fleetflow.ui.Driver

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fleetflow.Data.Service.SupabaseClient
import io.github.jan.supabase.auth.auth

@Composable
fun MyVehicleScreen(
    viewModel: DriverViewModel = viewModel()
) {
    val vehicle by viewModel.assignedVehicle.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val user = SupabaseClient.client.auth.currentUserOrNull()

    LaunchedEffect(user?.id) {
        user?.id?.let { viewModel.fetchAssignedVehicle(it) }
    }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            vehicle?.let {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Assigned Vehicle", style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Plate: ${it.plate_number}", style = MaterialTheme.typography.titleMedium)
                        Text(text = "Fleet: ${it.fleet_number}", style = MaterialTheme.typography.bodyLarge)
                        Text(text = "Route: ${it.route ?: "N/A"}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } ?: Text(text = "No vehicle assigned to you yet.")
        }
    }
}

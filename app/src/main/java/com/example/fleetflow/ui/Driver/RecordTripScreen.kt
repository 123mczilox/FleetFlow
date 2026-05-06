package com.example.fleetflow.ui.Driver

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fleetflow.Data.Model.Trip
import com.example.fleetflow.Data.Service.SupabaseClient
import io.github.jan.supabase.auth.auth

@Composable
fun RecordTripScreen(
    onTripRecorded: () -> Unit,
    viewModel: DriverViewModel = viewModel()
) {
    var tripsCount by remember { mutableStateOf("") }
    var revenue by remember { mutableStateOf("") }
    val user = SupabaseClient.client.auth.currentUserOrNull()
    val assignedVehicle by viewModel.assignedVehicle.collectAsState()

    LaunchedEffect(Unit) {
        user?.let { viewModel.fetchAssignedVehicle(it.id) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Record Daily Trip", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        assignedVehicle?.let { vehicle ->
            Text(text = "Vehicle: ${vehicle.plate_number}", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(16.dp))
        } ?: Text(text = "No vehicle assigned", color = MaterialTheme.colorScheme.error)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = tripsCount,
            onValueChange = { tripsCount = it },
            label = { Text("Number of Trips") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = revenue,
            onValueChange = { revenue = it },
            label = { Text("Total Revenue (Ksh)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val user_id = user?.id
                val vehicle_id = assignedVehicle?.id
                if (user_id != null && vehicle_id != null) {
                    val trip = Trip(
                        vehicle_id = vehicle_id,
                        driver_id = user_id,
                        trips_count = tripsCount.toIntOrNull() ?: 0,
                        revenue = revenue.toDoubleOrNull() ?: 0.0
                    )
                    viewModel.recordTrip(trip, onTripRecorded)
                }
            },
            enabled = assignedVehicle != null && tripsCount.isNotEmpty() && revenue.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Submit Records")
        }
    }
}

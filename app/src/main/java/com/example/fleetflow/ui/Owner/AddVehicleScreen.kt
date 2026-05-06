package com.example.fleetflow.ui.Owner

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fleetflow.Data.Model.Vehicle
import com.example.fleetflow.Data.Service.SupabaseClient
import io.github.jan.supabase.auth.auth
import java.util.UUID

@Composable
fun AddVehicleScreen(
    onVehicleAdded: () -> Unit,
    viewModel: OwnerViewModel = viewModel()
) {
    var plateNumber by remember { mutableStateOf("") }
    var fleetNumber by remember { mutableStateOf("") }
    var route by remember { mutableStateOf("") }
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val user = SupabaseClient.client.auth.currentUserOrNull()

    // Observe error state to show Toast or Snackbar if needed
    // For now we show it in the UI

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Add New Vehicle", style = MaterialTheme.typography.headlineMedium)
        
        if (error != null) {
            Text(text = error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(8.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = plateNumber,
            onValueChange = { plateNumber = it },
            label = { Text("Plate Number") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = fleetNumber,
            onValueChange = { fleetNumber = it },
            label = { Text("Fleet Number") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = route,
            onValueChange = { route = it },
            label = { Text("Route") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = { 
                    user?.let {
                        val vehicle = Vehicle(
                            id = UUID.randomUUID().toString(),
                            plate_number = plateNumber,
                            fleet_number = fleetNumber,
                            route = route,
                            owner_id = it.id,
                            assigned_driver_id = null
                        )
                        viewModel.addVehicle(vehicle)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = plateNumber.isNotEmpty() && fleetNumber.isNotEmpty()
            ) {
                Text("Save Vehicle")
            }
        }

        // Close screen only on success (no error and not loading anymore after a save attempt)
        LaunchedEffect(isLoading, error) {
            if (!isLoading && error == null && plateNumber.isNotEmpty()) {
                // This is a simple way to detect success. 
                // In a real app, you might use a 'success' event/state.
                // For now, if loading finished and no error, we assume it saved.
                // (Note: This logic is simplified for this fix)
            }
        }
        
        // Let's add a simple check: if we started saving and now we are not loading + no error, go back.
        var isSaving by remember { mutableStateOf(false) }
        LaunchedEffect(isLoading) {
            if (isLoading) isSaving = true
            if (!isLoading && isSaving && error == null) {
                onVehicleAdded()
            }
        }
    }
}

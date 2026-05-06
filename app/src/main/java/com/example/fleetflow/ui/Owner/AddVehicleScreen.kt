package com.example.fleetflow.ui.Owner

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddVehicleScreen(onVehicleAdded: () -> Unit) {
    var plateNumber by remember { mutableStateOf("") }
    var fleetNumber by remember { mutableStateOf("") }
    var route by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Add New Vehicle", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = plateNumber,
            onValueChange = { plateNumber = it },
            label = { Text("Plate Number") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = fleetNumber,
            onValueChange = { fleetNumber = it },
            label = { Text("Fleet Number") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = route,
            onValueChange = { route = it },
            label = { Text("Route") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = { 
                // Logic to save to Supabase will go here via ViewModel
                onVehicleAdded() 
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Vehicle")
        }
    }
}

package com.example.fleetflow.ui.Driver

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RecordTripScreen(onTripRecorded: () -> Unit) {
    var tripsCount by remember { mutableStateOf("") }
    var revenue by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Record Daily Trip", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

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
                // Logic to save trip to Supabase via ViewModel
                onTripRecorded()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Submit Records")
        }
    }
}

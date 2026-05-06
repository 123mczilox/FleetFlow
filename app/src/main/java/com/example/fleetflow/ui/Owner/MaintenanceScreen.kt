package com.example.fleetflow.ui.Owner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fleetflow.Data.Model.Maintenance
import com.example.fleetflow.Data.Service.SupabaseClient
import io.github.jan.supabase.auth.auth

@Composable
fun MaintenanceScreen(
    viewModel: OwnerViewModel = viewModel()
) {
    val logs by viewModel.maintenanceLogs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val user = SupabaseClient.client.auth.currentUserOrNull()

    LaunchedEffect(user?.id) {
        user?.id?.let { viewModel.fetchMaintenanceLogs(it) }
    }

    Scaffold { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(logs) { log ->
                        MaintenanceItem(log)
                    }
                }
            }
        }
    }
}

@Composable
fun MaintenanceItem(log: Maintenance) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Vehicle ID: ${log.vehicle_id}", style = MaterialTheme.typography.titleMedium)
            Text(text = "Date: ${log.date}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

package com.example.fleetflow.ui.Owner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fleetflow.Data.Model.Maintenance
import com.example.fleetflow.Data.Model.Vehicle
import com.example.fleetflow.Data.Service.SupabaseClient
import io.github.jan.supabase.auth.auth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceScreen(
    ownerId: String,
    viewModel: OwnerViewModel = viewModel()
) {
    val logs by viewModel.maintenanceLogs.collectAsState()
    val vehicles by viewModel.vehicles.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(ownerId) {
        viewModel.fetchOwnerData(ownerId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Maintenance History", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White
                ),
                modifier = Modifier.background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF6A11CB), Color(0xFF2575FC))
                    )
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF6A11CB),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Log")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8F9FA))
        ) {
            if (isLoading && logs.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF6A11CB)
                )
            } else if (logs.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No maintenance logs found", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(logs) { log ->
                        val vehicle = vehicles.find { it.id == log.vehicle_id }
                        MaintenanceItem(log, vehicle)
                    }
                }
            }

            if (showAddDialog) {
                AddMaintenanceDialog(
                    vehicles = vehicles,
                    onDismiss = { showAddDialog = false },
                    onConfirm = { vId: String, sType: String, cValue: Double, dText: String ->
                        val vehicle = vehicles.find { it.id == vId }
                        val newLog = Maintenance(
                            vehicle_id = vId,
                            owner_id = vehicle?.owner_id ?: ownerId,
                            service_type = sType,
                            cost = cValue,
                            description = dText,
                            date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                        )
                        viewModel.addMaintenanceLog(newLog, ownerId)
                        showAddDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun MaintenanceItem(log: Maintenance, vehicle: Vehicle?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF6A11CB).copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = null,
                    tint = Color(0xFF6A11CB),
                    modifier = Modifier.padding(12.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.service_type,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF2D3436)
                )
                Text(
                    text = vehicle?.plate_number ?: "Vehicle ID: ${log.vehicle_id.take(8)}",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                if (!log.description.isNullOrBlank()) {
                    Text(
                        text = log.description!!,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Ksh ${log.cost}",
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF2D3436),
                    fontSize = 15.sp
                )
                Text(
                    text = log.date ?: log.created_at?.take(10) ?: "",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMaintenanceDialog(
    vehicles: List<Vehicle>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, String) -> Unit
) {
    var selectedVehicleId by remember { mutableStateOf(vehicles.firstOrNull()?.id ?: "") }
    var serviceType by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Maintenance", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Vehicle Dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = vehicles.find { it.id == selectedVehicleId }?.plate_number ?: "Select Vehicle",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Vehicle") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        vehicles.forEach { vehicle ->
                            DropdownMenuItem(
                                text = { Text(vehicle.plate_number) },
                                onClick = {
                                    selectedVehicleId = vehicle.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = serviceType,
                    onValueChange = { serviceType = it },
                    label = { Text("Service Type (e.g. Oil Change)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Build, contentDescription = null) }
                )

                OutlinedTextField(
                    value = cost,
                    onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) cost = it },
                    label = { Text("Cost (Ksh)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) }
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedVehicleId, serviceType, cost.toDoubleOrNull() ?: 0.0, description) },
                enabled = selectedVehicleId.isNotEmpty() && serviceType.isNotEmpty() && cost.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A11CB)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Log")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

package com.example.fleetflow.ui.Owner

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fleetflow.Data.Model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriversManagementScreen(
    ownerId: String,
    viewModel: OwnerViewModel = viewModel()
) {
    val drivers by viewModel.drivers.collectAsState()
    val ownerVehicles by viewModel.vehicles.collectAsState()
    val allVehicles by viewModel.allVehicles.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(ownerId) {
        viewModel.fetchManagementData(ownerId)
    }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    var showAddDriverDialog by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Drivers Management", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDriverDialog = true },
                containerColor = Color(0xFF8E2DE2),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Driver")
            }
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading && drivers.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF8E2DE2)
                )
            } else if (drivers.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.People,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No Drivers Found",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Drivers must register an account with the invited email to appear in your list. You can invite them via SMS below.",
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { viewModel.fetchAvailableDrivers() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                        ) {
                            Text("Refresh List")
                        }
                        Button(
                            onClick = { showAddDriverDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E2DE2))
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Invite Driver")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(drivers) { driver ->
                        var showAssignDialog by remember { mutableStateOf(false) }
                        
                        // Check global assignment status
                        val globalAssignedVehicle = allVehicles.find { it.assigned_driver_id == driver.id }
                        val isAssignedToMe = ownerVehicles.any { it.id == globalAssignedVehicle?.id }
                        
                        DriverCard(
                            driver = driver,
                            assignedVehicle = globalAssignedVehicle?.plate_number,
                            isAssignedToMe = isAssignedToMe,
                            onAssignClick = { showAssignDialog = true }
                        )

                        if (showAssignDialog) {
                            AssignVehicleDialog(
                                vehicles = ownerVehicles.filter { it.assigned_driver_id == null || it.assigned_driver_id == driver.id },
                                onDismiss = { showAssignDialog = false },
                                onConfirm = { vehicleId, target ->
                                    val vehicle = ownerVehicles.find { it.id == vehicleId }
                                    if (vehicle != null) {
                                        viewModel.assignDriverWithTarget(vehicle, driver.id, target)
                                    }
                                    showAssignDialog = false
                                }
                            )
                        }
                    }
                }
            }
        }

        if (showAddDriverDialog) {
            AddDriverDialog(
                onDismiss = { showAddDriverDialog = false },
                onConfirm = { name, email, phone ->
                    // Trigger SMS Invitation
                    val message = "Hello $name, I would like to invite you to join FleetFlow as a driver. Please register using your email: $email. Download the app here: https://fleetflow.example.com/download"
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("smsto:$phone")
                        putExtra("sms_body", message)
                    }
                    context.startActivity(intent)
                    
                    viewModel.fetchAvailableDrivers()
                    showAddDriverDialog = false
                }
            )
        }
    }
}

@Composable
fun AddDriverDialog(onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Driver", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    placeholder = { Text("e.g. John Doe") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    placeholder = { Text("driver@example.com") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    placeholder = { Text("+254...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Text(
                    "Invitation will be sent. The driver must register with this email to appear in your list.",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    lineHeight = 16.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, email, phone) },
                enabled = name.isNotBlank() && email.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E2DE2))
            ) {
                Text("Invite Driver")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun DriverCard(driver: User, assignedVehicle: String?, isAssignedToMe: Boolean, onAssignClick: () -> Unit) {
    val context = LocalContext.current
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
            // Avatar
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = if (isAssignedToMe) 
                                listOf(Color(0xFF4CAF50), Color(0xFF2E7D32))
                            else 
                                listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = driver.full_name.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f) ) {
                Text(
                    text = driver.full_name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1A1C1E)
                )
                Text(
                    text = driver.email,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                
                if (assignedVehicle != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.DirectionsCar,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (isAssignedToMe) Color(0xFF4CAF50) else Color(0xFFD63031)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isAssignedToMe) "Assigned to you: $assignedVehicle" else "Assigned elsewhere: $assignedVehicle",
                            fontSize = 12.sp,
                            color = if (isAssignedToMe) Color(0xFF4CAF50) else Color(0xFFD63031),
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Available / Unassigned",
                        fontSize = 12.sp,
                        color = Color(0xFF8E2DE2),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Row {
                driver.phone_number?.let { phone ->
                    IconButton(onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                        context.startActivity(intent)
                    }) {
                        Icon(Icons.Default.Phone, contentDescription = "Call Driver", tint = Color(0xFF4CAF50))
                    }
                }
                
                IconButton(onClick = onAssignClick) {
                    Icon(
                        imageVector = if (isAssignedToMe) Icons.Default.Edit else Icons.Default.AddCircle,
                        contentDescription = "Assign Vehicle",
                        tint = Color(0xFF8E2DE2)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignVehicleDialog(
    vehicles: List<com.example.fleetflow.Data.Model.Vehicle>,
    onDismiss: () -> Unit,
    onConfirm: (String, Double) -> Unit
) {
    var selectedVehicleId by remember { mutableStateOf(vehicles.firstOrNull()?.id ?: "") }
    var dailyTarget by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assign Vehicle & Set Target", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (vehicles.isEmpty()) {
                    Text("No available vehicles.")
                } else {
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
                                    text = { Text("${vehicle.plate_number} (${vehicle.fleet_number})") },
                                    onClick = {
                                        selectedVehicleId = vehicle.id
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = dailyTarget,
                        onValueChange = { dailyTarget = it },
                        label = { Text("Daily Revenue Target (KSh)") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            if (vehicles.isNotEmpty()) {
                Button(onClick = { 
                    onConfirm(selectedVehicleId, dailyTarget.toDoubleOrNull() ?: 0.0) 
                }) {
                    Text("Assign & Save")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

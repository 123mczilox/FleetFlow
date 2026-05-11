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
import com.example.fleetflow.Data.Model.Vehicle
import com.example.fleetflow.Data.Service.SupabaseClient
import io.github.jan.supabase.auth.auth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleScreen(
    ownerId: String,
    onNavigateToAddVehicle: () -> Unit,
    viewModel: OwnerViewModel = viewModel()
) {
    val vehicles by viewModel.vehicles.collectAsState()
    val drivers by viewModel.drivers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(ownerId) {
        viewModel.fetchManagementData(ownerId)
    }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Fleet", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddVehicle,
                containerColor = Color(0xFF8E2DE2),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Vehicle")
            }
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (isLoading && vehicles.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF8E2DE2))
            } else if (vehicles.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.DirectionsCar,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No vehicles found", fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text("Add your first vehicle to start tracking", fontSize = 12.sp, color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(vehicles) { vehicle ->
                        val assignedDriver = drivers.find { it.id == vehicle.assigned_driver_id }
                        ModernVehicleItem(
                            vehicle = vehicle,
                            drivers = drivers,
                            assignedDriver = assignedDriver,
                            onAssignDriver = { driverId ->
                                viewModel.assignDriverToVehicle(vehicle.id, driverId, ownerId)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModernVehicleItem(
    vehicle: Vehicle,
    drivers: List<User>,
    assignedDriver: User?,
    onAssignDriver: (String) -> Unit
) {
    var showDriverMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val assignedDriverName = assignedDriver?.full_name ?: "Unassigned"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF8E2DE2).copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color(0xFF8E2DE2))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = vehicle.plate_number,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF1A1C1E)
                        )
                        Text(
                            text = "Fleet ID: ${vehicle.fleet_number}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    assignedDriver?.phone_number?.let { phone ->
                        IconButton(onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                            context.startActivity(intent)
                        }) {
                            Icon(
                                Icons.Default.Phone, 
                                contentDescription = "Call Driver", 
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Box {
                        TextButton(
                            onClick = { showDriverMenu = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = if (assignedDriverName == "Unassigned") Color.Red else Color(0xFF4CAF50))
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = assignedDriverName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        DropdownMenu(
                            expanded = showDriverMenu,
                            onDismissRequest = { showDriverMenu = false }
                        ) {
                            drivers.forEach { driver ->
                                DropdownMenuItem(
                                    text = { Text(driver.full_name) },
                                    onClick = {
                                        onAssignDriver(driver.id)
                                        showDriverMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFF1F3F4))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Route, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Route: ", fontSize = 14.sp, color = Color.Gray)
                    Text(text = vehicle.route ?: "Unassigned", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFE8F5E9)
                ) {
                    Text(
                        text = "Active",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                }
            }
        }
    }
}

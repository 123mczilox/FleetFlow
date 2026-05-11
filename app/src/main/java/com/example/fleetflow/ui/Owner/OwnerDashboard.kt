package com.example.fleetflow.ui.Owner

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fleetflow.Data.Model.Vehicle
import com.example.fleetflow.Data.Service.AuthService

import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerDashboardScreen(
    ownerId: String,
    onNavigateToVehicles: () -> Unit,
    onNavigateToTrips: () -> Unit,
    onNavigateToMaintenance: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToDrivers: (String) -> Unit,
    onLogout: () -> Unit,
    viewModel: OwnerViewModel = viewModel()
) {
    val vehicles by viewModel.vehicles.collectAsState()
    val totalVehicles by viewModel.totalVehicles.collectAsState()
    val todayRevenue by viewModel.todayRevenue.collectAsState()
    val activeTrips by viewModel.activeTrips.collectAsState()
    val serviceDue by viewModel.serviceDue.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val reports by viewModel.reports.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val pendingReports = reports.count { it.status == "pending" }
    
    val pullToRefreshState = rememberPullToRefreshState()

    LaunchedEffect(ownerId) {
        viewModel.fetchOwnerData(ownerId)
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { viewModel.fetchOwnerData(ownerId) },
            state = pullToRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header with Gradient
                item {
                    HeaderSection(
                        userName = currentUser?.full_name ?: "Loading...",
                        onLogout = onLogout
                    )
                }

                // Error Message
                if (error != null) {
                    item {
                        Surface(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            color = Color.Red.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = error!!,
                                color = Color.Red,
                                modifier = Modifier.padding(8.dp),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Stats Grid
                item {
                    StatsGrid(
                        totalVehicles = totalVehicles,
                        todayRevenue = todayRevenue,
                        activeTrips = activeTrips,
                        serviceDue = serviceDue + pendingReports
                    )
                }

                // Quick Actions
                item {
                    QuickActionsSection(
                        onAddVehicle = onNavigateToVehicles,
                        onTrips = onNavigateToTrips,
                        onMaintenance = onNavigateToMaintenance,
                        onReports = onNavigateToReports,
                        onDrivers = { onNavigateToDrivers(ownerId) }
                    )
                }

                // Fleet Overview Header
                item {
                    Text(
                        text = " Fleet Overview",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(16.dp)
                    )
                }

                // Empty State for Vehicles
                if (vehicles.isEmpty() && !isLoading) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                modifier = Modifier.size(64.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = Color.Gray.copy(alpha = 0.05f)
                            ) {
                                Icon(
                                    Icons.Default.DirectionsCar, 
                                    contentDescription = null, 
                                    tint = Color.Gray.copy(alpha = 0.5f), 
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No vehicles registered yet", fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                            Text("Add your first vehicle to start tracking", fontSize = 12.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = onNavigateToVehicles,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E2DE2))
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add Vehicle")
                            }
                        }
                    }
                } else {
                    items(vehicles) { vehicle ->
                        VehicleOverviewItem(vehicle)
                    }
                }
                
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun HeaderSection(userName: String, onLogout: () -> Unit) {
    val authService = remember { AuthService() }
    val scope = rememberCoroutineScope()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))
                ),
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            )
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "FleetFlow",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Owner Dashboard",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = userName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Fleet Owner",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = {
                    scope.launch {
                        authService.signOut()
                        onLogout()
                    }
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Logout",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun StatsGrid(
    totalVehicles: Int,
    todayRevenue: Double,
    activeTrips: Int,
    serviceDue: Int
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            StatCard(
                title = "Total Vehicles",
                value = totalVehicles.toString(),
                icon = Icons.Default.DirectionsCar,
                iconColor = Color(0xFF8E2DE2),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            StatCard(
                title = "Today's Revenue",
                value = "KES ${String.format(Locale.getDefault(), "%.1f", todayRevenue / 1000)}K",
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                iconColor = Color(0xFF00B894),
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            StatCard(
                title = "Active Trips",
                value = activeTrips.toString(),
                icon = Icons.Default.LocalShipping,
                iconColor = Color(0xFFFD9644),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            StatCard(
                title = "Service Due",
                value = serviceDue.toString(),
                icon = Icons.Default.Warning,
                iconColor = Color(0xFFD63031),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 12.sp, color = Color.Gray)
                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = iconColor
                )
            }
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(12.dp),
                color = iconColor.copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun QuickActionsSection(
    onAddVehicle: () -> Unit,
    onTrips: () -> Unit,
    onMaintenance: () -> Unit,
    onReports: () -> Unit,
    onDrivers: () -> Unit
) {
    Card(
        modifier = Modifier.padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "⚡ Quick Actions",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                QuickActionButton("Add Vehicle", Icons.Default.Add, Color(0xFF8E2DE2), onAddVehicle)
                QuickActionButton("Trips", Icons.Default.Route, Color(0xFF00B894), onTrips)
                QuickActionButton("Maintenance", Icons.Default.Build, Color(0xFFFD9644), onMaintenance)
                QuickActionButton("Drivers", Icons.Default.Person, Color(0xFF4A90E2), onDrivers)
                QuickActionButton("Reports", Icons.Default.Report, Color(0xFFD63031), onReports)
            }
        }
    }
}

@Composable
fun QuickActionButton(label: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier
                .size(56.dp)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(12.dp),
            color = color.copy(alpha = 0.1f),
            border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.padding(16.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, fontSize = 10.sp, color = Color.Gray)
    }
}

@Composable
fun VehicleOverviewItem(vehicle: Vehicle) {
    val status = vehicle.status ?: "active" // Fallback if status is null
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF8E2DE2).copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = Color(0xFF8E2DE2),
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = vehicle.plate_number, fontWeight = FontWeight.Bold)
                Text(text = vehicle.route ?: "No route assigned", fontSize = 12.sp, color = Color.Gray)
            }
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = (if(status == "active") Color(0xFF00B894) else Color.Gray).copy(alpha = 0.1f),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = status.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                    color = if(status == "active") Color(0xFF00B894) else Color.Gray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

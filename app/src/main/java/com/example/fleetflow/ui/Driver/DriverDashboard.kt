package com.example.fleetflow.ui.Driver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fleetflow.Data.Model.Vehicle
import com.example.fleetflow.Data.Service.SupabaseClient
import io.github.jan.supabase.auth.auth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverDashboardScreen(
    driverId: String,
    onNavigateToRecordTrip: (String) -> Unit,
    onNavigateToMyVehicle: (String) -> Unit,
    onNavigateToReport: (String) -> Unit,
    onLogout: () -> Unit,
    viewModel: DriverViewModel = viewModel()
) {
    val assignedVehicle by viewModel.assignedVehicle.collectAsState()
    val totalTrips by viewModel.totalTrips.collectAsState()
    val totalRevenue by viewModel.totalRevenue.collectAsState()
    val todayRevenue by viewModel.todayRevenue.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    // We'll use the driverId passed from navigation instead of relying on auth client state
    
    val pullToRefreshState = rememberPullToRefreshState()

    LaunchedEffect(driverId) {
        viewModel.fetchAssignedVehicle(driverId)
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { viewModel.fetchAssignedVehicle(driverId) },
            state = pullToRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                item {
                    DriverHeaderSection(
                        userName = assignedVehicle?.assigned_driver_id ?: "Driver", // Or better, fetch driver name from VM
                        onLogout = onLogout
                    )
                }

                // Stats
                item {
                    val dailyTarget = assignedVehicle?.daily_target ?: 0.0
                    val status = when {
                        dailyTarget <= 0 -> "No Target Set"
                        todayRevenue >= dailyTarget -> "Excellent"
                        todayRevenue >= dailyTarget * 0.8 -> "Good"
                        todayRevenue >= dailyTarget * 0.5 -> "Fair"
                        else -> "Low"
                    }
                    val statusColor = when(status) {
                        "Excellent" -> Color(0xFF4CAF50)
                        "Good" -> Color(0xFF8BC34A)
                        "Fair" -> Color(0xFFFFC107)
                        "Low" -> Color(0xFFF44336)
                        else -> Color.Gray
                    }

                    Column(modifier = Modifier.padding(16.dp)) {
                        DriverStatsGrid(
                            trips = totalTrips.toString(),
                            revenue = String.format("%.1fK", totalRevenue / 1000),
                            today = String.format("%.1fK", todayRevenue / 1000)
                        )
                        
                        if (dailyTarget > 0) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.1f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Daily Performance", fontSize = 12.sp, color = Color.Gray)
                                        Text(status, fontWeight = FontWeight.Bold, color = statusColor)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Target: KSh $dailyTarget", fontSize = 12.sp, color = Color.Gray)
                                        Text("Progress: ${((todayRevenue / dailyTarget) * 100).toInt()}%", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // My Assigned Vehicle
                item {
                    AssignedVehicleCard(assignedVehicle) { onNavigateToMyVehicle(driverId) }
                }

                // Record New Trip Button
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Button(
                            onClick = { onNavigateToRecordTrip(driverId) },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF008080))
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Record Trip", fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = { onNavigateToReport(driverId) },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD63031))
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Report, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Report", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DriverHeaderSection(userName: String, onLogout: () -> Unit) {
    val authService = remember { com.example.fleetflow.Data.Service.AuthService() }
    val scope = rememberCoroutineScope()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF008080), Color(0xFF00CED1))
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
                        text = "Driver Dashboard",
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
                        text = "Driver",
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
fun DriverStatsGrid(trips: String, revenue: String, today: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        DriverStatCard("Trips", trips, Icons.Default.Route, Color(0xFF00CED1), Modifier.weight(1f))
        Spacer(modifier = Modifier.width(12.dp))
        DriverStatCard("Revenue", revenue, Icons.AutoMirrored.Filled.TrendingUp, Color(0xFF00B894), Modifier.weight(1f))
        Spacer(modifier = Modifier.width(12.dp))
        DriverStatCard("Today", today, Icons.Default.AccessTime, Color(0xFFF09819), Modifier.weight(1f))
    }
}

@Composable
fun DriverStatCard(title: String, value: String, icon: ImageVector, iconColor: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(text = title, fontSize = 12.sp, color = Color.Gray)
                Surface(
                    modifier = Modifier.size(28.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = iconColor.copy(alpha = 0.1f)
                ) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.padding(6.dp))
                }
            }
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AssignedVehicleCard(vehicle: Vehicle?, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF008080).copy(alpha = 0.1f)
                ) {
                    Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Color(0xFF008080), modifier = Modifier.padding(8.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = "My Assigned Vehicle", fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (vehicle != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF0F8FF),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFB0E0E6))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(text = "Plate Number", fontSize = 10.sp, color = Color(0xFF008080))
                                Text(text = vehicle.plate_number, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "Fleet #", fontSize = 10.sp, color = Color(0xFF008080))
                                Text(text = vehicle.fleet_number, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Route, contentDescription = null, tint = Color(0xFFD63031), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Route: ", fontSize = 12.sp, color = Color.Gray)
                            Text(text = vehicle.route ?: "Not Assigned", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF008080))
                        }
                    }
                }
            } else {
                Text(text = "No vehicle assigned yet.", color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp))
            }
        }
    }
}

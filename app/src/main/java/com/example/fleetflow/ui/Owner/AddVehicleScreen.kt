package com.example.fleetflow.ui.Owner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fleetflow.Data.Model.Vehicle
import com.example.fleetflow.Data.Service.SupabaseClient
import io.github.jan.supabase.auth.auth
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVehicleScreen(
    ownerId: String,
    onVehicleAdded: () -> Unit,
    viewModel: OwnerViewModel = viewModel()
) {
    var plateNumber by rememberSaveable { mutableStateOf("") }
    var fleetNumber by rememberSaveable { mutableStateOf("") }
    var route by rememberSaveable { mutableStateOf("Nairobi - Thika") }
    var isExpanded by remember { mutableStateOf(false) }
    val routes = listOf("Nairobi - Thika", "Nairobi - Nakuru", "Nairobi - Mombasa", "Nairobi - Kisumu")

    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add New Vehicle", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF8E2DE2)
                ),
                navigationIcon = {
                    Surface(
                        modifier = Modifier.padding(start = 12.dp).size(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Color.White, modifier = Modifier.padding(8.dp))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ... (Card content remains the same)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(text = "Plate Number *", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = plateNumber,
                        onValueChange = { plateNumber = it },
                        placeholder = { Text("e.g. KCA 234X") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(text = "Fleet Number", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = fleetNumber,
                        onValueChange = { fleetNumber = it },
                        placeholder = { Text("e.g. MT-01") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "Assigned Route *", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    ExposedDropdownMenuBox(
                        expanded = isExpanded,
                        onExpandedChange = { isExpanded = !isExpanded }
                    ) {
                        OutlinedTextField(
                            value = route,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, true).fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = isExpanded,
                            onDismissRequest = { isExpanded = false }
                        ) {
                            routes.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        route = selectionOption
                                        isExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                CircularProgressIndicator(color = Color(0xFF8E2DE2))
            } else {
                Button(
                    onClick = {
                        val vehicle = Vehicle(
                            id = UUID.randomUUID().toString(),
                            plate_number = plateNumber,
                            fleet_number = fleetNumber,
                            route = route,
                            owner_id = ownerId,
                            assigned_driver_id = null
                        )
                        viewModel.addVehicle(vehicle)
                        onVehicleAdded()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E2DE2)),
                    enabled = plateNumber.isNotEmpty() && route.isNotEmpty()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Vehicle to Fleet", fontWeight = FontWeight.Bold)
                    }
                }
            }

            error?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

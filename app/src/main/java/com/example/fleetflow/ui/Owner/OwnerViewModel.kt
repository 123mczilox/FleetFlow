package com.example.fleetflow.ui.Owner

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fleetflow.Data.Model.Report
import com.example.fleetflow.Data.Repository.ReportRepository
import com.example.fleetflow.Data.Service.ReportService
import com.example.fleetflow.Data.Model.Maintenance
import com.example.fleetflow.Data.Repository.MaintenanceRepository
import com.example.fleetflow.Data.Service.MaintenanceService
import com.example.fleetflow.Data.Model.Trip
import com.example.fleetflow.Data.Repository.TripRepository
import com.example.fleetflow.Data.Service.TripService
import com.example.fleetflow.Data.Model.Vehicle
import com.example.fleetflow.Data.Repository.VehicleRepository
import com.example.fleetflow.Data.Service.VehicleService
import com.example.fleetflow.Data.Repository.DriverRepository
import com.example.fleetflow.Data.Service.DriverService
import com.example.fleetflow.Data.Model.User
import com.example.fleetflow.Data.Service.AuthService
import com.example.fleetflow.Data.Service.SupabaseClient
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OwnerViewModel : ViewModel() {
    private val vehicleRepository = VehicleRepository(VehicleService())
    private val tripRepository = TripRepository(TripService())
    private val maintenanceRepository = MaintenanceRepository(MaintenanceService())
    private val reportRepository = ReportRepository(ReportService())
    private val driverRepository = DriverRepository(DriverService())
    private val authService = AuthService()

    private val _vehicles = MutableStateFlow<List<Vehicle>>(emptyList())
    val vehicles: StateFlow<List<Vehicle>> = _vehicles

    private val _allVehicles = MutableStateFlow<List<Vehicle>>(emptyList())
    val allVehicles: StateFlow<List<Vehicle>> = _allVehicles

    private val _trips = MutableStateFlow<List<Trip>>(emptyList())
    val trips: StateFlow<List<Trip>> = _trips

    private val _maintenanceLogs = MutableStateFlow<List<Maintenance>>(emptyList())
    val maintenanceLogs: StateFlow<List<Maintenance>> = _maintenanceLogs

    private val _drivers = MutableStateFlow<List<User>>(emptyList())
    val drivers: StateFlow<List<User>> = _drivers

    private val _reports = MutableStateFlow<List<Report>>(emptyList())
    val reports: StateFlow<List<Report>> = _reports

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Stats
    val totalVehicles = _vehicles.map { it.size }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)
    
    val todayRevenue = _trips.map { tripList ->
        try {
            // Safer date check that doesn't rely solely on LocalDate
            val todayPrefix = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
            tripList.filter { it.created_at?.startsWith(todayPrefix) == true }.sumOf { it.revenue }
        } catch (e: Exception) {
            Log.e("OwnerViewModel", "Error calculating revenue", e)
            0.0
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0.0)

    val activeTrips = _trips.map { tripList ->
        tripList.count { !it.is_locked }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)

    val serviceDue = _maintenanceLogs.map { it.size }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)

    fun fetchOwnerData(ownerId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            Log.d("OwnerViewModel", "Fetching owner data for ID: $ownerId")
            try {
                // Fetch profile first
                val profile = authService.getUserProfile(ownerId)
                _currentUser.value = profile
                Log.d("OwnerViewModel", "Profile fetched: ${profile.email}, Role: ${profile.role}")

                // Fetch data belonging to this owner
                val vehiclesJob = async { 
                    Log.d("OwnerViewModel", "Fetching vehicles...")
                    vehicleRepository.getVehiclesByOwner(ownerId) 
                }
                val tripsJob = async { 
                    Log.d("OwnerViewModel", "Fetching trips...")
                    tripRepository.getTripsByOwner(ownerId) 
                }
                val maintenanceJob = async { 
                    Log.d("OwnerViewModel", "Fetching maintenance...")
                    maintenanceRepository.getMaintenanceByOwner(ownerId) 
                }
                val reportsJob = async { 
                    Log.d("OwnerViewModel", "Fetching reports...")
                    reportRepository.getReportsByOwner(ownerId) 
                }
                val driversJob = async { 
                    Log.d("OwnerViewModel", "Fetching drivers...")
                    try { 
                        driverRepository.getAllDrivers() 
                    } catch (e: Exception) { 
                        Log.e("OwnerViewModel", "Error fetching drivers", e)
                        emptyList() 
                    } 
                }

                val vehicles = vehiclesJob.await()
                val trips = tripsJob.await()
                val maintenance = maintenanceJob.await()
                val reports = reportsJob.await()
                val drivers = driversJob.await()

                Log.d("OwnerViewModel", "Data received - Vehicles: ${vehicles.size}, Trips: ${trips.size}, Maintenance: ${maintenance.size}, Reports: ${reports.size}, Drivers: ${drivers.size}")

                _vehicles.value = vehicles
                _trips.value = trips
                _maintenanceLogs.value = maintenance.sortedByDescending { it.date }
                _reports.value = reports
                _drivers.value = drivers

                _error.value = null
                
            } catch (e: Exception) {
                Log.e("OwnerViewModel", "Error fetching dashboard data", e)
                _error.value = "Error fetching dashboard: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private var isRealtimeSetup = false
    
    fun fetchManagementData(ownerId: String) {
        fetchOwnerData(ownerId)
        if (!isRealtimeSetup) {
            setupRealtimeUpdates(ownerId)
            isRealtimeSetup = true
        }
    }

    fun fetchAvailableDrivers() {
        viewModelScope.launch {
            try {
                _drivers.value = driverRepository.getAllDrivers()
            } catch (e: Exception) {
                _error.value = "Error fetching drivers: ${e.message}"
            }
        }
    }

    fun fetchAllTrips(ownerId: String) {
        viewModelScope.launch {
            try {
                _trips.value = tripRepository.getTripsByOwner(ownerId)
            } catch (e: Exception) {
                _error.value = "Error fetching trips: ${e.message}"
            }
        }
    }

    fun assignDriverToVehicle(vehicleId: String, driverId: String, ownerId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val vehicle = _vehicles.value.find { it.id == vehicleId }
                if (vehicle != null) {
                    val updatedVehicle = vehicle.copy(assigned_driver_id = driverId)
                    vehicleRepository.updateVehicle(updatedVehicle)
                    fetchOwnerData(ownerId)
                }
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error assigning driver: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun setupRealtimeUpdates(ownerId: String) {
        try {
            Log.d("OwnerViewModel", "Setting up realtime updates for owner: $ownerId")
            val channel = SupabaseClient.client.channel("owner_dashboard_$ownerId")
            
            // Listen for changes in trips
            channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "trips"
            }.onEach { action ->
                Log.d("OwnerViewModel", "Realtime update: trips changed")
                fetchOwnerData(ownerId) 
            }.launchIn(viewModelScope)

            // Listen for changes in reports
            channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "reports"
            }.onEach { action ->
                Log.d("OwnerViewModel", "Realtime update: reports changed")
                fetchOwnerData(ownerId)
            }.launchIn(viewModelScope)

            viewModelScope.launch {
                try {
                    channel.subscribe()
                    Log.d("OwnerViewModel", "Subscribed to realtime channel")
                } catch (e: Exception) {
                    Log.e("OwnerViewModel", "Realtime subscription failed - Check Supabase publication settings", e)
                }
            }
        } catch (e: Exception) {
            Log.e("OwnerViewModel", "SetupRealtimeUpdates Error: ${e.message}")
        }
    }

    fun addVehicle(vehicle: Vehicle) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                vehicleRepository.addVehicle(vehicle)
                fetchOwnerData(vehicle.owner_id)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to add vehicle"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun assignDriverWithTarget(vehicle: Vehicle, driverId: String, dailyTarget: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                vehicleRepository.clearDriverAssignment(driverId)
                val updatedVehicle = vehicle.copy(
                    assigned_driver_id = driverId,
                    daily_target = dailyTarget
                )
                vehicleRepository.updateVehicle(updatedVehicle)
                fetchOwnerData(vehicle.owner_id)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error assigning driver: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addMaintenanceLog(log: Maintenance, ownerId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                maintenanceRepository.addMaintenanceLog(log)
                fetchOwnerData(ownerId)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to add maintenance log"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

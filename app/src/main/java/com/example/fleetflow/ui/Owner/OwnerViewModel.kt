package com.example.fleetflow.ui.Owner

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
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

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
        val today = LocalDate.now().toString()
        tripList.filter { it.created_at?.startsWith(today) == true }.sumOf { it.revenue }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0.0)

    val activeTrips = _trips.map { tripList ->
        tripList.filter { !it.is_locked }.size
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)

    val serviceDue = _maintenanceLogs.map { it.size }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)

    fun fetchOwnerData(ownerId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Fetch profile first
                _currentUser.value = authService.getUserProfile(ownerId)

                // Fetch vehicles belonging to this owner
                val fetchedVehicles = vehicleRepository.getVehiclesByOwner(ownerId)
                _vehicles.value = fetchedVehicles
                
                // Fetch all drivers and all vehicles (for global assignment status)
                val driversList = try { driverRepository.getAllDrivers() } catch (e: Exception) { emptyList() }
                _drivers.value = driversList
                
                val allVehList = try { vehicleRepository.getAllVehicles() } catch (e: Exception) { emptyList() }
                _allVehicles.value = allVehList

                if (fetchedVehicles.isNotEmpty()) {
                    val vehicleIds = fetchedVehicles.map { it.id }

                    val tripsJob = async { 
                        try { tripRepository.getTripsByVehicles(vehicleIds) } catch (e: Exception) { emptyList() }
                    }
                    val maintenanceJob = async { 
                        try { maintenanceRepository.getMaintenanceByVehicles(vehicleIds) } catch (e: Exception) { emptyList() }
                    }
                    val reportsJob = async { 
                        try { reportRepository.getReportsByOwner(ownerId) } catch (e: Exception) { emptyList() }
                    }

                    _trips.value = tripsJob.await()
                    _maintenanceLogs.value = maintenanceJob.await().sortedByDescending { it.date }
                    _reports.value = reportsJob.await()
                }

                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error fetching dashboard: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchVehicles(ownerId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _vehicles.value = vehicleRepository.getVehiclesByOwner(ownerId)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error fetching vehicles: ${e.message}"
            } finally {
                _isLoading.value = false
            }
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

    fun fetchManagementData(ownerId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Fetch in parallel
                val driversJob = async { driverRepository.getAllDrivers() }
                val ownerVehiclesJob = async { vehicleRepository.getVehiclesByOwner(ownerId) }
                val allVehiclesJob = async { vehicleRepository.getAllVehicles() }
                
                _drivers.value = driversJob.await()
                _vehicles.value = ownerVehiclesJob.await()
                _allVehicles.value = allVehiclesJob.await()
                
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to load management data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchAvailableDrivers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _drivers.value = driverRepository.getAllDrivers()
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error fetching drivers: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun assignDriverToVehicle(vehicle: Vehicle, driverId: String) {
        assignDriverWithTarget(vehicle, driverId, vehicle.daily_target)
    }

    fun assignDriverWithTarget(vehicle: Vehicle, driverId: String, dailyTarget: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Clear any previous assignment for this driver from ANY vehicle in the system
                vehicleRepository.clearDriverAssignment(driverId)
                
                // 2. Assign to the new vehicle
                val updatedVehicle = vehicle.copy(
                    assigned_driver_id = driverId,
                    daily_target = dailyTarget
                )
                vehicleRepository.updateVehicle(updatedVehicle)
                
                // 3. Refresh all owner data to ensure consistency
                fetchOwnerData(vehicle.owner_id)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error assigning driver: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchAllTrips(ownerId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val ownerVehicles = vehicleRepository.getVehiclesByOwner(ownerId)
                if (ownerVehicles.isNotEmpty()) {
                    val vehicleIds = ownerVehicles.map { it.id }
                    _trips.value = tripRepository.getTripsByVehicles(vehicleIds)
                } else {
                    _trips.value = emptyList()
                }
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error fetching trips: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchMaintenanceLogs(ownerId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val ownerVehicles = vehicleRepository.getVehiclesByOwner(ownerId)
                if (ownerVehicles.isNotEmpty()) {
                    val vehicleIds = ownerVehicles.map { it.id }
                    val allLogs = maintenanceRepository.getMaintenanceByVehicles(vehicleIds)
                    _maintenanceLogs.value = allLogs.sortedByDescending { it.date }
                } else {
                    _maintenanceLogs.value = emptyList()
                }
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error fetching maintenance: ${e.message}"
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
                fetchMaintenanceLogs(ownerId)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to add maintenance log: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

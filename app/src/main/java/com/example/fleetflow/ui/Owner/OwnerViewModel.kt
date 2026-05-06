package com.example.fleetflow.ui.Owner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fleetflow.Data.Model.Maintenance
import com.example.fleetflow.Data.Repository.MaintenanceRepository
import com.example.fleetflow.Data.Service.MaintenanceService
import com.example.fleetflow.Data.Model.Trip
import com.example.fleetflow.Data.Repository.TripRepository
import com.example.fleetflow.Data.Service.TripService
import com.example.fleetflow.Data.Model.Vehicle
import com.example.fleetflow.Data.Repository.VehicleRepository
import com.example.fleetflow.Data.Service.VehicleService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OwnerViewModel : ViewModel() {
    private val vehicleRepository = VehicleRepository(VehicleService())

    private val _vehicles = MutableStateFlow<List<Vehicle>>(emptyList())
    val vehicles: StateFlow<List<Vehicle>> = _vehicles

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

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

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun addVehicle(vehicle: Vehicle) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                vehicleRepository.addVehicle(vehicle)
                fetchVehicles(vehicle.owner_id)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to add vehicle"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private val tripRepository = TripRepository(TripService())

    private val _trips = MutableStateFlow<List<Trip>>(emptyList())
    val trips: StateFlow<List<Trip>> = _trips

    private val maintenanceRepository = MaintenanceRepository(MaintenanceService())

    private val _maintenanceLogs = MutableStateFlow<List<Maintenance>>(emptyList())
    val maintenanceLogs: StateFlow<List<Maintenance>> = _maintenanceLogs

    fun fetchAllTrips(ownerId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val ownerVehicles = vehicleRepository.getVehiclesByOwner(ownerId)
                val allTrips = mutableListOf<Trip>()
                ownerVehicles.forEach { vehicle ->
                    allTrips.addAll(tripRepository.getTripsByVehicle(vehicle.id))
                }
                _trips.value = allTrips
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
                val allLogs = mutableListOf<Maintenance>()
                ownerVehicles.forEach { vehicle ->
                    allLogs.addAll(maintenanceRepository.getMaintenanceLogs(vehicle.id))
                }
                _maintenanceLogs.value = allLogs
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error fetching maintenance: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

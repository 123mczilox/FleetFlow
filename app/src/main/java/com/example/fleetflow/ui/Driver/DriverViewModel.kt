package com.example.fleetflow.ui.Driver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fleetflow.Data.Model.Trip
import com.example.fleetflow.Data.Model.Vehicle
import com.example.fleetflow.Data.Repository.TripRepository
import com.example.fleetflow.Data.Repository.VehicleRepository
import com.example.fleetflow.Data.Service.TripService
import com.example.fleetflow.Data.Service.VehicleService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DriverViewModel : ViewModel() {
    private val tripRepository = TripRepository(TripService())
    private val vehicleRepository = VehicleRepository(VehicleService())

    private val _assignedVehicle = MutableStateFlow<Vehicle?>(null)
    val assignedVehicle: StateFlow<Vehicle?> = _assignedVehicle

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fetchAssignedVehicle(driverId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _assignedVehicle.value = vehicleRepository.getVehicleByDriver(driverId)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to fetch vehicle: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun recordTrip(trip: Trip, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                tripRepository.recordTrip(trip)
                _error.value = null
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Failed to record trip: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

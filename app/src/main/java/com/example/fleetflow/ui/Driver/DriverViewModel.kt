package com.example.fleetflow.ui.Driver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fleetflow.Data.Model.Report
import com.example.fleetflow.Data.Model.Trip
import com.example.fleetflow.Data.Model.Vehicle
import com.example.fleetflow.Data.Repository.ReportRepository
import com.example.fleetflow.Data.Repository.TripRepository
import com.example.fleetflow.Data.Repository.VehicleRepository
import com.example.fleetflow.Data.Service.ReportService
import com.example.fleetflow.Data.Service.TripService
import com.example.fleetflow.Data.Service.VehicleService
import com.example.fleetflow.Data.Model.Maintenance
import com.example.fleetflow.Data.Repository.MaintenanceRepository
import com.example.fleetflow.Data.Service.MaintenanceService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

class DriverViewModel : ViewModel() {
    private val tripRepository = TripRepository(TripService())
    private val vehicleRepository = VehicleRepository(VehicleService())
    private val reportRepository = ReportRepository(ReportService())
    private val maintenanceRepository = MaintenanceRepository(MaintenanceService())

    private val _assignedVehicle = MutableStateFlow<Vehicle?>(null)
    val assignedVehicle: StateFlow<Vehicle?> = _assignedVehicle

    private val _maintenanceLogs = MutableStateFlow<List<Maintenance>>(emptyList())
    val maintenanceLogs: StateFlow<List<Maintenance>> = _maintenanceLogs

    private val _trips = MutableStateFlow<List<Trip>>(emptyList())
    val trips: StateFlow<List<Trip>> = _trips

    private val _reports = MutableStateFlow<List<Report>>(emptyList())
    val reports: StateFlow<List<Report>> = _reports

    val totalTrips = _trips.map { it.size }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)
    
    val totalRevenue = _trips.map { it.sumOf { trip -> trip.revenue } }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0.0)

    val todayRevenue = _trips.map { tripList ->
        val today = LocalDate.now().toString()
        tripList.filter { it.created_at?.startsWith(today) == true }.sumOf { it.revenue }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0.0)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fetchAssignedVehicle(driverId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val vehicle = vehicleRepository.getVehicleByDriver(driverId)
                _assignedVehicle.value = vehicle
                vehicle?.let {
                    _trips.value = tripRepository.getTripsByVehicle(it.id)
                    _reports.value = reportRepository.getReportsByDriver(driverId)
                    _maintenanceLogs.value = maintenanceRepository.getMaintenanceLogs(it.id)
                }
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

    fun submitReport(report: Report, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                reportRepository.createReport(report)
                _error.value = null
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Failed to submit report: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

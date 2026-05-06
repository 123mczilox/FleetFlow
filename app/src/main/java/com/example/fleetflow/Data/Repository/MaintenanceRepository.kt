package com.example.fleetflow.Data.Repository

import com.example.fleetflow.Data.Model.Maintenance
import com.example.fleetflow.Data.Service.MaintenanceService

class MaintenanceRepository(private val maintenanceService: MaintenanceService) {
    suspend fun getMaintenanceLogs(vehicleId: String) = maintenanceService.getMaintenanceLogs(vehicleId)
    suspend fun addMaintenanceLog(log: Maintenance) = maintenanceService.addMaintenanceLog(log)
}

package com.example.fleetflow.Data.Repository

import com.example.fleetflow.Data.Model.User
import com.example.fleetflow.Data.Service.DriverService

class DriverRepository(private val driverService: DriverService = DriverService()) {
    suspend fun getAllDrivers() = driverService.getAllDrivers()
    suspend fun getDriverById(driverId: String) = driverService.getDriverById(driverId)
}

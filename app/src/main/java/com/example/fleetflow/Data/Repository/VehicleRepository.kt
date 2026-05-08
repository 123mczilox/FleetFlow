package com.example.fleetflow.Data.Repository

import com.example.fleetflow.Data.Model.Vehicle
import com.example.fleetflow.Data.Service.VehicleService

class VehicleRepository(private val vehicleService: VehicleService) {
    suspend fun addVehicle(vehicle: Vehicle) = vehicleService.addVehicle(vehicle)
    suspend fun getVehiclesByOwner(ownerId: String) = vehicleService.getVehiclesByOwner(ownerId)
    suspend fun getVehicleByDriver(driverId: String) = vehicleService.getVehicleByDriver(driverId)
    suspend fun updateVehicle(vehicle: Vehicle) = vehicleService.updateVehicle(vehicle)
}

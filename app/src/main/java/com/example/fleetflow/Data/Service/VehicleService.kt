package com.example.fleetflow.Data.Service

import com.example.fleetflow.Data.Model.Vehicle
import io.github.jan.supabase.postgrest.postgrest

class VehicleService {
    private val client = SupabaseClient.client

    suspend fun addVehicle(vehicle: Vehicle) {
        client.postgrest["vehicles"].insert(vehicle)
    }

    suspend fun updateVehicle(vehicle: Vehicle) {
        client.postgrest["vehicles"].update(vehicle) {
            filter {
                eq("id", vehicle.id)
            }
        }
    }

    suspend fun getVehiclesByOwner(ownerId: String): List<Vehicle> {
        return client.postgrest["vehicles"].select {
            filter {
                eq("owner_id", ownerId)
            }
        }.decodeList<Vehicle>()
    }

    suspend fun getVehicleByDriver(driverId: String): Vehicle? {
        return client.postgrest["vehicles"].select {
            filter {
                eq("assigned_driver_id", driverId)
            }
        }.decodeSingleOrNull<Vehicle>()
    }

    suspend fun getAllVehicles(): List<Vehicle> {
        return client.postgrest["vehicles"].select().decodeList<Vehicle>()
    }

    suspend fun clearDriverAssignment(driverId: String) {
        val vehicle = getVehicleByDriver(driverId)
        vehicle?.let {
            val updated = it.copy(assigned_driver_id = null)
            updateVehicle(updated)
        }
    }
}

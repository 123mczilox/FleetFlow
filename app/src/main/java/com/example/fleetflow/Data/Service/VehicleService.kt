package com.example.fleetflow.Data.Service

import com.example.fleetflow.Data.Model.Vehicle
import io.github.jan.supabase.postgrest.postgrest

class VehicleService {
    private val client = SupabaseClient.client

    suspend fun addVehicle(vehicle: Vehicle) {
        client.postgrest["vehicles"].insert(vehicle)
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
}

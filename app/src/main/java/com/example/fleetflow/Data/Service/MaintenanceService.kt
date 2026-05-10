package com.example.fleetflow.Data.Service

import com.example.fleetflow.Data.Model.Maintenance
import io.github.jan.supabase.postgrest.postgrest

class MaintenanceService {
    private val client = SupabaseClient.client

    suspend fun getMaintenanceLogs(vehicleId: String): List<Maintenance> {
        return client.postgrest["maintenance"].select {
            filter {
                eq("vehicle_id", vehicleId)
            }
        }.decodeList<Maintenance>()
    }

    suspend fun addMaintenanceLog(log: Maintenance) {
        client.postgrest["maintenance"].insert(log)
    }

    suspend fun getMaintenanceByVehicles(vehicleIds: List<String>): List<Maintenance> {
        return client.postgrest["maintenance"].select {
            filter {
                isIn("vehicle_id", vehicleIds)
            }
        }.decodeList<Maintenance>()
    }
}

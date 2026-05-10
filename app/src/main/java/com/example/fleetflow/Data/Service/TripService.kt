package com.example.fleetflow.Data.Service

import com.example.fleetflow.Data.Model.Trip
import io.github.jan.supabase.postgrest.postgrest

class TripService {
    private val client = SupabaseClient.client

    suspend fun recordTrip(trip: Trip) {
        client.postgrest["trips"].insert(trip)
    }

    suspend fun getTripsByVehicle(vehicleId: String): List<Trip> {
        return client.postgrest["trips"].select {
            filter {
                eq("vehicle_id", vehicleId)
            }
        }.decodeList<Trip>()
    }

    suspend fun getTripsByDriver(driverId: String): List<Trip> {
        return client.postgrest["trips"].select {
            filter {
                eq("driver_id", driverId)
            }
        }.decodeList<Trip>()
    }

    suspend fun getTripsByVehicles(vehicleIds: List<String>): List<Trip> {
        return client.postgrest["trips"].select {
            filter {
                isIn("vehicle_id", vehicleIds)
            }
        }.decodeList<Trip>()
    }

    suspend fun getTripsByOwner(ownerId: String): List<Trip> {
        // This requires a join or owner_id column in trips table. 
        // If owner_id isn't in trips, we fetch vehicles then trips.
        // Let's ensure the fallback in ViewModel is robust.
        return emptyList()
    }
}

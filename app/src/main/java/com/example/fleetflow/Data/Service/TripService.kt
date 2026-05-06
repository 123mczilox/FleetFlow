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
}

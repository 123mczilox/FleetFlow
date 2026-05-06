package com.example.fleetflow.Data.Repository

import com.example.fleetflow.Data.Model.Trip
import com.example.fleetflow.Data.Service.TripService

class TripRepository(private val tripService: TripService) {
    suspend fun recordTrip(trip: Trip) = tripService.recordTrip(trip)
    suspend fun getTripsByVehicle(vehicleId: String) = tripService.getTripsByVehicle(vehicleId)
    suspend fun getTripsByDriver(driverId: String) = tripService.getTripsByDriver(driverId)
}

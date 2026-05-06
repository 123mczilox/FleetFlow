package com.example.fleetflow.Data.Model

import kotlinx.serialization.Serializable

@Serializable
data class Trip(
    val id: String? = null,
    val vehicle_id: String,
    val driver_id: String,
    val trips_count: Int,
    val revenue: Double,
    val created_at: String? = null,
    val is_locked: Boolean = false
)



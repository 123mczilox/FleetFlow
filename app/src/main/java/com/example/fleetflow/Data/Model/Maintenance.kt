package com.example.fleetflow.Data.Model

import kotlinx.serialization.Serializable

@Serializable
data class Maintenance(
    val id: String? = null,
    val vehicle_id: String,
    val service_type: String,
    val cost: Double,
    val description: String?,
    val date: String? = null,
    val created_at: String? = null
)

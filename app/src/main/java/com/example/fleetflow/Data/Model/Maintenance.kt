package com.example.fleetflow.Data.Model

import kotlinx.serialization.Serializable

@Serializable
data class Maintenance(
    val id: String? = null,
    val vehicle_id: String,
    val date: String,
    val description: String? = null,
    val cost: Double = 0.0,
    val created_at: String? = null
)

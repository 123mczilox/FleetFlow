package com.example.fleetflow.Data.Model

import kotlinx.serialization.Serializable

import java.util.UUID

@Serializable
data class Maintenance(
    val id: String = UUID.randomUUID().toString(),
    val vehicle_id: String,
    val owner_id: String,
    val service_type: String,
    val cost: Double,
    val description: String? = null,
    val date: String,
    val created_at: String? = null
)

package com.example.fleetflow.Data.Model

import kotlinx.serialization.Serializable

@Serializable
data class Report(
    val id: String? = null,
    val driver_id: String,
    val vehicle_id: String,
    val owner_id: String,
    val title: String,
    val description: String,
    val status: String = "pending", // pending, reviewed, resolved
    val created_at: String? = null
)

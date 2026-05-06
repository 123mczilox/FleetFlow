package com.example.fleetflow.Data.Model

import kotlinx.serialization.Serializable

@Serializable
data class Maintenance(
    val id: Int,
    val vehicle_id: Int,
    val date: String,

    val created_at: String,
)

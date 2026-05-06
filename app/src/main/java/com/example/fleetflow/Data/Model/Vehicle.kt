package com.example.fleetflow.Data.Model

import kotlinx.serialization.Serializable

@Serializable
data class Vehicle(
    val id: String,
    val plate_number: String,
    val fleet_number:String,
    val route: String?,
    val owner_id: String,
    val assigned_driver_id: String?

)

package com.example.fleetflow.Data.Model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val full_name: String,
    val role: String,//owner or driver
    val email: String,
    val created_at: String
)

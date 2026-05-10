package com.example.fleetflow.Data.Service

import com.example.fleetflow.Data.Model.User
import io.github.jan.supabase.postgrest.postgrest

class DriverService {
    private val client = SupabaseClient.client

    suspend fun getAllDrivers(): List<User> {
        return client.postgrest["users_profile"]
            .select {
                filter {
                    eq("role", "driver")
                }
            }
            .decodeList<User>()
    }

    suspend fun getDriverById(driverId: String): User {
        return client.postgrest["users_profile"]
            .select {
                filter {
                    eq("id", driverId)
                }
            }
            .decodeSingle<User>()
    }
}

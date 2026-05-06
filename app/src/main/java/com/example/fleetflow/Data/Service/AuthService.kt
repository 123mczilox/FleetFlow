package com.example.fleetflow.Data.Service

import com.example.fleetflow.Data.Model.User
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest

class AuthService {
    private val client = SupabaseClient.client

    suspend fun signUp(email: String, password: String, fullName: String, role: String) {
        client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }

        val userId = client.auth.currentUserOrNull()?.id ?: throw Exception("User creation failed")

        // Create the user profile in the database
        client.postgrest["users_profile"].insert(
            mapOf(
                "id" to userId,
                "full_name" to fullName,
                "role" to role,
                "email" to email
            )
        )
    }

    suspend fun signIn(email: String, password: String): User {
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }

        val userId = client.auth.currentUserOrNull()?.id ?: throw Exception("Login failed")
        
        return client.postgrest["users_profile"]
            .select {
                filter {
                    eq("email", email)
                }
            }
            .decodeSingle<User>()
    }

    suspend fun signOut() {
        client.auth.signOut()
    }
    
    fun getCurrentUserId(): String? = client.auth.currentUserOrNull()?.id
}

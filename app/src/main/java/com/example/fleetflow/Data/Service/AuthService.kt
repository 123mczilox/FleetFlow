package com.example.fleetflow.Data.Service

import com.example.fleetflow.Data.Model.User
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest

class AuthService {
    private val client = SupabaseClient.client

    suspend fun signUp(email: String, password: String, fullName: String, role: String) {
        try {
            client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
        } catch (e: Exception) {
            // If user already exists in Auth, we still try to create/update the profile
            if (e.message?.contains("already registered", ignoreCase = true) != true) {
                throw e
            }
        }

        val userId = client.auth.currentUserOrNull()?.id 
            ?: client.auth.retrieveUserForCurrentSession().id

        // Create the user profile in the database
        // Using upsert ensures that if the profile exists, it updates; if not, it creates.
        client.postgrest["users_profile"].upsert(
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
                    eq("id", userId)
                }
            }
            .decodeSingle<User>()
    }

    suspend fun signOut() {
        client.auth.signOut()
    }
    
    fun getCurrentUserId(): String? = client.auth.currentUserOrNull()?.id
}

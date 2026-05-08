package com.example.fleetflow.Data.Service

import com.example.fleetflow.Data.Model.User
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest

class AuthService {
    private val client = SupabaseClient.client

    suspend fun signUp(email: String, password: String, fullName: String, role: String, phoneNumber: String): User {
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
        val userProfile = User(
            id = userId,
            full_name = fullName,
            role = role,
            email = email,
            phone_number = phoneNumber
        )

        client.postgrest["users_profile"].upsert(userProfile)
        
        return userProfile
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

    suspend fun resetPassword(email: String) {
        client.auth.resetPasswordForEmail(email)
    }

    suspend fun signOut() {
        client.auth.signOut()
    }
    
    suspend fun getUserProfile(userId: String): User {
        return client.postgrest["users_profile"]
            .select {
                filter {
                    eq("id", userId)
                }
            }
            .decodeSingle<User>()
    }

    suspend fun getAllDrivers(): List<User> {
        return client.postgrest["users_profile"]
            .select {
                filter {
                    eq("role", "driver")
                }
            }
            .decodeList<User>()
    }
}

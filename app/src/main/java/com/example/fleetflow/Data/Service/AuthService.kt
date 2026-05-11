package com.example.fleetflow.Data.Service

import com.example.fleetflow.Data.Model.User
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest

class AuthService {
    private val client = SupabaseClient.client

    suspend fun signUp(email: String, password: String, fullName: String, role: String, phoneNumber: String): User {
        android.util.Log.d("FleetFlowAuth", "Starting signUp for $email with role $role")
        try {
            client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            android.util.Log.d("FleetFlowAuth", "Supabase Auth signUp successful")
        } catch (e: Exception) {
            android.util.Log.e("FleetFlowAuth", "Supabase Auth signUp error: ${e.message}")
            // If user already exists in Auth, we still try to create/update the profile
            if (e.message?.contains("already registered", ignoreCase = true) != true) {
                throw e
            }
        }

        val userId = client.auth.currentUserOrNull()?.id 
            ?: client.auth.retrieveUserForCurrentSession().id
        
        android.util.Log.d("FleetFlowAuth", "User ID obtained: $userId")

        // Create the user profile in the database
        val userProfile = User(
            id = userId,
            full_name = fullName,
            role = role.lowercase().trim(),
            email = email,
            phone_number = phoneNumber
        )

        try {
            client.postgrest["users_profile"].upsert(userProfile)
            android.util.Log.d("FleetFlowAuth", "User profile upserted successfully: $userProfile")
        } catch (e: Exception) {
            android.util.Log.e("FleetFlowAuth", "Error upserting user profile: ${e.message}")
            throw e
        }
        
        return userProfile
    }

    suspend fun signIn(email: String, password: String): User {
        android.util.Log.d("FleetFlowAuth", "Starting signIn for $email")
        try {
            client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            android.util.Log.d("FleetFlowAuth", "Supabase Auth signIn successful")
        } catch (e: Exception) {
            android.util.Log.e("FleetFlowAuth", "Supabase Auth signIn error: ${e.message}")
            throw e
        }

        val userId = client.auth.currentUserOrNull()?.id ?: throw Exception("Login failed: User ID not found")
        android.util.Log.d("FleetFlowAuth", "Fetching profile for userId: $userId")
        
        return try {
            val profile = client.postgrest["users_profile"]
                .select {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingleOrNull<User>()
            
            if (profile == null) {
                android.util.Log.e("FleetFlowAuth", "Profile not found for userId: $userId")
                throw Exception("User profile not found. Please contact support.")
            }
            
            android.util.Log.d("FleetFlowAuth", "Profile fetched successfully: $profile")
            profile
        } catch (e: Exception) {
            android.util.Log.e("FleetFlowAuth", "Error fetching profile: ${e.message}")
            throw e
        }
    }

    suspend fun resetPassword(email: String) {
        client.auth.resetPasswordForEmail(email)
    }

    suspend fun signOut() {
        client.auth.signOut()
    }
    
    suspend fun getUserProfile(userId: String): User {
        android.util.Log.d("FleetFlowAuth", "getUserProfile for: $userId")
        return try {
            val profile = client.postgrest["users_profile"]
                .select {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingleOrNull<User>()
            
            if (profile == null) {
                android.util.Log.e("FleetFlowAuth", "getUserProfile: Profile is null for $userId")
                throw Exception("Profile not found")
            }
            android.util.Log.d("FleetFlowAuth", "getUserProfile: Success")
            profile
        } catch (e: Exception) {
            android.util.Log.e("FleetFlowAuth", "getUserProfile error: ${e.message}")
            throw e
        }
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

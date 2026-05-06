package com.example.fleetflow.Data.Repository

import com.example.fleetflow.Data.Model.User
import com.example.fleetflow.Data.Service.AuthService

class AuthRepository(private val authService: AuthService = AuthService()) {

    suspend fun signUp(email: String, password: String, fullName: String, role: String): Result<Unit> {
        return try {
            authService.signUp(email, password, fullName, role)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, password: String): Result<User> {
        return try {
            val user = authService.signIn(email, password)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut(): Result<Unit> {
        return try {
            authService.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentUserId(): String? = authService.getCurrentUserId()
}

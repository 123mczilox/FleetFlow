package com.example.fleetflow.ui.Auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fleetflow.Data.Model.User
import com.example.fleetflow.Data.Repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.fold

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            repository.signIn(email, password).fold(
                onSuccess = { user ->
                    _uiState.value = AuthUiState.Success(user)
                },
                onFailure = { error ->
                    _uiState.value = AuthUiState.Error(error.message ?: "Login failed")
                }
            )
        }
    }

    fun signUp(email: String, password: String, fullName: String, role: String, phoneNumber: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            repository.signUp(email, password, fullName, role, phoneNumber).fold(
                onSuccess = { user ->
                    _uiState.value = AuthUiState.Success(user)
                },
                onFailure = { error ->
                    _uiState.value = AuthUiState.Error(error.message ?: "Registration failed")
                }
            )
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            repository.resetPassword(email).fold(
                onSuccess = {
                    _uiState.value = AuthUiState.ResetPasswordSent
                },
                onFailure = { error ->
                    _uiState.value = AuthUiState.Error(error.message ?: "Reset failed")
                }
            )
        }
    }

    fun clearState() {
        _uiState.value = AuthUiState.Idle
    }
}

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val user: User) : AuthUiState()
    object ResetPasswordSent : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

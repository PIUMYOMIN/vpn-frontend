package com.piu.vpn.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piu.vpn.ui.state.ForgotPasswordUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ForgotPasswordViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email.trim(), error = null) }
    }

    fun submit() {
        val state = _uiState.value
        if (state.email.isBlank()) {
            _uiState.update { it.copy(error = "Email ထည့်ပါ") }
            return
        }
        if (!state.email.contains("@")) {
            _uiState.update { it.copy(error = "Email ပုံစံ မှန်ကန်မှု မ ရှိ") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                // TODO: POST /auth/forgot-password { email }
                // Backend က reset link ပါ email ပို့ပါမည်
                delay(1200)

                _uiState.update { it.copy(isLoading = false, sent = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Network error")
                }
            }
        }
    }
}
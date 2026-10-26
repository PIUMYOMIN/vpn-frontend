package com.piu.vpn.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piu.vpn.ui.state.ResetPasswordUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ResetPasswordViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ResetPasswordUiState())
    val uiState: StateFlow<ResetPasswordUiState> = _uiState.asStateFlow()

    fun setToken(token: String) {
        _uiState.update { it.copy(token = token) }
    }

    fun onNewPasswordChange(pwd: String) {
        _uiState.update { it.copy(newPassword = pwd, error = null) }
    }

    fun onConfirmPasswordChange(pwd: String) {
        _uiState.update { it.copy(confirmPassword = pwd, error = null) }
    }

    fun submit() {
        val state = _uiState.value

        when {
            state.token.isBlank() -> {
                _uiState.update { it.copy(error = "Token မ ရှိပါ — email link ကို ပြန်နှိပ်ပါ") }
                return
            }
            state.newPassword.length < 8 -> {
                _uiState.update { it.copy(error = "Password အနည်းဆုံး ၈ လုံး ဖြစ်ရမည်") }
                return
            }
            !isStrongPassword(state.newPassword) -> {
                _uiState.update { it.copy(error = "Password မှာ အကြီး၊ အသေး၊ နံပါတ် ပါရမည်") }
                return
            }
            state.newPassword != state.confirmPassword -> {
                _uiState.update { it.copy(error = "Password ၂ ခု မ ကိုက်ညီပါ") }
                return
            }
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                // TODO: POST /auth/reset-password { token, new_password }
                delay(1200)

                _uiState.update { it.copy(isLoading = false, success = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Network error")
                }
            }
        }
    }

    private fun isStrongPassword(pwd: String): Boolean =
        pwd.any { it.isUpperCase() } &&
                pwd.any { it.isLowerCase() } &&
                pwd.any { it.isDigit() }
}
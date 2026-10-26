package com.piu.vpn.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.piu.vpn.data.api.ApiClient
import com.piu.vpn.data.repository.ApiResult
import com.piu.vpn.data.repository.AuthRepository
import com.piu.vpn.ui.state.AuthUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepo = AuthRepository(ApiClient.tokenStore)
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email.trim(), error = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, error = null) }
    }

    fun toggleMode() {
        _uiState.update {
            it.copy(
                isRegisterMode = !it.isRegisterMode,
                error = null,
                password = "",
            )
        }
    }

    fun submit() {
        val state = _uiState.value
        val error = validate(state)
        if (error != null) {
            _uiState.update { it.copy(error = error) }
            return
        }
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val result = if (state.isRegisterMode) {
                authRepo.register(state.email, state.password)
            } else {
                authRepo.login(state.email, state.password)
            }
            when (result) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isLoading = false, success = true)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = humanizeError(result.message, result.code),
                    )
                }
                ApiResult.NetworkError -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Network error — internet ချိတ်ဆက်မှု စစ်ပါ",
                    )
                }
            }
        }
    }

    fun resetSuccess() {
        _uiState.update { it.copy(success = false) }
    }

    private fun validate(state: AuthUiState): String? = when {
        state.email.isBlank() -> "Email ထည့်ပါ"
        !state.email.contains("@") -> "Email ပုံစံ မှန်ကန်မှု မ ရှိ"
        state.password.length < 8 -> "Password အနည်းဆုံး ၈ လုံး"
        state.isRegisterMode && !isStrongPassword(state.password) ->
            "Password မှာ အကြီး၊ အသေး၊ နံပါတ် ပါရမည်"
        else -> null
    }

    private fun isStrongPassword(pwd: String): Boolean =
        pwd.any { it.isUpperCase() } &&
        pwd.any { it.isLowerCase() } &&
        pwd.any { it.isDigit() }

    private fun humanizeError(code: String, http: Int?): String = when {
        code == "invalid_credentials" -> "Email သို့ Password မှား"
        code == "email_exists" -> "ဒီ email ကို သုံးပြီးသား"
        code == "weak_password" -> "Password အားနည်းနေတယ်"
        code == "account_banned" -> "Account ကို ပိတ်ထားတယ်"
        code == "account_locked" -> "ကြိုးစားမှု များလွန်း — ၁၅ မိနစ် စောင့်ပါ"
        code == "rate_limited" -> "ခဏနေမှ ပြန်ကြိုးစားပါ"
        http == 429 -> "ခဏနေမှ ပြန်ကြိုးစားပါ"
        else -> code
    }
}
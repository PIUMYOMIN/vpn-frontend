package com.piu.vpn.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.piu.vpn.data.api.ApiClient
import com.piu.vpn.data.repository.AccountRepository
import com.piu.vpn.data.repository.ApiResult
import com.piu.vpn.data.repository.VpnRepository
import com.piu.vpn.ui.state.AccountUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AccountViewModel(application: Application) : AndroidViewModel(application) {

    private val accountRepo = AccountRepository(ApiClient.tokenStore)
    private val vpnRepo = VpnRepository()
    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    // init {
    //     loadAccount()
    // }

    fun loadAccount() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    installId = ApiClient.tokenStore.installId,
                    isGuest = !ApiClient.tokenStore.hasToken(),
                )
            }

            // User info (logged in)
            if (ApiClient.tokenStore.hasToken()) {
                when (val r = accountRepo.getMe()) {
                    is ApiResult.Success -> {
                        val u = r.data
                        _uiState.update {
                            it.copy(
                                user = com.piu.vpn.data.User(
                                    id = u.id,
                                    email = u.email,
                                    role = u.role,
                                    emailVerified = u.emailVerified,
                                    createdAt = u.createdAt,
                                    lastLoginAt = u.lastLoginAt,
                                ),
                                isGuest = false,
                            )
                        }
                    }
                    else -> { /* keep guest state */ }
                }
            }

            // Entitlement
            when (val r = vpnRepo.getEntitlement()) {
                is ApiResult.Success -> {
                    val d = r.data
                    _uiState.update {
                        it.copy(
                            entitlement = com.piu.vpn.data.Entitlement(
                                active = d.active,
                                expiresAt = d.expiresAt,
                                remainingSeconds = d.remainingSeconds,
                                grantedBy = d.grantedBy,
                            ),
                        )
                    }
                }
                else -> { /* keep */ }
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun logout() {
        viewModelScope.launch {
            accountRepo.logout()
            _uiState.update { it.copy(logoutSuccess = true) }
        }
    }

    fun deleteAccount(password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val r = accountRepo.deleteAccount(password)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isLoading = false, deleteSuccess = true)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, error = r.message)
                }
                ApiResult.NetworkError -> _uiState.update {
                    it.copy(isLoading = false, error = "Network error")
                }
            }
        }
    }

    fun resetLogoutSuccess() = _uiState.update { it.copy(logoutSuccess = false) }
    fun resetDeleteSuccess() = _uiState.update { it.copy(deleteSuccess = false) }
}
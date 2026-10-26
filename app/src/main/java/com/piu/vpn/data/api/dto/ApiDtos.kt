package com.piu.vpn.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ============================================================
// Request DTOs
// ============================================================

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    @SerialName("install_id") val installId: String? = null,
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class RefreshRequest(
    @SerialName("refresh_token") val refreshToken: String,
)

@Serializable
data class LogoutRequest(
    @SerialName("refresh_token") val refreshToken: String,
)

@Serializable
data class DeleteAccountRequest(
    val password: String,
)

@Serializable
data class ForgotPasswordRequest(
    val email: String,
)

@Serializable
data class ResetPasswordRequest(
    val token: String,
    @SerialName("new_password") val newPassword: String,
)

// ============================================================
// Response DTOs
// ============================================================

@Serializable
data class TokenPairResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("expires_at") val expiresAt: String,
)

@Serializable
data class UserResponse(
    val id: String,
    val email: String,
    val role: String = "user",
    @SerialName("email_verified") val emailVerified: Boolean = false,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("last_login_at") val lastLoginAt: String? = null,
)

@Serializable
data class EntitlementResponse(
    val active: Boolean,
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("remaining_seconds") val remainingSeconds: Long = 0,
    @SerialName("granted_by") val grantedBy: String? = null,
)

@Serializable
data class VpnConfigResponse(
    val config: String,
    @SerialName("allowed_ip") val allowedIp: String,
    val endpoint: String,
)

@Serializable
data class DeviceResponse(
    val id: String,
    @SerialName("device_fingerprint") val deviceFingerprint: String,
    @SerialName("device_name") val deviceName: String? = null,
    val platform: String? = null,
    @SerialName("last_seen_at") val lastSeenAt: String = "",
    @SerialName("created_at") val createdAt: String = "",
)

@Serializable
data class DeviceListResponse(
    val devices: List<DeviceResponse> = emptyList(),
)

@Serializable
data class StatusResponse(
    val status: String,
)

@Serializable
data class ErrorResponse(
    val error: String,
)
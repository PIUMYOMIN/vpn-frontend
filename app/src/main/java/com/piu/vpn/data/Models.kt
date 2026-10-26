package com.piu.vpn.data

// ============================================================
// User & Auth
// ============================================================

data class User(
    val id: String,
    val email: String,
    val role: String = "user",
    val emailVerified: Boolean = false,
    val createdAt: String = "",
    val lastLoginAt: String? = null,
)

data class TokenPair(
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: String,
)

// ============================================================
// Entitlement
// ============================================================

data class Entitlement(
    val active: Boolean,
    val expiresAt: String? = null,
    val remainingSeconds: Long = 0,
    val grantedBy: String? = null,
)

// ============================================================
// VPN
// ============================================================

data class VpnConfig(
    val config: String,
    val allowedIp: String,
    val endpoint: String,
)

data class VpnStats(
    val bytesRx: Long = 0,
    val bytesTx: Long = 0,
    val connectedAt: Long = 0,
)
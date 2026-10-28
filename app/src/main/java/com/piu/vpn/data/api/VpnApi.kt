package com.piu.vpn.data.api

import com.piu.vpn.data.api.dto.*
import retrofit2.Response
import retrofit2.http.*

interface VpnApi {

    // ============================================================
    // Public Auth
    // ============================================================

    @POST("/auth/register")
    suspend fun register(@Body req: RegisterRequest): Response<TokenPairResponse>

    @POST("/auth/login")
    suspend fun login(@Body req: LoginRequest): Response<TokenPairResponse>

    @POST("/auth/refresh")
    suspend fun refresh(@Body req: RefreshRequest): Response<TokenPairResponse>

    @POST("/auth/logout")
    suspend fun logout(@Body req: LogoutRequest): Response<StatusResponse>

    // ============================================================
    // Password Reset (backend TODO)
    // ============================================================

    @POST("/auth/forgot-password")
    suspend fun forgotPassword(@Body req: ForgotPasswordRequest): Response<StatusResponse>

    @POST("/auth/reset-password")
    suspend fun resetPassword(@Body req: ResetPasswordRequest): Response<StatusResponse>

    // ============================================================
    // User
    // ============================================================

    @GET("/api/me")
    suspend fun me(): Response<UserResponse>

    @DELETE("/auth/account")
    suspend fun deleteAccount(@Body req: DeleteAccountRequest): Response<StatusResponse>

    @GET("/api/devices")
    suspend fun devices(): Response<DeviceListResponse>

    @DELETE("/api/devices/{id}")
    suspend fun deleteDevice(@Path("id") id: String): Response<StatusResponse>

    // ============================================================
    // Entitlement + VPN
    // ============================================================

    @GET("/api/entitlement")
    suspend fun entitlement(): Response<EntitlementResponse>

    // Request the server-controlled rare fallback entitlement after an AdMob no-fill/load failure.
    @POST("/api/entitlement/fallback")
    suspend fun fallbackEntitlement(): Response<EntitlementResponse>

    @GET("/api/vpn/config")
    suspend fun vpnConfig(): Response<VpnConfigResponse>

    // ============================================================
    // Health
    // ============================================================

    @GET("/healthz")
    suspend fun health(): Response<String>
}
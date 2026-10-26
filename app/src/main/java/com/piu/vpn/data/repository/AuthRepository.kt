package com.piu.vpn.data.repository

import com.piu.vpn.data.api.ApiClient
import com.piu.vpn.data.api.dto.*
import com.piu.vpn.data.local.TokenStore
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.Response
import java.io.IOException

class AuthRepository(
    private val tokenStore: TokenStore,
) {
    private val api = ApiClient.api
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun register(email: String, password: String): ApiResult<TokenPairResponse> {
        return call {
            api.register(
                RegisterRequest(
                    email = email,
                    password = password,
                    installId = tokenStore.installId,  // guest → user transfer
                )
            )
        }.also { result ->
            if (result is ApiResult.Success) tokenStore.savePair(result.data)
        }
    }

    suspend fun login(email: String, password: String): ApiResult<TokenPairResponse> {
        return call { api.login(LoginRequest(email, password)) }
            .also { result ->
                if (result is ApiResult.Success) tokenStore.savePair(result.data)
            }
    }

    suspend fun logout(): ApiResult<Unit> {
        val refresh = tokenStore.refreshToken ?: return ApiResult.Success(Unit)
        return try {
            api.logout(LogoutRequest(refresh))
            tokenStore.clear()
            ApiResult.Success(Unit)
        } catch (e: IOException) {
            // Network fail ဖြစ်ရင်တောင် local token ကို ရှင်း
            tokenStore.clear()
            ApiResult.NetworkError
        } catch (e: Exception) {
            tokenStore.clear()
            ApiResult.Error(e.message ?: "Logout failed")
        }
    }

    suspend fun forgotPassword(email: String): ApiResult<Unit> {
        return callUnit { api.forgotPassword(ForgotPasswordRequest(email)) }
    }

    suspend fun resetPassword(token: String, newPassword: String): ApiResult<Unit> {
        return callUnit {
            api.resetPassword(ResetPasswordRequest(token, newPassword))
        }
    }

    fun isLoggedIn(): Boolean = tokenStore.hasToken()

    // ============================================================
    // Helpers
    // ============================================================

    private suspend fun <T> call(block: suspend () -> Response<T>): ApiResult<T> {
        return try {
            val resp = block()
            val body = resp.body()
            if (resp.isSuccessful && body != null) {
                ApiResult.Success(body)
            } else {
                ApiResult.Error(parseError(resp), resp.code())
            }
        } catch (e: IOException) {
            ApiResult.NetworkError
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Unknown error")
        }
    }

    private suspend fun callUnit(block: suspend () -> Response<*>): ApiResult<Unit> {
        return try {
            val resp = block()
            if (resp.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error(parseError(resp), resp.code())
            }
        } catch (e: IOException) {
            ApiResult.NetworkError
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Unknown error")
        }
    }

    private fun parseError(resp: Response<*>): String {
        val raw = resp.errorBody()?.string() ?: return "HTTP ${resp.code()}"
        return try {
            json.parseToJsonElement(raw).jsonObject["error"]
                ?.jsonPrimitive?.content ?: raw
        } catch (e: Exception) {
            raw
        }
    }
}